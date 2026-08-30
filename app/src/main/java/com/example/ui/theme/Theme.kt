package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light, Happy, Premium 3D Color Scheme
private val LightGameColorScheme = lightColorScheme(
    primary = BrightBlue,
    onPrimary = Color.White,
    secondary = VividPurple,
    onSecondary = Color.White,
    tertiary = CandyPink,
    onTertiary = Color.White,
    background = LightSkyBlue,
    onBackground = TextDeepNavy,
    surface = SurfaceLight,
    onSurface = TextDeepNavy,
    surfaceVariant = SurfaceLightCard,
    onSurfaceVariant = TextSecondaryNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Forced Light & Vibrant Theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightGameColorScheme,
        typography = Typography,
        content = content
    )
}
