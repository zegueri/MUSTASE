package com.example.mustase.prescription.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: PrescriptionRepository,
    private val prescriptionId: Long
) : ViewModel() {

    sealed class DetailState {
        data object Loading : DetailState()
        data class Success(val prescription: PrescriptionEntity) : DetailState()
        data class Error(val message: String) : DetailState()
    }

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        loadPrescription()
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
}

