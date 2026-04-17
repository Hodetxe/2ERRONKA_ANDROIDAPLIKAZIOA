package com.example.androidapp.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidapp.R
import com.example.androidapp.core.SessionManager
import com.example.androidapp.data.model.LoginField
import com.example.androidapp.ui.theme.AppColors

@Composable
fun LoginPantaila(
    navController: NavController,
    viewModel: LoginPantailaViewModel = viewModel()
) {
    val state = viewModel.state
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.LoginGradientStart, AppColors.BrandDark)
                )
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = Color.Transparent
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
                    .background(AppColors.Surface.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nanobites_icono),
                    contentDescription = "Nanobites logo",
                    modifier = Modifier
                        .size(84.dp)
                        .background(Color.Transparent, CircleShape),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TEKNOBIDE",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Surface
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
                    disabledTextColor = AppColors.TextStrong,
                    disabledBorderColor = if (state.focusedField == LoginField.Code) AppColors.Primary else AppColors.Border,
                    disabledLabelColor = AppColors.Primary,
                    disabledContainerColor = AppColors.Surface,
                    disabledPlaceholderColor = AppColors.TextSecondary
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
                    disabledTextColor = AppColors.TextStrong,
                    disabledBorderColor = if (state.focusedField == LoginField.Password) AppColors.Primary else AppColors.Border,
                    disabledLabelColor = AppColors.Primary,
                    disabledContainerColor = AppColors.Surface
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
                    containerColor = AppColors.Primary,
                    contentColor = AppColors.Surface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = AppColors.Surface,
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
                    containerColor = AppColors.Danger,
                    contentColor = AppColors.Surface,
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("ITXI", color = AppColors.Surface)
                        }
                    }
                ) { Text(msg) }
            }
        }
        }
    }
}
