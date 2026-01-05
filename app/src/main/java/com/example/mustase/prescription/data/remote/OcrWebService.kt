package com.example.mustase.prescription.data.remote

import com.example.mustase.prescription.data.model.OcrResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OcrWebService {

    @Multipart
    @POST("parse/image")
    suspend fun parseImage(
        @Header("apikey") apiKey: String,
        @Part file: MultipartBody.Part,
        @Part("language") language: RequestBody,
        @Part("isOverlayRequired") isOverlayRequired: RequestBody
    ): Response<OcrResponse>
}

