package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.BookDao
import com.example.data.importer.BookImporter
import com.example.data.importer.HtmlBookParser
import com.example.data.importer.SampleBooksProvider
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

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
            if (book.id.startsWith("sample_") || book.id == SampleBooksProvider.LEGACY_ISLAMIC_ID) {
                bookDao.deleteBook(book)
                val bookDir = File(booksRoot, book.folderPath)
                if (bookDir.exists()) {
                    bookDir.deleteRecursively()
                }
            }
        }

        if (booksRoot.exists()) {
            booksRoot.listFiles()?.forEach { dir ->
                if (dir.name.startsWith("sample_") || dir.name == SampleBooksProvider.LEGACY_ISLAMIC_ID) {
                    dir.deleteRecursively()
                }
            }
        }

        // Ensure the sole default luxury book («خَيْمَةٌ فِي القَلْب») is generated and inserted/updated
        val defaultSamples = SampleBooksProvider.generateSampleBooks(context)
        val existingDefault = bookDao.getBookByIdOnce(SampleBooksProvider.DEFAULT_MASTERPIECE_ID)
        if (existingDefault == null) {
            for (sample in defaultSamples) {
                bookDao.insertBook(sample)
            }
        } else {
            val freshSample = defaultSamples.firstOrNull()
            if (freshSample != null) {
                bookDao.updateBookMetadataAndStyle(
                    id = existingDefault.id,
                    title = freshSample.title,
                    subtitle = freshSample.subtitle,
                    author = freshSample.author,
                    coverGradientStart = freshSample.coverGradientStart,
                    coverGradientEnd = freshSample.coverGradientEnd,
                    bgPrimaryHex = freshSample.bgPrimaryHex,
                    accentGoldHex = freshSample.accentGoldHex,
                    accentRoseHex = freshSample.accentRoseHex
                )
                bookDao.updateBookPageCountAndToc(
                    id = existingDefault.id,
                    pageCount = freshSample.pageCount,
                    tocJson = freshSample.tableOfContentsJson
                )
            }
        }

        // Auto-heal any existing user-imported books on disk (strip old CSS containment & refresh page count)
        val remainingBooks = bookDao.getAllBooks().first()
        for (userBook in remainingBooks) {
            if (userBook.id != SampleBooksProvider.DEFAULT_MASTERPIECE_ID && !userBook.id.startsWith("sample_")) {
                val entryFile = File(File(booksRoot, userBook.folderPath), userBook.entryHtmlPath)
                if (entryFile.isFile) {
                    runCatching {
                        val raw = entryFile.readText(Charsets.UTF_8)
                        val meta = HtmlBookParser.parse(entryFile, userBook.title)
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
                }
            }
        }
    }

    suspend fun importBook(uri: Uri, customTitle: String? = null): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val book = BookImporter.importFromUri(context, uri, customTitle)
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
        val endColor = HtmlBookParser.parseHexToColorLong(accentRoseHex, 0xFF0F172A)
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

    suspend fun createShelf(name: String, colorHex: String, iconName: String) = withContext(Dispatchers.IO) {
        val shelf = Shelf(
            id = "shelf_" + UUID.randomUUID().toString().take(8),
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            sortOrder = 99
        )
        bookDao.insertShelf(shelf)
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
}
