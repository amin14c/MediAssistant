package com.example.mediassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mediassistant.ui.screens.AddPatientScreen
import com.example.mediassistant.ui.screens.HomeScreen
import com.example.mediassistant.ui.screens.PatientDetailScreen
import com.example.mediassistant.ui.screens.SettingsScreen
import com.example.mediassistant.ui.theme.MediAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("add_patient") {
                            AddPatientScreen(navController)
                        }
                        composable("patient/{patientId}") { backStack ->
                            val id = backStack.arguments?.getString("patientId")?.toIntOrNull() ?: return@composable
                            PatientDetailScreen(navController, id)
                        }
                        composable("settings") {
                            SettingsScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
