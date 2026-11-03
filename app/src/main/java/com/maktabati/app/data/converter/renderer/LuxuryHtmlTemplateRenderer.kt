package com.maktabati.app.data.converter.renderer

import com.maktabati.app.data.converter.model.ConvertedPageItem
import com.maktabati.app.data.converter.model.PublishingLayoutStyle
import com.maktabati.app.data.converter.paginator.SemanticBookPaginator.escapeHtml
import com.maktabati.app.data.converter.theme.BookVisualTheme
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import org.json.JSONArray
import org.json.JSONObject

/**
 * Renders self-contained luxury HTML5 interactive books including SVG emblems,
 * layout variant CSS, full-book search, TOC drawer, bookmarks, and interactive journals.
 */
object LuxuryHtmlTemplateRenderer {

    fun buildCoverPageHtml(
        title: String,
        subtitle: String,
        author: String,
        theme: BookVisualTheme,
        isRtl: Boolean
    ): String {
        val svgArt = buildThemeSvgEmblem(theme)
        val startBtnLabel = if (isRtl) "افتح صفحات الكتاب ✦" else "Open Book ✦"
        val subtitleHtml = if (subtitle.isNotBlank()) {
            """<div class="subtitle">${escapeHtml(subtitle)}</div>"""
        } else {
            ""
        }
        val authorHtml = if (author.isNotBlank()) {
            """<div class="author">${escapeHtml(author)}</div>"""
        } else {
            ""
        }

        return """
            <div class="cover-page">
              $svgArt
              <h1>${escapeHtml(title)}</h1>
              $subtitleHtml
              <div class="ornament">❖ ❖ ❖</div>
              $authorHtml
              <button class="primary-btn" onclick="window.goToPage(1)">$startBtnLabel</button>
            </div>
        """.trimIndent()
    }

    fun buildThemeSvgEmblem(theme: BookVisualTheme): String {
        return when (theme.layoutStyle) {
            PublishingLayoutStyle.SOFT_BOTANICAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8" stroke-dasharray="4 3"/>
                  <circle cx="120" cy="120" r="95" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                  <path d="M60 140 C 50 110, 70 75, 120 60 C 170 75, 190 110, 180 140" stroke="${theme.accentSecondaryHex}" stroke-width="2.2" stroke-linecap="round"/>
                  <path d="M80 160 C 90 185, 150 185, 160 160" stroke="${theme.accentGoldHex}" stroke-width="1.8" stroke-linecap="round"/>
                  <circle cx="95" cy="176" r="3.5" fill="${theme.accentGoldHex}"/>
                  <circle cx="120" cy="182" r="4.5" fill="${theme.accentGoldHex}"/>
                  <circle cx="145" cy="176" r="3.5" fill="${theme.accentGoldHex}"/>
                  <g transform="translate(120, 122)">
                    <path d="M0 -34 C 18 -34, 30 -16, 26 4 C 20 22, -20 22, -26 4 C -30 -16, -18 -34, 0 -34 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                    <path d="M-18 -12 C -24 4, -12 24, 0 28 C 12 24, 24 4, 18 -12 C 12 -24, -12 -24, -18 -12 Z" fill="${theme.accentPrimaryLightHex}" fill-opacity="0.45"/>
                    <circle cx="0" cy="4" r="11" fill="${theme.accentPrimaryHex}"/>
                    <circle cx="0" cy="4" r="5.5" fill="${theme.accentPrimaryDarkHex}"/>
                  </g>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.HERITAGE_ORNATE -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <circle cx="120" cy="120" r="92" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.6"/>
                  <rect x="74" y="74" width="92" height="92" rx="8" stroke="${theme.accentGoldHex}" stroke-width="1.8" fill="${theme.bgSoftAccentHex}"/>
                  <rect x="74" y="74" width="92" height="92" rx="8" transform="rotate(45 120 120)" stroke="${theme.accentPrimaryHex}" stroke-width="1.8" fill="${theme.bgSecondaryHex}" fill-opacity="0.8"/>
                  <circle cx="120" cy="120" r="24" fill="${theme.accentPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="9" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.DAWAH_MOTIVATIONAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.8" stroke-dasharray="6 4"/>
                  <circle cx="120" cy="120" r="92" fill="${theme.bgSecondaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.5"/>
                  <polygon points="120,48 138,102 192,120 138,138 120,192 102,138 48,120 102,102" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="18" fill="${theme.accentPrimaryHex}"/>
                  <circle cx="120" cy="120" r="7" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.CLASSIC_BOOK_PRESS, PublishingLayoutStyle.NARRATIVE_STORY -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <rect x="28" y="28" width="184" height="184" rx="22" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <rect x="40" y="40" width="160" height="160" rx="16" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.4" stroke-dasharray="4 3"/>
                  <path d="M74 92 C 92 84, 112 88, 120 96 C 128 88, 148 84, 166 92 V 154 C 148 146, 128 150, 120 158 C 112 150, 92 146, 74 154 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="1.8"/>
                  <line x1="120" y1="96" x2="120" y2="158" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="70" r="5.5" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()

            PublishingLayoutStyle.ACADEMIC_STRUCTURED, PublishingLayoutStyle.EDITORIAL_MAGAZINE, PublishingLayoutStyle.PURE_MINIMAL -> """
                <svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="${theme.titleAr}">
                  <circle cx="120" cy="120" r="108" fill="${theme.bgPrimaryHex}" stroke="${theme.accentGoldHex}" stroke-width="1.8"/>
                  <circle cx="120" cy="120" r="94" fill="${theme.bgSecondaryHex}" stroke="${theme.accentPrimaryHex}" stroke-width="1.5"/>
                  <path d="M68 86 C 88 78, 112 82, 120 92 C 128 82, 152 78, 172 86 V 158 C 152 150, 128 154, 120 164 C 112 154, 88 150, 68 158 Z" fill="${theme.bgSoftAccentHex}" stroke="${theme.accentPrimaryDarkHex}" stroke-width="2"/>
                  <line x1="120" y1="92" x2="120" y2="164" stroke="${theme.accentGoldHex}" stroke-width="2"/>
                  <circle cx="120" cy="66" r="6" fill="${theme.accentGoldHex}"/>
                </svg>
            """.trimIndent()
        }
    }

    fun renderCompleteLuxuryHtml(
        bookTitle: String,
        bookSubtitle: String,
        bookAuthor: String,
        theme: BookVisualTheme,
        isRtl: Boolean,
        language: String,
        pages: List<ConvertedPageItem>
    ): String {
        val dirAttr = if (isRtl) "rtl" else "ltr"
        val pagesJsonArray = JSONArray()
        for (p in pages) {
            val obj = JSONObject()
            obj.put("tag", p.tag)
            obj.put("title", p.title)
            obj.put("type", p.type)
            obj.put("html", p.html)
            pagesJsonArray.put(obj)
        }

        val storageSlug = bookTitle.hashCode().toUInt().toString(16)
        val prevLabel = if (isRtl) "السابق" else "Previous"
        val nextLabel = if (isRtl) "التالي" else "Next"
        val searchHeaderTitle = if (isRtl) "البحث في الكتاب ${theme.badgeEmoji}" else "Search Book ${theme.badgeEmoji}"
        val searchPlaceholder = if (isRtl) "ابحث عن آية، حديث، عنوان، أو أي كلمة..." else "Search any word or phrase..."
        val searchEmptyPrompt = if (isRtl) "اكتب كلمة للبحث في جميع صفحات الكتاب..." else "Type a word to search across all pages..."
        val tocHeaderTitle = if (isRtl) "فهرس فصول الكتاب" else "Table of Contents"

        return """<!DOCTYPE html>
<html lang="$language" dir="$dirAttr">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover"/>
  <meta name="theme-color" content="${theme.bgPrimaryHex}"/>
  <meta name="color-scheme" content="light"/>
  <meta name="author" content="${escapeHtml(bookAuthor)}"/>
  <meta name="description" content="${escapeHtml(bookSubtitle)}"/>
  <title>${escapeHtml(bookTitle)}</title>

  <style>
:root {
  --bg-primary: ${theme.bgPrimaryHex};
  --bg-secondary: ${theme.bgSecondaryHex};
  --paper-bg: ${theme.bgSecondaryHex};
  --bg-soft-rose: ${theme.bgSoftAccentHex};
  --bg-soft-sage: ${theme.bgSoftSecondaryHex};
  --bg-pearl-gold: ${theme.bgPearlGoldHex};

  --accent-rose: ${theme.accentPrimaryHex};
  --accent-rose-dark: ${theme.accentPrimaryDarkHex};
  --accent-rose-light: ${theme.accentPrimaryLightHex};
  --accent-sage: ${theme.accentSecondaryHex};
  --accent-sage-dark: ${theme.accentSecondaryDarkHex};
  --accent-gold: ${theme.accentGoldHex};
  --accent-gold-soft: rgba(198,154,78,0.24);

  --text-primary: ${theme.textPrimaryHex};
  --ink-main: ${theme.textPrimaryHex};
  --text-secondary: ${theme.textSecondaryHex};
  --line: rgba(180,140,110,0.20);
  --border-ornate: rgba(198,154,78,0.34);
  --paper-edge: ${theme.paperEdgeHex};

  --shadow-card: 0 16px 42px rgba(40,25,30,0.07);
  --reader-font: 21px;
  --reader-font-size: 100%;
  --font-scale: 1;

  --font-body: "${theme.fontBody}", "Noto Naskh Arabic", "Amiri", serif;
  --font-headings: "${theme.fontHeadings}", "Aref Ruqaa", "Amiri", serif;
  --font-chapter-titles: "${theme.fontChapterTitles}", "Reem Kufi", "Noto Kufi Arabic", sans-serif;
  --font-quotes: "${theme.fontQuotes}", "Amiri", "Noto Naskh Arabic", serif;
  --font-quran: "${theme.fontQuran}", "Amiri Quran", "Amiri", serif;
  --font-interface: "Noto Sans Arabic", "Cairo", "IBM Plex Sans Arabic", sans-serif;
  --font-numbers: "Noto Sans Arabic", "Cairo", sans-serif;
  --reader-family: var(--font-body);

  color-scheme: light;
}

${MaktabatiFontRegistry.buildOfflineFontFacesCss("https://maktabati.app/assets")}

.hadith-quote {
  font-family: var(--font-quotes);
  color: var(--accent-sage-dark);
}
${buildLayoutVariantCss(theme)}

* {
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
  max-width: 100%;
  overflow-wrap: anywhere;
}

html, body, .app {
  touch-action: pan-y !important;
}

button, input, select, textarea {
  touch-action: manipulation;
  font: inherit;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-body);
}

body {
  overflow: hidden;
}

.app {
  height: 100vh;
  max-height: 100vh;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(155deg, var(--bg-primary), var(--bg-soft-rose) 100%);
}

.top-bar {
  height: 56px;
  min-height: 56px;
  flex: 0 0 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(12px, 3vw, 20px);
  gap: 10px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--line);
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brand-gem {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  font-size: 16px;
}

.tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
}

.icon-btn, .nav-btn {
  border: 1px solid var(--line);
  background: var(--bg-primary);
  color: var(--accent-rose-dark);
  border-radius: 12px;
  min-width: 40px;
  min-height: 40px;
  padding: 6px 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-interface);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.icon-btn svg {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: none;
  stroke-width: 2.1;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.icon-btn.bookmarked {
  background: var(--bg-pearl-gold);
  color: var(--accent-gold);
  border-color: var(--accent-gold);
}

.running-header {
  height: 34px;
  min-height: 34px;
  flex: 0 0 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 2px 14px;
  background: var(--accent-rose-dark);
  color: #FAF4EF;
  border-bottom: 2px solid var(--accent-gold);
  font-family: var(--font-interface);
  font-size: 11px;
  font-weight: 700;
  text-align: center;
}

.running-header[hidden] {
  display: none;
}

.running-header-mark {
  color: #FDE68A;
}

.book-stage {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: clamp(10px, 2.5vw, 20px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.page-canvas {
  width: min(100%, 720px);
  min-height: 100%;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  padding: clamp(18px, 4vw, 34px);
  box-shadow: var(--shadow-card);
  position: relative;
}

.page {
  font-family: var(--font-body);
  font-size: calc(var(--reader-font, 21px));
  line-height: 2.02;
  color: var(--text-primary);
}

.page p {
  margin: 0 0 14px;
  text-align: justify;
  line-height: 2.02;
}

.page h1 {
  font-family: var(--font-headings);
  font-size: clamp(1.35em, 4.2vw, 1.78em);
  line-height: 1.65;
  color: var(--accent-rose-dark);
  margin: 0 0 12px;
}

.page h2 {
  font-family: var(--font-headings);
  font-size: clamp(1.15em, 3.6vw, 1.42em);
  line-height: 1.65;
  color: var(--accent-rose-dark);
  margin: 14px 0 10px;
}

.cover-page {
  min-height: clamp(420px, 68vh, 720px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 20px 10px;
}

.cover-page .eyebrow {
  font-family: var(--font-interface);
  font-size: 0.62em;
  font-weight: 700;
  color: var(--accent-gold);
  margin-bottom: 12px;
}

.cover-art-svg {
  width: clamp(140px, 38vw, 195px);
  height: auto;
  margin: 6px auto 14px;
}

.cover-page h1 {
  font-family: var(--font-headings);
  font-size: clamp(1.55em, 5vw, 2.2em);
  color: var(--accent-rose-dark);
  margin: 6px auto 10px;
}

.cover-page .subtitle {
  font-family: var(--font-body);
  font-size: 0.92em;
  color: var(--text-secondary);
  max-width: 90%;
  margin: 0 auto 12px;
}

.ornament {
  color: var(--accent-gold);
  font-size: 0.9em;
  letter-spacing: 6px;
  margin: 8px 0;
}

.cover-page .author {
  font-family: var(--font-interface);
  font-size: 0.78em;
  font-weight: 700;
  color: var(--accent-rose-dark);
  margin-bottom: 18px;
}

.primary-btn {
  border: 1px solid var(--accent-gold);
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  color: #fff;
  border-radius: 99px;
  padding: 10px 24px;
  font-family: var(--font-interface);
  font-size: 0.78em;
  font-weight: 700;
  cursor: pointer;
}

.quran-box {
  margin: 16px 0;
  padding: 16px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-pearl-gold), var(--bg-secondary));
  border: 1.5px solid var(--accent-gold-soft);
  border-inline-start: 4px solid var(--accent-gold);
  text-align: center;
}

.quran-box p, .quran-verse {
  font-family: var(--font-quran);
  font-size: 1.14em;
  line-height: 2.15;
  color: var(--accent-rose-dark);
  margin: 0 0 4px;
  text-align: center;
}

.quran-box .ayah-source {
  font-family: var(--font-interface);
  font-size: 0.68em;
  color: var(--accent-gold);
  font-weight: 700;
}

.hadith-box {
  margin: 16px 0;
  padding: 15px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-sage), var(--bg-secondary));
  border: 1px solid var(--line);
  border-inline-start: 4px solid var(--accent-sage);
  text-align: center;
}

.hadith-box p {
  font-family: var(--font-quotes);
  font-size: 1.08em;
  line-height: 2.05;
  color: var(--accent-sage-dark);
  margin: 0;
  text-align: center;
}

.wisdom-pearl {
  margin: 16px 0;
  padding: 14px 18px;
  border-radius: 12px;
  background: var(--bg-pearl-gold);
  border: 1px solid var(--accent-gold-soft);
  border-inline-start: 3px solid var(--accent-gold);
  font-family: var(--font-quotes);
  font-size: 0.98em;
  line-height: 1.95;
}

.wisdom-badge {
  display: block;
  font-family: var(--font-interface);
  font-size: 0.65em;
  font-weight: 700;
  color: var(--accent-gold);
  margin-bottom: 5px;
}

.heart-keepsake-card {
  background: linear-gradient(145deg, var(--bg-secondary), var(--bg-soft-rose) 65%, var(--bg-pearl-gold) 100%);
  border: 1.5px solid var(--accent-rose-light);
  border-radius: 16px;
  padding: 18px;
  margin: 16px 0;
  text-align: center;
}

.heart-badge {
  display: inline-block;
  font-family: var(--font-interface);
  font-size: 0.68em;
  font-weight: 700;
  color: var(--accent-rose-dark);
  background: var(--bg-secondary);
  border: 1px solid var(--accent-rose-light);
  border-radius: 99px;
  padding: 3px 12px;
  margin-bottom: 10px;
}

.heart-keepsake-card p {
  font-family: var(--font-quotes);
  font-size: 1.12em;
  line-height: 2.1;
  color: var(--accent-rose-dark);
  margin: 0;
  text-align: center;
}

.letter-envelope-header {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-pearl-gold));
  border: 1px solid var(--accent-gold-soft);
  border-radius: 99px;
  padding: 5px 14px;
  margin-bottom: 12px;
}

.letter-envelope-title {
  font-family: var(--font-interface);
  font-size: 0.68em;
  font-weight: 700;
  color: var(--accent-rose-dark);
}

.letter-greeting {
  font-family: var(--font-headings);
  font-size: 1.08em;
  color: var(--accent-rose-dark);
  margin-bottom: 12px;
  font-weight: 700;
  border-bottom: 1px dashed var(--line);
  padding-bottom: 6px;
}

.step-card {
  margin: 10px 0;
  padding: 11px 14px;
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 10px;
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.step-num {
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  border-radius: 50%;
  background: var(--accent-rose);
  color: #fff;
  display: grid;
  place-items: center;
  font-family: var(--font-numbers);
  font-weight: 700;
  font-size: 0.75em;
}

.step-content {
  flex: 1;
}

.interactive-box {
  margin: 16px 0;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--bg-soft-rose);
  border: 1.5px solid var(--line);
}

.field-input, .field-textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1.5px solid var(--line);
  border-radius: 10px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 0.8em;
  outline: none;
}

.field-textarea {
  min-height: 76px;
  resize: vertical;
}

.save-indicator {
  font-family: var(--font-interface);
  font-size: 0.66em;
  color: var(--accent-sage-dark);
  font-weight: 700;
  margin-top: 5px;
}

.bottom-nav-dock {
  min-height: 62px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 14px calc(env(safe-area-inset-bottom, 6px) + 6px);
  background: var(--bg-secondary);
  border-top: 1px solid var(--line);
}

.nav-btn {
  min-width: 90px;
  border-radius: 12px;
  background: var(--accent-rose-dark);
  color: #fff;
  border-color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 12px;
  padding: 8px 14px;
}

.nav-btn:disabled {
  opacity: 0.35;
}

.page-counter {
  font-family: var(--font-numbers);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 99px;
  padding: 5px 14px;
  direction: ltr;
  unicode-bidi: isolate;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 45;
  background: rgba(30,20,25,0.45);
  display: none;
  align-items: flex-end;
  justify-content: center;
  padding: 12px;
}

.overlay.open {
  display: flex;
}

.panel {
  width: min(100%, 640px);
  max-height: 78vh;
  overflow-y: auto;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  padding: 16px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
}

.panel-head h2 {
  margin: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 15px;
}

.search-bar-wrap {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

#searchInput {
  width: 100%;
  min-height: 44px;
  padding: 9px 14px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 14px;
}

.search-clear-btn {
  border-radius: 8px;
  border: 1px solid var(--line);
  background: var(--bg-secondary);
  color: var(--accent-rose);
  font-family: var(--font-interface);
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  cursor: pointer;
}

.search-clear-btn[hidden] {
  display: none;
}

.search-status {
  font-family: var(--font-interface);
  font-size: 12px;
  color: var(--text-secondary);
  margin: 4px 2px 10px;
}

.search-result, .toc-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  text-align: start;
  padding: 10px 14px;
  margin: 6px 0;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 13px;
  cursor: pointer;
}

.search-result {
  display: block;
}

.search-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.toc-item.toc-part {
  font-family: var(--font-chapter-titles);
  background: var(--bg-soft-rose);
  font-weight: 700;
  color: var(--accent-rose-dark);
}

.toc-item small, .search-result-page {
  color: var(--accent-gold);
  font-family: var(--font-numbers);
  font-size: 11px;
  font-weight: 700;
  background: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid var(--accent-gold-soft);
}

mark.search-hit-mark, mark.page-search-highlight {
  background: rgba(198,154,78,0.25);
  color: var(--accent-rose-dark);
  padding: 1px 5px;
  border-radius: 5px;
  font-weight: 700;
}
  </style>
</head>
<body>
  <div class="app">
    <header class="top-bar">
      <div class="brand">
        <div class="brand-gem" aria-hidden="true">${theme.badgeEmoji}</div>
        <span>${escapeHtml(bookTitle)}</span>
      </div>
      <div class="tools">
        <button id="searchOpen" class="icon-btn" aria-label="بحث في الكتاب" title="بحث في الكتاب">
          <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </button>
        <button id="tocBtn" class="icon-btn" aria-label="فهرس الكتاب" title="فهرس الكتاب">
          <svg viewBox="0 0 24 24"><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="14" y2="12"/><line x1="4" y1="18" x2="18" y2="18"/></svg>
        </button>
        <button id="bookmarkBtn" class="icon-btn" aria-label="حفظ علامة مرجعية" title="حفظ علامة مرجعية">
          <svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
        </button>
      </div>
    </header>

    <div id="runningHeader" class="running-header" hidden>
      <span id="runningHeaderTag" class="running-header-mark">${escapeHtml(bookTitle)}</span>
    </div>

    <main id="bookStage" class="book-stage">
      <div class="page-canvas">
        <article id="activePageArticle" class="page"></article>
      </div>
    </main>

    <footer class="bottom-nav-dock">
      <button id="btnPrevPage" class="nav-btn" onclick="window.prevPage()">$prevLabel</button>
      <span id="pageCounter" class="page-counter" dir="ltr">1 / ${pages.size}</span>
      <button id="btnNextPage" class="nav-btn" onclick="window.nextPage()">$nextLabel</button>
    </footer>
  </div>

  <div id="searchPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>$searchHeaderTitle</h2>
        <button id="searchClose" class="icon-btn" aria-label="إغلاق البحث">✕</button>
      </div>
      <div class="search-bar-wrap">
        <input id="searchInput" type="text" placeholder="$searchPlaceholder" autocomplete="off"/>
        <button id="searchClearBtn" class="search-clear-btn" hidden aria-label="مسح">مسح</button>
      </div>
      <div class="search-status">
        <span id="searchStatus">$searchEmptyPrompt</span>
      </div>
      <div id="searchResults" class="search-results-list"></div>
    </div>
  </div>

  <div id="tocPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>$tocHeaderTitle</h2>
        <button id="tocClose" class="icon-btn" aria-label="إغلاق الفهرس">✕</button>
      </div>
      <div id="tocPanelList" class="toc-list"></div>
    </div>
  </div>

  <script>
    const STORAGE_PREFIX = "maktabati_conv_${storageSlug}_";
    const BOOK_DATA = {
      title: ${JSONObject.quote(bookTitle)},
      subtitle: ${JSONObject.quote(bookSubtitle)},
      author: ${JSONObject.quote(bookAuthor)},
      totalPages: ${pages.size},
      pages: ${pagesJsonArray}
    };

    let currentPageIndex = 0;
    window.currentPageIndex = 0;
    window.currentPage = 0;
    let savedBookmarks = [];
    try {
      const rawBm = localStorage.getItem(STORAGE_PREFIX + "bookmarks");
      if (rawBm) savedBookmarks = JSON.parse(rawBm) || [];
    } catch (e) {}

    const activePageArticle = document.getElementById("activePageArticle");
    const pageCounter = document.getElementById("pageCounter");
    const btnPrevPage = document.getElementById("btnPrevPage");
    const btnNextPage = document.getElementById("btnNextPage");
    const runningHeader = document.getElementById("runningHeader");
    const runningHeaderTag = document.getElementById("runningHeaderTag");
    const bookmarkBtn = document.getElementById("bookmarkBtn");
    const bookStage = document.getElementById("bookStage");

    const searchPanel = document.getElementById("searchPanel");
    const searchOpen = document.getElementById("searchOpen");
    const searchClose = document.getElementById("searchClose");
    const searchInput = document.getElementById("searchInput");
    const searchClearBtn = document.getElementById("searchClearBtn");
    const searchStatus = document.getElementById("searchStatus");
    const searchResults = document.getElementById("searchResults");

    const tocPanel = document.getElementById("tocPanel");
    const tocBtn = document.getElementById("tocBtn");
    const tocClose = document.getElementById("tocClose");
    const tocPanelList = document.getElementById("tocPanelList");

    function renderPage(index, highlightQuery) {
      if (index < 0) index = 0;
      if (index >= BOOK_DATA.totalPages) index = BOOK_DATA.totalPages - 1;
      currentPageIndex = index;
      window.currentPageIndex = index;
      window.currentPage = index;
      window.__maktabatiLastSavedPage = index;
      window.__maktabatiFallbackPage = index;

      const page = BOOK_DATA.pages[index];
      activePageArticle.innerHTML = page.html;

      pageCounter.textContent = (index + 1) + " / " + BOOK_DATA.totalPages;
      btnPrevPage.disabled = (index === 0);
      btnNextPage.disabled = (index === BOOK_DATA.totalPages - 1);

      if (page.type === "cover") {
        runningHeader.hidden = true;
      } else {
        runningHeader.hidden = false;
        runningHeaderTag.textContent = BOOK_DATA.title;
      }

      if (savedBookmarks.includes(index)) {
        bookmarkBtn.classList.add("bookmarked");
      } else {
        bookmarkBtn.classList.remove("bookmarked");
      }

      bookStage.scrollTop = 0;
      restorePageInputs();

      if (highlightQuery) {
        applyCalmPageSearchHighlight(highlightQuery);
      }

      try {
        localStorage.setItem(STORAGE_PREFIX + "last_page", String(index));
      } catch (e) {}

      if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
        window.MaktabatiBridge.onPageChanged(index + 1, BOOK_DATA.totalPages);
      }
    }

    window.goToPage = function(idx) {
      renderPage(idx);
    };

    window.nextPage = function() {
      const now = Date.now();
      if (window.__maktabatiLastPageTurnAt && (now - window.__maktabatiLastPageTurnAt) < 220) {
        return;
      }
      if (currentPageIndex < BOOK_DATA.totalPages - 1) {
        window.__maktabatiLastPageTurnAt = now;
        renderPage(currentPageIndex + 1);
      }
    };

    window.prevPage = function() {
      const now = Date.now();
      if (window.__maktabatiLastPageTurnAt && (now - window.__maktabatiLastPageTurnAt) < 220) {
        return;
      }
      if (currentPageIndex > 0) {
        window.__maktabatiLastPageTurnAt = now;
        renderPage(currentPageIndex - 1);
      }
    };

    document.addEventListener("touchmove", function(e) {
      if (e.touches && e.touches.length > 1) {
        e.preventDefault();
      }
    }, { passive: false });

    window.__maktabatiSetFluidZoom = function(zoomPct, smooth) {
      const pct = Math.max(75, Math.min(200, Number(zoomPct) || 100));
      const scale = pct / 100;
      const root = document.documentElement;
      root.style.setProperty("--reader-font", Math.round(21 * scale) + "px");
      root.style.setProperty("--reader-font-size", pct + "%");
      root.style.setProperty("--font-scale", String(scale));
      try {
        localStorage.setItem("maktabati_fluid_zoom_pct", String(pct));
      } catch (e) {}
    };

    bookmarkBtn.addEventListener("click", function() {
      const idx = currentPageIndex;
      if (savedBookmarks.includes(idx)) {
        savedBookmarks = savedBookmarks.filter(i => i !== idx);
      } else {
        savedBookmarks.push(idx);
      }
      try {
        localStorage.setItem(STORAGE_PREFIX + "bookmarks", JSON.stringify(savedBookmarks));
      } catch (e) {}
      renderPage(currentPageIndex);
    });

    tocBtn.addEventListener("click", function() {
      buildTocPanel();
      tocPanel.classList.add("open");
    });
    tocClose.addEventListener("click", function() {
      tocPanel.classList.remove("open");
    });
    tocPanel.addEventListener("click", function(e) {
      if (e.target === tocPanel) tocPanel.classList.remove("open");
    });

    function buildTocPanel() {
      let html = "";
      BOOK_DATA.pages.forEach((p, idx) => {
        const isPart = (p.type === "opener");
        const cls = isPart ? "toc-item toc-part" : "toc-item";
        html += `<div class="${'$'}{cls}" onclick="window.goToPage(${'$'}{idx}); tocPanel.classList.remove('open');">
          <span>${'$'}{p.tag ? p.tag + ' • ' : ''}${'$'}{p.title}</span>
          <small>${'$'}{idx + 1}</small>
        </div>`;
      });
      tocPanelList.innerHTML = html;
    }

    searchOpen.addEventListener("click", function() {
      searchPanel.classList.add("open");
      setTimeout(() => searchInput.focus(), 80);
    });
    searchClose.addEventListener("click", function() {
      searchPanel.classList.remove("open");
    });
    searchPanel.addEventListener("click", function(e) {
      if (e.target === searchPanel) searchPanel.classList.remove("open");
    });
    searchClearBtn.addEventListener("click", function() {
      searchInput.value = "";
      searchClearBtn.hidden = true;
      executeBookSearch();
      searchInput.focus();
    });

    function normalizeArabicWithMap(text) {
      if (!text) return { normalized: "", map: [] };
      const tashkeelRegex = /[\u064B-\u065F\u0670\u06D6-\u06EDـ]/;
      let normalized = "";
      const map = [];
      for (let i = 0; i < text.length; i++) {
        const ch = text[i];
        if (tashkeelRegex.test(ch)) continue;
        let c = ch;
        if (c === 'أ' || c === 'إ' || c === 'آ') c = 'ا';
        else if (c === 'ى') c = 'ي';
        else if (c === 'ة') c = 'ه';
        normalized += c.toLowerCase();
        map.push(i);
      }
      return { normalized, map };
    }

    function extractPagePlainText(html) {
      const tmp = document.createElement("div");
      tmp.innerHTML = html;
      return tmp.textContent || tmp.innerText || "";
    }

    searchInput.addEventListener("input", function() {
      searchClearBtn.hidden = (searchInput.value.trim().length === 0);
      executeBookSearch();
    });

    function executeBookSearch() {
      const rawQuery = searchInput.value.trim();
      if (!rawQuery || rawQuery.length < 2) {
        searchStatus.textContent = "$searchEmptyPrompt";
        searchResults.innerHTML = "";
        return;
      }

      const qNorm = normalizeArabicWithMap(rawQuery).normalized;
      const hits = [];

      for (let i = 0; i < BOOK_DATA.pages.length; i++) {
        const page = BOOK_DATA.pages[i];
        const plain = extractPagePlainText(page.html);
        const { normalized, map } = normalizeArabicWithMap(plain);

        let searchPos = 0;
        let matchCount = 0;

        while (true) {
          const matchIdx = normalized.indexOf(qNorm, searchPos);
          if (matchIdx === -1) break;
          matchCount++;

          if (matchCount <= 2) {
            const startOrig = map[matchIdx] || 0;
            const endNorm = matchIdx + qNorm.length - 1;
            const endOrig = (map[endNorm] !== undefined ? map[endNorm] + 1 : startOrig + rawQuery.length);

            const snippetStart = Math.max(0, startOrig - 40);
            const snippetEnd = Math.min(plain.length, endOrig + 40);

            const prefix = (snippetStart > 0 ? "..." : "") + plain.substring(snippetStart, startOrig);
            const matchText = plain.substring(startOrig, endOrig);
            const suffix = plain.substring(endOrig, snippetEnd) + (snippetEnd < plain.length ? "..." : "");

            hits.push({
              pageIndex: i,
              pageNumber: i + 1,
              title: page.title || ("Page " + (i + 1)),
              tag: page.tag || "",
              snippet: prefix + `<mark class="search-hit-mark">${'$'}{matchText}</mark>` + suffix
            });
          }

          searchPos = matchIdx + Math.max(1, qNorm.length);
          if (hits.length >= 40) break;
        }
        if (hits.length >= 40) break;
      }

      if (hits.length === 0) {
        searchStatus.textContent = `لا توجد نتائج مطابقة لـ «${'$'}{rawQuery}»`;
        searchResults.innerHTML = "";
      } else {
        searchStatus.textContent = `تم العثور على ${'$'}{hits.length} نتيجة:`;
        let html = "";
        hits.forEach(hit => {
          html += `<div class="search-result" onclick="selectSearchResult(${'$'}{hit.pageIndex}, '${'$'}{escapeQuery(rawQuery)}')">
            <div class="search-result-head">
              <strong>${'$'}{hit.tag ? hit.tag + ' • ' : ''}${'$'}{hit.title}</strong>
              <span class="search-result-page">${'$'}{hit.pageNumber}</span>
            </div>
            <small>${'$'}{hit.snippet}</small>
          </div>`;
        });
        searchResults.innerHTML = html;
      }
    }

    function escapeQuery(str) {
      return str.replace(/['"\\]/g, "");
    }

    window.selectSearchResult = function(pageIndex, query) {
      searchPanel.classList.remove("open");
      renderPage(pageIndex, query);
    };

    function applyCalmPageSearchHighlight(query) {
      if (!query || query.length < 2) return;
      const { normalized: qNorm } = normalizeArabicWithMap(query);

      function traverse(node) {
        if (node.nodeType === 3) {
          const text = node.nodeValue;
          const { normalized, map } = normalizeArabicWithMap(text);
          const idx = normalized.indexOf(qNorm);
          if (idx !== -1) {
            const startOrig = map[idx];
            const endNorm = idx + qNorm.length - 1;
            const endOrig = map[endNorm] !== undefined ? map[endNorm] + 1 : startOrig + query.length;

            const before = text.substring(0, startOrig);
            const match = text.substring(startOrig, endOrig);
            const after = text.substring(endOrig);

            const span = document.createElement("span");
            if (before) span.appendChild(document.createTextNode(before));

            const mark = document.createElement("mark");
            mark.className = "page-search-highlight";
            mark.textContent = match;
            span.appendChild(mark);

            if (after) span.appendChild(document.createTextNode(after));

            node.parentNode.replaceChild(span, node);
            mark.scrollIntoView({ behavior: "smooth", block: "center" });
            return true;
          }
        } else if (node.nodeType === 1 && node.childNodes && !["SCRIPT", "STYLE", "BUTTON"].includes(node.tagName)) {
          for (let i = 0; i < node.childNodes.length; i++) {
            if (traverse(node.childNodes[i])) return true;
          }
        }
        return false;
      }

      traverse(activePageArticle);
    }

    window.saveField = function(id, val) {
      try {
        localStorage.setItem(STORAGE_PREFIX + "f_" + id, val);
      } catch (e) {}
    };

    function restorePageInputs() {
      const inputs = activePageArticle.querySelectorAll("input, textarea");
      inputs.forEach(el => {
        if (!el.id) return;
        const val = localStorage.getItem(STORAGE_PREFIX + "f_" + el.id);
        if (val !== null) el.value = val;
      });
    }

    document.addEventListener("DOMContentLoaded", function() {
      try {
        const savedZoom = localStorage.getItem("maktabati_fluid_zoom_pct");
        if (savedZoom) window.__maktabatiSetFluidZoom(savedZoom, false);
      } catch (e) {}

      let startPage = 0;
      try {
        const lp = localStorage.getItem(STORAGE_PREFIX + "last_page");
        if (lp !== null) startPage = parseInt(lp, 10) || 0;
      } catch (e) {}

      renderPage(startPage);
    });
  </script>
</body>
</html>"""
    }

    fun buildLayoutVariantCss(theme: BookVisualTheme): String {
        val commonComponents = """
.poetry-stanza {
  margin: 16px 0;
  padding: 14px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-secondary));
  border: 1px dashed var(--accent-gold);
  text-align: center;
  font-family: var(--font-quotes);
}
.poetry-shatr {
  font-size: 1.06em;
  color: var(--accent-rose-dark);
  line-height: 1.95;
}
.poetry-ornament {
  color: var(--accent-gold);
  font-size: 0.78em;
  margin: 2px 0;
}
.action-callout {
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-pearl-gold));
  border-inline-start: 4px solid var(--accent-rose);
}
.academic-callout {
  background: var(--bg-soft-sage);
  border-inline-start: 4px solid var(--accent-sage-dark);
  border-radius: 8px;
}
        """.trimIndent()

        val styleSpecific = when (theme.layoutStyle) {
            PublishingLayoutStyle.HERITAGE_ORNATE -> """
.page-canvas {
  border: 2px solid var(--accent-gold);
  outline: 1px solid var(--accent-gold-soft);
  outline-offset: -7px;
  box-shadow: inset 0 0 28px rgba(180,123,38,0.06), var(--shadow-card);
}
.page h1 {
  text-align: center;
  border-bottom: 1.5px double var(--accent-gold-soft);
  padding-bottom: 8px;
}
            """.trimIndent()

            PublishingLayoutStyle.CLASSIC_BOOK_PRESS -> """
.page-canvas {
  border-radius: 10px;
  box-shadow: inset 0 0 36px rgba(124,77,43,0.06), 0 12px 30px rgba(30,20,10,0.08);
}
.running-header {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-bottom: 1.5px double var(--paper-edge);
}
.running-header-mark {
  color: var(--accent-rose-dark);
}
            """.trimIndent()

            PublishingLayoutStyle.SOFT_BOTANICAL -> """
.page-canvas {
  border-radius: 22px;
  border: 1.5px solid var(--paper-edge);
}
            """.trimIndent()

            PublishingLayoutStyle.DAWAH_MOTIVATIONAL -> """
.letter-envelope-header {
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  border-color: var(--accent-gold);
}
.letter-envelope-title {
  color: #FFFFFF;
}
.wisdom-pearl {
  border-inline-start-width: 5px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.04);
}
            """.trimIndent()

            PublishingLayoutStyle.NARRATIVE_STORY -> """
.page p {
  text-indent: 1.1em;
  line-height: 2.12;
}
.page h1 {
  text-align: center;
}
            """.trimIndent()

            PublishingLayoutStyle.ACADEMIC_STRUCTURED -> """
.page-canvas {
  border-radius: 8px;
  border-top: 4px solid var(--accent-rose);
}
.page h1 {
  border-inline-start: 4px solid var(--accent-rose);
  padding-inline-start: 10px;
}
            """.trimIndent()

            PublishingLayoutStyle.EDITORIAL_MAGAZINE -> """
.page-canvas {
  border-radius: 12px;
  border-top: 5px solid var(--accent-rose);
}
.page h1 {
  letter-spacing: 0.2px;
  border-bottom: 2px solid var(--accent-rose);
  padding-bottom: 6px;
}
            """.trimIndent()

            PublishingLayoutStyle.PURE_MINIMAL -> """
.app {
  background: var(--bg-primary);
}
.page-canvas {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}
.running-header {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  border-bottom: 1px solid var(--paper-edge);
}
.running-header-mark {
  color: var(--text-primary);
}
            """.trimIndent()
        }

        return "$commonComponents\n$styleSpecific"
    }
}
