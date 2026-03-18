package com.example.mediassistant.api

import com.example.mediassistant.data.Medication
import com.example.mediassistant.data.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService(private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzePatient(patient: Patient, medications: List<Medication>): String {
        return withContext(Dispatchers.IO) {
            val prompt = buildPrompt(patient, medications)
            callGeminiAPI(prompt)
        }
    }

    private fun buildPrompt(patient: Patient, medications: List<Medication>): String {
        val medsText = if (medications.isEmpty()) {
            "Aucun médicament enregistré"
        } else {
            medications.joinToString("\n") { "  - ${it.name}: ${it.dose}, ${it.frequency}" }
        }

        return """
Tu es un assistant clinique pour psychiatre. Analyse ce dossier et fournis des recommandations basées sur les guidelines officiels (DSM-5, CANMAT 2023, HAS).

=== DOSSIER PATIENT ===
Nom: ${patient.name}
Âge: ${patient.age} ans
Diagnostic: ${patient.diagnosis}

Plan médicamenteux actuel:
$medsText

=== INSTRUCTIONS ===
Fournis une analyse structurée incluant:
1. ✅ Évaluation de l'adéquation du traitement actuel
2. ⚠️ Interactions médicamenteuses potentielles
3. 💊 Ajustements de doses suggérés (avec justification)
4. ➕ Médicaments à envisager d'ajouter (avec indication)
5. ➖ Médicaments à reconsidérer (avec justification)
6. 📚 Références: guidelines utilisés

⚕️ RAPPEL IMPORTANT: Ces recommandations sont à titre informatif uniquement. La décision clinique finale appartient exclusivement au médecin traitant.
        """.trimIndent()
    }

    private fun callGeminiAPI(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 2048)
            })
        }.toString()

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return "Erreur API (${response.code}): ${response.body?.string()}"
            }
            val body = response.body?.string() ?: return "Réponse vide"
            try {
                JSONObject(body)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } catch (e: Exception) {
                "Erreur de parsing: ${e.message}"
            }
        }
    }
}
