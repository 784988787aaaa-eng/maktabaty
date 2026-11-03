package com.maktabati.app.data.sandbox

import com.maktabati.app.data.typography.BookTypographyProfile
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import java.io.File

/**
 * Coordinates HTML header cleanup and injects the `<style id="maktabati-perf-booster">`
 * and `<script id="maktabati-storage-isolation">` blocks right before `</head>`.
 */
object SandboxHtmlInjector {

    private val DVH_UNIT_REGEX = Regex("(\\d+(?:\\.\\d+)?)[dsl]vh\\b", RegexOption.IGNORE_CASE)
    private val VIEWPORT_META_REGEX = Regex("<meta\\b[^>]*name=[\"']viewport[\"'][^>]*>", RegexOption.IGNORE_CASE)
    private val PINCH_ZOOM_CSS_REGEX = Regex("touch-action\\s*:\\s*pan-y\\s+pinch-zoom", RegexOption.IGNORE_CASE)
    private val HEAD_OPEN_TAG_REGEX = Regex("<head[^>]*>", RegexOption.IGNORE_CASE)
    private const val LOCKED_VIEWPORT_META =
        """<meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover"/>"""

    fun buildInjectedSnippet(
        bookId: String,
        pageCount: Int,
        typographyProfile: BookTypographyProfile? = null
    ): String {
        val styleBlock = SandboxCssBuilder.buildPerfBoosterStyleBlock(typographyProfile)
        val scriptBlock = SandboxJsRuntimeBuilder.buildStorageIsolationAndRuntimeScript(bookId, pageCount)
        return "$styleBlock\n$scriptBlock"
    }

    private fun stripElementById(html: String, tagName: String, idPrefix: String): String {
        val idIndex = html.indexOf(idPrefix, ignoreCase = true)
        if (idIndex < 0) return html
        val openStart = html.lastIndexOf("<$tagName", idIndex, ignoreCase = true)
        if (openStart < 0) return html
        val closeTag = "</$tagName>"
        val closeEnd = html.indexOf(closeTag, idIndex, ignoreCase = true)
        if (closeEnd < 0) return html
        return html.substring(0, openStart) + html.substring(closeEnd + closeTag.length)
    }

    fun injectIntoHtml(
        rawHtml: String,
        bookId: String,
        pageCount: Int,
        bookDir: File? = null,
        typographyProfile: BookTypographyProfile? = null
    ): String {
        var cleanedHtml = stripElementById(rawHtml, "style", "maktabati-perf-booster")
        cleanedHtml = stripElementById(cleanedHtml, "script", "maktabati-storage-isolation")

        val headEndIdx = cleanedHtml.indexOf("</head>", ignoreCase = true)
        if (headEndIdx > 0) {
            var headPart = cleanedHtml.substring(0, headEndIdx)
                .replace(DVH_UNIT_REGEX, "$1vh")
                .replace(PINCH_ZOOM_CSS_REGEX, "touch-action:pan-y")
            headPart = if (VIEWPORT_META_REGEX.containsMatchIn(headPart)) {
                headPart.replace(VIEWPORT_META_REGEX, LOCKED_VIEWPORT_META)
            } else {
                headPart + "\n" + LOCKED_VIEWPORT_META
            }
            cleanedHtml = headPart + cleanedHtml.substring(headEndIdx)
        } else {
            cleanedHtml = cleanedHtml
                .replace(DVH_UNIT_REGEX, "$1vh")
                .replace(PINCH_ZOOM_CSS_REGEX, "touch-action:pan-y")
                .replace(VIEWPORT_META_REGEX, LOCKED_VIEWPORT_META)
        }

        val resolvedTypography = typographyProfile
            ?: bookDir?.let { MaktabatiFontRegistry.parseManifestTypography(File(it, "manifest.json")) }
            ?: if (!cleanedHtml.contains("--font-body")) BookTypographyProfile() else null

        val snippet = buildInjectedSnippet(
            bookId = bookId,
            pageCount = pageCount,
            typographyProfile = resolvedTypography
        )

        val targetHeadClose = cleanedHtml.indexOf("</head>", ignoreCase = true)
        if (targetHeadClose >= 0) {
            return cleanedHtml.substring(0, targetHeadClose) + "\n" + snippet + "\n" + cleanedHtml.substring(targetHeadClose)
        }

        val headOpenMatch = HEAD_OPEN_TAG_REGEX.find(cleanedHtml)
        return if (headOpenMatch != null) {
            val p = headOpenMatch.range.last + 1
            cleanedHtml.substring(0, p) + "\n" + snippet + "\n" + cleanedHtml.substring(p)
        } else {
            snippet + "\n" + cleanedHtml
        }
    }
}
