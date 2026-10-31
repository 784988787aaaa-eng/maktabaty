package com.example.data.typography

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import com.example.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

enum class FontCategory(val labelAr: String) {
    ALL("الكل"),
    QURAN_HERITAGE("القرآن والتراث"),
    BOOKS_NARRATIVE("المتون والكتب"),
    DIARIES_CALLIGRAPHY("المذكرات والرقعة"),
    HEADINGS_DISPLAY("العناوين والكوفي"),
    USER_CUSTOM("خطوطي المضافة")
}

data class FontWeightAsset(
    val weight: Int,
    val styleName: String,
    val woff2AssetPath: String
)

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
    }
}

data class EditorialTypographyPreset(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val profile: BookTypographyProfile
)

data class UserTypographyPreferences(
    val useCustomTypography: Boolean = false,
    val presetId: String = "editorial_narrative",
    val profile: BookTypographyProfile = BookTypographyProfile(),
    val customPreviewText: String = DEFAULT_CUSTOM_PREVIEW_TEXT
) {
    companion object {
        const val DEFAULT_CUSTOM_PREVIEW_TEXT =
            "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ — وَمَا السَّكِينَةُ إِلَّا خَيْمَةٌ تُضْرَبُ فِي أَعْمَاقِ القَلْبِ فَتَسْكُنُ إِلَيْهَا الرُّوحُ."
    }
}

/**
 * Central Arabic Typography Registry & Custom User Font Manager for the Maktabati («مكتبتي») platform.
 *
 * Includes:
 * - 25 offline bundled Arabic fonts covering Quranic Mushaf & Heritage, Book Body & Literature,
 *   Diaries & Ruqaa/Farsi Calligraphy, and Modern Kufi & Display Headings.
 * - Full support for User-Imported Custom Fonts (.ttf / .otf / .woff2) persisted on device
 *   and served seamlessly via `https://maktabati.app/user-fonts/<fileName>`.
 */
object MaktabatiFontRegistry {

    const val LICENSE_SIL_OFL = "SIL Open Font License 1.1"
    private const val USER_FONTS_PREFS_KEY = "typo_user_custom_fonts_json"

    // Mutable runtime list of user-imported fonts
    private val userCustomFonts = mutableListOf<RegisteredArabicFont>()

    val builtInFonts: List<RegisteredArabicFont> = listOf(
        // ================= 1. القرآن والتراث (Quran & Heritage) =================
        RegisteredArabicFont(
            familyName = "Amiri Quran",
            arabicDisplayName = "الأميري القرآني (Amiri Quran)",
            cssFamilyName = "Amiri Quran",
            folderName = "amiri-quran",
            weights = emptyList(),
            resFontId = R.font.amiri_quran,
            ttfFileName = "amiri_quran.ttf",
            category = FontCategory.QURAN_HERITAGE,
            editorialUsage = "الآيات القرآنية الكريمة والتشكيل المصحفي الكامل",
            fallbackStack = "\"Amiri Quran\", \"Scheherazade New\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Scheherazade New",
            arabicDisplayName = "شهرزاد المصحفي (Scheherazade)",
            cssFamilyName = "Scheherazade New",
            folderName = "scheherazade-new",
            weights = emptyList(),
            resFontId = R.font.scheherazade_new,
            ttfFileName = "scheherazade_new.ttf",
            category = FontCategory.QURAN_HERITAGE,
            editorialUsage = "رسم الآيات القرآنية والأحاديث النبوية والمتون التراثية",
            fallbackStack = "\"Scheherazade New\", \"Amiri Quran\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Amiri",
            arabicDisplayName = "الأميري التراثي (Amiri)",
            cssFamilyName = "Amiri",
            folderName = "amiri",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/amiri/amiri-400.woff2"),
                FontWeightAsset(700, "Bold", "fonts/amiri/amiri-700.woff2")
            ),
            resFontId = R.font.amiri,
            ttfFileName = "amiri.ttf",
            category = FontCategory.QURAN_HERITAGE,
            licenseAssetPath = "fonts/amiri/OFL.txt",
            editorialUsage = "النصوص الأدبية والكتب الكلاسيكية والاقتباسات",
            fallbackStack = "\"Amiri\", \"Noto Naskh Arabic\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Lateef",
            arabicDisplayName = "لطيف التراثي المبسوط (Lateef)",
            cssFamilyName = "Lateef",
            folderName = "lateef",
            weights = emptyList(),
            resFontId = R.font.lateef,
            ttfFileName = "lateef.ttf",
            category = FontCategory.QURAN_HERITAGE,
            editorialUsage = "المخطوطات الأندلسية والحكم والشروح الكلاسيكية",
            fallbackStack = "\"Lateef\", \"Scheherazade New\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Harmattan",
            arabicDisplayName = "هرمتان المشرقي الواضح (Harmattan)",
            cssFamilyName = "Harmattan",
            folderName = "harmattan",
            weights = emptyList(),
            resFontId = R.font.harmattan,
            ttfFileName = "harmattan.ttf",
            category = FontCategory.QURAN_HERITAGE,
            editorialUsage = "النصوص المشكولة والأدعية المأثورة والقراءة المريحة",
            fallbackStack = "\"Harmattan\", \"Noto Naskh Arabic\", serif"
        ),

        // ================= 2. المتون والكتب (Books & Narrative) =================
        RegisteredArabicFont(
            familyName = "Noto Naskh Arabic",
            arabicDisplayName = "نوتو نسخ السردي (Noto Naskh)",
            cssFamilyName = "Noto Naskh Arabic",
            folderName = "noto-naskh-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-naskh-arabic/noto-naskh-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-naskh-arabic/noto-naskh-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-naskh-arabic/noto-naskh-arabic-700.woff2")
            ),
            resFontId = R.font.noto_naskh_arabic,
            ttfFileName = "noto_naskh_arabic.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            licenseAssetPath = "fonts/noto-naskh-arabic/OFL.txt",
            editorialUsage = "نصوص المتن الطويلة والقراءة السردية",
            fallbackStack = "\"Noto Naskh Arabic\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Markazi Text",
            arabicDisplayName = "مركزي لطباعة الكتب (Markazi)",
            cssFamilyName = "Markazi Text",
            folderName = "markazi-text",
            weights = emptyList(),
            resFontId = R.font.markazi_text,
            ttfFileName = "markazi_text.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            editorialUsage = "طباعة الروايات والكتب الأدبية الحديثة بأناقة دور النشر",
            fallbackStack = "\"Markazi Text\", \"Noto Naskh Arabic\", serif"
        ),
        RegisteredArabicFont(
            familyName = "IBM Plex Sans Arabic",
            arabicDisplayName = "آي بي إم بليكس المعاصر (IBM Plex)",
            cssFamilyName = "IBM Plex Sans Arabic",
            folderName = "ibm-plex-sans-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-700.woff2")
            ),
            resFontId = R.font.ibm_plex_sans_arabic,
            ttfFileName = "ibm_plex_sans_arabic.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            licenseAssetPath = "fonts/ibm-plex-sans-arabic/OFL.txt",
            editorialUsage = "الكتب الفكرية والمعرفية المعاصرة",
            fallbackStack = "\"IBM Plex Sans Arabic\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Readex Pro",
            arabicDisplayName = "ريدكس برو القرائي (Readex Pro)",
            cssFamilyName = "Readex Pro",
            folderName = "readex-pro",
            weights = emptyList(),
            resFontId = R.font.readex_pro,
            ttfFileName = "readex_pro.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            editorialUsage = "القراءة الطويلة المريحة للعين على شاشات الهواتف",
            fallbackStack = "\"Readex Pro\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Almarai",
            arabicDisplayName = "المراعي النقي (Almarai)",
            cssFamilyName = "Almarai",
            folderName = "almarai",
            weights = emptyList(),
            resFontId = R.font.almarai,
            ttfFileName = "almarai.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            editorialUsage = "الكتب العلمية والتربوية ذات الصفاء البصري العالي",
            fallbackStack = "\"Almarai\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Tajawal",
            arabicDisplayName = "تجول الانسيابي (Tajawal)",
            cssFamilyName = "Tajawal",
            folderName = "tajawal",
            weights = emptyList(),
            resFontId = R.font.tajawal,
            ttfFileName = "tajawal.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            editorialUsage = "كتب تطوير الذات والرسائل العصرية الهادئة",
            fallbackStack = "\"Tajawal\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Cairo",
            arabicDisplayName = "القاهرة الحديث (Cairo)",
            cssFamilyName = "Cairo",
            folderName = "cairo",
            weights = emptyList(),
            resFontId = R.font.cairo,
            ttfFileName = "cairo.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            editorialUsage = "المتون الحديثة والواجهات والفهارس المتوازنة",
            fallbackStack = "\"Cairo\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Noto Sans Arabic",
            arabicDisplayName = "نوتو سانس الواضح (Noto Sans)",
            cssFamilyName = "Noto Sans Arabic",
            folderName = "noto-sans-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-sans-arabic/noto-sans-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-sans-arabic/noto-sans-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-sans-arabic/noto-sans-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-sans-arabic/noto-sans-arabic-700.woff2")
            ),
            resFontId = R.font.noto_sans_arabic,
            ttfFileName = "noto_sans_arabic.ttf",
            category = FontCategory.BOOKS_NARRATIVE,
            licenseAssetPath = "fonts/noto-sans-arabic/OFL.txt",
            editorialUsage = "واجهة التطبيق والفهارس والبيانات الوصفية والأرقام",
            fallbackStack = "\"Noto Sans Arabic\", \"IBM Plex Sans Arabic\", sans-serif"
        ),

        // ================= 3. المذكرات والرقعة والخط الحر (Diaries & Calligraphy) =================
        RegisteredArabicFont(
            familyName = "Aref Ruqaa",
            arabicDisplayName = "عارف رقعة الأدبي (Aref Ruqaa)",
            cssFamilyName = "Aref Ruqaa",
            folderName = "aref-ruqaa",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/aref-ruqaa/aref-ruqaa-400.woff2"),
                FontWeightAsset(700, "Bold", "fonts/aref-ruqaa/aref-ruqaa-700.woff2")
            ),
            resFontId = R.font.aref_ruqaa,
            ttfFileName = "aref_ruqaa.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            licenseAssetPath = "fonts/aref-ruqaa/OFL.txt",
            editorialUsage = "العناوين الأدبية والرسائل الوجدانية والاقتباسات",
            fallbackStack = "\"Aref Ruqaa\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Marhey",
            arabicDisplayName = "مرحي الحر للمذكرات (Marhey)",
            cssFamilyName = "Marhey",
            folderName = "marhey",
            weights = emptyList(),
            resFontId = R.font.marhey,
            ttfFileName = "marhey.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            editorialUsage = "كتابة المذكرات اليومية والهمسات والبطاقات التفاعلية",
            fallbackStack = "\"Marhey\", \"Aref Ruqaa\", cursive"
        ),
        RegisteredArabicFont(
            familyName = "Lemonada",
            arabicDisplayName = "ليمونادة الوجداني (Lemonada)",
            cssFamilyName = "Lemonada",
            folderName = "lemonada",
            weights = emptyList(),
            resFontId = R.font.lemonada,
            ttfFileName = "lemonada.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            editorialUsage = "اليوميات الدافئة والرسائل القلبية وكتب الفتيات",
            fallbackStack = "\"Lemonada\", \"Noto Sans Arabic\", cursive"
        ),
        RegisteredArabicFont(
            familyName = "Mirza",
            arabicDisplayName = "ميرزا الفارسي الأدبي (Mirza)",
            cssFamilyName = "Mirza",
            folderName = "mirza",
            weights = emptyList(),
            resFontId = R.font.mirza,
            ttfFileName = "mirza.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            editorialUsage = "الشعر والخواطر الأدبية والبطاقات التذكارية",
            fallbackStack = "\"Mirza\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Katibeh",
            arabicDisplayName = "كتيبة الديواني المورق (Katibeh)",
            cssFamilyName = "Katibeh",
            folderName = "katibeh",
            weights = emptyList(),
            resFontId = R.font.katibeh,
            ttfFileName = "katibeh.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            editorialUsage = "ترويسات الرسائل وعناوين المذكرات والإهداءات",
            fallbackStack = "\"Katibeh\", \"Aref Ruqaa\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Vibes",
            arabicDisplayName = "فايبس التوقيع اليدوي (Vibes)",
            cssFamilyName = "Vibes",
            folderName = "vibes",
            weights = emptyList(),
            resFontId = R.font.vibes,
            ttfFileName = "vibes.ttf",
            category = FontCategory.DIARIES_CALLIGRAPHY,
            editorialUsage = "الأوسمة الوجدانية والتوقيعات في ختام الرسائل",
            fallbackStack = "\"Vibes\", \"Aref Ruqaa\", cursive"
        ),

        // ================= 4. العناوين والكوفي (Headings & Kufi) =================
        RegisteredArabicFont(
            familyName = "Reem Kufi",
            arabicDisplayName = "ريم كوفي الهندسي (Reem Kufi)",
            cssFamilyName = "Reem Kufi",
            folderName = "reem-kufi",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/reem-kufi/reem-kufi-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/reem-kufi/reem-kufi-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/reem-kufi/reem-kufi-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/reem-kufi/reem-kufi-700.woff2")
            ),
            resFontId = R.font.reem_kufi,
            ttfFileName = "reem_kufi.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            licenseAssetPath = "fonts/reem-kufi/OFL.txt",
            editorialUsage = "عناوين الفصول والأبواب والغلاف الملكي",
            fallbackStack = "\"Reem Kufi\", \"Noto Kufi Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "El Messiri",
            arabicDisplayName = "المسيري الأندلسي الرشيق (El Messiri)",
            cssFamilyName = "El Messiri",
            folderName = "el-messiri",
            weights = emptyList(),
            resFontId = R.font.el_messiri,
            ttfFileName = "el_messiri.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            editorialUsage = "عناوين الكتب الراقية وفواتح الأبواب والبطاقات",
            fallbackStack = "\"El Messiri\", \"Reem Kufi\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Noto Kufi Arabic",
            arabicDisplayName = "نوتو كوفي الحديث (Noto Kufi)",
            cssFamilyName = "Noto Kufi Arabic",
            folderName = "noto-kufi-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-kufi-arabic/noto-kufi-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-kufi-arabic/noto-kufi-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-kufi-arabic/noto-kufi-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-kufi-arabic/noto-kufi-arabic-700.woff2")
            ),
            resFontId = R.font.noto_kufi_arabic,
            ttfFileName = "noto_kufi_arabic.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            licenseAssetPath = "fonts/noto-kufi-arabic/OFL.txt",
            editorialUsage = "العناوين الحديثة والتصنيفات والشارات",
            fallbackStack = "\"Noto Kufi Arabic\", \"Reem Kufi\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Changa",
            arabicDisplayName = "تشانجا الهندسي المتزن (Changa)",
            cssFamilyName = "Changa",
            folderName = "changa",
            weights = emptyList(),
            resFontId = R.font.changa,
            ttfFileName = "changa.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            editorialUsage = "العناوين الفرعية والبطاقات المعلوماتية والأرقام",
            fallbackStack = "\"Changa\", \"Noto Kufi Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Rakkas",
            arabicDisplayName = "رقاص الزخرفي البارز (Rakkas)",
            cssFamilyName = "Rakkas",
            folderName = "rakkas",
            weights = emptyList(),
            resFontId = R.font.rakkas,
            ttfFileName = "rakkas.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            editorialUsage = "عناوين الأغلفة الكبرى واللوحات الاستفتاحية",
            fallbackStack = "\"Rakkas\", \"Aref Ruqaa\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Lalezar",
            arabicDisplayName = "لاليزار الجريء للعناوين (Lalezar)",
            cssFamilyName = "Lalezar",
            folderName = "lalezar",
            weights = emptyList(),
            resFontId = R.font.lalezar,
            ttfFileName = "lalezar.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            editorialUsage = "الشارات البارزة وعناوين المحطات الرئيسية",
            fallbackStack = "\"Lalezar\", \"Reem Kufi\", sans-serif"
        )
    )

    val registeredFonts: List<RegisteredArabicFont>
        get() = builtInFonts + userCustomFonts

    private fun normalizeFamilyKey(name: String): String {
        return name.trim().lowercase().replace(Regex("[_\\-]+"), " ").replace(Regex("\\s+"), " ")
    }

    fun findFont(familyName: String?): RegisteredArabicFont? {
        if (familyName.isNullOrBlank()) return null
        val trimmed = familyName.trim()
        if (trimmed.contains("http://", ignoreCase = true) ||
            trimmed.contains("https://", ignoreCase = true) ||
            trimmed.contains("url(", ignoreCase = true) ||
            trimmed.contains("/")
        ) {
            return null
        }
        val key = normalizeFamilyKey(trimmed)
        return registeredFonts.firstOrNull { normalizeFamilyKey(it.familyName) == key }
    }

    fun isApprovedFont(familyName: String?): Boolean = findFont(familyName) != null

    fun validateOrFallback(familyName: String?, defaultFamily: String): String {
        return findFont(familyName)?.cssFamilyName ?: defaultFamily
    }

    fun resolveFallbackStack(familyName: String, defaultFamily: String = BookTypographyProfile.DEFAULT_BODY_FONT): String {
        return (findFont(familyName) ?: findFont(defaultFamily))?.fallbackStack
            ?: "\"Noto Naskh Arabic\", \"Amiri\", serif"
    }

    /**
     * Imports a user font file (.ttf / .otf / .woff2) from Android SAF Uri into `filesDir/user_fonts/`
     * and registers it immediately in `MaktabatiFontRegistry`.
     */
    fun importUserCustomFont(
        context: Context,
        prefs: android.content.SharedPreferences,
        uri: Uri,
        customDisplayName: String? = null
    ): Result<RegisteredArabicFont> {
        return runCatching {
            val rawName = queryDisplayName(context, uri) ?: "custom_font_${System.currentTimeMillis()}.ttf"
            val ext = rawName.substringAfterLast('.', "ttf").lowercase()
            require(ext in setOf("ttf", "otf", "woff2", "woff")) {
                "يرجى اختيار ملف خط صالح بصيغة TTF أو OTF أو WOFF2"
            }

            val baseTitle = customDisplayName?.trim()?.takeIf { it.isNotEmpty() }
                ?: rawName.substringBeforeLast('.')
                    .replace(Regex("[_\\-]+"), " ")
                    .trim()
                    .ifEmpty { "خط مخصص ${userCustomFonts.size + 1}" }

            val sanitizedSlug = baseTitle.lowercase()
                .replace(Regex("[^a-z0-9\\u0600-\\u06FF]+"), "_")
                .trim('_')
                .ifEmpty { "user_font_${System.currentTimeMillis()}" }

            val userFontsDir = File(context.filesDir, "user_fonts").apply { mkdirs() }
            val fileName = "${sanitizedSlug}.$ext"
            val destFile = File(userFontsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("تعذر قراءة ملف الخط المحدد")

            require(destFile.length() > 256) { "ملف الخط فارغ أو غير صالح" }

            val cssFamily = "UserFont_$sanitizedSlug"
            val newFont = RegisteredArabicFont(
                familyName = cssFamily,
                arabicDisplayName = "$baseTitle (خطي المضاف)",
                cssFamilyName = cssFamily,
                folderName = "user-$sanitizedSlug",
                weights = emptyList(),
                resFontId = R.font.noto_naskh_arabic,
                ttfFileName = fileName,
                category = FontCategory.USER_CUSTOM,
                editorialUsage = "خط مخصص أضافه القارئ من جهازه",
                fallbackStack = "\"$cssFamily\", \"Noto Naskh Arabic\", \"Amiri\", serif",
                isUserCustom = true,
                customFilePath = destFile.absolutePath
            )

            userCustomFonts.removeAll { normalizeFamilyKey(it.familyName) == normalizeFamilyKey(cssFamily) }
            userCustomFonts.add(0, newFont)
            persistUserCustomFonts(prefs)
            newFont
        }
    }

    fun removeUserCustomFont(
        prefs: android.content.SharedPreferences,
        familyName: String
    ) {
        val target = userCustomFonts.firstOrNull { it.familyName == familyName } ?: return
        target.customFilePath?.let { runCatching { File(it).delete() } }
        userCustomFonts.remove(target)
        persistUserCustomFonts(prefs)
    }

    fun loadUserCustomFonts(context: Context, prefs: android.content.SharedPreferences) {
        val rawJson = prefs.getString(USER_FONTS_PREFS_KEY, null) ?: return
        runCatching {
            val arr = JSONArray(rawJson)
            val loaded = mutableListOf<RegisteredArabicFont>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val path = obj.optString("customFilePath", "")
                val file = File(path)
                if (path.isNotBlank() && file.isFile) {
                    val cssFamily = obj.getString("familyName")
                    val display = obj.getString("arabicDisplayName")
                    val fileName = obj.getString("ttfFileName")
                    loaded.add(
                        RegisteredArabicFont(
                            familyName = cssFamily,
                            arabicDisplayName = display,
                            cssFamilyName = cssFamily,
                            folderName = obj.optString("folderName", "user-font"),
                            weights = emptyList(),
                            resFontId = R.font.noto_naskh_arabic,
                            ttfFileName = fileName,
                            category = FontCategory.USER_CUSTOM,
                            editorialUsage = "خط مخصص أضافه القارئ من جهازه",
                            fallbackStack = "\"$cssFamily\", \"Noto Naskh Arabic\", \"Amiri\", serif",
                            isUserCustom = true,
                            customFilePath = file.absolutePath
                        )
                    )
                }
            }
            userCustomFonts.clear()
            userCustomFonts.addAll(loaded)
        }
    }

    private fun persistUserCustomFonts(prefs: android.content.SharedPreferences) {
        val arr = JSONArray()
        for (f in userCustomFonts) {
            val obj = JSONObject()
            obj.put("familyName", f.familyName)
            obj.put("arabicDisplayName", f.arabicDisplayName)
            obj.put("folderName", f.folderName)
            obj.put("ttfFileName", f.ttfFileName ?: "")
            obj.put("customFilePath", f.customFilePath ?: "")
            arr.put(obj)
        }
        prefs.edit().putString(USER_FONTS_PREFS_KEY, arr.toString()).apply()
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        }.getOrNull() ?: uri.lastPathSegment
    }

    fun parseManifestTypography(manifestFile: File?): BookTypographyProfile? {
        if (manifestFile == null || !manifestFile.isFile) return null
        return runCatching {
            parseManifestTypographyJson(manifestFile.readText(Charsets.UTF_8))
        }.getOrNull()
    }

    fun parseManifestTypographyJson(jsonText: String): BookTypographyProfile? {
        if (jsonText.isBlank()) return null
        return runCatching {
            val root = JSONObject(jsonText)
            val typoObj = root.optJSONObject("typography") ?: return null
            BookTypographyProfile(
                body = validateOrFallback(
                    typoObj.optString("body", ""),
                    BookTypographyProfile.DEFAULT_BODY_FONT
                ),
                headings = validateOrFallback(
                    typoObj.optString("headings", ""),
                    BookTypographyProfile.DEFAULT_HEADINGS_FONT
                ),
                chapterTitles = validateOrFallback(
                    typoObj.optString("chapterTitles", ""),
                    BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT
                ),
                quotes = validateOrFallback(
                    typoObj.optString("quotes", ""),
                    BookTypographyProfile.DEFAULT_QUOTES_FONT
                ),
                quran = validateOrFallback(
                    typoObj.optString("quran", ""),
                    BookTypographyProfile.DEFAULT_QURAN_FONT
                ),
                interfaceFont = validateOrFallback(
                    typoObj.optString("interface", ""),
                    BookTypographyProfile.DEFAULT_INTERFACE_FONT
                ),
                numbers = validateOrFallback(
                    typoObj.optString("numbers", ""),
                    BookTypographyProfile.DEFAULT_NUMBERS_FONT
                )
            )
        }.getOrNull()
    }

    /**
     * Builds exact `@font-face` declarations for all registered Arabic font families
     * (WOFF2 assets, bundled TTF resources under `/fonts/<name>.ttf`, and user custom fonts under `/user-fonts/<name>`).
     */
    fun buildOfflineFontFacesCss(baseUrlPrefix: String = "https://maktabati.app/assets"): String {
        val sb = StringBuilder()
        val cleanPrefix = baseUrlPrefix.trimEnd('/')
        val rootDomain = if (cleanPrefix.contains("/assets")) cleanPrefix.substringBefore("/assets") else "https://maktabati.app"

        for (font in registeredFonts) {
            if (font.isUserCustom && !font.ttfFileName.isNullOrBlank()) {
                val ext = font.ttfFileName.substringAfterLast('.', "ttf").lowercase()
                val fmt = when (ext) {
                    "woff2" -> "woff2"
                    "woff" -> "woff"
                    "otf" -> "opentype"
                    else -> "truetype"
                }
                sb.appendLine(
                    """
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/user-fonts/${font.ttfFileName}") format("$fmt");
                      font-weight: 400;
                      font-style: normal;
                      font-display: swap;
                    }
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/user-fonts/${font.ttfFileName}") format("$fmt");
                      font-weight: 700;
                      font-style: normal;
                      font-display: swap;
                    }
                    """.trimIndent()
                )
            } else if (font.weights.isNotEmpty()) {
                for (weightSpec in font.weights) {
                    sb.appendLine(
                        """
                        @font-face {
                          font-family: "${font.cssFamilyName}";
                          src: url("$cleanPrefix/${weightSpec.woff2AssetPath}") format("woff2");
                          font-weight: ${weightSpec.weight};
                          font-style: normal;
                          font-display: swap;
                        }
                        """.trimIndent()
                    )
                }
            } else if (!font.ttfFileName.isNullOrBlank()) {
                sb.appendLine(
                    """
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/fonts/${font.ttfFileName}") format("truetype");
                      font-weight: 400;
                      font-style: normal;
                      font-display: swap;
                    }
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/fonts/${font.ttfFileName}") format("truetype");
                      font-weight: 700;
                      font-style: normal;
                      font-display: swap;
                    }
                    """.trimIndent()
                )
            }
        }
        return sb.toString()
    }

    val editorialPresets: List<EditorialTypographyPreset> = listOf(
        EditorialTypographyPreset(
            id = "editorial_narrative",
            titleAr = "الأصالة السردية",
            subtitleAr = "نسخ للمتن ورقعة للعناوين وأميري قرآني للآيات",
            profile = BookTypographyProfile(
                body = "Noto Naskh Arabic",
                headings = "Aref Ruqaa",
                chapterTitles = "Reem Kufi",
                quotes = "Amiri",
                quran = "Amiri Quran",
                interfaceFont = "Noto Sans Arabic",
                numbers = "Noto Sans Arabic"
            )
        ),
        EditorialTypographyPreset(
            id = "classical_diwan",
            titleAr = "الديوان الكلاسيكي",
            subtitleAr = "طابع تراثي بالأميري للمتن وشهرزاد للآيات والدرر",
            profile = BookTypographyProfile(
                body = "Amiri",
                headings = "Aref Ruqaa",
                chapterTitles = "Reem Kufi",
                quotes = "Scheherazade New",
                quran = "Amiri Quran",
                interfaceFont = "Noto Sans Arabic",
                numbers = "Noto Sans Arabic"
            )
        ),
        EditorialTypographyPreset(
            id = "diaries_whispers",
            titleAr = "مذكرات وهمسات",
            subtitleAr = "لمسة دافئة بخط ليمونادة ومرحي للمذكرات وكتيبة للعناوين",
            profile = BookTypographyProfile(
                body = "Tajawal",
                headings = "Katibeh",
                chapterTitles = "El Messiri",
                quotes = "Marhey",
                quran = "Amiri Quran",
                interfaceFont = "Tajawal",
                numbers = "Tajawal"
            )
        ),
        EditorialTypographyPreset(
            id = "modern_intellectual",
            titleAr = "الإشراق المعاصر",
            subtitleAr = "قراءة فكرية حديثة بخط IBM Plex والكوفي الحديث",
            profile = BookTypographyProfile(
                body = "IBM Plex Sans Arabic",
                headings = "Noto Kufi Arabic",
                chapterTitles = "Reem Kufi",
                quotes = "Noto Naskh Arabic",
                quran = "Scheherazade New",
                interfaceFont = "IBM Plex Sans Arabic",
                numbers = "IBM Plex Sans Arabic"
            )
        ),
        EditorialTypographyPreset(
            id = "kufi_bayan",
            titleAr = "البيان الكوفي",
            subtitleAr = "حضور هندسي واضح للعناوين مع متن مركزي ورقعة للدرر",
            profile = BookTypographyProfile(
                body = "Markazi Text",
                headings = "El Messiri",
                chapterTitles = "Reem Kufi",
                quotes = "Aref Ruqaa",
                quran = "Amiri Quran",
                interfaceFont = "Noto Sans Arabic",
                numbers = "Noto Sans Arabic"
            )
        )
    )

    fun loadUserTypographyPreferences(
        prefs: android.content.SharedPreferences,
        context: Context? = null
    ): UserTypographyPreferences {
        if (context != null) {
            loadUserCustomFonts(context, prefs)
        }
        val useCustom = prefs.getBoolean("typo_use_custom", false)
        val presetId = prefs.getString("typo_preset_id", "editorial_narrative") ?: "editorial_narrative"
        val customPreview = prefs.getString(
            "typo_custom_preview_text",
            UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT
        )?.takeIf { it.isNotBlank() } ?: UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT

        val profile = BookTypographyProfile(
            body = validateOrFallback(prefs.getString("typo_body", null), BookTypographyProfile.DEFAULT_BODY_FONT),
            headings = validateOrFallback(prefs.getString("typo_headings", null), BookTypographyProfile.DEFAULT_HEADINGS_FONT),
            chapterTitles = validateOrFallback(prefs.getString("typo_chapter_titles", null), BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT),
            quotes = validateOrFallback(prefs.getString("typo_quotes", null), BookTypographyProfile.DEFAULT_QUOTES_FONT),
            quran = validateOrFallback(prefs.getString("typo_quran", null), BookTypographyProfile.DEFAULT_QURAN_FONT),
            interfaceFont = validateOrFallback(prefs.getString("typo_interface", null), BookTypographyProfile.DEFAULT_INTERFACE_FONT),
            numbers = validateOrFallback(prefs.getString("typo_numbers", null), BookTypographyProfile.DEFAULT_NUMBERS_FONT)
        )
        return UserTypographyPreferences(
            useCustomTypography = useCustom,
            presetId = presetId,
            profile = profile,
            customPreviewText = customPreview
        )
    }

    fun saveUserTypographyPreferences(
        prefs: android.content.SharedPreferences,
        userPrefs: UserTypographyPreferences
    ) {
        prefs.edit()
            .putBoolean("typo_use_custom", userPrefs.useCustomTypography)
            .putString("typo_preset_id", userPrefs.presetId)
            .putString("typo_custom_preview_text", userPrefs.customPreviewText)
            .putString("typo_body", validateOrFallback(userPrefs.profile.body, BookTypographyProfile.DEFAULT_BODY_FONT))
            .putString("typo_headings", validateOrFallback(userPrefs.profile.headings, BookTypographyProfile.DEFAULT_HEADINGS_FONT))
            .putString("typo_chapter_titles", validateOrFallback(userPrefs.profile.chapterTitles, BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT))
            .putString("typo_quotes", validateOrFallback(userPrefs.profile.quotes, BookTypographyProfile.DEFAULT_QUOTES_FONT))
            .putString("typo_quran", validateOrFallback(userPrefs.profile.quran, BookTypographyProfile.DEFAULT_QURAN_FONT))
            .putString("typo_interface", validateOrFallback(userPrefs.profile.interfaceFont, BookTypographyProfile.DEFAULT_INTERFACE_FONT))
            .putString("typo_numbers", validateOrFallback(userPrefs.profile.numbers, BookTypographyProfile.DEFAULT_NUMBERS_FONT))
            .apply()
    }

    /**
     * Builds the CSS custom property block and role bindings for a book's validated `BookTypographyProfile`.
     */
    fun buildTypographyProfileCss(profile: BookTypographyProfile, important: Boolean = false): String {
        val imp = if (important) " !important" else ""
        val bodyStack = resolveFallbackStack(profile.body, BookTypographyProfile.DEFAULT_BODY_FONT)
        val headingsStack = resolveFallbackStack(profile.headings, BookTypographyProfile.DEFAULT_HEADINGS_FONT)
        val chapterStack = resolveFallbackStack(profile.chapterTitles, BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT)
        val quotesStack = resolveFallbackStack(profile.quotes, BookTypographyProfile.DEFAULT_QUOTES_FONT)
        val quranStack = resolveFallbackStack(profile.quran, BookTypographyProfile.DEFAULT_QURAN_FONT)
        val interfaceStack = resolveFallbackStack(profile.interfaceFont, BookTypographyProfile.DEFAULT_INTERFACE_FONT)
        val numbersStack = resolveFallbackStack(profile.numbers, BookTypographyProfile.DEFAULT_NUMBERS_FONT)

        return """
            :root {
              --font-body: $bodyStack$imp;
              --font-headings: $headingsStack$imp;
              --font-chapter-titles: $chapterStack$imp;
              --font-quotes: $quotesStack$imp;
              --font-quran: $quranStack$imp;
              --font-interface: $interfaceStack$imp;
              --font-numbers: $numbersStack$imp;
              --reader-family: var(--font-body)$imp;
            }
            html, body, .page, .book-page, .page-body, .page-content, #pageInnerContent,
            p, li, .reading p, .dedication p, .closing p, .subtitle {
              font-family: var(--font-body)$imp;
            }
            h1, h2, h3, h4, h5, h6, .page-heading, .section-title, .reading .section-opening,
            .cover-page h1, .cover-title, .dedication h1, .closing h1 {
              font-family: var(--font-headings)$imp;
            }
            .opener h1, .chapter-opener h1, .chapter-title, .chapter-number, .chapter-badge,
            .toc-item.toc-chapter {
              font-family: var(--font-chapter-titles)$imp;
            }
            .quote-card, .quote-text, .quote-box, .pullquote, .wisdom-pearl, .manal-voice,
            .heart-keepsake-card, blockquote, q {
              font-family: var(--font-quotes)$imp;
            }
            .quran-verse, .ayah-box, .mushaf-verse, .quran-ayah, [data-role="quran"] {
              font-family: var(--font-quran)$imp;
            }
            .top-bar, .topbar, .brand, .icon-btn, .nav-btn, .running-header, .eyebrow, .primary-btn,
            .panel-head h2, .field, #searchInput, .search-result, .search-result strong,
            .toc-item, .wisdom-badge, .manal-badge, .quote-caption, .cover-page .author {
              font-family: var(--font-interface)$imp;
            }
            .page-counter, .page-number, .search-result-page, .toc-item small, .search-result small {
              font-family: var(--font-numbers)$imp;
              font-variant-numeric: tabular-nums;
            }
        """.trimIndent()
    }

    /**
     * Generates JavaScript that live-updates `<style id="maktabati-user-typography-override">` inside
     * the active WebView so user font preferences (including custom imported fonts) take effect immediately.
     */
    fun buildLiveTypographyOverrideJs(userPrefs: UserTypographyPreferences): String {
        if (!userPrefs.useCustomTypography) {
            return """
                (function() {
                    const el = document.getElementById('maktabati-user-typography-override');
                    if (el) el.remove();
                })();
            """.trimIndent()
        }
        val fontFaces = buildOfflineFontFacesCss("https://maktabati.app/assets")
        val css = (fontFaces + "\n" + buildTypographyProfileCss(userPrefs.profile, important = true))
            .replace("\\", "\\\\")
            .replace("`", "\\`")
        return """
            (function() {
                let el = document.getElementById('maktabati-user-typography-override');
                if (!el) {
                    el = document.createElement('style');
                    el.id = 'maktabati-user-typography-override';
                    (document.head || document.documentElement).appendChild(el);
                }
                el.textContent = `$css`;
                if (window.MaktabatiLayoutGuard && typeof window.MaktabatiLayoutGuard.inspectAndHeal === 'function') {
                    window.MaktabatiLayoutGuard.inspectAndHeal();
                }
            })();
        """.trimIndent()
    }

    fun toJsonRegistry(): String {
        val root = JSONObject()
        root.put("version", "2.0.0")
        root.put("OfflineOnly", true)
        val fontsArr = JSONArray()
        for (f in registeredFonts) {
            val fObj = JSONObject()
            fObj.put("familyName", f.familyName)
            fObj.put("arabicDisplayName", f.arabicDisplayName)
            fObj.put("cssFamilyName", f.cssFamilyName)
            fObj.put("folder", "assets/fonts/${f.folderName}/")
            fObj.put("category", f.category.name)
            fObj.put("license", f.license)
            fObj.put("licensePath", "assets/${f.licenseAssetPath}")
            fObj.put("editorialUsage", f.editorialUsage)
            fObj.put("fallbackStack", f.fallbackStack)
            val wArr = JSONArray()
            for (w in f.weights) {
                val wObj = JSONObject()
                wObj.put("weight", w.weight)
                wObj.put("styleName", w.styleName)
                wObj.put("woff2Path", "assets/${w.woff2AssetPath}")
                wArr.put(wObj)
            }
            fObj.put("weights", wArr)
            fontsArr.put(fObj)
        }
        root.put("fonts", fontsArr)
        return root.toString(2)
    }

    /**
     * Intercepts font requests from WebView (for `https://maktabati.app/assets/fonts/...`,
     * `https://maktabati.app/fonts/...`, `https://maktabati.app/user-fonts/...`, and book-relative paths)
     * and serves them 100% offline.
     */
    fun interceptFontRequest(
        context: Context,
        requestPath: String?,
        bookDir: File? = null
    ): WebResourceResponse? {
        if (requestPath.isNullOrBlank()) return null

        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Cache-Control" to "public, max-age=31536000"
        )

        // 0. User-imported custom fonts under /user-fonts/<fileName>
        if (requestPath.startsWith("/user-fonts/")) {
            val fileName = requestPath.substringAfterLast("/")
            val userFontsDir = File(context.filesDir, "user_fonts")
            val targetFile = File(userFontsDir, fileName)
            if (targetFile.isFile && targetFile.canonicalPath.startsWith(userFontsDir.canonicalPath)) {
                val mime = when {
                    fileName.endsWith(".woff2", true) -> "font/woff2"
                    fileName.endsWith(".woff", true) -> "font/woff"
                    fileName.endsWith(".otf", true) -> "font/otf"
                    else -> "font/ttf"
                }
                return runCatching {
                    WebResourceResponse(
                        mime,
                        null,
                        200,
                        "OK",
                        headers,
                        BufferedInputStream(FileInputStream(targetFile))
                    )
                }.getOrNull()
            }
        }

        // 1. Direct WOFF2 or OFL.txt under /assets/fonts/... or /books/<id>/assets/fonts/...
        val fontsSubPath = when {
            requestPath.startsWith("/assets/fonts/") -> requestPath.removePrefix("/assets/")
            requestPath.startsWith("/fonts/") && requestPath.endsWith(".woff2") -> requestPath.removePrefix("/")
            requestPath.contains("/assets/fonts/") -> "fonts/" + requestPath.substringAfter("/assets/fonts/")
            else -> null
        }

        if (fontsSubPath != null) {
            if (bookDir != null && bookDir.isDirectory) {
                val localBookFont = File(bookDir, "assets/$fontsSubPath")
                if (localBookFont.isFile && localBookFont.canonicalPath.startsWith(bookDir.canonicalPath)) {
                    val mime = if (localBookFont.name.endsWith(".woff2", ignoreCase = true)) "font/woff2" else "text/plain"
                    return runCatching {
                        WebResourceResponse(
                            mime,
                            if (mime == "text/plain") "UTF-8" else null,
                            200,
                            "OK",
                            headers,
                            BufferedInputStream(FileInputStream(localBookFont))
                        )
                    }.getOrNull()
                }
            }
            return runCatching {
                val mime = if (fontsSubPath.endsWith(".woff2", ignoreCase = true)) "font/woff2" else "text/plain"
                val stream = context.assets.open(fontsSubPath)
                WebResourceResponse(
                    mime,
                    if (mime == "text/plain") "UTF-8" else null,
                    200,
                    "OK",
                    headers,
                    stream
                )
            }.getOrNull()
        }

        // 2. TTF resource requests under /fonts/<name>.ttf
        if (requestPath.startsWith("/fonts/") && requestPath.endsWith(".ttf", ignoreCase = true)) {
            val fileName = requestPath.substringAfterLast("/").lowercase()
            val fontResId = when (fileName) {
                "amiri.ttf" -> R.font.amiri
                "amiri_quran.ttf", "amiriquran.ttf" -> R.font.amiri_quran
                "scheherazade_new.ttf", "scheherazadenew.ttf" -> R.font.scheherazade_new
                "lateef.ttf" -> R.font.lateef
                "harmattan.ttf" -> R.font.harmattan
                "cairo.ttf" -> R.font.cairo
                "tajawal.ttf" -> R.font.tajawal
                "almarai.ttf" -> R.font.almarai
                "readex_pro.ttf", "readexpro.ttf" -> R.font.readex_pro
                "markazi_text.ttf", "markazitext.ttf" -> R.font.markazi_text
                "noto_naskh_arabic.ttf", "notonaskharabic.ttf" -> R.font.noto_naskh_arabic
                "aref_ruqaa.ttf", "arefruqaa.ttf" -> R.font.aref_ruqaa
                "marhey.ttf" -> R.font.marhey
                "lemonada.ttf" -> R.font.lemonada
                "mirza.ttf" -> R.font.mirza
                "katibeh.ttf" -> R.font.katibeh
                "vibes.ttf" -> R.font.vibes
                "reem_kufi.ttf", "reemkufi.ttf" -> R.font.reem_kufi
                "el_messiri.ttf", "elmessiri.ttf" -> R.font.el_messiri
                "changa.ttf" -> R.font.changa
                "rakkas.ttf" -> R.font.rakkas
                "lalezar.ttf" -> R.font.lalezar
                "ibm_plex_sans_arabic.ttf", "ibmplexsansarabic.ttf" -> R.font.ibm_plex_sans_arabic
                "noto_sans_arabic.ttf", "notosansarabic.ttf" -> R.font.noto_sans_arabic
                "noto_kufi_arabic.ttf", "notokufiarabic.ttf" -> R.font.noto_kufi_arabic
                else -> null
            }
            if (fontResId != null) {
                return runCatching {
                    WebResourceResponse(
                        "font/ttf",
                        null,
                        200,
                        "OK",
                        headers,
                        context.resources.openRawResource(fontResId)
                    )
                }.getOrNull()
            }
        }

        return null
    }
}
