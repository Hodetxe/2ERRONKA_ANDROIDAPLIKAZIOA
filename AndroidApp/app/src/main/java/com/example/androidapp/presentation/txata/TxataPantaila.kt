package com.example.androidapp.presentation.txata

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
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

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext

data class Message(
    val id: Int,
    val text: String,
    val isMe: Boolean,
    val senderIcon: ImageVector,
    val isFile: Boolean = false,
    val fileName: String? = null,
    val fileUri: Uri? = null,
    val mimeType: String? = null
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
    val emojis = remember { listOf("😀", "😂", "😍", "👍", "🙏", "☕", "🍔", "✅", "❗") }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                viewModel.sendFile(uri)
            } else {
                viewModel.setUiError("Ez da fitxategirik aukeratu.")
            }
        }
    )

    LaunchedEffect(viewModel.connectionError) {
        val msg = viewModel.connectionError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = msg, actionLabel = "ITXI", duration = SnackbarDuration.Short)
        viewModel.clearConnectionError()
    }

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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    MessageItem(
                        message = message,
                        onOpenFile = { fileUri, mimeType ->
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(fileUri, mimeType)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            // Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { emoji ->
                        Surface(
                            color = AppColors.Surface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
                            modifier = Modifier
                                .clickable { messageText += emoji }
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Text Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            runCatching {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            }.onFailure {
                                viewModel.setUiError("Ezin da fitxategi-aukeratzailea ireki gailu honetan.")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Fitxategia",
                            tint = AppColors.TextPrimary
                        )
                    }

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
fun MessageItem(
    message: Message,
    onOpenFile: (Uri, String?) -> Unit
) {
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
            if (message.isFile && message.fileUri != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = message.text,
                        color = AppColors.TextPrimary,
                        fontSize = 14.sp
                    )
                    Button(
                        onClick = { onOpenFile(message.fileUri, message.mimeType) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            contentColor = AppColors.Surface
                        )
                    ) {
                        Text(
                            text = "Ireki",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Text(
                    text = message.text,
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp
                )
            }
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
