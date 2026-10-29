package com.example.ui.reader

import android.content.Context
import android.webkit.WebView

class ObservableWebView(context: android.content.Context) : WebView(context) {
    var onScrollListener: ((scrollY: Int, maxScrollY: Int) -> Unit)? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val maxScroll = (computeVerticalScrollRange() - height).coerceAtLeast(1)
        onScrollListener?.invoke(t, maxScroll)
    }

    fun getMaxScroll(): Int {
        return (computeVerticalScrollRange() - height).coerceAtLeast(1)
    }
}

