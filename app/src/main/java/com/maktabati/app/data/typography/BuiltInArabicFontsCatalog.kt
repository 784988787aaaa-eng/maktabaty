package com.maktabati.app.data.typography

import com.maktabati.app.R
import com.maktabati.app.data.typography.model.BookTypographyProfile
import com.maktabati.app.data.typography.model.EditorialTypographyPreset
import com.maktabati.app.data.typography.model.FontCategory
import com.maktabati.app.data.typography.model.FontWeightAsset
import com.maktabati.app.data.typography.model.RegisteredArabicFont

/**
 * Static catalog of the 25 bundled offline Arabic fonts and editorial presets for Maktabati.
 */
object BuiltInArabicFontsCatalog {

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
            familyName = "El Messiri",
            arabicDisplayName = "المسيري الأندلسي الرشيق (El Messiri)",
            cssFamilyName = "El Messiri",
            folderName = "el-messiri",
            weights = emptyList(),
            resFontId = R.font.el_messiri,
            ttfFileName = "el_messiri.ttf",
            category = FontCategory.HEADINGS_DISPLAY,
            editorialUsage = "عناوين الكتب الراقية وفواتح الأبواب والبطاقات",
            fallbackStack = "\"El Messiri\", \"Noto Kufi Arabic\", sans-serif"
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
            fallbackStack = "\"Noto Kufi Arabic\", \"Cairo\", sans-serif"
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
            fallbackStack = "\"Lalezar\", \"Noto Kufi Arabic\", sans-serif"
        )
    )

    val editorialPresets: List<EditorialTypographyPreset> = listOf(
        EditorialTypographyPreset(
            id = "editorial_narrative",
            titleAr = "الأصالة السردية",
            subtitleAr = "نسخ للمتن ورقعة للعناوين وأميري قرآني للآيات",
            profile = BookTypographyProfile(
                body = "Noto Naskh Arabic",
                headings = "Aref Ruqaa",
                chapterTitles = "Noto Kufi Arabic",
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
                chapterTitles = "Noto Kufi Arabic",
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
                chapterTitles = "Noto Kufi Arabic",
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
                chapterTitles = "Noto Kufi Arabic",
                quotes = "Aref Ruqaa",
                quran = "Amiri Quran",
                interfaceFont = "Noto Sans Arabic",
                numbers = "Noto Sans Arabic"
            )
        )
    )
}
