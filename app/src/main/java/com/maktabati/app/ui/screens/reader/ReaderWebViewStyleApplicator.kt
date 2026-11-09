package com.maktabati.app.ui.screens.reader

import android.webkit.WebView
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import com.maktabati.app.data.typography.UserTypographyPreferences
import com.maktabati.app.ui.reader.ReaderThemeMode

internal object ReaderWebViewStyleApplicator {

    const val VIRTUAL_BOOK_HOST = "maktabati.app"
    const val VIRTUAL_BOOK_BASE = "https://maktabati.app/books/"

    fun resolveMimeType(fileName: String): String {
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

    fun isTextMimeType(mime: String): Boolean {
        return mime.startsWith("text/") ||
            mime == "application/javascript" ||
            mime == "application/json" ||
            mime == "image/svg+xml"
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
            val bgSecondary = when (theme) {
                ReaderThemeMode.SEPIA -> "#F3E3C3"
                ReaderThemeMode.NIGHT -> "#1C1D24"
                ReaderThemeMode.LIGHT -> "#F8FAFC"
                else -> theme.bgHex
            }
            val cardSurface = when (theme) {
                ReaderThemeMode.SEPIA -> "rgba(185, 138, 68, 0.11)"
                ReaderThemeMode.NIGHT -> "rgba(255, 255, 255, 0.055)"
                ReaderThemeMode.LIGHT -> "rgba(15, 23, 42, 0.035)"
                else -> "transparent"
            }
            val cardBorder = when (theme) {
                ReaderThemeMode.SEPIA -> "rgba(180, 128, 58, 0.36)"
                ReaderThemeMode.NIGHT -> "rgba(218, 165, 92, 0.32)"
                ReaderThemeMode.LIGHT -> "rgba(148, 163, 184, 0.38)"
                else -> "rgba(180, 128, 58, 0.30)"
            }
            val js = """
                (function() {
                    let el = document.getElementById('maktabati-theme-override');
                    if (!el) {
                        el = document.createElement('style');
                        el.id = 'maktabati-theme-override';
                        document.head.appendChild(el);
                    }
                    el.innerHTML = `
                        :root {
                            --bg-primary: ${theme.bgHex} !important;
                            --bg-secondary: $bgSecondary !important;
                            --paper-bg: ${theme.bgHex} !important;
                            --bg-card: $bgSecondary !important;
                            --bg-soft-rose: $cardSurface !important;
                            --bg-soft-sage: $cardSurface !important;
                            --bg-pearl-gold: $cardSurface !important;
                            --text-primary: ${theme.textHex} !important;
                            --ink-main: ${theme.textHex} !important;
                            --text-secondary: ${theme.textHex}CC !important;
                            --paper-edge: $cardBorder !important;
                            --line: $cardBorder !important;
                        }
                        html, body, .app, .book-stage, .page-canvas, .page, .book-page {
                            background: ${theme.bgHex} !important;
                            background-color: ${theme.bgHex} !important;
                            color: ${theme.textHex} !important;
                        }
                        .top-bar, .bottom-nav-dock, .panel, .term-popover, .toc-item, .search-result, #searchInput {
                            background: $bgSecondary !important;
                            background-color: $bgSecondary !important;
                            color: ${theme.textHex} !important;
                            border-color: $cardBorder !important;
                        }
                        .brand, .panel-head h2, .panel-head h3, .term-popover-title, .page h1, .page h2, .page h3, .cover-page h1 {
                            color: ${theme.textHex} !important;
                        }
                        .reading p, .page-body p, .page-content p, li, td, th {
                            color: ${theme.textHex} !important;
                        }
                        .quran-box, .ayah-box, .hadith-box, .wisdom-pearl, .heart-keepsake-card,
                        .reflection-card, .did-you-know-card, .epigraph-box, .contrast-box, .poetry-stanza,
                        .quote-card, .quote-box, .interactive-box, .step-card, .cover-page, .cover-title-frame {
                            background: $cardSurface !important;
                            background-color: $cardSurface !important;
                            border-color: $cardBorder !important;
                            color: ${theme.textHex} !important;
                        }
                        .quran-box p, .ayah-box p, .hadith-box p, .wisdom-pearl p,
                        .heart-keepsake-card p, .reflection-card p, .did-you-know-card p,
                        .epigraph-box p, .contrast-box p, .poetry-stanza div, .quote-card p, .quote-box p, .term-popover-body {
                            color: ${theme.textHex} !important;
                        }
                    `;
                })();
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        }
    }

    fun applyFluidZoomToWebView(wv: WebView, zoomPct: Int) {
        if (wv.settings.textZoom != 100) {
            wv.settings.textZoom = 100
        }
        wv.evaluateJavascript(
            "if (typeof window.__maktabatiSetFluidZoom === 'function') window.__maktabatiSetFluidZoom($zoomPct, true);",
            null
        )
    }

    fun applyTypographyPreferencesToWebView(wv: WebView, typoPrefs: UserTypographyPreferences) {
        val js = MaktabatiFontRegistry.buildLiveTypographyOverrideJs(typoPrefs)
        wv.evaluateJavascript(js, null)
    }
}
