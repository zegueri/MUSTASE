package com.example.mustase.prescription.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "Ordonnance",
    val imageUri: String,
    val extractedText: String,
    val timestamp: Long = System.currentTimeMillis()
)

