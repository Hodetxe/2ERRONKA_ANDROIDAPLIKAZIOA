package com.example.androidapp.presentation.eskariak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.data.dto.EskariaDto
import com.example.androidapp.data.dto.EskariaProduktuaDto
import com.example.androidapp.presentation.components.AppTopBar
import com.example.androidapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EskariakPantaila(
    navController: NavController,
    viewModel: EskariakViewModel = viewModel()
) {
    val state = viewModel.state

    LaunchedEffect(Unit) {
        android.util.Log.d("EskariakPantaila", "Composing EskariakPantaila")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "ESKARIAK",
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.Background)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.Primary
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Errorea: ${state.error}", 
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchOrders() },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("Saiatu berriro")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.orders) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(eskaria: EskariaDto) {
    var expanded by remember { mutableStateOf(false) }
    
    val statusText = eskaria.egoera ?: "Ezezaguna"
    val tableText = if (eskaria.mahaiaZenbakia != null) "Mahaia ${eskaria.mahaiaZenbakia}" else "Mahaia ?"
    val customerName = eskaria.bezeroIzena ?: "Bezero ezezaguna"
    val idText = "Eskaria #${eskaria.id}"
    
    // Calculate total price locally from products to ensure accuracy
    val calculatedTotal = eskaria.produktuak?.sumOf { it.kantitatea * it.prezioa } ?: 0.0
    val priceText = "${String.format("%.2f", calculatedTotal)} €"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: ID + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = idText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextStrong
                )
                StatusChip(statusText)
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = AppColors.Border
            )

            // Info Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Column: Customer & Table
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OrderDetailItem(Icons.Default.Person, customerName)
                    OrderDetailItem(Icons.Default.Restaurant, tableText)
                }
                
                // Right Column: Price & Toggle
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                             imageVector = Icons.Default.Euro,
                             contentDescription = null,
                             tint = AppColors.Secondary,
                             modifier = Modifier.size(20.dp)
                         )
                         Spacer(modifier = Modifier.width(4.dp))
                         Text(
                             text = priceText,
                             style = MaterialTheme.typography.titleMedium,
                             fontWeight = FontWeight.Bold,
                             color = AppColors.TextStrong
                         )
                     }
                }
            }

            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "Produktuak",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextStrong,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        color = AppColors.Background,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            OrderProductsList(eskaria.produktuak ?: emptyList())
                        }
                    }
                }
            }
            
            // Expand Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun OrderDetailItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Secondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextPrimary
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, bgColor, icon) = when (status.lowercase()) {
        "amaituta" -> Triple(AppColors.Success, AppColors.SuccessSoft, Icons.Default.CheckCircle)
        "bertan behera" -> Triple(AppColors.DangerHover, AppColors.Danger.copy(alpha = 0.10f), Icons.Default.Cancel)
        else -> Triple(AppColors.Secondary, AppColors.Secondary.copy(alpha = 0.12f), Icons.Default.Info)
    }
    
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = status.uppercase(),
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OrderProductsList(products: List<EskariaProduktuaDto>) {
    if (products.isEmpty()) {
        Text(
            "Ez dago produkturik",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(8.dp)
        )
    } else {
        Column {
            products.forEachIndexed { index, prod ->
                val name = prod.produktuaIzena ?: "Produktua"
                val quantity = prod.kantitatea
                val price = prod.prezioa
                val total = prod.kantitatea * prod.prezioa
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${quantity} x ${String.format("%.2f", price)} €",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }
                    Text(
                        text = "${String.format("%.2f", total)} €",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
                
                if (index < products.lastIndex) {
                    Divider(color = AppColors.Border)
                }
            }
        }
    }
}
