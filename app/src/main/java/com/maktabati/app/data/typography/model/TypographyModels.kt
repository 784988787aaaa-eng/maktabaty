package com.maktabati.app.data.typography.model

import androidx.compose.runtime.Immutable
import com.maktabati.app.data.typography.MaktabatiFontRegistry

@Immutable
enum class FontCategory(val labelAr: String, val labelEn: String = labelAr) {
    ALL("الكل", "All"),
    QURAN_HERITAGE("القرآن والتراث", "Quran & Heritage"),
    BOOKS_NARRATIVE("المتون والكتب", "Books & Narrative"),
    DIARIES_CALLIGRAPHY("المذكرات والرقعة", "Diaries & Calligraphy"),
    HEADINGS_DISPLAY("العناوين والكوفي", "Headings & Kufi"),
    USER_CUSTOM("خطوطي المضافة", "Custom Fonts")
}

@Immutable
data class FontWeightAsset(
    val weight: Int,
    val styleName: String,
    val woff2AssetPath: String
)

@Immutable
data class RegisteredArabicFont(
    val familyName: String,
    val arabicDisplayName: String,
    val cssFamilyName: String,
    val folderName: String,
    val weights: List<FontWeightAsset>,
    val resFontId: Int,
    val ttfFileName: String? = null,
    val category: FontCategory = FontCategory.BOOKS_NARRATIVE,
    val license: String = MaktabatiFontRegistry.LICENSE_SIL_OFL,
    val licenseAssetPath: String = "",
    val editorialUsage: String,
    val fallbackStack: String,
    val isUserCustom: Boolean = false,
    val customFilePath: String? = null
)

@Immutable
data class BookTypographyProfile(
    val body: String = DEFAULT_BODY_FONT,
    val headings: String = DEFAULT_HEADINGS_FONT,
    val chapterTitles: String = DEFAULT_CHAPTER_TITLES_FONT,
    val quotes: String = DEFAULT_QUOTES_FONT,
    val quran: String = DEFAULT_QURAN_FONT,
    val interfaceFont: String = DEFAULT_INTERFACE_FONT,
    val numbers: String = DEFAULT_NUMBERS_FONT
) {
    companion object {
        const val DEFAULT_BODY_FONT = "Noto Naskh Arabic"
        const val DEFAULT_HEADINGS_FONT = "Aref Ruqaa"
        const val DEFAULT_CHAPTER_TITLES_FONT = "Reem Kufi"
        const val DEFAULT_QUOTES_FONT = "Amiri"
        const val DEFAULT_QURAN_FONT = "Amiri Quran"
        const val DEFAULT_INTERFACE_FONT = "Noto Sans Arabic"
        const val DEFAULT_NUMBERS_FONT = "Noto Sans Arabic"

        // نصوص نموذجية احترافية ومحققة لكل تفصيلة في الكتاب (المتن، القرآن، الأحاديث، العناوين، الأبواب، الفهرس، الأرقام)
        const val DEFAULT_SAMPLE_BODY =
            "إِنَّمَا الكُتُبُ مَنَائِرُ الحِكْمَةِ، وَرِيَاضُ العُقُولِ البَصِيرَةِ؛ تُزْهِرُ بِهَا المَعَانِي وَتَسْمُو بِبَيَانِهَا الأَرْوَاحُ."
        const val DEFAULT_SAMPLE_QURAN =
            "﴿ اللَّهُ نُورُ السَّمَاوَاتِ وَالْأَرْضِ ۚ مَثَلُ نُورِهِ كَمِشْكَاةٍ فِيهَا مِصْبَاحٌ ﴾ [النور: ٣٥]"
        const val DEFAULT_SAMPLE_HADITH_QUOTES =
            "قالَ رسولُ اللهِ ﷺ: «إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى»"
        const val DEFAULT_SAMPLE_HEADINGS =
            "مَدارِجُ البَيانِ وَجَوامِعُ الحِكْمَةِ الخَالِدَةِ"
        const val DEFAULT_SAMPLE_CHAPTERS =
            "البَابُ الأَوَّلُ — الفَصْلُ الثَّانِي: فِي أَدَبِ العِلْمِ وَالبَصِيرَةِ"
        const val DEFAULT_SAMPLE_TOC_INTERFACE =
            "الفهرس العام: ١. فاتحة الكتاب (ص ٥) • ٢. مقاصد البيان (ص ٢٤)"
        const val DEFAULT_SAMPLE_NUMBERS =
            "الأرقام: ٠١٢٣٤٥٦٧٨٩ • ص ١٤٨ من ٣٢٠ • ١٤٤٦هـ / 2025م"
    }
}

@Immutable
data class EditorialTypographyPreset(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val profile: BookTypographyProfile
)

@Immutable
data class UserTypographyPreferences(
    val useCustomTypography: Boolean = false,
    val presetId: String = "editorial_narrative",
    val profile: BookTypographyProfile = BookTypographyProfile(),
    val customPreviewText: String = DEFAULT_CUSTOM_PREVIEW_TEXT
) {
    companion object {
        const val DEFAULT_CUSTOM_PREVIEW_TEXT = ""
        const val LEGACY_PREFILLED_PREVIEW_TEXT =
            "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ — وَمَا السَّكِينَةُ إِلَّا خَيْمَةٌ تُضْرَبُ فِي أَعْمَاقِ القَلْبِ فَتَسْكُنُ إِلَيْهَا الرُّوحُ."
    }
}
