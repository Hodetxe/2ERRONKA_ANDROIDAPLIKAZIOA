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
import java.security.MessageDigest
import java.security.SecureRandom
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    var messages = mutableStateListOf<Message>()
        private set

    var isConnected by mutableStateOf(false)
        private set

    var connectionError by mutableStateOf<String?>(null)
        private set

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var isListening = false

    private val encryptionPrefix = "ENC:"
    private val fileMetaPrefix = "FILEMETA:"
    private val fileChunkPrefix = "FILECHUNK:"
    private val fileEndPrefix = "FILEEND:"
    private val secureRandom = SecureRandom()
    private val secretKey by lazy {
        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest("2ERRONKA_CHAT_PSK_v1".toByteArray(Charsets.UTF_8))
            .copyOf(16)
        SecretKeySpec(keyBytes, "AES")
    }

    // Server configuration
    private val serverIp get() = ApiClient.CHAT_HOST
    private val serverPort get() = ApiClient.CHAT_PORT

    private data class FileMeta(
        val id: String,
        val sender: String,
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

    fun connect() {
        if (isConnected) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                connectionError = null
                socket = Socket(serverIp, serverPort)
                val inputStream = socket?.getInputStream()
                val outputStream = socket?.getOutputStream()

                if (inputStream != null && outputStream != null) {
                    reader = BufferedReader(InputStreamReader(inputStream))
                    writer = PrintWriter(OutputStreamWriter(outputStream), true)

                    val username = SessionManager.currentUser?.izena ?: "AndroidUser"
                    writer?.println(encryptMessage("$username txat-ean sartu da!"))

                    launch(Dispatchers.Main) {
                        isConnected = true
                    }
                    
                    isListening = true
                    startListening()
                }
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
                    val line = reader?.readLine()
                    if (line != null) {
                        if (!handleFileProtocolLine(line)) {
                            val plaintext = decryptIfNeeded(line)
                            if (!shouldSkipOwnEcho(plaintext)) {
                                val message = parseMessage(plaintext)
                                launch(Dispatchers.Main) {
                                    messages.add(message)
                                }
                            }
                        }
                    } else {
                        // Server closed connection
                        disconnect()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

                writer?.println(encryptMessage(messageToSend))
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    connectionError = "Errorea mezua bidaltzean: ${e.message}"
                }
            }
        }
    }

    fun sendFile(uri: Uri) {
        if (!isConnected) return

        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val contentResolver = app.contentResolver

            val sender = SessionManager.currentUser?.izena ?: "AndroidUser"
            val meta = buildFileMeta(uri, sender)
                ?: run {
                    launch(Dispatchers.Main) {
                        connectionError = "Ezin da fitxategia irakurri."
                    }
                    return@launch
                }

            if (meta.sizeBytes != null && meta.sizeBytes > maxFileBytes) {
                launch(Dispatchers.Main) {
                    connectionError = "Fitxategia handiegia da (${meta.sizeBytes} bytes)."
                }
                return@launch
            }

            try {
                val metaLine = fileMetaPrefix + android.util.Base64.encodeToString(
                    metaToJson(meta).toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                )
                writer?.println(metaLine)

                contentResolver.openInputStream(uri).use { input ->
                    if (input == null) throw IllegalStateException("InputStream null")
                    val buffer = ByteArray(8 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        val chunkB64 = android.util.Base64.encodeToString(
                            buffer.copyOf(read),
                            android.util.Base64.NO_WRAP
                        )
                        writer?.println("$fileChunkPrefix${meta.id}:$chunkB64")
                    }
                }

                writer?.println("$fileEndPrefix${meta.id}")

                launch(Dispatchers.Main) {
                    messages.add(
                        Message(
                            id = System.currentTimeMillis().toInt(),
                            text = "${sender}: [FITXATEGIA] ${meta.fileName}",
                            isMe = true,
                            senderIcon = Icons.Default.Person,
                            isFile = true,
                            fileName = meta.fileName,
                            fileUri = uri,
                            mimeType = meta.mimeType
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
            reader?.close()
            writer?.close()
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
        reader = null
        writer = null
        
        viewModelScope.launch(Dispatchers.Main) {
            isConnected = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }

    private fun encryptMessage(plaintext: String): String {
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        val b64 = android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
        return "$encryptionPrefix$b64"
    }

    private fun decryptIfNeeded(input: String): String {
        if (!input.startsWith(encryptionPrefix)) return input

        return try {
            val b64 = input.removePrefix(encryptionPrefix)
            val combined = android.util.Base64.decode(b64, android.util.Base64.NO_WRAP)
            if (combined.size <= 12) return input

            val iv = combined.copyOfRange(0, 12)
            val ciphertext = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
            val plaintextBytes = cipher.doFinal(ciphertext)
            String(plaintextBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            input
        }
    }

    private fun handleFileProtocolLine(line: String): Boolean {
        return when {
            line.startsWith(fileMetaPrefix) -> {
                handleFileMeta(line.removePrefix(fileMetaPrefix))
                true
            }
            line.startsWith(fileChunkPrefix) -> {
                val payload = line.removePrefix(fileChunkPrefix)
                val sep = payload.indexOf(':')
                if (sep > 0) {
                    val id = payload.substring(0, sep)
                    val b64 = payload.substring(sep + 1)
                    handleFileChunk(id, b64)
                }
                true
            }
            line.startsWith(fileEndPrefix) -> {
                val id = line.removePrefix(fileEndPrefix)
                handleFileEnd(id)
                true
            }
            else -> false
        }
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

    private fun handleFileMeta(metaB64: String) {
        val app = getApplication<Application>()
        val json = try {
            val bytes = android.util.Base64.decode(metaB64, android.util.Base64.NO_WRAP)
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            return
        }

        val meta = jsonToMeta(json) ?: return
        if (meta.sizeBytes != null && meta.sizeBytes > maxFileBytes) return

        val tempFile = File(app.cacheDir, "chatfile_${meta.id}")
        runCatching { tempFile.delete() }
        val outputStream = FileOutputStream(tempFile, true)

        runCatching { receivingFiles[meta.id]?.outputStream?.close() }
        runCatching { receivingFiles[meta.id]?.tempFile?.delete() }
        receivingFiles[meta.id] = ReceivingFile(meta, tempFile, outputStream, 0)
    }

    private fun handleFileChunk(id: String, chunkB64: String) {
        val receiving = receivingFiles[id] ?: return
        val bytes = try {
            android.util.Base64.decode(chunkB64, android.util.Base64.NO_WRAP)
        } catch (_: Exception) {
            return
        }

        val newTotal = receiving.receivedBytes + bytes.size
        if (receiving.meta.sizeBytes != null && newTotal > receiving.meta.sizeBytes) return
        if (newTotal > maxFileBytes) return

        try {
            receiving.outputStream.write(bytes)
            receiving.receivedBytes = newTotal
        } catch (_: Exception) {
        }
    }

    private fun handleFileEnd(id: String) {
        val receiving = receivingFiles.remove(id) ?: return
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

        val senderName = receiving.meta.sender
        val icon = when {
            senderName.contains("Sukaldari", ignoreCase = true) -> Icons.Default.Restaurant
            senderName.contains("Zerbitzari", ignoreCase = true) -> Icons.Default.RoomService
            else -> Icons.Default.Person
        }

        if (savedUri != null) {
            viewModelScope.launch(Dispatchers.Main) {
                messages.add(
                    Message(
                        id = System.currentTimeMillis().toInt(),
                        text = "${senderName}: [FITXATEGIA] ${receiving.meta.fileName}",
                        isMe = false,
                        senderIcon = icon,
                        isFile = true,
                        fileName = receiving.meta.fileName,
                        fileUri = savedUri,
                        mimeType = receiving.meta.mimeType
                    )
                )
            }
        }
    }

    private fun buildFileMeta(uri: Uri, sender: String): FileMeta? {
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
        val id = "${System.currentTimeMillis()}_${secureRandom.nextInt(1_000_000)}"

        return FileMeta(
            id = id,
            sender = sender,
            fileName = safeName,
            mimeType = mimeType,
            sizeBytes = sizeBytes
        )
    }

    private fun metaToJson(meta: FileMeta): String {
        val obj = org.json.JSONObject()
        obj.put("id", meta.id)
        obj.put("sender", meta.sender)
        obj.put("fileName", meta.fileName)
        if (meta.mimeType != null) obj.put("mimeType", meta.mimeType)
        if (meta.sizeBytes != null) obj.put("sizeBytes", meta.sizeBytes)
        return obj.toString()
    }

    private fun jsonToMeta(json: String): FileMeta? {
        return runCatching {
            val obj = org.json.JSONObject(json)
            val id = obj.getString("id")
            val sender = obj.getString("sender")
            val fileName = obj.getString("fileName")
            val mimeType = if (obj.has("mimeType")) obj.optString("mimeType", null) else null
            val sizeBytes = if (obj.has("sizeBytes")) obj.optLong("sizeBytes") else null
            FileMeta(
                id = id,
                sender = sender,
                fileName = fileName,
                mimeType = mimeType?.takeIf { it.isNotBlank() },
                sizeBytes = sizeBytes?.takeIf { it > 0 }
            )
        }.getOrNull()
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
}
