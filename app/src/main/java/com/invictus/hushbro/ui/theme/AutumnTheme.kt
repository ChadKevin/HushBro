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

private val Paper = Color(0xFFF8F6F0)
private val Ink = Color(0xFF202B25)
private val Forest = Color(0xFF315B48)
private val Leaf = Color(0xFFDDE8DC)
private val Clay = Color(0xFFB85F3D)
private val Sand = Color(0xFFECE7DD)
private val Muted = Color(0xFF6F786F)

@Stable
val AutumnColors = ColorScheme(
    background = Paper, onBackground = Ink,
    surface = Color(0xFFFFFEFB), onSurface = Ink,
    surfaceVariant = Sand, onSurfaceVariant = Muted,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1EEE6),
    surfaceContainer = Color(0xFFE9E5DB),
    surfaceContainerHigh = Color(0xFFE3DED3),
    surfaceContainerHighest = Color(0xFFD9D4C9),
    primary = Forest, onPrimary = Color.White,
    primaryContainer = Leaf, onPrimaryContainer = Color(0xFF203D2D),
    secondary = Color(0xFF667B60), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E9D9), onSecondaryContainer = Color(0xFF2F432E),
    tertiary = Clay, onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4DED2), onTertiaryContainer = Color(0xFF5E2D1B),
    error = Color(0xFFAD3D32), onError = Color.White,
    errorContainer = Color(0xFFF8E2DE), onErrorContainer = Color(0xFF641D18),
    outline = Color(0xFFB7BDB2), outlineVariant = Color(0xFFE2E1D8),
    inverseSurface = Ink, inverseOnSurface = Paper, inversePrimary = Leaf,
    scrim = Color(0x80000000), surfaceTint = Forest,
    surfaceBright = Color.White, surfaceDim = Sand
)

@Stable
val AutumnTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1.2).sp, color = Ink),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.7).sp, color = Ink),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, color = Ink),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 27.sp, lineHeight = 33.sp, letterSpacing = (-0.6).sp, color = Ink),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 29.sp, color = Ink),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 25.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp, color = Ink),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp, color = Muted),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, color = Muted),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp, color = Muted)
)

@Stable
val AutumnShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
)

@Composable
fun AutumnTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AutumnColors, typography = AutumnTypography, shapes = AutumnShapes, content = content)
}
