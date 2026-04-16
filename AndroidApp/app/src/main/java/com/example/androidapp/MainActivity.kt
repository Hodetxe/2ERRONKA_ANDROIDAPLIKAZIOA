// MainActivity.kt
package com.example.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.androidapp.presentation.erreserbak.ErreserbaSortuPantaila
import com.example.androidapp.presentation.erreserbak.ErreserbakPantaila
import com.example.androidapp.presentation.eskariak.EskariaSortuPantaila
import com.example.androidapp.presentation.eskariak.EskariakPantaila
import com.example.androidapp.presentation.login.LoginPantaila
import com.example.androidapp.presentation.mahaiak.MahaiakPantaila
import com.example.androidapp.presentation.menu.MenuPantaila
import com.example.androidapp.presentation.txata.TxataAukeratuPantaila
import com.example.androidapp.presentation.txata.TxataPantaila

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { LoginPantaila(navController) }
                    composable("menu") { MenuPantaila(navController) }
                    composable("erreserbak") { ErreserbakPantaila(navController) }
                    composable("erreserba_sortu") { ErreserbaSortuPantaila(navController) }
                    composable("eskariak") { EskariakPantaila(navController) }
                    composable("mahaiak") { MahaiakPantaila(navController) }
                    composable(
                        route = "eskaria_sortu/{mahaiaId}",
                        arguments = listOf(navArgument("mahaiaId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val mahaiaId = backStackEntry.arguments?.getInt("mahaiaId") ?: 0
                        EskariaSortuPantaila(navController, mahaiaId)
                    }
                    composable("txata_aukeratu") { TxataAukeratuPantaila(navController) }
                    composable(
                        route = "txata/{chatId}/{chatName}",
                        arguments = listOf(
                            navArgument("chatId") { type = NavType.IntType },
                            navArgument("chatName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getInt("chatId") ?: 0
                        val chatName = backStackEntry.arguments?.getString("chatName") ?: ""
                        TxataPantaila(navController, chatId, chatName)
                    }
                }
            }
        }
    }
}
