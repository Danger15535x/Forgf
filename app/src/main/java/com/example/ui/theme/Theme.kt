package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = DeepBlueOnPrimary,
    primaryContainer = NeonBlueContainer,
    onPrimaryContainer = HighContrastTextContainer,
    secondary = CosmicIndigo,
    onSecondary = DeepIndigoOnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = SkyBlue,
    onTertiary = SkyBlueOnTertiary,
    tertiaryContainer = SkyBlueContainer,
    onTertiaryContainer = OnSkyBlueContainer,
    background = DeepSpaceBackground,
    onBackground = SlateText,
    surface = DeepSpaceBackground,
    onSurface = SlateText,
    surfaceVariant = TranslucentSurface,
    onSurfaceVariant = SubduedSlateText,
    outline = OutlineGrey,
    outlineVariant = OutlineVariantGrey,
    error = ErrorRed,
    onError = OnErrorRed,
    errorContainer = ErrorContainerRed,
    onErrorContainer = OnErrorContainerRed
)

// We maintain a consistent dark-mode look for the high-tech, secret call-copilot brand feel.
private val LightColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors by default to preserve custom branding aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepSpaceBackground.toArgb()
            window.navigationBarColor = DeepSpaceBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
