package com.example.androidapp.presentation.eskariak

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.data.dto.ProduktuaDto
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.androidapp.core.network.ApiClient
import com.example.androidapp.core.SessionManager
import com.example.androidapp.ui.theme.AppColors
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun EskariaSortuPantaila(
    navController: NavController,
    mahaiaId: Int,
    viewModel: EskariaSortuViewModel = viewModel()
) {
    val egoera = viewModel.egoera
    val currentUser = SessionManager.currentUser

    // Colors
    val primaryColor = AppColors.Primary
    val darkBrandColor = AppColors.BrandDark
    val panelColor = AppColors.Background

    // Time
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(now)
            delay(1000)
        }
    }
    
    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(mahaiaId) {
        viewModel.kargatuErreserbaAktiboa(mahaiaId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(16.dp)
        ) {
            // Back Button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atzera",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(32.dp)
                    .clickable { navController.popBackStack() }
            )

            // Reservation Selector (Center)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (egoera.erreserbak.isNotEmpty()) {
                    Box {
                         val selectedErreserba = egoera.erreserbak.find { it.id == egoera.erreserbaId }
                         Row(
                             verticalAlignment = Alignment.CenterVertically,
                             modifier = Modifier
                                 .clickable { expanded = true }
                                 .background(AppColors.Surface.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                 .padding(horizontal = 12.dp, vertical = 4.dp)
                         ) {
                             Text(
                                 text = selectedErreserba?.bezeroIzena ?: "Aukeratu Erreserba",
                                 color = Color.White,
                                 fontSize = 18.sp,
                                 fontWeight = FontWeight.Bold
                             )
                             Icon(
                                 imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropDown, // Use same icon or rotate it, keeping simple for now
                                 contentDescription = "Dropdown",
                                 tint = Color.White,
                                 modifier = Modifier.rotate(if (expanded) 180f else 0f)
                             )
                         }
                         
                         DropdownMenu(
                             expanded = expanded,
                             onDismissRequest = { expanded = false }
                         ) {
                             egoera.erreserbak.forEach { erreserba ->
                                 DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text("${erreserba.bezeroIzena}", fontWeight = FontWeight.Bold)
                                            Text("${erreserba.mahaiakId}. Mahaia (${erreserba.egunaOrdua})", fontSize = 12.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.aukeratuErreserba(erreserba.id)
                                        expanded = false
                                    }
                                )
                             }
                         }
                    }
                } else {
                    Text(
                         text = "Ez dago erreserbarik",
                         color = Color.White,
                         fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Side Content
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Table Number
                val selectedErreserba = egoera.erreserbak.find { it.id == egoera.erreserbaId }
                val displayMahaiaId = selectedErreserba?.mahaiakId ?: mahaiaId
                
                Text(
                    text = String.format("%02d Mahaia", displayMahaiaId),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 16.dp)
                )

                // Worker and Time
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currentUser?.izena ?: "Ezezaguna",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$currentDate $currentTime",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Main Content
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Side: Categories and Products (70%)
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                // Category Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val categories = egoera.kategoriak
                    if (categories.isEmpty() && !egoera.isLoading) {
                         Text("Ez da kategoriarik aurkitu")
                    } else {
                        categories.forEach { category ->
                            CategoryTab(
                                text = category,
                                isSelected = egoera.kategoriaAukeratua == category,
                                onClick = { viewModel.aldatuKategoria(category) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Title
                Text(
                    text = egoera.kategoriaAukeratua,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Product List
                if (egoera.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val filteredProducts = egoera.produktuak.filter { it.mota == egoera.kategoriaAukeratua }
                        items(filteredProducts) { product ->
                            val quantity = egoera.saskia[product.id] ?: 0
                            ProductItem(
                                product = product,
                                quantity = quantity,
                                isAddEnabled = quantity < product.stock,
                                onAdd = { viewModel.gehituProduktua(product.id) },
                                onRemove = { viewModel.kenduProduktua(product.id) }
                            )
                        }
                    }
                }
            }

            // Right Side: Order Summary (30%)
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                        .background(darkBrandColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Eskaria",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Cart Items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val cartProducts = egoera.saskia.keys.mapNotNull { id -> viewModel.getProduktuaById(id) }
                    items(cartProducts) { product ->
                        val quantity = egoera.saskia[product.id] ?: 0
                        CartItem(
                            product = product,
                            quantity = quantity,
                            isAddEnabled = quantity < product.stock,
                            onAdd = { viewModel.gehituProduktua(product.id) },
                            onRemove = { viewModel.kenduProduktua(product.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Send Button
                Button(
                    onClick = { 
                        viewModel.bidaliEskaria {
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (egoera.isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = "Bidali",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) AppColors.Secondary.copy(alpha = 0.14f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AppColors.Primary else AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}



@Composable
fun ProductItem(
    product: ProduktuaDto,
    quantity: Int,
    isAddEnabled: Boolean = true,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(12.dp),
            color = AppColors.Border.copy(alpha = 0.5f)
        ) {
            val context = LocalContext.current
            val model = remember(product.izena) {
                var normalizedName = product.izena.trim().lowercase()
                    .replace(" ", "_")
                    .replace("-", "_")
                    .replace("á", "a")
                    .replace("é", "e")
                    .replace("í", "i")
                    .replace("ó", "o")
                    .replace("ú", "u")
                    .replace("ñ", "n")
                    .replace("izokina", "izokin")
                    .replace("galletak", "galleta")
                    .replace("solomoa", "solomo")
                    .replace("txipiroiak", "txipiroi")
                    .replace("[^a-z0-9_]".toRegex(), "")
                
                if (normalizedName == "solomo_eta_patatak") normalizedName = "txerri_solomo_patatekin"
                if (normalizedName == "izokin_entsalada") normalizedName = "izokin_plantxan"
                if (normalizedName == "jogurta_eta_granola") normalizedName = "eztia_eta_jogurta"
                if (normalizedName == "galleta") normalizedName = "galleta_eta_mermelada"
                if (normalizedName == "izokin") normalizedName = "izokin_plantxan"
                if (normalizedName == "solomo") normalizedName = "txerri_solomo_patatekin"
                if (normalizedName == "txipiroi") normalizedName = "txipiroi_plantxan"
                if (normalizedName == "jogurta") normalizedName = "eztia_eta_jogurta"
                if (normalizedName == "albondigak") normalizedName = "albondigak_tomate_saltsan"
                if (normalizedName == "kalamarrak" || normalizedName == "kalamar_ogitartekoa") normalizedName = "aliolidun_kalamar_ogitartekoa"
                if (normalizedName == "piperrak" || normalizedName == "piper_beteak") normalizedName = "bakailaoz_betetako_piperrak"
                if (normalizedName == "kostilak") normalizedName = "barbekoa_kostilak"
                if (normalizedName.contains("pomal")) normalizedName = "pomal_ardo_beltza"
                if (normalizedName == "flana") normalizedName = "etxeko_flana"
                if (normalizedName == "natillak") normalizedName = "etxeko_natillak"
                if (normalizedName == "sagardoa") normalizedName = "sagardo_botila"
                if (normalizedName == "hirugiarra" || normalizedName == "hirugiar_ogitartekoa") normalizedName = "hirugiar_eta_gazta_ogitartekoa"
                if (normalizedName == "ardo_beltza") normalizedName = "pomal_ardo_beltza"
                if (normalizedName == "oilasko_hegalak") normalizedName = "oilasko_hegalak_ketuta"
                if (normalizedName == "mejilloiak") normalizedName = "mejilloiak_lurrinez"

                val resId = context.resources.getIdentifier(normalizedName, "drawable", context.packageName)
                
                if (resId != 0) resId else "${ApiClient.BASE_URL}irudiak/${product.izena}.jpg"
            }

            AsyncImage(
                model = model,
                contentDescription = product.izena,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.izena,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            QuantitySelector(
                quantity = quantity,
                isAddEnabled = isAddEnabled,
                onAdd = onAdd,
                onRemove = onRemove,
                backgroundColor = AppColors.Background
            )
        }
    }
}

@Composable
fun CartItem(
    product: ProduktuaDto,
    quantity: Int,
    isAddEnabled: Boolean = true,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = product.izena,
            color = AppColors.Surface,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        QuantitySelector(
            quantity = quantity,
            isAddEnabled = isAddEnabled,
            onAdd = onAdd,
            onRemove = onRemove,
            backgroundColor = AppColors.Surface.copy(alpha = 0.08f),
            contentColor = AppColors.Surface
        )
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    isAddEnabled: Boolean = true,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    backgroundColor: Color,
    contentColor: Color = Color.Black
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Text(
                    text = "−",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            
            Text(
                text = quantity.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            IconButton(
                onClick = onAdd, 
                enabled = isAddEnabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Gehitu",
                    tint = if (isAddEnabled) contentColor else contentColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}
