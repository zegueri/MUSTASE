package com.example.mustase.prescription.data.model

/**
 * Wrapper générique pour les résultats des opérations
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

