package com.maktabati.app.ui.screens.reader

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.maktabati.app.data.importer.HtmlBookParser
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Bookmark
import com.maktabati.app.data.typography.UserTypographyPreferences
import com.maktabati.app.ui.components.TypographySettingsDialog
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.reader.ObservableWebView
import com.maktabati.app.ui.reader.ReaderAddBookmarkDialog
import com.maktabati.app.ui.reader.ReaderBookmarksBottomSheet
import com.maktabati.app.ui.reader.ReaderExtendedBottomBar
import com.maktabati.app.ui.reader.ReaderFloatingZoomBadge
import com.maktabati.app.ui.reader.ReaderThemeMode
import com.maktabati.app.ui.reader.ReaderThemeSelectorDialog
import com.maktabati.app.ui.reader.ReaderTocBottomSheet
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    book: Book,
    bookmarks: List<Bookmark>,
    userTypographyPreferences: UserTypographyPreferences = UserTypographyPreferences(),
    onUpdateTypographyPreferences: (UserTypographyPreferences) -> Unit = {},
    onBack: () -> Unit,
    onSaveProgress: (progress: Float, scrollY: Int, currentPage: Int, actualTotalPages: Int) -> Unit,
    onAddBookmark: (title: String, scrollY: Int, progress: Float, excerpt: String) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
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
    var showTypographyDialog by remember { mutableStateOf(false) }

    var webViewRef by remember(book.id) { mutableStateOf<ObservableWebView?>(null) }
    val lastAppliedZoom = remember(book.id) { IntArray(1) { -1 } }
    val lastAppliedTheme = remember(book.id) { arrayOf<ReaderThemeMode?>(null) }
    val lastAppliedTypography = remember(book.id) { arrayOf<UserTypographyPreferences?>(null) }

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

    LaunchedEffect(zoomInteractionTick) {
        if (zoomInteractionTick > 0) {
            delay(1200)
            showFloatingZoomBadge = false
        }
    }

    BackHandler {
        onSaveProgress(currentProgress, scrollYHolder[0], currentPage, livePageCount)
        onBack()
    }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        ReaderWebViewContainer(
            book = book,
            livePageCount = livePageCount,
            textZoomPercent = textZoomPercent,
            readerTheme = readerTheme,
            userTypographyPreferences = userTypographyPreferences,
            lastAppliedZoom = lastAppliedZoom,
            lastAppliedTheme = lastAppliedTheme,
            lastAppliedTypography = lastAppliedTypography,
            onTouchCollapseControls = {
                if (!pillCollapsed || showExtendedControls) {
                    pillCollapsed = true
                    showExtendedControls = false
                }
            },
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
                showFloatingZoomBadge = true
                zoomInteractionTick++
            },
            onScrollChanged = { scrollY, maxScroll ->
                scrollYHolder[0] = scrollY
                if (maxScroll > 20 && livePageCount <= 1) {
                    val prog = (scrollY.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
                    currentProgress = prog
                }
            },
            onWebViewCreated = { webViewRef = it }
        )

        ReaderFloatingZoomBadge(
            visible = showFloatingZoomBadge,
            zoomPercent = textZoomPercent,
            onResetToOptimal = { updateFluidZoom(100) },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        ReaderEdgeControlPill(
            pillCollapsed = pillCollapsed,
            onExpandPill = { pillCollapsed = false },
            onCollapsePill = {
                pillCollapsed = true
                showExtendedControls = false
            },
            onBackClick = {
                onSaveProgress(currentProgress, scrollYHolder[0], currentPage, livePageCount)
                onBack()
            },
            onToggleTools = { showExtendedControls = !showExtendedControls },
            onOpenTypography = { showTypographyDialog = true },
            modifier = Modifier.align(Alignment.CenterStart)
        )

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
            onShowTypographySettings = { showTypographyDialog = true },
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

    if (showTypographyDialog) {
        TypographySettingsDialog(
            currentPreferences = userTypographyPreferences,
            onUpdatePreferences = { updated ->
                onUpdateTypographyPreferences(updated)
                webViewRef?.let { wv ->
                    ReaderWebViewStyleApplicator.applyTypographyPreferencesToWebView(wv, updated)
                }
            },
            onDismiss = { showTypographyDialog = false }
        )
    }

    if (showAddBookmarkDialog) {
        ReaderAddBookmarkDialog(
            defaultTitle = "${strings.readerPageIndicator(currentPage, livePageCount)} (${(currentProgress * 100).toInt()}%)",
            onConfirm = { title, note ->
                onAddBookmark(title, scrollYHolder[0], currentProgress, note)
                showAddBookmarkDialog = false
            },
            onDismiss = { showAddBookmarkDialog = false }
        )
    }

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
