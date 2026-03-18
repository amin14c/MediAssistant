package com.example.mediassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mediassistant", 0) }

    var apiKey by remember { mutableStateOf(prefs.getString("gemini_api_key", "") ?: "") }
    var showKey by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres / الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Key Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Clé API Gemini", style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        "Obtenez gratuitement votre clé sur:
https://ai.google.dev/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it; isSaved = false },
                        label = { Text("Entrez votre clé API") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showKey)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            prefs.edit().putString("gemini_api_key", apiKey.trim()).apply()
                            isSaved = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enregistrer / حفظ")
                    }

                    if (isSaved) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Clé API enregistrée avec succès", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // About Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("À propos / حول التطبيق", style = MaterialTheme.typography.titleMedium)
                    }
                    HorizontalDivider()
                    Text("MediAssistant v1.0", style = MaterialTheme.typography.bodyMedium)
                    Text("Assistant psychiatrique intelligent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("مساعد طب نفسي بالذكاء الاصطناعي", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(4.dp))
                    Text("Powered by Google Gemini AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
