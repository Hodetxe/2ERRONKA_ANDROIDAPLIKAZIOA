package com.example.androidapp.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.androidapp.core.SessionManager
import com.example.androidapp.presentation.components.AppTopBar
import com.example.androidapp.ui.theme.AppColors

data class MenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color = AppColors.Primary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuPantaila(navController: NavController) {
    val menuItems = listOf(
        MenuItem("Erreserbak ikusi", Icons.Default.DateRange, "erreserbak", AppColors.Secondary),
        MenuItem("Erreserba sortu", Icons.Default.EditCalendar, "erreserba_sortu", AppColors.Primary),
        MenuItem("Eskariak ikusi", Icons.Default.ReceiptLong, "eskariak", AppColors.BrandDark),
        MenuItem("Eskariak sortu", Icons.Default.ShoppingCart, "eskaria_sortu/1", AppColors.PrimaryHover),
        MenuItem("Mahaiak", Icons.Default.TableRestaurant, "mahaiak", AppColors.Secondary.copy(alpha = 0.85f)),
        MenuItem("Txata", Icons.Default.Chat, "txata_aukeratu", AppColors.Primary.copy(alpha = 0.85f))
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "TEKNOBIDE",
                navController = navController,
                showBackButton = false, // Hide back arrow on main menu
                onLogout = {
                    SessionManager.currentUser = null
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = AppColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ongi etorri!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextStrong,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Aukeratu aukera bat hasteko:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(menuItems) { item ->
                        MenuButton(item = item, onClick = {
                            navController.navigate(item.route)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun MenuButton(item: MenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(item.color)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(item.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
