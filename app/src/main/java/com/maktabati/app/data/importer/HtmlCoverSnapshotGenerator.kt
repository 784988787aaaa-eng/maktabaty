package com.maktabati.app.data.importer

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
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.webkit.WebView
import androidx.core.content.res.ResourcesCompat
import com.maktabati.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.cos
import kotlin.math.sin

object HtmlCoverSnapshotGenerator {

    const val SNAPSHOT_FILE_NAME = "cover_v3.png"
    const val COVER_SNAPSHOT_FILENAME = SNAPSHOT_FILE_NAME
    const val LEGACY_SNAPSHOT_FILENAME = "cover_snapshot.png"
    const val COVER_ASPECT_RATIO = 0.68f
    const val SNAPSHOT_WIDTH = 544
    const val SNAPSHOT_HEIGHT = 800 // 544 / 800 = 0.68f

    private val _snapshotVersion = MutableStateFlow(0)
    val snapshotVersion: StateFlow<Int> = _snapshotVersion.asStateFlow()

    private val MARKDOWN_OR_NOISE_WRAP_REGEX = Regex("[*#_~`^|]+")
    private val EMOJI_SURROGATE_REGEX = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+")
    private val DOTS_ONLY_REGEX = Regex("^[.\\s…\\-_—•*✦✧]+$")
    private val MULTI_SPACE_REGEX = Regex("\\s+")

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

    fun getCoverSnapshotFile(bookDir: File): File {
        return File(bookDir, SNAPSHOT_FILE_NAME)
    }

    fun hasSavedCoverSnapshot(bookDir: File): Boolean {
        val file = getCoverSnapshotFile(bookDir)
        return file.isFile && file.length() > 128L
    }

    fun cleanupLegacySnapshotIfPresent(bookDir: File?) {
        if (bookDir == null) return
        val legacy = File(bookDir, LEGACY_SNAPSHOT_FILENAME)
        if (legacy.exists()) {
            runCatching { legacy.delete() }
        }
    }

    fun notifySnapshotUpdated() {
        _snapshotVersion.value = _snapshotVersion.value + 1
    }

    fun sanitizeCoverTitle(rawTitle: String): String {
        val cleaned = rawTitle
            .replace(MARKDOWN_OR_NOISE_WRAP_REGEX, " ")
            .replace(EMOJI_SURROGATE_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
            .trim('*', '#', '-', '_', '•', '✦', '✧', ' ')
        return cleaned.ifBlank { rawTitle.trim().ifBlank { "كتاب" } }
    }

    fun sanitizeCoverSubtitle(rawSubtitle: String, cleanTitle: String = ""): String {
        val cleaned = rawSubtitle
            .replace(MARKDOWN_OR_NOISE_WRAP_REGEX, " ")
            .replace(EMOJI_SURROGATE_REGEX, " ")
            .replace(MULTI_SPACE_REGEX, " ")
            .trim()
            .trim('*', '#', '-', '_', ' ')
        if (cleaned.isBlank() || DOTS_ONLY_REGEX.matches(cleaned)) {
            return ""
        }
        if (cleanTitle.isNotBlank() && cleaned.equals(cleanTitle, ignoreCase = true)) {
            return ""
        }
        return cleaned
    }

    suspend fun ensureCoverSnapshotForBook(
        context: Context,
        bookDir: File,
        entryHtmlFile: File,
        fallbackTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!bookDir.exists() || !entryHtmlFile.isFile) return@withContext false

        cleanupLegacySnapshotIfPresent(bookDir)
        val snapshotFile = getCoverSnapshotFile(bookDir)

        if (hasSavedCoverSnapshot(bookDir)) {
            return@withContext true
        }

        val htmlSample = runCatching {
            entryHtmlFile.inputStream().bufferedReader(Charsets.UTF_8).use { reader ->
                val buf = CharArray(45_000)
                val read = reader.read(buf)
                if (read > 0) String(buf, 0, read) else ""
            }
        }.getOrDefault("")

        if (tryExtractExplicitCoverImage(bookDir, htmlSample, snapshotFile)) {
            notifySnapshotUpdated()
            return@withContext true
        }

        val metadata = runCatching {
            HtmlBookParser.parseQuickHeader(htmlSample, fallbackTitle, bookDir = bookDir)
        }.getOrNull() ?: return@withContext false

        if (!metadata.hasCoverPage) {
            if (snapshotFile.exists()) {
                snapshotFile.delete()
            }
            return@withContext false
        }

        val rendered = renderHtmlCoverPageSnapshot(
            htmlContent = htmlSample,
            metadata = metadata,
            targetFile = snapshotFile,
            context = context
        )
        if (rendered) {
            notifySnapshotUpdated()
        }

        hasSavedCoverSnapshot(bookDir)
    }

    fun tryExtractExplicitCoverImage(
        bookDir: File,
        htmlContent: String,
        targetSnapshotFile: File = getCoverSnapshotFile(bookDir)
    ): Boolean {
        for (relPath in EXPLICIT_COVER_FILENAMES) {
            val candidate = File(bookDir, relPath)
            if (candidate.isFile && candidate.length() > 64L) {
                if (decodeAndSaveCoverBitmap(candidate, targetSnapshotFile)) {
                    cleanupLegacySnapshotIfPresent(bookDir)
                    return true
                }
            }
        }

        val directCover = bookDir.listFiles()?.firstOrNull { f ->
            f.isFile &&
                f.name != SNAPSHOT_FILE_NAME &&
                f.name != LEGACY_SNAPSHOT_FILENAME &&
                f.nameWithoutExtension.equals("cover", ignoreCase = true) &&
                f.extension.lowercase() in setOf("png", "jpg", "jpeg", "webp")
        }
        if (directCover != null && decodeAndSaveCoverBitmap(directCover, targetSnapshotFile)) {
            cleanupLegacySnapshotIfPresent(bookDir)
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
                            val saved = saveBitmapToFile(cropToCoverAspect(bmp), targetSnapshotFile)
                            if (saved) cleanupLegacySnapshotIfPresent(bookDir)
                            return saved
                        }
                    }
                } else if (!trimmedSrc.startsWith("http://", ignoreCase = true) &&
                    !trimmedSrc.startsWith("https://", ignoreCase = true)
                ) {
                    val cleanRel = Uri.decode(trimmedSrc.substringBefore("?").substringBefore("#"))
                        .removePrefix("./")
                        .removePrefix("/")
                    val imgFile = File(bookDir, cleanRel)
                    if (imgFile.isFile && imgFile.canonicalPath.startsWith(bookDir.canonicalPath)) {
                        if (decodeAndSaveCoverBitmap(imgFile, targetSnapshotFile)) {
                            cleanupLegacySnapshotIfPresent(bookDir)
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    @Suppress("UNUSED_PARAMETER")
    fun captureFromLiveWebView(
        webView: WebView,
        bookDir: File,
        onCaptured: (() -> Unit)? = null
    ) {
        // Intentionally disabled: live WebView capture causes inconsistent zoom/aspect cropping across books.
    }

    @Suppress("UNUSED_PARAMETER")
    fun scheduleOffscreenWebViewCapture(
        context: Context,
        bookDir: File,
        entryHtmlFile: File
    ) {
        // Intentionally disabled: canonical fixed-grid Canvas cover renderer ensures 100% uniform proportions.
    }

    @Suppress("UNUSED_PARAMETER")
    fun renderHtmlCoverPageSnapshot(
        htmlContent: String,
        metadata: ParsedHtmlMetadata,
        targetFile: File,
        context: Context? = null
    ): Boolean {
        return try {
            cleanupLegacySnapshotIfPresent(targetFile.parentFile)

            val bmp = Bitmap.createBitmap(SNAPSHOT_WIDTH, SNAPSHOT_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            val serifBoldFallback = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            val serifNormalFallback = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

            val titleTypeface = if (context != null && metadata.isRtl) {
                runCatching { ResourcesCompat.getFont(context, R.font.aref_ruqaa) }.getOrNull()
                    ?.let { Typeface.create(it, Typeface.BOLD) }
                    ?: serifBoldFallback
            } else {
                serifBoldFallback
            }

            val badgeTypeface = if (context != null && metadata.isRtl) {
                runCatching { ResourcesCompat.getFont(context, R.font.noto_kufi_arabic) }.getOrNull()
                    ?.let { Typeface.create(it, Typeface.BOLD) }
                    ?: serifBoldFallback
            } else {
                serifBoldFallback
            }

            val bgLong = HtmlBookParser.parseHexToColorLong(metadata.bgPrimaryHex, 0xFFFAF3EB)
            val roseLong = HtmlBookParser.parseHexToColorLong(metadata.accentRoseHex, 0xFF6B2D46)
            val goldLong = HtmlBookParser.parseHexToColorLong(metadata.accentGoldHex, 0xFFC59B27)

            val isLightCover = HtmlBookParser.computeColorLuminance(bgLong) > 0.48f
            val endLong = if (isLightCover) {
                HtmlBookParser.blendColorLongs(bgLong, roseLong, 0.12f)
            } else {
                HtmlBookParser.blendColorLongs(bgLong, roseLong, 0.38f)
            }

            // 1. Uniform Background Gradient (544x800)
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

            // Subtle geometric watermark grid
            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = frameColorInt
                alpha = if (isLightCover) 18 else 24
            }
            var gx = 68f
            while (gx < SNAPSHOT_WIDTH - 40f) {
                canvas.drawLine(gx, 40f, gx, SNAPSHOT_HEIGHT - 40f, gridPaint)
                gx += 68f
            }
            var gy = 80f
            while (gy < SNAPSHOT_HEIGHT - 40f) {
                canvas.drawLine(40f, gy, SNAPSHOT_WIDTH - 40f, gy, gridPaint)
                gy += 80f
            }

            // 2. Fixed Double Outer Frame (28px & 40px insets)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = frameColorInt
                alpha = if (isLightCover) 195 else 215
            }
            val outerRect = RectF(28f, 28f, SNAPSHOT_WIDTH - 28f, SNAPSHOT_HEIGHT - 28f)
            canvas.drawRoundRect(outerRect, 20f, 20f, borderPaint)

            borderPaint.strokeWidth = 1.2f
            borderPaint.alpha = if (isLightCover) 135 else 155
            val innerRect = RectF(40f, 40f, SNAPSHOT_WIDTH - 40f, SNAPSHOT_HEIGHT - 40f)
            canvas.drawRoundRect(innerRect, 14f, 14f, borderPaint)

            // Fixed Corner Ornaments at the 4 corners of the inner frame
            val cornerAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = frameColorInt
                alpha = if (isLightCover) 185 else 205
            }
            val corners = listOf(
                Pair(40f, 40f),
                Pair(SNAPSHOT_WIDTH - 40f, 40f),
                Pair(40f, SNAPSHOT_HEIGHT - 40f),
                Pair(SNAPSHOT_WIDTH - 40f, SNAPSHOT_HEIGHT - 40f)
            )
            for ((cx, cy) in corners) {
                canvas.drawCircle(cx, cy, 4.5f, cornerAccentPaint)
            }

            // 3. Fixed Top Eyebrow at Y = 104px
            val eyebrowText = if (metadata.isRtl) {
                "✦ طبعة رقمية فاخرة • مكتبتي ✦"
            } else {
                "✦ LUXURY DIGITAL EDITION • MAKTABATI ✦"
            }
            val eyebrowPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = frameColorInt
                textSize = 16.5f
                textAlign = Paint.Align.CENTER
                typeface = badgeTypeface
            }
            canvas.drawText(eyebrowText, SNAPSHOT_WIDTH / 2f, 104f, eyebrowPaint)

            // 4. Compact Central Medallion at (X = 272px, Y = 215px, R = 52px)
            val emblemCenterX = 272f
            val emblemCenterY = 215f
            val emblemRadius = 52f

            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = primaryColorInt
                alpha = if (isLightCover) 20 else 44
            }
            canvas.drawCircle(emblemCenterX, emblemCenterY, emblemRadius, glowPaint)

            val dashedCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = frameColorInt
                alpha = 155
            }
            canvas.drawCircle(emblemCenterX, emblemCenterY, emblemRadius, dashedCirclePaint)

            val emblemStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2f
                color = frameColorInt
                alpha = 210
            }
            canvas.drawCircle(emblemCenterX, emblemCenterY, 40f, emblemStroke)

            // 8-pointed Rub el Hizb / Star inside the medallion
            val starRadiusOuter = 27f
            val starRadiusInner = 11f
            val starPath = Path()
            for (i in 0 until 8) {
                val angleDeg = i * 45.0 - 90.0
                val r = if (i % 2 == 0) starRadiusOuter else starRadiusInner
                val px = emblemCenterX + (r * cos(Math.toRadians(angleDeg))).toFloat()
                val py = emblemCenterY + (r * sin(Math.toRadians(angleDeg))).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            val starFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = primaryColorInt
                alpha = if (isLightCover) 36 else 65
            }
            canvas.drawPath(starPath, starFillPaint)
            val starStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.8f
                color = primaryColorInt
                alpha = 215
            }
            canvas.drawPath(starPath, starStrokePaint)
            canvas.drawCircle(emblemCenterX, emblemCenterY, 4.5f, cornerAccentPaint)

            // 5. Royal Hero Title Zone (cover-title-frame) with auto-fitted 34f..50f and max 3 lines
            val cleanTitle = sanitizeCoverTitle(metadata.title)
            val cleanSubtitle = sanitizeCoverSubtitle(metadata.subtitle, cleanTitle)
            val cleanAuthor = HtmlBookParser.sanitizeExtractedAuthor(
                metadata.author,
                cleanTitle,
                cleanSubtitle
            )

            val textMaxWidth = 420
            val textLeftX = (SNAPSHOT_WIDTH - textMaxWidth) / 2f
            val chosenTitleSize = when {
                cleanTitle.length > 48 -> 34f
                cleanTitle.length > 32 -> 40f
                cleanTitle.length > 18 -> 45f
                else -> 50f
            }

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = titleColorInt
                textSize = chosenTitleSize
                typeface = titleTypeface
            }
            val titleLayout = StaticLayout.Builder
                .obtain(cleanTitle, 0, cleanTitle.length, titlePaint, textMaxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(6f, 1.14f)
                .setMaxLines(3)
                .build()

            val titleStartY = 330f
            val frameTopY = titleStartY - 22f
            val frameBottomY = titleStartY + titleLayout.height + 22f
            val frameLeftX = 56f
            val frameRightX = SNAPSHOT_WIDTH - 56f

            // Soft gradient background for cover-title-frame
            val titleFrameBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    frameLeftX,
                    frameTopY,
                    frameRightX,
                    frameBottomY,
                    intArrayOf(
                        (frameColorInt and 0x00FFFFFF) or (if (isLightCover) 0x08000000 else 0x10000000),
                        (frameColorInt and 0x00FFFFFF) or (if (isLightCover) 0x22000000 else 0x2E000000),
                        (frameColorInt and 0x00FFFFFF) or (if (isLightCover) 0x08000000 else 0x10000000)
                    ),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(
                RectF(frameLeftX, frameTopY, frameRightX, frameBottomY),
                14f,
                14f,
                titleFrameBgPaint
            )

            val titleFrameRulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.2f
                color = frameColorInt
                alpha = if (isLightCover) 190 else 215
            }
            canvas.drawLine(frameLeftX + 18f, frameTopY, frameRightX - 18f, frameTopY, titleFrameRulePaint)
            canvas.drawLine(frameLeftX + 18f, frameBottomY, frameRightX - 18f, frameBottomY, titleFrameRulePaint)

            canvas.save()
            canvas.translate(textLeftX, titleStartY)
            titleLayout.draw(canvas)
            canvas.restore()

            // 6. Subtitle Zone directly below Title Frame (max 2 lines, font size 21f)
            if (cleanSubtitle.isNotBlank()) {
                val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = subTextColorInt
                    textSize = 21f
                    typeface = serifNormalFallback
                }
                val subLayout = StaticLayout.Builder
                    .obtain(cleanSubtitle, 0, cleanSubtitle.length, subPaint, textMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(4f, 1.14f)
                    .setMaxLines(2)
                    .build()

                val subtitleY = (frameBottomY + 20f).coerceAtMost(565f)
                canvas.save()
                canvas.translate(textLeftX, subtitleY)
                subLayout.draw(canvas)
                canvas.restore()
            }

            // 7. Fixed Triple Ornament at Y = 650px & Real Author ONLY when present
            val ornamentY = if (cleanAuthor.isNotBlank()) 642f else 675f
            val ornamentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = frameColorInt
                textSize = 20f
                textAlign = Paint.Align.CENTER
                typeface = serifBoldFallback
            }
            canvas.drawText("✦   ✦   ✦", SNAPSHOT_WIDTH / 2f, ornamentY, ornamentPaint)

            if (cleanAuthor.isNotBlank()) {
                val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isLightCover) primaryColorInt else frameColorInt
                    textSize = 21f
                    typeface = badgeTypeface
                }
                val authorLayout = StaticLayout.Builder
                    .obtain(cleanAuthor, 0, cleanAuthor.length, authorPaint, textMaxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setMaxLines(1)
                    .build()

                canvas.save()
                canvas.translate(textLeftX, 676f)
                authorLayout.draw(canvas)
                canvas.restore()
            }

            saveBitmapToFile(bmp, targetFile)
        } catch (_: Throwable) {
            false
        }
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
