package com.example.mustase.prescription.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getLong(KEY_REMINDER_ID, -1)
        val medicationName = inputData.getString(KEY_MEDICATION_NAME) ?: return Result.failure()
        val dosage = inputData.getString(KEY_DOSAGE) ?: ""

        if (reminderId == -1L) return Result.failure()

        NotificationHelper.showMedicationReminder(
            context = applicationContext,
            reminderId = reminderId,
            medicationName = medicationName,
            dosage = dosage
        )

        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminder_id"
        const val KEY_MEDICATION_NAME = "medication_name"
        const val KEY_DOSAGE = "dosage"
    }
}

