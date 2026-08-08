package io.github.nikolareljin.pharos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColors = darkColorScheme(
    primary = PharosAmber,
    onPrimary = PharosSlate900,
    secondary = PharosSky,
    onSecondary = PharosSlate900,
    tertiary = PharosFlame,
    background = PharosSlate900,
    onBackground = PharosSlate50,
    surface = PharosSlate900,
    onSurface = PharosSlate50,
    surfaceVariant = PharosSlate800,
    onSurfaceVariant = PharosSlate200,
    outline = PharosSlate700,
    error = PharosError,
)

private val LightColors = lightColorScheme(
    primary = PharosSkyDeep,
    onPrimary = PharosSlate50,
    secondary = PharosAmber,
    onSecondary = PharosSlate900,
    background = PharosSlate50,
    onBackground = PharosSlate900,
    surface = PharosSlate50,
    onSurface = PharosSlate900,
    surfaceVariant = PharosSlate200,
    onSurfaceVariant = PharosSlate800,
    outline = PharosSlate700,
    error = PharosError,
)

/**
 * Type sized for a screen across a room rather than a phone in a hand. Every
 * size here is larger than the Material default: a 14sp body is unreadable from
 * a sofa, and a display node is read at three metres or not at all.
 */
private val PharosTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 18.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun PharosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PharosTypography,
        content = content,
    )
}
