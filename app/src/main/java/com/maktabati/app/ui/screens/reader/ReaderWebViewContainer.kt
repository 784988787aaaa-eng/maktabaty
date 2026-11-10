package com.maktabati.app.ui.screens.reader

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.sandbox.SandboxHtmlInjector
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import com.maktabati.app.data.typography.UserTypographyPreferences
import com.maktabati.app.ui.reader.MaktabatiJsBridge
import com.maktabati.app.ui.reader.ObservableWebView
import com.maktabati.app.ui.reader.ReaderThemeMode
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import kotlin.math.max
import kotlin.math.min

/**
 * Fast O(1) check that inspects the header (up to 64KB) and tail (16KB) of an HTML file
 * using [RandomAccessFile] to verify whether `__maktabatiV9Initialized` is already injected,
 * avoiding full-file `readText()` allocations when opening large multi-megabyte books.
 */
fun isHtmlAlreadyInjected(file: File, marker: String = "__maktabatiV9Initialized"): Boolean {
    if (!file.isFile) return false
    val len = file.length()
    if (len <= 0L) return false
    return try {
        RandomAccessFile(file, "r").use { raf ->
            val headSize = min(len, 65_536L).toInt()
            val headBytes = ByteArray(headSize)
            raf.seek(0L)
            raf.readFully(headBytes)
            val headText = String(headBytes, Charsets.UTF_8)
            if (headText.contains(marker)) return true

            if (len > 65_536L) {
                val tailSize = min(len, 16_384L).toInt()
                val tailStart = max(0L, len - tailSize)
                val tailBytes = ByteArray(tailSize)
                raf.seek(tailStart)
                raf.readFully(tailBytes)
                val tailText = String(tailBytes, Charsets.UTF_8)
                if (tailText.contains(marker)) return true
            }
            false
        }
    } catch (_: Throwable) {
        false
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderWebViewContainer(
    book: Book,
    livePageCount: Int,
    textZoomPercent: Int,
    readerTheme: ReaderThemeMode,
    userTypographyPreferences: UserTypographyPreferences,
    lastAppliedZoom: IntArray,
    lastAppliedTheme: Array<ReaderThemeMode?>,
    lastAppliedTypography: Array<UserTypographyPreferences?>,
    onTouchCollapseControls: () -> Unit,
    onPageProgressChanged: (page: Int, total: Int, prog: Float) -> Unit,
    onZoomChangedFromJs: (zoomPct: Int) -> Unit,
    onScrollChanged: (scrollY: Int, maxScroll: Int) -> Unit,
    onWebViewCreated: (ObservableWebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookDirection = if (book.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides bookDirection) {
        key(book.id) {
            AndroidView(
                modifier = modifier
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
                        // Set native WebView canvas color immediately before any HTML loads to prevent white flash
                        setBackgroundColor(ReaderWebViewStyleApplicator.resolveThemeBackgroundColorInt(readerTheme))
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
                            setSupportZoom(false)
                            builtInZoomControls = false
                            displayZoomControls = false
                            useWideViewPort = false
                            loadWithOverviewMode = false
                            defaultTextEncodingName = "UTF-8"
                            textZoom = 100
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }
                        lastAppliedZoom[0] = textZoomPercent
                        lastAppliedTheme[0] = readerTheme
                        lastAppliedTypography[0] = userTypographyPreferences

                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = false

                        setOnTouchListener { _, event ->
                            if (event.action == MotionEvent.ACTION_DOWN) {
                                onTouchCollapseControls()
                            }
                            false
                        }

                        addJavascriptInterface(
                            MaktabatiJsBridge(
                                totalBookPages = livePageCount,
                                onPageProgressChanged = onPageProgressChanged,
                                onZoomChangedFromJs = onZoomChangedFromJs
                            ),
                            "MaktabatiBridge"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val reqUri = request?.url ?: return null
                                if (reqUri.host == ReaderWebViewStyleApplicator.VIRTUAL_BOOK_HOST) {
                                    MaktabatiFontRegistry.interceptFontRequest(
                                        context = ctx,
                                        requestPath = reqUri.path,
                                        bookDir = bookDir
                                    )?.let { return it }
                                }
                                if (reqUri.host == ReaderWebViewStyleApplicator.VIRTUAL_BOOK_HOST && reqUri.path?.startsWith("/books/") == true) {
                                    val rawRelPath = reqUri.path!!.removePrefix("/books/")
                                    val decodedRelPath = Uri.decode(rawRelPath)
                                    val targetFile = File(booksRoot, decodedRelPath).let { f ->
                                        if (f.exists()) f else File(booksRoot, rawRelPath)
                                    }
                                    try {
                                        if (targetFile.isFile && targetFile.canonicalPath.startsWith(booksRoot.canonicalPath)) {
                                            val mime = ReaderWebViewStyleApplicator.resolveMimeType(targetFile.name)
                                            if (mime == "text/html") {
                                                val activeTheme = lastAppliedTheme[0] ?: readerTheme
                                                val activeTypo = lastAppliedTypography[0] ?: userTypographyPreferences
                                                val activeZoom = lastAppliedZoom[0].takeIf { it > 0 } ?: textZoomPercent

                                                val baseInjectedHtml = if (!isHtmlAlreadyInjected(targetFile)) {
                                                    val raw = targetFile.readText(Charsets.UTF_8)
                                                    if (!raw.contains("__maktabatiV9Initialized")) {
                                                        val upgraded = SandboxHtmlInjector.injectIntoHtml(
                                                            rawHtml = raw,
                                                            bookId = book.id,
                                                            pageCount = livePageCount,
                                                            bookDir = bookDir
                                                        )
                                                        targetFile.writeText(upgraded, Charsets.UTF_8)
                                                        upgraded
                                                    } else {
                                                        raw
                                                    }
                                                } else {
                                                    targetFile.readText(Charsets.UTF_8)
                                                }

                                                val preStyledHtml = ReaderWebViewStyleApplicator.prepareHtmlStreamWithActiveState(
                                                    rawHtml = baseInjectedHtml,
                                                    theme = activeTheme,
                                                    typoPrefs = activeTypo,
                                                    zoomPct = activeZoom
                                                )

                                                return WebResourceResponse(
                                                    "text/html",
                                                    "UTF-8",
                                                    200,
                                                    "OK",
                                                    mapOf(
                                                        "Access-Control-Allow-Origin" to "*",
                                                        "Cache-Control" to "no-store"
                                                    ),
                                                    preStyledHtml.byteInputStream(Charsets.UTF_8)
                                                )
                                            }
                                            return WebResourceResponse(
                                                mime,
                                                if (ReaderWebViewStyleApplicator.isTextMimeType(mime)) "UTF-8" else null,
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
                                    ReaderWebViewStyleApplicator.applyThemeToWebView(wv, readerTheme)
                                    ReaderWebViewStyleApplicator.applyTypographyPreferencesToWebView(wv, userTypographyPreferences)
                                    if (textZoomPercent != 100) {
                                        ReaderWebViewStyleApplicator.applyFluidZoomToWebView(wv, textZoomPercent)
                                    }
                                    val savedPage = book.currentPage.coerceAtLeast(1)
                                    if (savedPage > 1) {
                                        wv.evaluateJavascript(
                                            "if (typeof window.__maktabatiGoToDomPage === 'function') window.__maktabatiGoToDomPage(${savedPage - 1}); else if (typeof goToPage === 'function') goToPage(${savedPage - 1});",
                                            null
                                        )
                                    }
                                }
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true && request.url?.host == ReaderWebViewStyleApplicator.VIRTUAL_BOOK_HOST && entryFile.isFile) {
                                    view?.loadUrl(Uri.fromFile(entryFile).toString())
                                }
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false
                            }
                        }

                        webChromeClient = WebChromeClient()

                        onScrollListener = { scrollY, maxScroll ->
                            onScrollChanged(scrollY, maxScroll)
                        }

                        if (entryFile.exists()) {
                            val virtualUrl = "${ReaderWebViewStyleApplicator.VIRTUAL_BOOK_BASE}${book.folderPath}/${book.entryHtmlPath}"
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

                        onWebViewCreated(this)
                    }
                },
                update = { wv ->
                    if (lastAppliedZoom[0] != textZoomPercent) {
                        lastAppliedZoom[0] = textZoomPercent
                        ReaderWebViewStyleApplicator.applyFluidZoomToWebView(wv, textZoomPercent)
                    }
                    if (lastAppliedTheme[0] != readerTheme) {
                        lastAppliedTheme[0] = readerTheme
                        wv.setBackgroundColor(ReaderWebViewStyleApplicator.resolveThemeBackgroundColorInt(readerTheme))
                        ReaderWebViewStyleApplicator.applyThemeToWebView(wv, readerTheme)
                    }
                    if (lastAppliedTypography[0] != userTypographyPreferences) {
                        lastAppliedTypography[0] = userTypographyPreferences
                        ReaderWebViewStyleApplicator.applyTypographyPreferencesToWebView(wv, userTypographyPreferences)
                    }
                }
            )
        }
    }
}
