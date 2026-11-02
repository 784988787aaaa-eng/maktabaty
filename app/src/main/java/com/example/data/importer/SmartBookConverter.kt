package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.text.Normalizer
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import java.util.zip.ZipInputStream

/**
 * Categories of visual book publishing templates covering all genres and reader tastes.
 */
enum class BookTemplateCategory(
    val id: String,
    val titleAr: String,
    val emoji: String
) {
    ALL("all", "جميع القوالب", "✦"),
    RELIGIOUS("religious", "دينية وإيمانية", "🕌"),
    BEIGE_CLASSIC("beige_classic", "ورق بيج وكلاسيكي", "📜"),
    GIRLS_AESTHETIC("girls_aesthetic", "ذوق الفتيات", "🌸"),
    STORIES_LITERATURE("stories_literature", "قصصية وأدبية", "📖"),
    SELF_DEVELOPMENT("self_development", "تنمية الذات", "🚀"),
    FAMILY_SOCIAL("family_social", "أسرية واجتماعية", "🏡"),
    EDUCATIONAL_ACADEMIC("educational_academic", "تعليمية وبحوث", "🎓"),
    MAGAZINE_MINIMAL("magazine_minimal", "مجلات وبساطة", "📰")
}

/**
 * Defines the structural and editorial CSS styling of a publishing template beyond colors.
 */
enum class PublishingLayoutStyle {
    HERITAGE_ORNATE,      // إطار تراثي مذهب ومزدوج للمخطوطات والحديث والتفسير والكتب الدينية الوقورة
    SOFT_BOTANICAL,       // بطاقات ناعمة وزوايا انسيابية للكتب النسائية وذوق الفتيات والأسرة
    DAWAH_MOTIVATIONAL,   // إخراج حيوي مشرق ببطاقات مواقف وإشراقات وخطوات عملية (طراز العريفي/عمرو خالد/أبو شادي/تنمية الذات)
    CLASSIC_BOOK_PRESS,   // إخراج المطابع الكلاسيكية والورق البيج والكتب القياسية بترويسة كتاب مطبوع وظل ورقي دافئ
    NARRATIVE_STORY,      // إخراج سردي للقصص والروايات والخواطر مع فواصل مشاهد وإبراز الحوارات والشعر
    ACADEMIC_STRUCTURED,  // إخراج منهجي منظم للكتب التعليمية والبحوث العلمية المحكّمة
    EDITORIAL_MAGAZINE,   // إخراج مجلات ثقافية وصحفية بعناوين بارزة واقتباسات جانبية كبيرة
    PURE_MINIMAL          // تجرد بصري أنيق وهادئ لعشاق البساطة والصفاء
}

/**
 * Represents the 26 luxury interactive HTML5 publishing templates supported by SmartBookConverter.
 * Named clearly by book genre and visual aesthetic rather than internal book titles.
 */
enum class BookVisualTheme(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val badgeEmoji: String,
    val category: BookTemplateCategory,
    val layoutStyle: PublishingLayoutStyle,
    val bgPrimaryHex: String,
    val bgSecondaryHex: String,
    val bgSoftAccentHex: String,
    val bgSoftSecondaryHex: String,
    val bgPearlGoldHex: String,
    val accentPrimaryHex: String,
    val accentPrimaryDarkHex: String,
    val accentPrimaryLightHex: String,
    val accentSecondaryHex: String,
    val accentSecondaryDarkHex: String,
    val accentGoldHex: String,
    val textPrimaryHex: String,
    val textSecondaryHex: String,
    val paperEdgeHex: String,
    val fontBody: String = "Noto Naskh Arabic",
    val fontHeadings: String = "Aref Ruqaa",
    val fontChapterTitles: String = "Reem Kufi",
    val fontQuotes: String = "Amiri",
    val fontQuran: String = "Amiri Quran"
) {
    // =========================================================================
    // 1. الكتب الدينية والإيمانية (Religious & Spiritual)
    // =========================================================================
    ISLAMIC_EMERALD(
        id = "islamic_emerald",
        titleAr = "كتاب ديني (طابع رجالي وقور)",
        subtitleAr = "زمردي أصيل وورق بيج تراثي مع تذهيب كلاسيكي رصين",
        badgeEmoji = "🕌",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.HERITAGE_ORNATE,
        bgPrimaryHex = "#F8F4E8",
        bgSecondaryHex = "#FFFDF7",
        bgSoftAccentHex = "#ECF6F1",
        bgSoftSecondaryHex = "#F3EFE2",
        bgPearlGoldHex = "#FBF3E0",
        accentPrimaryHex = "#065F46",
        accentPrimaryDarkHex = "#044332",
        accentPrimaryLightHex = "#348F73",
        accentSecondaryHex = "#785323",
        accentSecondaryDarkHex = "#4D3414",
        accentGoldHex = "#B47B26",
        textPrimaryHex = "#1E2923",
        textSecondaryHex = "#4B5E54",
        paperEdgeHex = "#D8CEB8",
        fontBody = "Amiri",
        fontHeadings = "Reem Kufi",
        fontChapterTitles = "Noto Kufi Arabic",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    ROSE_HEART(
        id = "rose_heart",
        titleAr = "كتاب ديني (طابع نسائي ناعم)",
        subtitleAr = "وردي لؤلؤي هادئ وذهبي دافئ وأخضر ريحاني مريح للعين",
        badgeEmoji = "🌸",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#FAF6F0",
        bgSecondaryHex = "#FFFDF9",
        bgSoftAccentHex = "#FCF2F5",
        bgSoftSecondaryHex = "#EFF5F0",
        bgPearlGoldHex = "#FDF7EC",
        accentPrimaryHex = "#BD6B80",
        accentPrimaryDarkHex = "#732E41",
        accentPrimaryLightHex = "#D98C9F",
        accentSecondaryHex = "#6B856E",
        accentSecondaryDarkHex = "#445946",
        accentGoldHex = "#C69A4E",
        textPrimaryHex = "#38272C",
        textSecondaryHex = "#6B565C",
        paperEdgeHex = "#DDD1C4",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    FAITH_REVIVAL(
        id = "faith_revival",
        titleAr = "الرقائق والخواطر الإيمانية",
        subtitleAr = "أسلوب كتب خالد أبو شادي • جرعات إيمانية وصناديق تزكية ومحاسبة",
        badgeEmoji = "✨",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.DAWAH_MOTIVATIONAL,
        bgPrimaryHex = "#FCF8F2",
        bgSecondaryHex = "#FFFDF9",
        bgSoftAccentHex = "#FEF3E6",
        bgSoftSecondaryHex = "#F4F1EA",
        bgPearlGoldHex = "#FDF6E2",
        accentPrimaryHex = "#9A3412",
        accentPrimaryDarkHex = "#7C2D12",
        accentPrimaryLightHex = "#EA580C",
        accentSecondaryHex = "#3F6212",
        accentSecondaryDarkHex = "#365314",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#291D18",
        textSecondaryHex = "#665247",
        paperEdgeHex = "#E5D5C3",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "El Messiri",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    DAWAH_INSPIRATION(
        id = "dawah_inspiration",
        titleAr = "الدعوة والقصص الإيماني المشوق",
        subtitleAr = "أسلوب كتب العريفي وعمرو خالد • حيوي ومشرق ببطاقات مواقف وعِبر",
        badgeEmoji = "🌿",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.DAWAH_MOTIVATIONAL,
        bgPrimaryHex = "#F4F9F8",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#E6F4F1",
        bgSoftSecondaryHex = "#FEF9EE",
        bgPearlGoldHex = "#FEF5E1",
        accentPrimaryHex = "#0F766E",
        accentPrimaryDarkHex = "#115E59",
        accentPrimaryLightHex = "#14B8A6",
        accentSecondaryHex = "#B45309",
        accentSecondaryDarkHex = "#78350F",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#132A28",
        textSecondaryHex = "#486562",
        paperEdgeHex = "#C6DDD9",
        fontBody = "Tajawal",
        fontHeadings = "Cairo",
        fontChapterTitles = "Changa",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    HADITH_SUNNAH(
        id = "hadith_sunnah",
        titleAr = "كتب الحديث والسنة والمتون",
        subtitleAr = "إخراج علمي تراثي للأحاديث النبوية مع تمييز المتن والشرح",
        badgeEmoji = "📜",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.HERITAGE_ORNATE,
        bgPrimaryHex = "#F9F5EB",
        bgSecondaryHex = "#FFFDF8",
        bgSoftAccentHex = "#EEF6F0",
        bgSoftSecondaryHex = "#F5EFE0",
        bgPearlGoldHex = "#FCF4DE",
        accentPrimaryHex = "#14532D",
        accentPrimaryDarkHex = "#052E16",
        accentPrimaryLightHex = "#22C55E",
        accentSecondaryHex = "#854D0E",
        accentSecondaryDarkHex = "#713F12",
        accentGoldHex = "#A16207",
        textPrimaryHex = "#1A241E",
        textSecondaryHex = "#4A584F",
        paperEdgeHex = "#D9CEB8",
        fontBody = "Scheherazade New",
        fontHeadings = "Reem Kufi",
        fontChapterTitles = "Noto Kufi Arabic",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    QURAN_TAFSIR(
        id = "quran_tafsir",
        titleAr = "التفسير وتدبر القرآن الكريم",
        subtitleAr = "حُلّة المصاحف وكتب التدبر مع إبراز الآيات بالرسم العثماني",
        badgeEmoji = "🕋",
        category = BookTemplateCategory.RELIGIOUS,
        layoutStyle = PublishingLayoutStyle.HERITAGE_ORNATE,
        bgPrimaryHex = "#FAF6ED",
        bgSecondaryHex = "#FFFDF8",
        bgSoftAccentHex = "#EEF2F6",
        bgSoftSecondaryHex = "#F6F1E3",
        bgPearlGoldHex = "#FCF3DC",
        accentPrimaryHex = "#1E3A5F",
        accentPrimaryDarkHex = "#0F2542",
        accentPrimaryLightHex = "#3B6998",
        accentSecondaryHex = "#065F46",
        accentSecondaryDarkHex = "#044332",
        accentGoldHex = "#B8860B",
        textPrimaryHex = "#1B2430",
        textSecondaryHex = "#4B5666",
        paperEdgeHex = "#D6CBB5",
        fontBody = "Amiri",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri Quran",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 2. الورق البيج والكتب الكلاسيكية (Warm Beige Paper & Classic Books)
    // =========================================================================
    CREAM_BEIGE_PAPER(
        id = "cream_beige_paper",
        titleAr = "كتاب بورق بيج مريح للعين",
        subtitleAr = "ورق بيج طبيعي دافئ كأوراق المطابع الفاخرة للقراءة الطويلة دون إجهاد",
        badgeEmoji = "📖",
        category = BookTemplateCategory.BEIGE_CLASSIC,
        layoutStyle = PublishingLayoutStyle.CLASSIC_BOOK_PRESS,
        bgPrimaryHex = "#F2E9D8",
        bgSecondaryHex = "#FAF3E4",
        bgSoftAccentHex = "#F5EBDD",
        bgSoftSecondaryHex = "#EFE4D2",
        bgPearlGoldHex = "#F8EDD6",
        accentPrimaryHex = "#7C4D2B",
        accentPrimaryDarkHex = "#4E2E16",
        accentPrimaryLightHex = "#A66E45",
        accentSecondaryHex = "#5C6B57",
        accentSecondaryDarkHex = "#3D4839",
        accentGoldHex = "#B47B26",
        textPrimaryHex = "#2B2118",
        textSecondaryHex = "#5E4D3E",
        paperEdgeHex = "#D8C8B0",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Amiri",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    WARM_CHAMOMILE_BEIGE(
        id = "warm_chamomile_beige",
        titleAr = "ورق كريمي دافئ (قراءة هادئة)",
        subtitleAr = "درجة البيج الكريمي المهدئة للبصر مع حبر بني داكن متوازن",
        badgeEmoji = "🕯️",
        category = BookTemplateCategory.BEIGE_CLASSIC,
        layoutStyle = PublishingLayoutStyle.CLASSIC_BOOK_PRESS,
        bgPrimaryHex = "#F4EFE6",
        bgSecondaryHex = "#FCF9F2",
        bgSoftAccentHex = "#F7F1E5",
        bgSoftSecondaryHex = "#EFE8DC",
        bgPearlGoldHex = "#FAF1DF",
        accentPrimaryHex = "#6B4423",
        accentPrimaryDarkHex = "#432810",
        accentPrimaryLightHex = "#9C6B3F",
        accentSecondaryHex = "#6E5A4F",
        accentSecondaryDarkHex = "#4A3B33",
        accentGoldHex = "#C28840",
        textPrimaryHex = "#261F1A",
        textSecondaryHex = "#5C5046",
        paperEdgeHex = "#DCD2C2",
        fontBody = "Markazi Text",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "El Messiri",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    VINTAGE_PARCHMENT(
        id = "vintage_parchment",
        titleAr = "ورق الكتب القديمة والتراث",
        subtitleAr = "طابع المخطوطات والكتب العتيقة بلون الرقّ المعتّق وحبر الوراقين",
        badgeEmoji = "🏺",
        category = BookTemplateCategory.BEIGE_CLASSIC,
        layoutStyle = PublishingLayoutStyle.HERITAGE_ORNATE,
        bgPrimaryHex = "#EBE0C8",
        bgSecondaryHex = "#F6EEDF",
        bgSoftAccentHex = "#EFE4CD",
        bgSoftSecondaryHex = "#E5D8BD",
        bgPearlGoldHex = "#F3E5C8",
        accentPrimaryHex = "#693617",
        accentPrimaryDarkHex = "#421F0B",
        accentPrimaryLightHex = "#945630",
        accentSecondaryHex = "#7A5C32",
        accentSecondaryDarkHex = "#523B1E",
        accentGoldHex = "#9C6B30",
        textPrimaryHex = "#24170E",
        textSecondaryHex = "#594331",
        paperEdgeHex = "#CBB999",
        fontBody = "Amiri",
        fontHeadings = "Rakkas",
        fontChapterTitles = "Aref Ruqaa",
        fontQuotes = "Scheherazade New",
        fontQuran = "Amiri Quran"
    ),
    STANDARD_CLEAN_BOOK(
        id = "standard_clean_book",
        titleAr = "كتاب عادي قياسي",
        subtitleAr = "الإخراج الكلاسيكي المتوازن والمألوف في دور النشر العربية والعالمية",
        badgeEmoji = "📘",
        category = BookTemplateCategory.BEIGE_CLASSIC,
        layoutStyle = PublishingLayoutStyle.CLASSIC_BOOK_PRESS,
        bgPrimaryHex = "#F6F5F2",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#F0EFEA",
        bgSoftSecondaryHex = "#F3F4F6",
        bgPearlGoldHex = "#FAF6EC",
        accentPrimaryHex = "#2D3748",
        accentPrimaryDarkHex = "#1A202C",
        accentPrimaryLightHex = "#4A5568",
        accentSecondaryHex = "#4A5568",
        accentSecondaryDarkHex = "#2D3748",
        accentGoldHex = "#8B6F47",
        textPrimaryHex = "#1A202C",
        textSecondaryHex = "#4A5568",
        paperEdgeHex = "#DCDAD3",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Noto Kufi Arabic",
        fontChapterTitles = "Cairo",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    CLASSIC_DIWAN(
        id = "classic_diwan",
        titleAr = "مجلد أدبي وتراثي فخم",
        subtitleAr = "طابع أمهات الكتب والمجلدات السلطانية بالعنابي والذهب",
        badgeEmoji = "👑",
        category = BookTemplateCategory.BEIGE_CLASSIC,
        layoutStyle = PublishingLayoutStyle.HERITAGE_ORNATE,
        bgPrimaryHex = "#F9F4EF",
        bgSecondaryHex = "#FFFDFB",
        bgSoftAccentHex = "#F8ECEE",
        bgSoftSecondaryHex = "#F4EFEA",
        bgPearlGoldHex = "#FDF5E6",
        accentPrimaryHex = "#7A1C2E",
        accentPrimaryDarkHex = "#4F0E1B",
        accentPrimaryLightHex = "#A8384E",
        accentSecondaryHex = "#6E4F32",
        accentSecondaryDarkHex = "#47311D",
        accentGoldHex = "#B8860B",
        textPrimaryHex = "#261519",
        textSecondaryHex = "#5E4348",
        paperEdgeHex = "#DFD1C5",
        fontBody = "Amiri",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 3. ذوق الفتيات والألوان الزاهية والناعمة (Girls' Aesthetic & Pastel)
    // =========================================================================
    VELVET_TENT(
        id = "velvet_tent",
        titleAr = "كتاب أنثوي (وردي مخملي)",
        subtitleAr = "زهري مخملي وذهبي وعاجي دافئ يناسب ذوق الفتيات والرسائل الوجدانية",
        badgeEmoji = "🌷",
        category = BookTemplateCategory.GIRLS_AESTHETIC,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#FFF1F6",
        bgSecondaryHex = "#FFFDFB",
        bgSoftAccentHex = "#FDE8F0",
        bgSoftSecondaryHex = "#F5EFE6",
        bgPearlGoldHex = "#FDF6E7",
        accentPrimaryHex = "#B94F7A",
        accentPrimaryDarkHex = "#6D2343",
        accentPrimaryLightHex = "#E089AD",
        accentSecondaryHex = "#8C583A",
        accentSecondaryDarkHex = "#5E3720",
        accentGoldHex = "#B88945",
        textPrimaryHex = "#331824",
        textSecondaryHex = "#6E4556",
        paperEdgeHex = "#E6CBD7",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    GIRLS_BLUSH_DIARY(
        id = "girls_blush_diary",
        titleAr = "مذكرات ودفاتر الفتيات",
        subtitleAr = "ألوان الباستيل الوردية والزهور الرقيقة مع خطوط انسيابية أنيقة",
        badgeEmoji = "🎀",
        category = BookTemplateCategory.GIRLS_AESTHETIC,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#FFF5F8",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#FCE7F3",
        bgSoftSecondaryHex = "#FDF2F8",
        bgPearlGoldHex = "#FEF9EB",
        accentPrimaryHex = "#DB2777",
        accentPrimaryDarkHex = "#9D174D",
        accentPrimaryLightHex = "#F472B6",
        accentSecondaryHex = "#9333EA",
        accentSecondaryDarkHex = "#6B21A8",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#3B1329",
        textSecondaryHex = "#75465F",
        paperEdgeHex = "#F3C6DC",
        fontBody = "Tajawal",
        fontHeadings = "Lemonada",
        fontChapterTitles = "El Messiri",
        fontQuotes = "Aref Ruqaa",
        fontQuran = "Amiri Quran"
    ),
    GIRLS_LAVENDER_DREAM(
        id = "girls_lavender_dream",
        titleAr = "إشراقة الخزامى (بنفسجي ناعم)",
        subtitleAr = "بنفسجي لافندر هادئ وورق لؤلؤي ناعم لذوق الفتيات الرفيع",
        badgeEmoji = "🪻",
        category = BookTemplateCategory.GIRLS_AESTHETIC,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#F7F4FF",
        bgSecondaryHex = "#FDFCFF",
        bgSoftAccentHex = "#EDE9FE",
        bgSoftSecondaryHex = "#F5F3FF",
        bgPearlGoldHex = "#FEF8E8",
        accentPrimaryHex = "#7C3AED",
        accentPrimaryDarkHex = "#4C1D95",
        accentPrimaryLightHex = "#A78BFA",
        accentSecondaryHex = "#C026D3",
        accentSecondaryDarkHex = "#86198F",
        accentGoldHex = "#C69249",
        textPrimaryHex = "#25173B",
        textSecondaryHex = "#5A4975",
        paperEdgeHex = "#D8CEEE",
        fontBody = "Readex Pro",
        fontHeadings = "El Messiri",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    GIRLS_PEACH_BLOSSOM(
        id = "girls_peach_blossom",
        titleAr = "زهور الخوخ والماتشا",
        subtitleAr = "تدرجات الخوخي الدافئ مع الأخضر الفستقي الناعم لإخراج مبهج وعصري",
        badgeEmoji = "🍑",
        category = BookTemplateCategory.GIRLS_AESTHETIC,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#FFF6F0",
        bgSecondaryHex = "#FFFDFB",
        bgSoftAccentHex = "#FFEDD5",
        bgSoftSecondaryHex = "#ECFDF5",
        bgPearlGoldHex = "#FEF7E6",
        accentPrimaryHex = "#C2410C",
        accentPrimaryDarkHex = "#7C2D12",
        accentPrimaryLightHex = "#FB923C",
        accentSecondaryHex = "#4D7C0F",
        accentSecondaryDarkHex = "#365314",
        accentGoldHex = "#CA8A04",
        textPrimaryHex = "#331D14",
        textSecondaryHex = "#6B4E40",
        paperEdgeHex = "#EBD1C1",
        fontBody = "Tajawal",
        fontHeadings = "Marhey",
        fontChapterTitles = "El Messiri",
        fontQuotes = "Aref Ruqaa",
        fontQuran = "Amiri Quran"
    ),
    GIRLS_TIFFANY_PEARL(
        id = "girls_tiffany_pearl",
        titleAr = "لؤلؤ وفيروز ناعم (تيفاني أنيق)",
        subtitleAr = "تركواز باستيل هادئ مع ذهبي وردي لإصدارات الفتيات المميزة",
        badgeEmoji = "💎",
        category = BookTemplateCategory.GIRLS_AESTHETIC,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#F0FDFB",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#CCFBF1",
        bgSoftSecondaryHex = "#FDF2F8",
        bgPearlGoldHex = "#FEF9EC",
        accentPrimaryHex = "#0D9488",
        accentPrimaryDarkHex = "#115E59",
        accentPrimaryLightHex = "#2DD4BF",
        accentSecondaryHex = "#BE185D",
        accentSecondaryDarkHex = "#831843",
        accentGoldHex = "#C68B59",
        textPrimaryHex = "#132E2B",
        textSecondaryHex = "#486965",
        paperEdgeHex = "#BCE3DF",
        fontBody = "Readex Pro",
        fontHeadings = "El Messiri",
        fontChapterTitles = "Cairo",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 4. الكتب القصصية والروايات والأدب (Stories, Novels & Literature)
    // =========================================================================
    STORY_NARRATIVE(
        id = "story_narrative",
        titleAr = "كتاب قصصي وسرد مشوق",
        subtitleAr = "تصميم سردي منساب للقصص والحكايات مع فواصل مشاهد وإبراز الحوارات",
        badgeEmoji = "🪶",
        category = BookTemplateCategory.STORIES_LITERATURE,
        layoutStyle = PublishingLayoutStyle.NARRATIVE_STORY,
        bgPrimaryHex = "#FAF6F0",
        bgSecondaryHex = "#FFFDF9",
        bgSoftAccentHex = "#EEF2FF",
        bgSoftSecondaryHex = "#F5F0E6",
        bgPearlGoldHex = "#FDF6E4",
        accentPrimaryHex = "#3730A3",
        accentPrimaryDarkHex = "#1E1B4B",
        accentPrimaryLightHex = "#6366F1",
        accentSecondaryHex = "#78350F",
        accentSecondaryDarkHex = "#451A03",
        accentGoldHex = "#B45309",
        textPrimaryHex = "#1E1B2E",
        textSecondaryHex = "#524E66",
        paperEdgeHex = "#DBD3C5",
        fontBody = "Amiri",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Katibeh",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    LITERARY_NOVEL(
        id = "literary_novel",
        titleAr = "رواية أدبية عالمية",
        subtitleAr = "هوية الروايات المطبوعة بهوامش مريحة وورق كريمي خفيف وعناوين فصول أنيقة",
        badgeEmoji = "🎭",
        category = BookTemplateCategory.STORIES_LITERATURE,
        layoutStyle = PublishingLayoutStyle.NARRATIVE_STORY,
        bgPrimaryHex = "#F7F4EE",
        bgSecondaryHex = "#FDFBF7",
        bgSoftAccentHex = "#F5EEF4",
        bgSoftSecondaryHex = "#EFECE4",
        bgPearlGoldHex = "#FAF2E1",
        accentPrimaryHex = "#581C4F",
        accentPrimaryDarkHex = "#3B0D34",
        accentPrimaryLightHex = "#863A7A",
        accentSecondaryHex = "#4A5D4E",
        accentSecondaryDarkHex = "#2E3B30",
        accentGoldHex = "#A37B45",
        textPrimaryHex = "#221A20",
        textSecondaryHex = "#594B55",
        paperEdgeHex = "#D9D0C4",
        fontBody = "Markazi Text",
        fontHeadings = "El Messiri",
        fontChapterTitles = "Reem Kufi",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    POETRY_THOUGHTS(
        id = "poetry_thoughts",
        titleAr = "خواطر وشعر وأدب وجداني",
        subtitleAr = "مساحات بصرية شاعرية وإطارات مقتبسات تبرز جمال الكلمة والعاطفة",
        badgeEmoji = "🕊️",
        category = BookTemplateCategory.STORIES_LITERATURE,
        layoutStyle = PublishingLayoutStyle.NARRATIVE_STORY,
        bgPrimaryHex = "#FCF8F5",
        bgSecondaryHex = "#FFFDFB",
        bgSoftAccentHex = "#FEF2F2",
        bgSoftSecondaryHex = "#F5EFEA",
        bgPearlGoldHex = "#FDF6E7",
        accentPrimaryHex = "#881337",
        accentPrimaryDarkHex = "#4C0519",
        accentPrimaryLightHex = "#BE123C",
        accentSecondaryHex = "#6B5344",
        accentSecondaryDarkHex = "#433228",
        accentGoldHex = "#B47B26",
        textPrimaryHex = "#2B181E",
        textSecondaryHex = "#63474F",
        paperEdgeHex = "#E2D2CC",
        fontBody = "Amiri",
        fontHeadings = "Aref Ruqaa",
        fontChapterTitles = "Vibes",
        fontQuotes = "Aref Ruqaa",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 5. كتب تنمية الذات والنجاح (Self-Development & Productivity)
    // =========================================================================
    SELF_DEVELOPMENT(
        id = "self_development",
        titleAr = "تنمية الذات وصناعة النجاح",
        subtitleAr = "تصميم عصري محفز يبرز خطوات العمل، القواعد الذهبية، وخلاصات الفصول",
        badgeEmoji = "🎯",
        category = BookTemplateCategory.SELF_DEVELOPMENT,
        layoutStyle = PublishingLayoutStyle.DAWAH_MOTIVATIONAL,
        bgPrimaryHex = "#F8FAFC",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#FEF3C7",
        bgSoftSecondaryHex = "#EFF6FF",
        bgPearlGoldHex = "#FFFBEB",
        accentPrimaryHex = "#B45309",
        accentPrimaryDarkHex = "#0F172A",
        accentPrimaryLightHex = "#F59E0B",
        accentSecondaryHex = "#0369A1",
        accentSecondaryDarkHex = "#0C4A6E",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#0F172A",
        textSecondaryHex = "#475569",
        paperEdgeHex = "#CBD5E1",
        fontBody = "IBM Plex Sans Arabic",
        fontHeadings = "Cairo",
        fontChapterTitles = "Changa",
        fontQuotes = "Noto Naskh Arabic",
        fontQuran = "Amiri Quran"
    ),
    YOUTH_HORIZON(
        id = "youth_horizon",
        titleAr = "كتاب شبابي وطموح معاصر",
        subtitleAr = "كحلي وفيروزي مشرق يناسب كتب الشباب والمهارات وبناء العادات",
        badgeEmoji = "🚀",
        category = BookTemplateCategory.SELF_DEVELOPMENT,
        layoutStyle = PublishingLayoutStyle.DAWAH_MOTIVATIONAL,
        bgPrimaryHex = "#F4F7FB",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#EBF3FC",
        bgSoftSecondaryHex = "#E6F6F8",
        bgPearlGoldHex = "#FEF8E8",
        accentPrimaryHex = "#0284C7",
        accentPrimaryDarkHex = "#1E3A8A",
        accentPrimaryLightHex = "#38BDF8",
        accentSecondaryHex = "#0D9488",
        accentSecondaryDarkHex = "#115E59",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#0F172A",
        textSecondaryHex = "#475569",
        paperEdgeHex = "#CBD5E1",
        fontBody = "Readex Pro",
        fontHeadings = "Cairo",
        fontChapterTitles = "Noto Kufi Arabic",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    LEADERSHIP_PRODUCTIVITY(
        id = "leadership_productivity",
        titleAr = "القيادة والإنتاجية وإدارة الحياة",
        subtitleAr = "إخراج احترافي رصين لكتب الإدارة، التخطيط، وتطوير المهارات",
        badgeEmoji = "⚡",
        category = BookTemplateCategory.SELF_DEVELOPMENT,
        layoutStyle = PublishingLayoutStyle.ACADEMIC_STRUCTURED,
        bgPrimaryHex = "#F4F7F5",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#E8F3EE",
        bgSoftSecondaryHex = "#F1F5F9",
        bgPearlGoldHex = "#FCF6E8",
        accentPrimaryHex = "#1B4332",
        accentPrimaryDarkHex = "#081C15",
        accentPrimaryLightHex = "#2D6A4F",
        accentSecondaryHex = "#334155",
        accentSecondaryDarkHex = "#1E293B",
        accentGoldHex = "#B07D3C",
        textPrimaryHex = "#111D17",
        textSecondaryHex = "#455A50",
        paperEdgeHex = "#C9D6D0",
        fontBody = "Almarai",
        fontHeadings = "Cairo",
        fontChapterTitles = "Noto Kufi Arabic",
        fontQuotes = "Noto Naskh Arabic",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 6. كتب أسرية واجتماعية وتربوية (Family, Social & Parenting)
    // =========================================================================
    FAMILY_PARENTING(
        id = "family_parenting",
        titleAr = "كتاب أسري وتربوي",
        subtitleAr = "ألوان دافئة وحانية تناسب كتب الأسرة، التربية، والعلاقات الزوجية",
        badgeEmoji = "🏡",
        category = BookTemplateCategory.FAMILY_SOCIAL,
        layoutStyle = PublishingLayoutStyle.SOFT_BOTANICAL,
        bgPrimaryHex = "#FDF8F4",
        bgSecondaryHex = "#FFFDFB",
        bgSoftAccentHex = "#FDEEE7",
        bgSoftSecondaryHex = "#F0F5EE",
        bgPearlGoldHex = "#FEF7E8",
        accentPrimaryHex = "#9C4221",
        accentPrimaryDarkHex = "#652B19",
        accentPrimaryLightHex = "#DD6B20",
        accentSecondaryHex = "#4D7C0F",
        accentSecondaryDarkHex = "#365314",
        accentGoldHex = "#C68B39",
        textPrimaryHex = "#2B1E19",
        textSecondaryHex = "#634C43",
        paperEdgeHex = "#E4D3C8",
        fontBody = "Tajawal",
        fontHeadings = "El Messiri",
        fontChapterTitles = "Cairo",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    SOCIAL_CULTURAL(
        id = "social_cultural",
        titleAr = "كتاب اجتماعي وفكري",
        subtitleAr = "إخراج رصين وواضح للدراسات الاجتماعية، الفكر، والوعي المجتمعي",
        badgeEmoji = "🤝",
        category = BookTemplateCategory.FAMILY_SOCIAL,
        layoutStyle = PublishingLayoutStyle.CLASSIC_BOOK_PRESS,
        bgPrimaryHex = "#F6F6F4",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#EFECE6",
        bgSoftSecondaryHex = "#F1F5F9",
        bgPearlGoldHex = "#FAF5E8",
        accentPrimaryHex = "#334155",
        accentPrimaryDarkHex = "#1E293B",
        accentPrimaryLightHex = "#64748B",
        accentSecondaryHex = "#9A3412",
        accentSecondaryDarkHex = "#7C2D12",
        accentGoldHex = "#B45309",
        textPrimaryHex = "#1E293B",
        textSecondaryHex = "#475569",
        paperEdgeHex = "#D6D6D2",
        fontBody = "IBM Plex Sans Arabic",
        fontHeadings = "Noto Kufi Arabic",
        fontChapterTitles = "Cairo",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 7. الكتب التعليمية والبحوث العلمية (Educational & Academic Research)
    // =========================================================================
    EDUCATIONAL_TEXTBOOK(
        id = "educational_textbook",
        titleAr = "كتاب تعليمي ومنهجي",
        subtitleAr = "منظم بصرياً بصناديق التعريفات، القواعد، الأمثلة، والملخصات التعليمية",
        badgeEmoji = "🎓",
        category = BookTemplateCategory.EDUCATIONAL_ACADEMIC,
        layoutStyle = PublishingLayoutStyle.ACADEMIC_STRUCTURED,
        bgPrimaryHex = "#F3F8F8",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#E6F3F3",
        bgSoftSecondaryHex = "#FEF9EE",
        bgPearlGoldHex = "#FEF6E4",
        accentPrimaryHex = "#0F5257",
        accentPrimaryDarkHex = "#093538",
        accentPrimaryLightHex = "#0D9488",
        accentSecondaryHex = "#B45309",
        accentSecondaryDarkHex = "#78350F",
        accentGoldHex = "#D97706",
        textPrimaryHex = "#10282A",
        textSecondaryHex = "#466366",
        paperEdgeHex = "#C5D8D9",
        fontBody = "IBM Plex Sans Arabic",
        fontHeadings = "Noto Kufi Arabic",
        fontChapterTitles = "Cairo",
        fontQuotes = "Noto Naskh Arabic",
        fontQuran = "Amiri Quran"
    ),
    ROYAL_ACADEMIC(
        id = "royal_academic",
        titleAr = "بحث علمي وأكاديمي محكّم",
        subtitleAr = "إخراج منهجي رصين للبحوث العلمية، الرسائل الجامعية، والدراسات المرجعية",
        badgeEmoji = "🏛️",
        category = BookTemplateCategory.EDUCATIONAL_ACADEMIC,
        layoutStyle = PublishingLayoutStyle.ACADEMIC_STRUCTURED,
        bgPrimaryHex = "#F9FAFB",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#EFF6FF",
        bgSoftSecondaryHex = "#F1F5F9",
        bgPearlGoldHex = "#FEF9EB",
        accentPrimaryHex = "#1D4ED8",
        accentPrimaryDarkHex = "#1E3A8A",
        accentPrimaryLightHex = "#60A5FA",
        accentSecondaryHex = "#334155",
        accentSecondaryDarkHex = "#1E293B",
        accentGoldHex = "#B45309",
        textPrimaryHex = "#111827",
        textSecondaryHex = "#4B5563",
        paperEdgeHex = "#D1D5DB",
        fontBody = "Noto Naskh Arabic",
        fontHeadings = "Noto Kufi Arabic",
        fontChapterTitles = "Cairo",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),

    // =========================================================================
    // 8. المجلات والبساطة العصرية (Magazines & Minimalist)
    // =========================================================================
    CULTURAL_MAGAZINE(
        id = "cultural_magazine",
        titleAr = "مجلة ثقافية وإخراج صحفي",
        subtitleAr = "طابع المجلات الثقافية الفاخرة بعناوين بارزة واقتباسات مميزة",
        badgeEmoji = "📰",
        category = BookTemplateCategory.MAGAZINE_MINIMAL,
        layoutStyle = PublishingLayoutStyle.EDITORIAL_MAGAZINE,
        bgPrimaryHex = "#FAF8F6",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#FEF2F2",
        bgSoftSecondaryHex = "#F5F5F4",
        bgPearlGoldHex = "#FEF8EC",
        accentPrimaryHex = "#991B1B",
        accentPrimaryDarkHex = "#450A0A",
        accentPrimaryLightHex = "#DC2626",
        accentSecondaryHex = "#292524",
        accentSecondaryDarkHex = "#1C1917",
        accentGoldHex = "#B45309",
        textPrimaryHex = "#1C1917",
        textSecondaryHex = "#57534E",
        paperEdgeHex = "#DFDBD7",
        fontBody = "Almarai",
        fontHeadings = "Lalezar",
        fontChapterTitles = "El Messiri",
        fontQuotes = "Amiri",
        fontQuran = "Amiri Quran"
    ),
    PURE_MINIMALIST(
        id = "pure_minimalist",
        titleAr = "ذوق المحبين للبساطة والصفاء",
        subtitleAr = "تجرد بصري أنيق، بياض هادئ، وتركيز خالص على جمال الحرف والفقرة",
        badgeEmoji = "🏳️",
        category = BookTemplateCategory.MAGAZINE_MINIMAL,
        layoutStyle = PublishingLayoutStyle.PURE_MINIMAL,
        bgPrimaryHex = "#FAFAFA",
        bgSecondaryHex = "#FFFFFF",
        bgSoftAccentHex = "#F4F4F5",
        bgSoftSecondaryHex = "#F4F4F5",
        bgPearlGoldHex = "#FAFAFA",
        accentPrimaryHex = "#27272A",
        accentPrimaryDarkHex = "#09090B",
        accentPrimaryLightHex = "#71717A",
        accentSecondaryHex = "#52525B",
        accentSecondaryDarkHex = "#27272A",
        accentGoldHex = "#71717A",
        textPrimaryHex = "#18181B",
        textSecondaryHex = "#52525B",
        paperEdgeHex = "#E4E4E7",
        fontBody = "Readex Pro",
        fontHeadings = "Noto Sans Arabic",
        fontChapterTitles = "IBM Plex Sans Arabic",
        fontQuotes = "Noto Naskh Arabic",
        fontQuran = "Amiri Quran"
    );

    val arabicName: String get() = titleAr
    val subtitleDescription: String get() = subtitleAr
    val paperBgHex: String get() = bgSecondaryHex

    companion object {
        fun fromId(id: String?): BookVisualTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ROSE_HEART
        }

        fun byCategory(category: BookTemplateCategory): List<BookVisualTheme> {
            if (category == BookTemplateCategory.ALL) return entries
            return entries.filter { it.category == category }
        }
    }
}

/**
 * Holds the pre-analyzed result of inspecting a text/office document before final HTML generation.
 */
data class ConvertibleBookProposal(
    val uri: Uri?,
    val originalFileName: String,
    val formatBadge: String,
    val detectedTitle: String,
    val detectedSubtitle: String,
    val detectedAuthor: String,
    val rawExtractedText: String,
    val isRtl: Boolean,
    val language: String,
    val wordCount: Int,
    val estimatedPages: Int,
    val recommendedTheme: BookVisualTheme = BookVisualTheme.ROSE_HEART
) {
    val sourceFormatLabel: String get() = formatBadge.substringBefore(" •").trim()
    val estimatedPageCount: Int get() = estimatedPages
}

data class ConvertedPageItem(
    val tag: String,
    val title: String,
    val type: String, // "cover", "opener", "reading"
    val html: String
)

/**
 * Comprehensive Offline Document-to-Luxury-HTML5 Book Converter Engine.
 * Supports: TXT, MD, MARKDOWN, DOC, DOCX, ODT, RTF, EPUB, PDF.
 * Full 100% fidelity for Arabic (all Tashkeel diacritics, Hamzas, Quranic brackets, punctuation) and English.
 */
object SmartBookConverter {

    val SUPPORTED_CONVERTIBLE_EXTENSIONS = setOf(
        "txt", "md", "markdown", "doc", "docx", "odt", "rtf", "epub", "pdf"
    )

    private val ARABIC_CHAR_REGEX = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]")
    private val LATIN_CHAR_REGEX = Regex("[A-Za-z]")

    fun isConvertibleFileName(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return ext in SUPPORTED_CONVERTIBLE_EXTENSIONS
    }

    fun isConvertibleMimeType(mimeType: String?): Boolean {
        if (mimeType.isNullOrBlank()) return false
        val m = mimeType.lowercase()
        return m == "text/plain" ||
            m == "text/markdown" ||
            m == "text/x-markdown" ||
            m == "application/rtf" ||
            m == "text/rtf" ||
            m == "application/msword" ||
            m == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            m == "application/vnd.oasis.opendocument.text" ||
            m == "application/epub+zip" ||
            m == "application/pdf"
    }

    fun detectFormatBadge(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return when (ext) {
            "docx" -> "DOCX • وورد حديث"
            "doc" -> "DOC • وورد كلاسيكي"
            "pdf" -> "PDF • مستند رقمي"
            "epub" -> "EPUB • كتاب إلكتروني"
            "odt" -> "ODT • مستند مفتوح"
            "rtf" -> "RTF • نص منسق"
            "md", "markdown" -> "MD • ماركداون"
            else -> "TXT • ملف نصي"
        }
    }

    /**
     * Inspects a URI and extracts its text, title, author, language, and estimated page count
     * so the user can preview/customize in `ConvertTextBookDialog` before generating the book.
     */
    fun inspectDocumentUri(context: Context, uri: Uri): ConvertibleBookProposal {
        val fileName = queryFileName(context, uri) ?: "كتاب_محول.txt"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف من الذاكرة")
        return inspectDocumentBytes(bytes, fileName, uri)
    }

    fun inspectDocumentFile(file: File): ConvertibleBookProposal {
        val bytes = FileInputStream(file).use { it.readBytes() }
        return inspectDocumentBytes(bytes, file.name, Uri.fromFile(file))
    }

    fun inspectDocumentBytes(
        bytes: ByteArray,
        originalFileName: String,
        uri: Uri? = null
    ): ConvertibleBookProposal {
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على نصوص")
        }

        val extracted = extractStructuredDocument(bytes, originalFileName)
        val normalizedBody = normalizeArabicFidelity(extracted.bodyText)

        if (normalizedBody.isBlank()) {
            throw IllegalArgumentException("لم يتم العثور على نصوص قابلة للقراءة داخل الملف ($originalFileName)")
        }

        val fallbackTitle = originalFileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .ifBlank { "كتاب تفاعلي" }

        val semanticMeta = extractTitleSubtitleAndAuthor(
            rawText = normalizedBody,
            explicitTitle = extracted.explicitTitle,
            explicitAuthor = extracted.explicitAuthor,
            fallbackTitle = fallbackTitle
        )

        val (isRtl, language) = detectLanguageAndDirection(normalizedBody)
        val words = normalizedBody.split(Regex("\\s+")).count { it.isNotBlank() }
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)

        return ConvertibleBookProposal(
            uri = uri,
            originalFileName = originalFileName,
            formatBadge = detectFormatBadge(originalFileName),
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = normalizedBody,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme
        )
    }

    /**
     * Inspects raw text pasted from the Clipboard (or typed/shared directly) and produces
     * a ready-to-customize `ConvertibleBookProposal` with smart genre & template detection.
     */
    fun inspectRawText(
        rawText: String,
        sourceLabel: String = "CLIPBOARD • نصوص الحافظة",
        preferredTitle: String? = null,
        preferredAuthor: String? = null
    ): ConvertibleBookProposal {
        val normalizedBody = normalizeArabicFidelity(rawText)
        if (normalizedBody.isBlank()) {
            throw IllegalArgumentException("النص المدخل فارغ. يرجى لصق أو كتابة نص لتحويله إلى كتاب.")
        }

        val (isRtl, language) = detectLanguageAndDirection(normalizedBody)
        val fallbackTitle = if (isRtl) "إشراقات من الحافظة" else "Clipboard Anthology"

        val semanticMeta = extractTitleSubtitleAndAuthor(
            rawText = normalizedBody,
            explicitTitle = preferredTitle?.takeIf { it.isNotBlank() },
            explicitAuthor = preferredAuthor?.takeIf { it.isNotBlank() },
            fallbackTitle = fallbackTitle
        )

        val words = normalizedBody.split(Regex("\\s+")).count { it.isNotBlank() }
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)

        return ConvertibleBookProposal(
            uri = null,
            originalFileName = if (isRtl) "نص_الحافظة.txt" else "clipboard_text.txt",
            formatBadge = sourceLabel,
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = normalizedBody,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme
        )
    }

    /**
     * Converts raw text or a `ConvertibleBookProposal` directly into a complete, self-contained
     * luxury HTML5 interactive book string matching Maktabati's flagship templates.
     */
    fun convertRawTextToLuxuryHtml(
        rawText: String,
        title: String? = null,
        subtitle: String? = null,
        author: String? = null,
        theme: BookVisualTheme = BookVisualTheme.ROSE_HEART,
        fallbackFileName: String = "كتاب_تفاعلي.txt"
    ): String {
        val cleanText = normalizeArabicFidelity(rawText)
        val fallbackTitle = fallbackFileName.substringBeforeLast('.').replace('_', ' ').trim().ifBlank { "كتاب تفاعلي" }
        val autoMeta = extractTitleSubtitleAndAuthor(
            rawText = cleanText,
            explicitTitle = title,
            explicitAuthor = author,
            fallbackTitle = fallbackTitle
        )

        val finalTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.first
        val finalSubtitle = subtitle?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.second
        val finalAuthor = author?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.third
        val (isRtl, lang) = detectLanguageAndDirection(cleanText)

        val pages = buildSemanticPages(
            rawText = cleanText,
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = isRtl
        )

        return renderCompleteLuxuryHtml(
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = isRtl,
            language = lang,
            pages = pages
        )
    }

    fun convertFileToLuxuryHtml(
        file: File,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme? = null
    ): String {
        val proposal = inspectDocumentFile(file)
        return convertProposalToLuxuryHtml(
            proposal = proposal,
            customTitle = customTitle,
            customSubtitle = customSubtitle,
            customAuthor = customAuthor,
            theme = theme ?: proposal.recommendedTheme
        )
    }

    fun convertProposalToLuxuryHtml(
        proposal: ConvertibleBookProposal,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme = proposal.recommendedTheme
    ): String {
        val finalTitle = customTitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedTitle
        val finalSubtitle = customSubtitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedSubtitle
        val finalAuthor = customAuthor?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedAuthor

        val pages = buildSemanticPages(
            rawText = proposal.rawExtractedText,
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = proposal.isRtl
        )

        return renderCompleteLuxuryHtml(
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = proposal.isRtl,
            language = proposal.language,
            pages = pages
        )
    }

    // =============================================================================================
    // 1. MULTI-FORMAT DOCUMENT EXTRACTION ENGINE (TXT, MD, DOCX, DOC, ODT, RTF, EPUB, PDF)
    // =============================================================================================

    private data class ExtractedDocumentData(
        val bodyText: String,
        val explicitTitle: String? = null,
        val explicitAuthor: String? = null
    )

    private fun extractStructuredDocument(bytes: ByteArray, fileName: String): ExtractedDocumentData {
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()

        // Check ZIP magic bytes (PK\x03\x04) for container formats (DOCX, ODT, EPUB)
        val isZipMagic = bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            bytes[2] == 0x03.toByte() &&
            bytes[3] == 0x04.toByte()

        // Check PDF magic bytes (%PDF-)
        val isPdfMagic = bytes.size >= 5 &&
            bytes[0] == 0x25.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x44.toByte() &&
            bytes[3] == 0x46.toByte()

        // Check RTF magic bytes ({\rtf)
        val isRtfMagic = bytes.size >= 5 &&
            String(bytes, 0, minOf(10, bytes.size), Charsets.US_ASCII).trimStart().startsWith("{\\rtf")

        return when {
            ext == "docx" -> extractFromDocx(bytes)
            ext == "odt" -> extractFromOdt(bytes)
            ext == "epub" -> extractFromEpub(bytes)
            ext == "pdf" || isPdfMagic -> extractFromPdf(bytes)
            ext == "rtf" || isRtfMagic -> extractFromRtf(bytes)
            ext == "doc" -> {
                if (isZipMagic) extractFromDocx(bytes) else extractFromClassicDoc(bytes)
            }
            isZipMagic -> {
                // Auto-detect ZIP-based document (DOCX vs ODT vs EPUB)
                detectAndExtractZipDocument(bytes)
            }
            else -> extractFromPlainTextOrMarkdown(bytes)
        }
    }

    /**
     * 1.1 Plain Text (.txt) & Markdown (.md / .markdown) with Automatic Encoding Detection
     * Supports UTF-8, UTF-16LE, UTF-16BE, and Windows-1256 (Arabic).
     */
    private fun extractFromPlainTextOrMarkdown(bytes: ByteArray): ExtractedDocumentData {
        val text = decodeTextBytesSmart(bytes)
        return ExtractedDocumentData(bodyText = text)
    }

    fun decodeTextBytesSmart(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        // 1. Check UTF-8 BOM (EF BB BF)
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }

        // 2. Check UTF-16LE BOM (FF FE)
        if (bytes.size >= 2 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }

        // 3. Check UTF-16BE BOM (FE FF)
        if (bytes.size >= 2 &&
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        // 4. Heuristic check for UTF-16LE without BOM (frequent null bytes at odd indices or Arabic 0x06 high bytes)
        if (bytes.size >= 8 && bytes.size % 2 == 0) {
            var oddZeroOrArabicCount = 0
            val samplePairs = minOf(bytes.size / 2, 120)
            for (i in 0 until samplePairs) {
                val hi = bytes[i * 2 + 1].toInt() and 0xFF
                if (hi == 0x00 || hi == 0x06) {
                    oddZeroOrArabicCount++
                }
            }
            if (oddZeroOrArabicCount > samplePairs * 0.65) {
                return String(bytes, Charsets.UTF_16LE)
            }
        }

        // 5. Validate UTF-8 vs Windows-1256
        val utf8Text = String(bytes, Charsets.UTF_8)
        val replacementCount = utf8Text.count { it == '\uFFFD' }
        if (replacementCount == 0) {
            return utf8Text
        }

        // Fallback to Windows-1256 for legacy Arabic text files
        val win1256 = runCatching { Charset.forName("windows-1256") }.getOrDefault(Charsets.UTF_8)
        return String(bytes, win1256)
    }

    /**
     * 1.2 Microsoft Word Modern (.docx) Extractor
     * Unzips archive via `ZipInputStream` and parses `word/document.xml` & `docProps/core.xml` via `XmlPullParser`.
     */
    private fun extractFromDocx(bytes: ByteArray): ExtractedDocumentData {
        var documentXmlBytes: ByteArray? = null
        var coreXmlBytes: ByteArray? = null

        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace("\\", "/")
                when {
                    entryName.equals("word/document.xml", ignoreCase = true) -> {
                        documentXmlBytes = zis.readBytes()
                    }
                    entryName.equals("docProps/core.xml", ignoreCase = true) -> {
                        coreXmlBytes = zis.readBytes()
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val docBytes = documentXmlBytes
            ?: throw IllegalArgumentException("ملف وورد (.docx) غير صالح: لم يتم العثور على word/document.xml")

        var explicitTitle: String? = null
        var explicitAuthor: String? = null

        if (coreXmlBytes != null) {
            runCatching {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(ByteArrayInputStream(coreXmlBytes), "UTF-8")
                var event = parser.eventType
                var currentTag = ""
                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> currentTag = parser.name.substringAfterLast(':').lowercase()
                        XmlPullParser.TEXT -> {
                            val text = parser.text?.trim().orEmpty()
                            if (text.isNotEmpty()) {
                                if (currentTag == "title" && explicitTitle.isNullOrBlank()) explicitTitle = text
                                if (currentTag == "creator" && explicitAuthor.isNullOrBlank()) explicitAuthor = text
                            }
                        }
                        XmlPullParser.END_TAG -> currentTag = ""
                    }
                    event = parser.next()
                }
            }
        }

        val bodyBuilder = StringBuilder()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(ByteArrayInputStream(docBytes), "UTF-8")

        var eventType = parser.eventType
        var inParagraph = false
        var paragraphStyle = ""
        val paragraphText = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val localName = parser.name.substringAfterLast(':')
                    when (localName) {
                        "p" -> {
                            inParagraph = true
                            paragraphStyle = ""
                            paragraphText.setLength(0)
                        }
                        "pStyle" -> {
                            if (inParagraph) {
                                val styleVal = parser.getAttributeValue(null, "w:val")
                                    ?: parser.getAttributeValue(null, "val")
                                    ?: ""
                                paragraphStyle = styleVal
                            }
                        }
                        "tab" -> if (inParagraph) paragraphText.append(" ")
                        "br", "cr" -> if (inParagraph) paragraphText.append("\n")
                        "t" -> {
                            val tContent = parser.nextText()
                            if (inParagraph) {
                                paragraphText.append(tContent)
                            } else if (tContent.isNotBlank()) {
                                bodyBuilder.append(tContent).append("\n")
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val localName = parser.name.substringAfterLast(':')
                    if (localName == "p" && inParagraph) {
                        val line = paragraphText.toString().trim()
                        if (line.isNotEmpty()) {
                            val lowerStyle = paragraphStyle.lowercase()
                            when {
                                lowerStyle.contains("heading1") || lowerStyle.contains("title") || lowerStyle == "h1" -> {
                                    bodyBuilder.append("# ").append(line).append("\n\n")
                                }
                                lowerStyle.contains("heading2") || lowerStyle.contains("subtitle") || lowerStyle == "h2" -> {
                                    bodyBuilder.append("## ").append(line).append("\n\n")
                                }
                                lowerStyle.contains("heading") || lowerStyle.startsWith("h3") -> {
                                    bodyBuilder.append("### ").append(line).append("\n\n")
                                }
                                else -> {
                                    bodyBuilder.append(line).append("\n\n")
                                }
                            }
                        }
                        inParagraph = false
                    }
                }
            }
            eventType = parser.next()
        }

        return ExtractedDocumentData(
            bodyText = bodyBuilder.toString().trim(),
            explicitTitle = explicitTitle,
            explicitAuthor = explicitAuthor
        )
    }

    /**
     * 1.3 Classic Microsoft Word (.doc) Smart Binary Extractor
     * Extracts clean UTF-16LE (Unicode Word streams) and UTF-8 / Windows-1256 runs while stripping binary noise.
     */
    private fun extractFromClassicDoc(bytes: ByteArray): ExtractedDocumentData {
        // If it's actually plain text or RTF saved with .doc extension, handle gracefully
        val headAscii = String(bytes, 0, minOf(64, bytes.size), Charsets.US_ASCII)
        if (headAscii.trimStart().startsWith("{\\rtf")) {
            return extractFromRtf(bytes)
        }
        val isOleBinary = bytes.size >= 8 &&
            bytes[0] == 0xD0.toByte() &&
            bytes[1] == 0xCF.toByte() &&
            bytes[2] == 0x11.toByte() &&
            bytes[3] == 0xE0.toByte()

        if (!isOleBinary) {
            return extractFromPlainTextOrMarkdown(bytes)
        }

        val utf16Segments = extractCleanUtf16LeSegmentsFromBinary(bytes)
        val utf8Or1256Segments = extractCleanSingleByteSegmentsFromBinary(bytes)

        val bestText = if (scoreReadableBookText(utf16Segments) >= scoreReadableBookText(utf8Or1256Segments)) {
            utf16Segments
        } else {
            utf8Or1256Segments
        }

        return ExtractedDocumentData(bodyText = bestText.trim())
    }

    private fun extractCleanUtf16LeSegmentsFromBinary(bytes: ByteArray): String {
        val out = StringBuilder()
        val run = StringBuilder()

        fun flushRun() {
            val cleaned = cleanBinaryExtractedRun(run.toString())
            if (cleaned.length >= 4 && isMeaningfulHumanRun(cleaned)) {
                out.append(cleaned).append("\n\n")
            }
            run.setLength(0)
        }

        var i = 0
        while (i + 1 < bytes.size) {
            val lo = bytes[i].toInt() and 0xFF
            val hi = bytes[i + 1].toInt() and 0xFF
            val code = (hi shl 8) or lo
            val ch = code.toChar()

            val isValidChar = when {
                code == 0x000D || code == 0x000A -> true
                code == 0x0009 || code == 0x0020 -> true
                code in 0x0021..0x007E -> true
                code in 0x00A0..0x00FF -> true
                code in 0x0600..0x06FF -> true
                code in 0x0750..0x077F -> true
                code in 0xFB50..0xFDFF -> true
                code in 0xFE70..0xFEFF -> true
                code in 0x2000..0x203A -> true
                else -> false
            }

            if (isValidChar) {
                if (code == 0x000D || code == 0x000A) {
                    run.append('\n')
                } else {
                    run.append(ch)
                }
                i += 2
            } else {
                flushRun()
                i += 2
            }
        }
        flushRun()
        return out.toString()
    }

    private fun extractCleanSingleByteSegmentsFromBinary(bytes: ByteArray): String {
        val decoded = decodeTextBytesSmart(bytes)
        val out = StringBuilder()
        val current = StringBuilder()

        for (ch in decoded) {
            val code = ch.code
            val valid = ch == '\n' || ch == '\r' || ch == '\t' || ch == ' ' ||
                code in 0x0021..0x007E ||
                code in 0x0600..0x06FF ||
                code in 0xFB50..0xFDFF ||
                code in 0xFE70..0xFEFF ||
                ch in setOf('«', '»', '،', '؛', '؟', '﴿', '﴾')
            if (valid) {
                current.append(ch)
            } else {
                val cleaned = cleanBinaryExtractedRun(current.toString())
                if (cleaned.length >= 6 && isMeaningfulHumanRun(cleaned)) {
                    out.append(cleaned).append("\n\n")
                }
                current.setLength(0)
            }
        }
        val tail = cleanBinaryExtractedRun(current.toString())
        if (tail.length >= 6 && isMeaningfulHumanRun(tail)) {
            out.append(tail).append("\n\n")
        }
        return out.toString()
    }

    private fun cleanBinaryExtractedRun(raw: String): String {
        val ignoredNoiseTokens = listOf(
            "Root Entry", "WordDocument", "SummaryInformation", "DocumentSummaryInformation",
            "CompObj", "1Table", "0Table", "Normal.dotm", "Microsoft Office Word", "MSWordDoc",
            "Times New Roman", "Calibri", "Arial", "HYPERLINK", "MERGEFORMAT"
        )
        var s = raw
        for (token in ignoredNoiseTokens) {
            s = s.replace(token, " ", ignoreCase = true)
        }
        return s.lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.length >= 2 && isMeaningfulHumanRun(it) }
            .joinToString("\n")
            .trim()
    }

    private fun isMeaningfulHumanRun(text: String): Boolean {
        if (text.isBlank()) return false
        val arabicCount = text.count { it.code in 0x0600..0x06FF || it.code in 0xFB50..0xFEFF }
        val latinCount = text.count { it in 'A'..'Z' || it in 'a'..'z' }
        val letterCount = arabicCount + latinCount
        if (letterCount < 2) return false
        return letterCount.toFloat() / text.length.toFloat() >= 0.35f
    }

    private fun scoreReadableBookText(text: String): Int {
        val arabic = text.count { it.code in 0x0600..0x06FF }
        val words = text.split(Regex("\\s+")).count { it.length >= 2 }
        return arabic * 3 + words
    }

    /**
     * 1.4 OpenDocument Text (.odt) Extractor
     * Unzips archive and parses `content.xml` and `meta.xml` via `XmlPullParser`.
     */
    private fun extractFromOdt(bytes: ByteArray): ExtractedDocumentData {
        var contentXmlBytes: ByteArray? = null
        var metaXmlBytes: ByteArray? = null

        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name.replace("\\", "/")
                when {
                    name.equals("content.xml", ignoreCase = true) -> contentXmlBytes = zis.readBytes()
                    name.equals("meta.xml", ignoreCase = true) -> metaXmlBytes = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val cBytes = contentXmlBytes
            ?: throw IllegalArgumentException("ملف OpenDocument (.odt) غير صالح: لم يتم العثور على content.xml")

        var explicitTitle: String? = null
        var explicitAuthor: String? = null
        if (metaXmlBytes != null) {
            runCatching {
                val parser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(ByteArrayInputStream(metaXmlBytes), "UTF-8")
                var ev = parser.eventType
                var tag = ""
                while (ev != XmlPullParser.END_DOCUMENT) {
                    when (ev) {
                        XmlPullParser.START_TAG -> tag = parser.name.substringAfterLast(':').lowercase()
                        XmlPullParser.TEXT -> {
                            val t = parser.text?.trim().orEmpty()
                            if (t.isNotEmpty()) {
                                if (tag == "title" && explicitTitle.isNullOrBlank()) explicitTitle = t
                                if ((tag == "creator" || tag == "initial-creator") && explicitAuthor.isNullOrBlank()) explicitAuthor = t
                            }
                        }
                        XmlPullParser.END_TAG -> tag = ""
                    }
                    ev = parser.next()
                }
            }
        }

        val out = StringBuilder()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(ByteArrayInputStream(cBytes), "UTF-8")

        var ev = parser.eventType
        var blockDepth = 0
        var isHeading = false
        val currentBlock = StringBuilder()

        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> {
                    val local = parser.name.substringAfterLast(':').lowercase()
                    if (local == "h" || local == "p") {
                        if (blockDepth == 0) {
                            isHeading = (local == "h")
                            currentBlock.setLength(0)
                        }
                        blockDepth++
                    } else if (local == "line-break" && blockDepth > 0) {
                        currentBlock.append("\n")
                    } else if (local == "s" && blockDepth > 0) {
                        currentBlock.append(" ")
                    } else if (local == "tab" && blockDepth > 0) {
                        currentBlock.append(" ")
                    }
                }
                XmlPullParser.TEXT -> {
                    if (blockDepth > 0) {
                        currentBlock.append(parser.text.orEmpty())
                    }
                }
                XmlPullParser.END_TAG -> {
                    val local = parser.name.substringAfterLast(':').lowercase()
                    if ((local == "h" || local == "p") && blockDepth > 0) {
                        blockDepth--
                        if (blockDepth == 0) {
                            val line = currentBlock.toString().trim()
                            if (line.isNotEmpty()) {
                                if (isHeading) {
                                    out.append("# ").append(line).append("\n\n")
                                } else {
                                    out.append(line).append("\n\n")
                                }
                            }
                        }
                    }
                }
            }
            ev = parser.next()
        }

        return ExtractedDocumentData(
            bodyText = out.toString().trim(),
            explicitTitle = explicitTitle,
            explicitAuthor = explicitAuthor
        )
    }

    /**
     * 1.5 Rich Text Format (.rtf) Parser & Unicode Decoder
     * Decodes `\uN` signed/unsigned 16-bit Unicode escapes (including full Arabic + Tashkeel),
     * `\'xx` Windows-1256 hex escapes, and paragraph breaks (`\par`, `\line`), while skipping non-content groups.
     */
    private fun extractFromRtf(bytes: ByteArray): ExtractedDocumentData {
        val raw = String(bytes, Charsets.ISO_8859_1)
        val win1256 = runCatching { Charset.forName("windows-1256") }.getOrDefault(Charsets.UTF_8)
        val out = StringBuilder()

        val ignoredDestinations = setOf(
            "fonttbl", "colortbl", "stylesheet", "info", "pict", "object",
            "header", "footer", "headerl", "headerr", "footerl", "footerr",
            "footnote", "xmlopen", "themedata", "colorschememapping", "latentstyles",
            "datastore", "rsidtbl", "generator", "listtable", "listoverridetable"
        )

        var i = 0
        val len = raw.length
        var braceDepth = 0
        var skipBelowDepth = -1
        var ucSkipCount = 1
        val ucStack = ArrayDeque<Int>()

        while (i < len) {
            val ch = raw[i]
            when (ch) {
                '{' -> {
                    ucStack.addLast(ucSkipCount)
                    braceDepth++
                    // Check if starts with {\*\destination or {\destination
                    var look = i + 1
                    var starred = false
                    while (look < len && raw[look].isWhitespace()) look++
                    if (look + 1 < len && raw[look] == '\\' && raw[look + 1] == '*') {
                        starred = true
                        look += 2
                        while (look < len && raw[look].isWhitespace()) look++
                    }
                    if (look < len && raw[look] == '\\') {
                        val wordStart = look + 1
                        var wordEnd = wordStart
                        while (wordEnd < len && raw[wordEnd].isLetter()) wordEnd++
                        val word = raw.substring(wordStart, wordEnd).lowercase()
                        if (starred || word in ignoredDestinations) {
                            if (skipBelowDepth == -1) {
                                skipBelowDepth = braceDepth
                            }
                        }
                    }
                    i++
                }
                '}' -> {
                    if (skipBelowDepth == braceDepth) {
                        skipBelowDepth = -1
                    }
                    braceDepth = (braceDepth - 1).coerceAtLeast(0)
                    if (ucStack.isNotEmpty()) {
                        ucSkipCount = ucStack.removeLast()
                    }
                    i++
                }
                '\\' -> {
                    i++
                    if (i >= len) break
                    val next = raw[i]
                    when {
                        next == '\\' || next == '{' || next == '}' -> {
                            if (skipBelowDepth == -1) out.append(next)
                            i++
                        }
                        next == '~' -> {
                            if (skipBelowDepth == -1) out.append(' ')
                            i++
                        }
                        next == '-' || next == '_' -> {
                            i++
                        }
                        next == '\n' || next == '\r' -> {
                            if (skipBelowDepth == -1) out.append("\n")
                            i++
                        }
                        next == '\'' -> {
                            // Hex byte sequence \'xx (possibly consecutive bytes)
                            val byteBuffer = ByteArrayOutputStream()
                            while (i < len && raw[i] == '\'' && i + 2 < len) {
                                val hex = raw.substring(i + 1, i + 3)
                                val bVal = hex.toIntOrNull(16)
                                if (bVal != null) {
                                    byteBuffer.write(bVal)
                                    i += 3
                                    if (i < len && raw[i] == '\\' && i + 1 < len && raw[i + 1] == '\'') {
                                        i++ // advance to next \'
                                    } else {
                                        break
                                    }
                                } else {
                                    break
                                }
                            }
                            if (skipBelowDepth == -1 && byteBuffer.size() > 0) {
                                val rawHexBytes = byteBuffer.toByteArray()
                                val utf8Candidate = String(rawHexBytes, Charsets.UTF_8)
                                if (!utf8Candidate.contains('\uFFFD')) {
                                    out.append(utf8Candidate)
                                } else {
                                    out.append(String(rawHexBytes, win1256))
                                }
                            }
                        }
                        next.isLetter() -> {
                            val wordStart = i
                            while (i < len && raw[i].isLetter()) i++
                            val controlWord = raw.substring(wordStart, i)

                            // Optional signed integer parameter
                            val numStart = i
                            if (i < len && (raw[i] == '-' || raw[i].isDigit())) {
                                i++
                                while (i < len && raw[i].isDigit()) i++
                            }
                            val numParamStr = raw.substring(numStart, i)

                            // Single space delimiter after control word is consumed
                            if (i < len && raw[i] == ' ') {
                                i++
                            }

                            if (controlWord == "uc" && numParamStr.isNotEmpty()) {
                                ucSkipCount = numParamStr.toIntOrNull()?.coerceIn(0, 8) ?: 1
                            } else if (skipBelowDepth == -1) {
                                when (controlWord) {
                                    "par", "pard", "sect", "page", "line" -> {
                                        if (controlWord == "par" || controlWord == "line" || controlWord == "page" || controlWord == "sect") {
                                            out.append("\n\n")
                                        }
                                    }
                                    "tab" -> out.append(" ")
                                    "emdash" -> out.append("—")
                                    "endash" -> out.append("–")
                                    "lquote" -> out.append("‘")
                                    "rquote" -> out.append("’")
                                    "ldblquote" -> out.append("«")
                                    "rdblquote" -> out.append("»")
                                    "bullet" -> out.append("• ")
                                    "u" -> {
                                        val codeVal = numParamStr.toIntOrNull()
                                        if (codeVal != null) {
                                            val unsigned = if (codeVal < 0) codeVal + 65536 else codeVal
                                            out.append(unsigned.toChar())
                                            // Skip fallback characters according to ucSkipCount
                                            var toSkip = ucSkipCount
                                            while (toSkip > 0 && i < len) {
                                                if (raw[i] == '\\' && i + 3 < len && raw[i + 1] == '\'') {
                                                    i += 4
                                                } else if (raw[i] != '{' && raw[i] != '}' && raw[i] != '\\') {
                                                    i++
                                                } else {
                                                    break
                                                }
                                                toSkip--
                                            }
                                        }
                                    }
                                    else -> {
                                        if (controlWord in ignoredDestinations) {
                                            skipBelowDepth = braceDepth
                                        }
                                    }
                                }
                            }
                        }
                        else -> i++
                    }
                }
                '\r', '\n' -> i++
                else -> {
                    if (skipBelowDepth == -1) {
                        out.append(ch)
                    }
                    i++
                }
            }
        }

        return ExtractedDocumentData(bodyText = out.toString().trim())
    }

    /**
     * 1.6 EPUB (.epub) E-Book Archive Extractor
     * Reads OPF metadata and chapter XHTML/HTML files in reading order.
     */
    private fun extractFromEpub(bytes: ByteArray): ExtractedDocumentData {
        val entriesMap = linkedMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val name = entry.name.replace("\\", "/")
                    entriesMap[name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        var explicitTitle: String? = null
        var explicitAuthor: String? = null
        val orderedChapterPaths = mutableListOf<String>()

        // Locate .opf package file
        val opfEntry = entriesMap.entries.firstOrNull { it.key.endsWith(".opf", ignoreCase = true) }
        if (opfEntry != null) {
            val opfDir = opfEntry.key.substringBeforeLast('/', "")
            val opfText = String(opfEntry.value, Charsets.UTF_8)

            explicitTitle = Regex("<dc:title[^>]*>([^<]+)</dc:title>", RegexOption.IGNORE_CASE)
                .find(opfText)?.groupValues?.get(1)?.trim()
            explicitAuthor = Regex("<dc:creator[^>]*>([^<]+)</dc:creator>", RegexOption.IGNORE_CASE)
                .find(opfText)?.groupValues?.get(1)?.trim()

            // Map manifest id -> href
            val manifestMap = mutableMapOf<String, String>()
            val itemRegex = Regex("<item\\b([^>]+)/?>", RegexOption.IGNORE_CASE)
            for (m in itemRegex.findAll(opfText)) {
                val attrs = m.groupValues[1]
                val id = Regex("\\bid\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
                    .find(attrs)?.groupValues?.get(1)
                val href = Regex("\\bhref\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
                    .find(attrs)?.groupValues?.get(1)
                if (!id.isNullOrBlank() && !href.isNullOrBlank()) {
                    val cleanHref = href.substringBefore('#')
                    val fullPath = if (opfDir.isNotEmpty() && !cleanHref.startsWith(opfDir)) {
                        "$opfDir/$cleanHref"
                    } else {
                        cleanHref
                    }
                    manifestMap[id] = fullPath
                }
            }

            // Read spine order
            val itemrefRegex = Regex("<itemref\\b[^>]*\\bidref\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
            for (m in itemrefRegex.findAll(opfText)) {
                val idref = m.groupValues[1]
                val path = manifestMap[idref]
                if (path != null) {
                    orderedChapterPaths.add(path)
                }
            }
        }

        // Fallback if spine was empty: take all .xhtml / .html / .htm files in sorted order
        if (orderedChapterPaths.isEmpty()) {
            entriesMap.keys
                .filter {
                    val lower = it.lowercase()
                    (lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm")) &&
                        !lower.contains("nav") && !lower.contains("toc")
                }
                .sorted()
                .forEach { orderedChapterPaths.add(it) }
        }

        val bodyBuilder = StringBuilder()
        for (chapterPath in orderedChapterPaths) {
            val chapterBytes = entriesMap[chapterPath]
                ?: entriesMap.entries.firstOrNull { it.key.equals(chapterPath, ignoreCase = true) }?.value
                ?: continue
            val chapterHtml = decodeTextBytesSmart(chapterBytes)
            val chapterText = extractCleanTextFromHtmlChapter(chapterHtml)
            if (chapterText.isNotBlank()) {
                bodyBuilder.append(chapterText).append("\n\n")
            }
        }

        return ExtractedDocumentData(
            bodyText = bodyBuilder.toString().trim(),
            explicitTitle = explicitTitle,
            explicitAuthor = explicitAuthor
        )
    }

    private fun extractCleanTextFromHtmlChapter(html: String): String {
        val noScripts = html
            .replace(Regex("(?is)<(script|style|head|nav)[^>]*>.*?</\\1>"), " ")
        val withHeadings = noScripts
            .replace(Regex("(?is)<h1[^>]*>(.*?)</h1>")) { "\n\n# ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(Regex("(?is)<h2[^>]*>(.*?)</h2>")) { "\n\n## ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(Regex("(?is)<h[3-6][^>]*>(.*?)</h[3-6]>")) { "\n\n### ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(Regex("(?is)</(p|div|article|section|blockquote|li|tr)>"), "\n\n")
            .replace(Regex("(?is)<br\\s*/?>"), "\n")
        return decodeHtmlEntities(stripHtmlTags(withHeadings))
            .lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
    }

    private fun stripHtmlTags(input: String): String {
        return input.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&laquo;", "«")
            .replace("&raquo;", "»")
            .replace(Regex("&#(\\d+);")) { m ->
                val code = m.groupValues[1].toIntOrNull()
                if (code != null) code.toChar().toString() else m.value
            }
            .replace(Regex("&#x([0-9a-fA-F]+);")) { m ->
                val code = m.groupValues[1].toIntOrNull(16)
                if (code != null) code.toChar().toString() else m.value
            }
    }

    private fun detectAndExtractZipDocument(bytes: ByteArray): ExtractedDocumentData {
        val entryNames = mutableSetOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entryNames.add(entry.name.replace("\\", "/").lowercase())
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return when {
            "word/document.xml" in entryNames -> extractFromDocx(bytes)
            "content.xml" in entryNames -> extractFromOdt(bytes)
            entryNames.any { it.endsWith(".opf") || it.endsWith(".xhtml") } -> extractFromEpub(bytes)
            else -> extractFromPlainTextOrMarkdown(bytes)
        }
    }

    /**
     * 1.7 PDF (.pdf) Text Stream & ToUnicode CMap Extractor
     * Extracts Arabic and English text from uncompressed and FlateDecode-compressed PDF streams,
     * and detects Scanned Image PDFs to display a polite, clear user message.
     */
    private fun extractFromPdf(bytes: ByteArray): ExtractedDocumentData {
        val rawLatin1 = String(bytes, Charsets.ISO_8859_1)

        // Extract optional /Title and /Author from PDF Info dictionary
        val explicitTitle = extractPdfMetadataField(rawLatin1, "Title")
        val explicitAuthor = extractPdfMetadataField(rawLatin1, "Author")

        // Collect all decoded streams + raw content
        val decodedStreams = mutableListOf<String>()
        val streamMarker = "stream"
        val endStreamMarker = "endstream"

        var pos = 0
        while (pos < rawLatin1.length) {
            val sIdx = rawLatin1.indexOf(streamMarker, pos)
            if (sIdx == -1) break
            // Verify it's not "endstream"
            if (sIdx >= 3 && rawLatin1.substring(sIdx - 3, sIdx) == "end") {
                pos = sIdx + 6
                continue
            }
            var dataStart = sIdx + streamMarker.length
            if (dataStart < rawLatin1.length && rawLatin1[dataStart] == '\r') dataStart++
            if (dataStart < rawLatin1.length && rawLatin1[dataStart] == '\n') dataStart++

            val eIdx = rawLatin1.indexOf(endStreamMarker, dataStart)
            if (eIdx == -1) break

            var dataEnd = eIdx
            while (dataEnd > dataStart && (rawLatin1[dataEnd - 1] == '\r' || rawLatin1[dataEnd - 1] == '\n')) {
                dataEnd--
            }

            if (dataEnd > dataStart) {
                val slice = bytes.copyOfRange(dataStart, dataEnd)
                val headerStart = maxOf(0, sIdx - 260)
                val dictHeader = rawLatin1.substring(headerStart, sIdx)
                val isFlate = dictHeader.contains("FlateDecode", ignoreCase = true)

                val decompressedBytes = if (isFlate) {
                    tryInflatePdfStream(slice) ?: slice
                } else {
                    tryInflatePdfStream(slice) ?: slice
                }
                decodedStreams.add(String(decompressedBytes, Charsets.ISO_8859_1))
            }
            pos = eIdx + endStreamMarker.length
        }

        if (decodedStreams.isEmpty()) {
            decodedStreams.add(rawLatin1)
        }

        // Parse any ToUnicode CMap tables first (maps glyph hex codes -> Unicode strings)
        val cmapTable = mutableMapOf<Int, String>()
        for (streamText in decodedStreams) {
            if (streamText.contains("beginbfchar") || streamText.contains("beginbfrange")) {
                parsePdfToUnicodeCMap(streamText, cmapTable)
            }
        }

        // Extract text from BT ... ET text blocks across all content streams
        val extractedLines = StringBuilder()
        for (streamText in decodedStreams) {
            if (streamText.contains("beginbfchar") || streamText.contains("beginbfrange")) continue
            extractPdfTextBlocksFromStream(streamText, cmapTable, extractedLines)
        }

        val finalBody = extractedLines.toString()
            .lines()
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
            .trim()

        val wordCount = finalBody.split(Regex("\\s+")).count { it.length >= 2 }
        if (wordCount < 5) {
            val hasImages = rawLatin1.contains("/Subtype /Image") ||
                rawLatin1.contains("/Subtype/Image") ||
                rawLatin1.contains("/XObject") ||
                rawLatin1.contains("/Page")
            if (hasImages) {
                throw IllegalArgumentException(
                    "ملف الـ PDF المختار مصوّر كصور ممسوحة ضوئياً (Scanned Image PDF) ولا يحتوي على طبقة نصية قابلة للاستخراج المباشر. يرجى اختيار ملف PDF نصي أو ملف وورد (.docx) أو نصي (.txt)."
                )
            }
        }

        return ExtractedDocumentData(
            bodyText = finalBody,
            explicitTitle = explicitTitle,
            explicitAuthor = explicitAuthor
        )
    }

    private fun tryInflatePdfStream(compressed: ByteArray): ByteArray? {
        for (nowrap in listOf(false, true)) {
            try {
                val inflater = Inflater(nowrap)
                val iis = InflaterInputStream(ByteArrayInputStream(compressed), inflater)
                val out = ByteArrayOutputStream()
                val buf = ByteArray(4096)
                var n: Int
                while (iis.read(buf).also { n = it } != -1) {
                    out.write(buf, 0, n)
                    if (out.size() > 16 * 1024 * 1024) break
                }
                if (out.size() > 0) return out.toByteArray()
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parsePdfToUnicodeCMap(streamText: String, cmap: MutableMap<Int, String>) {
        // 1. beginbfchar ... endbfchar: <srcHex> <dstUnicodeHex>
        val bfCharRegex = Regex("beginbfchar(.*?)endbfchar", RegexOption.DOT_MATCHES_ALL)
        val pairRegex = Regex("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>")
        for (block in bfCharRegex.findAll(streamText)) {
            for (pair in pairRegex.findAll(block.groupValues[1])) {
                val src = pair.groupValues[1].toIntOrNull(16) ?: continue
                val dst = decodeUtf16BeHex(pair.groupValues[2])
                if (dst.isNotEmpty()) {
                    cmap[src] = dst
                }
            }
        }

        // 2. beginbfrange ... endbfrange: <startHex> <endHex> <dstStartHex>
        val bfRangeRegex = Regex("beginbfrange(.*?)endbfrange", RegexOption.DOT_MATCHES_ALL)
        val rangeRegex = Regex("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>")
        for (block in bfRangeRegex.findAll(streamText)) {
            for (rng in rangeRegex.findAll(block.groupValues[1])) {
                val start = rng.groupValues[1].toIntOrNull(16) ?: continue
                val end = rng.groupValues[2].toIntOrNull(16) ?: continue
                val dstStart = rng.groupValues[3].toIntOrNull(16) ?: continue
                if (end >= start && (end - start) <= 512) {
                    for (offset in 0..(end - start)) {
                        cmap[start + offset] = (dstStart + offset).toChar().toString()
                    }
                }
            }
        }
    }

    private fun extractPdfTextBlocksFromStream(
        streamText: String,
        cmap: Map<Int, String>,
        out: StringBuilder
    ) {
        val btEtRegex = Regex("\\bBT\\b(.*?)\\bET\\b", RegexOption.DOT_MATCHES_ALL)
        val tokenRegex = Regex("\\((?:\\\\.|[^\\\\()])*\\)|<[0-9A-Fa-f\\s]+>|\\b(Td|TD|T\\*|'|\"|Tj|TJ)\\b")

        val blocks = btEtRegex.findAll(streamText).toList()
        val targetTexts = if (blocks.isNotEmpty()) blocks.map { it.groupValues[1] } else listOf(streamText)

        for (block in targetTexts) {
            val lineBuf = StringBuilder()
            for (match in tokenRegex.findAll(block)) {
                val tok = match.value
                when {
                    tok.startsWith("(") && tok.endsWith(")") -> {
                        val inner = tok.substring(1, tok.length - 1)
                        lineBuf.append(decodePdfLiteralString(inner, cmap))
                    }
                    tok.startsWith("<") && tok.endsWith(">") -> {
                        val hex = tok.substring(1, tok.length - 1).replace(Regex("\\s+"), "")
                        lineBuf.append(decodePdfHexString(hex, cmap))
                    }
                    tok == "Td" || tok == "TD" || tok == "T*" || tok == "'" || tok == "\"" -> {
                        if (lineBuf.isNotEmpty()) {
                            lineBuf.append("\n")
                        }
                    }
                }
            }
            val blockStr = lineBuf.toString().trim()
            if (blockStr.isNotEmpty()) {
                out.append(blockStr).append("\n\n")
            }
        }
    }

    private fun decodePdfLiteralString(literal: String, cmap: Map<Int, String>): String {
        val bytesOut = ByteArrayOutputStream()
        var i = 0
        while (i < literal.length) {
            val c = literal[i]
            if (c == '\\' && i + 1 < literal.length) {
                val next = literal[i + 1]
                when {
                    next == 'n' -> {
                        bytesOut.write('\n'.code)
                        i += 2
                    }
                    next == 'r' -> {
                        bytesOut.write('\r'.code)
                        i += 2
                    }
                    next == 't' -> {
                        bytesOut.write('\t'.code)
                        i += 2
                    }
                    next == '(' || next == ')' || next == '\\' -> {
                        bytesOut.write(next.code)
                        i += 2
                    }
                    next in '0'..'7' -> {
                        var oct = next.toString()
                        i += 2
                        var count = 1
                        while (count < 3 && i < literal.length && literal[i] in '0'..'7') {
                            oct += literal[i]
                            i++
                            count++
                        }
                        bytesOut.write(oct.toInt(8) and 0xFF)
                    }
                    else -> {
                        bytesOut.write(next.code)
                        i += 2
                    }
                }
            } else {
                bytesOut.write(c.code and 0xFF)
                i++
            }
        }

        val rawBytes = bytesOut.toByteArray()
        if (rawBytes.size >= 2 && rawBytes[0] == 0xFE.toByte() && rawBytes[1] == 0xFF.toByte()) {
            return String(rawBytes, 2, rawBytes.size - 2, Charsets.UTF_16BE)
        }
        val utf8 = String(rawBytes, Charsets.UTF_8)
        if (!utf8.contains('\uFFFD')) {
            return utf8
        }
        if (cmap.isNotEmpty()) {
            val mapped = StringBuilder()
            for (b in rawBytes) {
                val code = b.toInt() and 0xFF
                mapped.append(cmap[code] ?: code.toChar().toString())
            }
            return mapped.toString()
        }
        val win1256 = runCatching { Charset.forName("windows-1256") }.getOrDefault(Charsets.UTF_8)
        return String(rawBytes, win1256)
    }

    private fun decodePdfHexString(hex: String, cmap: Map<Int, String>): String {
        if (hex.isEmpty()) return ""
        if (hex.startsWith("FEFF", ignoreCase = true)) {
            return decodeUtf16BeHex(hex.substring(4))
        }
        if (cmap.isNotEmpty() && hex.length % 4 == 0) {
            val sb = StringBuilder()
            var allMapped = true
            var i = 0
            while (i + 4 <= hex.length) {
                val code = hex.substring(i, i + 4).toIntOrNull(16)
                val mapped = if (code != null) cmap[code] else null
                if (mapped != null) {
                    sb.append(mapped)
                } else if (code != null && (code in 0x0020..0x007E || code in 0x0600..0x06FF || code in 0xFB50..0xFEFF)) {
                    sb.append(code.toChar())
                } else {
                    allMapped = false
                    break
                }
                i += 4
            }
            if (allMapped && sb.isNotEmpty()) return sb.toString()
        }
        return decodeUtf16BeHex(hex)
    }

    private fun decodeUtf16BeHex(hex: String): String {
        val clean = if (hex.length % 2 == 0) hex else hex + "0"
        val bytes = ByteArray(clean.length / 2)
        for (i in bytes.indices) {
            bytes[i] = (clean.substring(i * 2, i * 2 + 2).toIntOrNull(16) ?: 0).toByte()
        }
        return if (bytes.size % 2 == 0) {
            val utf16 = String(bytes, Charsets.UTF_16BE)
            val validCount = utf16.count {
                it.code in 0x0020..0x007E || it.code in 0x0600..0x06FF || it.code in 0xFB50..0xFEFF
            }
            if (validCount > 0) utf16 else decodeTextBytesSmart(bytes)
        } else {
            decodeTextBytesSmart(bytes)
        }
    }

    private fun extractPdfMetadataField(rawPdf: String, fieldName: String): String? {
        val literalMatch = Regex("/$fieldName\\s*\\(((?:\\\\.|[^\\\\()])+)\\)").find(rawPdf)
        if (literalMatch != null) {
            val decoded = decodePdfLiteralString(literalMatch.groupValues[1], emptyMap()).trim()
            if (decoded.length >= 2) return decoded
        }
        val hexMatch = Regex("/$fieldName\\s*<([0-9A-Fa-f\\s]+)>").find(rawPdf)
        if (hexMatch != null) {
            val decoded = decodePdfHexString(hexMatch.groupValues[1].replace(Regex("\\s+"), ""), emptyMap()).trim()
            if (decoded.length >= 2) return decoded
        }
        return null
    }

    // =============================================================================================
    // 2. ARABIC PRESENTATION FORMS NORMALIZATION & 100% TASHKEEL/HAMZA FIDELITY
    // =============================================================================================

    /**
     * Normalizes isolated Arabic Presentation Forms-A/B (`\uFB50-\uFDFF`, `\uFE70-\uFEFF`) into
     * standard connected Arabic characters while 100% preserving:
     * - All Tashkeel diacritics (`\u064B-\u065F`, `\u0670`)
     * - All Hamzas (`أ، إ، آ، ؤ، ئ، ء`)
     * - Quranic ornate brackets (`﴿` `\uFD3F` and `﴾` `\uFD3E`)
     * - Islamic honorific ligatures (`ﷺ` `\uFDFA` and `ﷻ` `\uFDFB`)
     * - All Arabic and English punctuation (`،`, `؛`, `؟`, `«`, `»`)
     */
    fun normalizeAndCleanExtractedText(input: String): String = normalizeArabicFidelity(input)

    fun normalizeArabicFidelity(input: String): String {
        if (input.isEmpty()) return ""

        val sb = StringBuilder(input.length)
        for (ch in input) {
            val code = ch.code
            when {
                // Keep Quranic ornate brackets and honorifics untouched
                code == 0xFD3E || code == 0xFD3F || code == 0xFDFA || code == 0xFDFB -> {
                    sb.append(ch)
                }
                // Normalize Arabic Presentation Forms-A and B to standard connected Arabic letters
                code in 0xFB50..0xFDFF || code in 0xFE70..0xFEFF -> {
                    val normalizedChar = Normalizer.normalize(ch.toString(), Normalizer.Form.NFKC)
                    sb.append(normalizedChar)
                }
                else -> {
                    sb.append(ch)
                }
            }
        }

        return sb.toString()
            .replace("\r\n", "\n")
            .replace('\r', '\n')
            .replace("\u0000", "")
            .trim()
    }

    fun detectLanguageAndDirection(text: String): Pair<Boolean, String> {
        val arabicMatches = ARABIC_CHAR_REGEX.findAll(text).count()
        val latinMatches = LATIN_CHAR_REGEX.findAll(text).count()
        return if (arabicMatches >= latinMatches || arabicMatches > 12) {
            Pair(true, "ar")
        } else {
            Pair(false, "en")
        }
    }

    fun suggestThemeForContent(title: String, body: String, isRtl: Boolean): BookVisualTheme {
        if (!isRtl) return BookVisualTheme.ROYAL_ACADEMIC
        val sample = stripArabicDiacriticsForMatching(title + " " + body.take(2000)).lowercase()
        return when {
            sample.contains("حديث") || sample.contains("حدثنا") || sample.contains("روى البخاري") || sample.contains("روى مسلم") || sample.contains("متن") ->
                BookVisualTheme.HADITH_SUNNAH
            sample.contains("تفسير") || sample.contains("تدبر") || sample.contains("سورة ") || sample.contains("آية") ->
                BookVisualTheme.QURAN_TAFSIR
            sample.contains("فتاة") || sample.contains("ابنتي") || sample.contains("مذكرات") || sample.contains("يومياتي") ->
                BookVisualTheme.GIRLS_BLUSH_DIARY
            sample.contains("خيمة") || sample.contains("أنثى") || sample.contains("ملكة") ->
                BookVisualTheme.VELVET_TENT
            sample.contains("محاسبة") || sample.contains("جرعة") || sample.contains("توبة") || sample.contains("أبو شادي") ->
                BookVisualTheme.FAITH_REVIVAL
            sample.contains("موقف") || sample.contains("استمتع") || sample.contains("العريفي") || sample.contains("عمرو خالد") ->
                BookVisualTheme.DAWAH_INSPIRATION
            sample.contains("قرآن") || sample.contains("فقه") || sample.contains("إيمان") || sample.contains("عقيدة") || sample.contains("صلاة") ->
                BookVisualTheme.ISLAMIC_EMERALD
            sample.contains("رواية") || sample.contains("كان يا ما كان") || sample.contains("قصة") || sample.contains("حكاية") ->
                BookVisualTheme.STORY_NARRATIVE
            sample.contains("قصيدة") || sample.contains("شعر") || sample.contains("خواطر") || sample.contains("ديوان") ->
                BookVisualTheme.POETRY_THOUGHTS
            sample.contains("أسرة") || sample.contains("تربية") || sample.contains("أبناء") || sample.contains("زواج") || sample.contains("بيت") ->
                BookVisualTheme.FAMILY_PARENTING
            sample.contains("بحث") || sample.contains("دراسة") || sample.contains("أكاديمي") || sample.contains("المراجع") || sample.contains("منهجية") ->
                BookVisualTheme.ROYAL_ACADEMIC
            sample.contains("درس") || sample.contains("تعريف") || sample.contains("منهج") || sample.contains("تعليم") ->
                BookVisualTheme.EDUCATIONAL_TEXTBOOK
            sample.contains("نجاح") || sample.contains("تنمية") || sample.contains("عادات") || sample.contains("أهداف") || sample.contains("إنتاجية") ->
                BookVisualTheme.SELF_DEVELOPMENT
            sample.contains("شباب") || sample.contains("مستقبل") || sample.contains("علوم") ->
                BookVisualTheme.YOUTH_HORIZON
            sample.contains("قلب") || sample.contains("رسالة") || sample.contains("سكينة") || sample.contains("مودة") ->
                BookVisualTheme.ROSE_HEART
            else -> BookVisualTheme.CREAM_BEIGE_PAPER
        }
    }

    // =============================================================================================
    // 3. SMART SEMANTIC FORMATTER & BALANCED PAGINATION ENGINE
    // =============================================================================================

    private val CHAPTER_HEADING_PREFIXES = listOf(
        "الباب ", "الفصل ", "الرسالة ", "المبحث ", "الجزء ", "القسم ",
        "مقدمة", "المقدمة", "تمهيد", "التمهيد", "الإهداء", "إهداء",
        "الخاتمة", "خاتمة", "ملحق", "الملحق",
        "chapter ", "part ", "section ", "introduction", "preface", "prologue", "epilogue", "conclusion"
    )

    private val AUTHOR_LINE_REGEX = Regex(
        "^(?:تأليف|بقلم|المؤلف|إعداد|الكاتب|كتبه|جمع وترتيب|author|by|written by)\\s*[:：\\-]?\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    private val TITLE_LINE_REGEX = Regex(
        "^(?:عنوان الكتاب|الكتاب|title)\\s*[:：\\-]\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    private val SUBTITLE_LINE_REGEX = Regex(
        "^(?:العنوان الفرعي|subtitle)\\s*[:：\\-]\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    private fun extractTitleSubtitleAndAuthor(
        rawText: String,
        explicitTitle: String?,
        explicitAuthor: String?,
        fallbackTitle: String
    ): Triple<String, String, String> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var detectedTitle = explicitTitle?.trim()?.takeIf { it.isNotEmpty() }
        var detectedSubtitle = ""
        var detectedAuthor = explicitAuthor?.trim()?.takeIf { it.isNotEmpty() }

        for ((idx, line) in lines.take(18).withIndex()) {
            val cleanLine = line.removePrefix("#").removePrefix("#").removePrefix("#").trim()

            val titleMatch = TITLE_LINE_REGEX.find(cleanLine)
            if (titleMatch != null && detectedTitle.isNullOrBlank()) {
                detectedTitle = titleMatch.groupValues[1].trim()
                continue
            }

            val subMatch = SUBTITLE_LINE_REGEX.find(cleanLine)
            if (subMatch != null && detectedSubtitle.isBlank()) {
                detectedSubtitle = subMatch.groupValues[1].trim()
                continue
            }

            val authorMatch = AUTHOR_LINE_REGEX.find(cleanLine)
            if (authorMatch != null && detectedAuthor.isNullOrBlank()) {
                detectedAuthor = authorMatch.groupValues[1].trim()
                continue
            }

            if (idx == 0 && detectedTitle.isNullOrBlank() && cleanLine.length in 2..95) {
                detectedTitle = cleanLine
            } else if (idx == 1 && detectedSubtitle.isBlank() && cleanLine.length in 4..110 &&
                !isChapterHeadingLine(cleanLine) && AUTHOR_LINE_REGEX.find(cleanLine) == null
            ) {
                detectedSubtitle = cleanLine
            }
        }

        val finalTitle = detectedTitle?.takeIf { it.isNotBlank() } ?: fallbackTitle
        val finalSubtitle = detectedSubtitle.ifBlank {
            if (detectLanguageAndDirection(rawText).first) {
                "طبعة رقمية تفاعلية منسقة • منصة مكتبتي"
            } else {
                "Interactive Digital Edition • Maktabati"
            }
        }
        val finalAuthor = detectedAuthor?.takeIf { it.isNotBlank() } ?: (
            if (detectLanguageAndDirection(rawText).first) "إصدارات مكتبتي الفاخرة" else "Maktabati Publishing"
        )

        return Triple(finalTitle, finalSubtitle, finalAuthor)
    }

    private fun stripArabicDiacriticsForMatching(s: String): String {
        return s.replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06EDـ]"), "").trim()
    }

    private fun isChapterHeadingLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.length > 120) return false
        if (trimmed.startsWith("# ") || trimmed.startsWith("## ")) return true

        val unvocalized = stripArabicDiacriticsForMatching(
            trimmed.removePrefix("#").removePrefix("*").removePrefix("✦").removePrefix("❖").trim()
        ).lowercase()

        return CHAPTER_HEADING_PREFIXES.any { prefix ->
            unvocalized.startsWith(prefix) || unvocalized == prefix.trim()
        }
    }

    private fun isSubheadingLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.startsWith("### ")) return true
        if (trimmed.length in 3..70 &&
            (trimmed.endsWith(":") || trimmed.endsWith("：")) &&
            !trimmed.contains("قال تعالى") &&
            !trimmed.contains("قال رسول")
        ) {
            return true
        }
        return false
    }

    private data class SemanticSection(
        val chapterTag: String,
        val chapterTitle: String,
        val paragraphs: MutableList<String> = mutableListOf()
    )

    private fun buildSemanticPages(
        rawText: String,
        bookTitle: String,
        bookSubtitle: String,
        bookAuthor: String,
        theme: BookVisualTheme,
        isRtl: Boolean
    ): List<ConvertedPageItem> {
        val pages = mutableListOf<ConvertedPageItem>()

        // 1. Page 0: Luxury SVG Cover Page
        pages.add(
            ConvertedPageItem(
                tag = if (isRtl) "الغلاف الملكي" else "Royal Cover",
                title = bookTitle,
                type = "cover",
                html = buildCoverPageHtml(bookTitle, bookSubtitle, bookAuthor, theme, isRtl)
            )
        )

        // Split raw text into paragraphs/blocks
        val rawBlocks = rawText
            .split(Regex("\\n\\s*\\n+"))
            .flatMap { block ->
                // If a block has explicit heading lines inside it, split around them
                val subLines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (subLines.size > 1 && subLines.any { isChapterHeadingLine(it) || it.startsWith("#") }) {
                    val splitList = mutableListOf<String>()
                    val currentBuf = StringBuilder()
                    for (l in subLines) {
                        if (isChapterHeadingLine(l) || l.startsWith("#") || AUTHOR_LINE_REGEX.matches(l)) {
                            if (currentBuf.isNotEmpty()) {
                                splitList.add(currentBuf.toString().trim())
                                currentBuf.setLength(0)
                            }
                            splitList.add(l)
                        } else {
                            if (currentBuf.isNotEmpty()) currentBuf.append(" ")
                            currentBuf.append(l)
                        }
                    }
                    if (currentBuf.isNotEmpty()) splitList.add(currentBuf.toString().trim())
                    splitList
                } else {
                    listOf(subLines.joinToString(" ").trim())
                }
            }
            .filter { it.isNotBlank() }

        // Group blocks into semantic sections (Chapters/Messages)
        val sections = mutableListOf<SemanticSection>()
        var currentChapterIndex = 0
        var activeSection = SemanticSection(
            chapterTag = if (isRtl) "مدخل الكتاب" else "Introduction",
            chapterTitle = bookTitle
        )

        for ((idx, block) in rawBlocks.withIndex()) {
            val clean = block.removePrefix("#").removePrefix("#").removePrefix("#").trim()

            // Skip the very first title/author metadata lines if they match bookTitle/bookAuthor
            if (idx <= 2) {
                if (clean.equals(bookTitle, ignoreCase = true) ||
                    TITLE_LINE_REGEX.matches(clean) ||
                    SUBTITLE_LINE_REGEX.matches(clean) ||
                    AUTHOR_LINE_REGEX.matches(clean)
                ) {
                    continue
                }
            }

            if (isChapterHeadingLine(block)) {
                if (activeSection.paragraphs.isNotEmpty()) {
                    sections.add(activeSection)
                }
                currentChapterIndex++
                val parts = clean.split(Regex("[:：\\-—]"), limit = 2).map { it.trim() }
                val tag = if (parts.size == 2 && parts[0].length <= 32) {
                    parts[0]
                } else if (isRtl) {
                    "الفصل $currentChapterIndex"
                } else {
                    "Chapter $currentChapterIndex"
                }
                val title = if (parts.size == 2 && parts[1].isNotEmpty()) parts[1] else clean
                activeSection = SemanticSection(chapterTag = tag, chapterTitle = title)
            } else {
                // Ensure oversized single paragraphs (> 140 words) are split cleanly on sentence boundaries
                val words = block.split(Regex("\\s+")).filter { it.isNotBlank() }
                if (words.size > 135) {
                    activeSection.paragraphs.addAll(splitLongParagraphBySentences(block, targetWords = 95))
                } else {
                    activeSection.paragraphs.add(block)
                }
            }
        }
        if (activeSection.paragraphs.isNotEmpty()) {
            sections.add(activeSection)
        }

        if (sections.isEmpty()) {
            sections.add(
                SemanticSection(
                    chapterTag = if (isRtl) "متن الكتاب" else "Book Content",
                    chapterTitle = bookTitle,
                    paragraphs = mutableListOf(rawText)
                )
            )
        }

        // Paginate each section into balanced pages (85 to 125 words per page)
        var chapterCounter = 0
        for (section in sections) {
            chapterCounter++
            val currentPageBlocks = mutableListOf<String>()
            var currentPageWords = 0
            var sectionPageIndex = 0

            fun flushSectionPage() {
                if (currentPageBlocks.isEmpty()) return
                sectionPageIndex++
                val isFirstPageOfSection = (sectionPageIndex == 1)
                val pageHtmlBuilder = StringBuilder()

                if (isFirstPageOfSection) {
                    pageHtmlBuilder.append(
                        """
                        <div class="letter-envelope-header chapter-opener">
                          <span class="letter-seal">${theme.badgeEmoji}</span>
                          <span class="letter-envelope-title">${escapeHtml(section.chapterTag)}</span>
                        </div>
                        <h1>${escapeHtml(section.chapterTitle)}</h1>
                        <div class="letter-greeting">${if (isRtl) "تأمل وإشراقة" else "Reflection"} <span class="reader-name-slot" data-reader-name="true"></span></div>
                        """.trimIndent()
                    ).append("\n")
                } else {
                    pageHtmlBuilder.append(
                        """
                        <h2>${escapeHtml(section.chapterTitle)} <small style="font-size:0.65em;color:var(--accent-gold);">(${sectionPageIndex})</small></h2>
                        """.trimIndent()
                    ).append("\n")
                }

                for (blk in currentPageBlocks) {
                    pageHtmlBuilder.append(formatSemanticParagraphToHtml(blk, isRtl)).append("\n")
                }

                // On the first page of the first section, add the subtle Personal Reader Name input card
                if (chapterCounter == 1 && sectionPageIndex == 1) {
                    pageHtmlBuilder.append(buildReaderNameWelcomeBoxHtml(isRtl)).append("\n")
                }

                val pageTitle = if (sectionPageIndex == 1) {
                    section.chapterTitle
                } else {
                    "${section.chapterTitle} (${sectionPageIndex})"
                }

                pages.add(
                    ConvertedPageItem(
                        tag = section.chapterTag,
                        title = pageTitle,
                        type = if (isFirstPageOfSection) "opener" else "reading",
                        html = pageHtmlBuilder.toString()
                    )
                )
                currentPageBlocks.clear()
                currentPageWords = 0
            }

            for (para in section.paragraphs) {
                val pWords = para.split(Regex("\\s+")).count { it.isNotBlank() }.coerceAtLeast(1)
                // Target 85..125 words per page, always ending at a complete paragraph/sentence
                if (currentPageWords >= 85 && (currentPageWords + pWords) > 125) {
                    flushSectionPage()
                }
                currentPageBlocks.add(para)
                currentPageWords += pWords
                if (currentPageWords >= 110) {
                    flushSectionPage()
                }
            }
            flushSectionPage()
        }

        // Add interactive reflection & bookmark keepsake page at the end
        pages.add(
            ConvertedPageItem(
                tag = if (isRtl) "مساحة التأمل" else "Reader Journal",
                title = if (isRtl) "دفتر التأملات والخلاصة الشخصية" else "Personal Reflections & Notes",
                type = "reading",
                html = buildClosingReflectionPageHtml(bookTitle, theme, isRtl)
            )
        )

        return pages
    }

    private fun splitLongParagraphBySentences(paragraph: String, targetWords: Int = 95): List<String> {
        val sentences = paragraph.split(Regex("(?<=[.!?؟؛\\n])\\s+"))
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        var count = 0

        for (s in sentences) {
            val w = s.split(Regex("\\s+")).count { it.isNotBlank() }
            if (count >= 85 && (count + w) > 125 && current.isNotEmpty()) {
                chunks.add(current.toString().trim())
                current.setLength(0)
                count = 0
            }
            if (current.isNotEmpty()) current.append(" ")
            current.append(s.trim())
            count += w
            if (count >= targetWords) {
                chunks.add(current.toString().trim())
                current.setLength(0)
                count = 0
            }
        }
        if (current.isNotEmpty()) {
            chunks.add(current.toString().trim())
        }
        return chunks
    }

    /**
     * Converts an individual paragraph into rich semantic HTML boxes:
     * - `.quran-box` for Quranic verses (`﴿...﴾` or `قال تعالى`)
     * - `.hadith-box` for Prophetic Hadiths & quotes (`«...»` or `قال رسول الله`)
     * - `.wisdom-pearl` or `.heart-keepsake-card` for pearls, summaries, whispers (`خلاصة`, `همسة`, `فائدة`, `درة`, `وصية`)
     * - `.step-card` for numbered/bulleted items
     */
    private fun formatSemanticParagraphToHtml(rawBlock: String, isRtl: Boolean): String {
        val trimmed = rawBlock.trim()
        if (trimmed.isEmpty()) return ""

        // 1. Subheading (`### ...` or short `...:`)
        if (isSubheadingLine(trimmed)) {
            val cleanSub = trimmed.removePrefix("###").removePrefix("##").removePrefix("#").trim()
            return "<h2>${escapeHtml(cleanSub)}</h2>"
        }

        val unvocalized = stripArabicDiacriticsForMatching(trimmed)

        // 2. Quranic Verse Detection (`﴿...﴾` or starts with `قال تعالى` / `قال الله تعالى`)
        if ((trimmed.contains("﴿") && trimmed.contains("﴾") && trimmed.length <= 320) ||
            unvocalized.startsWith("قال تعالى") ||
            unvocalized.startsWith("قال الله تعالى") ||
            unvocalized.startsWith("يقول الله تعالى") ||
            unvocalized.startsWith("قوله تعالى")
        ) {
            val sourceMatch = Regex("\\[([^\\]]+)\\]").find(trimmed)
            val sourceText = sourceMatch?.groupValues?.get(1)?.trim()
            val mainVerse = if (sourceMatch != null) {
                trimmed.removeRange(sourceMatch.range).trim()
            } else {
                trimmed
            }.replace(Regex("\\{\\s*([^}]+)\\s*\\}"), "﴿ $1 ﴾")
                .replace(Regex("\\(\\(\\s*([^)]+)\\s*\\)\\)"), "﴿ $1 ﴾")

            val formattedVerse = if (mainVerse.contains("﴿")) {
                escapeHtml(mainVerse)
            } else {
                "﴿ ${escapeHtml(mainVerse)} ﴾"
            }
            val sourceHtml = if (!sourceText.isNullOrBlank()) {
                "\n  <div class=\"ayah-source\">[${escapeHtml(sourceText)}]</div>"
            } else {
                ""
            }
            return """
                <div class="quran-box" data-role="quran">
                  <p>$formattedVerse</p>$sourceHtml
                </div>
            """.trimIndent()
        }

        // 3. Hadith & Quote Detection (`«...»` or `قال رسول الله` / `قال النبي` / `وفي الحديث`)
        val isHadithOrQuote = (trimmed.startsWith("«") && trimmed.endsWith("»")) ||
            unvocalized.startsWith("قال رسول الله") ||
            unvocalized.startsWith("قال النبي") ||
            unvocalized.startsWith("وفي الحديث") ||
            unvocalized.startsWith("حديث شريف") ||
            unvocalized.startsWith("عن أبي ") ||
            unvocalized.startsWith("عن عائشة") ||
            unvocalized.startsWith("عن ابن ") ||
            unvocalized.startsWith("عن أنس") ||
            unvocalized.startsWith("عن عمر") ||
            (trimmed.contains("«") && trimmed.contains("»") && trimmed.length <= 280)

        if (isHadithOrQuote) {
            val upgradedHadith = trimmed
                .replace(Regex("\\(\\(\\s*([^)]+)\\s*\\)\\)"), "« $1 »")
            return """
                <div class="hadith-box" data-role="hadith">
                  <p>${escapeHtml(upgradedHadith)}</p>
                </div>
            """.trimIndent()
        }

        // 4. Pearls, Whispers, Summaries, Da'wah Stories, Self-Help Actions & Academic Definitions
        val pearlPrefixes = listOf(
            "خلاصة", "همسة", "فائدة", "درة", "وصية", "قاعدة", "إشراقة", "تأمل", "تقول ", "حكمة",
            "جرعة إيمانية", "محاسبة", "واجب عملي", "خطوة عملية", "تطبيق عملي", "تدريب",
            "موقف وعبرة", "موقف", "عبرة", "قصة قصيرة", "إضاءة", "قاعدة ذهبية",
            "تعريف", "مفهوم", "مثال تطبيقي", "مثال", "ملاحظة هامة", "ملاحظة", "نتيجة",
            "note:", "key takeaway:", "summary:", "tip:", "reflection:", "definition:", "example:", "action step:"
        )
        val matchedPearlPrefix = pearlPrefixes.firstOrNull { unvocalized.lowercase().startsWith(it) }
        if (matchedPearlPrefix != null) {
            val parts = trimmed.split(Regex("[:：\\-—]"), limit = 2)
            val badge = if (parts.size == 2 && parts[0].length <= 42) parts[0].trim() else (if (isRtl) "✦ درة وإشراقة" else "✦ Key Insight")
            val content = if (parts.size == 2 && parts[1].isNotBlank()) parts[1].trim() else trimmed

            return when {
                unvocalized.startsWith("همسة") || unvocalized.startsWith("وصية") || unvocalized.startsWith("جرعة") ->
                    """
                    <div class="heart-keepsake-card">
                      <div class="heart-badge">🌸 ${escapeHtml(badge)}</div>
                      <p>${formatInlineHighlights(escapeHtml(content))}</p>
                    </div>
                    """.trimIndent()
                unvocalized.startsWith("خطوة") || unvocalized.startsWith("واجب") || unvocalized.startsWith("تطبيق") || unvocalized.startsWith("تدريب") || unvocalized.startsWith("محاسبة") ->
                    """
                    <div class="wisdom-pearl action-callout">
                      <span class="wisdom-badge">🎯 ${escapeHtml(badge)}</span>
                      ${formatInlineHighlights(escapeHtml(content))}
                    </div>
                    """.trimIndent()
                unvocalized.startsWith("تعريف") || unvocalized.startsWith("مفهوم") || unvocalized.startsWith("مثال") || unvocalized.startsWith("نتيجة") ->
                    """
                    <div class="wisdom-pearl academic-callout">
                      <span class="wisdom-badge">📘 ${escapeHtml(badge)}</span>
                      ${formatInlineHighlights(escapeHtml(content))}
                    </div>
                    """.trimIndent()
                else ->
                    """
                    <div class="wisdom-pearl">
                      <span class="wisdom-badge">✦ ${escapeHtml(badge)}</span>
                      ${formatInlineHighlights(escapeHtml(content))}
                    </div>
                    """.trimIndent()
            }
        }

        // 5. Poetry Bait / Stanza detection (lines containing `***` or `...` separating two hemistichs)
        if (trimmed.contains(" *** ") || (trimmed.contains(" ... ") && trimmed.length <= 130 && !trimmed.endsWith("."))) {
            val delimiter = if (trimmed.contains(" *** ")) " *** " else " ... "
            val hemistichs = trimmed.split(delimiter, limit = 2).map { it.trim() }
            if (hemistichs.size == 2 && hemistichs[0].isNotEmpty() && hemistichs[1].isNotEmpty()) {
                return """
                    <div class="poetry-stanza">
                      <div class="poetry-shatr">${escapeHtml(hemistichs[0])}</div>
                      <div class="poetry-ornament">✦</div>
                      <div class="poetry-shatr">${escapeHtml(hemistichs[1])}</div>
                    </div>
                """.trimIndent()
            }
        }

        // 6. Numbered or Bulleted Step Card
        val stepMatch = Regex("^([0-9٠-٩]{1,2})[.)\\-]\\s+(.+)$", RegexOption.DOT_MATCHES_ALL).find(trimmed)
        if (stepMatch != null) {
            val num = stepMatch.groupValues[1]
            val stepBody = stepMatch.groupValues[2].trim()
            return """
                <div class="step-card">
                  <div class="step-num">${escapeHtml(num)}</div>
                  <div class="step-content">${formatInlineHighlights(escapeHtml(stepBody))}</div>
                </div>
            """.trimIndent()
        }

        if (trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.startsWith("* ")) {
            val bulletBody = trimmed.substring(2).trim()
            return """
                <div class="step-card">
                  <div class="step-num">✦</div>
                  <div class="step-content">${formatInlineHighlights(escapeHtml(bulletBody))}</div>
                </div>
            """.trimIndent()
        }

        // 7. Standard Narrative Paragraph (with inline Quranic/Quote/Numbers highlighting)
        return "<p dir=\"auto\">${formatInlineHighlights(escapeHtml(trimmed))}</p>"
    }

    private fun formatInlineHighlights(escapedText: String): String {
        return escapedText
            .replace(Regex("﴿([^﴾]+)﴾")) { m ->
                "<span class=\"quran-verse\" data-role=\"quran\">﴿${m.groupValues[1]}﴾</span>"
            }
            .replace(Regex("«([^»]+)»")) { m ->
                "<span class=\"hadith-quote\" data-role=\"hadith\">«${m.groupValues[1]}»</span>"
            }
            .replace(Regex("\\[([0-9٠-٩]{1,4})\\]")) { m ->
                "<span class=\"num-badge\" data-role=\"numbers\">[${m.groupValues[1]}]</span>"
            }
            .replace(Regex("\\*\\*([^*]+)\\*\\*")) { m ->
                "<strong>${m.groupValues[1]}</strong>"
            }
    }

    private fun buildReaderNameWelcomeBoxHtml(isRtl: Boolean): String {
        return if (isRtl) {
            """
            <div class="interactive-box" style="margin-top: 18px;">
              <h3 style="margin-bottom: 6px; font-size: 0.92em;">💌 اكتبي/اكتب اسمك الكريم لنخاطبك به بين صفحات الكتاب:</h3>
              <div style="display: flex; gap: 8px;">
                <input id="readerNameInput" class="field-input" type="text" placeholder="اكتب اسمك هنا..." oninput="saveReaderName(this.value)"/>
                <button class="primary-btn" style="padding: 6px 16px; font-size: .75em;" onclick="alertSavedName()">حفظ</button>
              </div>
            </div>
            """.trimIndent()
        } else {
            """
            <div class="interactive-box" style="margin-top: 18px;">
              <h3 style="margin-bottom: 6px; font-size: 0.92em;">💌 Personalize Your Reading Journey:</h3>
              <div style="display: flex; gap: 8px;">
                <input id="readerNameInput" class="field-input" type="text" placeholder="Enter your name..." oninput="saveReaderName(this.value)"/>
                <button class="primary-btn" style="padding: 6px 16px; font-size: .75em;" onclick="alertSavedName()">Save</button>
              </div>
            </div>
            """.trimIndent()
        }
    }

    private fun buildClosingReflectionPageHtml(
        bookTitle: String,
        theme: BookVisualTheme,
        isRtl: Boolean
    ): String {
        return if (isRtl) {
            """
            <div class="letter-envelope-header">
              <span class="letter-seal">${theme.badgeEmoji}</span>
              <span class="letter-envelope-title">مساحة التأمل الشخصي</span>
            </div>
            <h1>خُلاَصَةُ رِحْلَتِي مَعَ الكِتَاب</h1>
            <div class="heart-keepsake-card">
              <div class="heart-badge">${theme.badgeEmoji} مسك الختام <span class="reader-name-slot" data-reader-name="true"></span></div>
              <p>«كل كتاب تقرؤه يترك في قلبك بذرة نور، فاحرص على أن تسقيها بالعمل والتطبيق.»</p>
            </div>
            <div class="interactive-box" style="margin-top: 16px;">
              <h3 style="margin-bottom: 6px;">✍️ أهم الفوائد والخواطر التي استوقفتني في «${escapeHtml(bookTitle)}»:</h3>
              <textarea id="journal_book_reflection" class="field-textarea" placeholder="دوّن هنا خلاصة أفكارك، الفوائد التي لمست قلبك، أو خطتك العملية..." oninput="saveField(this.id, this.value)"></textarea>
              <div class="save-indicator">✓ تُحفظ ملاحظاتك تلقائياً في جهازك</div>
            </div>
            """.trimIndent()
        } else {
            """
            <div class="letter-envelope-header">
              <span class="letter-seal">${theme.badgeEmoji}</span>
              <span class="letter-envelope-title">Personal Reflection Journal</span>
            </div>
            <h1>My Key Takeaways</h1>
            <div class="heart-keepsake-card">
              <div class="heart-badge">${theme.badgeEmoji} Journey Completed <span class="reader-name-slot" data-reader-name="true"></span></div>
              <p>“Every great book plants a seed of wisdom—nurture it with reflection and action.”</p>
            </div>
            <div class="interactive-box" style="margin-top: 16px;">
              <h3 style="margin-bottom: 6px;">✍️ My Notes & Reflections on “${escapeHtml(bookTitle)}”:</h3>
              <textarea id="journal_book_reflection" class="field-textarea" placeholder="Write your key takeaways, favorite quotes, and action steps..." oninput="saveField(this.id, this.value)"></textarea>
              <div class="save-indicator">✓ Saved automatically on your device</div>
            </div>
            """.trimIndent()
        }
    }

    private fun buildCoverPageHtml(
        title: String,
        subtitle: String,
        author: String,
        theme: BookVisualTheme,
        isRtl: Boolean
    ): String {
        val svgArt = buildThemeSvgEmblem(theme)
        val startBtnLabel = if (isRtl) "افتح صفحات الكتاب ✦" else "Open Book ✦"
        val editionEyebrow = if (isRtl) {
            "✦ طَبْعَةٌ رَقْمِيَّةٌ تَفَاعُلِيَّةٌ فَاخِرَة • مَكْتَبَتِي ✦"
        } else {
            "✦ LUXURY INTERACTIVE EDITION • MAKTABATI ✦"
        }

        return """
            <div class="cover-page">
              <div class="eyebrow">$editionEyebrow</div>
              $svgArt
              <h1>${escapeHtml(title)}</h1>
              <div class="subtitle">${escapeHtml(subtitle)}</div>
              <div class="ornament">❖ ❖ ❖</div>
              <div class="author">${escapeHtml(author)}</div>
              <button class="primary-btn" onclick="window.goToPage(1)">$startBtnLabel</button>
            </div>
        """.trimIndent()
    }

    private fun buildThemeSvgEmblem(theme: BookVisualTheme): String {
        return when (theme.layoutStyle) {
            PublishingLayoutStyle.SOFT_BOTANICAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8" stroke-dasharray="4 3"/>
                  <circle cx="120" cy="120" r="95" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                  <path d="M60 140 C 50 110, 70 75, 120 60 C 170 75, 190 110, 180 140" stroke="${theme.accentSecondaryHex}" stroke-width="2.2" stroke-linecap="round"/>
                  <path d="M80 160 C 90 185, 150 185, 160 160" stroke="${theme.accentGoldHex}" stroke-width="1.8" stroke-linecap="round"/>
                  <circle cx="95" cy="176" r="3.5" fill="${theme.accentGoldHex}"/>
                  <circle cx="120" cy="182" r="4.5" fill="${theme.accentGoldHex}"/>
                  <circle cx="145" cy="176" r="3.5" fill="${theme.accentGoldHex}"/>
                  <g transform="translate(120, 122)">
                    <path d="M0 -34 C 18 -34, 30 -16, 26 4 C 20 22, -20 22, -26 4 C -30 -16, -18 -34, 0 -34 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                    <path d="M-18 -12 C -24 4, -12 24, 0 28 C 12 24, 24 4, 18 -12 C 12 -24, -12 -24, -18 -12 Z" fill="${theme.accentPrimaryLightHex}" fill-opacity="0.45"/>
                    <circle cx="0" cy="4" r="11" fill="${theme.accentPrimaryHex}"/>
                    <circle cx="0" cy="4" r="5.5" fill="${theme.accentPrimaryDarkHex}"/>
                  </g>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.HERITAGE_ORNATE -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <circle cx="120" cy="120" r="92" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.6"/>
                  <rect x="74" y="74" width="92" height="92" rx="8" stroke="${theme.accentGoldHex}" stroke-width="1.8" fill="${theme.bgSoftAccentHex}"/>
                  <rect x="74" y="74" width="92" height="92" rx="8" transform="rotate(45 120 120)" stroke="${theme.accentPrimaryHex}" stroke-width="1.8" fill="${theme.bgSecondaryHex}" fill-opacity="0.8"/>
                  <circle cx="120" cy="120" r="24" fill="${theme.accentPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="9" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.DAWAH_MOTIVATIONAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.8" stroke-dasharray="6 4"/>
                  <circle cx="120" cy="120" r="92" fill="${theme.bgSecondaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.5"/>
                  <polygon points="120,48 138,102 192,120 138,138 120,192 102,138 48,120 102,102" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="18" fill="${theme.accentPrimaryHex}"/>
                  <circle cx="120" cy="120" r="7" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.CLASSIC_BOOK_PRESS, PublishingLayoutStyle.NARRATIVE_STORY -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <rect x="28" y="28" width="184" height="184" rx="22" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <rect x="40" y="40" width="160" height="160" rx="16" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.4" stroke-dasharray="4 3"/>
                  <path d="M74 92 C 92 84, 112 88, 120 96 C 128 88, 148 84, 166 92 V 154 C 148 146, 128 150, 120 158 C 112 150, 92 146, 74 154 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="1.8"/>
                  <line x1="120" y1="96" x2="120" y2="158" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="70" r="5.5" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.ACADEMIC_STRUCTURED, PublishingLayoutStyle.EDITORIAL_MAGAZINE, PublishingLayoutStyle.PURE_MINIMAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="94" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                  <path d="M68 86 C 88 78, 112 82, 120 92 C 128 82, 152 78, 172 86 V 158 C 152 150, 128 154, 120 164 C 112 154, 88 150, 68 158 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="2"/>
                  <line x1="120" y1="92" x2="120" y2="164" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <circle cx="120" cy="66" r="6" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()
        }
    }

    // =============================================================================================
    // 4. COMPLETE SELF-CONTAINED LUXURY HTML5 BOOK TEMPLATE RENDERER
    // =============================================================================================

    private fun renderCompleteLuxuryHtml(
        bookTitle: String,
        bookSubtitle: String,
        bookAuthor: String,
        theme: BookVisualTheme,
        isRtl: Boolean,
        language: String,
        pages: List<ConvertedPageItem>
    ): String {
        val dirAttr = if (isRtl) "rtl" else "ltr"
        val pagesJsonArray = JSONArray()
        for (p in pages) {
            val obj = JSONObject()
            obj.put("tag", p.tag)
            obj.put("title", p.title)
            obj.put("type", p.type)
            obj.put("html", p.html)
            pagesJsonArray.put(obj)
        }

        val storageSlug = bookTitle.hashCode().toUInt().toString(16)
        val prevLabel = if (isRtl) "السابق" else "Previous"
        val nextLabel = if (isRtl) "التالي" else "Next"
        val searchHeaderTitle = if (isRtl) "البحث في الكتاب ${theme.badgeEmoji}" else "Search Book ${theme.badgeEmoji}"
        val searchPlaceholder = if (isRtl) "ابحث عن آية، حديث، عنوان، أو أي كلمة..." else "Search any word or phrase..."
        val searchEmptyPrompt = if (isRtl) "اكتب كلمة للبحث في جميع صفحات الكتاب..." else "Type a word to search across all pages..."
        val tocHeaderTitle = if (isRtl) "فهرس فصول الكتاب" else "Table of Contents"

        return """<!DOCTYPE html>
<html lang="$language" dir="$dirAttr">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover"/>
  <meta name="theme-color" content="${theme.bgPrimaryHex}"/>
  <meta name="color-scheme" content="light"/>
  <meta name="author" content="${escapeHtml(bookAuthor)}"/>
  <meta name="description" content="${escapeHtml(bookSubtitle)}"/>
  <title>${escapeHtml(bookTitle)}</title>

  <style>
:root {
  --bg-primary: ${theme.bgPrimaryHex};
  --bg-secondary: ${theme.bgSecondaryHex};
  --paper-bg: ${theme.bgSecondaryHex};
  --bg-soft-rose: ${theme.bgSoftAccentHex};
  --bg-soft-sage: ${theme.bgSoftSecondaryHex};
  --bg-pearl-gold: ${theme.bgPearlGoldHex};

  --accent-rose: ${theme.accentPrimaryHex};
  --accent-rose-dark: ${theme.accentPrimaryDarkHex};
  --accent-rose-light: ${theme.accentPrimaryLightHex};
  --accent-sage: ${theme.accentSecondaryHex};
  --accent-sage-dark: ${theme.accentSecondaryDarkHex};
  --accent-gold: ${theme.accentGoldHex};
  --accent-gold-soft: rgba(198,154,78,0.24);

  --text-primary: ${theme.textPrimaryHex};
  --ink-main: ${theme.textPrimaryHex};
  --text-secondary: ${theme.textSecondaryHex};
  --line: rgba(180,140,110,0.20);
  --border-ornate: rgba(198,154,78,0.34);
  --paper-edge: ${theme.paperEdgeHex};

  --shadow-card: 0 16px 42px rgba(40,25,30,0.07);
  --reader-font: 21px;
  --reader-font-size: 100%;
  --font-scale: 1;

  --font-body: "${theme.fontBody}", "Noto Naskh Arabic", "Amiri", serif;
  --font-headings: "${theme.fontHeadings}", "Aref Ruqaa", "Amiri", serif;
  --font-chapter-titles: "${theme.fontChapterTitles}", "Reem Kufi", "Noto Kufi Arabic", sans-serif;
  --font-quotes: "${theme.fontQuotes}", "Amiri", "Noto Naskh Arabic", serif;
  --font-quran: "${theme.fontQuran}", "Amiri Quran", "Amiri", serif;
  --font-interface: "Noto Sans Arabic", "Cairo", "IBM Plex Sans Arabic", sans-serif;
  --font-numbers: "Noto Sans Arabic", "Cairo", sans-serif;
  --reader-family: var(--font-body);

  color-scheme: light;
}

${com.example.data.typography.MaktabatiFontRegistry.buildOfflineFontFacesCss("https://maktabati.app/assets")}

.hadith-quote {
  font-family: var(--font-quotes);
  color: var(--accent-sage-dark);
}
${buildLayoutVariantCss(theme)}

* {
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
  max-width: 100%;
  overflow-wrap: anywhere;
}

html, body, .app {
  touch-action: pan-y !important;
}

button, input, select, textarea {
  touch-action: manipulation;
  font: inherit;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-body);
}

body {
  overflow: hidden;
}

.app {
  height: 100vh;
  max-height: 100vh;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(155deg, var(--bg-primary), var(--bg-soft-rose) 100%);
}

.top-bar {
  height: 56px;
  min-height: 56px;
  flex: 0 0 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(12px, 3vw, 20px);
  gap: 10px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--line);
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brand-gem {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  font-size: 16px;
}

.tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
}

.icon-btn, .nav-btn {
  border: 1px solid var(--line);
  background: var(--bg-primary);
  color: var(--accent-rose-dark);
  border-radius: 12px;
  min-width: 40px;
  min-height: 40px;
  padding: 6px 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-interface);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.icon-btn svg {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: none;
  stroke-width: 2.1;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.icon-btn.bookmarked {
  background: var(--bg-pearl-gold);
  color: var(--accent-gold);
  border-color: var(--accent-gold);
}

.running-header {
  height: 34px;
  min-height: 34px;
  flex: 0 0 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 2px 14px;
  background: var(--accent-rose-dark);
  color: #FAF4EF;
  border-bottom: 2px solid var(--accent-gold);
  font-family: var(--font-interface);
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.running-header[hidden] {
  display: none;
}

.running-header-mark {
  color: #FDE68A;
}

.book-stage {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: clamp(10px, 2.5vw, 20px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.page-canvas {
  width: min(100%, 720px);
  min-height: 100%;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  padding: clamp(18px, 4vw, 34px);
  box-shadow: var(--shadow-card);
  position: relative;
}

.page {
  font-family: var(--font-body);
  font-size: calc(var(--reader-font, 21px));
  line-height: 2.02;
  color: var(--text-primary);
}

.page p {
  margin: 0 0 14px;
  text-align: justify;
  line-height: 2.02;
}

.page h1 {
  font-family: var(--font-headings);
  font-size: clamp(1.35em, 4.2vw, 1.78em);
  line-height: 1.65;
  color: var(--accent-rose-dark);
  margin: 0 0 12px;
}

.page h2 {
  font-family: var(--font-headings);
  font-size: clamp(1.15em, 3.6vw, 1.42em);
  line-height: 1.65;
  color: var(--accent-rose-dark);
  margin: 14px 0 10px;
}

.cover-page {
  min-height: clamp(420px, 68vh, 720px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 20px 10px;
}

.cover-page .eyebrow {
  font-family: var(--font-interface);
  font-size: 0.62em;
  font-weight: 700;
  color: var(--accent-gold);
  margin-bottom: 12px;
}

.cover-art-svg {
  width: clamp(140px, 38vw, 195px);
  height: auto;
  margin: 6px auto 14px;
}

.cover-page h1 {
  font-family: var(--font-headings);
  font-size: clamp(1.55em, 5vw, 2.2em);
  color: var(--accent-rose-dark);
  margin: 6px auto 10px;
}

.cover-page .subtitle {
  font-family: var(--font-body);
  font-size: 0.92em;
  color: var(--text-secondary);
  max-width: 90%;
  margin: 0 auto 12px;
}

.ornament {
  color: var(--accent-gold);
  font-size: 0.9em;
  letter-spacing: 6px;
  margin: 8px 0;
}

.cover-page .author {
  font-family: var(--font-interface);
  font-size: 0.78em;
  font-weight: 700;
  color: var(--accent-rose-dark);
  margin-bottom: 18px;
}

.primary-btn {
  border: 1px solid var(--accent-gold);
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  color: #fff;
  border-radius: 99px;
  padding: 10px 24px;
  font-family: var(--font-interface);
  font-size: 0.78em;
  font-weight: 700;
  cursor: pointer;
}

.quran-box {
  margin: 16px 0;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-pearl-gold), var(--bg-secondary));
  border: 1.5px solid var(--accent-gold-soft);
  border-inline-start: 4px solid var(--accent-gold);
  text-align: center;
}

.quran-box p, .quran-verse {
  font-family: var(--font-quran);
  font-size: 1.14em;
  line-height: 2.15;
  color: var(--accent-rose-dark);
  margin: 0 0 4px;
  text-align: center;
}

.quran-box .ayah-source {
  font-family: var(--font-interface);
  font-size: 0.68em;
  color: var(--accent-gold);
  font-weight: 700;
}

.hadith-box {
  margin: 16px 0;
  padding: 15px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-sage), var(--bg-secondary));
  border: 1px solid var(--line);
  border-inline-start: 4px solid var(--accent-sage);
  text-align: center;
}

.hadith-box p {
  font-family: var(--font-quotes);
  font-size: 1.08em;
  line-height: 2.05;
  color: var(--accent-sage-dark);
  margin: 0;
  text-align: center;
}

.wisdom-pearl {
  margin: 16px 0;
  padding: 14px 18px;
  border-radius: 12px;
  background: var(--bg-pearl-gold);
  border: 1px solid var(--accent-gold-soft);
  border-inline-start: 3px solid var(--accent-gold);
  font-family: var(--font-quotes);
  font-size: 0.98em;
  line-height: 1.95;
}

.wisdom-badge {
  display: block;
  font-family: var(--font-interface);
  font-size: 0.65em;
  font-weight: 700;
  color: var(--accent-gold);
  margin-bottom: 5px;
}

.heart-keepsake-card {
  background: linear-gradient(145deg, var(--bg-secondary), var(--bg-soft-rose) 65%, var(--bg-pearl-gold) 100%);
  border: 1.5px solid var(--accent-rose-light);
  border-radius: 16px;
  padding: 18px;
  margin: 16px 0;
  text-align: center;
}

.heart-badge {
  display: inline-block;
  font-family: var(--font-interface);
  font-size: 0.68em;
  font-weight: 700;
  color: var(--accent-rose-dark);
  background: var(--bg-secondary);
  border: 1px solid var(--accent-rose-light);
  border-radius: 99px;
  padding: 3px 12px;
  margin-bottom: 10px;
}

.heart-keepsake-card p {
  font-family: var(--font-quotes);
  font-size: 1.12em;
  line-height: 2.1;
  color: var(--accent-rose-dark);
  margin: 0;
  text-align: center;
}

.letter-envelope-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-pearl-gold));
  border: 1px solid var(--accent-gold-soft);
  border-radius: 99px;
  padding: 5px 14px;
  margin-bottom: 12px;
}

.letter-envelope-title {
  font-family: var(--font-interface);
  font-size: 0.68em;
  font-weight: 700;
  color: var(--accent-rose-dark);
}

.letter-greeting {
  font-family: var(--font-headings);
  font-size: 1.08em;
  color: var(--accent-rose-dark);
  margin-bottom: 12px;
  font-weight: 700;
  border-bottom: 1px dashed var(--line);
  padding-bottom: 6px;
}

.step-card {
  margin: 10px 0;
  padding: 11px 14px;
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 10px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.step-num {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border-radius: 50%;
  background: var(--accent-rose);
  color: #fff;
  display: grid;
  place-items: center;
  font-family: var(--font-numbers);
  font-weight: 700;
  font-size: 0.75em;
}

.step-content {
  flex: 1;
}

.interactive-box {
  margin: 16px 0;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--bg-soft-rose);
  border: 1.5px solid var(--line);
}

.field-input, .field-textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1.5px solid var(--line);
  border-radius: 10px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 0.8em;
  outline: none;
}

.field-textarea {
  min-height: 76px;
  resize: vertical;
}

.save-indicator {
  font-family: var(--font-interface);
  font-size: 0.66em;
  color: var(--accent-sage-dark);
  font-weight: 700;
  margin-top: 5px;
}

.bottom-nav-dock {
  min-height: 62px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 14px calc(env(safe-area-inset-bottom, 6px) + 6px);
  background: var(--bg-secondary);
  border-top: 1px solid var(--line);
}

.nav-btn {
  min-width: 90px;
  border-radius: 12px;
  background: var(--accent-rose-dark);
  color: #fff;
  border-color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 12px;
  padding: 8px 14px;
}

.nav-btn:disabled {
  opacity: 0.35;
}

.page-counter {
  font-family: var(--font-numbers);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 99px;
  padding: 5px 14px;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 45;
  background: rgba(30,20,25,0.45);
  display: none;
  align-items: flex-end;
  justify-content: center;
  padding: 12px;
}

.overlay.open {
  display: flex;
}

.panel {
  width: min(100%, 640px);
  max-height: 78vh;
  overflow-y: auto;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
}

.panel-head h2 {
  margin: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 15px;
}

.search-bar-wrap {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

#searchInput {
  width: 100%;
  min-height: 44px;
  padding: 9px 14px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 14px;
}

.search-clear-btn {
  border-radius: 8px;
  border: 1px solid var(--line);
  background: var(--bg-secondary);
  color: var(--accent-rose);
  font-family: var(--font-interface);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  cursor: pointer;
}

.search-clear-btn[hidden] {
  display: none;
}

.search-status {
  font-family: var(--font-interface);
  font-size: 12px;
  color: var(--text-secondary);
  margin: 4px 2px 10px;
}

.search-result, .toc-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  text-align: start;
  padding: 10px 14px;
  margin: 6px 0;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 13px;
  cursor: pointer;
}

.search-result {
  display: block;
}

.search-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.toc-item.toc-part {
  font-family: var(--font-chapter-titles);
  background: var(--bg-soft-rose);
  font-weight: 700;
  color: var(--accent-rose-dark);
}

.toc-item small, .search-result-page {
  color: var(--accent-gold);
  font-family: var(--font-numbers);
  font-size: 11px;
  font-weight: 700;
  background: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid var(--accent-gold-soft);
}

mark.search-hit-mark, mark.page-search-highlight {
  background: rgba(198,154,78,0.25);
  color: var(--accent-rose-dark);
  padding: 1px 5px;
  border-radius: 5px;
  font-weight: 700;
}
  </style>
</head>
<body>
  <div class="app">
    <header class="top-bar">
      <div class="brand">
        <div class="brand-gem" aria-hidden="true">${theme.badgeEmoji}</div>
        <span>${escapeHtml(bookTitle)}</span>
      </div>
      <div class="tools">
        <button id="searchOpen" class="icon-btn" aria-label="بحث في الكتاب" title="بحث في الكتاب">
          <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </button>
        <button id="tocBtn" class="icon-btn" aria-label="فهرس الكتاب" title="فهرس الكتاب">
          <svg viewBox="0 0 24 24"><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="14" y2="12"/><line x1="4" y1="18" x2="18" y2="18"/></svg>
        </button>
        <button id="bookmarkBtn" class="icon-btn" aria-label="حفظ علامة مرجعية" title="حفظ علامة مرجعية">
          <svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
        </button>
      </div>
    </header>

    <div id="runningHeader" class="running-header" hidden>
      <span id="runningHeaderTag" class="running-header-mark">${escapeHtml(bookTitle)}</span>
      <span>•</span>
      <span id="runningHeaderTitle">${escapeHtml(bookSubtitle)}</span>
    </div>

    <main id="bookStage" class="book-stage">
      <div class="page-canvas">
        <article id="activePageArticle" class="page"></article>
      </div>
    </main>

    <footer class="bottom-nav-dock">
      <button id="btnPrevPage" class="nav-btn" onclick="window.prevPage()">$prevLabel</button>
      <span id="pageCounter" class="page-counter">1 / ${pages.size}</span>
      <button id="btnNextPage" class="nav-btn" onclick="window.nextPage()">$nextLabel</button>
    </footer>
  </div>

  <div id="searchPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>$searchHeaderTitle</h2>
        <button id="searchClose" class="icon-btn" aria-label="إغلاق البحث">✕</button>
      </div>
      <div class="search-bar-wrap">
        <input id="searchInput" type="text" placeholder="$searchPlaceholder" autocomplete="off"/>
        <button id="searchClearBtn" class="search-clear-btn" hidden aria-label="مسح">مسح</button>
      </div>
      <div class="search-status">
        <span id="searchStatus">$searchEmptyPrompt</span>
      </div>
      <div id="searchResults" class="search-results-list"></div>
    </div>
  </div>

  <div id="tocPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>$tocHeaderTitle</h2>
        <button id="tocClose" class="icon-btn" aria-label="إغلاق الفهرس">✕</button>
      </div>
      <div id="tocPanelList" class="toc-list"></div>
    </div>
  </div>

  <script>
    const STORAGE_PREFIX = "maktabati_conv_${storageSlug}_";
    const BOOK_DATA = {
      title: ${JSONObject.quote(bookTitle)},
      subtitle: ${JSONObject.quote(bookSubtitle)},
      author: ${JSONObject.quote(bookAuthor)},
      totalPages: ${pages.size},
      pages: ${pagesJsonArray}
    };

    let currentPageIndex = 0;
    let savedBookmarks = [];
    try {
      const rawBm = localStorage.getItem(STORAGE_PREFIX + "bookmarks");
      if (rawBm) savedBookmarks = JSON.parse(rawBm) || [];
    } catch (e) {}

    const activePageArticle = document.getElementById("activePageArticle");
    const pageCounter = document.getElementById("pageCounter");
    const btnPrevPage = document.getElementById("btnPrevPage");
    const btnNextPage = document.getElementById("btnNextPage");
    const runningHeader = document.getElementById("runningHeader");
    const runningHeaderTag = document.getElementById("runningHeaderTag");
    const runningHeaderTitle = document.getElementById("runningHeaderTitle");
    const bookmarkBtn = document.getElementById("bookmarkBtn");
    const bookStage = document.getElementById("bookStage");

    const searchPanel = document.getElementById("searchPanel");
    const searchOpen = document.getElementById("searchOpen");
    const searchClose = document.getElementById("searchClose");
    const searchInput = document.getElementById("searchInput");
    const searchClearBtn = document.getElementById("searchClearBtn");
    const searchStatus = document.getElementById("searchStatus");
    const searchResults = document.getElementById("searchResults");

    const tocPanel = document.getElementById("tocPanel");
    const tocBtn = document.getElementById("tocBtn");
    const tocClose = document.getElementById("tocClose");
    const tocPanelList = document.getElementById("tocPanelList");

    function renderPage(index, highlightQuery) {
      if (index < 0) index = 0;
      if (index >= BOOK_DATA.totalPages) index = BOOK_DATA.totalPages - 1;
      currentPageIndex = index;

      const page = BOOK_DATA.pages[index];
      activePageArticle.innerHTML = page.html;

      pageCounter.textContent = (index + 1) + " / " + BOOK_DATA.totalPages;
      btnPrevPage.disabled = (index === 0);
      btnNextPage.disabled = (index === BOOK_DATA.totalPages - 1);

      if (page.type === "cover") {
        runningHeader.hidden = true;
      } else {
        runningHeader.hidden = false;
        runningHeaderTag.textContent = page.tag || BOOK_DATA.title;
        runningHeaderTitle.textContent = page.title || "";
      }

      if (savedBookmarks.includes(index)) {
        bookmarkBtn.classList.add("bookmarked");
      } else {
        bookmarkBtn.classList.remove("bookmarked");
      }

      bookStage.scrollTop = 0;
      restorePageInputs();
      updateReaderNameInDom();

      if (highlightQuery) {
        applyCalmPageSearchHighlight(highlightQuery);
      }

      try {
        localStorage.setItem(STORAGE_PREFIX + "last_page", String(index));
      } catch (e) {}

      if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
        window.MaktabatiBridge.onPageChanged(index + 1, BOOK_DATA.totalPages);
      }
    }

    window.goToPage = function(idx) {
      renderPage(idx);
    };

    window.nextPage = function() {
      if (currentPageIndex < BOOK_DATA.totalPages - 1) {
        renderPage(currentPageIndex + 1);
      }
    };

    window.prevPage = function() {
      if (currentPageIndex > 0) {
        renderPage(currentPageIndex - 1);
      }
    };

    document.addEventListener("touchmove", function(e) {
      if (e.touches && e.touches.length > 1) {
        e.preventDefault();
      }
    }, { passive: false });

    window.__maktabatiSetFluidZoom = function(zoomPct, smooth) {
      const pct = Math.max(75, Math.min(200, Number(zoomPct) || 100));
      const scale = pct / 100;
      const root = document.documentElement;
      root.style.setProperty("--reader-font", Math.round(21 * scale) + "px");
      root.style.setProperty("--reader-font-size", pct + "%");
      root.style.setProperty("--font-scale", String(scale));
      try {
        localStorage.setItem("maktabati_fluid_zoom_pct", String(pct));
      } catch (e) {}
    };

    bookmarkBtn.addEventListener("click", function() {
      const idx = currentPageIndex;
      if (savedBookmarks.includes(idx)) {
        savedBookmarks = savedBookmarks.filter(i => i !== idx);
      } else {
        savedBookmarks.push(idx);
      }
      try {
        localStorage.setItem(STORAGE_PREFIX + "bookmarks", JSON.stringify(savedBookmarks));
      } catch (e) {}
      renderPage(currentPageIndex);
    });

    tocBtn.addEventListener("click", function() {
      buildTocPanel();
      tocPanel.classList.add("open");
    });
    tocClose.addEventListener("click", function() {
      tocPanel.classList.remove("open");
    });
    tocPanel.addEventListener("click", function(e) {
      if (e.target === tocPanel) tocPanel.classList.remove("open");
    });

    function buildTocPanel() {
      let html = "";
      BOOK_DATA.pages.forEach((p, idx) => {
        const isPart = (p.type === "opener");
        const cls = isPart ? "toc-item toc-part" : "toc-item";
        html += `<div class="${'$'}{cls}" onclick="window.goToPage(${'$'}{idx}); tocPanel.classList.remove('open');">
          <span>${'$'}{p.tag ? p.tag + ' • ' : ''}${'$'}{p.title}</span>
          <small>${'$'}{idx + 1}</small>
        </div>`;
      });
      tocPanelList.innerHTML = html;
    }

    searchOpen.addEventListener("click", function() {
      searchPanel.classList.add("open");
      setTimeout(() => searchInput.focus(), 80);
    });
    searchClose.addEventListener("click", function() {
      searchPanel.classList.remove("open");
    });
    searchPanel.addEventListener("click", function(e) {
      if (e.target === searchPanel) searchPanel.classList.remove("open");
    });
    searchClearBtn.addEventListener("click", function() {
      searchInput.value = "";
      searchClearBtn.hidden = true;
      executeBookSearch();
      searchInput.focus();
    });

    function normalizeArabicWithMap(text) {
      if (!text) return { normalized: "", map: [] };
      const tashkeelRegex = /[\u064B-\u065F\u0670\u06D6-\u06EDـ]/;
      let normalized = "";
      const map = [];
      for (let i = 0; i < text.length; i++) {
        const ch = text[i];
        if (tashkeelRegex.test(ch)) continue;
        let c = ch;
        if (c === 'أ' || c === 'إ' || c === 'آ') c = 'ا';
        else if (c === 'ى') c = 'ي';
        else if (c === 'ة') c = 'ه';
        normalized += c.toLowerCase();
        map.push(i);
      }
      return { normalized, map };
    }

    function extractPagePlainText(html) {
      const tmp = document.createElement("div");
      tmp.innerHTML = html;
      return tmp.textContent || tmp.innerText || "";
    }

    searchInput.addEventListener("input", function() {
      searchClearBtn.hidden = (searchInput.value.trim().length === 0);
      executeBookSearch();
    });

    function executeBookSearch() {
      const rawQuery = searchInput.value.trim();
      if (!rawQuery || rawQuery.length < 2) {
        searchStatus.textContent = "$searchEmptyPrompt";
        searchResults.innerHTML = "";
        return;
      }

      const qNorm = normalizeArabicWithMap(rawQuery).normalized;
      const hits = [];

      for (let i = 0; i < BOOK_DATA.pages.length; i++) {
        const page = BOOK_DATA.pages[i];
        const plain = extractPagePlainText(page.html);
        const { normalized, map } = normalizeArabicWithMap(plain);

        let searchPos = 0;
        let matchCount = 0;

        while (true) {
          const matchIdx = normalized.indexOf(qNorm, searchPos);
          if (matchIdx === -1) break;
          matchCount++;

          if (matchCount <= 2) {
            const startOrig = map[matchIdx] || 0;
            const endNorm = matchIdx + qNorm.length - 1;
            const endOrig = (map[endNorm] !== undefined ? map[endNorm] + 1 : startOrig + rawQuery.length);

            const snippetStart = Math.max(0, startOrig - 40);
            const snippetEnd = Math.min(plain.length, endOrig + 40);

            const prefix = (snippetStart > 0 ? "..." : "") + plain.substring(snippetStart, startOrig);
            const matchText = plain.substring(startOrig, endOrig);
            const suffix = plain.substring(endOrig, snippetEnd) + (snippetEnd < plain.length ? "..." : "");

            hits.push({
              pageIndex: i,
              pageNumber: i + 1,
              title: page.title || ("Page " + (i + 1)),
              tag: page.tag || "",
              snippet: prefix + `<mark class="search-hit-mark">${'$'}{matchText}</mark>` + suffix
            });
          }

          searchPos = matchIdx + Math.max(1, qNorm.length);
          if (hits.length >= 40) break;
        }
        if (hits.length >= 40) break;
      }

      if (hits.length === 0) {
        searchStatus.textContent = `لا توجد نتائج مطابقة لـ «${'$'}{rawQuery}»`;
        searchResults.innerHTML = "";
      } else {
        searchStatus.textContent = `تم العثور على ${'$'}{hits.length} نتيجة:`;
        let html = "";
        hits.forEach(hit => {
          html += `<div class="search-result" onclick="selectSearchResult(${'$'}{hit.pageIndex}, '${'$'}{escapeQuery(rawQuery)}')">
            <div class="search-result-head">
              <strong>${'$'}{hit.tag ? hit.tag + ' • ' : ''}${'$'}{hit.title}</strong>
              <span class="search-result-page">${'$'}{hit.pageNumber}</span>
            </div>
            <small>${'$'}{hit.snippet}</small>
          </div>`;
        });
        searchResults.innerHTML = html;
      }
    }

    function escapeQuery(str) {
      return str.replace(/['"\\]/g, "");
    }

    window.selectSearchResult = function(pageIndex, query) {
      searchPanel.classList.remove("open");
      renderPage(pageIndex, query);
    };

    function applyCalmPageSearchHighlight(query) {
      if (!query || query.length < 2) return;
      const { normalized: qNorm } = normalizeArabicWithMap(query);

      function traverse(node) {
        if (node.nodeType === 3) {
          const text = node.nodeValue;
          const { normalized, map } = normalizeArabicWithMap(text);
          const idx = normalized.indexOf(qNorm);
          if (idx !== -1) {
            const startOrig = map[idx];
            const endNorm = idx + qNorm.length - 1;
            const endOrig = map[endNorm] !== undefined ? map[endNorm] + 1 : startOrig + query.length;

            const before = text.substring(0, startOrig);
            const match = text.substring(startOrig, endOrig);
            const after = text.substring(endOrig);

            const span = document.createElement("span");
            if (before) span.appendChild(document.createTextNode(before));

            const mark = document.createElement("mark");
            mark.className = "page-search-highlight";
            mark.textContent = match;
            span.appendChild(mark);

            if (after) span.appendChild(document.createTextNode(after));

            node.parentNode.replaceChild(span, node);
            mark.scrollIntoView({ behavior: "smooth", block: "center" });
            return true;
          }
        } else if (node.nodeType === 1 && node.childNodes && !["SCRIPT", "STYLE", "BUTTON"].includes(node.tagName)) {
          for (let i = 0; i < node.childNodes.length; i++) {
            if (traverse(node.childNodes[i])) return true;
          }
        }
        return false;
      }

      traverse(activePageArticle);
    }

    window.saveField = function(id, val) {
      try {
        localStorage.setItem(STORAGE_PREFIX + "f_" + id, val);
      } catch (e) {}
    };

    window.saveReaderName = function(name) {
      try {
        localStorage.setItem(STORAGE_PREFIX + "reader_name", name.trim());
        updateReaderNameInDom();
      } catch (e) {}
    };

    window.alertSavedName = function() {
      updateReaderNameInDom();
    };

    function updateReaderNameInDom() {
      const savedName = localStorage.getItem(STORAGE_PREFIX + "reader_name") || "";
      const slots = document.querySelectorAll(".reader-name-slot, [data-reader-name]");
      slots.forEach(slot => {
        slot.textContent = savedName ? " «" + savedName + "»" : "";
      });
      const nameInput = document.getElementById("readerNameInput");
      if (nameInput && savedName) {
        nameInput.value = savedName;
      }
    }

    function restorePageInputs() {
      const inputs = activePageArticle.querySelectorAll("input, textarea");
      inputs.forEach(el => {
        if (!el.id) return;
        const val = localStorage.getItem(STORAGE_PREFIX + "f_" + el.id);
        if (val !== null) el.value = val;
      });
    }

    document.addEventListener("DOMContentLoaded", function() {
      try {
        const savedZoom = localStorage.getItem("maktabati_fluid_zoom_pct");
        if (savedZoom) window.__maktabatiSetFluidZoom(savedZoom, false);
      } catch (e) {}

      let startPage = 0;
      try {
        const lp = localStorage.getItem(STORAGE_PREFIX + "last_page");
        if (lp !== null) startPage = parseInt(lp, 10) || 0;
      } catch (e) {}

      renderPage(startPage);
    });
  </script>
</body>
</html>"""
    }

    private fun buildLayoutVariantCss(theme: BookVisualTheme): String {
        val commonComponents = """
.poetry-stanza {
  margin: 16px 0;
  padding: 14px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-secondary));
  border: 1px dashed var(--accent-gold);
  text-align: center;
  font-family: var(--font-quotes);
}
.poetry-shatr {
  font-size: 1.06em;
  color: var(--accent-rose-dark);
  line-height: 1.95;
}
.poetry-ornament {
  color: var(--accent-gold);
  font-size: 0.78em;
  margin: 2px 0;
}
.action-callout {
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-pearl-gold));
  border-inline-start: 4px solid var(--accent-rose);
}
.academic-callout {
  background: var(--bg-soft-sage);
  border-inline-start: 4px solid var(--accent-sage-dark);
  border-radius: 8px;
}
        """.trimIndent()

        val styleSpecific = when (theme.layoutStyle) {
            PublishingLayoutStyle.HERITAGE_ORNATE -> """
.page-canvas {
  border: 2px solid var(--accent-gold);
  outline: 1px solid var(--accent-gold-soft);
  outline-offset: -7px;
  box-shadow: inset 0 0 28px rgba(180,123,38,0.06), var(--shadow-card);
}
.page h1 {
  text-align: center;
  border-bottom: 1.5px double var(--accent-gold-soft);
  padding-bottom: 8px;
}
            """.trimIndent()

            PublishingLayoutStyle.CLASSIC_BOOK_PRESS -> """
.page-canvas {
  border-radius: 10px;
  box-shadow: inset 0 0 36px rgba(124,77,43,0.06), 0 12px 30px rgba(30,20,10,0.08);
}
.running-header {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-bottom: 1.5px double var(--paper-edge);
}
.running-header-mark {
  color: var(--accent-rose-dark);
}
            """.trimIndent()

            PublishingLayoutStyle.SOFT_BOTANICAL -> """
.page-canvas {
  border-radius: 22px;
  border: 1.5px solid var(--paper-edge);
}
            """.trimIndent()

            PublishingLayoutStyle.DAWAH_MOTIVATIONAL -> """
.letter-envelope-header {
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  border-color: var(--accent-gold);
}
.letter-envelope-title {
  color: #FFFFFF;
}
.wisdom-pearl {
  border-inline-start-width: 5px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}
            """.trimIndent()

            PublishingLayoutStyle.NARRATIVE_STORY -> """
.page p {
  text-indent: 1.1em;
  line-height: 2.12;
}
.page h1 {
  text-align: center;
}
            """.trimIndent()

            PublishingLayoutStyle.ACADEMIC_STRUCTURED -> """
.page-canvas {
  border-radius: 8px;
  border-top: 4px solid var(--accent-rose);
}
.page h1 {
  border-inline-start: 4px solid var(--accent-rose);
  padding-inline-start: 10px;
}
            """.trimIndent()

            PublishingLayoutStyle.EDITORIAL_MAGAZINE -> """
.page-canvas {
  border-radius: 12px;
  border-top: 5px solid var(--accent-rose);
}
.page h1 {
  letter-spacing: 0.2px;
  border-bottom: 2px solid var(--accent-rose);
  padding-bottom: 6px;
}
            """.trimIndent()

            PublishingLayoutStyle.PURE_MINIMAL -> """
.app {
  background: var(--bg-primary);
}
.page-canvas {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}
.running-header {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-bottom: 1px solid var(--paper-edge);
}
.running-header-mark {
  color: var(--text-primary);
}
            """.trimIndent()
        }

        return "$commonComponents\n$styleSpecific"
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {
            }
        }
        return uri.lastPathSegment
    }
}
