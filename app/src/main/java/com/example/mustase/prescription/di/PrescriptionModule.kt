package com.example.mustase.prescription.di

import androidx.room.Room
import com.example.mustase.prescription.data.local.PrescriptionDatabase
import com.example.mustase.prescription.data.remote.OcrWebService
import com.example.mustase.prescription.data.repository.PrescriptionRepository
import com.example.mustase.prescription.data.repository.ReminderRepository
import com.example.mustase.prescription.ui.viewmodel.DetailViewModel
import com.example.mustase.prescription.ui.viewmodel.HistoryViewModel
import com.example.mustase.prescription.ui.viewmodel.ReminderViewModel
import com.example.mustase.prescription.ui.viewmodel.ScanViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

val prescriptionModule = module {

    // Database Room
    single {
        Room.databaseBuilder(
            androidContext(),
            PrescriptionDatabase::class.java,
            "prescription_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // DAOs
    single { get<PrescriptionDatabase>().prescriptionDao() }
    single { get<PrescriptionDatabase>().reminderDao() }

    // OkHttpClient avec timeout augmenté pour l'OCR
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // JSON Serializer
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    // Retrofit pour OCR.space
    single {
        Retrofit.Builder()
            .baseUrl("https://api.ocr.space/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // WebService
    single<OcrWebService> { get<Retrofit>().create(OcrWebService::class.java) }

    // Repository
    single {
        PrescriptionRepository(
            ocrWebService = get(),
            prescriptionDao = get(),
            context = androidContext()
        )
    }

    single {
        ReminderRepository(
            reminderDao = get(),
            context = androidContext()
        )
    }

    // ViewModels
    viewModel { HistoryViewModel(get()) }
    viewModel { ScanViewModel(get()) }
    viewModel { (prescriptionId: Long) -> DetailViewModel(get(), get(), prescriptionId) }
    viewModel { (prescriptionId: Long) -> ReminderViewModel(get(), prescriptionId) }
}

