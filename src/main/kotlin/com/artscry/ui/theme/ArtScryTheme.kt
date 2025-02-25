package com.artscry.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF6B5B95)
val DarkPurple = Color(0xFF4A3D71)
val Teal = Color(0xFF03DAC6)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceElevated = Color(0xFF2D2D2D)

val LightBackground = Color.White
val LightSurface = Color(0xFFF5F5F5)

private val DarkColorPalette = darkColors(
    primary = Purple,
    primaryVariant = DarkPurple,
    secondary = Teal,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorPalette = lightColors(
    primary = Purple,
    primaryVariant = DarkPurple,
    secondary = Teal,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

object ThemeState {
    var isDarkMode by mutableStateOf(false)
}

class ArtScryColors(
    val surfaceElevated: Color,
    val textSecondary: Color,
    val divider: Color,
    val inputField: Color,
    val inputFieldBorder: Color,
    val cardBorder: Color,
    val buttonText: Color
)

val LocalArtScryColors = staticCompositionLocalOf {
    ArtScryColors(
        surfaceElevated = Color.Unspecified,
        textSecondary = Color.Unspecified,
        divider = Color.Unspecified,
        inputField = Color.Unspecified,
        inputFieldBorder = Color.Unspecified,
        cardBorder = Color.Unspecified,
        buttonText = Color.Unspecified
    )
}

@Composable
fun ArtScryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    LaunchedEffect(darkTheme) {
        ThemeState.isDarkMode = darkTheme
    }

    val colors = if (ThemeState.isDarkMode) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val additionalColors = if (ThemeState.isDarkMode) {
        ArtScryColors(
            surfaceElevated = DarkSurfaceElevated,
            textSecondary = Color.White.copy(alpha = 0.7f),
            divider = Color.White.copy(alpha = 0.1f),
            inputField = DarkSurfaceElevated,
            inputFieldBorder = Color.White.copy(alpha = 0.2f),
            cardBorder = Purple.copy(alpha = 0.3f),
            buttonText = Color.White
        )
    } else {
        ArtScryColors(
            surfaceElevated = Color.White,
            textSecondary = Color.Black.copy(alpha = 0.6f),
            divider = Color.Black.copy(alpha = 0.1f),
            inputField = Color.White,
            inputFieldBorder = Color.Black.copy(alpha = 0.1f),
            cardBorder = Purple.copy(alpha = 0.2f),
            buttonText = Purple
        )
    }

    CompositionLocalProvider(
        LocalArtScryColors provides additionalColors
    ) {
        MaterialTheme(
            colors = colors,
            content = content
        )
    }
}

val MaterialTheme.artScryColors: ArtScryColors
    @Composable
    get() = LocalArtScryColors.current