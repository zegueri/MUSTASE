package com.example.mustase.prescription

import android.app.Application
import com.example.mustase.prescription.di.prescriptionModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PrescriptionApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@PrescriptionApplication)
            modules(prescriptionModule)
        }
    }
}

