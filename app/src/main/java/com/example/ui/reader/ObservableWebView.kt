package com.example.ui.reader

import android.content.Context
import android.webkit.WebView

class ObservableWebView(context: Context) : WebView(context) {
    var onScrollListener: ((scrollY: Int, maxScrollY: Int) -> Unit)? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (l != 0) {
            scrollTo(0, t)
        }
        super.onScrollChanged(0, t, 0, oldt)
        val maxScroll = (computeVerticalScrollRange() - height).coerceAtLeast(1)
        onScrollListener?.invoke(t, maxScroll)
    }

    override fun canScrollHorizontally(direction: Int): Boolean = false

    override fun zoomIn(): Boolean = false

    override fun zoomOut(): Boolean = false

    override fun zoomBy(zoomFactor: Float) {
        // Prevent native viewport image-like zoom; zooming is handled via Fluid Reflow Content Zoom
    }

    fun getMaxScroll(): Int {
        return (computeVerticalScrollRange() - height).coerceAtLeast(1)
    }
}
