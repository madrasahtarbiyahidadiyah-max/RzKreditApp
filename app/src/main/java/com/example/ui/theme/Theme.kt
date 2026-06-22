package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ObsidianColorScheme = darkColorScheme(
    primary = ColorPayEmerald,
    onPrimary = Color.White,
    secondary = ColorHoldSky,
    onSecondary = Color(0xFF060913),
    tertiary = ColorWarnAmber,
    error = ColorDangerRose,
    background = ObsidianBg,
    onBackground = ObsidianTextMain,
    surface = ObsidianPanel,
    onSurface = ObsidianTextMain,
    surfaceVariant = ObsidianLedger,
    onSurfaceVariant = ObsidianTextSub,
    outline = Color(0xFF1E293B)
)

private val PearlColorScheme = lightColorScheme(
    primary = ColorPayEmeraldDark,
    onPrimary = Color.White,
    secondary = ColorHoldSkyDark,
    onSecondary = Color.White,
    tertiary = ColorWarnAmberDark,
    error = ColorDangerRoseDark,
    background = PearlBg,
    onBackground = PearlTextMain,
    surface = PearlPanel,
    onSurface = PearlTextMain,
    surfaceVariant = PearlLedger,
    onSurfaceVariant = PearlTextSub,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianColorScheme else PearlColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
