package com.example.data.importer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Engine for capturing and saving the real cover page snapshot (`cover_snapshot.png`) of an HTML/ZIP book:
 * 1. Direct Cover Image Extraction (`cover.png`, `cover.jpg`, `cover.webp`, `<img class="...cover...">`, `og:image`).
 * 2. WebView First-Page Cover Capture (both offscreen on Main thread and from live ReaderScreen WebView):
 *    Injects temporary CSS to hide `.top-bar, .bottom-nav-dock, .book-header, .controls-bar, header, footer, nav`
 *    and make `.page-canvas, .cover-page, .page, .book-page` fill the snapshot frame at 0.68f aspect ratio,
 *    captures the pure artistic cover `Bitmap` to `File(bookDir, "cover_snapshot.png")`, and immediately restores the UI bars.
 * 3. Instant High-Fidelity Cover Page Renderer fallback for immediate availability before WebView finishes drawing.
 */
object HtmlCoverSnapshotGenerator {

    const val COVER_SNAPSHOT_FILENAME = "cover_snapshot.png"
    const val COVER_ASPECT_RATIO = 0.68f
    private const val SNAPSHOT_WIDTH = 544
    private const val SNAPSHOT_HEIGHT = 800 // 544 / 800 = 0.68f

    private val _snapshotVersion = MutableStateFlow(0)
    val snapshotVersion: StateFlow<Int> = _snapshotVersion.asStateFlow()

    private val generatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val inProgressBookDirs = mutableSetOf<String>()

    private val EXPLICIT_COVER_FILENAMES = listOf(
        "cover.png",
        "cover.jpg",
        "cover.jpeg",
        "cover.webp",
        "images/cover.png",
        "images/cover.jpg",
        "images/cover.jpeg",
        "images/cover.webp",
        "assets/cover.png",
        "assets/cover.jpg",
        "assets/cover.webp",
        "img/cover.png",
        "img/cover.jpg"
    )

    private val IMG_COVER_SRC_REGEX = Regex(
        """<img\b[^>]*class\s*=\s*["'][^"']*cover[^"']*["'][^>]*src\s*=\s*["']([^"']+)["']|<img\b[^>]*src\s*=\s*["']([^"']+)["'][^>]*class\s*=\s*["'][^"']*cover[^"']*["']""",
        RegexOption.IGNORE_CASE
    )

    private val OG_IMAGE_CONTENT_REGEX = Regex(
        """<meta\b[^>]*property\s*=\s*["']og:image["'][^>]*content\s*=\s*["']([^"']+)["']|<meta\b[^>]*content\s*=\s*["']([^"']+)["'][^>]*property\s*=\s*["']og:image["']""",
        RegexOption.IGNORE_CASE
    )

    /**
     * CSS injected temporarily before capturing the WebView cover snapshot so that
     * all top/bottom control bars are hidden and the central cover card fills the 0.68f frame.
     */
    const val COVER_ISOLATION_CSS = """
        .top-bar, .bottom-nav-dock, .book-header, .controls-bar,
        .reader-header, .reader-footer, .nav-bar, .pagination-bar,
        #maktabati-floating-bar, #maktabati-zoom-pill,
        header, footer, nav {
            display: none !important;
            visibility: hidden !important;
            opacity: 0 !important;
            height: 0 !important;
            pointer-events: none !important;
        }
        html, body {
            margin: 0 !important;
            padding: 0 !important;
            width: 100% !important;
            height: 100% !important;
            overflow: hidden !important;
        }
        .book-viewport, .stage-container, .reader-stage, .book-container, main {
            margin: 0 !important;
            padding: 0 !important;
            width: 100% !important;
            height: 100% !important;
            display: flex !important;
            align-items: stretch !important;
            justify-content: stretch !important;
        }
        .page-canvas, .cover-page, .layout-cover, .page, .book-page, [data-page="0"] {
            margin: 0 !important;
            width: 100% !important;
            min-width: 100% !important;
            max-width: 100% !important;
            height: 100% !important;
            min-height: 100% !important;
            max-height: 100% !important;
            border-radius: 0 !important;
            box-shadow: none !important;
            box-sizing: border-box !important;
        }
    """

    fun getCoverSnapshotFile(bookDir: File): File {
        return File(bookDir, COVER_SNAPSHOT_FILENAME)
    }

    fun hasSavedCoverSnapshot(bookDir: File): Boolean {
        val file = getCoverSnapshotFile(bookDir)
        return file.isFile && file.length() > 128L
    }

    fun notifySnapshotUpdated() {
        _snapshotVersion.value = _snapshotVersion.value + 1
    }

    /**
     * Synchronously or asynchronously ensures that a book directory has its `cover_snapshot.png`
     * if and only if the book actually has a cover page or explicit cover image.
     */
    suspend fun ensureCoverSnapshotForBook(
        context: Context,
        bookDir: File,
        entryHtmlFile: File,
        fallbackTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!bookDir.exists() || !entryHtmlFile.isFile) return@withContext false

        val snapshotFile = getCoverSnapshotFile(bookDir)

        // 1. Check for explicit cover image file in bookDir or referenced in HTML
        val htmlContent = runCatching { entryHtmlFile.readText(Charsets.UTF_8) }.getOrDefault("")
        if (tryExtractExplicitCoverImage(bookDir, htmlContent, snapshotFile)) {
            notifySnapshotUpdated()
            return@withContext true
        }

        // 2. Check if the HTML book has a designed cover page
        val metadata = runCatching {
            HtmlBookParser.parseContent(htmlContent, fallbackTitle, quickMode = true)
        }.getOrNull() ?: return@withContext false

        if (!metadata.hasCoverPage) {
            // If the book does NOT have a cover page, do not generate a snapshot so BookCard uses the generated fallback cover
            if (snapshotFile.exists()) {
                snapshotFile.delete()
            }
            return@withContext false
        }

        // 3. If snapshot doesn't exist yet, create an immediate high-fidelity cover page rendering
        //    from the HTML cover DOM/colors/SVG, then schedule offscreen WebView capture on Main thread.
        if (!hasSavedCoverSnapshot(snapshotFile.parentFile ?: bookDir)) {
            val rendered = renderHtmlCoverPageSnapshot(
                htmlContent = htmlContent,
                metadata = metadata,
                targetFile = snapshotFile
            )
            if (rendered) {
                notifySnapshotUpdated()
            }
        }

        // 4. Trigger offscreen WebView capture on Main thread to capture exact CSS/SVG layout
        scheduleOffscreenWebViewCapture(
            context = context.applicationContext,
            bookDir = bookDir,
            entryHtmlFile = entryHtmlFile
        )

        hasSavedCoverSnapshot(bookDir)
    }

    /**
     * Checks for an explicit cover image file (`cover.png`, `cover.jpg`, `cover.webp`, `<img class="...cover...">`, `og:image`)
     * and saves it as `cover_snapshot.png`.
     */
    fun tryExtractExplicitCoverImage(
        bookDir: File,
        htmlContent: String,
        targetSnapshotFile: File = getCoverSnapshotFile(bookDir)
    ): Boolean {
        // Check well-known cover filenames inside bookDir
        for (relPath in EXPLICIT_COVER_FILENAMES) {
            val candidate = File(bookDir, relPath)
            if (candidate.isFile && candidate.length() > 64L) {
                if (decodeAndSaveCoverBitmap(candidate, targetSnapshotFile)) {
                    return true
                }
            }
        }

        // Check any file directly named cover.* in bookDir
        val directCover = bookDir.listFiles()?.firstOrNull { f ->
            f.isFile && f.name != COVER_SNAPSHOT_FILENAME &&
                f.nameWithoutExtension.equals("cover", ignoreCase = true) &&
                f.extension.lowercase() in setOf("png", "jpg", "jpeg", "webp")
        }
        if (directCover != null && decodeAndSaveCoverBitmap(directCover, targetSnapshotFile)) {
            return true
        }

        if (htmlContent.isNotBlank()) {
            val sample = if (htmlContent.length > 35000) htmlContent.substring(0, 35000) else htmlContent
            val imgMatch = IMG_COVER_SRC_REGEX.find(sample)
            val ogMatch = OG_IMAGE_CONTENT_REGEX.find(sample)
            val rawSrc = imgMatch?.let { it.groupValues[1].ifBlank { it.groupValues[2] } }
                ?: ogMatch?.let { it.groupValues[1].ifBlank { it.groupValues[2] } }

            if (!rawSrc.isNullOrBlank()) {
                val trimmedSrc = rawSrc.trim()
                if (trimmedSrc.startsWith("data:image/", ignoreCase = true) && trimmedSrc.contains("base64,")) {
                    val base64Part = trimmedSrc.substringAfter("base64,").trim()
                    val decodedBytes = runCatching { Base64.decode(base64Part, Base64.DEFAULT) }.getOrNull()
                    if (decodedBytes != null && decodedBytes.isNotEmpty()) {
                        val bmp = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bmp != null) {
                            return saveBitmapToFile(cropToCoverAspect(bmp), targetSnapshotFile)
                        }
                    }
                } else if (!trimmedSrc.startsWith("http://", ignoreCase = true) &&
                    !trimmedSrc.startsWith("https://", ignoreCase = true)
                ) {
                    val cleanRel = Uri.decode(trimmedSrc.substringBefore("?").substringBefore("#")).removePrefix("./").removePrefix("/")
                    val imgFile = File(bookDir, cleanRel)
                    if (imgFile.isFile && imgFile.canonicalPath.startsWith(bookDir.canonicalPath)) {
                        if (decodeAndSaveCoverBitmap(imgFile, targetSnapshotFile)) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    /**
     * Captures the first page (cover page) from a live `WebView` in `ReaderScreen.kt`:
     * 1. Injects temporary CSS to hide `.top-bar, .bottom-nav-dock, .book-header, .controls-bar, header, footer, nav`
     *    and expand `.page-canvas, .cover-page, .page, .book-page` to fill the 0.68f frame.
     * 2. Captures the `Bitmap` and writes it to `File(bookDir, "cover_snapshot.png")`.
     * 3. Immediately restores the hidden UI bars so the reader experience is uninterrupted.
     */
    fun captureFromLiveWebView(
        webView: WebView,
        bookDir: File,
        onCaptured: (() -> Unit)? = null
    ) {
        if (webView.width <= 0 || webView.height <= 0) return
        val snapshotFile = getCoverSnapshotFile(bookDir)

        val injectJs = """
            (function() {
                try {
                    var style = document.getElementById('maktabati-cover-snapshot-style');
                    if (!style) {
                        style = document.createElement('style');
                        style.id = 'maktabati-cover-snapshot-style';
                        document.head.appendChild(style);
                    }
                    style.textContent = ${ org.json.JSONObject.quote(COVER_ISOLATION_CSS) };
                } catch(e) {}
                return 'OK';
            })();
        """.trimIndent()

        val restoreJs = """
            (function() {
                try {
                    var style = document.getElementById('maktabati-cover-snapshot-style');
                    if (style) style.remove();
                } catch(e) {}
            })();
        """.trimIndent()

        webView.evaluateJavascript(injectJs) {
            webView.postDelayed({
                try {
                    val w = webView.width.coerceAtLeast(1)
                    val h = webView.height.coerceAtLeast(1)
                    val rawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(rawBitmap)
                    webView.draw(canvas)

                    // Immediately restore the top and bottom bars inside the WebView
                    webView.evaluateJavascript(restoreJs, null)

                    if (isBitmapMeaningful(rawBitmap)) {
                        val cropped = cropToCoverAspect(rawBitmap)
                        generatorScope.launch(Dispatchers.IO) {
                            if (saveBitmapToFile(cropped, snapshotFile)) {
                                notifySnapshotUpdated()
                                withContext(Dispatchers.Main) {
                                    onCaptured?.invoke()
                                }
                            }
                        }
                    } else {
                        rawBitmap.recycle()
                    }
                } catch (_: Throwable) {
                    webView.evaluateJavascript(restoreJs, null)
                }
            }, 90L)
        }
    }

    /**
     * Schedules an offscreen WebView capture on the Main thread so books in the library
     * get their real HTML/CSS/SVG cover captured even before the user opens them.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun scheduleOffscreenWebViewCapture(
        context: Context,
        bookDir: File,
        entryHtmlFile: File
    ) {
        val dirKey = bookDir.absolutePath
        synchronized(inProgressBookDirs) {
            if (dirKey in inProgressBookDirs) return
            inProgressBookDirs.add(dirKey)
        }

        Handler(Looper.getMainLooper()).post {
            try {
                val booksRoot = File(context.filesDir, "books")
                val offscreenWebView = WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = true
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = true
                        defaultTextEncodingName = "UTF-8"
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }
                }

                val widthSpec = View.MeasureSpec.makeMeasureSpec(SNAPSHOT_WIDTH, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(SNAPSHOT_HEIGHT, View.MeasureSpec.EXACTLY)
                offscreenWebView.measure(widthSpec, heightSpec)
                offscreenWebView.layout(0, 0, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT)

                offscreenWebView.webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val reqUri = request?.url ?: return null
                        if (reqUri.host == "maktabati.app" && reqUri.path?.startsWith("/books/") == true) {
                            val rawRelPath = reqUri.path!!.removePrefix("/books/")
                            val decodedRelPath = Uri.decode(rawRelPath)
                            val targetFile = File(booksRoot, decodedRelPath).let { f ->
                                if (f.exists()) f else File(booksRoot, rawRelPath)
                            }
                            try {
                                if (targetFile.isFile && targetFile.canonicalPath.startsWith(booksRoot.canonicalPath)) {
                                    val lower = targetFile.name.lowercase()
                                    val mime = when {
                                        lower.endsWith(".html") || lower.endsWith(".htm") -> "text/html"
                                        lower.endsWith(".css") -> "text/css"
                                        lower.endsWith(".js") -> "application/javascript"
                                        lower.endsWith(".svg") -> "image/svg+xml"
                                        lower.endsWith(".png") -> "image/png"
                                        lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
                                        lower.endsWith(".webp") -> "image/webp"
                                        else -> "application/octet-stream"
                                    }
                                    return WebResourceResponse(
                                        mime,
                                        "UTF-8",
                                        BufferedInputStream(FileInputStream(targetFile))
                                    )
                                }
                            } catch (_: Exception) {}
                        }
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val wv = view ?: return
                        val injectJs = """
                            (function() {
                                try {
                                    if (typeof goToPage === 'function') goToPage(0);
                                    var style = document.createElement('style');
                                    style.id = 'maktabati-cover-snapshot-style';
                                    style.textContent = ${ org.json.JSONObject.quote(COVER_ISOLATION_CSS) };
                                    document.head.appendChild(style);
                                } catch(e) {}
                                return 'OK';
                            })();
                        """.trimIndent()

                        wv.evaluateJavascript(injectJs) {
                            wv.postDelayed({
                                try {
                                    wv.measure(widthSpec, heightSpec)
                                    wv.layout(0, 0, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT)
                                    val bmp = Bitmap.createBitmap(SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bmp)
                                    wv.draw(canvas)

                                    if (isBitmapMeaningful(bmp)) {
                                        val snapshotFile = getCoverSnapshotFile(bookDir)
                                        generatorScope.launch(Dispatchers.IO) {
                                            if (saveBitmapToFile(bmp, snapshotFile)) {
                                                notifySnapshotUpdated()
                                            }
                                        }
                                    } else {
                                        bmp.recycle()
                                    }
                                } catch (_: Throwable) {
                                } finally {
                                    runCatching {
                                        wv.stopLoading()
                                        wv.destroy()
                                    }
                                    synchronized(inProgressBookDirs) {
                                        inProgressBookDirs.remove(dirKey)
                                    }
                                }
                            }, 280L)
                        }
                    }
                }

                val relFolder = bookDir.name
                val relEntry = entryHtmlFile.relativeTo(bookDir).path.replace("\\", "/")
                val virtualUrl = "https://maktabati.app/books/$relFolder/$relEntry"
                offscreenWebView.loadUrl(virtualUrl)
            } catch (_: Throwable) {
                synchronized(inProgressBookDirs) {
                    inProgressBookDirs.remove(dirKey)
                }
            }
        }
    }

    /**
     * Renders an immediate, high-fidelity cover page bitmap directly from the book's actual
     * cover page parameters (true background color, true primary/rose color, true gold/ornament color,
     * title, subtitle, author, and decorative cover geometry) so `cover_snapshot.png` is immediately ready on disk.
     */
    fun renderHtmlCoverPageSnapshot(
        htmlContent: String,
        metadata: ParsedHtmlMetadata,
        targetFile: File
    ): Boolean {
        return try {
            val bmp = Bitmap.createBitmap(SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            val bgLong = HtmlBookParser.parseHexToColorLong(metadata.bgPrimaryHex, 0xFF1E293B)
            val roseLong = HtmlBookParser.parseHexToColorLong(metadata.accentRoseHex, 0xFF5B3A29)
            val goldLong = HtmlBookParser.parseHexToColorLong(metadata.accentGoldHex, 0xFFD97706)

            val isLightCover = HtmlBookParser.computeColorLuminance(bgLong) > 0.48f
            val endLong = if (isLightCover) {
                HtmlBookParser.blendColorLongs(bgLong, roseLong, 0.14f)
            } else {
                HtmlBookParser.blendColorLongs(bgLong, roseLong, 0.42f)
            }

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    SNAPSHOT_WIDTH.toFloat(),
                    SNAPSHOT_HEIGHT.toFloat(),
                    bgLong.toInt(),
                    endLong.toInt(),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, SNAPSHOT_WIDTH.toFloat(), SNAPSHOT_HEIGHT.toFloat(), bgPaint)

            val frameColorInt = goldLong.toInt()
            val primaryColorInt = roseLong.toInt()
            val titleColorInt = if (isLightCover) primaryColorInt else 0xFFFFFFFF.toInt()
            val subTextColorInt = if (isLightCover) {
                HtmlBookParser.blendColorLongs(roseLong, 0xFF2D1A22, 0.45f).toInt()
            } else {
                0xFFE2E8F0.toInt()
            }

            // Outer & inner luxury ornamental frames
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
                color = frameColorInt
                alpha = if (isLightCover) 190 else 210
            }
            val outerRect = RectF(28f, 28f, SNAPSHOT_WIDTH - 28f, SNAPSHOT_HEIGHT - 28f)
            canvas.drawRoundRect(outerRect, 20f, 20f, borderPaint)

            borderPaint.strokeWidth = 1.6f
            borderPaint.alpha = if (isLightCover) 130 else 150
            val innerRect = RectF(40f, 40f, SNAPSHOT_WIDTH - 40f, SNAPSHOT_HEIGHT - 40f)
            canvas.drawRoundRect(innerRect, 14f, 14f, borderPaint)

            // Center medallion / SVG-inspired emblem at top center
            val emblemCenterY = 195f
            val emblemCenterX = SNAPSHOT_WIDTH / 2f
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = primaryColorInt
                alpha = if (isLightCover) 24 else 48
            }
            canvas.drawCircle(emblemCenterX, emblemCenterY, 72f, glowPaint)

            val emblemStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = frameColorInt
                alpha = 200
            }
            canvas.drawCircle(emblemCenterX, emblemCenterY, 52f, emblemStroke)

            // Diamond ornament inside medallion
            val diamondPath = Path().apply {
                moveTo(emblemCenterX, emblemCenterY - 32f)
                lineTo(emblemCenterX + 32f, emblemCenterY)
                lineTo(emblemCenterX, emblemCenterY + 32f)
                lineTo(emblemCenterX - 32f, emblemCenterY)
                close()
            }
            canvas.drawPath(diamondPath, emblemStroke)

            // Draw Book Title
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = titleColorInt
                textSize = 44f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            val textMaxWidth = SNAPSHOT_WIDTH - 120
            val titleLayout = StaticLayout.Builder
                .obtain(metadata.title, 0, metadata.title.length, titlePaint, textMaxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(6f, 1.12f)
                .setMaxLines(3)
                .build()

            canvas.save()
            val titleY = 310f
            canvas.translate(60f, titleY)
            titleLayout.draw(canvas)
            canvas.restore()

            // Decorative divider under title
            val dividerY = titleY + titleLayout.height + 28f
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = frameColorInt
                alpha = 190
            }
            canvas.drawLine(emblemCenterX - 85f, dividerY, emblemCenterX + 85f, dividerY, dividerPaint)

            // Draw Subtitle if present
            if (metadata.subtitle.isNotBlank()) {
                val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = subTextColorInt
                    textSize = 24f
                    typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                }
                val subLayout = StaticLayout.Builder
                    .obtain(metadata.subtitle, 0, metadata.subtitle.length, subPaint, textMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(4f, 1.15f)
                    .setMaxLines(3)
                    .build()

                canvas.save()
                canvas.translate(60f, dividerY + 24f)
                subLayout.draw(canvas)
                canvas.restore()
            }

            // Draw Author badge at bottom
            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isLightCover) primaryColorInt else frameColorInt
                textSize = 25f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }
            val authorLayout = StaticLayout.Builder
                .obtain(metadata.author, 0, metadata.author.length, authorPaint, textMaxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(1)
                .build()

            canvas.save()
            canvas.translate(60f, SNAPSHOT_HEIGHT - 115f)
            authorLayout.draw(canvas)
            canvas.restore()

            saveBitmapToFile(bmp, targetFile)
        } catch (_: Throwable) {
            false
        }
    }

    private fun isBitmapMeaningful(bitmap: Bitmap): Boolean {
        if (bitmap.width < 10 || bitmap.height < 10) return false
        val samplePoints = listOf(
            Pair(bitmap.width / 2, bitmap.height / 2),
            Pair(bitmap.width / 3, bitmap.height / 3),
            Pair(bitmap.width * 2 / 3, bitmap.height / 3),
            Pair(bitmap.width / 3, bitmap.height * 2 / 3),
            Pair(bitmap.width * 2 / 3, bitmap.height * 2 / 3),
            Pair(bitmap.width / 2, bitmap.height / 4),
            Pair(bitmap.width / 2, bitmap.height * 3 / 4)
        )
        val colors = samplePoints.map { (x, y) ->
            bitmap.getPixel(x.coerceIn(0, bitmap.width - 1), y.coerceIn(0, bitmap.height - 1))
        }
        val nonTransparent = colors.any { ((it ushr 24) and 0xFF) > 10 }
        if (!nonTransparent) return false
        // Check if at least 2 sample points differ (meaning content/text/ornaments were drawn)
        return colors.distinct().size >= 2
    }

    private fun decodeAndSaveCoverBitmap(sourceFile: File, targetFile: File): Boolean {
        return try {
            val decoded = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return false
            val cropped = cropToCoverAspect(decoded)
            saveBitmapToFile(cropped, targetFile)
        } catch (_: Throwable) {
            false
        }
    }

    private fun cropToCoverAspect(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0) return source
        val currentRatio = w.toFloat() / h.toFloat()
        val targetRatio = COVER_ASPECT_RATIO

        return if (kotlin.math.abs(currentRatio - targetRatio) < 0.04f) {
            if (w > SNAPSHOT_WIDTH * 1.4f) {
                Bitmap.createScaledBitmap(source, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, true)
            } else {
                source
            }
        } else if (currentRatio > targetRatio) {
            val newW = (h * targetRatio).toInt().coerceAtLeast(1)
            val startX = ((w - newW) / 2).coerceAtLeast(0)
            val cropped = Bitmap.createBitmap(source, startX, 0, newW, h)
            Bitmap.createScaledBitmap(cropped, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, true)
        } else {
            val newH = (w / targetRatio).toInt().coerceAtLeast(1)
            val startY = ((h - newH) / 2).coerceAtLeast(0)
            val cropped = Bitmap.createBitmap(source, 0, startY, w, newH)
            Bitmap.createScaledBitmap(cropped, SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, true)
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, targetFile: File): Boolean {
        return try {
            targetFile.parentFile?.mkdirs()
            val tempFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 92, fos)
                fos.flush()
            }
            if (tempFile.exists() && tempFile.length() > 128L) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (_: Throwable) {
            false
        }
    }
}
