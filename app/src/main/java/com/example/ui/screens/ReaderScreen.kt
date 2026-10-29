package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.importer.HtmlBookParser
import com.example.data.sandbox.SandboxHtmlInjector
import com.example.ui.reader.ObservableWebView
import com.example.ui.reader.MaktabatiJsBridge
import com.example.ui.reader.ReaderThemeMode
import com.example.data.model.Book
import com.example.data.model.Bookmark
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderScreen(
    book: Book,
    bookmarks: List<Bookmark>,
    onBack: () -> Unit,
    onSaveProgress: (progress: Float, scrollY: Int, currentPage: Int) -> Unit,
    onAddBookmark: (title: String, scrollY: Int, progress: Float, excerpt: String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("maktabati_preferences", android.content.Context.MODE_PRIVATE)
    }
    var showExtendedControls by remember { mutableStateOf(false) }
    var pillCollapsed by remember { mutableStateOf(false) }
    var pillOffset by remember { mutableStateOf(Offset(0f, 110f)) }
    var currentProgress by remember { mutableFloatStateOf(book.readingProgress) }
    var currentScrollY by remember { mutableIntStateOf(book.scrollY) }
    var currentPage by remember { mutableIntStateOf(book.currentPage.coerceAtLeast(1)) }
    var textZoomPercent by remember(book.id) { mutableIntStateOf(prefs.getInt("textZoomPercent", 100)) }
    var readerTheme by remember(book.id) {
        mutableStateOf(
            runCatching { ReaderThemeMode.valueOf(prefs.getString("readerTheme", "ORIGINAL")!!) }
                .getOrDefault(ReaderThemeMode.ORIGINAL)
        )
    }

    var showTocSheet by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var webViewRef by remember { mutableStateOf<ObservableWebView?>(null) }
    val tocEntries = remember(book.tableOfContentsJson) {
        HtmlBookParser.jsonToToc(book.tableOfContentsJson)
    }

    // Handle system back button cleanly
    BackHandler {
        onSaveProgress(currentProgress, currentScrollY, currentPage)
        onBack()
    }

    // Memory Cleanup & Progress Persistence when Reader closes
    DisposableEffect(book.id) {
        onDispose {
            onSaveProgress(currentProgress, currentScrollY, currentPage)
            webViewRef?.let { wv ->
                try {
                    wv.stopLoading()
                    wv.loadUrl("about:blank")
                    wv.clearHistory()
                    wv.clearCache(false)
                    wv.removeAllViews()
                    wv.destroy()
                } catch (_: Exception) {}
            }
            webViewRef = null
        }
    }

    fun applyThemeToWebView(wv: WebView, theme: ReaderThemeMode) {
        if (theme == ReaderThemeMode.ORIGINAL) {
            val js = """
                (function() {
                    const el = document.getElementById('maktabati-theme-override');
                    if (el) el.remove();
                })();
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        } else {
            val js = """
                (function() {
                    let el = document.getElementById('maktabati-theme-override');
                    if (!el) {
                        el = document.createElement('style');
                        el.id = 'maktabati-theme-override';
                        document.head.appendChild(el);
                    }
                    el.innerHTML = `
                        html, body, .page {
                            background: ${theme.bgHex} !important;
                            background-color: ${theme.bgHex} !important;
                            color: ${theme.textHex} !important;
                        }
                        p, span, h1, h2, h3, h4, h5, h6, li, td, th, div:not([class*="code"]) {
                            color: ${theme.textHex} !important;
                        }
                    `;
                })();
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Full-Screen Sandboxed HTML Book Engine
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("sandboxed_reader_webview"),
            factory = { ctx ->
                ObservableWebView(ctx).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = false
                        builtInZoomControls = true
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        defaultTextEncodingName = "UTF-8"
                        textZoom = textZoomPercent
                    }
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = false

                    addJavascriptInterface(
                        MaktabatiJsBridge(book.pageCount) { page, prog ->
                            currentPage = page
                            currentProgress = prog
                            onSaveProgress(prog, currentScrollY, page)
                        },
                        "MaktabatiBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            // Ensure storage isolation is active even on multi-file navigations
                            view?.evaluateJavascript(
                                SandboxHtmlInjector.buildInjectedSnippet(book.id, book.pageCount)
                                    .substringAfter("<script id=\"maktabati-storage-isolation\">")
                                    .substringBefore("</script>"),
                                null
                            )
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.let { wv ->
                                if (book.scrollY > 0) {
                                    wv.postDelayed({
                                        wv.scrollTo(0, book.scrollY)
                                    }, 80)
                                }
                                applyThemeToWebView(wv, readerTheme)
                                val savedPage = book.currentPage.coerceAtLeast(1)
                                if (savedPage > 1) wv.evaluateJavascript("if (typeof goToPage === 'function') goToPage(${savedPage - 1});", null)
                            }
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }
                    }

                    webChromeClient = WebChromeClient()

                    onScrollListener = { scrollY, maxScroll ->
                        currentScrollY = scrollY
                        if (maxScroll > 20 && book.pageCount <= 1) {
                            val prog = (scrollY.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
                            currentProgress = prog
                        }
                    }

                    val booksRoot = File(ctx.filesDir, "books")
                    val bookDir = File(booksRoot, book.folderPath)
                    val entryFile = File(bookDir, book.entryHtmlPath)
                    if (entryFile.exists()) {
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) settings.offscreenPreRaster = true
                        loadUrl(android.net.Uri.fromFile(entryFile).toString())
                    } else {
                        loadDataWithBaseURL(
                            null,
                            "<h3 style='text-align:center;padding:40px;'>تعذر العثور على ملف الكتاب</h3>",
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }

                    webViewRef = this
                }
            },
            update = { wv ->
                wv.settings.textZoom = textZoomPercent
                applyThemeToWebView(wv, readerTheme)
            }
        )

        // Draggable floating pill: back, tools toggle, and collapse handle.
        Row(
            modifier = Modifier
                .align(if (pillCollapsed) Alignment.CenterStart else Alignment.TopStart)
                .offset { IntOffset(pillOffset.x.roundToInt(), pillOffset.y.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        pillOffset = Offset(
                            (pillOffset.x + drag.x).coerceAtLeast(0f),
                            (pillOffset.y + drag.y).coerceAtLeast(0f)
                        )
                    }
                }
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!pillCollapsed) {
                Surface(
                    onClick = {
                        onSaveProgress(currentProgress, currentScrollY, currentPage)
                        onBack()
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.shadow(5.dp, RoundedCornerShape(50))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .testTag("reader_back_button")
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "العودة إلى مكتبتي", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("مكتبتي", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    onClick = { showExtendedControls = !showExtendedControls },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp).shadow(5.dp, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                        .testTag("reader_tools_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, contentDescription = "فتح أو إغلاق أدوات القراءة", modifier = Modifier.size(18.dp))
                    }
                }
                Surface(
                    onClick = { pillCollapsed = !pillCollapsed; if (pillCollapsed) showExtendedControls = false },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp).testTag("reader_pill_collapse_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "طي الكبسولة", modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                Surface(
                    onClick = { pillCollapsed = false },
                    shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(width = 32.dp, height = 54.dp)
                        .testTag("reader_pill_expand_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "توسيع الكبسولة", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Expandable Floating Reader Control Bar (Bottom) when user taps the Tune icon
        AnimatedVisibility(
            visible = showExtendedControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${book.author} • صفحة $currentPage من ${book.pageCount} (${(currentProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { showTocSheet = true },
                                modifier = Modifier.testTag("reader_toc_button")
                            ) {
                                Icon(imageVector = Icons.Default.List, contentDescription = "فهرس الكتاب")
                            }
                            IconButton(
                                onClick = { showBookmarksSheet = true },
                                modifier = Modifier.testTag("reader_bookmarks_button")
                            ) {
                                Icon(imageVector = Icons.Default.Bookmark, contentDescription = "العلامات المرجعية")
                            }
                            IconButton(
                                onClick = { showThemeDialog = true },
                                modifier = Modifier.testTag("reader_theme_button")
                            ) {
                                Icon(imageVector = Icons.Default.ColorLens, contentDescription = "سمة القراءة")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (textZoomPercent > 70) { textZoomPercent -= 10; prefs.edit().putInt("textZoomPercent", textZoomPercent).apply() } }) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "تصغير الخط")
                            }
                            Text(
                                text = "$textZoomPercent%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { if (textZoomPercent < 220) { textZoomPercent += 10; prefs.edit().putInt("textZoomPercent", textZoomPercent).apply() } }) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "تكبير الخط")
                            }
                        }

                        Button(
                            onClick = { showAddBookmarkDialog = true },
                            modifier = Modifier.testTag("add_bookmark_button")
                        ) {
                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ علامة", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(currentProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = currentProgress,
                            onValueChange = { newProg ->
                                currentProgress = newProg
                                val targetPage = ((newProg * book.pageCount).toInt()).coerceIn(1, book.pageCount.coerceAtLeast(1))
                                currentPage = targetPage
                                webViewRef?.let { wv ->
                                    if (book.pageCount > 1) {
                                        wv.evaluateJavascript("if (typeof goToPage === 'function') goToPage(${targetPage - 1});", null)
                                    } else {
                                        val total = wv.getMaxScroll()
                                        if (total > 0) {
                                            val targetY = (newProg * total).toInt()
                                            wv.scrollTo(0, targetY)
                                            currentScrollY = targetY
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("progress_slider")
                        )
                    }
                }
            }
        }
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("مظهر وسمة القراءة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (mode in ReaderThemeMode.values()) {
                        val isSelected = readerTheme == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    readerTheme = mode
                                    prefs.edit().putString("readerTheme", mode.name).apply()
                                    showThemeDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // Add Bookmark Dialog
    if (showAddBookmarkDialog) {
        var bmTitle by remember { mutableStateOf("صفحة $currentPage (${(currentProgress * 100).toInt()}%)") }
        var bmNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            title = { Text("إضافة علامة مرجعية جديدة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = bmTitle,
                        onValueChange = { bmTitle = it },
                        label = { Text("عنوان العلامة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bmNote,
                        onValueChange = { bmNote = it },
                        label = { Text("ملاحظة أو اقتباس (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bmTitle.isNotBlank()) {
                            onAddBookmark(bmTitle.trim(), currentScrollY, currentProgress, bmNote.trim())
                            showAddBookmarkDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Table of Contents Sheet
    if (showTocSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "فهرس صفحات وفصول الكتاب (${tocEntries.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (tocEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد فصول مفهرسة في هذا الكتاب",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                        items(tocEntries) { entry ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTocSheet = false
                                        webViewRef?.let { wv ->
                                            if (entry.href.startsWith("#page-")) {
                                                val pageNum = entry.href.removePrefix("#page-").toIntOrNull() ?: 1
                                                currentPage = pageNum
                                                currentProgress = (pageNum.toFloat() / book.pageCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                                                wv.evaluateJavascript("if (typeof goToPage === 'function') goToPage(${pageNum - 1}); else window.location.hash = '${entry.href}';", null)
                                            } else if (entry.href.isNotBlank()) {
                                                wv.evaluateJavascript("window.location.hash = '${entry.href}';", null)
                                            }
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Bookmarks Sheet
    if (showBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookmarksSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "العلامات المرجعية المحفوظة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showAddBookmarkDialog = true }) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "إضافة")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم تقم بحفظ أي علامات مرجعية في هذا الكتاب بعد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                        items(bookmarks) { bm ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        showBookmarksSheet = false
                                        webViewRef?.scrollTo(0, bm.scrollY)
                                        currentScrollY = bm.scrollY
                                        currentProgress = bm.progress
                                    },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = bm.title, style = MaterialTheme.typography.titleMedium)
                                        if (bm.excerpt.isNotBlank()) {
                                            Text(
                                                text = bm.excerpt,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "الموضع: ${(bm.progress * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDeleteBookmark(bm) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف العلامة",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
