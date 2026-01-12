package com.example.mustase.prescription.data.repository

import android.content.Context
import com.example.mustase.prescription.data.local.ReminderDao
import com.example.mustase.prescription.data.model.ReminderEntity
import com.example.mustase.prescription.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val reminderDao: ReminderDao,
    private val context: Context
) {

    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    fun getRemindersByPrescription(prescriptionId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersByPrescription(prescriptionId)

    fun getActiveReminders(): Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()

    suspend fun getReminderById(id: Long): ReminderEntity? = reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: ReminderEntity): Long {
        val id = reminderDao.insertReminder(reminder)
        val savedReminder = reminder.copy(id = id)
        // Programmer les notifications
        ReminderScheduler.scheduleReminder(context, savedReminder)
        return id
    }

    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long> {
        val ids = reminderDao.insertReminders(reminders)
        // Programmer les notifications pour chaque rappel
        reminders.zip(ids).forEach { (reminder, id) ->
            val savedReminder = reminder.copy(id = id)
            ReminderScheduler.scheduleReminder(context, savedReminder)
        }
        return ids
    }

    suspend fun updateReminder(reminder: ReminderEntity) {
        reminderDao.updateReminder(reminder)
        // Reprogrammer les notifications
        ReminderScheduler.cancelReminder(context, reminder.id)
        if (reminder.isActive) {
            ReminderScheduler.scheduleReminder(context, reminder)
        }
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        // Annuler les notifications
        ReminderScheduler.cancelReminder(context, reminder.id)
        reminderDao.deleteReminder(reminder)
    }

    suspend fun deleteReminderById(id: Long) {
        ReminderScheduler.cancelReminder(context, id)
        reminderDao.deleteReminderById(id)
    }

    suspend fun deleteRemindersByPrescription(prescriptionId: Long) {
        val reminders = reminderDao.getRemindersByPrescription(prescriptionId)
        reminders.collect { list ->
            list.forEach { reminder ->
                ReminderScheduler.cancelReminder(context, reminder.id)
            }
        }
        reminderDao.deleteRemindersByPrescription(prescriptionId)
    }

    suspend fun setReminderActive(id: Long, isActive: Boolean) {
        reminderDao.setReminderActive(id, isActive)
        val reminder = reminderDao.getReminderById(id)
        if (reminder != null) {
            if (isActive) {
                ReminderScheduler.scheduleReminder(context, reminder)
            } else {
                ReminderScheduler.cancelReminder(context, id)
            }
        }
    }
}

