package com.example.mustase.prescription.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: PrescriptionRepository
) : ViewModel() {

    sealed class HistoryState {
        data object Loading : HistoryState()
        data class Success(val prescriptions: List<PrescriptionEntity>) : HistoryState()
        data class Error(val message: String) : HistoryState()
    }

    private val _state = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        loadPrescriptions()
    }

    private fun loadPrescriptions() {
        viewModelScope.launch {
            _state.value = HistoryState.Loading
            try {
                repository.getAllPrescriptions().collect { prescriptions ->
                    _state.value = HistoryState.Success(prescriptions)
                }
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Erreur lors du chargement")
            }
        }
    }

    fun deletePrescription(id: Long) {
        viewModelScope.launch {
            try {
                repository.deletePrescription(id)
            } catch (e: Exception) {
                // En cas d'erreur, on ne fait rien de spécial car le Flow se mettra à jour automatiquement
            }
        }
    }

    fun refresh() {
        loadPrescriptions()
    }
}

