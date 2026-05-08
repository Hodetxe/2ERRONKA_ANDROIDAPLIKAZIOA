package com.example.androidapp.presentation.txata

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.core.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.SecureRandom
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    var messages = mutableStateListOf<Message>()
        private set

    var isConnected by mutableStateOf(false)
        private set

    var connectionError by mutableStateOf<String?>(null)
        private set

    private var socket: Socket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private var isListening = false

    private val secureRandom = SecureRandom()
    private var sessionKey: SecretKeySpec? = null

    // Server configuration
    private val serverIp get() = ApiClient.CHAT_HOST
    private val serverPort get() = ApiClient.CHAT_PORT

    private val motaHello: Byte = 1
    private val motaGakoa: Byte = 2
    private val motaTestua: Byte = 3
    private val motaFitxategiHasiera: Byte = 4
    private val motaFitxategiZatia: Byte = 5
    private val motaFitxategiAmaiera: Byte = 6

    private val maxPacketBytes = 16 * 1024 * 1024

    private data class FileMeta(
        val idKey: String,
        val fileName: String,
        val mimeType: String?,
        val sizeBytes: Long?
    )

    private data class ReceivingFile(
        val meta: FileMeta,
        val tempFile: File,
        val outputStream: FileOutputStream,
        var receivedBytes: Long
    )

    private val receivingFiles = mutableMapOf<String, ReceivingFile>()
    private val maxFileBytes: Long = 10L * 1024L * 1024L
    private val pendingEchoLock = Any()
    private val pendingOwnEchoes = ArrayDeque<String>()

    fun setUiError(message: String) {
        connectionError = message
    }

    fun clearConnectionError() {
        connectionError = null
    }

    fun connect() {
        if (isConnected) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                connectionError = null
                socket = Socket(serverIp, serverPort)
                val rawIn = socket?.getInputStream()
                val rawOut = socket?.getOutputStream()

                if (rawIn == null || rawOut == null) throw IllegalStateException("Stream-ak null dira")

                input = BufferedInputStream(rawIn)
                output = BufferedOutputStream(rawOut)

                val (privateKey, publicKey) = sortuRsaGakoak()
                val pem = publicKeyToPem(publicKey)
                bidaliPaketea(motaHello, pem.toByteArray(Charsets.UTF_8))

                val keyPacket = irakurriPaketea() ?: throw IllegalStateException("Ez da gako-paketerik jaso")
                if (keyPacket.first != motaGakoa) throw IllegalStateException("Gako-paketea espero zen")

                val aesKey = decryptAesKey(privateKey, keyPacket.second)
                sessionKey = SecretKeySpec(aesKey, "AES")

                val username = SessionManager.currentUser?.izena ?: "AndroidUser"
                bidaliTestuaZifratuta("$username txat-ean sartu da!")

                launch(Dispatchers.Main) {
                    isConnected = true
                    connectionError = null
                }

                isListening = true
                startListening()
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    connectionError = "Ezin da konektatu: ${e.message}"
                    isConnected = false
                }
            }
        }
    }

    private fun startListening() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (isListening) {
                    val packet = irakurriPaketea() ?: break

                    when (packet.first) {
                        motaTestua -> {
                            val plaintext = desenkriptatuTestua(packet.second) ?: continue
                            if (!shouldSkipOwnEcho(plaintext)) {
                                val message = parseMessage(plaintext)
                                launch(Dispatchers.Main) {
                                    messages.add(message)
                                }
                            }
                        }
                        motaFitxategiHasiera -> handleFileStart(packet.second)
                        motaFitxategiZatia -> handleFileChunk(packet.second)
                        motaFitxategiAmaiera -> handleFileEnd(packet.second)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                disconnect()
            }
        }
    }

    private fun parseMessage(text: String): Message {
        val currentUser = SessionManager.currentUser?.izena ?: "AndroidUser"
        
        // Check if message starts with "Username:"
        val parts = text.split(":", limit = 2)
        val senderName = if (parts.size > 1) parts[0].trim() else ""
        val content = if (parts.size > 1) parts[1].trim() else text
        
        // Determine if it's me
        val isMe = senderName.equals(currentUser, ignoreCase = true)
        
        // Determine icon based on sender name (heuristic)
        val icon = when {
            senderName.contains("Sukaldari", ignoreCase = true) -> Icons.Default.Restaurant
            senderName.contains("Zerbitzari", ignoreCase = true) -> Icons.Default.RoomService
            else -> Icons.Default.Person
        }

        return Message(
            id = System.currentTimeMillis().toInt(),
            text = text, // Display full text for now to match TPV behavior if needed, or just content
            isMe = isMe,
            senderIcon = icon
        )
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (!isConnected) {
            connectionError = "Ez dago konektatuta."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val username = SessionManager.currentUser?.izena ?: "AndroidUser"
                val messageToSend = "$username: $text".trim()

                synchronized(pendingEchoLock) {
                    pendingOwnEchoes.addLast(messageToSend)
                    while (pendingOwnEchoes.size > 50) pendingOwnEchoes.removeFirst()
                }

                launch(Dispatchers.Main) {
                    messages.add(
                        Message(
                            id = System.currentTimeMillis().toInt(),
                            text = messageToSend,
                            isMe = true,
                            senderIcon = Icons.Default.Person
                        )
                    )
                }

                bidaliTestuaZifratuta(messageToSend)
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    connectionError = "Errorea mezua bidaltzean: ${e.message}"
                }
            }
        }
    }

    fun sendFile(uri: Uri) {
        if (!isConnected) {
            connectionError = "Ez dago konektatuta."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (output == null) {
                launch(Dispatchers.Main) {
                    connectionError = "Ez dago konektatuta."
                }
                return@launch
            }

            val app = getApplication<Application>()
            val contentResolver = app.contentResolver

            val (fileName, sizeBytes, mimeType) = buildOutgoingFileInfo(uri) ?: run {
                launch(Dispatchers.Main) { connectionError = "Ezin da fitxategia irakurri." }
                return@launch
            }

            if (sizeBytes != null && sizeBytes > maxFileBytes) {
                launch(Dispatchers.Main) {
                    connectionError = "Fitxategia handiegia da (${sizeBytes} bytes)."
                }
                return@launch
            }

            try {
                val idBytes = ByteArray(16)
                secureRandom.nextBytes(idBytes)
                val startPayload = sortuFitxategiHasieraPayload(idBytes, fileName, sizeBytes ?: -1L)
                bidaliPaketea(motaFitxategiHasiera, startPayload)

                contentResolver.openInputStream(uri).use { fileIn ->
                    if (fileIn == null) throw IllegalStateException("InputStream null")
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = fileIn.read(buffer)
                        if (read <= 0) break
                        val chunkPayload = ByteArray(16 + read)
                        System.arraycopy(idBytes, 0, chunkPayload, 0, 16)
                        System.arraycopy(buffer, 0, chunkPayload, 16, read)
                        bidaliPaketea(motaFitxategiZatia, chunkPayload)
                    }
                }

                bidaliPaketea(motaFitxategiAmaiera, idBytes)

                val sender = SessionManager.currentUser?.izena ?: "AndroidUser"
                launch(Dispatchers.Main) {
                    messages.add(
                        Message(
                            id = System.currentTimeMillis().toInt(),
                            text = "${sender}: [FITXATEGIA] $fileName",
                            isMe = true,
                            senderIcon = Icons.Default.Person,
                            isFile = true,
                            fileName = fileName,
                            fileUri = uri,
                            mimeType = mimeType
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    connectionError = "Errorea fitxategia bidaltzean: ${e.message}"
                }
            }
        }
    }

    fun disconnect() {
        isListening = false
        
        try {
            socket?.close()
            input?.close()
            output?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            receivingFiles.values.forEach {
                runCatching { it.outputStream.close() }
                runCatching { it.tempFile.delete() }
            }
        } catch (_: Exception) {
        } finally {
            receivingFiles.clear()
        }

        socket = null
        input = null
        output = null
        sessionKey = null
        
        viewModelScope.launch(Dispatchers.Main) {
            isConnected = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    private fun shouldSkipOwnEcho(plaintext: String): Boolean {
        val currentUser = SessionManager.currentUser?.izena ?: "AndroidUser"
        val parts = plaintext.split(":", limit = 2)
        val senderName = if (parts.size > 1) parts[0].trim() else ""
        if (!senderName.equals(currentUser, ignoreCase = true)) return false

        synchronized(pendingEchoLock) {
            val idx = pendingOwnEchoes.indexOf(plaintext)
            if (idx >= 0) {
                pendingOwnEchoes.removeAt(idx)
                return true
            }
        }

        return false
    }

    private fun handleFileStart(payload: ByteArray) {
        if (payload.size < 16 + 4 + 8) return

        val idBytes = payload.copyOfRange(0, 16)
        val idKey = android.util.Base64.encodeToString(idBytes, android.util.Base64.NO_WRAP)

        val nameLen = leInt(payload, 16)
        if (nameLen < 0) return

        val nameOffset = 16 + 4
        val sizeOffset = nameOffset + nameLen
        if (payload.size < sizeOffset + 8) return

        val fileName = String(payload, nameOffset, nameLen, Charsets.UTF_8).ifBlank { "fitxategia" }
        val sizeBytes = leLong(payload, sizeOffset).takeIf { it >= 0 }
        if (sizeBytes != null && sizeBytes > maxFileBytes) return

        val app = getApplication<Application>()
        val tempFile = File(app.cacheDir, "chatfile_$idKey")
        runCatching { tempFile.delete() }
        val outputStream = FileOutputStream(tempFile, true)

        runCatching { receivingFiles[idKey]?.outputStream?.close() }
        runCatching { receivingFiles[idKey]?.tempFile?.delete() }

        val meta = FileMeta(
            idKey = idKey,
            fileName = fileName,
            mimeType = null,
            sizeBytes = sizeBytes
        )
        receivingFiles[idKey] = ReceivingFile(meta, tempFile, outputStream, 0)
    }

    private fun handleFileChunk(payload: ByteArray) {
        if (payload.size < 16) return

        val idBytes = payload.copyOfRange(0, 16)
        val idKey = android.util.Base64.encodeToString(idBytes, android.util.Base64.NO_WRAP)
        val receiving = receivingFiles[idKey] ?: return

        val bytes = payload.copyOfRange(16, payload.size)
        val newTotal = receiving.receivedBytes + bytes.size
        if (receiving.meta.sizeBytes != null && newTotal > receiving.meta.sizeBytes) return
        if (newTotal > maxFileBytes) return

        try {
            receiving.outputStream.write(bytes)
            receiving.receivedBytes = newTotal
        } catch (_: Exception) {
        }
    }

    private fun handleFileEnd(payload: ByteArray) {
        if (payload.size < 16) return
        val idBytes = payload.copyOfRange(0, 16)
        val idKey = android.util.Base64.encodeToString(idBytes, android.util.Base64.NO_WRAP)

        val receiving = receivingFiles.remove(idKey) ?: return
        runCatching { receiving.outputStream.flush() }
        runCatching { receiving.outputStream.close() }

        val app = getApplication<Application>()
        val savedUri = runCatching {
            saveToDownloads(
                tempFile = receiving.tempFile,
                fileName = receiving.meta.fileName,
                mimeType = receiving.meta.mimeType
            )
        }.getOrNull()

        runCatching { receiving.tempFile.delete() }

        if (savedUri != null) {
            viewModelScope.launch(Dispatchers.Main) {
                messages.add(
                    Message(
                        id = System.currentTimeMillis().toInt(),
                        text = "Fitxategia: [FITXATEGIA] ${receiving.meta.fileName}",
                        isMe = false,
                        senderIcon = Icons.Default.Person,
                        isFile = true,
                        fileName = receiving.meta.fileName,
                        fileUri = savedUri,
                        mimeType = receiving.meta.mimeType
                    )
                )
            }
        }
    }

    private fun buildOutgoingFileInfo(uri: Uri): Triple<String, Long?, String?>? {
        val app = getApplication<Application>()
        val cr = app.contentResolver

        var fileName: String? = null
        var sizeBytes: Long? = null
        val mimeType = cr.getType(uri)

        runCatching {
            cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) fileName = c.getString(nameIdx)
                    if (sizeIdx >= 0) sizeBytes = c.getLong(sizeIdx).takeIf { it >= 0 }
                }
            }
        }

        val safeName = fileName?.takeIf { it.isNotBlank() } ?: "fitxategia"
        return Triple(
            safeName,
            sizeBytes?.takeIf { it > 0 },
            mimeType?.takeIf { it.isNotBlank() }
        )
    }

    private fun saveToDownloads(tempFile: File, fileName: String, mimeType: String?): Uri? {
        val app = getApplication<Application>()
        val resolver = app.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            if (!mimeType.isNullOrBlank()) put(MediaStore.Downloads.MIME_TYPE, mimeType)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(tempFile).use { input ->
                input.copyTo(out)
            }
        } ?: return null

        return uri
    }

    private fun sortuRsaGakoak(): Pair<PrivateKey, PublicKey> {
        val gen = KeyPairGenerator.getInstance("RSA")
        gen.initialize(2048)
        val pair = gen.generateKeyPair()
        return pair.private to pair.public
    }

    private fun publicKeyToPem(publicKey: PublicKey): String {
        val b64 = android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.NO_WRAP)
        val sb = StringBuilder()
        sb.append("-----BEGIN PUBLIC KEY-----\n")
        var i = 0
        while (i < b64.length) {
            sb.append(b64.substring(i, min(i + 64, b64.length))).append('\n')
            i += 64
        }
        sb.append("-----END PUBLIC KEY-----\n")
        return sb.toString()
    }

    private fun decryptAesKey(privateKey: PrivateKey, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encrypted)
    }

    private fun bidaliTestuaZifratuta(testua: String) {
        val payload = enkriptatuTestua(testua) ?: throw IllegalStateException("Ez dago saio-gakorik")
        bidaliPaketea(motaTestua, payload)
    }

    private fun enkriptatuTestua(testua: String): ByteArray? {
        val key = sessionKey ?: return null
        val nonce = ByteArray(12)
        secureRandom.nextBytes(nonce)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, nonce))
        val cipherPlusTag = cipher.doFinal(testua.toByteArray(Charsets.UTF_8))
        if (cipherPlusTag.size < 16) return null

        val cipherLen = cipherPlusTag.size - 16
        val ciphertext = cipherPlusTag.copyOfRange(0, cipherLen)
        val tag = cipherPlusTag.copyOfRange(cipherLen, cipherPlusTag.size)

        val payload = ByteArray(nonce.size + tag.size + ciphertext.size)
        System.arraycopy(nonce, 0, payload, 0, nonce.size)
        System.arraycopy(tag, 0, payload, nonce.size, tag.size)
        System.arraycopy(ciphertext, 0, payload, nonce.size + tag.size, ciphertext.size)
        return payload
    }

    private fun desenkriptatuTestua(payload: ByteArray): String? {
        val key = sessionKey ?: return null
        if (payload.size < 12 + 16) return null

        val nonce = payload.copyOfRange(0, 12)
        val tag = payload.copyOfRange(12, 28)
        val ciphertext = payload.copyOfRange(28, payload.size)

        val combined = ByteArray(ciphertext.size + tag.size)
        System.arraycopy(ciphertext, 0, combined, 0, ciphertext.size)
        System.arraycopy(tag, 0, combined, ciphertext.size, tag.size)

        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, nonce))
            val plaintext = cipher.doFinal(combined)
            String(plaintext, Charsets.UTF_8)
        }.getOrNull()
    }

    private fun bidaliPaketea(mota: Byte, payload: ByteArray) {
        val out = output ?: throw IllegalStateException("Ez dago konektatuta")
        if (payload.size > maxPacketBytes) throw IllegalArgumentException("Pakete handiegia")

        synchronized(out) {
            out.write(byteArrayOf(mota))
            out.write(intToLeBytes(payload.size))
            out.write(payload)
            out.flush()
        }
    }

    private fun irakurriPaketea(): Pair<Byte, ByteArray>? {
        val `in` = input ?: return null

        val typeInt = `in`.read()
        if (typeInt == -1) return null
        val lenBytes = readExact(`in`, 4) ?: return null
        val len = leInt(lenBytes, 0)
        if (len < 0 || len > maxPacketBytes) throw IllegalStateException("Pakete luzera baliogabea: $len")
        val payload = readExact(`in`, len) ?: return null
        return (typeInt.toByte() to payload)
    }

    private fun readExact(`in`: InputStream, size: Int): ByteArray? {
        if (size == 0) return ByteArray(0)
        val buf = ByteArray(size)
        var off = 0
        while (off < size) {
            val read = `in`.read(buf, off, size - off)
            if (read == -1) return null
            off += read
        }
        return buf
    }

    private fun intToLeBytes(value: Int): ByteArray {
        val b = ByteArray(4)
        b[0] = (value and 0xFF).toByte()
        b[1] = ((value ushr 8) and 0xFF).toByte()
        b[2] = ((value ushr 16) and 0xFF).toByte()
        b[3] = ((value ushr 24) and 0xFF).toByte()
        return b
    }

    private fun longToLeBytes(value: Long): ByteArray {
        val b = ByteArray(8)
        b[0] = (value and 0xFF).toByte()
        b[1] = ((value ushr 8) and 0xFF).toByte()
        b[2] = ((value ushr 16) and 0xFF).toByte()
        b[3] = ((value ushr 24) and 0xFF).toByte()
        b[4] = ((value ushr 32) and 0xFF).toByte()
        b[5] = ((value ushr 40) and 0xFF).toByte()
        b[6] = ((value ushr 48) and 0xFF).toByte()
        b[7] = ((value ushr 56) and 0xFF).toByte()
        return b
    }

    private fun leInt(bytes: ByteArray, offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun leLong(bytes: ByteArray, offset: Int): Long {
        return (bytes[offset].toLong() and 0xFF) or
            ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
            ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
            ((bytes[offset + 3].toLong() and 0xFF) shl 24) or
            ((bytes[offset + 4].toLong() and 0xFF) shl 32) or
            ((bytes[offset + 5].toLong() and 0xFF) shl 40) or
            ((bytes[offset + 6].toLong() and 0xFF) shl 48) or
            ((bytes[offset + 7].toLong() and 0xFF) shl 56)
    }

    private fun sortuFitxategiHasieraPayload(idBytes: ByteArray, fileName: String, sizeBytes: Long): ByteArray {
        val nameBytes = fileName.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(16 + 4 + nameBytes.size + 8)
        System.arraycopy(idBytes, 0, payload, 0, 16)
        val lenBytes = intToLeBytes(nameBytes.size)
        System.arraycopy(lenBytes, 0, payload, 16, 4)
        System.arraycopy(nameBytes, 0, payload, 20, nameBytes.size)
        val sizeLe = longToLeBytes(sizeBytes)
        System.arraycopy(sizeLe, 0, payload, 20 + nameBytes.size, 8)
        return payload
    }
}
