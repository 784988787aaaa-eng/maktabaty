package com.maktabati.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maktabati.app.data.backup.CloudBackupManager
import com.maktabati.app.data.backup.CloudSyncState
import com.maktabati.app.data.db.AppDatabase
import com.maktabati.app.data.importer.BookImporter
import com.maktabati.app.data.importer.BookVisualTheme
import com.maktabati.app.data.importer.ConvertibleBookProposal
import com.maktabati.app.data.importer.HtmlCoverSnapshotGenerator
import com.maktabati.app.data.importer.ImportUriResult
import com.maktabati.app.data.importer.MultiBookArchiveProposal
import com.maktabati.app.data.importer.ParsedHtmlMetadata
import com.maktabati.app.data.importer.SmartBookConverter
import com.maktabati.app.data.importer.SmartHtmlPhaseMerger
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Bookmark
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.data.repository.BookRepository
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import com.maktabati.app.data.typography.UserTypographyPreferences
import com.maktabati.app.ui.localization.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@Immutable
sealed class ImportState {
    object Idle : ImportState()
    data class Loading(val message: String = "جاري استيراد وتحليل الكتاب...") : ImportState()
    data class Success(val book: Book, val count: Int = 1) : ImportState()
    data class Error(val message: String) : ImportState()
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = application.getSharedPreferences("maktabati_preferences", Context.MODE_PRIVATE)
    private val db = AppDatabase.getInstance(application)
    val repository = BookRepository(db.bookDao(), application)
    val cloudBackupManager = CloudBackupManager(application, db.bookDao(), preferences)

    private val queueManager = BookImportQueueManager()

    private val cloudSyncDelegate = CloudSyncUiDelegate(
        scope = viewModelScope,
        cloudBackupManager = cloudBackupManager,
        onPreferencesRestored = {
            isDarkTheme.value = preferences.getBoolean("isDarkTheme", false)
            viewMode.value = runCatching {
                ViewMode.valueOf(preferences.getString("viewMode", ViewMode.GRID.name) ?: ViewMode.GRID.name)
            }.getOrDefault(ViewMode.GRID)
            gridCardWidthDp.value = preferences.getFloat("gridCardWidthDp", 112f).coerceIn(82f, 175f)
            _appLanguage.value = AppLanguage.fromCode(preferences.getString("appLanguage", AppLanguage.AR.code))
            userTypographyPreferences.value = MaktabatiFontRegistry.loadUserTypographyPreferences(
                preferences,
                getApplication()
            )
        }
    )

    val connectedGoogleEmail: StateFlow<String?> = cloudSyncDelegate.connectedGoogleEmail
    val lastBackupTimeText: StateFlow<String?> = cloudSyncDelegate.lastBackupTimeText
    val cloudSyncState: StateFlow<CloudSyncState> = cloudSyncDelegate.cloudSyncState

    val shelves: StateFlow<List<Shelf>> = repository.allShelves
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rawBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedBookIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedBookIds: StateFlow<Set<String>> = _selectedBookIds.asStateFlow()

    val selectedShelfId = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow(FilterType.ALL)
    val viewMode = MutableStateFlow(
        runCatching { ViewMode.valueOf(preferences.getString("viewMode", ViewMode.GRID.name) ?: ViewMode.GRID.name) }
            .getOrDefault(ViewMode.GRID)
    )
    val isDarkTheme = MutableStateFlow(preferences.getBoolean("isDarkTheme", false))

    private val _appLanguage = MutableStateFlow(
        AppLanguage.fromCode(preferences.getString("appLanguage", AppLanguage.AR.code))
    )
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    val gridCardWidthDp = MutableStateFlow(
        preferences.getFloat("gridCardWidthDp", 112f).coerceIn(82f, 175f)
    )
    val userTypographyPreferences = MutableStateFlow(
        MaktabatiFontRegistry.loadUserTypographyPreferences(preferences, application)
    )

    val importState = MutableStateFlow<ImportState>(ImportState.Idle)

    val pendingMultiBookProposal: MutableStateFlow<MultiBookArchiveProposal?> = queueManager.pendingMultiBookProposal
    val pendingConversionProposal: MutableStateFlow<ConvertibleBookProposal?> = queueManager.pendingConversionProposal

    val isClipboardStudioOpen = MutableStateFlow(false)
    val clipboardStudioInitialText = MutableStateFlow("")

    val isOpeningExternalFile = MutableStateFlow(false)

    private fun filterBooksList(
        books: List<Book>,
        shelfId: String?,
        query: String,
        filter: FilterType
    ): List<Book> {
        return books.filter { book ->
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
    }

    val filteredBooks: StateFlow<List<Book>> = combine(
        rawBooks,
        selectedShelfId,
        searchQuery,
        filterType
    ) { books, shelfId, query, filter ->
        filterBooksList(books, shelfId, query, filter)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeBook = MutableStateFlow<Book?>(null)
    val activeBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    private var bookmarksJob: Job? = null

    init {
        viewModelScope.launch {
            if (isOpeningExternalFile.value) {
                delay(600)
            }
            repository.initializeDefaultDataIfNeeded()
            cloudSyncDelegate.refreshAccountInfo()
        }
    }

    fun ensureLibraryCoversAndPalettes() {
        viewModelScope.launch {
            repository.ensureBooksCoversAndTruePalettes()
        }
    }

    fun toggleDarkTheme() {
        val next = !isDarkTheme.value
        isDarkTheme.value = next
        val syncedReaderTheme = if (next) "NIGHT" else "ORIGINAL"
        preferences.edit()
            .putBoolean("isDarkTheme", next)
            .putString("readerTheme", syncedReaderTheme)
            .apply()
    }

    fun toggleAppLanguage() {
        val next = if (_appLanguage.value == AppLanguage.AR) AppLanguage.EN else AppLanguage.AR
        setAppLanguage(next)
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
        preferences.edit().putString("appLanguage", language.code).apply()
    }

    fun updateUserTypographyPreferences(newPrefs: UserTypographyPreferences) {
        userTypographyPreferences.value = newPrefs
        MaktabatiFontRegistry.saveUserTypographyPreferences(preferences, newPrefs)
    }

    fun scaleGridCardWidth(zoomFactor: Float) {
        val next = (gridCardWidthDp.value * zoomFactor).coerceIn(82f, 175f)
        if (kotlin.math.abs(next - gridCardWidthDp.value) > 0.2f) {
            gridCardWidthDp.value = next
            preferences.edit().putFloat("gridCardWidthDp", next).apply()
        }
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

    fun openExternalUriImmediately(uri: Uri) {
        isOpeningExternalFile.value = true
        viewModelScope.launch {
            if (BookImporter.isConvertibleDocumentUri(getApplication(), uri)) {
                val inspectResult = repository.inspectConvertibleDocument(uri)
                isOpeningExternalFile.value = false
                inspectResult.onSuccess { proposal ->
                    queueManager.enqueueConversionProposals(listOf(proposal))
                }.onFailure { err ->
                    importState.value = ImportState.Error(err.message ?: "تعذر تحليل وتحويل الملف المحدد")
                }
                return@launch
            }

            val result = repository.importOrInspectBook(
                uri = uri,
                customTitle = null,
                onQuickBookStaged = { stagedBook ->
                    activeBook.value = stagedBook
                    isOpeningExternalFile.value = false
                    observeBookmarksFor(stagedBook.id)
                }
            )
            result.onSuccess { outcome ->
                when (outcome) {
                    is ImportUriResult.SingleBookImported -> {
                        val fullBook = outcome.book
                        val current = activeBook.value
                        if (current == null || current.id == fullBook.id) {
                            activeBook.value = fullBook
                        }
                        isOpeningExternalFile.value = false
                        observeBookmarksFor(fullBook.id)
                        if (connectedGoogleEmail.value != null) {
                            triggerAutoCloudBackup()
                        }
                    }
                    is ImportUriResult.MultiBookArchiveDetected -> {
                        isOpeningExternalFile.value = false
                        queueManager.enqueueProposals(listOf(outcome.proposal))
                    }
                }
            }.onFailure { err ->
                isOpeningExternalFile.value = false
                importState.value = ImportState.Error(err.message ?: "تعذر فتح الملف المحدد")
            }
        }
    }

    fun importBook(uri: Uri, customTitle: String? = null, onComplete: ((Book?) -> Unit)? = null) {
        viewModelScope.launch {
            if (customTitle == null && BookImporter.isConvertibleDocumentUri(getApplication(), uri)) {
                importState.value = ImportState.Loading("جاري قراءة النص واستخراج الفصول والعناوين...")
                val inspectResult = repository.inspectConvertibleDocument(uri)
                inspectResult.onSuccess { proposal ->
                    importState.value = ImportState.Idle
                    queueManager.enqueueConversionProposals(listOf(proposal))
                    onComplete?.invoke(null)
                }.onFailure { err ->
                    importState.value = ImportState.Error(err.message ?: "تعذر قراءة وتحويل الملف النصي")
                    onComplete?.invoke(null)
                }
                return@launch
            }

            importState.value = ImportState.Loading("جاري فحص واستيراد الملف...")
            val result = repository.importOrInspectBook(uri, customTitle)
            result.onSuccess { outcome ->
                when (outcome) {
                    is ImportUriResult.SingleBookImported -> {
                        importState.value = ImportState.Success(outcome.book, 1)
                        onComplete?.invoke(outcome.book)
                        if (connectedGoogleEmail.value != null) {
                            triggerAutoCloudBackup()
                        }
                    }
                    is ImportUriResult.MultiBookArchiveDetected -> {
                        importState.value = ImportState.Idle
                        queueManager.enqueueProposals(listOf(outcome.proposal))
                        onComplete?.invoke(null)
                    }
                }
            }.onFailure { err ->
                importState.value = ImportState.Error(err.message ?: "تعذر استيراد الملف المحدد")
                onComplete?.invoke(null)
            }
        }
    }

    fun confirmConvertAndImportBook(
        proposal: ConvertibleBookProposal? = pendingConversionProposal.value,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme? = null,
        onComplete: ((Book?) -> Unit)? = null
    ) {
        val activeProposal = proposal ?: return
        viewModelScope.launch {
            val chosenTheme = theme ?: activeProposal.recommendedTheme
            importState.value = ImportState.Loading("جاري صياغة الكتاب التفاعلي الفاخر بحلة «${chosenTheme.arabicName}»...")
            val result = repository.convertAndImportProposal(
                proposal = activeProposal,
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                customAuthor = customAuthor,
                theme = chosenTheme
            )
            result.onSuccess { book ->
                queueManager.advanceNextConversionProposalOrClear()
                importState.value = ImportState.Success(book, 1)
                if (connectedGoogleEmail.value != null) {
                    triggerAutoCloudBackup()
                }
                onComplete?.invoke(book)
            }.onFailure { err ->
                queueManager.advanceNextConversionProposalOrClear()
                importState.value = ImportState.Error(err.message ?: "تعذر إتمام تحويل الكتاب")
                onComplete?.invoke(null)
            }
        }
    }

    fun cancelConversionProposal() {
        pendingConversionProposal.value?.cleanupStagedFile()
        queueManager.advanceNextConversionProposalOrClear()
    }

    fun readSystemClipboardText(): String {
        return runCatching {
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clip = clipboard?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val directText = item.text?.toString()
                val coercedText = if (directText.isNullOrBlank()) {
                    item.coerceToText(getApplication())?.toString()
                } else {
                    directText
                }
                coercedText.orEmpty().trim()
            } else {
                ""
            }
        }.getOrDefault("")
    }

    fun pasteFromClipboardAsBook(explicitText: String? = null) {
        val resolvedText = if (!explicitText.isNullOrBlank()) {
            explicitText.trim()
        } else {
            readSystemClipboardText()
        }
        if (resolvedText.isBlank()) {
            importState.value = ImportState.Error("الحافظة فارغة حالياً. انسخ أي نص أولاً ثم اختر «لصق من الحافظة» لتحويله مباشرة إلى كتاب.")
            return
        }
        if (SmartHtmlPhaseMerger.isCompleteReadyHtmlBook(resolvedText)) {
            viewModelScope.launch {
                importState.value = ImportState.Loading("جاري استيراد كتاب HTML التفاعلي الجاهز من الحافظة...")
                val result = repository.importRawHtmlBook(
                    rawHtml = SmartHtmlPhaseMerger.unwrapMarkdownCodeFence(resolvedText),
                    fallbackName = "كتاب تفاعلي من الحافظة"
                )
                result.onSuccess { book ->
                    importState.value = ImportState.Success(book, 1)
                    openBook(book)
                    if (connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                }.onFailure { err ->
                    importState.value = ImportState.Error(err.message ?: "تعذر استيراد كود HTML من الحافظة")
                }
            }
            return
        }
        if (resolvedText.length >= 50_000) {
            viewModelScope.launch {
                importState.value = ImportState.Loading("جاري قراءة النص وتحليل الفصول...")
                val result = repository.inspectRawText(
                    rawText = resolvedText,
                    sourceLabel = "الحافظة"
                )
                result.onSuccess { proposal ->
                    importState.value = ImportState.Idle
                    queueManager.enqueueConversionProposals(listOf(proposal))
                }.onFailure { err ->
                    importState.value = ImportState.Error(err.message ?: "تعذر تحليل النص المنسوخ من الحافظة")
                }
            }
        } else {
            runCatching {
                SmartBookConverter.inspectRawText(
                    rawText = resolvedText,
                    sourceLabel = "الحافظة",
                    stagingCacheDir = getApplication<Application>().cacheDir
                )
            }.onSuccess { proposal ->
                queueManager.enqueueConversionProposals(listOf(proposal))
            }.onFailure { err ->
                if (err is OutOfMemoryError || err is StackOverflowError) {
                    System.gc()
                }
                importState.value = ImportState.Error(err.message ?: "تعذر تحليل النص المنسوخ من الحافظة")
            }
        }
    }

    fun openClipboardStudio(initialText: String = "") {
        pasteFromClipboardAsBook(initialText)
    }

    fun closeClipboardStudio() {
        isClipboardStudioOpen.value = false
    }

    fun convertRawTextAndImportBook(
        rawText: String,
        customTitle: String? = null,
        customSubtitle: String? = null,
        customAuthor: String? = null,
        theme: BookVisualTheme? = null,
        onComplete: ((Book?) -> Unit)? = null
    ) {
        val cleanText = rawText.trim()
        if (cleanText.isBlank()) {
            importState.value = ImportState.Error("يرجى لصق أو كتابة نص لتحويله إلى كتاب تفاعلي")
            return
        }
        isClipboardStudioOpen.value = false
        viewModelScope.launch {
            if (SmartHtmlPhaseMerger.isCompleteReadyHtmlBook(cleanText)) {
                importState.value = ImportState.Loading("جاري استيراد كتاب HTML التفاعلي الجاهز...")
                val result = repository.importRawHtmlBook(
                    rawHtml = SmartHtmlPhaseMerger.unwrapMarkdownCodeFence(cleanText),
                    fallbackName = customTitle?.takeIf { it.isNotBlank() } ?: "كتاب تفاعلي",
                    customTitle = customTitle?.takeIf { it.isNotBlank() }
                )
                result.onSuccess { book ->
                    importState.value = ImportState.Success(book, 1)
                    if (connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                    onComplete?.invoke(book)
                }.onFailure { err ->
                    importState.value = ImportState.Error(err.message ?: "تعذر استيراد كتاب HTML الجاهز")
                    onComplete?.invoke(null)
                }
                return@launch
            }

            val chosenName = theme?.arabicName ?: "قالب النشر المختار"
            importState.value = ImportState.Loading("جاري صياغة الكتاب التفاعلي الفاخر بحلة «$chosenName»...")
            val result = repository.convertAndImportRawText(
                rawText = cleanText,
                customTitle = customTitle,
                customSubtitle = customSubtitle,
                customAuthor = customAuthor,
                theme = theme
            )
            result.onSuccess { book ->
                importState.value = ImportState.Success(book, 1)
                if (connectedGoogleEmail.value != null) {
                    triggerAutoCloudBackup()
                }
                onComplete?.invoke(book)
            }.onFailure { err ->
                importState.value = ImportState.Error(err.message ?: "تعذر صياغة وتحويل النص إلى كتاب")
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
            importState.value = ImportState.Loading("جاري استيراد وتحليل ${uris.size} ملفات...")
            val result = repository.importOrInspectMultipleBooks(uris)
            result.onSuccess { batch ->
                if (batch.multiBookProposals.isNotEmpty()) {
                    queueManager.enqueueProposals(batch.multiBookProposals)
                }
                if (batch.importedSingleBooks.isNotEmpty()) {
                    importState.value = ImportState.Success(batch.importedSingleBooks.first(), batch.importedSingleBooks.size)
                    if (connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                } else {
                    importState.value = ImportState.Idle
                }
                onComplete?.invoke(batch.importedSingleBooks)
            }.onFailure { err ->
                importState.value = ImportState.Error(err.message ?: "تعذر استيراد الكتب المحددة")
                onComplete?.invoke(emptyList())
            }
        }
    }

    fun confirmMultiBookImportWithNewShelf(proposal: MultiBookArchiveProposal? = pendingMultiBookProposal.value) {
        val activeProposal = proposal ?: return
        viewModelScope.launch {
            importState.value = ImportState.Loading("جاري إنشاء رف «${activeProposal.zipBaseName}» وإضافة الكتب...")
            runCatching {
                repository.commitMultiBookProposalToNewShelf(activeProposal)
            }.onSuccess { (createdShelf, books) ->
                selectedShelfId.value = createdShelf.id
                filterType.value = FilterType.ALL
                queueManager.advanceNextProposalOrClear()
                if (books.isNotEmpty()) {
                    importState.value = ImportState.Success(books.first(), books.size)
                    if (connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                } else {
                    importState.value = ImportState.Idle
                }
            }.onFailure { err ->
                queueManager.advanceNextProposalOrClear()
                importState.value = ImportState.Error(err.message ?: "تعذر إتمام إضافة كتب الأرشيف")
            }
        }
    }

    fun confirmMultiBookImportStandalone(proposal: MultiBookArchiveProposal? = pendingMultiBookProposal.value) {
        val activeProposal = proposal ?: return
        viewModelScope.launch {
            importState.value = ImportState.Loading("جاري إضافة ${activeProposal.archiveBooksCount} كتب إلى مكتبتي...")
            runCatching {
                repository.commitMultiBookProposalStandalone(activeProposal)
            }.onSuccess { books ->
                queueManager.advanceNextProposalOrClear()
                if (books.isNotEmpty()) {
                    importState.value = ImportState.Success(books.first(), books.size)
                    if (connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                } else {
                    importState.value = ImportState.Idle
                }
            }.onFailure { err ->
                queueManager.advanceNextProposalOrClear()
                importState.value = ImportState.Error(err.message ?: "تعذر إتمام إضافة كتب الأرشيف")
            }
        }
    }

    fun cancelMultiBookImport(proposal: MultiBookArchiveProposal? = pendingMultiBookProposal.value) {
        val activeProposal = proposal ?: return
        repository.cancelMultiBookProposal(activeProposal)
        queueManager.advanceNextProposalOrClear()
    }

    fun clearImportState() {
        importState.value = ImportState.Idle
    }

    fun prepareGoogleSignInIntent(): Intent? = cloudSyncDelegate.prepareGoogleSignInIntent()

    fun handleGoogleSignInResult(data: Intent?, resultCode: Int) =
        cloudSyncDelegate.handleGoogleSignInResult(data, resultCode)

    fun signOutGoogleCloud() = cloudSyncDelegate.signOutGoogleCloud()

    fun backupLibraryToCloudNow() = cloudSyncDelegate.backupLibraryToCloudNow()

    fun restoreLibraryFromCloudNow() = cloudSyncDelegate.restoreLibraryFromCloudNow()

    fun triggerAutoCloudBackup() = cloudSyncDelegate.triggerAutoCloudBackup()

    fun dismissCloudSyncState() = cloudSyncDelegate.dismissCloudSyncState()

    private fun observeBookmarksFor(bookId: String) {
        bookmarksJob?.cancel()
        bookmarksJob = viewModelScope.launch {
            repository.getBookmarks(bookId).collect { bookmarks ->
                activeBookmarks.value = bookmarks
            }
        }
    }

    fun openBook(book: Book) {
        activeBook.value = book
        observeBookmarksFor(book.id)
    }

    fun closeReader() {
        isOpeningExternalFile.value = false
        activeBook.value = null
    }

    fun saveProgress(bookId: String, progress: Float, scrollY: Int, currentPage: Int = 1, actualTotalPages: Int = 0) {
        val currentActive = activeBook.value
        if (currentActive != null && currentActive.id == bookId) {
            val resolvedTotal = if (actualTotalPages > 0) actualTotalPages else currentActive.pageCount
            val resolvedProg = if (resolvedTotal > 0 && currentPage >= resolvedTotal) 1.0f else progress.coerceIn(0f, 1f)
            activeBook.value = currentActive.copy(
                readingProgress = resolvedProg,
                scrollY = scrollY,
                currentPage = currentPage.coerceAtLeast(1),
                pageCount = resolvedTotal.coerceAtLeast(1)
            )
        }
        viewModelScope.launch {
            repository.updateReadingProgress(bookId, progress, scrollY, currentPage, actualTotalPages)
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
            withContext(Dispatchers.IO) {
                runCatching {
                    val updatedBook = repository.getBookOnce(bookId) ?: return@withContext
                    val bookDir = repository.getBookDirectory(updatedBook)
                    val snapshotFile = HtmlCoverSnapshotGenerator.getCoverSnapshotFile(bookDir)
                    if (snapshotFile.exists()) {
                        val entryFile = repository.getBookAbsoluteFile(updatedBook)
                        val htmlContent = if (entryFile.isFile) entryFile.readText(Charsets.UTF_8) else ""
                        val tempCheckFile = File(bookDir, ".explicit_cover_check.tmp")
                        val hasExplicitImage = HtmlCoverSnapshotGenerator.tryExtractExplicitCoverImage(
                            bookDir = bookDir,
                            htmlContent = htmlContent,
                            targetSnapshotFile = tempCheckFile
                        )
                        if (tempCheckFile.exists()) tempCheckFile.delete()

                        if (!hasExplicitImage) {
                            val metadata = ParsedHtmlMetadata(
                                title = title,
                                subtitle = subtitle,
                                author = author,
                                description = updatedBook.description,
                                language = updatedBook.language,
                                isRtl = updatedBook.isRtl,
                                pageCount = updatedBook.pageCount,
                                accentRoseHex = accentRoseHex,
                                accentGoldHex = accentGoldHex,
                                bgPrimaryHex = bgPrimaryHex,
                                coverGradientStart = updatedBook.coverGradientStart,
                                coverGradientEnd = updatedBook.coverGradientEnd,
                                hasExternalCss = updatedBook.hasExternalCss,
                                hasExternalJs = updatedBook.hasExternalJs,
                                tableOfContents = emptyList(),
                                hasCoverPage = true
                            )
                            val rendered = HtmlCoverSnapshotGenerator.renderHtmlCoverPageSnapshot(
                                htmlContent = htmlContent,
                                metadata = metadata,
                                targetFile = snapshotFile,
                                context = getApplication()
                            )
                            if (rendered) {
                                HtmlCoverSnapshotGenerator.notifySnapshotUpdated()
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteBook(bookId: String) {
        viewModelScope.launch {
            if (activeBook.value?.id == bookId) {
                activeBook.value = null
            }
            if (bookId in _selectedBookIds.value) {
                _selectedBookIds.value = _selectedBookIds.value - bookId
            }
            repository.deleteBook(bookId)
            if (connectedGoogleEmail.value != null) {
                triggerAutoCloudBackup()
            }
        }
    }

    fun toggleBookSelection(bookId: String) {
        if (bookId.isBlank()) return
        val current = _selectedBookIds.value
        _selectedBookIds.value = if (bookId in current) {
            current - bookId
        } else {
            current + bookId
        }
    }

    fun selectAllVisibleBooks() {
        val currentFiltered = filteredBooks.value.ifEmpty {
            val sourceBooks = rawBooks.value.ifEmpty {
                kotlinx.coroutines.runBlocking(Dispatchers.IO) {
                    db.bookDao().getAllBooksOnce()
                }
            }
            filterBooksList(
                books = sourceBooks,
                shelfId = selectedShelfId.value,
                query = searchQuery.value,
                filter = filterType.value
            )
        }
        val visibleIds = currentFiltered.map { it.id }.toSet()
        if (visibleIds.isEmpty()) return
        val currentSelected = _selectedBookIds.value
        _selectedBookIds.value = if (currentSelected.containsAll(visibleIds)) {
            currentSelected - visibleIds
        } else {
            currentSelected + visibleIds
        }
    }

    fun clearSelection() {
        _selectedBookIds.value = emptySet()
    }

    fun deleteSelectedBooks(targetIds: Set<String> = emptySet(), onComplete: (() -> Unit)? = null) {
        val ids = (if (targetIds.isNotEmpty()) targetIds else _selectedBookIds.value)
            .filter { it.isNotBlank() }
            .toSet()
        if (ids.isEmpty()) {
            onComplete?.invoke()
            return
        }
        _selectedBookIds.value = _selectedBookIds.value - ids
        viewModelScope.launch {
            if (activeBook.value?.id in ids) {
                activeBook.value = null
            }
            repository.deleteBooksBatch(ids)
            if (connectedGoogleEmail.value != null) {
                triggerAutoCloudBackup()
            }
            onComplete?.invoke()
        }
    }

    fun moveSelectedBooksToShelf(shelfId: String?, onComplete: (() -> Unit)? = null) {
        val ids = _selectedBookIds.value
        if (ids.isEmpty()) {
            onComplete?.invoke()
            return
        }
        viewModelScope.launch {
            repository.updateBooksShelfBatch(ids, shelfId)
            clearSelection()
            if (connectedGoogleEmail.value != null) {
                triggerAutoCloudBackup()
            }
            onComplete?.invoke()
        }
    }

    fun createShelfAndMoveSelectedBooks(
        name: String,
        colorHex: String,
        iconName: String,
        onComplete: (() -> Unit)? = null
    ) {
        val ids = _selectedBookIds.value
        viewModelScope.launch {
            val createdShelf = repository.createShelf(name, colorHex, iconName)
            if (ids.isNotEmpty()) {
                repository.updateBooksShelfBatch(ids, createdShelf.id)
            }
            selectedShelfId.value = createdShelf.id
            clearSelection()
            if (connectedGoogleEmail.value != null) {
                triggerAutoCloudBackup()
            }
            onComplete?.invoke()
        }
    }

    fun shareSelectedBooks(context: Context) {
        val ids = _selectedBookIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val allSourceBooks = rawBooks.value.ifEmpty {
                withContext(Dispatchers.IO) { db.bookDao().getAllBooksOnce() }
            }
            val selectedBooks = allSourceBooks.filter { it.id in ids }
            if (selectedBooks.isEmpty()) {
                clearSelection()
                return@launch
            }
            if (selectedBooks.size == 1) {
                val singleBook = selectedBooks.first()
                clearSelection()
                shareBook(context, singleBook)
                return@launch
            }
            runCatching {
                val preparedFiles = withContext(Dispatchers.IO) {
                    repository.prepareSharedBooksBatch(selectedBooks)
                }
                val uris = ArrayList<Uri>()
                for (file in preparedFiles) {
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        uris.add(uri)
                    }
                }
                if (uris.isNotEmpty()) {
                    val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "*/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                        putExtra(Intent.EXTRA_SUBJECT, "كتب مختارة من منصة مكتبتي (${uris.size})")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            selectedBooks.joinToString(separator = "\n") { "• ${it.title}" }
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(
                        sendIntent,
                        "مشاركة (${uris.size}) كتب"
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
            }
            clearSelection()
        }
    }

    fun createShelf(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val createdShelf = repository.createShelf(name, colorHex, iconName)
            selectedShelfId.value = createdShelf.id
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

    fun getBookFile(book: Book): File {
        return repository.getBookAbsoluteFile(book)
    }

    fun shareBook(context: Context, book: Book) {
        viewModelScope.launch {
            runCatching {
                val targetFile = withContext(Dispatchers.IO) {
                    repository.prepareSharedBookFile(book)
                }
                if (targetFile.exists()) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        targetFile
                    )
                    val mimeType = if (book.fileCount > 1 || targetFile.extension.equals("zip", ignoreCase = true)) {
                        "application/zip"
                    } else {
                        "text/html"
                    }
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, book.title)
                        putExtra(Intent.EXTRA_TEXT, "كتاب «${book.title}» من منصة مكتبتي")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(sendIntent, "مشاركة كتاب «${book.title}»").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
            }
        }
    }
}
