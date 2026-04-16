package com.example.androidapp.presentation.txata

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidapp.presentation.components.AppTopBar

data class ChatOption(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val type: ChatType,
    val isPinned: Boolean = false
)

enum class ChatType {
    TPV, GROUP, WAITER, CHEF
}

@Composable
fun TxataAukeratuPantaila(navController: NavController) {
    val chatOptions = listOf(
        ChatOption(1, "TPV", Icons.Default.Computer, ChatType.TPV, true),
        ChatOption(2, "Talde 1", Icons.Default.Group, ChatType.GROUP, true),
        ChatOption(3, "Zerbitzari 1", Icons.Default.RoomService, ChatType.WAITER),
        ChatOption(4, "Zerbitzari 2", Icons.Default.RoomService, ChatType.WAITER),
        ChatOption(5, "Sukaldari 2", Icons.Default.Restaurant, ChatType.CHEF),
        ChatOption(6, "Zerbitzari 3", Icons.Default.RoomService, ChatType.WAITER),
        ChatOption(7, "Zerbitzari 4", Icons.Default.RoomService, ChatType.WAITER)
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "TXATA",
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF3E0)) // Light beige background
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chatOptions) { chat ->
                    ChatOptionItem(chat = chat, onClick = {
                        navController.navigate("txata/${chat.id}/${chat.name}")
                    })
                }
            }
        }
    }
}

@Composable
fun ChatOptionItem(chat: ChatOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFFBCAAA4), RoundedCornerShape(40.dp)) // Brownish background
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Icon
        Icon(
            imageVector = chat.icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Black
        )

        // Center Name Button style
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .height(48.dp)
                .background(Color(0xFFE65100), RoundedCornerShape(8.dp)), // Orange/Brown inner box
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.name,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Right Icon (Pin)
        if (chat.isPinned) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pinned",
                modifier = Modifier.size(32.dp),
                tint = Color.Black
            )
        } else {
            Spacer(modifier = Modifier.size(32.dp))
        }
    }
}
