package com.maktabati.app.data.importer

/**
 * Detects complete standalone HTML5 interactive books and merges two-phase (Phase 1 + Phase 2)
 * split HTML5 books into a single valid, self-contained HTML5 document.
 */
object SmartHtmlPhaseMerger {

    private val LEADING_CODE_FENCE_REGEX = Regex("^\\s*```(?:html|HTML)?\\s*\r?\n?")
    private val TRAILING_CODE_FENCE_REGEX = Regex("\r?\n?\\s*```\\s*$")

    private val TRAILING_PHASE1_COMMENT_REGEX = Regex(
        """(?s)(?:/\*[^*]*\*+(?:[^/*][^*]*\*+)*/\s*)+$"""
    )
    private val LEADING_PHASE2_COMMENT_REGEX = Regex(
        """(?s)^(?:\s*/\*[^*]*\*+(?:[^/*][^*]*\*+)*/)+"""
    )
    private val TOTAL_PAGES_PROP_REGEX = Regex(
        """(["']?\btotalPages\b["']?\s*:\s*)(\d+)"""
    )

    fun unwrapMarkdownCodeFence(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst(LEADING_CODE_FENCE_REGEX, "")
            cleaned = cleaned.replaceFirst(TRAILING_CODE_FENCE_REGEX, "")
        }
        return cleaned.trim()
    }

    fun isCompleteReadyHtmlBook(raw: String): Boolean {
        val cleaned = unwrapMarkdownCodeFence(raw)
        if (cleaned.length < 60) return false

        val headProbe = cleaned.take(512).trimStart().lowercase()
        val tailProbe = cleaned.takeLast(512).trimEnd().lowercase()

        val startsAsHtmlDoc = headProbe.startsWith("<!doctype html") || headProbe.startsWith("<html")
        val endsAsHtmlDoc = tailProbe.endsWith("</html>")
        if (!startsAsHtmlDoc || !endsAsHtmlDoc) return false

        val lowerSample = if (cleaned.length > 35_000) {
            (cleaned.take(25_000) + "\n" + cleaned.takeLast(10_000)).lowercase()
        } else {
            cleaned.lowercase()
        }
        return lowerSample.contains("book_data") || lowerSample.contains("<body")
    }

    fun isIncompletePhase1HtmlBook(raw: String): Boolean {
        val cleaned = unwrapMarkdownCodeFence(raw)
        if (cleaned.length < 60) return false

        val headProbe = cleaned.take(512).trimStart().lowercase()
        val tailProbe = cleaned.takeLast(512).trimEnd().lowercase()

        val startsAsHtmlOrBookData = headProbe.startsWith("<!doctype html") ||
            headProbe.startsWith("<html") ||
            cleaned.contains("BOOK_DATA")

        val endsWithHtmlClosing = tailProbe.endsWith("</html>")
        val hasPhase1Marker = tailProbe.contains("نهاية المرحلة 1") ||
            tailProbe.contains("end of phase 1")

        return startsAsHtmlOrBookData && (!endsWithHtmlClosing || hasPhase1Marker)
    }

    fun isPhase2HtmlContinuation(raw: String): Boolean {
        val cleaned = unwrapMarkdownCodeFence(raw)
        if (cleaned.length < 30) return false

        val headProbe = cleaned.take(512).trimStart().lowercase()
        val tailProbe = cleaned.takeLast(512).trimEnd().lowercase()

        if (headProbe.startsWith("<!doctype html") || headProbe.startsWith("<html")) {
            return false
        }
        if (!tailProbe.endsWith("</html>")) {
            return false
        }

        val lower = cleaned.lowercase()
        return lower.contains("</script>") ||
            lower.contains("</body>") ||
            lower.contains("html:") ||
            lower.contains("\"html\"") ||
            lower.contains("'html'")
    }

    fun canMergeAsHtmlPhases(firstText: String, secondText: String): Boolean {
        return isIncompletePhase1HtmlBook(firstText) && isPhase2HtmlContinuation(secondText)
    }

    fun mergeTwoHtmlPhases(phase1Raw: String, phase2Raw: String): String {
        var part1 = unwrapMarkdownCodeFence(phase1Raw)
        var part2 = unwrapMarkdownCodeFence(phase2Raw)

        // Strip trailing block comments (such as /* === [نهاية المرحلة 1...] === */) from Phase 1
        part1 = part1.replace(TRAILING_PHASE1_COMMENT_REGEX, "").trimEnd()
        // Also strip any trailing single-line comment at the very end of Phase 1
        while (part1.lines().lastOrNull()?.trim()?.startsWith("//") == true) {
            part1 = part1.substringBeforeLast("\n").trimEnd()
        }

        // Strip leading block comments (such as /* === [بداية المرحلة 2...] === */) from Phase 2
        part2 = part2.replace(LEADING_PHASE2_COMMENT_REGEX, "").trimStart()
        while (part2.lines().firstOrNull()?.trim()?.startsWith("//") == true) {
            part2 = part2.substringAfter("\n").trimStart()
        }

        part1 = part1.trimEnd().trimEnd(',').trimEnd()
        part2 = part2.trimStart().trimStart(',').trimStart()

        val separator = when {
            part1.endsWith("}") && part2.startsWith("{") -> ",\n"
            part1.endsWith("[") && part2.startsWith("{") -> "\n"
            part2.startsWith("]") -> "\n"
            else -> ",\n"
        }

        val merged = part1 + separator + part2

        // Update BOOK_DATA.totalPages if present so it matches the combined page count
        return runCatching {
            val meta = HtmlBookParser.parseQuickHeader(merged, "كتاب مدمج")
            if (meta.pageCount > 0 && TOTAL_PAGES_PROP_REGEX.containsMatchIn(merged)) {
                merged.replaceFirst(TOTAL_PAGES_PROP_REGEX, "$1${meta.pageCount}")
            } else {
                merged
            }
        }.getOrDefault(merged)
    }
}
