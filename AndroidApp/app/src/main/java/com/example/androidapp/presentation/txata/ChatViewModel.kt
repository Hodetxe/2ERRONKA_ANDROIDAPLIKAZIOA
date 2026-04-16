package com.example.androidapp.presentation.txata

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidapp.core.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class ChatViewModel : ViewModel() {

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

    // Server configuration
    private val serverIp = "192.168.1.112"
    private val serverPort = 5555

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
                    writer?.println("$username txat-ean sartu da!")

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
                        val message = parseMessage(line)
                        launch(Dispatchers.Main) {
                            messages.add(message)
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
        if (text.isBlank() || !isConnected) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val username = SessionManager.currentUser?.izena ?: "AndroidUser"
                val messageToSend = "$username: $text"
                writer?.println(messageToSend)
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    connectionError = "Errorea mezua bidaltzean: ${e.message}"
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
}
