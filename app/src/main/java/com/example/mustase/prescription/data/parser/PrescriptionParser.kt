package com.example.mustase.prescription.data.parser

import com.example.mustase.prescription.data.model.ExtractedPrescription

/**
 * Parser pour extraire les prescriptions médicales du texte OCR
 */
object PrescriptionParser {

    // Patterns pour détecter les médicaments courants et les posologies
    private val medicationPatterns = listOf(
        // Pattern: "Médicament" suivi de posologie
        """(?i)([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ]?[a-zà-ÿ]+)*\s*(?:\d+\s*(?:mg|g|ml|µg|mcg|UI))?)\s*[:\-]?\s*(\d+)\s*(?:fois|x|X)\s*(?:par\s*)?jour""".toRegex(),

        // Pattern: "Prendre X médicament N fois par jour"
        """(?i)(?:prendre|prenez)\s+(\d+)\s*(?:comprimé|gélule|dose|cachets?|cp)s?\s+(?:de\s+)?([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ]?[a-zà-ÿ]+)*(?:\s*\d+\s*(?:mg|g|ml))?)\s*[,\s]*(\d+)\s*(?:fois|x)\s*(?:par\s*)?jour""".toRegex(),

        // Pattern: "Médicament: 1 cp matin, midi et soir"
        """(?i)([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+\d+\s*(?:mg|g|ml))?)\s*[:\-]?\s*\d*\s*(?:cp|comprimé|gélule)s?\s*(?:le\s*)?(?:matin|midi|soir|au coucher)(?:\s*,?\s*(?:et\s*)?(?:le\s*)?(?:midi|soir|au coucher))*""".toRegex(),

        // Pattern simple: "Doliprane 1000mg 3x/jour"
        """(?i)([A-ZÀ-ÿ][a-zà-ÿ]+\s*\d*\s*(?:mg|g|ml|µg)?)\s*[:\-]?\s*(\d+)\s*[xX×]/?\s*(?:par\s*)?jour""".toRegex(),

        // Pattern: "Médicament - posologie"
        """(?i)([A-ZÀ-ÿ][a-zà-ÿ]+(?:\s+[A-ZÀ-ÿ]?[a-zà-ÿ]*)*\s*\d*\s*(?:mg|g|ml)?)\s*[-–]\s*(\d+)\s*(?:à|a)\s*(\d+)\s*(?:fois|x)\s*(?:par\s*)?jour""".toRegex()
    )

    // Patterns pour la durée du traitement
    private val durationPatterns = listOf(
        """(?i)(?:pendant|durant|pour)\s+(\d+)\s*(?:jours?|j\b)""".toRegex(),
        """(?i)(\d+)\s*(?:jours?|j\b)\s*(?:de\s*)?traitement""".toRegex(),
        """(?i)(?:traitement\s*(?:de|:)?\s*)?(\d+)\s*jours?""".toRegex()
    )

    // Patterns pour le nombre de fois par jour via matin/midi/soir
    private val timeOfDayPattern = """(?i)(matin|midi|soir|coucher)""".toRegex()

    /**
     * Extrait les prescriptions du texte OCR
     */
    fun extractPrescriptions(text: String): List<ExtractedPrescription> {
        val prescriptions = mutableListOf<ExtractedPrescription>()
        val processedMedications = mutableSetOf<String>()

        // Normaliser le texte
        val normalizedText = text
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        // Essayer chaque pattern
        for (pattern in medicationPatterns) {
            val matches = pattern.findAll(normalizedText)
            for (match in matches) {
                val prescription = parsePrescriptionMatch(match, normalizedText)
                if (prescription != null && prescription.medicationName.lowercase() !in processedMedications) {
                    prescriptions.add(prescription)
                    processedMedications.add(prescription.medicationName.lowercase())
                }
            }
        }

        // Si aucune prescription trouvée, essayer une extraction plus basique
        if (prescriptions.isEmpty()) {
            prescriptions.addAll(extractBasicPrescriptions(normalizedText))
        }

        return prescriptions
    }

    private fun parsePrescriptionMatch(match: MatchResult, fullText: String): ExtractedPrescription? {
        val groups = match.groupValues.filter { it.isNotBlank() }
        if (groups.size < 2) return null

        var medicationName = ""
        var timesPerDay = 1
        var dosage = ""

        // Extraire le nom du médicament et la fréquence selon le pattern
        when {
            groups.size >= 3 && groups[2].toIntOrNull() != null -> {
                medicationName = cleanMedicationName(groups[1])
                timesPerDay = groups[2].toIntOrNull() ?: 1
            }
            groups.size >= 4 && groups[1].toIntOrNull() != null -> {
                // Pattern "Prendre X médicament N fois"
                dosage = "${groups[1]} comprimé(s)"
                medicationName = cleanMedicationName(groups[2])
                timesPerDay = groups[3].toIntOrNull() ?: 1
            }
            else -> {
                medicationName = cleanMedicationName(groups[1])
                // Compter matin/midi/soir
                timesPerDay = countTimeOfDay(match.value)
            }
        }

        if (medicationName.length < 3) return null

        // Extraire la durée du traitement
        val duration = extractDuration(fullText)

        return ExtractedPrescription(
            medicationName = medicationName,
            dosage = dosage,
            timesPerDay = maxOf(1, timesPerDay),
            durationDays = duration,
            rawText = match.value.trim()
        )
    }

    private fun cleanMedicationName(name: String): String {
        return name
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("^(prendre|prenez|le|la|les|de|du|des)\\s+", RegexOption.IGNORE_CASE), "")
            .replaceFirstChar { it.uppercase() }
    }

    private fun countTimeOfDay(text: String): Int {
        val matches = timeOfDayPattern.findAll(text.lowercase())
        val times = matches.map { it.value }.toSet()
        return if (times.isNotEmpty()) times.size else 1
    }

    private fun extractDuration(text: String): Int {
        for (pattern in durationPatterns) {
            val match = pattern.find(text)
            if (match != null) {
                val days = match.groupValues[1].toIntOrNull()
                if (days != null && days in 1..365) {
                    return days
                }
            }
        }
        return 7 // Durée par défaut
    }

    private fun extractBasicPrescriptions(text: String): List<ExtractedPrescription> {
        val prescriptions = mutableListOf<ExtractedPrescription>()

        // Liste de médicaments courants à rechercher
        val commonMedications = listOf(
            "doliprane", "paracétamol", "paracetamol", "ibuprofène", "ibuprofene", "advil",
            "aspirine", "aspirin", "amoxicilline", "amoxicillin", "augmentin",
            "oméprazole", "omeprazole", "pantoprazole", "gaviscon",
            "ventoline", "salbutamol", "aerius", "zyrtec", "cetirizine",
            "levothyrox", "metformine", "metformin", "atorvastatine",
            "tramadol", "codéine", "codeine", "morphine",
            "xanax", "lexomil", "seresta", "stilnox", "zolpidem",
            "kardegic", "plavix", "eliquis", "xarelto",
            "voltarène", "voltarene", "ketoprofene", "kétoprofène"
        )

        val lowerText = text.lowercase()

        for (medication in commonMedications) {
            if (lowerText.contains(medication)) {
                // Essayer de trouver le contexte autour du médicament
                val index = lowerText.indexOf(medication)
                val contextStart = maxOf(0, index - 20)
                val contextEnd = minOf(text.length, index + medication.length + 50)
                val context = text.substring(contextStart, contextEnd)

                // Chercher la fréquence dans le contexte
                val freqPattern = """(\d+)\s*(?:fois|x|X)\s*(?:par\s*)?jour""".toRegex()
                val freqMatch = freqPattern.find(context)
                val timesPerDay = freqMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1

                prescriptions.add(
                    ExtractedPrescription(
                        medicationName = medication.replaceFirstChar { it.uppercase() },
                        timesPerDay = timesPerDay,
                        durationDays = extractDuration(text),
                        rawText = context.trim()
                    )
                )
            }
        }

        return prescriptions.distinctBy { it.medicationName.lowercase() }
    }
}

