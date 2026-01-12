package com.example.mustase.prescription.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mustase.prescription.data.model.ExtractedPrescription
import com.example.mustase.prescription.data.model.ReminderEntity
import com.example.mustase.prescription.data.repository.ReminderRepository
import com.example.mustase.prescription.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val prescriptionId: Long
) : ViewModel() {

    private val _reminders = MutableStateFlow<List<ReminderEntity>>(emptyList())
    val reminders: StateFlow<List<ReminderEntity>> = _reminders.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            repository.getRemindersByPrescription(prescriptionId).collect { list ->
                _reminders.value = list
            }
        }
    }

    fun createReminder(
        prescription: ExtractedPrescription,
        timesPerDay: Int,
        durationDays: Int,
        reminderTimes: List<String>
    ) {
        viewModelScope.launch {
            try {
                _saveState.value = SaveState.Saving

                val reminder = ReminderEntity(
                    prescriptionId = prescriptionId,
                    medicationName = prescription.medicationName,
                    dosage = prescription.dosage,
                    timesPerDay = timesPerDay,
                    durationDays = durationDays,
                    reminderTimes = ReminderScheduler.timesToJson(reminderTimes),
                    isActive = true
                )

                repository.insertReminder(reminder)
                _saveState.value = SaveState.Success
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                repository.updateReminder(reminder)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erreur lors de la mise à jour")
            }
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminder)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erreur lors de la suppression")
            }
        }
    }

    fun toggleReminderActive(reminder: ReminderEntity) {
        viewModelScope.launch {
            try {
                repository.setReminderActive(reminder.id, !reminder.isActive)
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Erreur lors de la modification")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    sealed class SaveState {
        data object Idle : SaveState()
        data object Saving : SaveState()
        data object Success : SaveState()
        data class Error(val message: String) : SaveState()
    }
}

