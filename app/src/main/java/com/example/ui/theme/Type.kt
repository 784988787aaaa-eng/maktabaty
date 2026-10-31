package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val AmiriFontFamily = FontFamily(Font(R.font.amiri, FontWeight.Normal))
val AmiriQuranFontFamily = FontFamily(Font(R.font.amiri_quran, FontWeight.Normal))
val ScheherazadeNewFontFamily = FontFamily(Font(R.font.scheherazade_new, FontWeight.Normal))
val LateefFontFamily = FontFamily(Font(R.font.lateef, FontWeight.Normal))
val NotoNaskhArabicFontFamily = FontFamily(Font(R.font.noto_naskh_arabic, FontWeight.Normal))
val ArefRuqaaFontFamily = FontFamily(Font(R.font.aref_ruqaa, FontWeight.Normal))
val ReemKufiFontFamily = FontFamily(Font(R.font.reem_kufi, FontWeight.Normal))
val IbmPlexSansArabicFontFamily = FontFamily(Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal))
val NotoSansArabicFontFamily = FontFamily(Font(R.font.noto_sans_arabic, FontWeight.Normal))
val NotoKufiArabicFontFamily = FontFamily(Font(R.font.noto_kufi_arabic, FontWeight.Normal))
val CairoFontFamily = FontFamily(Font(R.font.cairo, FontWeight.Normal))
val TajawalFontFamily = FontFamily(Font(R.font.tajawal, FontWeight.Normal))
val AlmaraiFontFamily = FontFamily(Font(R.font.almarai, FontWeight.Normal))
val ReadexProFontFamily = FontFamily(Font(R.font.readex_pro, FontWeight.Normal))
val ChangaFontFamily = FontFamily(Font(R.font.changa, FontWeight.Normal))
val ElMessiriFontFamily = FontFamily(Font(R.font.el_messiri, FontWeight.Normal))
val HarmattanFontFamily = FontFamily(Font(R.font.harmattan, FontWeight.Normal))
val LalezarFontFamily = FontFamily(Font(R.font.lalezar, FontWeight.Normal))
val RakkasFontFamily = FontFamily(Font(R.font.rakkas, FontWeight.Normal))
val MarheyFontFamily = FontFamily(Font(R.font.marhey, FontWeight.Normal))
val LemonadaFontFamily = FontFamily(Font(R.font.lemonada, FontWeight.Normal))
val KatibehFontFamily = FontFamily(Font(R.font.katibeh, FontWeight.Normal))
val MirzaFontFamily = FontFamily(Font(R.font.mirza, FontWeight.Normal))
val MarkaziTextFontFamily = FontFamily(Font(R.font.markazi_text, FontWeight.Normal))
val VibesFontFamily = FontFamily(Font(R.font.vibes, FontWeight.Normal))

fun fontFamilyForName(familyName: String): FontFamily {
    val registeredFont = com.example.data.typography.MaktabatiFontRegistry.findFont(familyName)
    if (registeredFont?.isUserCustom == true && !registeredFont.customFilePath.isNullOrBlank()) {
        val customFile = java.io.File(registeredFont.customFilePath)
        if (customFile.exists() && customFile.canRead()) {
            val loadedFamily = runCatching {
                FontFamily(Font(customFile, FontWeight.Normal))
            }.getOrNull()
            if (loadedFamily != null) {
                return loadedFamily
            }
        }
    }

    return when (familyName.trim().lowercase()) {
        "amiri" -> AmiriFontFamily
        "amiri quran" -> AmiriQuranFontFamily
        "scheherazade new" -> ScheherazadeNewFontFamily
        "lateef" -> LateefFontFamily
        "noto naskh arabic" -> NotoNaskhArabicFontFamily
        "aref ruqaa" -> ArefRuqaaFontFamily
        "reem kufi" -> ReemKufiFontFamily
        "ibm plex sans arabic" -> IbmPlexSansArabicFontFamily
        "noto sans arabic" -> NotoSansArabicFontFamily
        "noto kufi arabic" -> NotoKufiArabicFontFamily
        "cairo" -> CairoFontFamily
        "tajawal" -> TajawalFontFamily
        "almarai" -> AlmaraiFontFamily
        "readex pro" -> ReadexProFontFamily
        "changa" -> ChangaFontFamily
        "el messiri" -> ElMessiriFontFamily
        "harmattan" -> HarmattanFontFamily
        "lalezar" -> LalezarFontFamily
        "rakkas" -> RakkasFontFamily
        "marhey" -> MarheyFontFamily
        "lemonada" -> LemonadaFontFamily
        "katibeh" -> KatibehFontFamily
        "mirza" -> MirzaFontFamily
        "markazi text" -> MarkaziTextFontFamily
        "vibes" -> VibesFontFamily
        else -> NotoNaskhArabicFontFamily
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
