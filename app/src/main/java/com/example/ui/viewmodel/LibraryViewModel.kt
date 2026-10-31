package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.CloudBackupManager
import com.example.data.backup.CloudSyncState
import com.example.data.backup.SignInOutcome
import com.example.data.db.AppDatabase
import com.example.data.importer.ImportUriResult
import com.example.data.importer.MultiBookArchiveProposal
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
import com.example.data.repository.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val cloudBackupManager = CloudBackupManager(application, db.bookDao(), preferences)

    private val _connectedGoogleEmail = MutableStateFlow(cloudBackupManager.getConnectedEmail())
    val connectedGoogleEmail: StateFlow<String?> = _connectedGoogleEmail.asStateFlow()

    private val _lastBackupTimeText = MutableStateFlow(cloudBackupManager.getLastBackupTimeText())
    val lastBackupTimeText: StateFlow<String?> = _lastBackupTimeText.asStateFlow()

    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    private var autoBackupJob: Job? = null

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
    val gridCardWidthDp = MutableStateFlow(
        preferences.getFloat("gridCardWidthDp", 112f).coerceIn(82f, 175f)
    )
    val userTypographyPreferences = MutableStateFlow(
        com.example.data.typography.MaktabatiFontRegistry.loadUserTypographyPreferences(preferences, application)
    )

    val importState = MutableStateFlow<ImportState>(ImportState.Idle)

    // Multi-book ZIP archive user choice state
    val pendingMultiBookProposal = MutableStateFlow<MultiBookArchiveProposal?>(null)
    private val proposalQueue = ArrayDeque<MultiBookArchiveProposal>()

    // True when launched directly via ACTION_VIEW / ACTION_SEND so LibraryScreen never flashes
    val isOpeningExternalFile = MutableStateFlow(false)

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
    private var bookmarksJob: Job? = null

    init {
        viewModelScope.launch {
            if (isOpeningExternalFile.value) {
                delay(600)
            }
            repository.initializeDefaultDataIfNeeded()
            _connectedGoogleEmail.value = cloudBackupManager.getConnectedEmail()
            _lastBackupTimeText.value = cloudBackupManager.getLastBackupTimeText()
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
        preferences.edit().putBoolean("isDarkTheme", next).apply()
    }

    fun updateUserTypographyPreferences(newPrefs: com.example.data.typography.UserTypographyPreferences) {
        userTypographyPreferences.value = newPrefs
        com.example.data.typography.MaktabatiFontRegistry.saveUserTypographyPreferences(preferences, newPrefs)
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

    private fun enqueueProposals(proposals: List<MultiBookArchiveProposal>) {
        if (proposals.isEmpty()) return
        for (p in proposals) {
            proposalQueue.addLast(p)
        }
        if (pendingMultiBookProposal.value == null) {
            pendingMultiBookProposal.value = proposalQueue.removeFirstOrNull()
        }
    }

    private fun advanceNextProposalOrClear() {
        pendingMultiBookProposal.value = proposalQueue.removeFirstOrNull()
    }

    /**
     * Direct-to-Reader fast path for external file intents (WhatsApp, Files, Telegram):
     * Immediately stages single HTML/ZIP books on disk and opens ReaderScreen, or shows MultiBookZipDialog if a multi-book ZIP is opened.
     */
    fun openExternalUriImmediately(uri: Uri) {
        isOpeningExternalFile.value = true
        viewModelScope.launch {
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
                        if (_connectedGoogleEmail.value != null) {
                            triggerAutoCloudBackup()
                        }
                    }
                    is ImportUriResult.MultiBookArchiveDetected -> {
                        isOpeningExternalFile.value = false
                        enqueueProposals(listOf(outcome.proposal))
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
            importState.value = ImportState.Loading("جاري فحص واستيراد الملف...")
            val result = repository.importOrInspectBook(uri, customTitle)
            result.onSuccess { outcome ->
                when (outcome) {
                    is ImportUriResult.SingleBookImported -> {
                        importState.value = ImportState.Success(outcome.book, 1)
                        onComplete?.invoke(outcome.book)
                        if (_connectedGoogleEmail.value != null) {
                            triggerAutoCloudBackup()
                        }
                    }
                    is ImportUriResult.MultiBookArchiveDetected -> {
                        importState.value = ImportState.Idle
                        enqueueProposals(listOf(outcome.proposal))
                        onComplete?.invoke(null)
                    }
                }
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
            importState.value = ImportState.Loading("جاري استيراد وتحليل ${uris.size} ملفات...")
            val result = repository.importOrInspectMultipleBooks(uris)
            result.onSuccess { batch ->
                if (batch.multiBookProposals.isNotEmpty()) {
                    enqueueProposals(batch.multiBookProposals)
                }
                if (batch.importedSingleBooks.isNotEmpty()) {
                    importState.value = ImportState.Success(batch.importedSingleBooks.first(), batch.importedSingleBooks.size)
                    if (_connectedGoogleEmail.value != null) {
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
                advanceNextProposalOrClear()
                if (books.isNotEmpty()) {
                    importState.value = ImportState.Success(books.first(), books.size)
                    if (_connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                } else {
                    importState.value = ImportState.Idle
                }
            }.onFailure { err ->
                advanceNextProposalOrClear()
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
                advanceNextProposalOrClear()
                if (books.isNotEmpty()) {
                    importState.value = ImportState.Success(books.first(), books.size)
                    if (_connectedGoogleEmail.value != null) {
                        triggerAutoCloudBackup()
                    }
                } else {
                    importState.value = ImportState.Idle
                }
            }.onFailure { err ->
                advanceNextProposalOrClear()
                importState.value = ImportState.Error(err.message ?: "تعذر إتمام إضافة كتب الأرشيف")
            }
        }
    }

    fun cancelMultiBookImport(proposal: MultiBookArchiveProposal? = pendingMultiBookProposal.value) {
        val activeProposal = proposal ?: return
        repository.cancelMultiBookProposal(activeProposal)
        advanceNextProposalOrClear()
    }

    fun clearImportState() {
        importState.value = ImportState.Idle
    }

    // =========================================================================
    // Google Cloud Backup & Restore Actions
    // =========================================================================

    fun prepareGoogleSignInIntent(): Intent? {
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return null
        }
        return cloudBackupManager.buildSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?, resultCode: Int) {
        when (val outcome = cloudBackupManager.handleSignInResult(data)) {
            is SignInOutcome.Success -> {
                _connectedGoogleEmail.value = outcome.email
                _cloudSyncState.value = CloudSyncState.Success("تم الاتصال بحساب Google بنجاح ☁️")
            }
            is SignInOutcome.Cancelled -> {
                // User cancelled account picker — do not show any error
            }
            is SignInOutcome.Error -> {
                if (resultCode == android.app.Activity.RESULT_CANCELED && data == null) {
                    // Silent cancel when user backed out without selecting an account
                    return
                }
                _cloudSyncState.value = CloudSyncState.Error(outcome.arabicMessage)
            }
        }
    }

    fun signOutGoogleCloud() {
        viewModelScope.launch {
            cloudBackupManager.signOut()
            _connectedGoogleEmail.value = null
            _cloudSyncState.value = CloudSyncState.Idle
        }
    }

    fun backupLibraryToCloudNow() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return
        }
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.Syncing("جاري حفظ النسخة الاحتياطية في السحابة...")
            val result = cloudBackupManager.createAndUploadBackup()
            result.onSuccess { timestampText ->
                _lastBackupTimeText.value = timestampText
                _cloudSyncState.value = CloudSyncState.Success("تم حفظ النسخة الاحتياطية في السحابة بنجاح ✨")
            }.onFailure { err ->
                _cloudSyncState.value = CloudSyncState.Error(
                    err.message ?: CloudBackupManager.MSG_GENERIC_CLOUD_ERROR
                )
            }
        }
    }

    fun restoreLibraryFromCloudNow() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return
        }
        viewModelScope.launch {
            _cloudSyncState.value = CloudSyncState.Syncing("جاري استرداد النسخة الاحتياطية من السحابة...")
            val result = cloudBackupManager.downloadAndRestoreBackup()
            result.onSuccess { restoredCount ->
                // Refresh preferences states immediately without restarting app
                isDarkTheme.value = preferences.getBoolean("isDarkTheme", false)
                viewMode.value = runCatching {
                    ViewMode.valueOf(preferences.getString("viewMode", ViewMode.GRID.name) ?: ViewMode.GRID.name)
                }.getOrDefault(ViewMode.GRID)
                gridCardWidthDp.value = preferences.getFloat("gridCardWidthDp", 112f).coerceIn(82f, 175f)
                userTypographyPreferences.value =
                    com.example.data.typography.MaktabatiFontRegistry.loadUserTypographyPreferences(
                        preferences,
                        getApplication()
                    )
                _lastBackupTimeText.value = cloudBackupManager.getLastBackupTimeText()
                _cloudSyncState.value = CloudSyncState.Success(
                    "تم استرداد النسخة الاحتياطية ($restoredCount كتاب) بنجاح ✨"
                )
            }.onFailure { err ->
                _cloudSyncState.value = CloudSyncState.Error(
                    err.message ?: CloudBackupManager.MSG_GENERIC_CLOUD_ERROR
                )
            }
        }
    }

    /**
     * Quiet background auto-backup triggered automatically whenever a new book is added
     * while the user's Google account is connected.
     */
    fun triggerAutoCloudBackup() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) return
        autoBackupJob?.cancel()
        autoBackupJob = viewModelScope.launch {
            val result = cloudBackupManager.createAndUploadBackup()
            result.onSuccess { timestampText ->
                _lastBackupTimeText.value = timestampText
            }
            // Quiet in background: do not interrupt reader with error dialogs on silent auto-sync
        }
    }

    fun dismissCloudSyncState() {
        _cloudSyncState.value = CloudSyncState.Idle
    }

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
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val updatedBook = repository.getBookOnce(bookId) ?: return@withContext
                    val bookDir = repository.getBookDirectory(updatedBook)
                    val snapshotFile = com.example.data.importer.HtmlCoverSnapshotGenerator.getCoverSnapshotFile(bookDir)
                    if (snapshotFile.exists()) {
                        val entryFile = repository.getBookAbsoluteFile(updatedBook)
                        val htmlContent = if (entryFile.isFile) entryFile.readText(Charsets.UTF_8) else ""
                        val tempCheckFile = File(bookDir, ".explicit_cover_check.tmp")
                        val hasExplicitImage = com.example.data.importer.HtmlCoverSnapshotGenerator.tryExtractExplicitCoverImage(
                            bookDir = bookDir,
                            htmlContent = htmlContent,
                            targetSnapshotFile = tempCheckFile
                        )
                        if (tempCheckFile.exists()) tempCheckFile.delete()

                        if (!hasExplicitImage) {
                            val metadata = com.example.data.importer.ParsedHtmlMetadata(
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
                            val rendered = com.example.data.importer.HtmlCoverSnapshotGenerator.renderHtmlCoverPageSnapshot(
                                htmlContent = htmlContent,
                                metadata = metadata,
                                targetFile = snapshotFile
                            )
                            if (rendered) {
                                com.example.data.importer.HtmlCoverSnapshotGenerator.notifySnapshotUpdated()
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
            repository.deleteBook(bookId)
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
                val targetFile = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
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
