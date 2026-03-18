package com.example.mediassistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mediassistant.viewmodel.PatientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    navController: NavController,
    patientId: Int,
    viewModel: PatientViewModel = viewModel()
) {
    val patients by viewModel.patients.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    val patient = patients.find { it.id == patientId }
    val prefs = remember { context.getSharedPreferences("mediassistant", 0) }
    val apiKey = remember { prefs.getString("gemini_api_key", "") ?: "" }

    LaunchedEffect(Unit) { viewModel.clearAnalysis() }

    if (patient == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val medications = viewModel.getMedicationsForPatient(patient)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(patient.name)
                        Text(patient.diagnosis, style = MaterialTheme.typography.labelSmall)
                    }
                },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Informations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    InfoRow("Nom", patient.name)
                    InfoRow("Âge", "${patient.age} ans")
                    InfoRow("Diagnostic", patient.diagnosis)
                }
            }

            // Medications card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Medication, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Plan médicamenteux / الخطة الدوائية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                    if (medications.isEmpty()) {
                        Text(
                            "Aucun médicament enregistré",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        medications.forEach { med ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Circle,
                                    null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(8.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(med.name, fontWeight = FontWeight.Medium)
                                    if (med.dose.isNotBlank())
                                        Text("${med.dose} · ${med.frequency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            // API key warning
            if (apiKey.isBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Configurez votre clé API Gemini dans les Paramètres",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Analyze button
            Button(
                onClick = {
                    if (apiKey.isNotBlank()) viewModel.analyzePatient(patient, apiKey)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading && apiKey.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Analyse en cours...")
                } else {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Analyser avec Gemini AI / تحليل بالذكاء الاصطناعي")
                }
            }

            // Error
            if (error.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(error, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            // Analysis result
            if (analysisResult.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Recommandations IA / توصيات الذكاء الاصطناعي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        Text(analysisResult, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "La décision finale appartient au médecin / القرار للطبيب",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Previous analysis
            if (patient.lastAnalysis.isNotBlank() && analysisResult.isBlank() && error.isBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Dernière analyse sauvegardée", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(patient.lastAnalysis, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
