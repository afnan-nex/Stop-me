package com.afnan.stopme.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.afnan.stopme.domain.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Indigo300,
    onPrimary = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = Indigo200,
    secondary = Indigo400,
    onSecondary = Navy900,
    secondaryContainer = Navy500,
    onSecondaryContainer = Indigo200,
    background = Navy900,
    onBackground = OffWhite,
    surface = Navy800,
    onSurface = OffWhite,
    surfaceVariant = Navy700,
    onSurfaceVariant = LightGray,
    outline = MidGray,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = Navy700,
    onPrimary = White,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = Navy700,
    secondary = Indigo400,
    onSecondary = White,
    secondaryContainer = Indigo200,
    onSecondaryContainer = Navy700,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = ErrorRed,
    onError = White
)

@Composable
fun StopMeTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (theme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemDark
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StopMeTypography,
        shapes = StopMeShapes,
        content = content
    )
}
