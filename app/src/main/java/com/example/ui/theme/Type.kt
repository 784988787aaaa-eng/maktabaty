package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal)
)

val NotoNaskhArabicFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, FontWeight.Normal)
)

val ArefRuqaaFontFamily = FontFamily(
    Font(R.font.aref_ruqaa, FontWeight.Normal)
)

val ReemKufiFontFamily = FontFamily(
    Font(R.font.reem_kufi, FontWeight.Normal)
)

val IbmPlexSansArabicFontFamily = FontFamily(
    Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal)
)

val NotoSansArabicFontFamily = FontFamily(
    Font(R.font.noto_sans_arabic, FontWeight.Normal)
)

val NotoKufiArabicFontFamily = FontFamily(
    Font(R.font.noto_kufi_arabic, FontWeight.Normal)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal)
)

fun fontFamilyForName(familyName: String): FontFamily {
    return when (familyName.trim().lowercase()) {
        "amiri" -> AmiriFontFamily
        "noto naskh arabic" -> NotoNaskhArabicFontFamily
        "aref ruqaa" -> ArefRuqaaFontFamily
        "reem kufi" -> ReemKufiFontFamily
        "ibm plex sans arabic" -> IbmPlexSansArabicFontFamily
        "noto sans arabic" -> NotoSansArabicFontFamily
        "noto kufi arabic" -> NotoKufiArabicFontFamily
        else -> NotoSansArabicFontFamily
    }
}

fun buildAppTypography(profile: com.example.data.typography.BookTypographyProfile? = null): Typography {
    if (profile == null) return Typography
    val headingsFamily = fontFamilyForName(profile.headings)
    val chapterFamily = fontFamilyForName(profile.chapterTitles)
    val bodyFamily = fontFamilyForName(profile.body)
    val interfaceFamily = fontFamilyForName(profile.interfaceFont)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = headingsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 42.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = chapterFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingsFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = interfaceFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = interfaceFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ),
        labelLarge = TextStyle(
            fontFamily = interfaceFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ),
        labelSmall = TextStyle(
            fontFamily = interfaceFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        )
    )
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ArefRuqaaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = ReemKufiFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoKufiArabicFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansArabicFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansArabicFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)
