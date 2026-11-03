package com.maktabati.app.data.converter.extractor

import com.maktabati.app.data.converter.model.ExtractedDocumentData
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * PDF (.pdf) Text Stream & ToUnicode CMap Extractor.
 * Extracts Arabic and English text from uncompressed and FlateDecode-compressed PDF streams,
 * and detects Scanned Image PDFs to display a polite, clear user message.
 */
object PdfStreamExtractor {

    private val WHITESPACE_REGEX = Regex("\\s+")
    private val BF_CHAR_BLOCK_REGEX = Regex("beginbfchar(.*?)endbfchar", RegexOption.DOT_MATCHES_ALL)
    private val HEX_PAIR_REGEX = Regex("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>")
    private val BF_RANGE_BLOCK_REGEX = Regex("beginbfrange(.*?)endbfrange", RegexOption.DOT_MATCHES_ALL)
    private val HEX_TRIPLE_REGEX = Regex("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>")
    private val BT_ET_BLOCK_REGEX = Regex("\\bBT\\b(.*?)\\bET\\b", RegexOption.DOT_MATCHES_ALL)
    private val PDF_TEXT_TOKEN_REGEX =
        Regex("\\((?:\\\\.|[^\\\\()])*\\)|<[0-9A-Fa-f\\s]+>|\\b(Td|TD|T\\*|'|\"|Tj|TJ)\\b")
    private val PDF_TITLE_LITERAL_REGEX = Regex("/Title\\s*\\(((?:\\\\.|[^\\\\()])+)\\)")
    private val PDF_TITLE_HEX_REGEX = Regex("/Title\\s*<([0-9A-Fa-f\\s]+)>")
    private val PDF_AUTHOR_LITERAL_REGEX = Regex("/Author\\s*\\(((?:\\\\.|[^\\\\()])+)\\)")
    private val PDF_AUTHOR_HEX_REGEX = Regex("/Author\\s*<([0-9A-Fa-f\\s]+)>")

    fun extractFromPdf(bytes: ByteArray): ExtractedDocumentData {
        val rawLatin1 = String(bytes, Charsets.ISO_8859_1)

        val explicitTitle = extractPdfMetadataField(rawLatin1, "Title")
        val explicitAuthor = extractPdfMetadataField(rawLatin1, "Author")

        val decodedStreams = mutableListOf<String>()
        val streamMarker = "stream"
        val endStreamMarker = "endstream"

        var pos = 0
        while (pos < rawLatin1.length) {
            val sIdx = rawLatin1.indexOf(streamMarker, pos)
            if (sIdx == -1) break
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
                val decompressedBytes = tryInflatePdfStream(slice) ?: slice
                decodedStreams.add(String(decompressedBytes, Charsets.ISO_8859_1))
            }
            pos = eIdx + endStreamMarker.length
        }

        if (decodedStreams.isEmpty()) {
            decodedStreams.add(rawLatin1)
        }

        val cmapTable = mutableMapOf<Int, String>()
        for (streamText in decodedStreams) {
            if (streamText.contains("beginbfchar") || streamText.contains("beginbfrange")) {
                parsePdfToUnicodeCMap(streamText, cmapTable)
            }
        }

        val extractedLines = StringBuilder()
        for (streamText in decodedStreams) {
            if (streamText.contains("beginbfchar") || streamText.contains("beginbfrange")) continue
            extractPdfTextBlocksFromStream(streamText, cmapTable, extractedLines)
        }

        val finalBody = extractedLines.toString()
            .lines()
            .map { it.replace(WHITESPACE_REGEX, " ").trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
            .trim()

        val wordCount = finalBody.split(WHITESPACE_REGEX).count { it.length >= 2 }
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
        for (block in BF_CHAR_BLOCK_REGEX.findAll(streamText)) {
            for (pair in HEX_PAIR_REGEX.findAll(block.groupValues[1])) {
                val src = pair.groupValues[1].toIntOrNull(16) ?: continue
                val dst = decodeUtf16BeHex(pair.groupValues[2])
                if (dst.isNotEmpty()) {
                    cmap[src] = dst
                }
            }
        }

        for (block in BF_RANGE_BLOCK_REGEX.findAll(streamText)) {
            for (rng in HEX_TRIPLE_REGEX.findAll(block.groupValues[1])) {
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
        val blocks = BT_ET_BLOCK_REGEX.findAll(streamText).toList()
        val targetTexts = if (blocks.isNotEmpty()) blocks.map { it.groupValues[1] } else listOf(streamText)

        for (block in targetTexts) {
            val lineBuf = StringBuilder()
            for (match in PDF_TEXT_TOKEN_REGEX.findAll(block)) {
                val tok = match.value
                when {
                    tok.startsWith("(") && tok.endsWith(")") -> {
                        val inner = tok.substring(1, tok.length - 1)
                        lineBuf.append(decodePdfLiteralString(inner, cmap))
                    }
                    tok.startsWith("<") && tok.endsWith(">") -> {
                        val hex = tok.substring(1, tok.length - 1).replace(WHITESPACE_REGEX, "")
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
            if (validCount > 0) utf16 else DocumentTextExtractor.decodeTextBytesSmart(bytes)
        } else {
            DocumentTextExtractor.decodeTextBytesSmart(bytes)
        }
    }

    private fun extractPdfMetadataField(rawPdf: String, fieldName: String): String? {
        val literalRegex = if (fieldName == "Title") PDF_TITLE_LITERAL_REGEX else PDF_AUTHOR_LITERAL_REGEX
        val hexRegex = if (fieldName == "Title") PDF_TITLE_HEX_REGEX else PDF_AUTHOR_HEX_REGEX

        val literalMatch = literalRegex.find(rawPdf)
        if (literalMatch != null) {
            val decoded = decodePdfLiteralString(literalMatch.groupValues[1], emptyMap()).trim()
            if (decoded.length >= 2) return decoded
        }
        val hexMatch = hexRegex.find(rawPdf)
        if (hexMatch != null) {
            val decoded = decodePdfHexString(hexMatch.groupValues[1].replace(WHITESPACE_REGEX, ""), emptyMap()).trim()
            if (decoded.length >= 2) return decoded
        }
        return null
    }
}
