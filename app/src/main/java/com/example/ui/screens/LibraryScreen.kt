package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Book
import com.example.ui.components.BookDetailDialog
import com.example.ui.components.BookGridItem
import com.example.ui.components.BookListItem
import com.example.ui.components.CreateShelfDialog
import com.example.ui.components.ShelfSelector
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
    val books by viewModel.filteredBooks.collectAsStateWithLifecycle()
    val allRawBooks by viewModel.rawBooks.collectAsStateWithLifecycle()
    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val selectedShelfId by viewModel.selectedShelfId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var selectedBookForDetails by remember { mutableStateOf<Book?>(null) }
    var showTopMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مكتبتي",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${allRawBooks.size} كتب",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
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

                    Box {
                        IconButton(onClick = { showTopMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "المزيد")
                        }
                        DropdownMenu(
                            expanded = showTopMenu,
                            onDismissRequest = { showTopMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("استعادة الكتب التفاعلية النموذجية") },
                                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                onClick = {
                                    showTopMenu = false
                                    viewModel.reloadSamples()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("عن منصة مكتبتي") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    showTopMenu = false
                                    showAboutDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
            // Compact reading statistics card, aligned with the youthful visual identity.
            val readingBooksCount = allRawBooks.count { it.readingProgress > 0f && it.readingProgress < 0.95f }
            val averageProgress = if (allRawBooks.isEmpty()) 0f else allRawBooks.map { it.readingProgress.coerceIn(0f, 1f) }.average().toFloat()
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("رحلتك مع القراءة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("إجمالي الكتب", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${allRawBooks.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("قيد القراءة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("$readingBooksCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("متوسط الإنجاز", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${(averageProgress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { averageProgress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.secondary, trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                }
            }

            // Shelves Selector Row
            ShelfSelector(
                shelves = shelves,
                selectedShelfId = selectedShelfId,
                onSelectShelf = { viewModel.selectShelf(it) },
                onAddShelfClick = { showCreateShelfDialog = true }
            )

            // Secondary Status Filter Tabs (الكل، قيد القراءة، المكتملة، المفضلة)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == FilterType.ALL,
                    onClick = { viewModel.setFilterType(FilterType.ALL) },
                    label = { Text("الكل", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("filter_all")
                )
                FilterChip(
                    selected = filterType == FilterType.READING,
                    onClick = { viewModel.setFilterType(FilterType.READING) },
                    label = { Text("قيد القراءة", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("filter_reading")
                )
                FilterChip(
                    selected = filterType == FilterType.COMPLETED,
                    onClick = { viewModel.setFilterType(FilterType.COMPLETED) },
                    label = { Text("المكتملة", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("filter_completed")
                )
                FilterChip(
                    selected = filterType == FilterType.FAVORITES,
                    onClick = { viewModel.setFilterType(FilterType.FAVORITES) },
                    label = { Text("المفضلة ★", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.testTag("filter_favorites")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                if (viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 156.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("books_grid")
                    ) {
                        items(books, key = { it.id }) { book ->
                            val shelf = shelves.find { it.id == book.shelfId }
                            BookGridItem(
                                book = book,
                                shelfName = shelf?.name,
                                onClick = { onOpenBook(book) },
                                onFavoriteToggle = { viewModel.toggleFavorite(book) },
                                onDetailsClick = { selectedBookForDetails = book }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
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
                                onDetailsClick = { selectedBookForDetails = book }
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

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("عن منصة مكتبتي", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    Text(
                        text = "«مكتبتي» هي منصة ومكتبة رقمية فاخرة عالية الأداء لاستيراد وإدارة وتشغيل الكتب التفاعلية المصممة بصيغة HTML/CSS/JS المستقلة.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• تخزين غير متزامن للملفات الخام في نظام الملفات الأصلي مع فهرس Room سريع.\n" +
                            "• محلل ذكي يستخرج تلقائياً BOOK_DATA (العنوان، العنوان الفرعي، المؤلف، عدد الصفحات) ومتغيرات :root لتلوين الأغلفة.\n" +
                            "• بيئة تشغيل معزولة (Sandboxed Reader) مع عزل مفاتيح localStorage لكل كتاب على حدة وتسريع رسومي 60fps وتنظيف فوري للذاكرة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
