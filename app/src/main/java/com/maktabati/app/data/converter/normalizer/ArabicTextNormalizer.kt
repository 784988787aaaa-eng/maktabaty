package com.maktabati.app.data.converter.normalizer

import com.maktabati.app.data.converter.theme.BookVisualTheme
import java.text.Normalizer

/**
 * Arabic Presentation Forms Normalization & 100% Tashkeel/Hamza Fidelity Engine,
 * plus zero-allocation word counting, language/direction detection, content-based theme recommendation,
 * and streaming title/author extraction.
 */
object ArabicTextNormalizer {

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

    private val WHATSAPP_HEADER_REGEX_1 = Regex(
        "^\\[?\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?:ص|م|AM|PM|am|pm)?\\s*[،,\\-]\\s*\\d{1,4}[/\\-.]\\d{1,2}[/\\-.]\\d{1,4}\\]?\\s*[^:]{1,35}:\\s*"
    )

    private val WHATSAPP_HEADER_REGEX_2 = Regex(
        "^\\[?\\d{1,4}[/\\-.]\\d{1,2}[/\\-.]\\d{1,4}\\s*[،,]?\\s*\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?:ص|م|AM|PM|am|pm)?\\]?\\s*(?:-\\s*)?[^:]{1,35}:\\s*"
    )

    private val FORWARDED_LINE_REGEX = Regex(
        "^(?:[\\s*•✦❖➡️↪️]*)(?:تمت إعادة التوجيه|رسالة محولة|رسالة معاد توجيهها|forwarded|forwarded message)(?:[\\s*•✦❖:]*)$",
        RegexOption.IGNORE_CASE
    )

    private val SEPARATOR_LINE_REGEX = Regex(
        "^[\\s=═\\-─━_~*•·.…✦✧❖❀🌸💫🌟✨〰️]{4,}$"
    )

    private val PURE_DOTS_OR_NOISE_REGEX = Regex(
        "^[\\s.*#_~`^|•·…\\-—=═─━✦✧❖❀🌸💫🤔🌟✨〰️!:؛،,?؟]+$"
    )

    private val MARKDOWN_AND_NOISE_CHARS_REGEX = Regex("[*#_~`^|═─━]+")
    private val EMOJI_SURROGATE_REGEX = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF\\uFE0F]+")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

    private val STANDALONE_WHATSAPP_BOLD_HEADING_REGEX = Regex("^\\*{1,2}([^*]{3,85})\\*{1,2}$")

    private val INLINE_PEARL_Check_PREFIXES = listOf(
        "خلاصة", "همسة", "فائدة", "درة", "وصية", "قاعدة", "إشراقة", "تأمل", "تقول ", "حكمة",
        "جرعة إيمانية", "محاسبة", "واجب عملي", "خطوة عملية", "تطبيق عملي", "تدريب",
        "موقف وعبرة", "موقف", "عبرة", "قصة قصيرة", "إضاءة", "قاعدة ذهبية",
        "تعريف", "مفهوم", "مثال تطبيقي", "مثال", "ملاحظة هامة", "ملاحظة", "نتيجة",
        "note:", "key takeaway:", "summary:", "tip:", "reflection:", "definition:", "example:", "action step:"
    )

    fun containsLetterOrDigit(text: String): Boolean {
        for (i in 0 until text.length) {
            if (Character.isLetterOrDigit(text[i].code)) return true
        }
        return false
    }

    fun isOrnamentalDividerLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed == "* * *" || trimmed == "***" || trimmed == "---" || trimmed == "___") return true
        if (containsLetterOrDigit(trimmed)) return false
        if (SEPARATOR_LINE_REGEX.matches(trimmed)) {
            // Distinguish pure emoji lines (like 💫🤔💫) from structural divider lines (like ══════════ or *....)
            val withoutEmojis = trimmed.replace(EMOJI_SURROGATE_REGEX, "").trim()
            return withoutEmojis.length >= 3
        }
        return false
    }

    fun stripFormattingAndNoiseFromHeading(raw: String): String {
        if (raw.isBlank()) return ""
        var cleaned = raw.trim()
        cleaned = WHATSAPP_HEADER_REGEX_1.replace(cleaned, "")
        cleaned = WHATSAPP_HEADER_REGEX_2.replace(cleaned, "")
        if (FORWARDED_LINE_REGEX.matches(cleaned)) return ""
        cleaned = cleaned
            .replace(MARKDOWN_AND_NOISE_CHARS_REGEX, " ")
            .replace(EMOJI_SURROGATE_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
            .trim('*', '#', '-', '_', '—', '–', '•', '✦', '✧', '❖', '❀', '.', '…', ' ')
        if (cleaned.isEmpty() || PURE_DOTS_OR_NOISE_REGEX.matches(cleaned) || !containsLetterOrDigit(cleaned)) {
            return ""
        }
        return cleaned
    }

    /**
     * Cleans WhatsApp/Telegram chat headers, forwarded markers, pure emoji noise lines,
     * and normalizes long symbol separator lines into canonical `* * *` dividers.
     */
    fun normalizeRawText(raw: String): String {
        val fidelity = normalizeArabicFidelity(raw)
        return fidelity
    }

    /**
     * Single-pass Arabic fidelity normalization that avoids creating multiple full-book intermediate strings.
     * Preserves 100% of Quranic symbols, tashkeel, and hamzas while normalizing isolated/contextual PDF/Word
     * presentation forms, stripping WhatsApp/Telegram chat metadata, and unifying line endings in one pass.
     */
    fun normalizeArabicFidelity(input: String): String {
        val len = input.length
        if (len == 0) return ""

        // Fast trim bounds
        var start = 0
        while (start < len && (input[start].isWhitespace() || input[start] == '\u0000')) {
            start++
        }
        var end = len - 1
        while (end >= start && (input[end].isWhitespace() || input[end].code == 0)) {
            end--
        }
        if (start > end) return ""

        val rawNormalized = StringBuilder(end - start + 1)
        val charBuf = CharArray(1)
        var i = start
        while (i <= end) {
            val ch = input[i]
            val code = ch.code
            when {
                ch == '\u0000' -> {
                    // Skip null bytes
                }
                ch == '\r' -> {
                    if (i + 1 <= end && input[i + 1] == '\n') {
                        i++
                    }
                    rawNormalized.append('\n')
                }
                code == 0xFD3E || code == 0xFD3F || code == 0xFDFA || code == 0xFDFB -> {
                    rawNormalized.append(ch)
                }
                code in 0xFB50..0xFDFF || code in 0xFE70..0xFEFF -> {
                    charBuf[0] = ch
                    rawNormalized.append(Normalizer.normalize(String(charBuf), Normalizer.Form.NFKC))
                }
                else -> {
                    rawNormalized.append(ch)
                }
            }
            i++
        }

        // Line-by-line WhatsApp/Telegram & separator sanitation
        val finalSb = StringBuilder(rawNormalized.length)
        var hasSeenContentLine = false
        var lastWasDivider = false

        for (rawLine in rawNormalized.lineSequence()) {
            var line = rawLine.trim()
            if (line.isEmpty()) {
                if (hasSeenContentLine && finalSb.isNotEmpty() && finalSb.last() != '\n') {
                    finalSb.append('\n')
                }
                continue
            }

            line = WHATSAPP_HEADER_REGEX_1.replace(line, "").trim()
            line = WHATSAPP_HEADER_REGEX_2.replace(line, "").trim()
            if (line.isEmpty() || FORWARDED_LINE_REGEX.matches(line)) {
                continue
            }

            // Skip standalone pure-emoji lines (e.g. "💫🤔💫") so they don't become titles or noisy paragraphs
            if (!containsLetterOrDigit(line)) {
                if (isOrnamentalDividerLine(line)) {
                    // Ignore leading dividers or dots (like "*....") right at the top of the message
                    if (hasSeenContentLine && !lastWasDivider) {
                        val withoutDots = line.trim('*', '.', '…', ' ')
                        // Only turn genuine horizontal rules (════, ----, * * *) into ornamental dividers;
                        // skip pure dot spacers like "*...." or "....."
                        if (withoutDots.isNotEmpty() || line == "* * *" || line == "***") {
                            if (finalSb.isNotEmpty()) finalSb.append("\n\n")
                            finalSb.append("* * *")
                            lastWasDivider = true
                        }
                    }
                }
                continue
            }

            if (finalSb.isNotEmpty()) {
                finalSb.append('\n')
            }
            finalSb.append(line)
            hasSeenContentLine = true
            lastWasDivider = false
        }

        return finalSb.toString().trim()
    }

    /**
     * Counts words in O(N) time and O(1) memory without allocating any arrays or substrings.
     */
    fun countWordsFast(text: String): Int {
        var count = 0
        var inWord = false
        for (i in 0 until text.length) {
            val ch = text[i]
            if (ch.isWhitespace()) {
                inWord = false
            } else if (!inWord) {
                inWord = true
                count++
            }
        }
        return count
    }

    /**
     * Extracts a clean preview excerpt using a lazy line sequence in O(1) memory,
     * stripped of any WhatsApp/Markdown formatting symbols.
     */
    fun extractPreviewExcerptFast(text: String): String {
        return text.lineSequence()
            .map { stripFormattingAndNoiseFromHeading(it) }
            .firstOrNull {
                it.length > 25 &&
                    !it.startsWith("تأليف") &&
                    !it.startsWith("بقلم") &&
                    !it.startsWith("عنوان الكتاب")
            }
            ?.take(150)
            ?: "بسم الله الرحمن الرحيم.. تزداد الكلمات جمالاً وأثراً حين تُصاغ في قوالب تليق بمعانيها وتُريح عين القارئ."
    }

    fun stripArabicDiacriticsForMatching(s: String): String {
        if (s.isEmpty()) return ""
        val sb = StringBuilder(s.length)
        for (i in 0 until s.length) {
            val c = s[i]
            val code = c.code
            val isDiacritic = code in 0x064B..0x065F ||
                code == 0x0670 ||
                code in 0x06D6..0x06ED ||
                c == 'ـ'
            if (!isDiacritic) {
                sb.append(c)
            }
        }
        return sb.toString().trim()
    }

    /**
     * Detects language and direction from a 4,000-character sample in a single character loop
     * without running Regex.findAll across multi-megabyte texts.
     */
    fun detectLanguageAndDirection(text: String): Pair<Boolean, String> {
        val limit = minOf(text.length, 4000)
        var arabicMatches = 0
        var latinMatches = 0
        for (i in 0 until limit) {
            val code = text[i].code
            if (code in 0x0600..0x06FF || code in 0x0750..0x077F || code in 0x08A0..0x08FF) {
                arabicMatches++
            } else if (code in 0x0041..0x005A || code in 0x0061..0x007A) {
                latinMatches++
            }
        }
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

        val stripped = stripFormattingAndNoiseFromHeading(trimmed)
        if (stripped.isEmpty()) return false
        val unvocalized = stripArabicDiacriticsForMatching(stripped).lowercase()

        return CHAPTER_HEADING_PREFIXES.any { prefix ->
            unvocalized.startsWith(prefix) || unvocalized == prefix.trim()
        }
    }

    fun isSubheadingLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || isOrnamentalDividerLine(trimmed)) return false
        if (trimmed.startsWith("### ")) return true

        val stripped = stripFormattingAndNoiseFromHeading(trimmed)
        if (stripped.isEmpty()) return false

        val unvocalized = stripArabicDiacriticsForMatching(stripped).lowercase()
        val isPearlWithInlineBody = INLINE_PEARL_Check_PREFIXES.any { prefix ->
            unvocalized.startsWith(prefix) && stripped.contains(":") && stripped.substringAfter(":").trim().length > 4
        }
        if (isPearlWithInlineBody) return false

        // Standalone WhatsApp/Markdown bold line like `*أثر الاستغفار في القلوب*`
        val boldMatch = STANDALONE_WHATSAPP_BOLD_HEADING_REGEX.matchEntire(trimmed)
        if (boldMatch != null) {
            val innerClean = stripFormattingAndNoiseFromHeading(boldMatch.groupValues[1])
            if (innerClean.length in 3..80 &&
                !innerClean.endsWith(".") &&
                !innerClean.endsWith("؟") &&
                !innerClean.contains("قال تعالى") &&
                !innerClean.contains("قال رسول") &&
                !isChapterHeadingLine(innerClean)
            ) {
                return true
            }
        }

        if (stripped.length in 3..70 &&
            (trimmed.endsWith(":") || trimmed.endsWith("：") || stripped.endsWith(":") || stripped.endsWith("：")) &&
            !stripped.contains("قال تعالى") &&
            !stripped.contains("قال رسول")
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
        val topLines = rawText.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !isOrnamentalDividerLine(it) }
            .take(18)
            .toList()

        var detectedTitle = explicitTitle?.let { stripFormattingAndNoiseFromHeading(it) }?.takeIf { it.isNotEmpty() }
        var detectedSubtitle = ""
        var detectedAuthor = explicitAuthor?.let { stripFormattingAndNoiseFromHeading(it) }?.takeIf { it.isNotEmpty() }

        for ((idx, line) in topLines.withIndex()) {
            val cleanLine = stripFormattingAndNoiseFromHeading(line)
            if (cleanLine.isEmpty()) continue

            val titleMatch = TITLE_LINE_REGEX.find(cleanLine)
            if (titleMatch != null && detectedTitle.isNullOrBlank()) {
                detectedTitle = stripFormattingAndNoiseFromHeading(titleMatch.groupValues[1])
                continue
            }

            val subMatch = SUBTITLE_LINE_REGEX.find(cleanLine)
            if (subMatch != null && detectedSubtitle.isBlank()) {
                detectedSubtitle = stripFormattingAndNoiseFromHeading(subMatch.groupValues[1])
                continue
            }

            val authorMatch = AUTHOR_LINE_REGEX.find(cleanLine)
            if (authorMatch != null && detectedAuthor.isNullOrBlank()) {
                detectedAuthor = stripFormattingAndNoiseFromHeading(authorMatch.groupValues[1])
                continue
            }

            if (idx <= 2 && detectedTitle.isNullOrBlank() && cleanLine.length in 2..95) {
                detectedTitle = cleanLine
            }
        }

        val finalTitle = detectedTitle?.takeIf { it.isNotBlank() }
            ?: stripFormattingAndNoiseFromHeading(fallbackTitle).ifBlank { "كتاب تفاعلي" }
        val finalSubtitle = stripFormattingAndNoiseFromHeading(detectedSubtitle)
            .takeIf { !it.equals(finalTitle, ignoreCase = true) }
            .orEmpty()
        val finalAuthor = detectedAuthor?.let { stripFormattingAndNoiseFromHeading(it) }.orEmpty()

        return Triple(finalTitle, finalSubtitle, finalAuthor)
    }

    fun suggestThemeForContent(title: String, body: String, isRtl: Boolean): BookVisualTheme {
        if (!isRtl) return BookVisualTheme.ROYAL_ACADEMIC
        val bodySample = if (body.length > 2000) body.substring(0, 2000) else body
        val sample = stripArabicDiacriticsForMatching("$title $bodySample").lowercase()
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
