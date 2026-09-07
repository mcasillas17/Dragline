package com.mcasillas.dragline.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.mcasillas.dragline.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = TetherAmber,
    onPrimary = Color.Black,
    primaryContainer = TetherAmberDark,
    onPrimaryContainer = Color.White,
    secondary = TetherCyan,
    onSecondary = Color.Black,
    secondaryContainer = TetherCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = StatusMock,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = TetherAmberDark,
    onPrimary = Color.White,
    primaryContainer = TetherAmberLight,
    onPrimaryContainer = Color.Black,
    secondary = TetherCyanDark,
    onSecondary = Color.White,
    secondaryContainer = TetherCyanLight,
    onSecondaryContainer = Color.Black,
    tertiary = StatusMock,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun DraglineTheme(
    appTheme: AppTheme = AppTheme.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
