package com.maktabati.app.data.repository

import android.content.Context
import android.net.Uri
import com.maktabati.app.data.db.BookDao
import com.maktabati.app.data.importer.BookImporter
import com.maktabati.app.data.importer.BookVisualTheme
import com.maktabati.app.data.importer.ConvertibleBookProposal
import com.maktabati.app.data.importer.HtmlBookParser
import com.maktabati.app.data.importer.HtmlCoverSnapshotGenerator
import com.maktabati.app.data.importer.ImportUriResult
import com.maktabati.app.data.importer.MultiBookArchiveProposal
import com.maktabati.app.data.importer.SampleBooksProvider
import com.maktabati.app.data.importer.SmartBookConverter
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Bookmark
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.data.sandbox.SandboxHtmlInjector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class BatchImportOutcome(
    val importedSingleBooks: List<Book>,
    val multiBookProposals: List<MultiBookArchiveProposal>
)

class BookRepository(
    private val bookDao: BookDao,
    private val context: Context
) {
    companion object {
        private val FILENAME_SANITIZE_REGEX = Regex("[\\\\/:*?\"<>|]")
    }

    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    val allShelves: Flow<List<Shelf>> = bookDao.getAllShelves()
    val favoriteBooks: Flow<List<Book>> = bookDao.getFavoriteBooks()

    fun getBooksByShelf(shelfId: String): Flow<List<Book>> = bookDao.getBooksByShelf(shelfId)

    fun getBook(id: String): Flow<Book?> = bookDao.getBookById(id)

    suspend fun getBookOnce(id: String): Book? = bookDao.getBookByIdOnce(id)

    fun getBookmarks(bookId: String): Flow<List<Bookmark>> = bookDao.getBookmarksForBook(bookId)

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val defaultShelfIds = setOf("shelf_literature", "shelf_science", "shelf_tech")
        val currentShelves = bookDao.getAllShelves().first()
        for (shelf in currentShelves) {
            if (shelf.id in defaultShelfIds) {
                bookDao.clearShelfFromBooks(shelf.id)
                bookDao.deleteShelf(shelf)
            }
        }

        val booksRoot = File(context.filesDir, "books")
        val currentBooks = bookDao.getAllBooks().first()
        for (book in currentBooks) {
            if (SampleBooksProvider.isDefaultOrSampleBookId(book.id) ||
                SampleBooksProvider.isDefaultOrSampleBookId(book.folderPath)
            ) {
                bookDao.deleteBook(book)
                val bookDir = File(booksRoot, book.folderPath)
                if (bookDir.exists()) {
                    bookDir.deleteRecursively()
                }
            }
        }

        if (booksRoot.exists()) {
            booksRoot.listFiles()?.forEach { dir ->
                if (SampleBooksProvider.isDefaultOrSampleBookId(dir.name)) {
                    dir.deleteRecursively()
                }
            }
        }

        ensureBooksCoversAndTruePalettes()
    }

    suspend fun ensureBooksCoversAndTruePalettes() = withContext(Dispatchers.IO) {
        val booksRoot = File(context.filesDir, "books")
        val remainingBooks = bookDao.getAllBooks().first()
        for (userBook in remainingBooks) {
            if (!SampleBooksProvider.isDefaultOrSampleBookId(userBook.id)) {
                val bookDir = File(booksRoot, userBook.folderPath)
                val entryFile = File(bookDir, userBook.entryHtmlPath)
                if (entryFile.isFile) {
                    runCatching {
                        HtmlCoverSnapshotGenerator.cleanupLegacySnapshotIfPresent(bookDir)
                        val hasUpToDateCover = HtmlCoverSnapshotGenerator.hasSavedCoverSnapshot(bookDir)
                        val isInjected = com.maktabati.app.ui.screens.reader.isHtmlAlreadyInjected(entryFile)
                        val sanitizedSavedTitle = HtmlCoverSnapshotGenerator.sanitizeCoverTitle(userBook.title)
                        val sanitizedSavedSubtitle = HtmlCoverSnapshotGenerator.sanitizeCoverSubtitle(
                            userBook.subtitle,
                            sanitizedSavedTitle
                        )
                        val sanitizedSavedAuthor = HtmlBookParser.sanitizeExtractedAuthor(
                            userBook.author,
                            sanitizedSavedTitle,
                            sanitizedSavedSubtitle
                        )
                        val hasDirtyMetadata = userBook.title != sanitizedSavedTitle ||
                            userBook.subtitle != sanitizedSavedSubtitle ||
                            userBook.author != sanitizedSavedAuthor

                        val hasValidPalette = userBook.bgPrimaryHex.isNotBlank() &&
                            userBook.accentGoldHex.isNotBlank() &&
                            userBook.accentRoseHex.isNotBlank()

                        // Fast-Path: if book already has cover_v3.png, valid palette, clean title/subtitle/author, and v9 runtime,
                        // do NOT read or parse index.html during app startup!
                        if (hasUpToDateCover && isInjected && hasValidPalette && !hasDirtyMetadata && userBook.pageCount >= 1) {
                            return@runCatching
                        }

                        // If only the unified cover_v3.png is missing or metadata had raw WhatsApp symbols,
                        // heal metadata and regenerate cover_v3.png from header without reading/parsing the entire multi-MB book
                        if (isInjected && hasValidPalette && userBook.pageCount >= 1) {
                            if (hasDirtyMetadata) {
                                bookDao.updateBookMetadataAndStyle(
                                    id = userBook.id,
                                    title = sanitizedSavedTitle,
                                    subtitle = sanitizedSavedSubtitle,
                                    author = sanitizedSavedAuthor,
                                    coverGradientStart = userBook.coverGradientStart,
                                    coverGradientEnd = userBook.coverGradientEnd,
                                    bgPrimaryHex = userBook.bgPrimaryHex,
                                    accentGoldHex = userBook.accentGoldHex,
                                    accentRoseHex = userBook.accentRoseHex
                                )
                                val snapshotFile = HtmlCoverSnapshotGenerator.getCoverSnapshotFile(bookDir)
                                if ( snapshotFile.exists()) {
                                    snapshotFile.delete()
                                }
                            }
                            HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
                                context = context,
                                bookDir = bookDir,
                                entryHtmlFile = entryFile,
                                fallbackTitle = sanitizedSavedTitle
                            )
                            return@runCatching
                        }

                        val raw = entryFile.readText(Charsets.UTF_8)
                        val meta = HtmlBookParser.parse(entryFile, sanitizedSavedTitle)
                        val finalPages = meta.pageCount.coerceAtLeast(1)

                        if (!raw.contains("__maktabatiV9Initialized") || userBook.pageCount != finalPages) {
                            val healedHtml = SandboxHtmlInjector.injectIntoHtml(raw, userBook.id, finalPages, bookDir = bookDir)
                            entryFile.writeText(healedHtml, Charsets.UTF_8)
                        }

                        if (userBook.pageCount != finalPages || (meta.tableOfContents.isNotEmpty() && userBook.tableOfContentsJson == "[]")) {
                            bookDao.updateBookPageCountAndToc(
                                id = userBook.id,
                                pageCount = finalPages,
                                tocJson = HtmlBookParser.tocToJson(meta.tableOfContents)
                            )
                        }

                        if (userBook.pageCount != finalPages && (userBook.readingProgress > 0f || userBook.currentPage > 1)) {
                            val clampedPage = userBook.currentPage.coerceIn(1, finalPages)
                            val correctedProgress = if (clampedPage >= finalPages) {
                                1.0f
                            } else {
                                (clampedPage.toFloat() / finalPages.toFloat()).coerceIn(0f, 1f)
                            }
                            bookDao.updateReadingProgress(
                                id = userBook.id,
                                progress = correctedProgress,
                                scrollY = userBook.scrollY,
                                currentPage = clampedPage,
                                timestamp = userBook.lastReadTimestamp
                            )
                        }

                        if (hasDirtyMetadata ||
                            userBook.bgPrimaryHex != meta.bgPrimaryHex ||
                            userBook.accentRoseHex != meta.accentRoseHex ||
                            userBook.accentGoldHex != meta.accentGoldHex ||
                            userBook.coverGradientStart != meta.coverGradientStart ||
                            userBook.coverGradientEnd != meta.coverGradientEnd
                        ) {
                            bookDao.updateBookMetadataAndStyle(
                                id = userBook.id,
                                title = sanitizedSavedTitle,
                                subtitle = sanitizedSavedSubtitle,
                                author = sanitizedSavedAuthor,
                                coverGradientStart = meta.coverGradientStart,
                                coverGradientEnd = meta.coverGradientEnd,
                                bgPrimaryHex = meta.bgPrimaryHex,
                                accentGoldHex = meta.accentGoldHex,
                                accentRoseHex = meta.accentRoseHex
                            )
                            if (hasDirtyMetadata) {
                                val snapshotFile = HtmlCoverSnapshotGenerator.getCoverSnapshotFile(bookDir)
                                if (snapshotFile.exists()) {
                                    snapshotFile.delete()
                                }
                            }
                        }

                        if (!HtmlCoverSnapshotGenerator.hasSavedCoverSnapshot(bookDir)) {
                            HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
                                context = context,
                                bookDir = bookDir,
                                entryHtmlFile = entryFile,
                                fallbackTitle = sanitizedSavedTitle
                            )
                        }
                    }
                }
            }
        }
    }

    private fun mapImportThrowable(t: Throwable, fallbackMessage: String): Throwable {
        if (t is kotlinx.coroutines.CancellationException) throw t
        if (t is OutOfMemoryError || t is StackOverflowError) {
            System.gc()
            return IllegalStateException("حجم الملف كبير جداً على ذاكرة الجهاز الحالية؛ تم تفريغ الذاكرة المؤقتة، يرجى المحاولة مرة أخرى.")
        }
        return t as? Exception ?: IllegalStateException(t.message ?: fallbackMessage, t)
    }

    suspend fun inspectConvertibleDocument(uri: Uri): Result<ConvertibleBookProposal> = withContext(Dispatchers.IO) {
        try {
            val proposal = SmartBookConverter.inspectDocumentUri(context, uri)
            Result.success(proposal)
        } catch (t: Throwable) {
            Result.failure(mapImportThrowable(t, "تعذر قراءة وتحليل الملف النصي"))
        }
    }

    suspend fun inspectRawText(
        rawText: String,
        sourceLabel: String = "CLIPBOARD • نصوص الحافظة"
    ): Result<ConvertibleBookProposal> = withContext(Dispatchers.IO) {
        try {
            val proposal = SmartBookConverter.inspectRawText(
                rawText = rawText,
                sourceLabel = sourceLabel,
                stagingCacheDir = context.cacheDir
            )
            Result.success(proposal)
        } catch (t: Throwable) {
            Result.failure(mapImportThrowable(t, "تعذر تحليل النص المدخل"))
        }
    }

    suspend fun convertAndImportRawText(
        rawText: String,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme? = null,
        sourceLabel: String = "CLIPBOARD • نصوص الحافظة"
    ): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val proposal = SmartBookConverter.inspectRawText(
                rawText = rawText,
                sourceLabel = sourceLabel,
                preferredTitle = customTitle,
                preferredAuthor = customAuthor,
                stagingCacheDir = context.cacheDir
            )
            val chosenTheme = theme ?: proposal.recommendedTheme
            val book = BookImporter.importConvertedProposal(
                context = context,
                proposal = proposal,
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                customAuthor = customAuthor,
                theme = chosenTheme
            )
            bookDao.insertBook(book)
            Result.success(book)
        } catch (t: Throwable) {
            Result.failure(mapImportThrowable(t, "تعذر صياغة وتحويل النص إلى كتاب"))
        }
    }

    suspend fun convertAndImportProposal(
        proposal: ConvertibleBookProposal,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme = proposal.recommendedTheme
    ): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val book = BookImporter.importConvertedProposal(
                context = context,
                proposal = proposal,
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                customAuthor = customAuthor,
                theme = theme
            )
            bookDao.insertBook(book)
            Result.success(book)
        } catch (t: Throwable) {
            Result.failure(mapImportThrowable(t, "تعذر إتمام تحويل الكتاب"))
        }
    }

    suspend fun importOrInspectBook(
        uri: Uri,
        customTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): Result<ImportUriResult> = withContext(Dispatchers.IO) {
        try {
            val outcome = BookImporter.importOrInspectUri(context, uri, customTitle, onQuickBookStaged)
            if (outcome is ImportUriResult.SingleBookImported) {
                bookDao.insertBook(outcome.book)
            }
            Result.success(outcome)
        } catch (t: Throwable) {
            Result.failure(mapImportThrowable(t, "تعذر استيراد الملف المحدد"))
        }
    }

    suspend fun importOrInspectMultipleBooks(uris: List<Uri>): Result<BatchImportOutcome> = withContext(Dispatchers.IO) {
        val importedBooks = mutableListOf<Book>()
        val proposals = mutableListOf<MultiBookArchiveProposal>()
        var lastError: Throwable? = null

        for (uri in uris) {
            try {
                when (val res = BookImporter.importOrInspectUri(context, uri)) {
                    is ImportUriResult.SingleBookImported -> {
                        bookDao.insertBook(res.book)
                        importedBooks.add(res.book)
                    }
                    is ImportUriResult.MultiBookArchiveDetected -> {
                        proposals.add(res.proposal)
                    }
                }
            } catch (t: Throwable) {
                lastError = mapImportThrowable(t, "تعذر استيراد الملفات المحددة")
            }
        }

        if (importedBooks.isNotEmpty() || proposals.isNotEmpty()) {
            Result.success(BatchImportOutcome(importedBooks, proposals))
        } else {
            Result.failure(lastError ?: IllegalArgumentException("تعذر استيراد الملفات المحددة"))
        }
    }

    suspend fun commitMultiBookProposalToNewShelf(
        proposal: MultiBookArchiveProposal,
        colorHex: String = "#0284C7",
        iconName: String = "auto_stories"
    ): Pair<Shelf, List<Book>> = withContext(Dispatchers.IO) {
        val createdShelf = createShelf(
            name = proposal.zipBaseName,
            colorHex = colorHex,
            iconName = iconName
        )
        val finalizedBooks = BookImporter.commitMultiBookProposal(
            context = context,
            proposal = proposal,
            shelfId = createdShelf.id
        )
        for (book in finalizedBooks) {
            bookDao.insertBook(book)
        }
        Pair(createdShelf, finalizedBooks)
    }

    suspend fun commitMultiBookProposalStandalone(
        proposal: MultiBookArchiveProposal
    ): List<Book> = withContext(Dispatchers.IO) {
        val finalizedBooks = BookImporter.commitMultiBookProposal(
            context = context,
            proposal = proposal,
            shelfId = null
        )
        for (book in finalizedBooks) {
            bookDao.insertBook(book)
        }
        finalizedBooks
    }

    fun cancelMultiBookProposal(proposal: MultiBookArchiveProposal) {
        BookImporter.cancelMultiBookProposal(proposal)
    }

    suspend fun importBook(
        uri: Uri,
        customTitle: String? = null,
        onQuickBookStaged: ((Book) -> Unit)? = null
    ): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val book = BookImporter.importFromUri(context, uri, customTitle, onQuickBookStaged)
            bookDao.insertBook(book)
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importMultipleBooks(uris: List<Uri>): Result<List<Book>> = withContext(Dispatchers.IO) {
        val importedBooks = mutableListOf<Book>()
        var lastError: Throwable? = null
        for (uri in uris) {
            try {
                val book = BookImporter.importFromUri(context, uri)
                bookDao.insertBook(book)
                importedBooks.add(book)
            } catch (e: Exception) {
                lastError = e
            }
        }
        if (importedBooks.isNotEmpty()) {
            Result.success(importedBooks)
        } else {
            Result.failure(lastError ?: IllegalArgumentException("تعذر استيراد الملفات المحددة"))
        }
    }

    suspend fun updateReadingProgress(
        bookId: String,
        progress: Float,
        scrollY: Int,
        currentPage: Int = 1,
        actualTotalPages: Int = 0
    ) = withContext(Dispatchers.IO) {
        if (actualTotalPages > 0) {
            bookDao.updateBookPageCount(bookId, actualTotalPages)
        }
        val finalProgress = if (actualTotalPages > 0 && currentPage >= actualTotalPages) {
            1.0f
        } else {
            progress.coerceIn(0f, 1f)
        }
        bookDao.updateReadingProgress(
            id = bookId,
            progress = finalProgress,
            scrollY = scrollY,
            currentPage = currentPage.coerceAtLeast(1),
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun updateBookShelf(bookId: String, shelfId: String?) = withContext(Dispatchers.IO) {
        bookDao.updateBookShelf(bookId, shelfId)
    }

    suspend fun toggleFavorite(bookId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        bookDao.updateBookFavorite(bookId, isFavorite)
    }

    suspend fun updateBookMetadataAndStyle(
        bookId: String,
        title: String,
        subtitle: String,
        author: String,
        bgPrimaryHex: String,
        accentGoldHex: String,
        accentRoseHex: String
    ) = withContext(Dispatchers.IO) {
        val startColor = HtmlBookParser.parseHexToColorLong(bgPrimaryHex, 0xFF1E293B)
        val roseColor = HtmlBookParser.parseHexToColorLong(accentRoseHex, 0xFF5B3A29)
        val isLight = HtmlBookParser.computeColorLuminance(startColor) > 0.48f
        val endColor = if (isLight) {
            HtmlBookParser.blendColorLongs(startColor, roseColor, 0.15f)
        } else {
            HtmlBookParser.blendColorLongs(startColor, roseColor, 0.45f)
        }
        bookDao.updateBookMetadataAndStyle(
            id = bookId,
            title = title,
            subtitle = subtitle,
            author = author,
            coverGradientStart = startColor,
            coverGradientEnd = endColor,
            bgPrimaryHex = bgPrimaryHex,
            accentGoldHex = accentGoldHex,
            accentRoseHex = accentRoseHex
        )
    }

    suspend fun deleteBook(bookId: String) = withContext(Dispatchers.IO) {
        val book = bookDao.getBookByIdOnce(bookId)
        if (book != null) {
            bookDao.deleteBook(book)
            val bookDir = File(File(context.filesDir, "books"), book.folderPath)
            if (bookDir.exists()) {
                bookDir.deleteRecursively()
            }
        }
    }

    suspend fun createShelf(name: String, colorHex: String, iconName: String): Shelf = withContext(Dispatchers.IO) {
        val shelf = Shelf(
            id = "shelf_" + UUID.randomUUID().toString().take(8),
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            sortOrder = 99
        )
        bookDao.insertShelf(shelf)
        shelf
    }

    suspend fun deleteShelf(shelf: Shelf) = withContext(Dispatchers.IO) {
        bookDao.clearShelfFromBooks(shelf.id)
        bookDao.deleteShelf(shelf)
    }

    suspend fun addBookmark(bookId: String, title: String, scrollY: Int, progress: Float, excerpt: String) = withContext(Dispatchers.IO) {
        val bookmark = Bookmark(
            id = "bm_" + UUID.randomUUID().toString().take(8),
            bookId = bookId,
            title = title,
            scrollY = scrollY,
            progress = progress,
            excerpt = excerpt
        )
        bookDao.insertBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: Bookmark) = withContext(Dispatchers.IO) {
        bookDao.deleteBookmark(bookmark)
    }

    fun getBookAbsoluteFile(book: Book): File {
        val booksRoot = File(context.filesDir, "books")
        val bookDir = File(booksRoot, book.folderPath)
        return File(bookDir, book.entryHtmlPath)
    }

    fun getBookDirectory(book: Book): File {
        val booksRoot = File(context.filesDir, "books")
        return File(booksRoot, book.folderPath)
    }

    fun prepareSharedBookFile(book: Book): File {
        val sharedDir = File(context.cacheDir, "shared_books").apply {
            if (!exists()) mkdirs()
        }
        val cleanTitle = book.title
            .replace(FILENAME_SANITIZE_REGEX, "_")
            .trim()
            .ifBlank { "book" }

        val bookDir = getBookDirectory(book)
        if (book.fileCount > 1 && bookDir.exists() && bookDir.isDirectory) {
            val targetZip = File(sharedDir, "$cleanTitle.zip")
            if (targetZip.exists()) {
                targetZip.delete()
            }
            zipDirectoryContents(bookDir, targetZip)
            return targetZip
        } else {
            val sourceFile = getBookAbsoluteFile(book)
            val targetFile = File(sharedDir, "$cleanTitle.html")
            if (sourceFile.exists()) {
                sourceFile.copyTo(targetFile, overwrite = true)
            }
            return targetFile
        }
    }

    private fun zipDirectoryContents(sourceDir: File, targetZipFile: File) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(targetZipFile))).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile &&
                    file.name != HtmlCoverSnapshotGenerator.COVER_SNAPSHOT_FILENAME &&
                    file.name != HtmlCoverSnapshotGenerator.LEGACY_SNAPSHOT_FILENAME
                ) {
                    val relPath = file.relativeTo(sourceDir).path.replace("\\", "/")
                    val entry = ZipEntry(relPath)
                    zos.putNextEntry(entry)
                    BufferedInputStream(FileInputStream(file)).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }
}
