package com.example.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.importer.HtmlBookParser
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.sandbox.SandboxHtmlInjector
import com.example.ui.reader.MaktabatiJsBridge
import com.example.ui.reader.ObservableWebView
import com.example.ui.reader.ReaderAddBookmarkDialog
import com.example.ui.reader.ReaderBookmarksBottomSheet
import com.example.ui.reader.ReaderExtendedBottomBar
import com.example.ui.reader.ReaderFloatingZoomBadge
import com.example.ui.reader.ReaderThemeMode
import com.example.ui.reader.ReaderThemeSelectorDialog
import com.example.ui.reader.ReaderTocBottomSheet
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

private const val VIRTUAL_BOOK_HOST = "maktabati.app"
private const val VIRTUAL_BOOK_BASE = "https://maktabati.app/books/"

private fun resolveMimeType(fileName: String): String {
    val lower = fileName.lowercase()
    return when {
        lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".xhtml") -> "text/html"
        lower.endsWith(".css") -> "text/css"
        lower.endsWith(".js") || lower.endsWith(".mjs") -> "application/javascript"
        lower.endsWith(".json") -> "application/json"
        lower.endsWith(".svg") -> "image/svg+xml"
        lower.endsWith(".png") -> "image/png"
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
        lower.endsWith(".webp") -> "image/webp"
        lower.endsWith(".gif") -> "image/gif"
        lower.endsWith(".bmp") -> "image/bmp"
        lower.endsWith(".ico") -> "image/x-icon"
        lower.endsWith(".mp3") -> "audio/mpeg"
        lower.endsWith(".wav") -> "audio/wav"
        lower.endsWith(".ogg") || lower.endsWith(".oga") -> "audio/ogg"
        lower.endsWith(".m4a") -> "audio/mp4"
        lower.endsWith(".aac") -> "audio/aac"
        lower.endsWith(".flac") -> "audio/flac"
        lower.endsWith(".mp4") || lower.endsWith(".m4v") -> "video/mp4"
        lower.endsWith(".webm") -> "video/webm"
        lower.endsWith(".woff2") -> "font/woff2"
        lower.endsWith(".woff") -> "font/woff"
        lower.endsWith(".ttf") -> "font/ttf"
        lower.endsWith(".otf") -> "font/otf"
        else -> "application/octet-stream"
    }
}

private fun isTextMimeType(mime: String): Boolean {
    return mime.startsWith("text/") ||
        mime == "application/javascript" ||
        mime == "application/json" ||
        mime == "image/svg+xml"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderScreen(
    book: Book,
    bookmarks: List<Bookmark>,
    onBack: () -> Unit,
    onSaveProgress: (progress: Float, scrollY: Int, currentPage: Int, actualTotalPages: Int) -> Unit,
    onAddBookmark: (title: String, scrollY: Int, progress: Float, excerpt: String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("maktabati_preferences", android.content.Context.MODE_PRIVATE)
    }
    var showExtendedControls by remember(book.id) { mutableStateOf(false) }
    var pillCollapsed by remember(book.id) { mutableStateOf(true) }
    var currentProgress by remember(book.id) { mutableFloatStateOf(book.readingProgress) }
    val scrollYHolder = remember(book.id) { IntArray(1) { book.scrollY } }
    var currentPage by remember(book.id) { mutableIntStateOf(book.currentPage.coerceAtLeast(1)) }
    var livePageCount by remember(book.id, book.pageCount) { mutableIntStateOf(book.pageCount.coerceAtLeast(1)) }
    var textZoomPercent by remember(book.id) {
        mutableIntStateOf(prefs.getInt("textZoomPercent", 100).coerceIn(75, 200))
    }
    var showFloatingZoomBadge by remember(book.id) { mutableStateOf(false) }
    var zoomInteractionTick by remember(book.id) { mutableIntStateOf(0) }

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

    var webViewRef by remember(book.id) { mutableStateOf<ObservableWebView?>(null) }
    val lastAppliedZoom = remember(book.id) { IntArray(1) { -1 } }
    val lastAppliedTheme = remember(book.id) { arrayOf<ReaderThemeMode?>(null) }

    val tocEntries = remember(book.tableOfContentsJson) {
        HtmlBookParser.jsonToToc(book.tableOfContentsJson)
    }

    fun updateFluidZoom(newZoom: Int) {
        val clamped = newZoom.coerceIn(75, 200)
        textZoomPercent = clamped
        prefs.edit().putInt("textZoomPercent", clamped).apply()
        showFloatingZoomBadge = true
        zoomInteractionTick++
    }

    // Auto-hide the floating zoom badge after 1.2 seconds (1200ms)
    LaunchedEffect(zoomInteractionTick) {
        if (zoomInteractionTick > 0) {
            delay(1200)
            showFloatingZoomBadge = false
        }
    }

    // Handle system back button cleanly
    BackHandler {
        onSaveProgress(currentProgress, scrollYHolder[0], currentPage, livePageCount)
        onBack()
    }

    // Memory Cleanup, Audio/Media Stop & Progress Persistence when Reader closes
    DisposableEffect(book.id) {
        onDispose {
            onSaveProgress(currentProgress, scrollYHolder[0], currentPage, livePageCount)
            webViewRef?.let { wv ->
                try {
                    wv.stopLoading()
                    wv.onPause()
                    wv.pauseTimers()
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

    fun applyFluidZoomToWebView(wv: WebView, zoomPct: Int) {
        wv.settings.textZoom = zoomPct
        wv.evaluateJavascript(
            "if (typeof window.__maktabatiSetFluidZoom === 'function') window.__maktabatiSetFluidZoom($zoomPct, true);",
            null
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Full-Screen Hardware-Accelerated HTML Book Engine
        key(book.id) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("sandboxed_reader_webview"),
                factory = { ctx ->
                    val booksRoot = File(ctx.filesDir, "books")
                    val bookDir = File(booksRoot, book.folderPath)
                    val entryFile = File(bookDir, book.entryHtmlPath)

                    ObservableWebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        onResume()
                        resumeTimers()

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            allowFileAccess = true
                            allowContentAccess = true
                            @Suppress("DEPRECATION")
                            allowFileAccessFromFileURLs = true
                            @Suppress("DEPRECATION")
                            allowUniversalAccessFromFileURLs = true
                            mediaPlaybackRequiresUserGesture = false
                            builtInZoomControls = false
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            defaultTextEncodingName = "UTF-8"
                            textZoom = textZoomPercent
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        lastAppliedZoom[0] = textZoomPercent
                        lastAppliedTheme[0] = readerTheme

                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = false

                        // Automatically fold the edge handle / extended controls when user touches the book
                        setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                if (!pillCollapsed || showExtendedControls) {
                                    pillCollapsed = true
                                    showExtendedControls = false
                                }
                            }
                            false
                        }

                        addJavascriptInterface(
                            MaktabatiJsBridge(
                                totalBookPages = livePageCount,
                                onPageProgressChanged = { page, total, prog ->
                                    currentPage = page
                                    val resolvedTotal = if (total > 0) total else livePageCount
                                    if (resolvedTotal != livePageCount) {
                                        livePageCount = resolvedTotal
                                    }
                                    val finalProg = if (page >= resolvedTotal) 1.0f else prog
                                    currentProgress = finalProg
                                    onSaveProgress(finalProg, scrollYHolder[0], page, resolvedTotal)
                                },
                                onZoomChangedFromJs = { jsZoomPct ->
                                    val clamped = jsZoomPct.coerceIn(75, 200)
                                    lastAppliedZoom[0] = clamped
                                    textZoomPercent = clamped
                                    prefs.edit().putInt("textZoomPercent", clamped).apply()
                                    settings.textZoom = clamped
                                    showFloatingZoomBadge = true
                                    zoomInteractionTick++
                                }
                            ),
                            "MaktabatiBridge"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val reqUri = request?.url ?: return null
                                if (reqUri.host == VIRTUAL_BOOK_HOST && reqUri.path?.startsWith("/fonts/") == true) {
                                    val fontResId = when (reqUri.path) {
                                        "/fonts/amiri.ttf" -> com.example.R.font.amiri
                                        "/fonts/cairo.ttf" -> com.example.R.font.cairo
                                        else -> null
                                    }
                                    if (fontResId != null) {
                                        try {
                                            return WebResourceResponse(
                                                "font/ttf",
                                                null,
                                                200,
                                                "OK",
                                                mapOf(
                                                    "Access-Control-Allow-Origin" to "*",
                                                    "Cache-Control" to "public, max-age=31536000"
                                                ),
                                                ctx.resources.openRawResource(fontResId)
                                            )
                                        } catch (_: Exception) {}
                                    }
                                }
                                if (reqUri.host == VIRTUAL_BOOK_HOST && reqUri.path?.startsWith("/books/") == true) {
                                    val rawRelPath = reqUri.path!!.removePrefix("/books/")
                                    val decodedRelPath = Uri.decode(rawRelPath)
                                    val targetFile = File(booksRoot, decodedRelPath).let { f ->
                                        if (f.exists()) f else File(booksRoot, rawRelPath)
                                    }
                                    try {
                                        if (targetFile.isFile && targetFile.canonicalPath.startsWith(booksRoot.canonicalPath)) {
                                            val mime = resolveMimeType(targetFile.name)
                                            // Upgrade legacy book files on background I/O thread if needed
                                            if (mime == "text/html") {
                                                val raw = targetFile.readText(Charsets.UTF_8)
                                                if (!raw.contains("__maktabatiV8Initialized")) {
                                                    val upgraded = SandboxHtmlInjector.injectIntoHtml(raw, book.id, livePageCount)
                                                    targetFile.writeText(upgraded, Charsets.UTF_8)
                                                    return WebResourceResponse(
                                                        "text/html",
                                                        "UTF-8",
                                                        200,
                                                        "OK",
                                                        mapOf(
                                                            "Access-Control-Allow-Origin" to "*",
                                                            "Cache-Control" to "no-cache"
                                                        ),
                                                        upgraded.byteInputStream(Charsets.UTF_8)
                                                    )
                                                }
                                            }
                                            return WebResourceResponse(
                                                mime,
                                                if (isTextMimeType(mime)) "UTF-8" else null,
                                                200,
                                                "OK",
                                                mapOf(
                                                    "Access-Control-Allow-Origin" to "*",
                                                    "Cache-Control" to "no-cache"
                                                ),
                                                BufferedInputStream(FileInputStream(targetFile), 65536)
                                            )
                                        }
                                    } catch (_: Exception) {}
                                }
                                return super.shouldInterceptRequest(view, request)
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.let { wv ->
                                    if (book.scrollY > 0) {
                                        wv.postDelayed({
                                            wv.scrollTo(0, book.scrollY)
                                        }, 30)
                                    }
                                    if (readerTheme != ReaderThemeMode.ORIGINAL) {
                                        applyThemeToWebView(wv, readerTheme)
                                    }
                                    if (textZoomPercent != 100) {
                                        applyFluidZoomToWebView(wv, textZoomPercent)
                                    }
                                    val savedPage = book.currentPage.coerceAtLeast(1)
                                    if (savedPage > 1) {
                                        wv.evaluateJavascript(
                                            "if (typeof window.__maktabatiGoToDomPage === 'function') window.__maktabatiGoToDomPage(${savedPage - 1}); else if (typeof goToPage === 'function') goToPage(${savedPage - 1});",
                                            null
                                        )
                                    } else if (book.scrollY == 0 && readerTheme == ReaderThemeMode.ORIGINAL) {
                                        // Capture real cover snapshot from the first page of the live WebView
                                        wv.postDelayed({
                                            com.example.data.importer.HtmlCoverSnapshotGenerator.captureFromLiveWebView(
                                                webView = wv,
                                                bookDir = bookDir
                                            )
                                        }, 140L)
                                    }
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true && request.url?.host == VIRTUAL_BOOK_HOST && entryFile.isFile) {
                                    view?.loadUrl(Uri.fromFile(entryFile).toString())
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }

                        webChromeClient = WebChromeClient()

                        onScrollListener = { scrollY, maxScroll ->
                            scrollYHolder[0] = scrollY
                            if (maxScroll > 20 && livePageCount <= 1) {
                                val prog = (scrollY.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
                                currentProgress = prog
                            }
                        }

                        if (entryFile.exists()) {
                            // Load over virtual HTTPS origin so Chromium streams off the UI thread with full localStorage & CSS support
                            val virtualUrl = "$VIRTUAL_BOOK_BASE${book.folderPath}/${book.entryHtmlPath}"
                            loadUrl(virtualUrl)
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
                    if (lastAppliedZoom[0] != textZoomPercent) {
                        lastAppliedZoom[0] = textZoomPercent
                        applyFluidZoomToWebView(wv, textZoomPercent)
                    }
                    if (lastAppliedTheme[0] != readerTheme) {
                        lastAppliedTheme[0] = readerTheme
                        applyThemeToWebView(wv, readerTheme)
                    }
                }
            )
        }

        // Single Sleek Floating Fluid Reflow Zoom Capsule at Top Center
        ReaderFloatingZoomBadge(
            visible = showFloatingZoomBadge,
            zoomPercent = textZoomPercent,
            onResetToOptimal = { updateFluidZoom(100) },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Smart Auto-Hiding Edge Handle (collapsed by default on the middle side edge, non-intrusive 18.dp x 56.dp)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = if (pillCollapsed) 0.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!pillCollapsed) {
                Surface(
                    onClick = {
                        onSaveProgress(currentProgress, scrollYHolder[0], currentPage, livePageCount)
                        onBack()
                    },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .shadow(6.dp, RoundedCornerShape(50))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .testTag("reader_back_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "العودة إلى مكتبتي",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("مكتبتي", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    onClick = { showExtendedControls = !showExtendedControls },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(6.dp, CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                        .testTag("reader_tools_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "فتح أو إغلاق أدوات القراءة",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Surface(
                    onClick = {
                        pillCollapsed = true
                        showExtendedControls = false
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("reader_pill_collapse_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "طي الكبسولة",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                Surface(
                    onClick = { pillCollapsed = false },
                    shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(width = 18.dp, height = 56.dp)
                        .border(
                            0.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            RoundedCornerShape(10.dp)
                        )
                        .testTag("reader_pill_expand_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                        )
                    }
                }
            }
        }

        // Expandable Floating Reader Control Bar (Bottom) when user taps the Tune icon
        ReaderExtendedBottomBar(
            visible = showExtendedControls,
            book = book,
            currentPage = currentPage,
            livePageCount = livePageCount,
            currentProgress = currentProgress,
            textZoomPercent = textZoomPercent,
            onZoomChange = { updateFluidZoom(it) },
            onShowToc = { showTocSheet = true },
            onShowBookmarks = { showBookmarksSheet = true },
            onShowTheme = { showThemeDialog = true },
            onShowAddBookmark = { showAddBookmarkDialog = true },
            onProgressSliderChange = { newProg ->
                currentProgress = newProg
                val targetPage = ((newProg * livePageCount).toInt()).coerceIn(1, livePageCount.coerceAtLeast(1))
                val delta = targetPage - currentPage
                currentPage = targetPage
                webViewRef?.let { wv ->
                    if (livePageCount > 1) {
                        wv.evaluateJavascript(
                            "if (typeof window.__maktabatiGoToDomPage === 'function') window.__maktabatiGoToDomPage(${targetPage - 1}); else if (typeof goToPage === 'function') goToPage(${targetPage - 1}); else if (typeof window.__maktabatiTurn === 'function') window.__maktabatiTurn($delta);",
                            null
                        )
                    } else {
                        val total = wv.getMaxScroll()
                        if (total > 0) {
                            val targetY = (newProg * total).toInt()
                            wv.scrollTo(0, targetY)
                            scrollYHolder[0] = targetY
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Theme Selector Dialog
    if (showThemeDialog) {
        ReaderThemeSelectorDialog(
            currentTheme = readerTheme,
            onSelectTheme = { mode ->
                readerTheme = mode
                prefs.edit().putString("readerTheme", mode.name).apply()
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Add Bookmark Dialog
    if (showAddBookmarkDialog) {
        ReaderAddBookmarkDialog(
            defaultTitle = "صفحة $currentPage (${(currentProgress * 100).toInt()}%)",
            onConfirm = { title, note ->
                onAddBookmark(title, scrollYHolder[0], currentProgress, note)
                showAddBookmarkDialog = false
            },
            onDismiss = { showAddBookmarkDialog = false }
        )
    }

    // Table of Contents Sheet
    if (showTocSheet) {
        ReaderTocBottomSheet(
            tocEntries = tocEntries,
            onSelectEntry = { entry ->
                showTocSheet = false
                webViewRef?.let { wv ->
                    if (entry.href.startsWith("#page-")) {
                        val pageNum = entry.href.removePrefix("#page-").toIntOrNull() ?: 1
                        currentPage = pageNum
                        currentProgress = if (pageNum >= livePageCount.coerceAtLeast(1)) 1.0f else (pageNum.toFloat() / livePageCount.coerceAtLeast(1)).coerceIn(0f, 1f)
                        wv.evaluateJavascript(
                            "if (typeof window.__maktabatiGoToDomPage === 'function') window.__maktabatiGoToDomPage(${pageNum - 1}); else if (typeof goToPage === 'function') goToPage(${pageNum - 1}); else window.location.hash = '${entry.href}';",
                            null
                        )
                    } else if (entry.href.isNotBlank()) {
                        wv.evaluateJavascript("window.location.hash = '${entry.href}';", null)
                    }
                }
            },
            onDismiss = { showTocSheet = false }
        )
    }

    // Bookmarks Sheet
    if (showBookmarksSheet) {
        ReaderBookmarksBottomSheet(
            bookmarks = bookmarks,
            onSelectBookmark = { bm ->
                showBookmarksSheet = false
                webViewRef?.scrollTo(0, bm.scrollY)
                scrollYHolder[0] = bm.scrollY
                currentProgress = bm.progress
            },
            onDeleteBookmark = onDeleteBookmark,
            onAddBookmarkClick = { showAddBookmarkDialog = true },
            onDismiss = { showBookmarksSheet = false }
        )
    }
}
