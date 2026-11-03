package com.maktabati.app.data.converter

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.maktabati.app.data.converter.extractor.DocumentTextExtractor
import com.maktabati.app.data.converter.model.BookTemplateCategory
import com.maktabati.app.data.converter.model.ConvertibleBookProposal
import com.maktabati.app.data.converter.model.PublishingLayoutStyle
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.paginator.SemanticBookPaginator
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.converter.theme.BookVisualTheme
import java.io.File
import java.io.FileInputStream

/**
 * Clean Facade for the Maktabati Offline Document-to-Luxury-HTML5 Book Converter Engine.
 * Delegates extraction, Arabic fidelity normalization, semantic pagination, and luxury HTML5 rendering
 * to dedicated modular components under `com.maktabati.app.data.converter.*`.
 */
object SmartBookConverter {

    val SUPPORTED_CONVERTIBLE_EXTENSIONS = setOf(
        "txt", "md", "markdown", "doc", "docx", "odt", "rtf", "epub", "pdf"
    )

    private val WHITESPACE_REGEX = Regex("\\s+")

    fun isConvertibleFileName(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return ext in SUPPORTED_CONVERTIBLE_EXTENSIONS
    }

    fun isConvertibleMimeType(mimeType: String?): Boolean {
        if (mimeType.isNullOrBlank()) return false
        val m = mimeType.lowercase()
        return m == "text/plain" ||
            m == "text/markdown" ||
            m == "text/x-markdown" ||
            m == "application/rtf" ||
            m == "text/rtf" ||
            m == "application/msword" ||
            m == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            m == "application/vnd.oasis.opendocument.text" ||
            m == "application/epub+zip" ||
            m == "application/pdf"
    }

    fun detectFormatBadge(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return when (ext) {
            "docx" -> "DOCX • وورد حديث"
            "doc" -> "DOC • وورد كلاسيكي"
            "pdf" -> "PDF • مستند رقمي"
            "epub" -> "EPUB • كتاب إلكتروني"
            "odt" -> "ODT • مستند مفتوح"
            "rtf" -> "RTF • نص منسق"
            "md", "markdown" -> "MD • ماركداون"
            else -> "TXT • ملف نصي"
        }
    }

    fun inspectDocumentUri(context: Context, uri: Uri): ConvertibleBookProposal {
        val fileName = queryFileName(context, uri) ?: "كتاب_محول.txt"
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف من الذاكرة")
        return inspectDocumentBytes(bytes, fileName, uri)
    }

    fun inspectDocumentFile(file: File): ConvertibleBookProposal {
        val bytes = FileInputStream(file).use { it.readBytes() }
        return inspectDocumentBytes(bytes, file.name, Uri.fromFile(file))
    }

    fun inspectDocumentBytes(
        bytes: ByteArray,
        originalFileName: String,
        uri: Uri? = null
    ): ConvertibleBookProposal {
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على نصوص")
        }

        val extracted = DocumentTextExtractor.extractStructuredDocument(bytes, originalFileName)
        val normalizedBody = ArabicTextNormalizer.normalizeArabicFidelity(extracted.bodyText)

        if (normalizedBody.isBlank()) {
            throw IllegalArgumentException("لم يتم العثور على نصوص قابلة للقراءة داخل الملف ($originalFileName)")
        }

        val fallbackTitle = originalFileName
            .substringBeforeLast('.')
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .ifBlank { "كتاب تفاعلي" }

        val semanticMeta = ArabicTextNormalizer.extractTitleSubtitleAndAuthor(
            rawText = normalizedBody,
            explicitTitle = extracted.explicitTitle,
            explicitAuthor = extracted.explicitAuthor,
            fallbackTitle = fallbackTitle
        )

        val (isRtl, language) = ArabicTextNormalizer.detectLanguageAndDirection(normalizedBody)
        val words = normalizedBody.split(WHITESPACE_REGEX).count { it.isNotBlank() }
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = ArabicTextNormalizer.suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)

        return ConvertibleBookProposal(
            uri = uri,
            originalFileName = originalFileName,
            formatBadge = detectFormatBadge(originalFileName),
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = normalizedBody,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme
        )
    }

    fun inspectRawText(
        rawText: String,
        sourceLabel: String = "CLIPBOARD • نصوص الحافظة",
        preferredTitle: String? = null,
        preferredAuthor: String? = null
    ): ConvertibleBookProposal {
        val normalizedBody = ArabicTextNormalizer.normalizeArabicFidelity(rawText)
        if (normalizedBody.isBlank()) {
            throw IllegalArgumentException("النص المدخل فارغ. يرجى لصق أو كتابة نص لتحويله إلى كتاب.")
        }

        val (isRtl, language) = ArabicTextNormalizer.detectLanguageAndDirection(normalizedBody)
        val fallbackTitle = if (isRtl) "إشراقات من الحافظة" else "Clipboard Anthology"

        val semanticMeta = ArabicTextNormalizer.extractTitleSubtitleAndAuthor(
            rawText = normalizedBody,
            explicitTitle = preferredTitle?.takeIf { it.isNotBlank() },
            explicitAuthor = preferredAuthor?.takeIf { it.isNotBlank() },
            fallbackTitle = fallbackTitle
        )

        val words = normalizedBody.split(WHITESPACE_REGEX).count { it.isNotBlank() }
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = ArabicTextNormalizer.suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)

        return ConvertibleBookProposal(
            uri = null,
            originalFileName = if (isRtl) "نص_الحافظة.txt" else "clipboard_text.txt",
            formatBadge = sourceLabel,
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = normalizedBody,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme
        )
    }

    fun convertRawTextToLuxuryHtml(
        rawText: String,
        title: String? = null,
        subtitle: String? = null,
        author: String? = null,
        theme: BookVisualTheme = BookVisualTheme.ROSE_HEART,
        fallbackFileName: String = "كتاب_تفاعلي.txt"
    ): String {
        val cleanText = ArabicTextNormalizer.normalizeArabicFidelity(rawText)
        val fallbackTitle = fallbackFileName.substringBeforeLast('.').replace('_', ' ').trim().ifBlank { "كتاب تفاعلي" }
        val autoMeta = ArabicTextNormalizer.extractTitleSubtitleAndAuthor(
            rawText = cleanText,
            explicitTitle = title,
            explicitAuthor = author,
            fallbackTitle = fallbackTitle
        )

        val finalTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.first
        val finalSubtitle = subtitle?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.second
        val finalAuthor = author?.trim()?.takeIf { it.isNotEmpty() } ?: autoMeta.third
        val (isRtl, lang) = ArabicTextNormalizer.detectLanguageAndDirection(cleanText)

        val pages = SemanticBookPaginator.buildSemanticPages(
            rawText = cleanText,
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = isRtl
        )

        return LuxuryHtmlTemplateRenderer.renderCompleteLuxuryHtml(
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = isRtl,
            language = lang,
            pages = pages
        )
    }

    fun convertFileToLuxuryHtml(
        file: File,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme? = null
    ): String {
        val proposal = inspectDocumentFile(file)
        return convertProposalToLuxuryHtml(
            proposal = proposal,
            customTitle = customTitle,
            customSubtitle = customSubtitle,
            customAuthor = customAuthor,
            theme = theme ?: proposal.recommendedTheme
        )
    }

    fun convertProposalToLuxuryHtml(
        proposal: ConvertibleBookProposal,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme = proposal.recommendedTheme
    ): String {
        val finalTitle = customTitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedTitle
        val finalSubtitle = customSubtitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedSubtitle
        val finalAuthor = customAuthor?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedAuthor

        val pages = SemanticBookPaginator.buildSemanticPages(
            rawText = proposal.rawExtractedText,
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = proposal.isRtl
        )

        return LuxuryHtmlTemplateRenderer.renderCompleteLuxuryHtml(
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = proposal.isRtl,
            language = proposal.language,
            pages = pages
        )
    }

    fun decodeTextBytesSmart(bytes: ByteArray): String = DocumentTextExtractor.decodeTextBytesSmart(bytes)

    fun normalizeAndCleanExtractedText(input: String): String = ArabicTextNormalizer.normalizeArabicFidelity(input)

    fun normalizeArabicFidelity(input: String): String = ArabicTextNormalizer.normalizeArabicFidelity(input)

    fun detectLanguageAndDirection(text: String): Pair<Boolean, String> =
        ArabicTextNormalizer.detectLanguageAndDirection(text)

    fun suggestThemeForContent(title: String, body: String, isRtl: Boolean): BookVisualTheme =
        ArabicTextNormalizer.suggestThemeForContent(title, body, isRtl)

    private fun queryFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") {
            return uri.lastPathSegment
        }
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        return cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {
            }
        }
        return uri.lastPathSegment
    }
}
