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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.backup.CloudSyncState
import com.example.data.importer.MultiBookArchiveProposal
import com.example.data.model.Book
import com.example.ui.components.BookDetailDialog
import com.example.ui.components.BookGridItem
import com.example.ui.components.BookListItem
import com.example.ui.components.CreateShelfDialog
import com.example.ui.components.TypographySettingsDialog
import com.example.ui.components.UnifiedShelfAndFilterRow
import com.example.ui.viewmodel.FilterType
import com.example.ui.viewmodel.ImportState
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.ViewMode
import kotlinx.coroutines.delay

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
    val pendingMultiBookProposal by viewModel.pendingMultiBookProposal.collectAsStateWithLifecycle()
    val userTypographyPreferences by viewModel.userTypographyPreferences.collectAsStateWithLifecycle()
    val connectedGoogleEmail by viewModel.connectedGoogleEmail.collectAsStateWithLifecycle()
    val lastBackupTimeText by viewModel.lastBackupTimeText.collectAsStateWithLifecycle()
    val cloudSyncState by viewModel.cloudSyncState.collectAsStateWithLifecycle()
    val cardScaleFactor = (gridCardWidthDp / 135f).coerceIn(0.68f, 1.25f)

    LaunchedEffect(allRawBooks.size) {
        if (allRawBooks.isNotEmpty()) {
            viewModel.ensureLibraryCoversAndPalettes()
        }
    }

    LaunchedEffect(cloudSyncState) {
        if (cloudSyncState is CloudSyncState.Success) {
            delay(4000)
            viewModel.dismissCloudSyncState()
        }
    }

    var isSearchActive by remember { mutableStateOf(false) }
    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var selectedBookForDetails by remember { mutableStateOf<Book?>(null) }
    var bookPendingDelete by remember { mutableStateOf<Book?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTypographySettingsDialog by remember { mutableStateOf(false) }
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

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

                    // Gear Icon: Typography Preferences Studio
                    IconButton(
                        onClick = { showTypographySettingsDialog = true },
                        modifier = Modifier.testTag("typography_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "إعدادات الخطوط الطباعية",
                            tint = if (userTypographyPreferences.useCustomTypography) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    // Three-dots Options Menu (Google Cloud Backup & Restore + About)
                    Box {
                        IconButton(
                            onClick = { showMoreOptionsMenu = true },
                            modifier = Modifier.testTag("about_icon_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "خيارات السحابة ومعلومات المنصة"
                                )
                                if (connectedGoogleEmail != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMoreOptionsMenu,
                            onDismissRequest = { showMoreOptionsMenu = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            shadowElevation = 14.dp,
                            modifier = Modifier
                                .width(290.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .testTag("more_options_dropdown_menu")
                        ) {
                            // Section 1: Google Cloud Backup & Restore
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (connectedGoogleEmail != null) {
                                                Icons.Default.CloudDone
                                            } else {
                                                Icons.Default.Cloud
                                            },
                                            contentDescription = null,
                                            tint = if (connectedGoogleEmail != null) {
                                                Color(0xFF10B981)
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "السحابة والنسخ الاحتياطي",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (connectedGoogleEmail != null) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFF10B981).copy(alpha = 0.14f),
                                            border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                                            modifier = Modifier.testTag("cloud_connected_badge")
                                        ) {
                                            Text(
                                                text = "متصل بالسحابة ☁️",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF059669),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (connectedGoogleEmail == null) {
                                    // Before Sign-In: Only show Google Sign-In button (Backup/Restore hidden!)
                                    Text(
                                        text = "اربط حساب Google لحفظ كتبك ورفوفك وتقدم قراءتك تلقائياً في سحابتك الخاصة.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )

                                    Button(
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            val signInIntent = viewModel.prepareGoogleSignInIntent()
                                            if (signInIntent != null) {
                                                googleSignInLauncher.launch(signInIntent)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("google_sign_in_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cloud,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تسجيل الدخول بحساب Google",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    // After Sign-In: Connected Email + Last Backup Time + Backup Now + Restore + Sign out
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = connectedGoogleEmail.orEmpty(),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (!lastBackupTimeText.isNullOrBlank()) {
                                                    "آخر نسخة احتياطية: $lastBackupTimeText"
                                                } else {
                                                    "النسخ التلقائي مفعل عند إضافة أي كتاب"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    // 1. Save Backup Now Button
                                    Button(
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            viewModel.backupLibraryToCloudNow()
                                        },
                                        shape = RoundedCornerShape(11.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("cloud_backup_now_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "حفظ نسخة احتياطية الآن",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // 2. Restore Backup Button
                                    OutlinedButton(
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            viewModel.restoreLibraryFromCloudNow()
                                        },
                                        shape = RoundedCornerShape(11.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("cloud_restore_backup_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "استرداد النسخة الاحتياطية",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // 3. Calm Sign-Out / Switch Account Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                showMoreOptionsMenu = false
                                                viewModel.signOutGoogleCloud()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .testTag("cloud_sign_out_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "تسجيل الخروج أو تبديل الحساب",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )

                            // Section 2: About Maktabati Platform
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "عن منصة مكتبتي",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMoreOptionsMenu = false
                                    showAboutDialog = true
                                },
                                modifier = Modifier.testTag("about_menu_item")
                            )
                        }
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
            // Elegant Cloud Sync Progress / Notification Banner
            AnimatedVisibility(
                visible = cloudSyncState !is CloudSyncState.Idle,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val bannerState = cloudSyncState
                if (bannerState !is CloudSyncState.Idle) {
                    val containerColor = when (bannerState) {
                        is CloudSyncState.Syncing -> MaterialTheme.colorScheme.secondaryContainer
                        is CloudSyncState.Success -> Color(0xFFECFDF5)
                        is CloudSyncState.Error -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val contentColor = when (bannerState) {
                        is CloudSyncState.Syncing -> MaterialTheme.colorScheme.onSecondaryContainer
                        is CloudSyncState.Success -> Color(0xFF065F46)
                        is CloudSyncState.Error -> MaterialTheme.colorScheme.onErrorContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val messageText = when (bannerState) {
                        is CloudSyncState.Syncing -> bannerState.message
                        is CloudSyncState.Success -> bannerState.message
                        is CloudSyncState.Error -> bannerState.message
                        else -> ""
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(
                            0.8.dp,
                            contentColor.copy(alpha = 0.28f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("cloud_sync_status_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                when (bannerState) {
                                    is CloudSyncState.Syncing -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = contentColor
                                        )
                                    }
                                    is CloudSyncState.Success -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    is CloudSyncState.Error -> {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    else -> Unit
                                }
                                Text(
                                    text = messageText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor
                                )
                            }

                            if (bannerState !is CloudSyncState.Syncing) {
                                IconButton(
                                    onClick = { viewModel.dismissCloudSyncState() },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "إغلاق التنبيه",
                                        tint = contentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

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

    // Multi-Book ZIP Choice Dialog
    pendingMultiBookProposal?.let { proposal ->
        MultiBookZipDialog(
            proposal = proposal,
            onAddInNewShelf = { viewModel.confirmMultiBookImportWithNewShelf(proposal) },
            onAddStandalone = { viewModel.confirmMultiBookImportStandalone(proposal) },
            onCancel = { viewModel.cancelMultiBookImport(proposal) }
        )
    }

    // Typography Preferences Studio Dialog (Gear Icon)
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

@Composable
fun MultiBookZipDialog(
    proposal: MultiBookArchiveProposal,
    onAddInNewShelf: () -> Unit,
    onAddStandalone: () -> Unit,
    onCancel: () -> Unit
) {
    val archiveBooksCount = proposal.archiveBooksCount
    val zipBaseName = proposal.zipBaseName

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier.testTag("multi_book_zip_dialog"),
        title = {
            Text(
                text = "أرشيف يضم عدة كتب ($archiveBooksCount كتب)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "تم اكتشاف $archiveBooksCount كتب داخل الملف المضغوط «$zipBaseName». كيف تود إضافتها إلى مكتبتي؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onAddInNewShelf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multi_book_new_shelf_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "إضافتها في رف جديد باسم «$zipBaseName»",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = onAddStandalone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multi_book_standalone_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "إضافتها ككتب مستقلة (بدون رف جديد)",
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("multi_book_cancel_button")
            ) {
                Text("إلغاء")
            }
        }
    )
}

