package com.maktabati.app.ui.localization

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

@Immutable
enum class AppLanguage(
    val code: String,
    val isRtl: Boolean,
    val displayLabel: String,
    val badgeText: String
) {
    AR(
        code = "ar",
        isRtl = true,
        displayLabel = "العربية",
        badgeText = "AR"
    ),
    EN(
        code = "en",
        isRtl = false,
        displayLabel = "English",
        badgeText = "EN"
    );

    val layoutDirection: LayoutDirection
        get() = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    fun toggle(): AppLanguage = if (this == AR) EN else AR

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code?.trim(), ignoreCase = true) } ?: AR
        }
    }
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.AR }
