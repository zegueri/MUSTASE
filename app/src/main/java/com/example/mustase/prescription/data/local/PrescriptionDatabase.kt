package com.example.mustase.prescription.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mustase.prescription.data.model.PrescriptionEntity

@Database(
    entities = [PrescriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrescriptionDatabase : RoomDatabase() {
    abstract fun prescriptionDao(): PrescriptionDao
}

