package com.maktabati.app.data.converter.extractor

import android.util.Xml
import com.maktabati.app.data.converter.model.ExtractedDocumentData
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

/**
 * Multi-format document text & metadata extractor supporting TXT, MD, DOCX, DOC, ODT, RTF, EPUB, and PDF.
 */
object DocumentTextExtractor {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val OPF_DC_TITLE_REGEX = Regex("<dc:title[^>]*>([^<]+)</dc:title>", RegexOption.IGNORE_CASE)
    private val OPF_DC_CREATOR_REGEX = Regex("<dc:creator[^>]*>([^<]+)</dc:creator>", RegexOption.IGNORE_CASE)
    private val OPF_ITEM_REGEX = Regex("<item\\b([^>]+)/?>", RegexOption.IGNORE_CASE)
    private val ATTR_ID_REGEX = Regex("\\bid\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
    private val ATTR_HREF_REGEX = Regex("\\bhref\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)
    private val OPF_ITEMREF_REGEX = Regex("<itemref\\b[^>]*\\bidref\\s*=\\s*[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE)

    private val HTML_NON_CONTENT_TAG_REGEX = Regex("(?is)<(script|style|head|nav)[^>]*>.*?</\\1>")
    private val HTML_H1_REGEX = Regex("(?is)<h1[^>]*>(.*?)</h1>")
    private val HTML_H2_REGEX = Regex("(?is)<h2[^>]*>(.*?)</h2>")
    private val HTML_H3_TO_6_REGEX = Regex("(?is)<h[3-6][^>]*>(.*?)</h[3-6]>")
    private val HTML_BLOCK_END_REGEX = Regex("(?is)</(p|div|article|section|blockquote|li|tr)>")
    private val HTML_BR_REGEX = Regex("(?is)<br\\s*/?>")
    private val HTML_ANY_TAG_REGEX = Regex("<[^>]+>")
    private val HTML_DEC_ENTITY_REGEX = Regex("&#(\\d+);")
    private val HTML_HEX_ENTITY_REGEX = Regex("&#x([0-9a-fA-F]+);")

    fun extractStructuredDocument(bytes: ByteArray, fileName: String): ExtractedDocumentData {
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()

        val isZipMagic = bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            bytes[2] == 0x03.toByte() &&
            bytes[3] == 0x04.toByte()

        val isPdfMagic = bytes.size >= 5 &&
            bytes[0] == 0x25.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x44.toByte() &&
            bytes[3] == 0x46.toByte()

        val isRtfMagic = bytes.size >= 5 &&
            String(bytes, 0, minOf(10, bytes.size), Charsets.US_ASCII).trimStart().startsWith("{\\rtf")

        return when {
            ext == "docx" -> extractFromDocx(bytes)
            ext == "odt" -> extractFromOdt(bytes)
            ext == "epub" -> extractFromEpub(bytes)
            ext == "pdf" || isPdfMagic -> PdfStreamExtractor.extractFromPdf(bytes)
            ext == "rtf" || isRtfMagic -> extractFromRtf(bytes)
            ext == "doc" -> {
                if (isZipMagic) extractFromDocx(bytes) else extractFromClassicDoc(bytes)
            }
            isZipMagic -> detectAndExtractZipDocument(bytes)
            else -> extractFromPlainTextOrMarkdown(bytes)
        }
    }

    private fun extractFromPlainTextOrMarkdown(bytes: ByteArray): ExtractedDocumentData {
        val text = decodeTextBytesSmart(bytes)
        return ExtractedDocumentData(bodyText = text)
    }

    fun decodeTextBytesSmart(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }

        if (bytes.size >= 2 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }

        if (bytes.size >= 2 &&
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

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

        val utf8Text = String(bytes, Charsets.UTF_8)
        val replacementCount = utf8Text.count { it == '\uFFFD' }
        if (replacementCount == 0) {
            return utf8Text
        }

        val win1256 = runCatching { Charset.forName("windows-1256") }.getOrDefault(Charsets.UTF_8)
        return String(bytes, win1256)
    }

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

    private fun extractFromClassicDoc(bytes: ByteArray): ExtractedDocumentData {
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
            .map { it.replace(WHITESPACE_REGEX, " ").trim() }
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
        val words = text.split(WHITESPACE_REGEX).count { it.length >= 2 }
        return arabic * 3 + words
    }

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
                        next == '-' || next == '_' -> i++
                        next == '\n' || next == '\r' -> {
                            if (skipBelowDepth == -1) out.append("\n")
                            i++
                        }
                        next == '\'' -> {
                            val byteBuffer = ByteArrayOutputStream()
                            while (i < len && raw[i] == '\'' && i + 2 < len) {
                                val hex = raw.substring(i + 1, i + 3)
                                val bVal = hex.toIntOrNull(16)
                                if (bVal != null) {
                                    byteBuffer.write(bVal)
                                    i += 3
                                    if (i < len && raw[i] == '\\' && i + 1 < len && raw[i + 1] == '\'') {
                                        i++
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

                            val numStart = i
                            if (i < len && (raw[i] == '-' || raw[i].isDigit())) {
                                i++
                                while (i < len && raw[i].isDigit()) i++
                            }
                            val numParamStr = raw.substring(numStart, i)

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

        val opfEntry = entriesMap.entries.firstOrNull { it.key.endsWith(".opf", ignoreCase = true) }
        if (opfEntry != null) {
            val opfDir = opfEntry.key.substringBeforeLast('/', "")
            val opfText = String(opfEntry.value, Charsets.UTF_8)

            explicitTitle = OPF_DC_TITLE_REGEX.find(opfText)?.groupValues?.get(1)?.trim()
            explicitAuthor = OPF_DC_CREATOR_REGEX.find(opfText)?.groupValues?.get(1)?.trim()

            val manifestMap = mutableMapOf<String, String>()
            for (m in OPF_ITEM_REGEX.findAll(opfText)) {
                val attrs = m.groupValues[1]
                val id = ATTR_ID_REGEX.find(attrs)?.groupValues?.get(1)
                val href = ATTR_HREF_REGEX.find(attrs)?.groupValues?.get(1)
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

            for (m in OPF_ITEMREF_REGEX.findAll(opfText)) {
                val idref = m.groupValues[1]
                val path = manifestMap[idref]
                if (path != null) {
                    orderedChapterPaths.add(path)
                }
            }
        }

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
        val noScripts = html.replace(HTML_NON_CONTENT_TAG_REGEX, " ")
        val withHeadings = noScripts
            .replace(HTML_H1_REGEX) { "\n\n# ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(HTML_H2_REGEX) { "\n\n## ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(HTML_H3_TO_6_REGEX) { "\n\n### ${stripHtmlTags(it.groupValues[1])}\n\n" }
            .replace(HTML_BLOCK_END_REGEX, "\n\n")
            .replace(HTML_BR_REGEX, "\n")
        return decodeHtmlEntities(stripHtmlTags(withHeadings))
            .lines()
            .map { it.replace(WHITESPACE_REGEX, " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
    }

    private fun stripHtmlTags(input: String): String {
        return input.replace(HTML_ANY_TAG_REGEX, " ").replace(WHITESPACE_REGEX, " ").trim()
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
            .replace(HTML_DEC_ENTITY_REGEX) { m ->
                val code = m.groupValues[1].toIntOrNull()
                if (code != null) code.toChar().toString() else m.value
            }
            .replace(HTML_HEX_ENTITY_REGEX) { m ->
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
}
