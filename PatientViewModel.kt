package com.example.mediassistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediassistant.api.GeminiService
import com.example.mediassistant.data.AppDatabase
import com.example.mediassistant.data.Medication
import com.example.mediassistant.data.Patient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val gson = Gson()

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    private val _analysisResult = MutableStateFlow("")
    val analysisResult: StateFlow<String> = _analysisResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _patients.value = db.patientDao().getAllPatients()
        }
    }

    fun savePatient(name: String, age: Int, diagnosis: String, medications: List<Medication>) {
        viewModelScope.launch {
            val patient = Patient(
                name = name.trim(),
                age = age,
                diagnosis = diagnosis.trim(),
                medications = gson.toJson(medications)
            )
            db.patientDao().insertPatient(patient)
            loadPatients()
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            db.patientDao().deletePatient(patient)
            loadPatients()
        }
    }

    fun analyzePatient(patient: Patient, apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = ""
            _error.value = ""
            try {
                val medications = getMedicationsForPatient(patient)
                val service = GeminiService(apiKey)
                val result = service.analyzePatient(patient, medications)
                _analysisResult.value = result
                db.patientDao().updatePatient(patient.copy(lastAnalysis = result))
                loadPatients()
            } catch (e: Exception) {
                _error.value = "Erreur: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun getMedicationsForPatient(patient: Patient): List<Medication> {
        if (patient.medications.isBlank() || patient.medications == "[]") return emptyList()
        return try {
            val type = object : TypeToken<List<Medication>>() {}.type
            gson.fromJson(patient.medications, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearAnalysis() {
        _analysisResult.value = ""
        _error.value = ""
    }
}
