package com.example.mustase.prescription.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Couleurs personnalisées pour l'app médicale
private val LightPrimary = Color(0xFF1565C0) // Bleu médical
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFD1E4FF)
private val LightOnPrimaryContainer = Color(0xFF001D36)

private val LightSecondary = Color(0xFF00897B) // Vert santé
private val LightOnSecondary = Color.White
private val LightSecondaryContainer = Color(0xFFB2DFDB)
private val LightOnSecondaryContainer = Color(0xFF002020)

private val LightTertiary = Color(0xFF7E57C2) // Violet accent
private val LightOnTertiary = Color.White
private val LightTertiaryContainer = Color(0xFFEDE7F6)
private val LightOnTertiaryContainer = Color(0xFF1D192B)

private val LightError = Color(0xFFD32F2F)
private val LightOnError = Color.White
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

private val LightBackground = Color(0xFFFDFCFF)
private val LightOnBackground = Color(0xFF1A1C1E)
private val LightSurface = Color(0xFFFDFCFF)
private val LightOnSurface = Color(0xFF1A1C1E)
private val LightSurfaceVariant = Color(0xFFE7E0EC)
private val LightOnSurfaceVariant = Color(0xFF49454F)

// Dark mode colors
private val DarkPrimary = Color(0xFF9ECAFF)
private val DarkOnPrimary = Color(0xFF003258)
private val DarkPrimaryContainer = Color(0xFF004A77)
private val DarkOnPrimaryContainer = Color(0xFFD1E4FF)

private val DarkSecondary = Color(0xFF80CBC4)
private val DarkOnSecondary = Color(0xFF003734)
private val DarkSecondaryContainer = Color(0xFF00504D)
private val DarkOnSecondaryContainer = Color(0xFFB2DFDB)

private val DarkTertiary = Color(0xFFCDBCFF)
private val DarkOnTertiary = Color(0xFF352A5E)
private val DarkTertiaryContainer = Color(0xFF4C4076)
private val DarkOnTertiaryContainer = Color(0xFFE9DDFF)

private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkBackground = Color(0xFF1A1C1E)
private val DarkOnBackground = Color(0xFFE3E2E6)
private val DarkSurface = Color(0xFF1A1C1E)
private val DarkOnSurface = Color(0xFFE3E2E6)
private val DarkSurfaceVariant = Color(0xFF49454F)
private val DarkOnSurfaceVariant = Color(0xFFCAC4D0)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun PrescriptionScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val Typography = Typography()

