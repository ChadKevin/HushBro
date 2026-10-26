package com.invictus.hushbro.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Coffee & Autumn color palette ─────────────────────────────────────────
// background: warm cream paper
// surface:    latte foam white
// primary:    espresso brown
// secondary:  sage leaf green
// tertiary:   cinnamon / terracotta accent
// error:      burnt umber

@Stable
val AutumnColors = ColorScheme(
    background = Color(0xFFFAF6EF),
    onBackground = Color(0xFF3E2F23),
    surface = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFEDE0CC),
    surfaceContainer = Color(0xFFE5D6C2),
    surfaceContainerHigh = Color(0xFFEADCCB),
    surfaceContainerLow = Color(0xFFF3E9DA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2D3C0),
    onSurfaceVariant = Color(0xFF8C7A6A),
    primary = Color(0xFF6F4E37),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3E5D7),
    onPrimaryContainer = Color(0xFF4A3325),
    secondary = Color(0xFF8A9A7D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDEE6D2),
    onSecondaryContainer = Color(0xFF3E2F23),
    tertiary = Color(0xFFC87955),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF7D9C5),
    onTertiaryContainer = Color(0xFF5E3A2A),
    error = Color(0xFF9C4A42),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFAE4E0),
    onErrorContainer = Color(0xFF6E2E28),
    onSurface = Color(0xFF3E2F23),
    outline = Color(0xFFE2D3C0),
    outlineVariant = Color(0xFFEDE0CC),
    inverseSurface = Color(0xFF3E2F23),
    inverseOnSurface = Color(0xFFFAF6EF),
    inversePrimary = Color(0xFFC87955),
    scrim = Color(0x803E2F23),
    surfaceTint = Color(0xFF6F4E37),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFEDE0CC),
)
@Stable
val AutumnTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W400,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.5).sp,
        color = Color(0xFF3E2F23)
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500,
        fontSize = 34.sp, lineHeight = 42.sp, letterSpacing = (-0.25).sp,
        color = Color(0xFF3E2F23)
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500,
        fontSize = 28.sp, lineHeight = 36.sp, color = Color(0xFF3E2F23)
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W600,
        fontSize = 26.sp, lineHeight = 32.sp, color = Color(0xFF3E2F23)
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W600,
        fontSize = 22.sp, lineHeight = 28.sp, color = Color(0xFF3E2F23)
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500,
        fontSize = 18.sp, lineHeight = 24.sp, color = Color(0xFF3E2F23)
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W600,
        fontSize = 18.sp, lineHeight = 24.sp, color = Color(0xFF3E2F23)
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500,
        fontSize = 16.sp, lineHeight = 22.sp, color = Color(0xFF3E2F23)
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500,
        fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF3E2F23)
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400,
        fontSize = 16.sp, lineHeight = 24.sp, color = Color(0xFF3E2F23)
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400,
        fontSize = 14.sp, lineHeight = 21.sp, color = Color(0xFF3E2F23)
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400,
        fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF8C7A6A)
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500,
        fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF3E2F23)
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500,
        fontSize = 12.sp, lineHeight = 16.sp, color = Color(0xFF8C7A6A)
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400,
        fontSize = 10.sp, lineHeight = 14.sp, color = Color(0xFF8C7A6A)
    ),
)

@Stable
val AutumnShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun AutumnTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColors() else AutumnColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AutumnTypography,
        shapes = AutumnShapes,
        content = content
    )
}

private fun darkColors() = ColorScheme(
    background = Color(0xFF2A241F),
    onBackground = Color(0xFFF7F2E9),
    surface = Color(0xFF332B22),
    surfaceContainerHighest = Color(0xFF4A3C30),
    surfaceContainer = Color(0xFF3E3327),
    surfaceContainerHigh = Color(0xFF44382C),
    surfaceContainerLow = Color(0xFF382F25),
    surfaceContainerLowest = Color(0xFF2A241F),
    surfaceVariant = Color(0xFF5A4B3D),
    onSurfaceVariant = Color(0xFFB5A994),
    primary = Color(0xFFC87955),
    onPrimary = Color(0xFF2A241F),
    primaryContainer = Color(0xFF795548),
    onPrimaryContainer = Color(0xFFF7F2E9),
    secondary = Color(0xFFA3B48F),
    onSecondary = Color(0xFF2A241F),
    secondaryContainer = Color(0xFF87967A),
    onSecondaryContainer = Color(0xFF2A241F),
    tertiary = Color(0xFFD6AD60),
    onTertiary = Color(0xFF2A241F),
    tertiaryContainer = Color(0xFF876E4F),
    onTertiaryContainer = Color(0xFFF7F2E9),
    error = Color(0xFFD97A6F),
    onError = Color(0xFF2A241F),
    errorContainer = Color(0xFFB64E42),
    onErrorContainer = Color(0xFFF7F2E9),
    onSurface = Color(0xFFF7F2E9),
    outline = Color(0xFF5A4B3D),
    outlineVariant = Color(0xFF5A4B3D),
    inverseSurface = Color(0xFFF7F2E9),
    inverseOnSurface = Color(0xFF2A241F),
    inversePrimary = Color(0xFF795548),
    scrim = Color(0xB0000000),
    surfaceTint = Color(0xFFC87955),
    surfaceBright = Color(0xFF4A3C30),
    surfaceDim = Color(0xFF3E3327),
)

