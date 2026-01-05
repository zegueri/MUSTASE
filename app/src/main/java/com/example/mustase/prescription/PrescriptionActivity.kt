package com.example.mustase.prescription

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mustase.prescription.ui.navigation.PrescriptionNavHost
import com.example.mustase.prescription.ui.theme.PrescriptionScannerTheme

class PrescriptionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrescriptionScannerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PrescriptionNavHost()
                }
            }
        }
    }
}

