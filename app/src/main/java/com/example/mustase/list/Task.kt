package com.example.mustase.list

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Task(
    @SerialName("id")
    val id: String,
    @SerialName("content")
    val title: String,
    @SerialName("description")
    val description: String = ""
) : java.io.Serializable

