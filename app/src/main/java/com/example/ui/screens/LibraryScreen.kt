package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Book
import com.example.ui.components.BookDetailDialog
import com.example.ui.components.BookGridItem
import com.example.ui.components.BookListItem
import com.example.ui.components.CreateShelfDialog
import com.example.ui.components.UnifiedShelfAndFilterRow
import com.example.ui.viewmodel.FilterType
import com.example.ui.viewmodel.ImportState
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenBook: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val allRawBooks by viewModel.rawBooks.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val selectedShelfId by viewModel.selectedShelfId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val gridCardWidthDp by viewModel.gridCardWidthDp.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val cardScaleFactor = (gridCardWidthDp / 135f).coerceIn(0.68f, 1.25f)

    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var selectedBookForDetails by remember { mutableStateOf<Book?>(null) }
    var bookPendingDelete by remember { mutableStateOf<Book?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

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

    // Multi-file HTML & ZIP Document Picker
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
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("ابحث بالعنوان، المؤلف، أو الوصف...") },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    isSearchActive = false
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء البحث")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("library_search_input")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LibraryBooks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مكتبتي",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_icon_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "بحث في مكتبتي")
                        }
                    }

                    // Day / Night Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "تبديل الوضع الليلي والنهاري"
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleViewMode() },
                        modifier = Modifier.testTag("toggle_view_mode_button")
                    ) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "تبديل نمط الرفوف"
                        )
                    }

                    IconButton(
                        onClick = { showAboutDialog = true },
                        modifier = Modifier.testTag("about_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "عن منصة مكتبتي"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    multiFilePickerLauncher.launch(
                        arrayOf(
                            "text/html",
                            "application/xhtml+xml",
                            "application/zip",
                            "application/x-zip-compressed",
                            "*/*"
                        )
                    )
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة كتاب HTML", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("import_book_fab")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Micro 32.dp single-line reading statistics bar that auto-collapses on scroll or when library is empty
            val readingBooksCount = allRawBooks.count { it.readingProgress > 0f && it.readingProgress < 0.95f }
            val averageProgress = if (allRawBooks.isEmpty()) 0f else allRawBooks.map { it.readingProgress.coerceIn(0f, 1f) }.average().toFloat()

            AnimatedVisibility(
                visible = allRawBooks.isNotEmpty() && !isScrollingDown,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "رحلتك مع القراءة",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "الكتب: ${allRawBooks.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "تقرأ: $readingBooksCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { averageProgress },
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${(averageProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // 3-Button Dropdown Control Bar (Status Dropdown | Shelves Dropdown | + رف جديد)
            UnifiedShelfAndFilterRow(
                shelves = shelves,
                selectedShelfId = selectedShelfId,
                filterType = filterType,
                onSelectFilter = { viewModel.setFilterType(it) },
                onSelectShelf = { viewModel.selectShelf(it) },
                onAddShelfClick = { showCreateShelfDialog = true },
                onDeleteShelf = { viewModel.deleteShelf(it) }
            )

            // 3D Bookshelf Grid or List View
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
                            text = if (searchQuery.isNotBlank()) "لا توجد كتب تطابق عبارة البحث"
                            else "لا توجد كتب معروضة على هذا الرف",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اضغط على «إضافة كتاب HTML» لاستيراد كتاب واحد أو عدة كتب تفاعلية دفعة واحدة",
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
                            BookGridItem(
                                book = book,
                                shelfName = shelf?.name,
                                onClick = { onOpenBook(book) },
                                onFavoriteToggle = { viewModel.toggleFavorite(book) },
                                onDetailsClick = { selectedBookForDetails = book },
                                onShareClick = { viewModel.shareBook(context, book) },
                                onDeleteClick = { bookPendingDelete = book },
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
                            BookListItem(
                                book = book,
                                shelfName = shelf?.name,
                                onClick = { onOpenBook(book) },
                                onFavoriteToggle = { viewModel.toggleFavorite(book) },
                                onDetailsClick = { selectedBookForDetails = book },
                                onShareClick = { viewModel.shareBook(context, book) },
                                onDeleteClick = { bookPendingDelete = book }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Shelf Dialog
    if (showCreateShelfDialog) {
        CreateShelfDialog(
            onDismiss = { showCreateShelfDialog = false },
            onConfirm = { name, colorHex, iconName ->
                viewModel.createShelf(name, colorHex, iconName)
                showCreateShelfDialog = false
            }
        )
    }

    // Book Detail, Rename & Cover Color Customizer Dialog
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

    // Direct Delete Confirmation Dialog from 3-dots menu
    bookPendingDelete?.let { book ->
        AlertDialog(
            onDismissRequest = { bookPendingDelete = null },
            title = { Text("تأكيد حذف الكتاب من مكتبتي") },
            text = {
                Text("هل أنت متأكد من حذف «${book.title}» من المكتبة؟ سيتم إزالة نسخته المحفوظة وسجل القراءة نهائياً دون المساس بملفاتك الأصلية في الجهاز.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBook(book.id)
                        bookPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_book_button")
                ) {
                    Text("نعم، احذف الكتاب")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookPendingDelete = null }) {
                    Text("تراجع")
                }
            }
        )
    }

    // Import Progress & Error Handling Dialog
    when (val state = importState) {
        is ImportState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text("استيراد إلى مكتبتي") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
        is ImportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.clearImportState() },
                title = { Text("تنبيه استيراد الملف") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearImportState() }) {
                        Text("حسناً")
                    }
                }
            )
        }
        is ImportState.Success -> {
            if (state.count > 1) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearImportState() },
                    title = { Text("تم الاستيراد بنجاح") },
                    text = { Text("تم استيراد وتحليل ${state.count} كتب بنجاح وإضافتها إلى رفوف مكتبتي.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearImportState() }) {
                            Text("رائع")
                        }
                    }
                )
            } else {
                viewModel.clearImportState()
            }
        }
        else -> Unit
    }

    // About Dialog with Designer Card & WhatsApp Contact Pill
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "عن منصة مكتبتي",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "«مكتبتي» هي منصة ومكتبة رقمية فاخرة لاستيراد وإدارة وتشغيل الكتب التفاعلية المصممة بصيغة HTML بجمال وسلاسة فائقة.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Luxury Designer & WhatsApp Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFF59E0B).copy(alpha = 0.7f),
                                        Color(0xFFE11D48).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "فكرة وتصميم:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "م/منصور قطينه",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // WhatsApp Emerald Capsule Button
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF10B981),
                                contentColor = Color.White,
                                shadowElevation = 3.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        runCatching {
                                            val whatsappIntent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://wa.me/967774004399")
                                            )
                                            context.startActivity(whatsappIntent)
                                        }
                                    }
                                    .testTag("whatsapp_contact_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "تواصل عبر واتساب",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "774004399",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}
