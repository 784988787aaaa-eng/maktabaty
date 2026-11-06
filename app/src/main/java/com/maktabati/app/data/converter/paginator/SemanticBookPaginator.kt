package com.maktabati.app.data.converter.paginator

import com.maktabati.app.data.converter.model.ConvertedPageItem
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.converter.theme.BookVisualTheme

/**
 * Splits extracted book text into balanced chapters & pages (85–125 words per page)
 * using streaming lineSequence() processing that gracefully handles both `\n\n` and single-`\n`
 * large Arabic/English text files without regex backtracking or OutOfMemoryError on Android 8+.
 */
object SemanticBookPaginator {

    private val COLON_DASH_SPLIT_REGEX = Regex("[:：\\-—]")
    private val BRACKET_SOURCE_REGEX = Regex("\\[([^\\]]+)\\]")
    private val CURLY_VERSE_REGEX = Regex("\\{\\s*([^}]+)\\s*\\}")
    private val DOUBLE_PAREN_REGEX = Regex("\\(\\(\\s*([^)]+)\\s*\\)\\)")
    private val INLINE_QURAN_REGEX = Regex("﴿([^﴾]+)﴾")
    private val INLINE_HADITH_REGEX = Regex("«([^»]+)»")
    private val INLINE_NUM_BADGE_REGEX = Regex("\\[([0-9٠-٩]{1,4})\\]")
    private val INLINE_BOLD_REGEX = Regex("\\*\\*([^*]+)\\*\\*")
    private val INLINE_WHATSAPP_BOLD_REGEX = Regex("(?<!\\*)\\*([^\\s*][^*]*?[^\\s*]|[^\\s*])\\*(?!\\*)")
    private val INLINE_WHATSAPP_ITALIC_REGEX = Regex("(?<![a-zA-Z0-9_])_([^\\s_][^_]*?[^\\s_])_(?![a-zA-Z0-9_])")
    private val STRAY_ASTERISK_REGEX = Regex("(?<=^|\\s)\\*+|\\*+(?=\\s|$|[.،؛:!؟])")

    private val PEARL_PREFIXES = listOf(
        "خلاصة", "همسة", "فائدة", "درة", "وصية", "قاعدة", "إشراقة", "تأمل", "تقول ", "حكمة",
        "جرعة إيمانية", "محاسبة", "واجب عملي", "خطوة عملية", "تطبيق عملي", "تدريب",
        "موقف وعبرة", "موقف", "عبرة", "قصة قصيرة", "إضاءة", "قاعدة ذهبية",
        "تعريف", "مفهوم", "مثال تطبيقي", "مثال", "ملاحظة هامة", "ملاحظة", "نتيجة",
        "note:", "key takeaway:", "summary:", "tip:", "reflection:", "definition:", "example:", "action step:"
    )

    private data class SemanticSection(
        val chapterTag: String,
        val chapterTitle: String,
        val paragraphs: MutableList<String> = mutableListOf()
    )

    fun buildSemanticPages(
        rawText: String,
        bookTitle: String,
        bookSubtitle: String,
        bookAuthor: String,
        theme: BookVisualTheme,
        isRtl: Boolean
    ): List<ConvertedPageItem> {
        val cleanBookTitle = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(bookTitle).ifBlank { bookTitle.trim() }
        val cleanBookSubtitle = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(bookSubtitle)
            .takeIf { !it.equals(cleanBookTitle, ignoreCase = true) }
            .orEmpty()
        val cleanBookAuthor = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(bookAuthor)

        val pages = mutableListOf<ConvertedPageItem>()

        pages.add(
            ConvertedPageItem(
                tag = if (isRtl) "الغلاف الملكي" else "Royal Cover",
                title = cleanBookTitle,
                type = "cover",
                html = LuxuryHtmlTemplateRenderer.buildCoverPageHtml(
                    title = cleanBookTitle,
                    subtitle = cleanBookSubtitle,
                    author = cleanBookAuthor,
                    theme = theme,
                    isRtl = isRtl
                )
            )
        )

        val normalizedRawText = ArabicTextNormalizer.normalizeArabicFidelity(rawText)
        val rawBlocks = extractBalancedBlocksStreaming(normalizedRawText)
        val hasExplicitChapters = rawBlocks.any { ArabicTextNormalizer.isChapterHeadingLine(it) }
        val normalizedBookTitle = ArabicTextNormalizer.stripArabicDiacriticsForMatching(cleanBookTitle).trim()

        val sections = mutableListOf<SemanticSection>()
        var currentChapterIndex = 0
        var activeSection = SemanticSection(
            chapterTag = "",
            chapterTitle = ""
        )

        for ((idx, block) in rawBlocks.withIndex()) {
            val clean = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(block)
            val normalizedClean = ArabicTextNormalizer.stripArabicDiacriticsForMatching(clean).trim()

            if (idx <= 2) {
                if (clean.equals(cleanBookTitle, ignoreCase = true) ||
                    (normalizedBookTitle.isNotEmpty() && normalizedClean.equals(normalizedBookTitle, ignoreCase = true)) ||
                    ArabicTextNormalizer.TITLE_LINE_REGEX.matches(clean) ||
                    ArabicTextNormalizer.SUBTITLE_LINE_REGEX.matches(clean) ||
                    ArabicTextNormalizer.AUTHOR_LINE_REGEX.matches(clean)
                ) {
                    continue
                }
            }

            if (ArabicTextNormalizer.isChapterHeadingLine(block)) {
                if (activeSection.paragraphs.isNotEmpty()) {
                    sections.add(activeSection)
                }
                currentChapterIndex++
                val parts = clean.split(COLON_DASH_SPLIT_REGEX, limit = 2).map { it.trim() }
                val hasDistinctTagAndTitle = parts.size == 2 &&
                    parts[0].isNotEmpty() &&
                    parts[0].length <= 36 &&
                    parts[1].isNotEmpty() &&
                    !parts[0].equals(parts[1], ignoreCase = true)
                val tag = if (hasDistinctTagAndTitle) parts[0] else ""
                val title = if (hasDistinctTagAndTitle) parts[1] else clean
                activeSection = SemanticSection(chapterTag = tag, chapterTitle = title)
            } else {
                val wordCount = ArabicTextNormalizer.countWordsFast(block)
                if (wordCount > 135) {
                    activeSection.paragraphs.addAll(splitLongParagraphBySentencesLinear(block, targetWords = 95))
                } else {
                    activeSection.paragraphs.add(block)
                }
            }
        }
        if (activeSection.paragraphs.isNotEmpty()) {
            sections.add(activeSection)
        }

        if (sections.isEmpty() && normalizedRawText.isNotBlank()) {
            val fallbackChunks = splitLongParagraphBySentencesLinear(normalizedRawText.trim(), targetWords = 95)
            sections.add(
                SemanticSection(
                    chapterTag = "",
                    chapterTitle = "",
                    paragraphs = fallbackChunks.toMutableList()
                )
            )
        }

        for (section in sections) {
            val currentPageBlocks = mutableListOf<String>()
            var currentPageWords = 0
            var sectionPageIndex = 0

            fun flushSectionPage() {
                if (currentPageBlocks.isEmpty()) return
                sectionPageIndex++
                val isFirstPageOfSection = (sectionPageIndex == 1)
                val pageHtmlBuilder = StringBuilder(1024)

                val normalizedSectionTitle = ArabicTextNormalizer
                    .stripArabicDiacriticsForMatching(section.chapterTitle)
                    .trim()
                val hasDistinctSectionTitle = section.chapterTitle.isNotBlank() &&
                    !section.chapterTitle.equals(cleanBookTitle, ignoreCase = true) &&
                    (normalizedBookTitle.isEmpty() || !normalizedSectionTitle.equals(normalizedBookTitle, ignoreCase = true))

                if (isFirstPageOfSection) {
                    if (hasExplicitChapters && section.chapterTag.isNotBlank() &&
                        !section.chapterTag.equals(section.chapterTitle, ignoreCase = true)
                    ) {
                        pageHtmlBuilder.append(
                            """
                            <div class="letter-envelope-header chapter-opener">
                              <span class="letter-seal">${theme.badgeEmoji}</span>
                              <span class="letter-envelope-title">${escapeHtml(section.chapterTag)}</span>
                            </div>
                            """.trimIndent()
                        ).append("\n")
                    }
                    if (hasDistinctSectionTitle) {
                        pageHtmlBuilder.append("<h1 class=\"chapter-opener\">${escapeHtml(section.chapterTitle)}</h1>\n")
                    }
                }

                var firstSubheadingOnPage = ""
                for (blk in currentPageBlocks) {
                    if (firstSubheadingOnPage.isEmpty() && ArabicTextNormalizer.isSubheadingLine(blk)) {
                        firstSubheadingOnPage = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(blk)
                            .removeSuffix(":")
                            .removeSuffix("：")
                            .trim()
                    }
                    val renderedBlock = formatSemanticParagraphToHtml(blk, isRtl)
                    if (renderedBlock.isNotBlank()) {
                        pageHtmlBuilder.append(renderedBlock).append("\n")
                    }
                }

                val effectivePageTitle = when {
                    !isFirstPageOfSection && firstSubheadingOnPage.isNotBlank() -> firstSubheadingOnPage
                    hasDistinctSectionTitle -> section.chapterTitle
                    firstSubheadingOnPage.isNotBlank() -> firstSubheadingOnPage
                    else -> cleanBookTitle
                }

                pages.add(
                    ConvertedPageItem(
                        tag = if (hasExplicitChapters) section.chapterTag else "",
                        title = effectivePageTitle,
                        type = if (isFirstPageOfSection && (hasExplicitChapters || hasDistinctSectionTitle)) "opener" else "reading",
                        html = pageHtmlBuilder.toString()
                    )
                )
                currentPageBlocks.clear()
                currentPageWords = 0
            }

            for (para in section.paragraphs) {
                val isDivider = ArabicTextNormalizer.isOrnamentalDividerLine(para)
                if (isDivider && currentPageBlocks.isEmpty()) {
                    // Avoid starting a brand-new page with an ornamental divider
                    continue
                }
                val pWords = if (isDivider) 4 else ArabicTextNormalizer.countWordsFast(para).coerceAtLeast(1)
                if (currentPageWords >= 50 && (currentPageWords + pWords) > 125) {
                    flushSectionPage()
                }
                currentPageBlocks.add(para)
                currentPageWords += pWords
                if (currentPageWords >= 100) {
                    flushSectionPage()
                }
            }
            flushSectionPage()
        }

        return pages
    }

    /**
     * Streaming block extractor using `lineSequence()` that handles both blank-line separated (`\n\n`)
     * and single-newline (`\n`) text files without merging an entire book into a single multi-megabyte line.
     */
    private fun extractBalancedBlocksStreaming(rawText: String): List<String> {
        val blocks = mutableListOf<String>()
        val currentParagraph = StringBuilder()
        var currentParagraphWords = 0
        var lineIndex = 0

        fun flushCurrentParagraph() {
            if (currentParagraph.isNotEmpty()) {
                val text = currentParagraph.toString().trim()
                if (text.isNotEmpty()) {
                    blocks.add(text)
                }
                currentParagraph.setLength(0)
                currentParagraphWords = 0
            }
        }

        for (rawLine in rawText.lineSequence()) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                flushCurrentParagraph()
                continue
            }

            val strippedForMeta = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(line)
            val isMetadataHeaderLine = lineIndex <= 4 && strippedForMeta.isNotEmpty() && (
                ArabicTextNormalizer.TITLE_LINE_REGEX.matches(strippedForMeta) ||
                    ArabicTextNormalizer.SUBTITLE_LINE_REGEX.matches(strippedForMeta) ||
                    ArabicTextNormalizer.AUTHOR_LINE_REGEX.matches(strippedForMeta) ||
                    (lineIndex == 0 && strippedForMeta.length in 2..95 && !strippedForMeta.endsWith("."))
                )
            lineIndex++

            val isStandaloneSemanticBlock = isMetadataHeaderLine ||
                ArabicTextNormalizer.isOrnamentalDividerLine(line) ||
                ArabicTextNormalizer.isChapterHeadingLine(line) ||
                line.startsWith("#") ||
                ArabicTextNormalizer.isSubheadingLine(line) ||
                isStandaloneCalloutOrVerseOrListLine(line)

            if (isStandaloneSemanticBlock) {
                flushCurrentParagraph()
                blocks.add(line)
                continue
            }

            val lineWords = ArabicTextNormalizer.countWordsFast(line)
            if (currentParagraph.isNotEmpty() && (currentParagraphWords + lineWords) > 105) {
                flushCurrentParagraph()
            }

            if (currentParagraph.isNotEmpty()) {
                currentParagraph.append(' ')
            }
            currentParagraph.append(line)
            currentParagraphWords += lineWords

            val lastChar = line.last()
            val endsWithSentenceStop = lastChar == '.' ||
                lastChar == '؟' ||
                lastChar == '!' ||
                lastChar == '؛' ||
                lastChar == '»' ||
                lastChar == '﴾'

            if ((currentParagraphWords >= 55 && endsWithSentenceStop) || currentParagraphWords >= 95) {
                flushCurrentParagraph()
            }
        }

        flushCurrentParagraph()
        return blocks
    }

    private fun isStandaloneCalloutOrVerseOrListLine(trimmed: String): Boolean {
        if (ArabicTextNormalizer.isOrnamentalDividerLine(trimmed)) return true
        if (trimmed.startsWith("- ") || trimmed.startsWith("• ") || trimmed.startsWith("* ")) return true
        if (parseStepItemPrefix(trimmed) != null) return true
        if (trimmed.contains(" *** ")) return true
        if ((trimmed.startsWith("﴿") && trimmed.endsWith("﴾")) || (trimmed.startsWith("«") && trimmed.endsWith("»"))) return true

        val stripped = trimmed.trimStart('*', '#', '_', '✦', '❖', '•', ' ')
        val unvocalized = ArabicTextNormalizer.stripArabicDiacriticsForMatching(stripped)
        if (unvocalized.startsWith("قال تعالى") ||
            unvocalized.startsWith("قال الله تعالى") ||
            unvocalized.startsWith("يقول الله تعالى") ||
            unvocalized.startsWith("قوله تعالى") ||
            unvocalized.startsWith("قال رسول الله") ||
            unvocalized.startsWith("قال النبي") ||
            unvocalized.startsWith("وفي الحديث") ||
            unvocalized.startsWith("حديث شريف")
        ) {
            return true
        }
        val lowerUnvoc = unvocalized.lowercase()
        return PEARL_PREFIXES.any { lowerUnvoc.startsWith(it) }
    }

    /**
     * Linear O(N) paragraph splitter that avoids Regex lookbehinds and intermediate word array allocations.
     */
    private fun splitLongParagraphBySentencesLinear(paragraph: String, targetWords: Int = 95): List<String> {
        val len = paragraph.length
        if (len == 0) return emptyList()

        val chunks = mutableListOf<String>()
        var chunkStart = 0
        var wordCount = 0
        var inWord = false
        var lastPunctuationSplitPos = -1

        for (i in 0 until len) {
            val ch = paragraph[i]
            if (ch.isWhitespace()) {
                if (inWord) {
                    inWord = false
                    if (wordCount >= 65) {
                        val prevChar = if (i > 0) paragraph[i - 1] else ' '
                        if (prevChar == '.' || prevChar == '؟' || prevChar == '!' || prevChar == '؛' || prevChar == '،' || prevChar == '»' || prevChar == '﴾') {
                            lastPunctuationSplitPos = i
                        }
                    }
                    if (wordCount >= targetWords && lastPunctuationSplitPos > chunkStart) {
                        val sub = paragraph.substring(chunkStart, lastPunctuationSplitPos).trim()
                        if (sub.isNotEmpty()) chunks.add(sub)
                        chunkStart = lastPunctuationSplitPos
                        while (chunkStart < len && paragraph[chunkStart].isWhitespace()) chunkStart++
                        wordCount = ArabicTextNormalizer.countWordsFast(paragraph.substring(chunkStart, i + 1))
                        lastPunctuationSplitPos = -1
                    } else if (wordCount >= 115) {
                        val splitAt = if (lastPunctuationSplitPos > chunkStart) lastPunctuationSplitPos else i
                        val sub = paragraph.substring(chunkStart, splitAt).trim()
                        if (sub.isNotEmpty()) chunks.add(sub)
                        chunkStart = splitAt
                        while (chunkStart < len && paragraph[chunkStart].isWhitespace()) chunkStart++
                        wordCount = 0
                        lastPunctuationSplitPos = -1
                    }
                }
            } else if (!inWord) {
                inWord = true
                wordCount++
            }
        }

        if (chunkStart < len) {
            val tail = paragraph.substring(chunkStart).trim()
            if (tail.isNotEmpty()) {
                chunks.add(tail)
            }
        }
        return chunks
    }

    /**
     * Non-backtracking O(1) prefix parser for numbered step items (e.g. `1. ...`, `٢- ...`, `12) ...`).
     * Eliminates `RegexOption.DOT_MATCHES_ALL` backtracking on Android 8.
     */
    private fun parseStepItemPrefix(trimmed: String): Pair<String, String>? {
        val len = trimmed.length
        if (len < 4) return null
        var i = 0
        while (i < len && i < 3) {
            val c = trimmed[i]
            val isDigit = c in '0'..'9' || c in '٠'..'٩'
            if (isDigit) {
                i++
            } else {
                break
            }
        }
        if (i == 0 || i >= len) return null
        val sep = trimmed[i]
        if (sep != '.' && sep != ')' && sep != '-') return null
        val afterSep = i + 1
        if (afterSep >= len || !trimmed[afterSep].isWhitespace()) return null
        val numStr = trimmed.substring(0, i)
        val bodyStr = trimmed.substring(afterSep).trim()
        if (bodyStr.isEmpty()) return null
        return Pair(numStr, bodyStr)
    }

    fun formatSemanticParagraphToHtml(rawBlock: String, isRtl: Boolean): String {
        val trimmed = rawBlock.trim()
        if (trimmed.isEmpty()) return ""

        if (ArabicTextNormalizer.isOrnamentalDividerLine(trimmed)) {
            return "<div class=\"ornament\" aria-hidden=\"true\">✦ ✦ ✦</div>"
        }

        if (!ArabicTextNormalizer.containsLetterOrDigit(trimmed)) {
            return ""
        }

        if (ArabicTextNormalizer.isSubheadingLine(trimmed)) {
            val cleanSub = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(trimmed)
            if (cleanSub.isEmpty()) return ""
            return "<h2>${escapeHtml(cleanSub)}</h2>"
        }

        val strippedLeading = trimmed.trimStart('*', '#', '_', '✦', '❖', ' ')
        val unvocalized = ArabicTextNormalizer.stripArabicDiacriticsForMatching(strippedLeading)

        if ((trimmed.contains("﴿") && trimmed.contains("﴾") && trimmed.length <= 320) ||
            unvocalized.startsWith("قال تعالى") ||
            unvocalized.startsWith("قال الله تعالى") ||
            unvocalized.startsWith("يقول الله تعالى") ||
            unvocalized.startsWith("قوله تعالى")
        ) {
            val sourceMatch = BRACKET_SOURCE_REGEX.find(strippedLeading)
            val sourceText = sourceMatch?.groupValues?.get(1)?.trim()
            val mainVerse = if (sourceMatch != null) {
                strippedLeading.removeRange(sourceMatch.range).trim()
            } else {
                strippedLeading
            }.replace(CURLY_VERSE_REGEX, "﴿ $1 ﴾")
                .replace(DOUBLE_PAREN_REGEX, "﴿ $1 ﴾")
                .trim('*')

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

        val isHadithOrQuote = (strippedLeading.startsWith("«") && strippedLeading.endsWith("»")) ||
            unvocalized.startsWith("قال رسول الله") ||
            unvocalized.startsWith("قال النبي") ||
            unvocalized.startsWith("وفي الحديث") ||
            unvocalized.startsWith("حديث شريف") ||
            unvocalized.startsWith("عن أبي ") ||
            unvocalized.startsWith("عن عائشة") ||
            unvocalized.startsWith("عن ابن ") ||
            unvocalized.startsWith("عن أنس") ||
            unvocalized.startsWith("عن عمر") ||
            (strippedLeading.contains("«") && strippedLeading.contains("»") && strippedLeading.length <= 280)

        if (isHadithOrQuote) {
            val upgradedHadith = strippedLeading.replace(DOUBLE_PAREN_REGEX, "« $1 »").trim('*')
            return """
                <div class="hadith-box" data-role="hadith">
                  <p>${escapeHtml(upgradedHadith)}</p>
                </div>
            """.trimIndent()
        }

        val matchedPearlPrefix = PEARL_PREFIXES.firstOrNull { unvocalized.lowercase().startsWith(it) }
        if (matchedPearlPrefix != null) {
            val parts = strippedLeading.split(COLON_DASH_SPLIT_REGEX, limit = 2)
            val rawBadge = if (parts.size == 2 && parts[0].length <= 42) parts[0] else (if (isRtl) "درة وإشراقة" else "Key Insight")
            val badge = ArabicTextNormalizer.stripFormattingAndNoiseFromHeading(rawBadge).ifBlank {
                if (isRtl) "درة وإشراقة" else "Key Insight"
            }
            val content = if (parts.size == 2 && parts[1].isNotBlank()) {
                parts[1].trim().trim('*')
            } else {
                strippedLeading.trim('*')
            }

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

        if (trimmed.contains(" *** ") || (trimmed.contains(" ... ") && trimmed.length <= 130 && !trimmed.endsWith("."))) {
            val delimiter = if (trimmed.contains(" *** ")) " *** " else " ... "
            val hemistichs = trimmed.split(delimiter, limit = 2).map { it.trim().trim('*') }
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

        val stepPair = parseStepItemPrefix(trimmed)
        if (stepPair != null) {
            val (num, stepBody) = stepPair
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

        return "<p dir=\"auto\">${formatInlineHighlights(escapeHtml(trimmed))}</p>"
    }

    fun applyInlineFormatting(escapedText: String): String = formatInlineHighlights(escapedText)

    private fun formatInlineHighlights(escapedText: String): String {
        val formatted = escapedText
            .replace(INLINE_QURAN_REGEX) { m ->
                "<span class=\"quran-verse\" data-role=\"quran\">﴿${m.groupValues[1]}﴾</span>"
            }
            .replace(INLINE_HADITH_REGEX) { m ->
                "<span class=\"hadith-quote\" data-role=\"hadith\">«${m.groupValues[1]}»</span>"
            }
            .replace(INLINE_NUM_BADGE_REGEX) { m ->
                "<span class=\"num-badge\" data-role=\"numbers\">[${m.groupValues[1]}]</span>"
            }
            .replace(INLINE_BOLD_REGEX) { m ->
                "<strong>${m.groupValues[1]}</strong>"
            }
            .replace(INLINE_WHATSAPP_BOLD_REGEX) { m ->
                "<strong>${m.groupValues[1]}</strong>"
            }
            .replace(INLINE_WHATSAPP_ITALIC_REGEX) { m ->
                "<em>${m.groupValues[1]}</em>"
            }
        return formatted.replace(STRAY_ASTERISK_REGEX, "")
    }

    fun escapeHtml(text: String): String {
        if (text.isEmpty()) return ""
        val sb = StringBuilder(text.length + 16)
        for (i in 0 until text.length) {
            when (val ch = text[i]) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
