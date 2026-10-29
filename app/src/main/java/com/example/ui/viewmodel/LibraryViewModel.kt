package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
import com.example.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class FilterType {
    ALL,
    READING,
    COMPLETED,
    FAVORITES
}

enum class ViewMode {
    GRID,
    LIST
}

sealed class ImportState {
    object Idle : ImportState()
    data class Loading(val message: String = "جاري استيراد وتحليل الكتاب...") : ImportState()
    data class Success(val book: Book, val count: Int = 1) : ImportState()
    data class Error(val message: String) : ImportState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences("maktabati_preferences", android.content.Context.MODE_PRIVATE)
    private val db = AppDatabase.getInstance(application)
    val repository = BookRepository(db.bookDao(), application)

    val shelves: StateFlow<List<Shelf>> = repository.allShelves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedShelfId = MutableStateFlow<String?>(null) // null = all
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(FilterType.ALL)
    val viewMode = MutableStateFlow(
        runCatching { ViewMode.valueOf(preferences.getString("viewMode", ViewMode.GRID.name) ?: ViewMode.GRID.name) }
            .getOrDefault(ViewMode.GRID)
    )
    val isDarkTheme = MutableStateFlow(preferences.getBoolean("isDarkTheme", false))

    val importState = MutableStateFlow<ImportState>(ImportState.Idle)

    val filteredBooks: StateFlow<List<Book>> = combine(
        rawBooks,
        selectedShelfId,
        searchQuery,
        filterType
    ) { books, shelfId, query, filter ->
        books.filter { book ->
            val matchesShelf = when {
                shelfId == null -> true
                shelfId == "favorites" -> book.isFavorite
                else -> book.shelfId == shelfId
            }

            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                book.title.contains(query, ignoreCase = true) ||
                    book.subtitle.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true) ||
                    book.description.contains(query, ignoreCase = true)
            }

            val matchesFilter = when (filter) {
                FilterType.ALL -> true
                FilterType.READING -> book.readingProgress > 0f && book.readingProgress < 0.95f
                FilterType.COMPLETED -> book.readingProgress >= 0.95f
                FilterType.FAVORITES -> book.isFavorite
            }

            matchesShelf && matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Reader State
    val activeBook = MutableStateFlow<Book?>(null)
    val activeBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfNeeded()
        }
    }

    fun toggleDarkTheme() {
        val next = !isDarkTheme.value
        isDarkTheme.value = next
        preferences.edit().putBoolean("isDarkTheme", next).apply()
    }

    fun selectShelf(shelfId: String?) {
        selectedShelfId.value = shelfId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setFilterType(filter: FilterType) {
        filterType.value = filter
    }

    fun toggleViewMode() {
        val next = if (viewMode.value == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
        viewMode.value = next
        preferences.edit().putString("viewMode", next.name).apply()
    }

    fun importBook(uri: Uri, customTitle: String? = null, onComplete: ((Book?) -> Unit)? = null) {
        viewModelScope.launch {
            importState.value = ImportState.Loading("جاري تحليل البنية الوصفية وألوان الغلاف...")
            val result = repository.importBook(uri, customTitle)
            result.onSuccess { book ->
                importState.value = ImportState.Success(book, 1)
                onComplete?.invoke(book)
            }.onFailure { err ->
                importState.value = ImportState.Error(err.message ?: "تعذر استيراد الملف المحدد")
                onComplete?.invoke(null)
            }
        }
    }

    fun importMultipleBooks(uris: List<Uri>, onComplete: ((List<Book>) -> Unit)? = null) {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            importBook(uris.first()) { book ->
                onComplete?.invoke(if (book != null) listOf(book) else emptyList())
            }
            return
        }
        viewModelScope.launch {
            importState.value = ImportState.Loading("جاري استيراد وتحليل ${uris.size} كتب...")
            val result = repository.importMultipleBooks(uris)
            result.onSuccess { list ->
                importState.value = ImportState.Success(list.first(), list.size)
                onComplete?.invoke(list)
            }.onFailure { err ->
                importState.value = ImportState.Error(err.message ?: "تعذر استيراد الكتب المحددة")
                onComplete?.invoke(emptyList())
            }
        }
    }

    fun clearImportState() {
        importState.value = ImportState.Idle
    }

    fun openBook(book: Book) {
        activeBook.value = book
        viewModelScope.launch {
            repository.getBookmarks(book.id).collect { bookmarks ->
                activeBookmarks.value = bookmarks
            }
        }
    }

    fun closeReader() {
        activeBook.value = null
    }

    fun saveProgress(bookId: String, progress: Float, scrollY: Int, currentPage: Int = 1) {
        viewModelScope.launch {
            repository.updateReadingProgress(bookId, progress, scrollY, currentPage)
            if (activeBook.value?.id == bookId) {
                activeBook.value = activeBook.value?.copy(
                    readingProgress = progress,
                    scrollY = scrollY,
                    currentPage = currentPage,
                    lastReadTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            repository.toggleFavorite(book.id, !book.isFavorite)
        }
    }

    fun moveBookToShelf(bookId: String, shelfId: String?) {
        viewModelScope.launch {
            repository.updateBookShelf(bookId, shelfId)
        }
    }

    fun updateBookMetadataAndStyle(
        bookId: String,
        title: String,
        subtitle: String,
        author: String,
        bgPrimaryHex: String,
        accentGoldHex: String,
        accentRoseHex: String
    ) {
        viewModelScope.launch {
            repository.updateBookMetadataAndStyle(
                bookId = bookId,
                title = title,
                subtitle = subtitle,
                author = author,
                bgPrimaryHex = bgPrimaryHex,
                accentGoldHex = accentGoldHex,
                accentRoseHex = accentRoseHex
            )
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            if (activeBook.value?.id == bookId) {
                activeBook.value = null
            }
            repository.deleteBook(bookId)
        }
    }

    fun createShelf(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.createShelf(name, colorHex, iconName)
        }
    }

    fun deleteShelf(shelf: Shelf) {
        viewModelScope.launch {
            if (selectedShelfId.value == shelf.id) {
                selectedShelfId.value = null
            }
            repository.deleteShelf(shelf)
        }
    }

    fun addBookmark(title: String, scrollY: Int, progress: Float, excerpt: String) {
        val book = activeBook.value ?: return
        viewModelScope.launch {
            repository.addBookmark(book.id, title, scrollY, progress, excerpt)
        }
    }

    fun deleteBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun reloadSamples() {
        viewModelScope.launch {
            repository.reloadSampleBooks()
        }
    }

    fun getBookFile(book: Book): File {
        return repository.getBookAbsoluteFile(book)
    }
}
