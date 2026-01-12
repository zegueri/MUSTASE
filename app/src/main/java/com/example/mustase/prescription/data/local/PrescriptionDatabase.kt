package com.example.mustase.prescription.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mustase.prescription.data.model.PrescriptionEntity
import com.example.mustase.prescription.data.model.ReminderEntity

@Database(
    entities = [PrescriptionEntity::class, ReminderEntity::class],
    version = 3,
    exportSchema = false
)
abstract class PrescriptionDatabase : RoomDatabase() {
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun reminderDao(): ReminderDao
}

