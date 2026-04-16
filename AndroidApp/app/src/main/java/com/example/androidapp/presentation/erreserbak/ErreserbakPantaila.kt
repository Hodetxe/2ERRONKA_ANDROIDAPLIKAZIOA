package com.example.androidapp.presentation.erreserbak

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.data.dto.ErreserbaDto
import com.example.androidapp.presentation.components.AppTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErreserbakPantaila(
    navController: NavController,
    viewModel: ErreserbakViewModel = viewModel()
) {
    val egoera = viewModel.egoera

    Scaffold(
        topBar = {
            AppTopBar(
                title = "ERRESERBAK",
                navController = navController
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF3E0))
        ) {
            if (egoera.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFF57C00)
                )
            } else if (egoera.error != null) {
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
                        text = egoera.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.lortuDatuak() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                    ) {
                        Text("Saiatu berriro")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(egoera.erreserbak) { erreserba ->
                        val mahaiaZenbakia = egoera.mahaiak[erreserba.mahaiakId]
                        ErreserbaTxartela(erreserba, mahaiaZenbakia)
                    }
                }
            }
        }
    }
}

@Composable
fun ErreserbaTxartela(erreserba: ErreserbaDto, mahaiaZenbakia: Int?) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = erreserba.bezeroIzena,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                StatusChip(paid = erreserba.ordainduta == 1)
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color(0xFFFFE0B2)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ErreserbaDetailItem(
                    icon = Icons.Default.CalendarToday,
                    text = erreserba.egunaOrdua
                )
                ErreserbaDetailItem(
                    icon = Icons.Default.Restaurant,
                    text = if (mahaiaZenbakia != null) "Mahaia $mahaiaZenbakia" else "Mahaia ?"
                )
                ErreserbaDetailItem(
                    icon = Icons.Default.Phone,
                    text = erreserba.telefonoa
                )
                ErreserbaDetailItem(
                    icon = Icons.Default.People,
                    text = "${erreserba.pertsonaKopurua} pertsona"
                )
            }
        }
    }
}

@Composable
fun StatusChip(paid: Boolean) {
    Surface(
        color = if (paid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (paid) Color(0xFF4CAF50) else Color(0xFFEF5350)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (paid) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (paid) Color(0xFF4CAF50) else Color(0xFFEF5350),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (paid) "Ordainduta" else "Ordaindu gabe",
                style = MaterialTheme.typography.labelMedium,
                color = if (paid) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErreserbaDetailItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFF57C00),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}
