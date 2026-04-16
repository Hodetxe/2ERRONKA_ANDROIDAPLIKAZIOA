package com.example.androidapp.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.core.SessionManager
import com.example.androidapp.data.model.LoginField

@Composable
fun LoginPantaila(
    navController: NavController,
    viewModel: LoginPantailaViewModel = viewModel()
) {
    val state = viewModel.state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFFF3E0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFF57C00), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TEKNOBIDE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.workerCode,
                onValueChange = {},
                label = { Text("LANGILE-KODEA") },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onFieldSelected(LoginField.Code) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = if (state.focusedField == LoginField.Code) Color(0xFFE65100) else Color(0xFFFFCCBC),
                    disabledLabelColor = Color(0xFFE65100),
                    disabledContainerColor = Color.White,
                    disabledPlaceholderColor = Color.Gray
                ),
                textStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = "*".repeat(state.password.length),
                onValueChange = {},
                label = { Text("PASAHITZA") },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onFieldSelected(LoginField.Password) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = if (state.focusedField == LoginField.Password) Color(0xFFE65100) else Color(0xFFFFCCBC),
                    disabledLabelColor = Color(0xFFE65100),
                    disabledContainerColor = Color.White
                ),
                textStyle = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Teklatua(onKeyPress = { key -> viewModel.onKeyPress(key) })

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.login(
                        onSuccess = { langilea ->
                            SessionManager.currentUser = langilea
                            navController.navigate("menu")
                        },
                        onError = { msg -> errorMessage = msg }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF57C00),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "SAIOA HASI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        errorMessage?.let { msg ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("ITXI", color = Color.White)
                        }
                    }
                ) { Text(msg) }
            }
        }
    }
}
