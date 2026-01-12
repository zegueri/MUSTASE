package com.example.mustase.prescription.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.model.ReminderEntity
import com.example.mustase.prescription.data.repository.PrescriptionRepository
import com.example.mustase.prescription.data.repository.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: PrescriptionRepository,
    private val reminderRepository: ReminderRepository,
    private val prescriptionId: Long
) : ViewModel() {

    sealed class DetailState {
        data object Loading : DetailState()
        data class Success(val prescription: PrescriptionEntity) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _reminders = MutableStateFlow<List<ReminderEntity>>(emptyList())
    val reminders: StateFlow<List<ReminderEntity>> = _reminders.asStateFlow()

    init {
        loadPrescription()
        loadReminders()
    }

    private fun loadPrescription() {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            try {
                val prescription = repository.getPrescriptionById(prescriptionId)
                if (prescription != null) {
                    _state.value = DetailState.Success(prescription)
                } else {
                    _state.value = DetailState.Error("Ordonnance introuvable")
                }
            } catch (e: Exception) {
                _state.value = DetailState.Error(e.message ?: "Erreur lors du chargement")
            }
        }
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getRemindersByPrescription(prescriptionId).collect { list ->
                _reminders.value = list
            }
        }
    }

    fun deletePrescription(onDeleted: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePrescription(prescriptionId)
                onDeleted()
            } catch (e: Exception) {
                _state.value = DetailState.Error(e.message ?: "Erreur lors de la suppression")
            }
        }
    }

    fun updateTitle(newTitle: String) {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                if (currentState is DetailState.Success) {
                    val updatedPrescription = currentState.prescription.copy(title = newTitle)
                    repository.updatePrescription(updatedPrescription)
                    _state.value = DetailState.Success(updatedPrescription)
                }
            } catch (e: Exception) {
                // Ignorer l'erreur silencieusement ou logger
            }
        }
    }
}

