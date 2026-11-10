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
                    document.documentElement.setAttribute('data-theme', 'original');
                    if (typeof window.__maktabatiSetThemeMode === 'function') {
                        try { window.__maktabatiSetThemeMode('original'); } catch (e) {}
                    }
                    const el = document.getElementById('maktabati-theme-override');
                    if (el) el.remove();
                })();
            """.trimIndent()
            wv.evaluateJavascript(js, null)
        } else {
            val modeKey = when (theme) {
                ReaderThemeMode.NIGHT -> "night"
                ReaderThemeMode.SEPIA -> "sepia"
                ReaderThemeMode.LIGHT -> "light"
                else -> "original"
            }
            val bgSecondary = when (theme) {
                ReaderThemeMode.SEPIA -> "#F3E3C3"
                ReaderThemeMode.NIGHT -> "#171B24"
                ReaderThemeMode.LIGHT -> "#F8FAFC"
                else -> theme.bgHex
            }
            val cardSurface = when (theme) {
                ReaderThemeMode.SEPIA -> "rgba(185, 138, 68, 0.11)"
                ReaderThemeMode.NIGHT -> "#1D222E"
                ReaderThemeMode.LIGHT -> "rgba(15, 23, 42, 0.035)"
                else -> "transparent"
            }
            val cardBorder = when (theme) {
                ReaderThemeMode.SEPIA -> "rgba(180, 128, 58, 0.36)"
                ReaderThemeMode.NIGHT -> "rgba(243, 198, 99, 0.32)"
                ReaderThemeMode.LIGHT -> "rgba(148, 163, 184, 0.38)"
                else -> "rgba(180, 128, 58, 0.30)"
            }
            val nightSpecificCss = if (theme == ReaderThemeMode.NIGHT) {
                """
                :root, html[data-theme="night"] {
                    --bg-primary: ${theme.bgHex} !important;
                    --bg-secondary: $bgSecondary !important;
                    --paper-bg: $bgSecondary !important;
                    --bg-card: $cardSurface !important;
                    --bg-soft-rose: #18232C !important;
                    --bg-soft-sage: #211F1B !important;
                    --bg-pearl-gold: #241F14 !important;
                    --accent-rose: #34D399 !important;
                    --accent-rose-dark: #6EE7B7 !important;
                    --accent-rose-light: rgba(52, 211, 153, 0.42) !important;
                    --accent-sage: #F59E0B !important;
                    --accent-sage-dark: #FDE68A !important;
                    --accent-gold: #F3C663 !important;
                    --accent-gold-soft: rgba(243, 198, 99, 0.34) !important;
                    --text-primary: ${theme.textHex} !important;
                    --ink-main: ${theme.textHex} !important;
                    --text-secondary: #A9B4C2 !important;
                    --paper-edge: $cardBorder !important;
                    --border-ornate: rgba(243, 198, 99, 0.44) !important;
                    --line: rgba(243, 198, 99, 0.20) !important;
                    --cover-center-glow: #202838 !important;
                }
                .cover-page {
                    background: radial-gradient(ellipse at 50% 36%, #202838 0%, #151A24 58%, #0E1219 100%) !important;
                    border-color: rgba(243, 198, 99, 0.48) !important;
                    box-shadow: inset 0 0 0 6px #151A24, inset 0 0 0 7.5px rgba(243, 198, 99, 0.28) !important;
                }
                .cover-title-frame {
                    background: linear-gradient(180deg, rgba(26, 33, 48, 0.95), rgba(37, 32, 21, 0.92)) !important;
                    border-top-color: #F3C663 !important;
                    border-bottom-color: #F3C663 !important;
                    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.45) !important;
                }
                .cover-page h1 {
                    color: #F8FAFC !important;
                }
                .cover-page .subtitle {
                    color: #CBD5E1 !important;
                }
                .cover-page .author, .heart-badge, .letter-envelope-header, .author-signature {
                    background: #1E2534 !important;
                    color: #F3C663 !important;
                    border-color: rgba(243, 198, 99, 0.42) !important;
                }
                .letter-envelope-title {
                    color: #F3C663 !important;
                }
                .cover-art-svg [fill="#FFFFFF"], .cover-art-svg [fill="#ffffff"],
                .cover-art-svg [fill="#FFF"], .cover-art-svg [fill="#fff"],
                .cover-art-svg [fill="#FAF6EE"], .cover-art-svg [fill="#FFFDF7"],
                .cover-art-svg [fill="#F8F4E8"], .page svg [fill="#FFFFFF"], .page svg [fill="#ffffff"] {
                    fill: #1A2130 !important;
                }
                .cover-art-svg [fill="#044332"], .cover-art-svg [fill="#065F46"], .cover-art-svg [fill="#732E41"] {
                    fill: #F3C663 !important;
                }
                .cover-art-svg [stroke="#044332"], .cover-art-svg [stroke="#065F46"], .cover-art-svg [stroke="#732E41"] {
                    stroke: #6EE7B7 !important;
                }
                .running-header {
                    background: #131822 !important;
                    color: #E2E8F0 !important;
                    border-bottom-color: #F3C663 !important;
                }
                .running-header-mark {
                    color: #F3C663 !important;
                }
                .nav-btn, .primary-btn, .did-you-know-badge, .step-num, .brand-gem {
                    background: linear-gradient(135deg, #065F46, #044332) !important;
                    color: #F8FAFC !important;
                    border-color: rgba(243, 198, 99, 0.45) !important;
                }
                .icon-btn {
                    background: #1E2432 !important;
                    color: #F3C663 !important;
                    border-color: rgba(243, 198, 99, 0.30) !important;
                }
                .term-btn {
                    background: rgba(243, 198, 99, 0.14) !important;
                    color: #F3C663 !important;
                    border-bottom-color: #F3C663 !important;
                }
                .term-btn:active, .term-btn.active-term {
                    background: #F3C663 !important;
                    color: #0F1218 !important;
                }
                .book-table-wrap th {
                    background: #231F15 !important;
                    color: #F3C663 !important;
                }
                """.trimIndent()
            } else {
                ""
            }
            val js = """
                (function() {
                    document.documentElement.setAttribute('data-theme', '$modeKey');
                    if (typeof window.__maktabatiSetThemeMode === 'function') {
                        try { window.__maktabatiSetThemeMode('$modeKey'); } catch (e) {}
                    }
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
                        .quote-card, .quote-box, .interactive-box, .step-card, .cover-page, .cover-title-frame, .book-table-wrap {
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
                        $nightSpecificCss
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
