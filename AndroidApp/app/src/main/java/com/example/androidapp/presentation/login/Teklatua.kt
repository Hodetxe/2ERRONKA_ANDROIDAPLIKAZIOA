package com.example.androidapp.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import com.example.androidapp.ui.theme.AppColors

@Composable
fun Teklatua(onKeyPress: (String) -> Unit) {
    val teklak = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "←")

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        for (row in teklak.chunked(3)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { tekla ->
                    if (tekla.isEmpty()) {
                        Spacer(modifier = Modifier.size(80.dp))
                    } else {
                        val isDelete = tekla == "←"
                        Button(
                            onClick = { onKeyPress(tekla) },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDelete) AppColors.Primary else AppColors.Surface,
                                contentColor = if (isDelete) AppColors.Surface else AppColors.TextPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text(
                                text = tekla,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
