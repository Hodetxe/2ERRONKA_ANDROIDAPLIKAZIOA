package com.example.androidapp.presentation.txata

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidapp.presentation.components.AppTopBar

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.androidapp.ui.theme.AppColors

data class Message(
    val id: Int,
    val text: String,
    val isMe: Boolean,
    val senderIcon: ImageVector
)

@Composable
fun TxataPantaila(
    navController: NavController,
    chatId: Int,
    chatName: String,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Determine icon based on name (simple logic for prototype)
    val headerIcon = when {
        chatName.contains("Sukaldari", ignoreCase = true) -> Icons.Default.Restaurant
        chatName.contains("Zerbitzari", ignoreCase = true) -> Icons.Default.RoomService
        else -> Icons.Default.Restaurant // Default
    }

    val messages = viewModel.messages
    var messageText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.connect()
        focusRequester.requestFocus()
        // keyboardController?.show() // Optional: Show keyboard on start
    }

    // Disconnect when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnect()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = chatName,
                navController = navController,
                titleIcon = headerIcon
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.Background)
        ) {
            // Connection Status
            if (!viewModel.isConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Danger.copy(alpha = 0.12f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.connectionError ?: "Konektatzen...",
                        color = AppColors.TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    MessageItem(message)
                }
            }

            // Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
            ) {
                // Text Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Input Box
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                text = "Idatzi mezua...",
                                color = AppColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.Secondary.copy(alpha = 0.10f),
                            unfocusedContainerColor = AppColors.Secondary.copy(alpha = 0.10f),
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = AppColors.Border,
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(AppColors.Primary, CircleShape)
                            .clickable {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Bidali",
                            tint = AppColors.Surface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!message.isMe) {
            Icon(
                imageVector = message.senderIcon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (message.isMe) AppColors.Secondary.copy(alpha = 0.12f) else AppColors.Surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (message.isMe) AppColors.Primary else AppColors.Border,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = AppColors.TextPrimary,
                fontSize = 14.sp
            )
        }

        if (message.isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = message.senderIcon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color.Black
            )
        }
    }
}
