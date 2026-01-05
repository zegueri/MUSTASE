package com.example.mustase.prescription.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.model.Resource
import com.example.mustase.prescription.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(
    private val repository: PrescriptionRepository
) : ViewModel() {

    sealed class ScanState {
        data object Idle : ScanState()
        data object Loading : ScanState()
        data class Success(val prescription: PrescriptionEntity) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    fun selectImage(uri: Uri) {
        _selectedImageUri.value = uri
        scanImage(uri)
    }

    private fun scanImage(uri: Uri) {
        viewModelScope.launch {
            _state.value = ScanState.Loading

            when (val result = repository.scanAndSave(uri)) {
                is Resource.Success -> {
                    _state.value = ScanState.Success(result.data)
                }
                is Resource.Error -> {
                    _state.value = ScanState.Error(result.message)
                }
                is Resource.Loading -> {
                    _state.value = ScanState.Loading
                }
            }
        }
    }

    fun resetState() {
        _state.value = ScanState.Idle
        _selectedImageUri.value = null
    }

    fun clearError() {
        if (_state.value is ScanState.Error) {
            _state.value = ScanState.Idle
        }
    }

    fun setError(message: String) {
        _state.value = ScanState.Error(message)
    }
}

