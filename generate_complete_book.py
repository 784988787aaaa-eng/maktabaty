#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
generate_complete_book.py
Assembles the complete HTML file, manifest.json, and ZIP package for «أنا فتاة ولي رسالة».
"""

import os
import json
import zipfile
import shutil
from book_pages_data import PAGES
from book_assets import STYLES, COVER_SVG, FONTS_CSS

OUTPUT_HTML = "/أَنَا فَتَاةٌ وَلِي رِسَالَة.html"
OUTPUT_ZIP = "/أَنَا_فَتَاةٌ_وَلِي_رِسَالَة.zip"
MANIFEST_FILE = "/manifest_ana_fatat.json"


HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover"/>
  <meta name="theme-color" content="#FAF6F0"/>
  <meta name="color-scheme" content="light"/>
  <meta name="author" content="إصدارات مكتبتي الفاخرة — كتيب تربوي إيماني للفتيات"/>
  <meta name="description" content="أنا فتاة ولي رسالة — رحلة إلى أعماق الذات... من معرفة النفس إلى بناء الشخصية وصناعة الأثر."/>
  <title>🌸 أَنَا فَتَاةٌ وَلِي رِسَالَة — رِحْلَةٌ إِلَى أَعْمَاقِ الذَّات</title>

  <style>
__STYLES__
  </style>

  <style id="maktabati-perf-booster">
__FONTS_CSS__
  </style>
</head>
<body>
  <div class="app">
    <!-- 1. Top Bar -->
    <header class="top-bar">
      <div class="brand">
        <div class="brand-gem" aria-hidden="true">🌸</div>
        <span>أَنَا فَتَاةٌ وَلِي رِسَالَة</span>
      </div>
      <div class="tools">
        <button id="searchOpen" class="icon-btn" aria-label="بحث في الكتاب" title="بحث في الكتاب">
          <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        </button>
        <button id="tocBtn" class="icon-btn" aria-label="فهرس الفصول" title="فهرس الفصول">
          <svg viewBox="0 0 24 24"><line x1="4" y1="6" x2="20" y2="6"/><line x1="4" y1="12" x2="14" y2="12"/><line x1="4" y1="18" x2="18" y2="18"/></svg>
        </button>
        <button id="bookmarkBtn" class="icon-btn" aria-label="حفظ علامة مرجعية" title="حفظ علامة مرجعية">
          <svg viewBox="0 0 24 24"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
        </button>
      </div>
    </header>

    <!-- 2. Running Chapter Header -->
    <div id="runningHeader" class="running-header" hidden>
      <span id="runningHeaderTag" class="running-header-mark">الباب الأول</span>
      <span>•</span>
      <span id="runningHeaderTitle">أنا لست فتاة عادية</span>
    </div>

    <!-- 3. Middle Fluid Canvas Stage -->
    <main id="bookStage" class="book-stage">
      <div class="page-canvas">
        <article id="activePageArticle" class="page">
          <!-- Dynamically Rendered Page -->
        </article>
      </div>
    </main>

    <!-- 4. Bottom Navigation Dock -->
    <footer class="bottom-nav-dock">
      <button id="btnPrevPage" class="nav-btn" onclick="window.prevPage()">السابق</button>
      <span id="pageCounter" class="page-counter">١ / ٧٥</span>
      <button id="btnNextPage" class="nav-btn" onclick="window.nextPage()">التالي</button>
    </footer>
  </div>

  <!-- Search Overlay Panel -->
  <div id="searchPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>البحث في الكتاب 🌸</h2>
        <button id="searchClose" class="icon-btn" aria-label="إغلاق البحث">✕</button>
      </div>
      <div class="search-bar-wrap">
        <input id="searchInput" type="text" placeholder="ابحثي عن آية، حديث، مهارة، أو موضوع..." autocomplete="off"/>
        <button id="searchClearBtn" class="search-clear-btn" hidden aria-label="مسح">مسح</button>
      </div>
      <div class="search-status">
        <span id="searchStatus">اكتبي كلمة للبحث في صفحات الكتاب الـ ٧٥...</span>
      </div>
      <div id="searchResults" class="search-results-list"></div>
    </div>
  </div>

  <!-- TOC Overlay Panel -->
  <div id="tocPanel" class="overlay" aria-hidden="true">
    <div class="panel">
      <div class="panel-head">
        <h2>فهرس أبواب وفصول الكتاب</h2>
        <button id="tocClose" class="icon-btn" aria-label="إغلاق الفهرس">✕</button>
      </div>
      <div id="tocPanelList" class="toc-list"></div>
    </div>
  </div>

  <script>
    const BOOK_DATA = {
      title: "أَنَا فَتَاةٌ وَلِي رِسَالَة",
      totalPages: __TOTAL_PAGES__,
      pages: __PAGES_JSON__
    };

    let currentPageIndex = 0;
    let savedBookmarks = [];

    try {
      savedBookmarks = JSON.parse(localStorage.getItem("maktabati_fatat_bookmarks") || "[]");
    } catch (e) {
      savedBookmarks = [];
    }

    const activePageArticle = document.getElementById("activePageArticle");
    const pageCounter = document.getElementById("pageCounter");
    const btnPrevPage = document.getElementById("btnPrevPage");
    const btnNextPage = document.getElementById("btnNextPage");
    const runningHeader = document.getElementById("runningHeader");
    const runningHeaderTag = document.getElementById("runningHeaderTag");
    const runningHeaderTitle = document.getElementById("runningHeaderTitle");
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

      const page = BOOK_DATA.pages[index];
      activePageArticle.innerHTML = page.html;

      // Update counters & buttons
      pageCounter.textContent = (index + 1) + " / " + BOOK_DATA.totalPages;
      btnPrevPage.disabled = (index === 0);
      btnNextPage.disabled = (index === BOOK_DATA.totalPages - 1);

      // Running Header
      if (page.type === "cover") {
        runningHeader.hidden = true;
      } else {
        runningHeader.hidden = false;
        runningHeaderTag.textContent = page.tag || "مكتبتي";
        runningHeaderTitle.textContent = page.title || "";
      }

      // Bookmark button state
      if (savedBookmarks.includes(index)) {
        bookmarkBtn.classList.add("bookmarked");
      } else {
        bookmarkBtn.classList.remove("bookmarked");
      }

      // Scroll to top
      bookStage.scrollTop = 0;

      // Restore interactive inputs on this page
      restorePageInputs();

      // Update reader name slots
      updateReaderNameInDom();

      // Highlight search query if requested
      if (highlightQuery) {
        applyCalmPageSearchHighlight(highlightQuery);
      }

      // Save last read page in localStorage
      try {
        localStorage.setItem("maktabati_fatat_last_page", String(index));
      } catch (e) {}

      // Inform Maktabati Android Bridge if present
      if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
        window.MaktabatiBridge.onPageChanged(index + 1, BOOK_DATA.totalPages);
      }
    }

    window.goToPage = function(idx) {
      renderPage(idx);
    };

    window.nextPage = function() {
      if (currentPageIndex < BOOK_DATA.totalPages - 1) {
        renderPage(currentPageIndex + 1);
      }
    };

    window.prevPage = function() {
      if (currentPageIndex > 0) {
        renderPage(currentPageIndex - 1);
      }
    };

    // Keyboard navigation
    window.addEventListener("keydown", function(e) {
      if (e.target.tagName === "INPUT" || e.target.tagName === "TEXTAREA") return;
      if (e.key === "ArrowLeft") window.nextPage();
      if (e.key === "ArrowRight") window.prevPage();
      if (e.key === "Escape") {
        searchPanel.classList.remove("open");
        tocPanel.classList.remove("open");
      }
    });

    // Touch swipe navigation
    let touchStartX = 0;
    let touchStartY = 0;
    bookStage.addEventListener("touchstart", function(e) {
      if (e.touches && e.touches.length === 1) {
        touchStartX = e.touches[0].clientX;
        touchStartY = e.touches[0].clientY;
      }
    }, { passive: true });

    bookStage.addEventListener("touchend", function(e) {
      if (e.changedTouches && e.changedTouches.length === 1) {
        const diffX = e.changedTouches[0].clientX - touchStartX;
        const diffY = e.changedTouches[0].clientY - touchStartY;
        if (Math.abs(diffX) > 65 && Math.abs(diffY) < 55) {
          if (diffX < 0) {
            window.nextPage(); // RTL swipe left
          } else {
            window.prevPage(); // RTL swipe right
          }
        }
      }
    }, { passive: true });

    // Lock 2-finger touchmove to prevent random image-like viewport zooming
    document.addEventListener("touchmove", function(e) {
      if (e.touches && e.touches.length > 1) {
        e.preventDefault();
      }
    }, { passive: false });

    // Fluid Reflow Zoom Engine (75% to 200%)
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

    // Bookmark Toggle
    bookmarkBtn.addEventListener("click", function() {
      const idx = currentPageIndex;
      if (savedBookmarks.includes(idx)) {
        savedBookmarks = savedBookmarks.filter(i => i !== idx);
      } else {
        savedBookmarks.push(idx);
      }
      try {
        localStorage.setItem("maktabati_fatat_bookmarks", JSON.stringify(savedBookmarks));
      } catch (e) {}
      renderPage(currentPageIndex);
    });

    // TOC Panel
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
        html += `<div class="${cls}" onclick="window.goToPage(${idx}); tocPanel.classList.remove('open');">
          <span>${p.tag ? p.tag + ' • ' : ''}${p.title}</span>
          <small>صفحة ${idx + 1}</small>
        </div>`;
      });
      tocPanelList.innerHTML = html;
    }

    // Search Engine with Arabic Normalization & Mapping
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
      const tashkeelRegex = /[\\u064B-\\u065F\\u0670\\u06D6-\\u06EDـ]/;
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
        searchStatus.textContent = "اكتبي كلمتين على الأقل للبحث في الكتاب...";
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
              title: page.title || ("صفحة " + (i + 1)),
              tag: page.tag || "",
              snippet: prefix + `<mark class="search-hit-mark">${matchText}</mark>` + suffix
            });
          }

          searchPos = matchIdx + Math.max(1, qNorm.length);
          if (hits.length >= 40) break;
        }

        if (hits.length >= 40) break;
      }

      if (hits.length === 0) {
        searchStatus.textContent = `لم يتم العثور على نتائج لكلمة «${rawQuery}»`;
        searchResults.innerHTML = `<div class="empty">جرّبي البحث بكلمة أخرى أو مرادف بديل.</div>`;
      } else {
        searchStatus.textContent = `عُثر على ${hits.length} نتيجة مطابقة:`;
        let html = "";
        hits.forEach(hit => {
          html += `<div class="search-result" onclick="selectSearchResult(${hit.pageIndex}, '${escapeQuery(rawQuery)}')">
            <div class="search-result-head">
              <strong>${hit.tag ? hit.tag + ' • ' : ''}${hit.title}</strong>
              <span class="search-result-page">ص ${hit.pageNumber}</span>
            </div>
            <small>${hit.snippet}</small>
          </div>`;
        });
        searchResults.innerHTML = html;
      }
    }

    function escapeQuery(str) {
      return str.replace(/['"\\\\]/g, "");
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

    // LocalStorage Interactive Field Persistence
    window.saveField = function(id, val) {
      try {
        localStorage.setItem("maktabati_fatat_f_" + id, val);
      } catch (e) {}
    };

    window.saveCheck = function(id, val) {
      try {
        localStorage.setItem("maktabati_fatat_c_" + id, val ? "1" : "0");
      } catch (e) {}
    };

    window.saveReaderName = function(name) {
      try {
        localStorage.setItem("maktabati_fatat_reader_name", name.trim());
        updateReaderNameInDom();
      } catch (e) {}
    };

    window.alertSavedName = function() {
      const input = document.getElementById("readerNameInput");
      const name = input ? input.value.trim() : "";
      if (name) {
        alert("أهلاً بكِ يا " + name + " في رحلتكِ المباركة! 🌸 تم حفظ اسمكِ وسيرافقكِ في صفحات الكتاب.");
      }
    };

    window.saveCovenant = function() {
      const sig = document.getElementById("covenant_signature");
      const dt = document.getElementById("covenant_date");
      if (sig && sig.value.trim()) {
        alert("مبارك عليكِ اعتماد ميثاقكِ المبارك! 🌸 بارك الله في سعيكِ وجعلكِ منارة أثر.");
      } else {
        alert("اكتبي توقيعكِ الكريم لاعتماد الميثاق المبارك 🌸");
      }
    };

    function updateReaderNameInDom() {
      const savedName = localStorage.getItem("maktabati_fatat_reader_name") || "";
      const slots = document.querySelectorAll(".reader-name-slot");
      slots.forEach(slot => {
        slot.textContent = savedName ? " «" + savedName + "»" : "";
      });
      const nameInput = document.getElementById("readerNameInput");
      if (nameInput && savedName) {
        nameInput.value = savedName;
      }
    }

    function restorePageInputs() {
      const inputs = activePageArticle.querySelectorAll("input, textarea");
      inputs.forEach(el => {
        if (!el.id) return;
        if (el.type === "checkbox") {
          const val = localStorage.getItem("maktabati_fatat_c_" + el.id);
          if (val !== null) el.checked = (val === "1");
        } else {
          const val = localStorage.getItem("maktabati_fatat_f_" + el.id);
          if (val !== null) el.value = val;
        }
      });
    }

    // Init on DOM ready
    document.addEventListener("DOMContentLoaded", function() {
      // Restore saved zoom
      try {
        const savedZoom = localStorage.getItem("maktabati_fluid_zoom_pct");
        if (savedZoom) window.__maktabatiSetFluidZoom(savedZoom, false);
      } catch (e) {}

      // Restore last page or start at 0
      let startPage = 0;
      try {
        const lp = localStorage.getItem("maktabati_fatat_last_page");
        if (lp !== null) startPage = parseInt(lp, 10) || 0;
      } catch (e) {}

      renderPage(startPage);
    });
  </script>
</body>
</html>
"""

def generate():
    print(f"Total Pages configured: {len(PAGES)}")
    # Prepare pages with COVER_SVG replacement
    processed_pages = []
    for p in PAGES:
        html = p["html"].replace("__COVER_SVG__", COVER_SVG)
        processed_pages.append({
            "tag": p["tag"],
            "title": p["title"],
            "type": p["type"],
            "html": html
        })

    pages_json = json.dumps(processed_pages, ensure_ascii=False)

    full_html = HTML_TEMPLATE
    full_html = full_html.replace("__STYLES__", STYLES)
    full_html = full_html.replace("__FONTS_CSS__", FONTS_CSS)
    full_html = full_html.replace("__TOTAL_PAGES__", str(len(PAGES)))
    full_html = full_html.replace("__PAGES_JSON__", pages_json)

    with open(OUTPUT_HTML, "w", encoding="utf-8") as f:
        f.write(full_html)
    print(f"Generated {OUTPUT_HTML} ({len(full_html)} chars)")

    # Generate manifest_ana_fatat.json
    manifest = {
        "id": "ana_fatat_wa_li_risalah",
        "title": "أَنَا فَتَاةٌ وَلِي رِسَالَة",
        "subtitle": "رحلة إلى أعماق الذات... من معرفة النفس إلى بناء الشخصية وصناعة الأثر",
        "author": "إصدارات مكتبتي الفاخرة — كتيب تربوي إيماني للفتيات",
        "edition": "الطبعة الرقمية الفاخرة V9.0",
        "language": "ar",
        "direction": "rtl",
        "entry": "أَنَا فَتَاةٌ وَلِي رِسَالَة.html",
        "pageCount": len(PAGES),
        "theme": {
            "name": "dusty_rose_sage",
            "bgPrimary": "#FAF6F0",
            "bgSecondary": "#FFFDF9",
            "bgSoftRose": "#FCF2F5",
            "bgSoftSage": "#EFF5F0",
            "bgPearlGold": "#FDF7EC",
            "accentRose": "#BD6B80",
            "accentRoseDark": "#732E41",
            "accentRoseLight": "#D98C9F",
            "accentSage": "#6B856E",
            "accentSageDark": "#445946",
            "accentGold": "#C69A4E",
            "textPrimary": "#38272C",
            "textSecondary": "#6B565C"
        },
        "typography": {
            "body": "Noto Naskh Arabic",
            "headings": "Aref Ruqaa",
            "chapterTitles": "Reem Kufi",
            "quotes": "Amiri",
            "interface": "Noto Sans Arabic",
            "numbers": "Noto Sans Arabic"
        },
        "tags": [
            "بنات",
            "تربية",
            "إيماني",
            "بناء الذات",
            "تطوير الشخصية",
            "المرأة المسلمة",
            "صناعة الأثر"
        ]
    }

    with open(MANIFEST_FILE, "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)
    print(f"Generated {MANIFEST_FILE}")

    # Build ZIP Package
    print(f"Packaging {OUTPUT_ZIP}...")
    with zipfile.ZipFile(OUTPUT_ZIP, "w", zipfile.ZIP_DEFLATED) as zf:
        # 1. Main entry html
        zf.write(OUTPUT_HTML, arcname="أَنَا فَتَاةٌ وَلِي رِسَالَة.html")
        zf.write(OUTPUT_HTML, arcname="index.html")
        
        # 2. Manifest
        zf.write(MANIFEST_FILE, arcname="manifest.json")

        # 3. Assets fonts
        assets_fonts_dir = "/app/src/main/assets/fonts"
        if os.path.exists(assets_fonts_dir):
            for root, dirs, files in os.walk(assets_fonts_dir):
                for file in files:
                    full_p = os.path.join(root, file)
                    rel_p = os.path.relpath(full_p, "/app/src/main")
                    zf.write(full_p, arcname=rel_p)

    zip_size = os.path.getsize(OUTPUT_ZIP)
    print(f"Successfully packaged {OUTPUT_ZIP} ({zip_size} bytes)")

if __name__ == "__main__":
    generate()
