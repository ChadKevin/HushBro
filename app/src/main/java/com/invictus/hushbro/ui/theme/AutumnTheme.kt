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

@Stable
val AutumnColors = ColorScheme(
    background = Color(0xFFF7F2E9),
    onBackground = Color(0xFF382E29),
    surface = Color(0xFFFFFCF6),
    surfaceContainerHighest = Color(0xFFF1E7D8),
    surfaceContainer = Color(0xFFE9DECC),
    surfaceContainerHigh = Color(0xFFEFE4D3),
    surfaceContainerLow = Color(0xFFF5EEE3),
    surfaceContainerLowest = Color(0xFFFFFCF6),
    surfaceVariant = Color(0xFFE5D9CA),
    onSurfaceVariant = Color(0xFF81766C),
    primary = Color(0xFF795548),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEFE2D2),
    onPrimaryContainer = Color(0xFF3E2723),
    secondary = Color(0xFF87967A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E0D3),
    onSecondaryContainer = Color(0xFF382E29),
    tertiary = Color(0xFFC87955),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5D7C8),
    onTertiaryContainer = Color(0xFF4E342E),
    error = Color(0xFFB64E42),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9D6D3),
    onErrorContainer = Color(0xFF7A2E29),
    onSurface = Color(0xFF382E29),
    outline = Color(0xFFE5D9CA),
    outlineVariant = Color(0xFFE5D9CA),
    inverseSurface = Color(0xFF382E29),
    inverseOnSurface = Color(0xFFF7F2E9),
    inversePrimary = Color(0xFFC87955),
    scrim = Color(0x803E2723),
    surfaceTint = Color(0xFF795548),
    surfaceBright = Color(0xFFFFFCF6),
    surfaceDim = Color(0xFFF1E7D8),
)
@Stable
val AutumnTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W400, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.25).sp, color = Color(0xFF382E29)),
    displayMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500, fontSize = 28.sp, lineHeight = 35.sp, letterSpacing = 0.sp, color = Color(0xFF382E29)),
    displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500, fontSize = 24.sp, lineHeight = 31.sp, color = Color(0xFF382E29)),
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W600, fontSize = 22.sp, lineHeight = 28.sp, color = Color(0xFF382E29)),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W600, fontSize = 20.sp, lineHeight = 26.sp, color = Color(0xFF382E29)),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.W500, fontSize = 18.sp, lineHeight = 24.sp, color = Color(0xFF382E29)),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W600, fontSize = 18.sp, lineHeight = 24.sp, color = Color(0xFF382E29)),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500, fontSize = 16.sp, lineHeight = 22.sp, color = Color(0xFF382E29)),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF382E29)),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp, color = Color(0xFF382E29)),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 21.sp, color = Color(0xFF382E29)),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF81766C)),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF382E29)),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W500, fontSize = 12.sp, lineHeight = 16.sp, color = Color(0xFF81766C)),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.W400, fontSize = 10.sp, lineHeight = 14.sp, color = Color(0xFF81766C)),
)

@Stable
val AutumnShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
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

