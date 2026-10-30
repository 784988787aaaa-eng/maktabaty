package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.Book
import com.example.data.sandbox.SandboxHtmlInjector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class StagedBookEntry(
    val book: Book,
    val stagedBookDir: File
)

data class MultiBookArchiveProposal(
    val id: String,
    val zipBaseName: String,
    val stagingRootDir: File,
    val stagedBooks: List<StagedBookEntry>
) {
    val archiveBooksCount: Int get() = stagedBooks.size
    val books: List<Book> get() = stagedBooks.map { it.book }
}

sealed class ZipInspectionResult {
    data class SingleBook(val book: Book) : ZipInspectionResult()
    data class MultiBookArchive(val proposal: MultiBookArchiveProposal) : ZipInspectionResult()
}

sealed class ImportUriResult {
    data class SingleBookImported(val book: Book) : ImportUriResult()
    data class MultiBookArchiveDetected(val proposal: MultiBookArchiveProposal) : ImportUriResult()
}

object BookImporter {

    private val ROOT_ENTRY_CANDIDATES = listOf(
        "index.html",
        "index.htm",
        "book.html",
        "main.html",
        "default.html"
    )

    suspend fun importOrInspectUri(
        context: Context,
        uri: Uri,
        userCustomTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): ImportUriResult = withContext(Dispatchers.IO) {
        val originalName = queryFileName(context, uri) ?: "كتاب_مستورد.html"
        val lowerName = originalName.lowercase()
        val mimeType = runCatching { context.contentResolver.getType(uri)?.lowercase() }.getOrNull() ?: ""
        val isZip = lowerName.endsWith(".zip") || mimeType.contains("zip")
        val isHtmlExt = lowerName.endsWith(".html") || lowerName.endsWith(".htm") || lowerName.endsWith(".xhtml")

        if (!isZip && !isHtmlExt) {
            if (!mimeType.contains("html") && !mimeType.contains("text")) {
                throw IllegalArgumentException("صيغة الملف ($originalName) غير مدعومة. يرجى اختيار ملف كتاب بصيغة .html أو .htm أو حزمة .zip")
            }
        }

        if (isZip) {
            val inspection = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inspectZipArchive(
                    context = context,
                    inputStream = inputStream,
                    originalZipName = originalName,
                    userCustomTitle = userCustomTitle,
                    onQuickBookStaged = onQuickBookStaged
                )
            } ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف المضغوط من الذاكرة")

            when (inspection) {
                is ZipInspectionResult.SingleBook -> ImportUriResult.SingleBookImported(inspection.book)
                is ZipInspectionResult.MultiBookArchive -> ImportUriResult.MultiBookArchiveDetected(inspection.proposal)
            }
        } else {
            val singleBook = importSingleHtmlUri(context, uri, originalName, userCustomTitle, onQuickBookStaged)
            ImportUriResult.SingleBookImported(singleBook)
        }
    }

    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        userCustomTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): Book = withContext(Dispatchers.IO) {
        when (val result = importOrInspectUri(context, uri, userCustomTitle, onQuickBookStaged)) {
            is ImportUriResult.SingleBookImported -> result.book
            is ImportUriResult.MultiBookArchiveDetected -> {
                val finalized = commitMultiBookProposal(context, result.proposal, shelfId = null)
                finalized.firstOrNull()
                    ?: throw IllegalArgumentException("لم يتم العثور على أي كتاب صالح داخل الأرشيف")
            }
        }
    }

    /**
     * Inspects a `.zip` file directly from disk or stream, extracting to `cacheDir/zip_staging_${UUID}`
     * and classifying it as either:
     * 1. Single Rich-Media Book ZIP (has root index.html/book.html/main.html OR only 1 HTML file in total, plus any companion images/audio/fonts/CSS/JS).
     * 2. Multi-Book Archive ZIP (no unifying root index.html and 2+ independent HTML books or book folders).
     */
    suspend fun inspectZipFile(
        context: Context,
        zipFile: File,
        userCustomTitle: String? = null
    ): ZipInspectionResult = withContext(Dispatchers.IO) {
        FileInputStream(zipFile).use { fis ->
            inspectZipArchive(
                context = context,
                inputStream = fis,
                originalZipName = zipFile.name,
                userCustomTitle = userCustomTitle
            )
        }
    }

    suspend fun inspectZipArchive(
        context: Context,
        inputStream: InputStream,
        originalZipName: String,
        userCustomTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): ZipInspectionResult = withContext(Dispatchers.IO) {
        val stagingId = UUID.randomUUID().toString().replace("-", "").take(12)
        val stagingDir = File(context.cacheDir, "zip_staging_$stagingId").apply { mkdirs() }

        try {
            val extractedStats = extractZipSafely(inputStream, stagingDir)
            if (extractedStats.first == 0 || extractedStats.second == 0L) {
                stagingDir.deleteRecursively()
                throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على بيانات")
            }

            val cleanZipBaseName = cleanArchiveBaseName(originalZipName)
            val effectiveRoot = resolveEffectiveArchiveRoot(stagingDir)

            val allHtmlFiles = effectiveRoot.walkTopDown()
                .filter { it.isFile && isHtmlFile(it) }
                .toList()

            if (allHtmlFiles.isEmpty()) {
                stagingDir.deleteRecursively()
                throw IllegalArgumentException("لم يتم العثور على أي ملف HTML صالح داخل الحزمة المستوردة")
            }

            val rootEntryFile = findRootEntryHtml(effectiveRoot)
            val isSingleRichBook = (rootEntryFile != null) || (allHtmlFiles.size == 1)

            if (isSingleRichBook) {
                val entryFileInStaging = rootEntryFile ?: allHtmlFiles.first()
                val bookId = "book_" + UUID.randomUUID().toString().replace("-", "").take(10)
                val booksRoot = File(context.filesDir, "books").apply { mkdirs() }
                val targetDir = File(booksRoot, bookId).apply { mkdirs() }

                try {
                    effectiveRoot.copyRecursively(targetDir, overwrite = true)
                    val relativeEntryPath = entryFileInStaging.relativeTo(effectiveRoot).path.replace("\\", "/")
                    val targetEntryFile = File(targetDir, relativeEntryPath)

                    val fallbackTitle = userCustomTitle ?: cleanZipBaseName
                    val book = processAndBuildBookInDirectory(
                        context = context,
                        bookId = bookId,
                        bookDir = targetDir,
                        entryFile = targetEntryFile,
                        relativeEntryPath = relativeEntryPath,
                        fallbackTitle = fallbackTitle,
                        userCustomTitle = userCustomTitle,
                        onQuickBookStaged = onQuickBookStaged
                    )
                    stagingDir.deleteRecursively()
                    return@withContext ZipInspectionResult.SingleBook(book)
                } catch (e: Exception) {
                    if (targetDir.exists()) targetDir.deleteRecursively()
                    throw e
                }
            } else {
                // Multi-Book Archive ZIP (2+ books and no unifying root index.html)
                val preparedRootDir = File(context.cacheDir, "zip_prepared_$stagingId").apply { mkdirs() }
                val stagedEntries = try {
                    stageMultiBooksFromArchiveRoot(
                        context = context,
                        effectiveRoot = effectiveRoot,
                        preparedRootDir = preparedRootDir
                    )
                } finally {
                    stagingDir.deleteRecursively()
                }

                if (stagedEntries.isEmpty()) {
                    preparedRootDir.deleteRecursively()
                    throw IllegalArgumentException("لم يتم العثور على كتب صالحة داخل الأرشيف")
                }

                if (stagedEntries.size == 1) {
                    // Edge case where only 1 valid HTML book could be parsed
                    val only = stagedEntries.first()
                    val booksRoot = File(context.filesDir, "books").apply { mkdirs() }
                    val targetDir = File(booksRoot, only.book.folderPath).apply { mkdirs() }
                    only.stagedBookDir.copyRecursively(targetDir, overwrite = true)
                    preparedRootDir.deleteRecursively()
                    return@withContext ZipInspectionResult.SingleBook(only.book)
                }

                val proposal = MultiBookArchiveProposal(
                    id = stagingId,
                    zipBaseName = cleanZipBaseName,
                    stagingRootDir = preparedRootDir,
                    stagedBooks = stagedEntries
                )
                return@withContext ZipInspectionResult.MultiBookArchive(proposal)
            }
        } catch (e: Exception) {
            if (stagingDir.exists()) {
                stagingDir.deleteRecursively()
            }
            throw e
        }
    }

    /**
     * Commits a previously inspected `MultiBookArchiveProposal` by moving all staged book folders
     * into `filesDir/books/${bookId}` and assigning the requested `shelfId` (or `null` for standalone).
     */
    suspend fun commitMultiBookProposal(
        context: Context,
        proposal: MultiBookArchiveProposal,
        shelfId: String?
    ): List<Book> = withContext(Dispatchers.IO) {
        val booksRoot = File(context.filesDir, "books").apply { mkdirs() }
        val finalizedBooks = mutableListOf<Book>()
        try {
            for (entry in proposal.stagedBooks) {
                val targetDir = File(booksRoot, entry.book.folderPath)
                if (targetDir.exists()) targetDir.deleteRecursively()
                targetDir.mkdirs()
                entry.stagedBookDir.copyRecursively(targetDir, overwrite = true)
                finalizedBooks.add(entry.book.copy(shelfId = shelfId))
            }
        } finally {
            if (proposal.stagingRootDir.exists()) {
                proposal.stagingRootDir.deleteRecursively()
            }
        }
        finalizedBooks
    }

    fun cancelMultiBookProposal(proposal: MultiBookArchiveProposal) {
        if (proposal.stagingRootDir.exists()) {
            proposal.stagingRootDir.deleteRecursively()
        }
    }

    private fun stageMultiBooksFromArchiveRoot(
        context: Context,
        effectiveRoot: File,
        preparedRootDir: File
    ): List<StagedBookEntry> {
        val results = mutableListOf<StagedBookEntry>()
        val children = effectiveRoot.listFiles()
            ?.filter { !isIgnoredSystemEntry(it.name) }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

        // Any companion non-HTML files/folders at the root that might be shared assets if top-level HTML files exist
        val topLevelHtmlFiles = children.filter { it.isFile && isHtmlFile(it) }
        val topLevelAssetDirs = children.filter {
            it.isDirectory && it.walkTopDown().none { f -> f.isFile && isHtmlFile(f) }
        }
        val topLevelAssetFiles = children.filter { it.isFile && !isHtmlFile(it) }

        for (child in children) {
            if (child.isFile && isHtmlFile(child)) {
                val bookId = "book_" + UUID.randomUUID().toString().replace("-", "").take(10)
                val stagedBookDir = File(preparedRootDir, bookId).apply { mkdirs() }
                val destHtml = File(stagedBookDir, "index.html")
                child.copyTo(destHtml, overwrite = true)

                // Copy any shared top-level asset folders/files (images, audio, css, fonts) if present
                for (assetDir in topLevelAssetDirs) {
                    assetDir.copyRecursively(File(stagedBookDir, assetDir.name), overwrite = true)
                }
                for (assetFile in topLevelAssetFiles) {
                    assetFile.copyTo(File(stagedBookDir, assetFile.name), overwrite = true)
                }

                runCatching {
                    val book = processAndBuildBookInDirectory(
                        context = context,
                        bookId = bookId,
                        bookDir = stagedBookDir,
                        entryFile = destHtml,
                        relativeEntryPath = "index.html",
                        fallbackTitle = child.nameWithoutExtension,
                        userCustomTitle = null,
                        onQuickBookStaged = null
                    )
                    results.add(StagedBookEntry(book = book, stagedBookDir = stagedBookDir))
                }.onFailure {
                    stagedBookDir.deleteRecursively()
                }
            } else if (child.isDirectory) {
                val subHtmls = child.walkTopDown().filter { it.isFile && isHtmlFile(it) }.toList()
                if (subHtmls.isEmpty()) continue

                val subRootEntry = findRootEntryHtml(child)
                if (subRootEntry != null || subHtmls.size == 1) {
                    // This subdirectory is a single self-contained rich-media book
                    val entryInChild = subRootEntry ?: subHtmls.first()
                    val bookId = "book_" + UUID.randomUUID().toString().replace("-", "").take(10)
                    val stagedBookDir = File(preparedRootDir, bookId).apply { mkdirs() }
                    child.copyRecursively(stagedBookDir, overwrite = true)

                    val relPath = entryInChild.relativeTo(child).path.replace("\\", "/")
                    val stagedEntryFile = File(stagedBookDir, relPath)
                    val fallbackName = if (entryInChild.name.lowercase() in ROOT_ENTRY_CANDIDATES) {
                        child.name
                    } else {
                        entryInChild.nameWithoutExtension
                    }

                    runCatching {
                        val book = processAndBuildBookInDirectory(
                            context = context,
                            bookId = bookId,
                            bookDir = stagedBookDir,
                            entryFile = stagedEntryFile,
                            relativeEntryPath = relPath,
                            fallbackTitle = fallbackName,
                            userCustomTitle = null,
                            onQuickBookStaged = null
                        )
                        results.add(StagedBookEntry(book = book, stagedBookDir = stagedBookDir))
                    }.onFailure {
                        stagedBookDir.deleteRecursively()
                    }
                } else {
                    // Subdirectory itself contains multiple books
                    results.addAll(stageMultiBooksFromArchiveRoot(context, child, preparedRootDir))
                }
            }
        }

        return results
    }

    private fun processAndBuildBookInDirectory(
        context: Context,
        bookId: String,
        bookDir: File,
        entryFile: File,
        relativeEntryPath: String,
        fallbackTitle: String,
        userCustomTitle: String?,
        onQuickBookStaged: ((Book) -> Unit)?
    ): Book {
        val rawHtml = entryFile.readText(Charsets.UTF_8)
        val quickMeta = HtmlBookParser.parseQuickHeader(rawHtml, fallbackTitle, bookDir = bookDir)
        val injectedHtml = SandboxHtmlInjector.injectIntoHtml(rawHtml, bookId, quickMeta.pageCount)
        entryFile.writeText(injectedHtml, Charsets.UTF_8)

        val dirStats = computeDirectoryStats(bookDir)
        val fileCount = dirStats.first.coerceAtLeast(1)
        val totalSize = dirStats.second.coerceAtLeast(entryFile.length())

        if (onQuickBookStaged != null) {
            val quickTitle = if (!userCustomTitle.isNullOrBlank()) userCustomTitle else quickMeta.title
            onQuickBookStaged(
                Book(
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
            )
        }

        val metadata = HtmlBookParser.parse(entryFile, fallbackTitle)
        val finalTitle = if (!userCustomTitle.isNullOrBlank()) userCustomTitle else metadata.title

        // Generate real cover snapshot if book has an explicit cover image or a designed cover page
        val snapshotFile = HtmlCoverSnapshotGenerator.getCoverSnapshotFile(bookDir)
        if (!HtmlCoverSnapshotGenerator.tryExtractExplicitCoverImage(bookDir, rawHtml, snapshotFile)) {
            if (metadata.hasCoverPage) {
                HtmlCoverSnapshotGenerator.renderHtmlCoverPageSnapshot(
                    htmlContent = rawHtml,
                    metadata = metadata.copy(title = finalTitle),
                    targetFile = snapshotFile
                )
                HtmlCoverSnapshotGenerator.scheduleOffscreenWebViewCapture(
                    context = context.applicationContext,
                    bookDir = bookDir,
                    entryHtmlFile = entryFile
                )
            }
        }
        if (snapshotFile.isFile) {
            HtmlCoverSnapshotGenerator.notifySnapshotUpdated()
        }

        return Book(
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
    }

    private fun importSingleHtmlUri(
        context: Context,
        uri: Uri,
        originalName: String,
        userCustomTitle: String?,
        onQuickBookStaged: ((Book) -> Unit)?
    ): Book {
        val bookId = "book_" + UUID.randomUUID().toString().replace("-", "").take(10)
        val booksRoot = File(context.filesDir, "books").apply { mkdirs() }
        val targetDir = File(booksRoot, bookId).apply { mkdirs() }

        try {
            val fallbackName = userCustomTitle ?: originalName.substringBeforeLast(".")
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("تعذر فتح وقراءة الملف من الذاكرة")

            if (bytes.isEmpty()) {
                throw IllegalArgumentException("الملف المختار فارغ ولا يحتوي على بيانات")
            }

            val fileCount = 1
            val totalSize = bytes.size.toLong()
            val relativeEntryPath = "index.html"
            val rawHtml = String(bytes, Charsets.UTF_8)

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

            val metadata = HtmlBookParser.parseContent(rawHtml, fallbackName, quickMode = false)
            val finalTitle = if (!userCustomTitle.isNullOrBlank()) userCustomTitle else metadata.title

            // Generate real cover snapshot if book has an explicit cover image or a designed cover page
            val snapshotFile = HtmlCoverSnapshotGenerator.getCoverSnapshotFile(targetDir)
            if (!HtmlCoverSnapshotGenerator.tryExtractExplicitCoverImage(targetDir, rawHtml, snapshotFile)) {
                if (metadata.hasCoverPage) {
                    HtmlCoverSnapshotGenerator.renderHtmlCoverPageSnapshot(
                        htmlContent = rawHtml,
                        metadata = metadata.copy(title = finalTitle),
                        targetFile = snapshotFile
                    )
                    HtmlCoverSnapshotGenerator.scheduleOffscreenWebViewCapture(
                        context = context.applicationContext,
                        bookDir = targetDir,
                        entryHtmlFile = destFile
                    )
                }
            }
            if (snapshotFile.isFile) {
                HtmlCoverSnapshotGenerator.notifySnapshotUpdated()
            }

            return Book(
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
        val canonicalDest = destDir.canonicalPath
        ZipInputStream(inputStream).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace("\\", "/")
                if (!isIgnoredZipEntryPath(entryName)) {
                    val outFile = File(destDir, entryName)
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
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return Pair(count, size)
    }

    private fun isIgnoredZipEntryPath(entryPath: String): Boolean {
        val segments = entryPath.split("/").filter { it.isNotEmpty() }
        return segments.any { isIgnoredSystemEntry(it) }
    }

    private fun isIgnoredSystemEntry(name: String): Boolean {
        val lower = name.lowercase()
        return lower == "__macosx" || lower == ".ds_store" || lower == "thumbs.db" ||
            lower.startsWith("._") || lower == HtmlCoverSnapshotGenerator.COVER_SNAPSHOT_FILENAME
    }

    private fun resolveEffectiveArchiveRoot(rootDir: File): File {
        var current = rootDir
        while (true) {
            val children = current.listFiles()?.filter { !isIgnoredSystemEntry(it.name) } ?: break
            val files = children.filter { it.isFile }
            val dirs = children.filter { it.isDirectory }
            if (files.isEmpty() && dirs.size == 1) {
                current = dirs.first()
            } else {
                break
            }
        }
        return current
    }

    private fun findRootEntryHtml(dir: File): File? {
        for (name in ROOT_ENTRY_CANDIDATES) {
            val match = dir.listFiles()?.firstOrNull {
                it.isFile && it.name.equals(name, ignoreCase = true)
            }
            if (match != null) return match
        }
        return null
    }

    private fun isHtmlFile(file: File): Boolean {
        val lower = file.name.lowercase()
        return lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".xhtml")
    }

    private fun computeDirectoryStats(dir: File): Pair<Int, Long> {
        var count = 0
        var bytes = 0L
        dir.walkTopDown().forEach { f ->
            if (f.isFile && !isIgnoredSystemEntry(f.name)) {
                count++
                bytes += f.length()
            }
        }
        return Pair(count, bytes)
    }

    fun cleanArchiveBaseName(originalZipName: String): String {
        return originalZipName
            .replace(Regex("(?i)\\.(zip|html|htm|xhtml)$"), "")
            .trim()
            .ifBlank { "أرشيف كتب" }
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
