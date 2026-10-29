package com.example.ui.reader

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class MaktabatiJsBridge(
    private val totalBookPages: Int,
    private val onPageProgressChanged: (page: Int, progress: Float) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPageChanged(currentPage: Int, totalPages: Int) {
        val total = if (totalPages > 0) totalPages else totalBookPages.coerceAtLeast(1)
        val clampedPage = currentPage.coerceIn(1, total)
        val prog = if (total <= 1) 1f else (clampedPage.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        mainHandler.post {
            onPageProgressChanged(clampedPage, prog)
        }
    }
}

