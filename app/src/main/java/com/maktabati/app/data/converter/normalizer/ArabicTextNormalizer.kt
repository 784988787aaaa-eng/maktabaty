package com.maktabati.app.data.converter.normalizer

import com.maktabati.app.data.converter.theme.BookVisualTheme
import java.text.Normalizer

/**
 * Arabic Presentation Forms Normalization & 100% Tashkeel/Hamza Fidelity Engine,
 * plus language/direction detection, content-based theme recommendation, and title/author extraction.
 */
object ArabicTextNormalizer {

    private val ARABIC_CHAR_REGEX = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]")
    private val LATIN_CHAR_REGEX = Regex("[A-Za-z]")
    private val DIACRITICS_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06EDـ]")

    val AUTHOR_LINE_REGEX = Regex(
        "^(?:تأليف|بقلم|المؤلف|إعداد|الكاتب|كتبه|جمع وترتيب|author|by|written by)\\s*[:：\\-]?\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    val TITLE_LINE_REGEX = Regex(
        "^(?:عنوان الكتاب|الكتاب|title)\\s*[:：\\-]\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    val SUBTITLE_LINE_REGEX = Regex(
        "^(?:العنوان الفرعي|subtitle)\\s*[:：\\-]\\s*(.+)$",
        RegexOption.IGNORE_CASE
    )

    private val CHAPTER_HEADING_PREFIXES = listOf(
        "الباب ", "الفصل ", "الرسالة ", "المبحث ", "الجزء ", "القسم ",
        "مقدمة", "المقدمة", "تمهيد", "التمهيد", "الإهداء", "إهداء",
        "الخاتمة", "خاتمة", "ملحق", "الملحق",
        "chapter ", "part ", "section ", "introduction", "preface", "prologue", "epilogue", "conclusion"
    )

    fun normalizeArabicFidelity(input: String): String {
        if (input.isEmpty()) return ""

        val sb = StringBuilder(input.length)
        for (ch in input) {
            val code = ch.code
            when {
                code == 0xFD3E || code == 0xFD3F || code == 0xFDFA || code == 0xFDFB -> {
                    sb.append(ch)
                }
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

    fun stripArabicDiacriticsForMatching(s: String): String {
        return s.replace(DIACRITICS_REGEX, "").trim()
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

    fun isChapterHeadingLine(line: String): Boolean {
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

    fun isSubheadingLine(line: String): Boolean {
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

    fun extractTitleSubtitleAndAuthor(
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

        val isRtl = detectLanguageAndDirection(rawText).first
        val finalTitle = detectedTitle?.takeIf { it.isNotBlank() } ?: fallbackTitle
        val finalSubtitle = detectedSubtitle.ifBlank {
            if (isRtl) {
                "طبعة رقمية تفاعلية منسقة • منصة مكتبتي"
            } else {
                "Interactive Digital Edition • Maktabati"
            }
        }
        val finalAuthor = detectedAuthor?.takeIf { it.isNotBlank() } ?: (
            if (isRtl) "إصدارات مكتبتي الفاخرة" else "Maktabati Publishing"
        )

        return Triple(finalTitle, finalSubtitle, finalAuthor)
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
}
