package com.maktabati.app.data.converter.paginator

import com.maktabati.app.data.converter.model.ConvertedPageItem
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.converter.theme.BookVisualTheme

/**
 * Splits extracted book text into balanced chapters & pages (85–125 words per page)
 * and formats semantic blocks (`.quran-box`, `.hadith-box`, `.wisdom-pearl`, `.heart-keepsake-card`, `.step-card`, `.poetry-stanza`).
 */
object SemanticBookPaginator {

    private val PARAGRAPH_SPLIT_REGEX = Regex("\\n\\s*\\n+")
    private val COLON_DASH_SPLIT_REGEX = Regex("[:：\\-—]")
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val SENTENCE_SPLIT_REGEX = Regex("(?<=[.!?؟؛\\n])\\s+")
    private val BRACKET_SOURCE_REGEX = Regex("\\[([^\\]]+)\\]")
    private val CURLY_VERSE_REGEX = Regex("\\{\\s*([^}]+)\\s*\\}")
    private val DOUBLE_PAREN_REGEX = Regex("\\(\\(\\s*([^)]+)\\s*\\)\\)")
    private val STEP_ITEM_REGEX = Regex("^([0-9٠-٩]{1,2})[.)\\-]\\s+(.+)$", RegexOption.DOT_MATCHES_ALL)
    private val INLINE_QURAN_REGEX = Regex("﴿([^﴾]+)﴾")
    private val INLINE_HADITH_REGEX = Regex("«([^»]+)»")
    private val INLINE_NUM_BADGE_REGEX = Regex("\\[([0-9٠-٩]{1,4})\\]")
    private val INLINE_BOLD_REGEX = Regex("\\*\\*([^*]+)\\*\\*")

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
        val pages = mutableListOf<ConvertedPageItem>()

        pages.add(
            ConvertedPageItem(
                tag = if (isRtl) "الغلاف الملكي" else "Royal Cover",
                title = bookTitle,
                type = "cover",
                html = LuxuryHtmlTemplateRenderer.buildCoverPageHtml(
                    title = bookTitle,
                    subtitle = bookSubtitle,
                    author = bookAuthor,
                    theme = theme,
                    isRtl = isRtl
                )
            )
        )

        val rawBlocks = rawText
            .split(PARAGRAPH_SPLIT_REGEX)
            .flatMap { block ->
                val subLines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (subLines.size > 1 && subLines.any { ArabicTextNormalizer.isChapterHeadingLine(it) || it.startsWith("#") }) {
                    val splitList = mutableListOf<String>()
                    val currentBuf = StringBuilder()
                    for (l in subLines) {
                        if (ArabicTextNormalizer.isChapterHeadingLine(l) ||
                            l.startsWith("#") ||
                            ArabicTextNormalizer.AUTHOR_LINE_REGEX.matches(l)
                        ) {
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

        val sections = mutableListOf<SemanticSection>()
        var currentChapterIndex = 0
        var activeSection = SemanticSection(
            chapterTag = if (isRtl) "مدخل الكتاب" else "Introduction",
            chapterTitle = bookTitle
        )

        for ((idx, block) in rawBlocks.withIndex()) {
            val clean = block.removePrefix("#").removePrefix("#").removePrefix("#").trim()

            if (idx <= 2) {
                if (clean.equals(bookTitle, ignoreCase = true) ||
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
                val words = block.split(WHITESPACE_REGEX).filter { it.isNotBlank() }
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

        for (section in sections) {
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
                        """.trimIndent()
                    ).append("\n")
                } else {
                    pageHtmlBuilder.append(
                        """
                        <h2>${escapeHtml(section.chapterTitle)} <small style="font-size:0.65em;color:var(--accent-gold);">($sectionPageIndex)</small></h2>
                        """.trimIndent()
                    ).append("\n")
                }

                for (blk in currentPageBlocks) {
                    pageHtmlBuilder.append(formatSemanticParagraphToHtml(blk, isRtl)).append("\n")
                }

                val pageTitle = if (sectionPageIndex == 1) {
                    section.chapterTitle
                } else {
                    "${section.chapterTitle} ($sectionPageIndex)"
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
                val pWords = para.split(WHITESPACE_REGEX).count { it.isNotBlank() }.coerceAtLeast(1)
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
        val sentences = paragraph.split(SENTENCE_SPLIT_REGEX)
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        var count = 0

        for (s in sentences) {
            val w = s.split(WHITESPACE_REGEX).count { it.isNotBlank() }
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

    fun formatSemanticParagraphToHtml(rawBlock: String, isRtl: Boolean): String {
        val trimmed = rawBlock.trim()
        if (trimmed.isEmpty()) return ""

        if (ArabicTextNormalizer.isSubheadingLine(trimmed)) {
            val cleanSub = trimmed.removePrefix("###").removePrefix("##").removePrefix("#").trim()
            return "<h2>${escapeHtml(cleanSub)}</h2>"
        }

        val unvocalized = ArabicTextNormalizer.stripArabicDiacriticsForMatching(trimmed)

        if ((trimmed.contains("﴿") && trimmed.contains("﴾") && trimmed.length <= 320) ||
            unvocalized.startsWith("قال تعالى") ||
            unvocalized.startsWith("قال الله تعالى") ||
            unvocalized.startsWith("يقول الله تعالى") ||
            unvocalized.startsWith("قوله تعالى")
        ) {
            val sourceMatch = BRACKET_SOURCE_REGEX.find(trimmed)
            val sourceText = sourceMatch?.groupValues?.get(1)?.trim()
            val mainVerse = if (sourceMatch != null) {
                trimmed.removeRange(sourceMatch.range).trim()
            } else {
                trimmed
            }.replace(CURLY_VERSE_REGEX, "﴿ $1 ﴾")
                .replace(DOUBLE_PAREN_REGEX, "﴿ $1 ﴾")

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
            val upgradedHadith = trimmed.replace(DOUBLE_PAREN_REGEX, "« $1 »")
            return """
                <div class="hadith-box" data-role="hadith">
                  <p>${escapeHtml(upgradedHadith)}</p>
                </div>
            """.trimIndent()
        }

        val matchedPearlPrefix = PEARL_PREFIXES.firstOrNull { unvocalized.lowercase().startsWith(it) }
        if (matchedPearlPrefix != null) {
            val parts = trimmed.split(COLON_DASH_SPLIT_REGEX, limit = 2)
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

        val stepMatch = STEP_ITEM_REGEX.find(trimmed)
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

        return "<p dir=\"auto\">${formatInlineHighlights(escapeHtml(trimmed))}</p>"
    }

    private fun formatInlineHighlights(escapedText: String): String {
        return escapedText
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
              <div class="heart-badge">${theme.badgeEmoji} مسك الختام</div>
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
              <div class="heart-badge">${theme.badgeEmoji} Journey Completed</div>
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

    fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
