package com.maktabati.app.ui.screens.library

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maktabati.app.data.backup.CloudSyncState
import com.maktabati.app.data.model.Book
import com.maktabati.app.ui.components.BookDetailDialog
import com.maktabati.app.ui.components.BookGridItem
import com.maktabati.app.ui.components.BookListItem
import com.maktabati.app.ui.components.CreateShelfDialog
import com.maktabati.app.ui.components.TypographySettingsDialog
import com.maktabati.app.ui.components.UnifiedShelfAndFilterRow
import com.maktabati.app.ui.components.studio.ClipboardBookStudioDialog
import com.maktabati.app.ui.components.studio.ConvertTextBookDialog
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.screens.library.components.AddBookFabMenu
import com.maktabati.app.ui.screens.library.components.CloudSyncBanner
import com.maktabati.app.ui.screens.library.components.LibraryTopAppBar
import com.maktabati.app.ui.screens.library.components.ReadingStatsHeaderBar
import com.maktabati.app.ui.screens.library.dialogs.AboutMaktabatiDialog
import com.maktabati.app.ui.screens.library.dialogs.BatchDeleteConfirmDialog
import com.maktabati.app.ui.screens.library.dialogs.BatchMoveToShelfDialog
import com.maktabati.app.ui.screens.library.dialogs.DeleteBookConfirmDialog
import com.maktabati.app.ui.screens.library.dialogs.ImportStateDialogs
import com.maktabati.app.ui.screens.library.dialogs.MultiBookZipDialog
import com.maktabati.app.ui.viewmodel.LibraryViewModel
import com.maktabati.app.ui.viewmodel.ViewMode
import kotlinx.coroutines.delay

@Composable
fun BatchSelectionActionBar(
    selectedCount: Int,
    allVisibleSelected: Boolean,
    onClearSelection: () -> Unit,
    onSelectAllToggle: () -> Unit,
    onMoveToShelfClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("batch_selection_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClearSelection,
                    modifier = Modifier.testTag("batch_clear_selection_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.batchClearSelection,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.batchSelectedCount(selectedCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TextButton(
                    onClick = onSelectAllToggle,
                    modifier = Modifier.testTag("batch_select_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = if (allVisibleSelected) strings.batchDeselectAll else strings.batchSelectAll,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (allVisibleSelected) strings.batchDeselectAll else strings.batchSelectAll,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = onMoveToShelfClick,
                    modifier = Modifier.testTag("batch_move_shelf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = strings.batchMoveToShelf,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.testTag("batch_share_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = strings.batchShare,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("batch_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.batchDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenBook: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val allRawBooks by viewModel.rawBooks.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val selectedShelfId by viewModel.selectedShelfId.collectAsStateWithLifecycle()
    val selectedBookIds by viewModel.selectedBookIds.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val gridCardWidthDp by viewModel.gridCardWidthDp.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val pendingMultiBookProposal by viewModel.pendingMultiBookProposal.collectAsStateWithLifecycle()
    val pendingConversionProposal by viewModel.pendingConversionProposal.collectAsStateWithLifecycle()
    val isClipboardStudioOpen by viewModel.isClipboardStudioOpen.collectAsStateWithLifecycle()
    val clipboardStudioInitialText by viewModel.clipboardStudioInitialText.collectAsStateWithLifecycle()
    val userTypographyPreferences by viewModel.userTypographyPreferences.collectAsStateWithLifecycle()
    val connectedGoogleEmail by viewModel.connectedGoogleEmail.collectAsStateWithLifecycle()
    val lastBackupTimeText by viewModel.lastBackupTimeText.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val cardScaleFactor = (gridCardWidthDp / 135f).coerceIn(0.68f, 1.25f)

    val isSelectionMode = selectedBookIds.isNotEmpty()
    val allVisibleSelected = remember(books, selectedBookIds) {
        books.isNotEmpty() && books.all { it.id in selectedBookIds }
    }

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    LaunchedEffect(cloudSyncState) {
        if (cloudSyncState is CloudSyncState.Success) {
            delay(4000)
            viewModel.dismissCloudSyncState()
        }
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var moveSelectedBooksAfterCreatingShelf by remember { mutableStateOf(false) }
    var showBatchMoveDialog by remember { mutableStateOf(false) }
    var showBatchDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedBookForDetails by remember { mutableStateOf<Book?>(null) }
    var bookPendingDelete by remember { mutableStateOf<Book?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTypographySettingsDialog by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data, result.resultCode)
    }

    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()

    val isScrollingDown by remember {
        derivedStateOf {
            if (viewMode == ViewMode.GRID) {
                gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 15
            } else {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 15
            }
        }
    }

    val multiFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importMultipleBooks(uris) { importedList ->
                if (importedList.size == 1) {
                    onOpenBook(importedList.first())
                }
            }
        }
    }

    Scaffold(
        topBar = {
            LibraryTopAppBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onActivateSearch = { isSearchActive = true },
                onCloseSearch = {
                    viewModel.setSearchQuery("")
                    isSearchActive = false
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleDarkTheme() },
                viewMode = viewMode,
                onToggleViewMode = { viewModel.toggleViewMode() },
                appLanguage = appLanguage,
                onToggleLanguage = { viewModel.toggleAppLanguage() },
                useCustomTypography = userTypographyPreferences.useCustomTypography,
                onOpenTypographySettings = { showTypographySettingsDialog = true },
                connectedGoogleEmail = connectedGoogleEmail,
                lastBackupTimeText = lastBackupTimeText,
                onGoogleSignInClick = {
                    val signInIntent = viewModel.prepareGoogleSignInIntent()
                    if (signInIntent != null) {
                        googleSignInLauncher.launch(signInIntent)
                    }
                },
                onCloudBackupNow = { viewModel.backupLibraryToCloudNow() },
                onCloudRestoreNow = { viewModel.restoreLibraryFromCloudNow() },
                onCloudSignOut = { viewModel.signOutGoogleCloud() },
                onOpenAboutDialog = { showAboutDialog = true }
            )
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            if (!isSelectionMode) {
                AddBookFabMenu(
                    onPasteFromClipboard = { viewModel.pasteFromClipboardAsBook() },
                    onPickLocalFiles = {
                        multiFilePickerLauncher.launch(
                            arrayOf(
                                "text/html",
                                "application/xhtml+xml",
                                "application/zip",
                                "application/x-zip-compressed",
                                "text/plain",
                                "text/markdown",
                                "text/x-markdown",
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/vnd.oasis.opendocument.text",
                                "application/rtf",
                                "text/rtf",
                                "application/epub+zip",
                                "*/*"
                            )
                        )
                    }
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CloudSyncBanner(
                cloudSyncState = cloudSyncState,
                onDismiss = { viewModel.dismissCloudSyncState() }
            )

            AnimatedVisibility(
                visible = isSelectionMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BatchSelectionActionBar(
                    selectedCount = selectedBookIds.size,
                    allVisibleSelected = allVisibleSelected,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAllToggle = { viewModel.selectAllVisibleBooks() },
                    onMoveToShelfClick = { showBatchMoveDialog = true },
                    onShareClick = { viewModel.shareSelectedBooks(context) },
                    onDeleteClick = { showBatchDeleteConfirmDialog = true }
                )
            }

            ReadingStatsHeaderBar(
                allRawBooks = allRawBooks,
                isScrollingDown = isScrollingDown
            )

            UnifiedShelfAndFilterRow(
                shelves = shelves,
                selectedShelfId = selectedShelfId,
                filterType = filterType,
                onSelectFilter = { viewModel.setFilterType(it) },
                onSelectShelf = { viewModel.selectShelf(it) },
                onAddShelfClick = {
                    moveSelectedBooksAfterCreatingShelf = false
                    showCreateShelfDialog = true
                },
                onDeleteShelf = { viewModel.deleteShelf(it) }
            )

            if (books.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                if (isEn) "No books match your search query" else "لا توجد كتب تطابق عبارة البحث"
                            } else {
                                strings.emptyLibraryTitle
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.emptyLibrarySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = gridCardWidthDp.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy((gridCardWidthDp * 0.095f).dp),
                        verticalArrangement = Arrangement.spacedBy((gridCardWidthDp * 0.11f).dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        if (event.changes.count { it.pressed } >= 2) {
                                            val zoom = event.calculateZoom()
                                            if (zoom != 1f && zoom > 0f) {
                                                viewModel.scaleGridCardWidth(zoom)
                                                event.changes.forEach { it.consume() }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                            .testTag("books_grid")
                    ) {
                        items(books, key = { it.id }) { book ->
                            val shelf = shelves.find { it.id == book.shelfId }
                            val isBookSelected = book.id in selectedBookIds
                            BookGridItem(
                                book = book,
                                shelfName = shelf?.name,
                                onClick = { onOpenBook(book) },
                                onFavoriteToggle = { viewModel.toggleFavorite(book) },
                                onDetailsClick = { selectedBookForDetails = book },
                                onShareClick = { viewModel.shareBook(context, book) },
                                onDeleteClick = { bookPendingDelete = book },
                                isSelectionMode = isSelectionMode,
                                isSelected = isBookSelected,
                                onToggleSelect = { viewModel.toggleBookSelection(book.id) },
                                scaleFactor = cardScaleFactor
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("books_list")
                    ) {
                        items(books, key = { it.id }) { book ->
                            val shelf = shelves.find { it.id == book.shelfId }
                            val isBookSelected = book.id in selectedBookIds
                            BookListItem(
                                book = book,
                                shelfName = shelf?.name,
                                onClick = { onOpenBook(book) },
                                onFavoriteToggle = { viewModel.toggleFavorite(book) },
                                onDetailsClick = { selectedBookForDetails = book },
                                onShareClick = { viewModel.shareBook(context, book) },
                                onDeleteClick = { bookPendingDelete = book },
                                isSelectionMode = isSelectionMode,
                                isSelected = isBookSelected,
                                onToggleSelect = { viewModel.toggleBookSelection(book.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateShelfDialog) {
        CreateShelfDialog(
            onDismiss = {
                showCreateShelfDialog = false
                moveSelectedBooksAfterCreatingShelf = false
            },
            onConfirm = { name, colorHex, iconName ->
                if (moveSelectedBooksAfterCreatingShelf && selectedBookIds.isNotEmpty()) {
                    viewModel.createShelfAndMoveSelectedBooks(name, colorHex, iconName)
                } else {
                    viewModel.createShelf(name, colorHex, iconName)
                }
                moveSelectedBooksAfterCreatingShelf = false
                showCreateShelfDialog = false
            }
        )
    }

    if (showBatchMoveDialog) {
        BatchMoveToShelfDialog(
            selectedCount = selectedBookIds.size,
            shelves = shelves,
            onSelectShelf = { targetShelfId ->
                showBatchMoveDialog = false
                viewModel.moveSelectedBooksToShelf(targetShelfId)
            },
            onCreateNewShelfAndMove = {
                showBatchMoveDialog = false
                moveSelectedBooksAfterCreatingShelf = true
                showCreateShelfDialog = true
            },
            onDismiss = { showBatchMoveDialog = false }
        )
    }

    if (showBatchDeleteConfirmDialog) {
        BatchDeleteConfirmDialog(
            selectedCount = selectedBookIds.size,
            onConfirmBatchDelete = {
                showBatchDeleteConfirmDialog = false
                viewModel.deleteSelectedBooks()
            },
            onDismiss = { showBatchDeleteConfirmDialog = false }
        )
    }

    selectedBookForDetails?.let { book ->
        BookDetailDialog(
            book = book,
            shelves = shelves,
            onDismiss = { selectedBookForDetails = null },
            onOpenBook = { onOpenBook(book) },
            onSaveMetadataAndStyle = { newTitle, newSubtitle, newAuthor, newShelfId, bgHex, goldHex, roseHex ->
                viewModel.updateBookMetadataAndStyle(
                    bookId = book.id,
                    title = newTitle,
                    subtitle = newSubtitle,
                    author = newAuthor,
                    bgPrimaryHex = bgHex,
                    accentGoldHex = goldHex,
                    accentRoseHex = roseHex
                )
                viewModel.moveBookToShelf(book.id, newShelfId)
            },
            onDeleteBook = {
                viewModel.deleteBook(book.id)
                selectedBookForDetails = null
            },
            onShareBook = {
                viewModel.shareBook(context, book)
            }
        )
    }

    bookPendingDelete?.let { book ->
        DeleteBookConfirmDialog(
            book = book,
            onConfirmDelete = {
                viewModel.deleteBook(book.id)
                bookPendingDelete = null
            },
            onDismiss = { bookPendingDelete = null }
        )
    }

    ImportStateDialogs(
        importState = importState,
        onClearImportState = { viewModel.clearImportState() }
    )

    if (showAboutDialog) {
        AboutMaktabatiDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    pendingMultiBookProposal?.let { proposal ->
        MultiBookZipDialog(
            proposal = proposal,
            onAddInNewShelf = { viewModel.confirmMultiBookImportWithNewShelf(proposal) },
            onAddStandalone = { viewModel.confirmMultiBookImportStandalone(proposal) },
            onCancel = { viewModel.cancelMultiBookImport(proposal) }
        )
    }

    pendingConversionProposal?.let { proposal ->
        ConvertTextBookDialog(
            proposal = proposal,
            onConfirmConvert = { title, subtitle, author, theme ->
                viewModel.confirmConvertAndImportBook(
                    proposal = proposal,
                    customTitle = title,
                    customSubtitle = subtitle,
                    customAuthor = author,
                    theme = theme
                )
            },
            onDismiss = { viewModel.cancelConversionProposal() }
        )
    }

    if (isClipboardStudioOpen) {
        ClipboardBookStudioDialog(
            initialText = clipboardStudioInitialText,
            onConfirmCreateBook = { rawText, title, subtitle, author, theme ->
                viewModel.convertRawTextAndImportBook(
                    rawText = rawText,
                    customTitle = title,
                    customSubtitle = subtitle,
                    customAuthor = author,
                    theme = theme
                ) { createdBook ->
                    if (createdBook != null) {
                        onOpenBook(createdBook)
                    }
                }
            },
            onDismiss = { viewModel.closeClipboardStudio() }
        )
    }

    if (showTypographySettingsDialog) {
        TypographySettingsDialog(
            currentPreferences = userTypographyPreferences,
            onUpdatePreferences = { updated ->
                viewModel.updateUserTypographyPreferences(updated)
            },
            onDismiss = { showTypographySettingsDialog = false }
        )
    }
}
