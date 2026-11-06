package com.maktabati.app.data.converter

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.maktabati.app.data.converter.extractor.DocumentTextExtractor
import com.maktabati.app.data.converter.model.ConvertedPageItem
import com.maktabati.app.data.converter.model.ConvertibleBookProposal
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.paginator.SemanticBookPaginator
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.converter.theme.BookVisualTheme
import java.io.File
import java.io.FileInputStream
import java.util.UUID

/**
 * Clean Facade for the Maktabati Offline Document-to-Luxury-HTML5 Book Converter Engine.
 * Delegates extraction, Arabic fidelity normalization, semantic pagination, and luxury HTML5 rendering
 * to dedicated modular components under `com.maktabati.app.data.converter.*`.
 */
object SmartBookConverter {

    private const val DISK_STAGE_THRESHOLD_CHARS = 80_000

    val SUPPORTED_TEXT_EXTENSIONS = setOf(
        "txt", "md", "markdown", "docx", "odt", "rtf", "epub"
    )

    val SUPPORTED_CONVERTIBLE_EXTENSIONS = SUPPORTED_TEXT_EXTENSIONS

    fun isConvertibleFileName(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return ext in SUPPORTED_TEXT_EXTENSIONS
    }

    fun isConvertibleMimeType(mimeType: String?): Boolean {
        if (mimeType.isNullOrBlank()) return false
        val m = mimeType.lowercase()
        return m == "text/plain" ||
            m == "text/markdown" ||
            m == "text/x-markdown" ||
            m == "application/rtf" ||
            m == "text/rtf" ||
            m == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            m == "application/vnd.oasis.opendocument.text" ||
            m == "application/epub+zip"
    }

    fun detectFormatBadge(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase().trim()
        return when (ext) {
            "docx" -> "DOCX • وورد حديث"
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
        return inspectDocumentBytes(
            bytes = bytes,
            originalFileName = fileName,
            uri = uri,
            stagingCacheDir = context.cacheDir
        )
    }

    fun inspectDocumentFile(file: File): ConvertibleBookProposal {
        val bytes = FileInputStream(file).use { it.readBytes() }
        return inspectDocumentBytes(
            bytes = bytes,
            originalFileName = file.name,
            uri = Uri.fromFile(file),
            stagingCacheDir = file.parentFile
        )
    }

    fun inspectDocumentBytes(
        bytes: ByteArray,
        originalFileName: String,
        uri: Uri? = null,
        stagingCacheDir: File? = null
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
        val words = ArabicTextNormalizer.countWordsFast(normalizedBody)
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = ArabicTextNormalizer.suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)
        val previewExcerpt = ArabicTextNormalizer.extractPreviewExcerptFast(normalizedBody)

        var stagedFile: File? = null
        val retainedText = if (normalizedBody.length > DISK_STAGE_THRESHOLD_CHARS && stagingCacheDir != null) {
            runCatching {
                val stageDir = File(stagingCacheDir, "maktabati_text_stage").apply { mkdirs() }
                val tmp = File(stageDir, "stage_${UUID.randomUUID().toString().replace("-", "").take(12)}.txt")
                tmp.bufferedWriter(Charsets.UTF_8).use { it.write(normalizedBody) }
                stagedFile = tmp
                normalizedBody.substring(0, minOf(normalizedBody.length, 4096))
            }.getOrDefault(normalizedBody)
        } else {
            normalizedBody
        }

        return ConvertibleBookProposal(
            uri = uri,
            originalFileName = originalFileName,
            formatBadge = detectFormatBadge(originalFileName),
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = retainedText,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme,
            stagedTextFile = stagedFile,
            previewExcerpt = previewExcerpt
        )
    }

    fun inspectRawText(
        rawText: String,
        sourceLabel: String = "CLIPBOARD • نصوص الحافظة",
        preferredTitle: String? = null,
        preferredAuthor: String? = null,
        stagingCacheDir: File? = null
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

        val words = ArabicTextNormalizer.countWordsFast(normalizedBody)
        val estimatedPages = ((words / 100) + 2).coerceAtLeast(2)
        val recommendedTheme = ArabicTextNormalizer.suggestThemeForContent(semanticMeta.first, normalizedBody, isRtl)
        val previewExcerpt = ArabicTextNormalizer.extractPreviewExcerptFast(normalizedBody)

        var stagedFile: File? = null
        val retainedText = if (normalizedBody.length > DISK_STAGE_THRESHOLD_CHARS && stagingCacheDir != null) {
            runCatching {
                val stageDir = File(stagingCacheDir, "maktabati_text_stage").apply { mkdirs() }
                val tmp = File(stageDir, "stage_${UUID.randomUUID().toString().replace("-", "").take(12)}.txt")
                tmp.bufferedWriter(Charsets.UTF_8).use { it.write(normalizedBody) }
                stagedFile = tmp
                normalizedBody.substring(0, minOf(normalizedBody.length, 4096))
            }.getOrDefault(normalizedBody)
        } else {
            normalizedBody
        }

        return ConvertibleBookProposal(
            uri = null,
            originalFileName = if (isRtl) "نص_الحافظة.txt" else "clipboard_text.txt",
            formatBadge = sourceLabel,
            detectedTitle = semanticMeta.first,
            detectedSubtitle = semanticMeta.second,
            detectedAuthor = semanticMeta.third,
            rawExtractedText = retainedText,
            isRtl = isRtl,
            language = language,
            wordCount = words,
            estimatedPages = estimatedPages,
            recommendedTheme = recommendedTheme,
            stagedTextFile = stagedFile,
            previewExcerpt = previewExcerpt
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
        return try {
            convertProposalToLuxuryHtml(
                proposal = proposal,
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                customAuthor = customAuthor,
                theme = theme ?: proposal.recommendedTheme
            )
        } finally {
            proposal.cleanupStagedFile()
        }
    }

    fun buildProposalPages(
        proposal: ConvertibleBookProposal,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme = proposal.recommendedTheme
    ): List<ConvertedPageItem> {
        val finalTitle = customTitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedTitle
        val finalSubtitle = customSubtitle?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedSubtitle
        val finalAuthor = customAuthor?.trim()?.takeIf { it.isNotEmpty() } ?: proposal.detectedAuthor
        val fullText = proposal.resolveFullExtractedText()

        return SemanticBookPaginator.buildSemanticPages(
            rawText = fullText,
            bookTitle = finalTitle,
            bookSubtitle = finalSubtitle,
            bookAuthor = finalAuthor,
            theme = theme,
            isRtl = proposal.isRtl
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

        val pages = buildProposalPages(
            proposal = proposal,
            customTitle = finalTitle,
            customSubtitle = finalSubtitle,
            customAuthor = finalAuthor,
            theme = theme
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
