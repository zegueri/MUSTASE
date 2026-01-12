package com.example.mustase.prescription.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mustase.prescription.data.model.ReminderEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Programme tous les rappels pour un médicament
     */
    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        if (!reminder.isActive) return

        val reminderTimes = parseReminderTimes(reminder.reminderTimes)
        val workManager = WorkManager.getInstance(context)

        val calendar = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = reminder.startDate
        }

        // Pour chaque jour de traitement
        for (day in 0 until reminder.durationDays) {
            // Pour chaque heure de rappel
            for ((index, time) in reminderTimes.withIndex()) {
                val (hour, minute) = time.split(":").map { it.toInt() }

                val reminderCalendar = Calendar.getInstance().apply {
                    timeInMillis = startCalendar.timeInMillis
                    add(Calendar.DAY_OF_YEAR, day)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                // Ne programmer que les rappels futurs
                if (reminderCalendar.timeInMillis > calendar.timeInMillis) {
                    val delay = reminderCalendar.timeInMillis - calendar.timeInMillis

                    val inputData = Data.Builder()
                        .putLong(ReminderWorker.KEY_REMINDER_ID, reminder.id)
                        .putString(ReminderWorker.KEY_MEDICATION_NAME, reminder.medicationName)
                        .putString(ReminderWorker.KEY_DOSAGE, reminder.dosage)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .addTag("reminder_${reminder.id}")
                        .addTag("reminder_${reminder.id}_day${day}_time$index")
                        .build()

                    workManager.enqueueUniqueWork(
                        "reminder_${reminder.id}_day${day}_time$index",
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }
        }
    }

    /**
     * Annule tous les rappels pour un médicament
     */
    fun cancelReminder(context: Context, reminderId: Long) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("reminder_$reminderId")
    }

    /**
     * Annule tous les rappels
     */
    fun cancelAllReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }

    /**
     * Parse les heures de rappel depuis le JSON
     */
    private fun parseReminderTimes(timesJson: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(timesJson)
        } catch (_: Exception) {
            listOf("08:00") // Valeur par défaut
        }
    }

    /**
     * Génère des heures de rappel par défaut selon le nombre de prises par jour
     */
    fun generateDefaultTimes(timesPerDay: Int): List<String> {
        return when (timesPerDay) {
            1 -> listOf("08:00")
            2 -> listOf("08:00", "20:00")
            3 -> listOf("08:00", "12:00", "20:00")
            4 -> listOf("08:00", "12:00", "16:00", "20:00")
            else -> (0 until timesPerDay).map {
                val hour = 8 + (it * 16 / timesPerDay)
                String.format(Locale.getDefault(), "%02d:00", hour)
            }
        }
    }

    /**
     * Convertit une liste d'heures en JSON
     */
    fun timesToJson(times: List<String>): String {
        return json.encodeToString(times)
    }
}

