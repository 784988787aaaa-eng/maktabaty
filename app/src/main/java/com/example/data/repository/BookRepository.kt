package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.BookDao
import com.example.data.importer.BookImporter
import com.example.data.importer.HtmlBookParser
import com.example.data.importer.ImportUriResult
import com.example.data.importer.MultiBookArchiveProposal
import com.example.data.importer.SampleBooksProvider
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
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

        // Auto-heal any legacy user-imported books on disk and ensure true palettes & real cover snapshots
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
                        val raw = entryFile.readText(Charsets.UTF_8)
                        val meta = HtmlBookParser.parseContent(raw, userBook.title)
                        if (!raw.contains("__maktabatiV5Initialized")) {
                            val healedHtml = com.example.data.sandbox.SandboxHtmlInjector.injectIntoHtml(raw, userBook.id, meta.pageCount)
                            entryFile.writeText(healedHtml, Charsets.UTF_8)
                            if (meta.pageCount != userBook.pageCount || meta.tableOfContents.isNotEmpty()) {
                                bookDao.updateBookPageCountAndToc(
                                    id = userBook.id,
                                    pageCount = maxOf(meta.pageCount, userBook.pageCount),
                                    tocJson = HtmlBookParser.tocToJson(meta.tableOfContents)
                                )
                            }
                        }

                        // Refresh true palette if legacy default colors were stored previously
                        if (userBook.bgPrimaryHex != meta.bgPrimaryHex ||
                            userBook.accentRoseHex != meta.accentRoseHex ||
                            userBook.accentGoldHex != meta.accentGoldHex ||
                            userBook.coverGradientStart != meta.coverGradientStart ||
                            userBook.coverGradientEnd != meta.coverGradientEnd
                        ) {
                            bookDao.updateBookMetadataAndStyle(
                                id = userBook.id,
                                title = userBook.title,
                                subtitle = userBook.subtitle,
                                author = userBook.author,
                                coverGradientStart = meta.coverGradientStart,
                                coverGradientEnd = meta.coverGradientEnd,
                                bgPrimaryHex = meta.bgPrimaryHex,
                                accentGoldHex = meta.accentGoldHex,
                                accentRoseHex = meta.accentRoseHex
                            )
                        }

                        // Generate cover_snapshot.png if not present yet and book has a cover page or cover image
                        if (!com.example.data.importer.HtmlCoverSnapshotGenerator.hasSavedCoverSnapshot(bookDir)) {
                            com.example.data.importer.HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
                                context = context,
                                bookDir = bookDir,
                                entryHtmlFile = entryFile,
                                fallbackTitle = userBook.title
                            )
                        }
                    }
                }
            }
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
        } catch (e: Exception) {
            Result.failure(e)
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
            } catch (e: Exception) {
                lastError = e
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

    suspend fun updateReadingProgress(bookId: String, progress: Float, scrollY: Int, currentPage: Int = 1) = withContext(Dispatchers.IO) {
        bookDao.updateReadingProgress(
            id = bookId,
            progress = progress.coerceIn(0f, 1f),
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

    /**
     * Prepares the book for external sharing under its clean human-readable title:
     * - If `book.fileCount <= 1`: copies the standalone HTML file to `cacheDir/shared_books/${cleanTitle}.html`
     * - If `book.fileCount > 1` (rich-media book with images/audio/assets): compresses the entire book directory
     *   (`filesDir/books/${book.folderPath}`) into `cacheDir/shared_books/${cleanTitle}.zip` preserving relative paths.
     */
    fun prepareSharedBookFile(book: Book): File {
        val sharedDir = File(context.cacheDir, "shared_books").apply {
            if (!exists()) mkdirs()
        }
        val cleanTitle = book.title
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
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
                if (file.isFile && file.name != com.example.data.importer.HtmlCoverSnapshotGenerator.COVER_SNAPSHOT_FILENAME) {
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
