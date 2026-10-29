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
    val tableOfContents: List<TocEntry>
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

    // BOOK_DATA JavaScript Object Patterns
    private val BOOK_DATA_BLOCK_PATTERN = Pattern.compile("(?:const|let|var|window\\.)\\s*BOOK_DATA\\s*=\\s*\\{(.*?)\\};", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
    private val BOOK_DATA_TITLE_PATTERN = Pattern.compile("BOOK_DATA[\\s\\S]{0,800}?\\btitle\\s*:\\s*[\"']([^\"']+)[\"']")
    private val BOOK_DATA_SUBTITLE_PATTERN = Pattern.compile("BOOK_DATA[\\s\\S]{0,1200}?\\bsubtitle\\s*:\\s*[\"']([^\"']+)[\"']")
    private val BOOK_DATA_AUTHOR_PATTERN = Pattern.compile("BOOK_DATA[\\s\\S]{0,1500}?\\bauthor\\s*:\\s*[\"']([^\"']+)[\"']")

    // CSS :root Variable Patterns
    private val ACCENT_ROSE_PATTERN = Pattern.compile("--(?:accent-rose|accent-peach)\\s*:\\s*(#[0-9a-fA-F]{3,8})")
    private val ACCENT_GOLD_PATTERN = Pattern.compile("--accent-gold\\s*:\\s*(#[0-9a-fA-F]{3,8})")
    private val BG_PRIMARY_PATTERN = Pattern.compile("--(?:bg-primary|bg-secondary)\\s*:\\s*(#[0-9a-fA-F]{3,8})")

    fun isValidHtmlContent(content: String): Boolean {
        val trimmed = content.trim()
        if (trimmed.length < 15) return false
        // Reject binary files (e.g., null bytes in first KB)
        val checkLen = minOf(trimmed.length, 1024)
        for (i in 0 until checkLen) {
            if (trimmed[i] == '\u0000') return false
        }
        val lower = trimmed.lowercase()
        return lower.contains("<html") ||
            lower.contains("<!doctype html") ||
            lower.contains("<body") ||
            lower.contains("<head") ||
            lower.contains("<div") ||
            lower.contains("<section") ||
            lower.contains("<article") ||
            lower.contains("<h1") ||
            lower.contains("<p") ||
            lower.contains("book_data")
    }

    fun parse(entryHtmlFile: File, fallbackTitle: String): ParsedHtmlMetadata {
        val content = try {
            // Read up to 5MB safely for full BOOK_DATA.pages counting without blocking
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

        if (!isValidHtmlContent(content)) {
            throw IllegalArgumentException("الملف المحدد تالف أو ليس ملف كتاب HTML صالحاً")
        }

        // 1. Extract Title: Priority 1 -> BOOK_DATA.title, Priority 2 -> <title>, Priority 3 -> first <h1>
        var title = ""
        val bdTitleMatcher = BOOK_DATA_TITLE_PATTERN.matcher(content)
        if (bdTitleMatcher.find()) {
            title = bdTitleMatcher.group(1)?.trim() ?: ""
        }
        if (title.isBlank()) {
            val titleMatcher = TITLE_PATTERN.matcher(content)
            if (titleMatcher.find()) {
                title = titleMatcher.group(1)?.replace(Regex("<.*?>"), "")?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            val h1Matcher = H1_PATTERN.matcher(content)
            if (h1Matcher.find()) {
                title = h1Matcher.group(1)?.replace(Regex("<.*?>"), "")?.trim() ?: ""
            }
        }
        if (title.isBlank()) {
            title = fallbackTitle
        }

        // 2. Extract Subtitle & Author: Priority 1 -> BOOK_DATA.subtitle / BOOK_DATA.author, Priority 2 -> <meta>
        var subtitle = ""
        val bdSubtitleMatcher = BOOK_DATA_SUBTITLE_PATTERN.matcher(content)
        if (bdSubtitleMatcher.find()) {
            subtitle = bdSubtitleMatcher.group(1)?.trim() ?: ""
        }

        var description = ""
        val descMatcher = DESC_META_PATTERN.matcher(content)
        if (descMatcher.find()) {
            description = descMatcher.group(1)?.trim() ?: ""
        }
        if (subtitle.isBlank() && description.isNotBlank()) {
            subtitle = description
        }

        var author = ""
        val bdAuthorMatcher = BOOK_DATA_AUTHOR_PATTERN.matcher(content)
        if (bdAuthorMatcher.find()) {
            author = bdAuthorMatcher.group(1)?.trim() ?: ""
        }
        if (author.isBlank()) {
            val authorMatcher = AUTHOR_META_PATTERN.matcher(content)
            if (authorMatcher.find()) {
                author = authorMatcher.group(1)?.trim() ?: ""
            }
        }

        // 3. Direction & Language from <html dir="..." lang="...">
        var dirAttr: String? = null
        var langAttr: String? = null
        val htmlTagMatcher = HTML_TAG_PATTERN.matcher(content)
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

        // 4. Extract Page Count & TOC from BOOK_DATA.pages or HTML structure
        val tocList = mutableListOf<TocEntry>()
        var pageCount = extractBookDataPagesCountAndToc(content, tocList)

        if (tocList.isEmpty()) {
            val headingMatcher = HEADING_PATTERN.matcher(content)
            var count = 0
            while (headingMatcher.find() && count < 50) {
                val tag = headingMatcher.group(1)?.lowercase() ?: "h1"
                val id = headingMatcher.group(2)
                val headingText = headingMatcher.group(3)?.replace(Regex("<.*?>"), "")?.trim() ?: ""
                if (headingText.isNotBlank()) {
                    val level = when (tag) {
                        "h1" -> 1
                        "h2" -> 2
                        else -> 3
                    }
                    val href = if (!id.isNullOrBlank()) "#$id" else ""
                    tocList.add(TocEntry(title = headingText, href = href, level = level))
                    count++
                }
            }
        }

        if (pageCount <= 0) {
            // Fallback: count .page / <section / <article elements or estimate by length
            val pageClassCount = Regex("class\\s*=\\s*[\"'][^\"']*\\bpage\\b[^\"']*[\"']", RegexOption.IGNORE_CASE).findAll(content).count()
            val sectionCount = Regex("<section\\b", RegexOption.IGNORE_CASE).findAll(content).count()
            pageCount = when {
                pageClassCount > 0 -> pageClassCount
                sectionCount > 1 -> sectionCount
                tocList.size > 1 -> tocList.size
                else -> maxOf(1, (content.length / 1800))
            }
        }

        // 5. Extract CSS :root Colors (--accent-rose, --accent-gold, --bg-primary)
        val accentRoseHex = extractCssVar(content, ACCENT_ROSE_PATTERN) ?: "#E11D48"
        val accentGoldHex = extractCssVar(content, ACCENT_GOLD_PATTERN) ?: "#D97706"
        val bgPrimaryHex = extractCssVar(content, BG_PRIMARY_PATTERN) ?: "#1E293B"

        val startColor = parseHexToColorLong(bgPrimaryHex, 0xFF1E293B)
        val endColor = parseHexToColorLong(accentRoseHex, darkenColorLong(startColor))

        val hasExternalCss = CSS_LINK_PATTERN.matcher(content).find() || STYLE_TAG_PATTERN.matcher(content).find()
        val hasExternalJs = SCRIPT_PATTERN.matcher(content).find()

        return ParsedHtmlMetadata(
            title = title,
            subtitle = subtitle,
            author = author,
            description = if (description.isNotBlank()) description else subtitle,
            language = language,
            isRtl = isRtl,
            pageCount = pageCount,
            accentRoseHex = accentRoseHex,
            accentGoldHex = accentGoldHex,
            bgPrimaryHex = bgPrimaryHex,
            coverGradientStart = startColor,
            coverGradientEnd = endColor,
            hasExternalCss = hasExternalCss,
            hasExternalJs = hasExternalJs,
            tableOfContents = tocList
        )
    }

    private fun extractBookDataPagesCountAndToc(content: String, tocOut: MutableList<TocEntry>): Int {
        // Locate `pages: [` inside BOOK_DATA
        val pagesStartMatch = Regex("pages\\s*:\\s*\\[").find(content) ?: return 0
        val arrayStartIndex = pagesStartMatch.range.last // index of '['
        var bracketDepth = 0
        var inString = false
        var stringChar = ' '
        var escaped = false
        var topLevelObjectCount = 0
        var objectDepth = 0
        var currentObjectStart = -1

        for (i in arrayStartIndex until content.length) {
            val c = content[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (c == '\\') {
                escaped = true
                continue
            }
            if (inString) {
                if (c == stringChar) {
                    inString = false
                }
                continue
            }
            if (c == '"' || c == '\'' || c == '`') {
                inString = true
                stringChar = c
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
                        val objSlice = content.substring(currentObjectStart, i + 1)
                        val pageTitleMatch = Regex("(?:title|sectionTitle|chapterTag|chapterNumber|heading)\\s*:\\s*[\"']([^\"']+)[\"']").find(objSlice)
                        val pageTitle = pageTitleMatch?.groupValues?.get(1)?.trim()
                        if (!pageTitle.isNullOrBlank() && tocOut.size < 100) {
                            tocOut.add(
                                TocEntry(
                                    title = pageTitle,
                                    href = "#page-$topLevelObjectCount",
                                    level = 1
                                )
                            )
                        }
                        currentObjectStart = -1
                    }
                }
            }
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
