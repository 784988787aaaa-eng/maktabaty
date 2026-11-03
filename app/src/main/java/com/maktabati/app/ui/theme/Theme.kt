package com.maktabati.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import com.maktabati.app.data.typography.BookTypographyProfile
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.localization.stringsForLanguage

private val DarkColorScheme = darkColorScheme(
    primary = CoralRose,
    onPrimary = Color.White,
    primaryContainer = RoyalIndigo,
    onPrimaryContainer = Color(0xFFFFD58A),
    secondary = WarmGold,
    onSecondary = RoyalIndigo,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0E7FA),
    onPrimaryContainer = RoyalIndigo,
    secondary = CoralRose,
    onSecondary = Color.White,
    background = WarmBackground,
    onBackground = WarmText,
    surface = WarmSurface,
    onSurface = WarmText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = WarmTextMuted,
    outline = WarmBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent literary palette
    typographyProfile: BookTypographyProfile? = null,
    appLanguage: AppLanguage = AppLanguage.AR,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = remember(typographyProfile) { buildAppTypography(typographyProfile) }
    val strings = remember(appLanguage) { stringsForLanguage(appLanguage) }

    CompositionLocalProvider(
        LocalLayoutDirection provides appLanguage.layoutDirection,
        LocalAppLanguage provides appLanguage,
        LocalAppStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
