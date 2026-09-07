package com.panda.keyboards.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PandaPrimaryDark,
    onPrimary = DarkSurface,
    primaryContainer = PandaPrimary,
    onPrimaryContainer = Purple80,
    secondary = PandaSecondaryDark,
    onSecondary = DarkSurface,
    secondaryContainer = PandaSecondary,
    onSecondaryContainer = PandaSecondaryDark,
    tertiary = PandaTertiaryDark,
    onTertiary = DarkSurface,
    tertiaryContainer = PandaTertiary,
    onTertiaryContainer = PandaTertiaryDark,
    background = DarkSurface,
    onBackground = Purple80,
    surface = DarkSurface,
    onSurface = Purple80,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PurpleGrey80,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceVariant,
    outline = PurpleGrey40,
    outlineVariant = DarkSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = PandaPrimary,
    onPrimary = LightSurface,
    primaryContainer = Purple80,
    onPrimaryContainer = Purple40,
    secondary = PandaSecondary,
    onSecondary = LightSurface,
    secondaryContainer = PandaSecondaryDark,
    onSecondaryContainer = PandaSecondary,
    tertiary = PandaTertiary,
    onTertiary = LightSurface,
    tertiaryContainer = PandaTertiaryDark,
    onTertiaryContainer = PandaTertiary,
    background = LightSurface,
    onBackground = DarkSurface,
    surface = LightSurface,
    onSurface = DarkSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = PurpleGrey40,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceVariant,
    outline = PurpleGrey40,
    outlineVariant = LightSurfaceVariant
)

/**
 * Panda Keyboards Material 3 theme.
 *
 * Uses dynamic color on Android 12+ for a personalized feel,
 * falling back to the curated Panda brand palette on older versions.
 */
@Composable
fun PandaKeyboardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PandaTypography,
        content = content
    )
}
