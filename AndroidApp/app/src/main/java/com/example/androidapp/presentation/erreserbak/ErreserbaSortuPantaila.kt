package com.example.androidapp.presentation.erreserbak

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.presentation.components.AppTopBar
import com.example.androidapp.ui.theme.AppColors
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErreserbaSortuPantaila(
    navController: NavController,
    viewModel: ErreserbaSortuViewModel = viewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
            viewModel.resetSuccess()
        }
    }

    // Date Picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val formattedMonth = (month + 1).toString().padStart(2, '0')
            val formattedDay = dayOfMonth.toString().padStart(2, '0')
            viewModel.onDateChanged("$year-$formattedMonth-$formattedDay")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val allowedTimes = listOf(
        "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
        "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00"
    )
    var timeExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "ERRESERBA BERRIA",
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
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Bete ezazu formularioa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextStrong,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (state.error != null) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = state.customerName,
                            onValueChange = { viewModel.onCustomerNameChanged(it) },
                            label = { Text("Bezeroaren Izena") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.Secondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { viewModel.onPhoneChanged(it) },
                            label = { Text("Telefonoa") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = AppColors.Secondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        OutlinedTextField(
                            value = state.personCount,
                            onValueChange = { viewModel.onPersonCountChanged(it) },
                            label = { Text("Pertsona Kopurua") },
                            leadingIcon = { Icon(Icons.Default.Group, contentDescription = null, tint = AppColors.Secondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                focusedLabelColor = AppColors.Primary,
                                cursorColor = AppColors.Primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = state.date,
                                    onValueChange = {},
                                    label = { Text("Data") },
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = AppColors.Secondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        focusedLabelColor = AppColors.Primary
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { datePickerDialog.show() }
                                )
                            }
                            
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = state.time,
                                    onValueChange = {},
                                    label = { Text("Ordua") },
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = AppColors.Secondary) },
                                    trailingIcon = {
                                        Icon(
                                            if (timeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            "Aukeratu",
                                            tint = AppColors.Secondary
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppColors.Primary,
                                        focusedLabelColor = AppColors.Primary
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { timeExpanded = !timeExpanded }
                                )
                                DropdownMenu(
                                    expanded = timeExpanded,
                                    onDismissRequest = { timeExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .heightIn(max = 300.dp)
                                        .background(AppColors.Surface)
                                ) {
                                    allowedTimes.forEach { time ->
                                        DropdownMenuItem(
                                            text = { Text(time) },
                                            onClick = {
                                                viewModel.onTimeChanged(time)
                                                timeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                         }

                        // Mahaia Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.selectedTable?.let { "Mahaia ${it.zenbakia} (${it.kokapena})" } ?: "",
                                onValueChange = {},
                                label = { Text("Aukeratu Mahaia") },
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = AppColors.Secondary) },
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            "Aukeratu",
                                            tint = AppColors.Secondary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    focusedLabelColor = AppColors.Primary
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { expanded = !expanded }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .background(AppColors.Surface)
                            ) {
                                state.tables.forEach { table ->
                                    DropdownMenuItem(
                                        text = { Text("Mahaia ${table.zenbakia} - ${table.pertsonaKopurua} pax (${table.kokapena})") },
                                        onClick = {
                                            viewModel.onTableSelected(table)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.createReservation() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = AppColors.Surface
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                "SORTU ERRESERBA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
