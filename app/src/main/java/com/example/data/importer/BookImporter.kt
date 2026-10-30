package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.Book
import com.example.data.sandbox.SandboxHtmlInjector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object BookImporter {

    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        userCustomTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): Book = withContext(Dispatchers.IO) {
        val originalName = queryFileName(context, uri) ?: "كتاب_مستورد.html"
        val lowerName = originalName.lowercase()
        val isZip = lowerName.endsWith(".zip")
        val isHtmlExt = lowerName.endsWith(".html") || lowerName.endsWith(".htm") || lowerName.endsWith(".xhtml")

        if (!isZip && !isHtmlExt) {
            val mimeType = context.contentResolver.getType(uri)?.lowercase() ?: ""
            if (!mimeType.contains("html") && !mimeType.contains("zip") && !mimeType.contains("text")) {
                throw IllegalArgumentException("صيغة الملف ($originalName) غير مدعومة. يرجى اختيار ملف كتاب بصيغة .html أو .htm أو حزمة .zip")
            }
        }

        val bookId = "book_" + UUID.randomUUID().toString().replace("-", "").take(10)
        val booksRoot = File(context.filesDir, "books").apply { mkdirs() }
        val targetDir = File(booksRoot, bookId).apply { mkdirs() }

        var fileCount = 0
        var totalSize = 0L

        try {
            val fallbackName = userCustomTitle ?: originalName.substringBeforeLast(".")
            val relativeEntryPath: String
            val rawHtml: String

            if (isZip) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val stats = extractZipSafely(inputStream, targetDir)
                    fileCount = stats.first
                    totalSize = stats.second
                } ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف من الذاكرة")

                if (totalSize == 0L) {
                    throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على بيانات")
                }

                val entryFile = findEntryHtml(targetDir)
                    ?: throw IllegalArgumentException("لم يتم العثور على أي ملف HTML صالح داخل الحزمة المستوردة")

                relativeEntryPath = entryFile.relativeTo(targetDir).path
                rawHtml = entryFile.readText(Charsets.UTF_8)
                val quickMeta = HtmlBookParser.parseQuickHeader(rawHtml, fallbackName)
                val injectedHtml = SandboxHtmlInjector.injectIntoHtml(rawHtml, bookId, quickMeta.pageCount)
                entryFile.writeText(injectedHtml, Charsets.UTF_8)
            } else {
                // Single-pass in-memory read + inject + single disk write for instant Chrome-speed opening
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف من الذاكرة")

                if (bytes.isEmpty()) {
                    throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على بيانات")
                }

                fileCount = 1
                totalSize = bytes.size.toLong()
                relativeEntryPath = "index.html"
                rawHtml = String(bytes, Charsets.UTF_8)

                val quickMeta = HtmlBookParser.parseQuickHeader(rawHtml, fallbackName)
                val injectedHtml = SandboxHtmlInjector.injectIntoHtml(rawHtml, bookId, quickMeta.pageCount)
                val destFile = File(targetDir, "index.html")
                destFile.writeText(injectedHtml, Charsets.UTF_8)

                if (onQuickBookStaged != null) {
                    val quickTitle = if (!userCustomTitle.isNullOrBlank()) userCustomTitle else quickMeta.title
                    val stagedBook = Book(
                        id = bookId,
                        title = quickTitle,
                        subtitle = quickMeta.subtitle,
                        author = quickMeta.author,
                        description = quickMeta.description,
                        folderPath = bookId,
                        entryHtmlPath = relativeEntryPath,
                        shelfId = null,
                        isFavorite = false,
                        readingProgress = 0f,
                        scrollY = 0,
                        currentPage = 1,
                        pageCount = quickMeta.pageCount,
                        language = quickMeta.language,
                        isRtl = quickMeta.isRtl,
                        accentRoseHex = quickMeta.accentRoseHex,
                        accentGoldHex = quickMeta.accentGoldHex,
                        bgPrimaryHex = quickMeta.bgPrimaryHex,
                        coverGradientStart = quickMeta.coverGradientStart,
                        coverGradientEnd = quickMeta.coverGradientEnd,
                        coverIcon = if (quickMeta.hasExternalJs) "auto_stories" else "menu_book",
                        lastReadTimestamp = System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis(),
                        fileCount = fileCount,
                        totalSizeBytes = totalSize,
                        hasExternalCss = quickMeta.hasExternalCss,
                        hasExternalJs = quickMeta.hasExternalJs,
                        tableOfContentsJson = "[]"
                    )
                    onQuickBookStaged(stagedBook)
                }
            }

            val metadata = HtmlBookParser.parseContent(rawHtml, fallbackName, quickMode = false)
            val finalTitle = if (!userCustomTitle.isNullOrBlank()) userCustomTitle else metadata.title

            Book(
                id = bookId,
                title = finalTitle,
                subtitle = metadata.subtitle,
                author = metadata.author,
                description = metadata.description,
                folderPath = bookId,
                entryHtmlPath = relativeEntryPath,
                shelfId = null,
                isFavorite = false,
                readingProgress = 0f,
                scrollY = 0,
                currentPage = 1,
                pageCount = metadata.pageCount,
                language = metadata.language,
                isRtl = metadata.isRtl,
                accentRoseHex = metadata.accentRoseHex,
                accentGoldHex = metadata.accentGoldHex,
                bgPrimaryHex = metadata.bgPrimaryHex,
                coverGradientStart = metadata.coverGradientStart,
                coverGradientEnd = metadata.coverGradientEnd,
                coverIcon = if (metadata.hasExternalJs) "auto_stories" else "menu_book",
                lastReadTimestamp = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                fileCount = fileCount,
                totalSizeBytes = totalSize,
                hasExternalCss = metadata.hasExternalCss,
                hasExternalJs = metadata.hasExternalJs,
                tableOfContentsJson = HtmlBookParser.tocToJson(metadata.tableOfContents)
            )
        } catch (e: Exception) {
            if (targetDir.exists()) {
                targetDir.deleteRecursively()
            }
            throw e
        }
    }

    private fun extractZipSafely(inputStream: InputStream, destDir: File): Pair<Int, Long> {
        var count = 0
        var size = 0L
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val outFile = File(destDir, entry.name)
                val canonicalDest = destDir.canonicalPath
                val canonicalOut = outFile.canonicalPath
                if (!canonicalOut.startsWith(canonicalDest)) {
                    throw SecurityException("محاولة خروج غير مصرح بها عن مسار المجلد: ${entry.name}")
                }

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    FileOutputStream(outFile).use { fos ->
                        val bytes = zis.copyTo(fos)
                        size += bytes
                        count++
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return Pair(count, size)
    }

    private fun findEntryHtml(dir: File): File? {
        val candidates = listOf("index.html", "index.htm", "main.html", "book.html", "default.html")
        for (name in candidates) {
            val f = File(dir, name)
            if (f.exists() && f.isFile) return f
        }

        val allHtmlFiles = dir.walkTopDown().filter {
            it.isFile && (it.name.endsWith(".html", ignoreCase = true) || it.name.endsWith(".htm", ignoreCase = true))
        }.toList()
        return allHtmlFiles.firstOrNull()
    }

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
            } catch (_: Exception) {}
        }
        return uri.lastPathSegment
    }
}
