package com.example.mustase.prescription.data.model

/**
 * Représente une prescription médicale extraite du texte de l'ordonnance
 */
data class ExtractedPrescription(
    val medicationName: String,
    val dosage: String = "",
    val timesPerDay: Int = 1,
    val durationDays: Int = 7,
    val rawText: String = ""
)

