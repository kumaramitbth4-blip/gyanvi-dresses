package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AppManagementConfig
import com.example.model.AppTypographyConfig
import com.example.model.DashboardThemePreset

val LocalAppConfig = staticCompositionLocalOf { AppManagementConfig() }

private fun lightSchemeFor(preset: DashboardThemePreset) = lightColorScheme(
    primary = preset.accentColor,
    onPrimary = Color.White,
    primaryContainer = preset.containerColor,
    onPrimaryContainer = preset.accentColor,
    secondary = preset.primaryColor,
    onSecondary = TextPrimary,
    secondaryContainer = preset.containerColor,
    onSecondaryContainer = TextPrimary,
    tertiary = GreenRating,
    onTertiary = Color.White,
    background = preset.backgroundColor,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = preset.containerColor,
    onSurfaceVariant = TextSecondary,
    outline = preset.primaryColor.copy(alpha = 0.5f)
)

private fun darkSchemeFor(preset: DashboardThemePreset) = darkColorScheme(
    primary = preset.primaryColor,
    onPrimary = Color.Black,
    primaryContainer = preset.accentColor,
    onPrimaryContainer = Color.White,
    secondary = preset.accentColor,
    onSecondary = Color.White,
    background = Color(0xFF1A1812),
    onBackground = Color(0xFFFFF9E6),
    surface = Color(0xFF262319),
    onSurface = Color(0xFFFFF9E6),
    surfaceVariant = Color(0xFF363122),
    onSurfaceVariant = Color(0xFFE2DCBE)
)

@Composable
fun GyanviDressesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    managementConfig: AppManagementConfig = AppManagementConfig(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkSchemeFor(managementConfig.themePreset)
    } else {
        lightSchemeFor(managementConfig.themePreset)
    }
    val typography = buildGyanviTypography(managementConfig.typography)

    CompositionLocalProvider(LocalAppConfig provides managementConfig) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
