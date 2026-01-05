package com.example.mustase.prescription.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrResponse(
    @SerialName("ParsedResults")
    val parsedResults: List<ParsedResult>? = null,
    @SerialName("OCRExitCode")
    val ocrExitCode: Int,
    @SerialName("IsErroredOnProcessing")
    val isErroredOnProcessing: Boolean,
    @SerialName("ErrorMessage")
    val errorMessage: List<String>? = null
)

@Serializable
data class ParsedResult(
    @SerialName("ParsedText")
    val parsedText: String? = null,
    @SerialName("ErrorMessage")
    val errorMessage: String? = null,
    @SerialName("ErrorDetails")
    val errorDetails: String? = null
)

