package com.alif.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.alif.app.data.settings.AppSettings
import com.alif.app.data.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Green800,
    onPrimary = Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Gold,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = Color(0xFF4A3B08),
    tertiary = Green600,
    background = Cream,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFCF5),
    surfaceContainer = Color(0xFFF7F1E4),
    surfaceContainerHigh = CreamDeep,
    surfaceContainerHighest = Color(0xFFEDE4D0),
    outline = Color(0xFFCFC6B2),
    outlineVariant = Color(0xFFE5DCC8),
    error = Wrong,
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = GoldLight,
    onSecondary = Color(0xFF3A2E05),
    secondaryContainer = Color(0xFF4A3B08),
    onSecondaryContainer = GoldSoft,
    tertiary = Green200,
    background = NightBg,
    onBackground = NightText,
    surface = NightBg,
    onSurface = NightText,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = Color(0xFFB5BDB7),
    surfaceContainerLowest = Color(0xFF0B110E),
    surfaceContainerLow = Color(0xFF141C18),
    surfaceContainer = NightSurface,
    surfaceContainerHigh = NightSurfaceHigh,
    surfaceContainerHighest = Color(0xFF2B3A33),
    outline = Color(0xFF4A5751),
    outlineVariant = Color(0xFF34413B),
    error = Color(0xFFF08A80),
)

/** Текущие настройки доступны любому экрану без передачи параметров. */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

@Composable
fun isAppInDarkTheme(settings: AppSettings): Boolean = when (settings.themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun AlifTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val dark = isAppInDarkTheme(settings)
    CompositionLocalProvider(LocalAppSettings provides settings) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AlifTypography,
            shapes = AlifShapes,
            content = content,
        )
    }
}
