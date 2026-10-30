package com.example.ui.reader

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class MaktabatiJsBridge(
    private val totalBookPages: Int,
    private val onPageProgressChanged: (page: Int, totalPages: Int, progress: Float) -> Unit,
    private val onZoomChangedFromJs: ((zoomPercent: Int) -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPageChanged(currentPage: Int, totalPages: Int) {
        val total = if (totalPages > 0) totalPages else totalBookPages.coerceAtLeast(1)
        val clampedPage = currentPage.coerceIn(1, total)
        val prog = if (total <= 1 || clampedPage >= total) {
            1.0f
        } else {
            (clampedPage.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
        mainHandler.post {
            onPageProgressChanged(clampedPage, total, prog)
        }
    }

    @JavascriptInterface
    fun onZoomChanged(zoomPercent: Int) {
        val clamped = zoomPercent.coerceIn(75, 200)
        mainHandler.post {
            onZoomChangedFromJs?.invoke(clamped)
        }
    }
}
