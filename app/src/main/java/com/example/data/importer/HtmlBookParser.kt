package com.example.data.importer

import com.example.data.model.TocEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.regex.Pattern

data class ParsedHtmlMetadata(
    val title: String,
    val subtitle: String,
    val author: String,
    val description: String,
    val language: String,
    val isRtl: Boolean,
    val pageCount: Int,
    val accentRoseHex: String,
    val accentGoldHex: String,
    val bgPrimaryHex: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val hasExternalCss: Boolean,
    val hasExternalJs: Boolean,
    val tableOfContents: List<TocEntry>,
    val hasCoverPage: Boolean = true
)

data class TrueBookPalette(
    val bgPrimaryHex: String,
    val accentRoseHex: String,
    val accentGoldHex: String,
    val coverGradientStart: Long,
    val coverGradientEnd: Long
)

object HtmlBookParser {

    // HTML Tag & Meta Patterns
    private val HTML_TAG_PATTERN = Pattern.compile("<html([^>]*)>", Pattern.CASE_INSENSITIVE)
    private val DIR_ATTR_PATTERN = Pattern.compile("dir\\s*=\\s*[\"'](rtl|ltr)[\"']", Pattern.CASE_INSENSITIVE)
    private val LANG_ATTR_PATTERN = Pattern.compile("lang\\s*=\\s*[\"']([a-zA-Z\\-]+)[\"']", Pattern.CASE_INSENSITIVE)
    private val TITLE_PATTERN = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val H1_PATTERN = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val AUTHOR_META_PATTERN = Pattern.compile("<meta\\s+[^>]*name=[\"']author[\"'][^>]*content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE)
    private val DESC_META_PATTERN = Pattern.compile("<meta\\s+[^>]*name=[\"']description[\"'][^>]*content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE)
    private val CSS_LINK_PATTERN = Pattern.compile("<link\\s+[^>]*rel=[\"']stylesheet[\"'][^>]*>", Pattern.CASE_INSENSITIVE)
    private val STYLE_TAG_PATTERN = Pattern.compile("<style[^>]*>", Pattern.CASE_INSENSITIVE)
    private val SCRIPT_PATTERN = Pattern.compile("<script\\s*[^>]*>", Pattern.CASE_INSENSITIVE)
    private val HEADING_PATTERN = Pattern.compile("<(h[1-3])(?:\\s+[^>]*id=[\"']([^\"']+)[\"'][^>]*)?>(.*?)</\\1>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val ARABIC_UNICODE_PATTERN = Pattern.compile("[\\u0600-\\u06FF]")

    // BOOK_DATA JavaScript Object / JSON Patterns
    private val BOOK_DATA_DECL_REGEX = Regex("(?:const|let|var|window\\.)\\s*BOOK_DATA\\s*=")
    private val BOOK_DATA_TITLE_PATTERN = Pattern.compile("[\"']?\\btitle[\"']?\\s*:\\s*[\"']([^\"']+)[\"']")
    private val BOOK_DATA_SUBTITLE_PATTERN = Pattern.compile("[\"']?\\bsubtitle[\"']?\\s*:\\s*[\"']([^\"']+)[\"']")
    private val BOOK_DATA_AUTHOR_PATTERN = Pattern.compile("[\"']?\\bauthor[\"']?\\s*:\\s*[\"']([^\"']+)[\"']")
    private val KEY_TITLE_REGEX = Regex("[\"']?(?:title|sectionTitle|chapterTag|chapterNumber|heading|subtitle)[\"']?\\s*:\\s*[\"']([^\"']+)[\"']")
    private val ARRAY_START_REGEX = Regex("[\"']?(?:pages|chapters|slides)[\"']?\\s*:\\s*\\[")
    private val STRIP_TAGS_REGEX = Regex("<.*?>")

    // Ordered CSS variable candidates for True Palette Extraction
    private val BG_VAR_NAMES = listOf(
        "bg-primary",
        "paper-bg",
        "card-bg",
        "bg-secondary",
        "bg",
        "surface",
        "background"
    )

    private val PRIMARY_ROSE_VAR_NAMES = listOf(
        "accent-rose",
        "primary",
        "accent",
        "brand",
        "rose",
        "main-color",
        "heading-color"
    )

    private val SECONDARY_GOLD_VAR_NAMES = listOf(
        "accent-gold",
        "gold",
        "secondary",
        "border-color",
        "ornament",
        "accent-peach",
        "accent-rose-light"
    )

    private val BODY_OR_CANVAS_BG_REGEX = Regex(
        """(?:body|\.page-canvas|\.cover-page|\.page|html)\s*\{[^}]*?\bbackground(?:-color)?\s*:\s*(#[0-9a-fA-F]{3,8})\b""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private val H1_OR_TITLE_COLOR_REGEX = Regex(
        """(?:h1|\.cover-title|\.book-title)\s*\{[^}]*?(?<![-\w])color\s*:\s*(#[0-9a-fA-F]{3,8})\b""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private val SVG_STROKE_OR_FILL_REGEX = Regex(
        """<(?:svg|path|circle|rect|polygon|g)\b[^>]*?\b(?:stroke|fill)\s*=\s*["'](#[0-9a-fA-F]{3,8})["']""",
        RegexOption.IGNORE_CASE
    )

    private val STYLE_BLOCK_REGEX = Regex(
        """<style\b[^>]*>(.*?)</style>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private val ANY_HEX_COLOR_REGEX = Regex("""#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{3})\b""")

    // Cover Detection Regexes
    private val COVER_TYPE_JS_REGEX = Regex("""(?<![a-zA-Z0-9_])type\s*:\s*["'`]cover["'`]""", RegexOption.IGNORE_CASE)
    private val COVER_DOM_CLASS_REGEX = Regex("""class\s*=\s*["'][^"']*\b(?:cover|cover-page|layout-cover|cover-title|book-cover)\b[^"']*["']""", RegexOption.IGNORE_CASE)
    private val COVER_DATA_PAGE_ZERO_REGEX = Regex("""\bdata-page\s*=\s*["']0["']""", RegexOption.IGNORE_CASE)
    private val COVER_IMG_TAG_REGEX = Regex("""<img\b[^>]*class\s*=\s*["'][^"']*cover[^"']*["'][^>]*>""", RegexOption.IGNORE_CASE)
    private val OG_IMAGE_META_REGEX = Regex("""<meta\b[^>]*property\s*=\s*["']og:image["'][^>]*content\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    fun isValidHtmlContent(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.length < 15) return false
        val checkLen = minOf(trimmed.length, 1024)
        for (i in 0 until checkLen) {
            if (trimmed[i] == '\u0000') return false
        }
        val probe = if (trimmed.length > 16384) trimmed.substring(0, 16384).lowercase() else trimmed.lowercase()
        return probe.contains("<html") ||
            probe.contains("<!doctype html") ||
            probe.contains("<body") ||
            probe.contains("<head") ||
            probe.contains("<div") ||
            probe.contains("<section") ||
            probe.contains("<article") ||
            probe.contains("<h1") ||
            probe.contains("<p") ||
            probe.contains("book_data") ||
            trimmed.lowercase().contains("book_data")
    }

    fun parse(entryHtmlFile: File, fallbackTitle: String): ParsedHtmlMetadata {
        val content = try {
            if (entryHtmlFile.length() > 5 * 1024 * 1024) {
                entryHtmlFile.bufferedReader(Charsets.UTF_8).use { reader ->
                    val buffer = CharArray(5 * 1024 * 1024)
                    val read = reader.read(buffer)
                    if (read > 0) String(buffer, 0, read) else ""
                }
            } else {
                entryHtmlFile.readText(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            ""
        }
        return parseContent(content, fallbackTitle, quickMode = false)
    }

    fun parseQuickHeader(content: String, fallbackTitle: String): ParsedHtmlMetadata {
        return parseContent(content, fallbackTitle, quickMode = true)
    }

    fun normalizeHexColor(raw: String): String? {
        val cleaned = raw.trim().removePrefix("#")
        return when (cleaned.length) {
            3 -> {
                val r = cleaned[0]
                val g = cleaned[1]
                val b = cleaned[2]
                "#$r$r$g$g$b$b".uppercase()
            }
            6 -> "#${cleaned.uppercase()}"
            8 -> "#${cleaned.substring(cleaned.length - 6).uppercase()}"
            else -> null
        }
    }

    private fun findCssVarHex(sample: String, varNames: List<String>): String? {
        for (name in varNames) {
            val regex = Regex("""--${Regex.escape(name)}\s*:\s*(#[0-9a-fA-F]{3,8})\b""", RegexOption.IGNORE_CASE)
            val match = regex.find(sample)?.groupValues?.get(1)
            if (match != null) {
                val normalized = normalizeHexColor(match)
                if (normalized != null) return normalized
            }
        }
        return null
    }

    fun computeColorLuminance(colorLong: Long): Float {
        val r = ((colorLong shr 16) and 0xFF) / 255f
        val g = ((colorLong shr 8) and 0xFF) / 255f
        val b = (colorLong and 0xFF) / 255f
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    fun blendColorLongs(c1: Long, c2: Long, ratioC2: Float): Long {
        val t = ratioC2.coerceIn(0f, 1f)
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF
        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF
        val r = (r1 * (1f - t) + r2 * t).toLong().coerceIn(0L, 255L)
        val g = (g1 * (1f - t) + g2 * t).toLong().coerceIn(0L, 255L)
        val b = (b1 * (1f - t) + b2 * t).toLong().coerceIn(0L, 255L)
        return (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
    }

    /**
     * Extracts the true color palette from `:root`, `body`, `.page-canvas`, `h1`, `<style>`, and `<svg>`
     * without forcing default crimson (`#E11D48`) or darkening light/creamy backgrounds.
     */
    fun extractTruePalette(content: String): TrueBookPalette {
        val sample = if (content.length > 45000) content.substring(0, 45000) else content
        val styleContent = STYLE_BLOCK_REGEX.findAll(sample).joinToString("\n") { it.groupValues[1] }.ifBlank { sample }

        // 1. Background Color (bgPrimaryHex)
        var bgHex = findCssVarHex(styleContent, BG_VAR_NAMES)
            ?: findCssVarHex(sample, BG_VAR_NAMES)
            ?: BODY_OR_CANVAS_BG_REGEX.find(styleContent)?.groupValues?.get(1)?.let { normalizeHexColor(it) }
            ?: BODY_OR_CANVAS_BG_REGEX.find(sample)?.groupValues?.get(1)?.let { normalizeHexColor(it) }

        // 2. Primary / Rose / Heading Color (accentRoseHex)
        var roseHex = findCssVarHex(styleContent, PRIMARY_ROSE_VAR_NAMES)
            ?: findCssVarHex(sample, PRIMARY_ROSE_VAR_NAMES)
            ?: H1_OR_TITLE_COLOR_REGEX.find(styleContent)?.groupValues?.get(1)?.let { normalizeHexColor(it) }
            ?: H1_OR_TITLE_COLOR_REGEX.find(sample)?.groupValues?.get(1)?.let { normalizeHexColor(it) }

        // 3. Secondary / Gold / Ornament Color (accentGoldHex)
        var goldHex = findCssVarHex(styleContent, SECONDARY_GOLD_VAR_NAMES)
            ?: findCssVarHex(sample, SECONDARY_GOLD_VAR_NAMES)

        // 4. Fallback to SVG stroke/fill or <style> hex colors if any slot is still missing
        if (bgHex == null || roseHex == null || goldHex == null) {
            val svgColors = SVG_STROKE_OR_FILL_REGEX.findAll(sample)
                .mapNotNull { normalizeHexColor(it.groupValues[1]) }
                .filter { it != "#FFFFFF" && it != "#000000" && it != "#NONE" }
                .distinct()
                .toList()

            val styleColors = ANY_HEX_COLOR_REGEX.findAll(styleContent)
                .mapNotNull { normalizeHexColor(it.value) }
                .distinct()
                .toList()

            if (bgHex == null) {
                bgHex = styleColors.firstOrNull { hex ->
                    val lum = computeColorLuminance(parseHexToColorLong(hex, 0xFFFAF6F0))
                    lum > 0.80f || lum < 0.22f
                } ?: "#1E293B"
            }

            if (roseHex == null) {
                val candidateFromSvgOrStyle = (svgColors + styleColors).firstOrNull { hex ->
                    hex != bgHex && hex != "#FFFFFF" && hex != "#000000"
                }
                roseHex = candidateFromSvgOrStyle ?: "#5B3A29"
            }

            if (goldHex == null) {
                val candidateGold = (svgColors + styleColors).firstOrNull { hex ->
                    hex != bgHex && hex != roseHex && hex != "#FFFFFF" && hex != "#000000"
                }
                goldHex = candidateGold ?: "#D97706"
            }
        }

        val finalBgHex = bgHex ?: "#1E293B"
        val finalRoseHex = roseHex ?: "#5B3A29"
        val finalGoldHex = goldHex ?: "#D97706"

        val startColor = parseHexToColorLong(finalBgHex, 0xFF1E293B)
        val roseLong = parseHexToColorLong(finalRoseHex, 0xFF5B3A29)
        val isLightBg = computeColorLuminance(startColor) > 0.48f

        // Never darken light/creamy covers with dark navy 0xFF0F172A!
        val endColor = if (isLightBg) {
            blendColorLongs(startColor, roseLong, 0.15f)
        } else {
            blendColorLongs(startColor, roseLong, 0.45f)
        }

        return TrueBookPalette(
            bgPrimaryHex = finalBgHex,
            accentRoseHex = finalRoseHex,
            accentGoldHex = finalGoldHex,
            coverGradientStart = startColor,
            coverGradientEnd = endColor
        )
    }

    fun detectHasCoverPage(content: String, headSample: String, bdHeaderSlice: String): Boolean {
        val earlySlice = if (content.length > 35000) content.substring(0, 35000) else content
        if (bdHeaderSlice.isNotEmpty() && COVER_TYPE_JS_REGEX.containsMatchIn(bdHeaderSlice)) return true
        if (COVER_TYPE_JS_REGEX.containsMatchIn(earlySlice)) return true
        if (COVER_DOM_CLASS_REGEX.containsMatchIn(earlySlice)) return true
        if (COVER_DATA_PAGE_ZERO_REGEX.containsMatchIn(earlySlice)) return true
        if (COVER_IMG_TAG_REGEX.containsMatchIn(earlySlice)) return true
        if (OG_IMAGE_META_REGEX.containsMatchIn(headSample)) return true
        val firstChunk = if (content.length > 12000) content.substring(0, 12000) else content
        if (firstChunk.contains("الغلاف")) return true
        return false
    }

    fun parseContent(content: String, fallbackTitle: String, quickMode: Boolean = false): ParsedHtmlMetadata {
        if (!isValidHtmlContent(content)) {
            throw IllegalArgumentException("الملف المحدد تالف أو ليس ملف كتاب HTML صالحاً")
        }

        val headSample = if (content.length > 20000) content.substring(0, 20000) else content
        val bdDeclMatch = BOOK_DATA_DECL_REGEX.find(content)
        val bdStartIdx = bdDeclMatch?.range?.first ?: content.indexOf("BOOK_DATA")
        val bdHeaderSlice = if (bdStartIdx >= 0) {
            val pagesIdx = content.indexOf("pages", bdStartIdx).takeIf { it > bdStartIdx && it - bdStartIdx < 1600 }
            val endIdx = pagesIdx ?: minOf(content.length, bdStartIdx + 1600)
            content.substring(bdStartIdx, endIdx)
        } else {
            ""
        }

        // 1. Extract Title: Priority 1 -> BOOK_DATA.title, Priority 2 -> <title>, Priority 3 -> first <h1>
        var title = ""
        if (bdHeaderSlice.isNotEmpty()) {
            val bdTitleMatcher = BOOK_DATA_TITLE_PATTERN.matcher(bdHeaderSlice)
            if (bdTitleMatcher.find()) {
                title = bdTitleMatcher.group(1)?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            val titleMatcher = TITLE_PATTERN.matcher(headSample)
            if (titleMatcher.find()) {
                title = titleMatcher.group(1)?.replace(STRIP_TAGS_REGEX, "")?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            val h1Matcher = H1_PATTERN.matcher(headSample)
            if (h1Matcher.find()) {
                title = h1Matcher.group(1)?.replace(STRIP_TAGS_REGEX, "")?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            title = fallbackTitle
        }

        // 2. Extract Subtitle & Author
        var subtitle = ""
        if (bdHeaderSlice.isNotEmpty()) {
            val bdSubtitleMatcher = BOOK_DATA_SUBTITLE_PATTERN.matcher(bdHeaderSlice)
            if (bdSubtitleMatcher.find()) {
                subtitle = bdSubtitleMatcher.group(1)?.trim() ?: ""
            }
        }

        var description = ""
        val descMatcher = DESC_META_PATTERN.matcher(headSample)
        if (descMatcher.find()) {
            description = descMatcher.group(1)?.trim() ?: ""
        }
        if (subtitle.isBlank() && description.isNotBlank()) {
            subtitle = description
        }

        var author = ""
        if (bdHeaderSlice.isNotEmpty()) {
            val bdAuthorMatcher = BOOK_DATA_AUTHOR_PATTERN.matcher(bdHeaderSlice)
            if (bdAuthorMatcher.find()) {
                author = bdAuthorMatcher.group(1)?.trim() ?: ""
            }
        }
        if (author.isBlank()) {
            val authorMatcher = AUTHOR_META_PATTERN.matcher(headSample)
            if (authorMatcher.find()) {
                author = authorMatcher.group(1)?.trim() ?: ""
            }
        }

        // 3. Direction & Language from <html dir="..." lang="...">
        var dirAttr: String? = null
        var langAttr: String? = null
        val htmlTagMatcher = HTML_TAG_PATTERN.matcher(headSample)
        if (htmlTagMatcher.find()) {
            val attrs = htmlTagMatcher.group(1) ?: ""
            val dirM = DIR_ATTR_PATTERN.matcher(attrs)
            if (dirM.find()) dirAttr = dirM.group(1)?.lowercase()
            val langM = LANG_ATTR_PATTERN.matcher(attrs)
            if (langM.find()) langAttr = langM.group(1)?.lowercase()
        }

        val hasArabicTitleOrBody = ARABIC_UNICODE_PATTERN.matcher(title).find() || ARABIC_UNICODE_PATTERN.matcher(content.take(4000)).find()
        val isRtl = when (dirAttr) {
            "rtl" -> true
            "ltr" -> false
            else -> if (langAttr?.startsWith("ar") == true) true else if (langAttr?.startsWith("en") == true) false else hasArabicTitleOrBody
        }
        val language = when {
            langAttr?.startsWith("ar") == true -> "ar"
            langAttr?.startsWith("en") == true -> "en"
            isRtl -> "ar"
            else -> "en"
        }

        if (author.isBlank()) {
            author = if (language == "ar") "مؤلف غير محدد" else "Unknown Author"
        }

        // 4. Extract Page Count & TOC
        val tocList = mutableListOf<TocEntry>()
        var pageCount = if (quickMode) {
            maxOf(1, content.length / 950)
        } else {
            extractBookDataPagesCountAndToc(content, bdStartIdx, tocList)
        }

        if (!quickMode) {
            if (tocList.isEmpty()) {
                val headingMatcher = HEADING_PATTERN.matcher(content)
                var count = 0
                while (headingMatcher.find() && count < 1000) {
                    val tag = headingMatcher.group(1)?.lowercase() ?: "h1"
                    val id = headingMatcher.group(2)
                    val headingText = headingMatcher.group(3)?.replace(STRIP_TAGS_REGEX, "")?.trim() ?: ""
                    if (headingText.isNotBlank()) {
                        val level = when (tag) {
                            "h1" -> 1
                            "h2" -> 2
                            else -> 3
                        }
                        val href = if (!id.isNullOrBlank()) "#$id" else "#page-${count + 1}"
                        tocList.add(TocEntry(title = headingText, href = href, level = level))
                        count++
                    }
                }
            }

            if (pageCount <= 0) {
                val pageClassCount = Regex("class\\s*=\\s*[\"'][^\"']*\\b(?:page|book-page)\\b[^\"']*[\"']", RegexOption.IGNORE_CASE).findAll(content).count()
                val sectionCount = Regex("<(?:section|article)\\b", RegexOption.IGNORE_CASE).findAll(content).count()
                pageCount = when {
                    pageClassCount > 1 -> pageClassCount
                    sectionCount > 1 -> sectionCount
                    tocList.size > 1 -> tocList.size
                    else -> maxOf(1, (content.length / 1800))
                }
            }
        }

        // 5. Extract True Palette (preserving light/creamy and dark themes accurately)
        val palette = extractTruePalette(content)
        val hasCoverPage = detectHasCoverPage(content, headSample, bdHeaderSlice)

        val hasExternalCss = CSS_LINK_PATTERN.matcher(headSample).find() || STYLE_TAG_PATTERN.matcher(headSample).find()
        val hasExternalJs = bdStartIdx >= 0 || SCRIPT_PATTERN.matcher(headSample).find()

        return ParsedHtmlMetadata(
            title = title,
            subtitle = subtitle,
            author = author,
            description = if (description.isNotBlank()) description else subtitle,
            language = language,
            isRtl = isRtl,
            pageCount = pageCount,
            accentRoseHex = palette.accentRoseHex,
            accentGoldHex = palette.accentGoldHex,
            bgPrimaryHex = palette.bgPrimaryHex,
            coverGradientStart = palette.coverGradientStart,
            coverGradientEnd = palette.coverGradientEnd,
            hasExternalCss = hasExternalCss,
            hasExternalJs = hasExternalJs,
            tableOfContents = tocList,
            hasCoverPage = hasCoverPage
        )
    }

    private fun extractBookDataPagesCountAndToc(content: String, bdStartIdx: Int, tocOut: MutableList<TocEntry>): Int {
        val startSearch = if (bdStartIdx >= 0) bdStartIdx else 0
        val searchRegion = if (startSearch > 0) content.substring(startSearch) else content
        val pagesStartMatch = ARRAY_START_REGEX.find(searchRegion)
            ?: (if (startSearch > 0) ARRAY_START_REGEX.find(content) else null)
            ?: return 0

        val baseOffset = if (ARRAY_START_REGEX.find(searchRegion) != null) startSearch else 0
        val arrayStartIndex = baseOffset + pagesStartMatch.range.last // index of '['
        var bracketDepth = 0
        var inString = false
        var stringChar = ' '
        var inLineComment = false
        var inBlockComment = false
        var escaped = false
        var topLevelObjectCount = 0
        var objectDepth = 0
        var currentObjectStart = -1

        var i = arrayStartIndex
        val len = content.length
        while (i < len) {
            val c = content[i]
            val nextC = if (i + 1 < len) content[i + 1] else ' '

            if (inLineComment) {
                if (c == '\n' || c == '\r') inLineComment = false
                i++
                continue
            }
            if (inBlockComment) {
                if (c == '*' && nextC == '/') {
                    inBlockComment = false
                    i += 2
                    continue
                }
                i++
                continue
            }
            if (escaped) {
                escaped = false
                i++
                continue
            }
            if (inString) {
                if (c == '\\') {
                    escaped = true
                } else if (c == stringChar) {
                    inString = false
                }
                i++
                continue
            }

            if (c == '/' && nextC == '/') {
                inLineComment = true
                i += 2
                continue
            }
            if (c == '/' && nextC == '*') {
                inBlockComment = true
                i += 2
                continue
            }
            if (c == '"' || c == '\'' || c == '`') {
                inString = true
                stringChar = c
                i++
                continue
            }

            when (c) {
                '[' -> bracketDepth++
                ']' -> {
                    bracketDepth--
                    if (bracketDepth == 0) {
                        break
                    }
                }
                '{' -> {
                    if (bracketDepth == 1 && objectDepth == 0) {
                        currentObjectStart = i
                    }
                    objectDepth++
                }
                '}' -> {
                    objectDepth--
                    if (bracketDepth == 1 && objectDepth == 0 && currentObjectStart != -1) {
                        topLevelObjectCount++
                        if (tocOut.size < 1000) {
                            val sliceEnd = minOf(i + 1, currentObjectStart + 450)
                            val objSlice = content.substring(currentObjectStart, sliceEnd)
                            val pageTitleMatch = KEY_TITLE_REGEX.find(objSlice)
                            val pageTitle = pageTitleMatch?.groupValues?.get(1)?.trim() ?: "صفحة $topLevelObjectCount"
                            if (pageTitle.isNotBlank()) {
                                tocOut.add(
                                    TocEntry(
                                        title = pageTitle,
                                        href = "#page-$topLevelObjectCount",
                                        level = 1
                                    )
                                )
                            }
                        }
                        currentObjectStart = -1
                    }
                }
            }
            i++
        }
        return topLevelObjectCount
    }

    private fun extractCssVar(content: String, pattern: Pattern): String? {
        val m = pattern.matcher(content)
        return if (m.find()) m.group(1)?.trim() else null
    }

    fun parseHexToColorLong(hex: String, defaultColor: Long): Long {
        val clean = hex.trim().removePrefix("#")
        return try {
            when (clean.length) {
                3 -> {
                    val r = clean[0].toString().repeat(2)
                    val g = clean[1].toString().repeat(2)
                    val b = clean[2].toString().repeat(2)
                    ("FF$r$g$b").toLong(16)
                }
                6 -> ("FF$clean").toLong(16)
                8 -> clean.toLong(16)
                else -> defaultColor
            }
        } catch (_: Exception) {
            defaultColor
        }
    }

    private fun darkenColorLong(color: Long): Long {
        val a = (color shr 24) and 0xFF
        val r = (((color shr 16) and 0xFF) * 65) / 100
        val g = (((color shr 8) and 0xFF) * 65) / 100
        val b = ((color and 0xFF) * 65) / 100
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun tocToJson(tocList: List<TocEntry>): String {
        val array = JSONArray()
        for (entry in tocList) {
            val obj = JSONObject()
            obj.put("title", entry.title)
            obj.put("href", entry.href)
            obj.put("level", entry.level)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToToc(jsonStr: String): List<TocEntry> {
        val list = mutableListOf<TocEntry>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TocEntry(
                        title = obj.optString("title", ""),
                        href = obj.optString("href", ""),
                        level = obj.optInt("level", 1)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
