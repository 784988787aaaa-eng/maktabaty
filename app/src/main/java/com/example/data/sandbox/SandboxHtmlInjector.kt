package com.example.data.sandbox

/**
 * High-speed, non-intrusive preprocessor & runtime engine for standalone HTML/CSS/JS books:
 * 1. Preserves `<meta charset="utf-8">` and `<meta name="viewport">` at byte 0 by injecting right before `</head>`.
 * 2. Normalizes `dvh`/`svh`/`lvh` viewport units to `vh` and enforces 100vh fallback on `.app`.
 * 3. Provides crash-proof per-book `localStorage` isolation + full `window.localStorage` polyfill.
 * 4. Silent Fluid Reflow Zoom Engine (`Fluid Reflow Zoom`): pinch-to-zoom & button zoom (`75%`–`200%`) that updates
 *    `--reader-font`, `--reader-font-size`, and `--font-scale` so text, cards, quotes, and ornaments reflow
 *    inside the page width without rigid PDF-like horizontal clipping. Never injects any HUD or toast into WebView DOM.
 * 5. Silent Layout Containment (`MaktabatiLayoutGuard`): silently keeps elements fitting within `.page-canvas`
 *    (`max-width: 100%; box-sizing: border-box; overflow-wrap: anywhere;`) without emitting any warning or toast.
 * 6. Subtle Smooth Fade & Micro-Slide Page Transition: clean 1-finger swipe navigation with a gentle
 *    `180ms cubic-bezier(0.22, 1, 0.36, 1)` micro-slide (`translateX(±10px) -> translateX(0)`) and opacity fade
 *    (`opacity: 0.5 -> 1.0`) only when the page changes, with zero overlays or shapes during finger drag.
 */
object SandboxHtmlInjector {

    private val DVH_UNIT_REGEX = Regex("(\\d+(?:\\.\\d+)?)[dsl]vh\\b", RegexOption.IGNORE_CASE)

    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
          @font-face {
            font-family: "Amiri";
            src: url("https://maktabati.app/fonts/amiri.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Noto Naskh Arabic";
            src: url("https://maktabati.app/fonts/amiri.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Scheherazade New";
            src: url("https://maktabati.app/fonts/amiri.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Traditional Arabic";
            src: url("https://maktabati.app/fonts/amiri.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Cairo";
            src: url("https://maktabati.app/fonts/cairo.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Tajawal";
            src: url("https://maktabati.app/fonts/cairo.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          @font-face {
            font-family: "Readex Pro";
            src: url("https://maktabati.app/fonts/cairo.ttf") format("truetype");
            font-weight: 100 900;
            font-style: normal;
            font-display: swap;
          }
          :root {
            --reader-font: 1;
            --reader-font-size: 100%;
            --font-scale: 1;
          }
          html, body {
            width: 100%;
            max-width: 100% !important;
            height: 100%;
            min-height: 100vh;
            margin: 0;
            padding: 0;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
            -webkit-tap-highlight-color: transparent;
            -webkit-text-size-adjust: 100%;
          }
          .app {
            width: 100%;
            max-width: 100% !important;
            height: 100vh;
            min-height: 100vh;
            max-height: 100vh;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
          }
          .book-stage, .stage {
            width: 100%;
            max-width: 100%;
            box-sizing: border-box;
          }
          /* Never clip a page container: long content must remain scrollable and visible. */
          .page-canvas, .page, .book-page, .page-container {
            max-width: 100%;
            box-sizing: border-box;
            position: relative;
            transform-origin: top center;
          }
          /* The selected page grows with its text; it is never clipped by a fixed-height page shell. */
          .page.active, .book-page.active, [data-page].active,
          #pageInnerContent, #pageBody, .page-body, .page-content {
            max-height: none !important;
            overflow-y: visible !important;
            overflow-x: visible;
          }
          html { font-size: calc(16px * var(--reader-font, 1)); }
          body { font-size: 1rem; overflow-x: hidden; overflow-y: auto; }
          .maktabati-gpu-hint {
            will-change: transform, opacity;
            transform: translateZ(0);
          }
          @keyframes maktabatiMicroSlideNext {
            0% {
              opacity: 0.5;
              transform: translateX(-10px);
            }
            100% {
              opacity: 1;
              transform: translateX(0);
            }
          }
          @keyframes maktabatiMicroSlidePrev {
            0% {
              opacity: 0.5;
              transform: translateX(10px);
            }
            100% {
              opacity: 1;
              transform: translateX(0);
            }
          }
          .maktabati-transition-next {
            animation: maktabatiMicroSlideNext 180ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
          }
          .maktabati-transition-prev {
            animation: maktabatiMicroSlidePrev 180ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
          }
          #pageInnerContent, .page-body, .page-content {
            max-width: 100%;
            box-sizing: border-box;
            overflow-wrap: break-word;
          }
          /* Constrain media, not every descendant: preserve designed grids and page layouts. */
          #pageInnerContent img, .page-body img, .page-content img,
          #pageInnerContent svg, .page-body svg, .page-content svg,
          #pageInnerContent video, .page-body video, .page-content video {
            max-width: 100%;
            height: auto;
          }
          #pageInnerContent table, .page-body table, .page-content table {
            max-width: 100%;
            box-sizing: border-box;
          }
          #pageInnerContent pre, .page-body pre, .page-content pre {
            max-width: 100%;
            overflow-x: auto;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
          }
          .quote-box, blockquote, .insight-card, .flip-card, .quiz-card, .quiz-option {
            max-width: 100% !important;
            box-sizing: border-box !important;
            overflow-wrap: anywhere !important;
          }
          .overlay {
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
          }
          .bottom-nav-dock, .controls-bar, .page-footer {
            flex-shrink: 0;
            z-index: 25;
          }
        </style>
        <script id="maktabati-storage-isolation">
        (function(){
          if (window.__maktabatiV9Initialized) return;
          window.__maktabatiV9Initialized = true;
          window.__maktabatiV8Initialized = true;
          window.__maktabatiV7Initialized = true;
          window.__maktabatiV6Initialized = true;
          window.__maktabatiV5Initialized = true;
          window.__maktabatiV4Initialized = true;
          window.__maktabatiV3Initialized = true;
          try {
            const P = "$safePrefix", TOTAL = ${pageCount.coerceAtLeast(1)};
            const memStore = {};
            const key = function(k) { k = String(k); return k.indexOf(P) === 0 ? k : P + k; };

            function purgeLegacyDomArtifacts() {
              try {
                const legacyIds = [
                  'maktabati-zoom-badge',
                  'maktabati-distortion-toast',
                  'maktabati-guard-toast',
                  'maktabati-page-curl-overlay',
                  'maktabati-curl-layer',
                  'maktabati-curl-backside',
                  'maktabati-curl-back',
                  'maktabati-curl-shadow',
                  'maktabati-curl-spine'
                ];
                for (let i = 0; i < legacyIds.length; i++) {
                  const el = document.getElementById(legacyIds[i]);
                  if (el && el.parentNode) el.parentNode.removeChild(el);
                }
              } catch (e) {}
            }

            function getDomPageElements() {
              try {
                const dataPageEls = Array.from(document.querySelectorAll('[data-page]')).filter(function(el) {
                  return /^\d+$/.test((el.getAttribute('data-page') || '').trim());
                });
                if (dataPageEls.length > 0) {
                  /* Ignore nested data-page annotations inside a page and sort by the numeric index. */
                  const set = new Set(dataPageEls);
                  const pageRoots = dataPageEls.filter(function(el) {
                    let parent = el.parentElement;
                    while (parent && parent !== document.body) {
                      if (set.has(parent)) return false;
                      parent = parent.parentElement;
                    }
                    return true;
                  });
                  const byIndex = new Map();
                  pageRoots.forEach(function(el) {
                    const index = Number(el.getAttribute('data-page'));
                    if (!byIndex.has(index)) byIndex.set(index, el);
                  });
                  return Array.from(byIndex.entries()).sort(function(a, b) {
                    return a[0] - b[0];
                  }).map(function(entry) { return entry[1]; });
                }
                return Array.from(document.querySelectorAll('article.page, section.page, div.page, .book-page')).filter(function(el) {
                  return el.classList && (el.classList.contains('page') || el.classList.contains('book-page'));
                });
              } catch (e) {
                return [];
              }
            }

            function resolveTrueTotalPages() {
              try {
                const data = getBookData();
                const pages = getPagesArray(data);
                if (pages && pages.length > 0) return pages.length;
                const domPages = getDomPageElements();
                if (domPages.length > 0) return domPages.length;
                const counterEl = document.getElementById('pageCount') || document.getElementById('pageCounter');
                if (counterEl) {
                  const m = (counterEl.textContent || '').match(/(\d+)\s*(?:\/|من)\s*(\d+)/);
                  if (m && Number(m[2]) > 0) return Number(m[2]);
                }
              } catch (e) {}
              return TOTAL;
            }

            function reportPageToBridge(oneBasedPage, totalPages) {
              try {
                const tot = Math.max(1, Number(totalPages) || resolveTrueTotalPages() || 1);
                const cur = Math.max(1, Math.min(tot, Number(oneBasedPage) || 1));
                window.__maktabatiLastSavedPage = cur - 1;
                if (window.__maktabatiLastReportedPage === cur && window.__maktabatiLastReportedTotal === tot) {
                  return;
                }
                window.__maktabatiLastReportedPage = cur;
                window.__maktabatiLastReportedTotal = tot;
                if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                  window.MaktabatiBridge.onPageChanged(cur, tot);
                }
              } catch (e) {}
            }

            function syncDomActivePageWithBridge() {
              try {
                const domPages = getDomPageElements();
                if (domPages.length > 0) {
                  let activeIdx = -1;
                  for (let i = 0; i < domPages.length; i++) {
                    if (domPages[i].classList && domPages[i].classList.contains('active')) {
                      activeIdx = i;
                      break;
                    }
                  }
                  if (activeIdx >= 0) {
                    reportPageToBridge(activeIdx + 1, domPages.length);
                    return;
                  }
                }
                const counterEl = document.getElementById('pageCount') || document.getElementById('pageCounter');
                if (counterEl) {
                  const m = (counterEl.textContent || '').match(/(\d+)\s*(?:\/|من)\s*(\d+)/);
                  if (m && Number(m[1]) > 0 && Number(m[2]) > 0) {
                    reportPageToBridge(Number(m[1]), Number(m[2]));
                  }
                }
              } catch (e) {}
            }

            function notifyBridgeFromJson(sv) {
              try {
                if (!sv || sv.charAt(0) !== '{') return;
                const d = JSON.parse(sv);
                if (!d) return;
                const pNum = typeof d.currentPage === 'number' ? d.currentPage : (typeof d.page === 'number' ? d.page : null);
                if (pNum !== null) {
                  reportPageToBridge(pNum + 1, resolveTrueTotalPages());
                }
              } catch (e) {}
            }

            const fallbackStorage = {
              getItem: function(k) {
                const sk = key(k);
                return Object.prototype.hasOwnProperty.call(memStore, sk) ? memStore[sk] : null;
              },
              setItem: function(k, v) {
                const sk = key(k);
                const sv = String(v);
                memStore[sk] = sv;
                notifyBridgeFromJson(sv);
              },
              removeItem: function(k) {
                delete memStore[key(k)];
              },
              clear: function() {
                for (const prop in memStore) {
                  if (Object.prototype.hasOwnProperty.call(memStore, prop)) delete memStore[prop];
                }
              }
            };

            let nativeStorageOk = false;
            try {
              const ls = window.localStorage;
              if (ls) {
                ls.setItem(P + "__probe__", "1");
                ls.removeItem(P + "__probe__");
                nativeStorageOk = true;
              }
            } catch (e) {
              nativeStorageOk = false;
            }

            if (!nativeStorageOk) {
              try {
                Object.defineProperty(window, 'localStorage', {
                  configurable: true,
                  enumerable: true,
                  get: function() { return fallbackStorage; }
                });
              } catch (e) {}
            } else {
              try {
                const origGet = Storage.prototype.getItem;
                const origSet = Storage.prototype.setItem;
                const origRem = Storage.prototype.removeItem;

                Storage.prototype.getItem = function(k) {
                  const sk = key(k);
                  try {
                    const v = origGet.call(this, sk);
                    return v !== null ? v : (memStore[sk] !== undefined ? memStore[sk] : null);
                  } catch (e) {
                    return memStore[sk] !== undefined ? memStore[sk] : null;
                  }
                };

                Storage.prototype.setItem = function(k, v) {
                  const sk = key(k);
                  const sv = String(v);
                  memStore[sk] = sv;
                  try { origSet.call(this, sk, sv); } catch (e) {}
                  notifyBridgeFromJson(sv);
                };

                Storage.prototype.removeItem = function(k) {
                  const sk = key(k);
                  delete memStore[sk];
                  try { origRem.call(this, sk); } catch (e) {}
                };
              } catch (storageErr) {}
            }

            const pageHtmlCache = {};

            function getBookData() {
              try { if (typeof BOOK_DATA !== 'undefined' && BOOK_DATA) return BOOK_DATA; } catch (e) {}
              try { if (window.BOOK_DATA) return window.BOOK_DATA; } catch (e) {}
              return null;
            }

            function getPagesArray(data) {
              if (!data) return null;
              if (Array.isArray(data.pages)) return data.pages;
              if (Array.isArray(data.chapters)) return data.chapters;
              if (Array.isArray(data.sections)) return data.sections;
              if (Array.isArray(data.slides)) return data.slides;
              return null;
            }

            function getCurrentPageIndex() {
              try {
                if (typeof state !== 'undefined' && state) {
                  if (typeof state.currentPage === 'number') return state.currentPage;
                  if (typeof state.page === 'number') return state.page;
                }
              } catch (e) {}
              try { if (typeof currentPage === 'number') return currentPage; } catch (e) {}
              if (window.state) {
                if (typeof window.state.currentPage === 'number') return window.state.currentPage;
                if (typeof window.state.page === 'number') return window.state.page;
              }
              if (typeof window.currentPage === 'number') return window.currentPage;
              const domPages = getDomPageElements();
              if (domPages.length > 0) {
                for (let i = 0; i < domPages.length; i++) {
                  if (domPages[i].classList && domPages[i].classList.contains('active')) {
                    return i;
                  }
                }
              }
              if (typeof window.__maktabatiLastSavedPage === 'number') return window.__maktabatiLastSavedPage;
              if (typeof window.__maktabatiFallbackPage === 'number') return window.__maktabatiFallbackPage;
              return 0;
            }

            function setCurrentPageIndex(newIdx) {
              window.__maktabatiFallbackPage = newIdx;
              window.__maktabatiLastSavedPage = newIdx;
              try {
                if (typeof state !== 'undefined' && state) {
                  state.currentPage = newIdx;
                  state.page = newIdx;
                }
              } catch (e) {}
              try { if (typeof currentPage !== 'undefined') currentPage = newIdx; } catch (e) {}
              if (window.state) {
                window.state.currentPage = newIdx;
                window.state.page = newIdx;
              }
            }

            function getActivePageCanvas() {
              return document.getElementById('pageCanvas')
                || document.getElementById('pageInnerContent')
                || document.getElementById('pageContent')
                || document.querySelector('.page-canvas, .page.active, .book-page.active, .page-body, .app')
                || document.body;
            }

            let transitionCleanupTimer = null;
            function animateSubtlePageTransition(delta) {
              try {
                const target = getActivePageCanvas();
                if (!target || target === document.body) return;
                target.classList.remove('maktabati-transition-next', 'maktabati-transition-prev');
                void target.offsetWidth;
                const cls = delta >= 0 ? 'maktabati-transition-next' : 'maktabati-transition-prev';
                target.classList.add(cls);
                if (transitionCleanupTimer) clearTimeout(transitionCleanupTimer);
                transitionCleanupTimer = setTimeout(function() {
                  try {
                    target.classList.remove('maktabati-transition-next', 'maktabati-transition-prev');
                  } catch (e) {}
                }, 200);
              } catch (e) {}
            }

            function buildPageHtml(pageObj, idx) {
              if (pageHtmlCache[idx]) return pageHtmlCache[idx];
              if (!pageObj) return '';
              const parts = [];
              const badge = pageObj.chapterTag || pageObj.chapterNumber || pageObj.sectionTitle || pageObj.badge || ('صفحة ' + (idx + 1));
              const heading = pageObj.title || pageObj.heading || pageObj.name || '';
              const sub = pageObj.subtitle || '';

              if (badge) {
                parts.push('<div style="display:inline-block;align-self:center;padding:3px 12px;border-radius:99px;font-size:0.8em;font-weight:bold;opacity:0.88;border:1px solid currentColor;margin-bottom:8px;">' + badge + '</div>');
              }
              if (heading) {
                parts.push('<h2 style="margin:0 0 8px 0;line-height:1.45;text-align:center;font-size:1.32em;">' + heading + '</h2>');
              }
              if (sub && sub !== heading) {
                parts.push('<div style="text-align:center;opacity:0.8;font-size:0.92em;margin-bottom:10px;">' + sub + '</div>');
              }
              if (pageObj.ayah || pageObj.verse) {
                parts.push('<div class="quote-box" style="margin:8px 0;padding:10px 14px;border-right:3px solid #D97706;background:rgba(217,119,6,0.08);border-radius:10px;text-align:center;font-weight:bold;font-size:1.05em;line-height:1.8;">' + (pageObj.ayah || pageObj.verse) + '</div>');
              }
              if (pageObj.hadith) {
                parts.push('<div class="quote-box" style="margin:8px 0;padding:9px 13px;border-right:3px solid #047857;background:rgba(4,120,87,0.07);border-radius:10px;font-size:0.96em;line-height:1.7;">' + pageObj.hadith + '</div>');
              }
              if (typeof pageObj.content === 'string' && pageObj.content.trim()) {
                parts.push('<div style="line-height:1.85;font-size:1em;text-align:justify;">' + pageObj.content + '</div>');
              } else if (typeof pageObj.html === 'string' && pageObj.html.trim()) {
                parts.push('<div style="line-height:1.85;font-size:1em;">' + pageObj.html + '</div>');
              } else if (typeof pageObj.body === 'string' && pageObj.body.trim()) {
                parts.push('<p style="line-height:1.85;font-size:1em;text-align:justify;">' + pageObj.body + '</p>');
              } else if (typeof pageObj.text === 'string' && pageObj.text.trim()) {
                parts.push('<p style="line-height:1.85;font-size:1em;text-align:justify;">' + pageObj.text + '</p>');
              }
              if (Array.isArray(pageObj.paragraphs)) {
                pageObj.paragraphs.forEach(function(p) {
                  if (typeof p === 'string' && p.trim()) {
                    parts.push('<p style="line-height:1.85;font-size:1em;margin:0 0 10px 0;text-align:justify;">' + p + '</p>');
                  } else if (p && typeof p.text === 'string') {
                    parts.push('<p style="line-height:1.85;font-size:1em;margin:0 0 10px 0;text-align:justify;">' + p.text + '</p>');
                  }
                });
              }
              const quoteText = pageObj.quote || pageObj.epigraph || pageObj.reflection || '';
              if (quoteText) {
                parts.push('<blockquote style="margin:10px 0;padding:10px 14px;border-right:3px solid currentColor;opacity:0.9;font-style:italic;border-radius:8px;background:rgba(128,128,128,0.07);">' + quoteText + '</blockquote>');
              }
              const built = parts.join('');
              pageHtmlCache[idx] = built;
              return built;
            }

            // =========================================================================
            // 1. SILENT FLUID REFLOW ZOOM ENGINE & SILENT LAYOUT CONTAINMENT
            //    (Zero DOM HUD elements or warning toasts inside WebView)
            // =========================================================================
            let currentZoomPercent = 100;

            const MaktabatiLayoutGuard = {
              inspectAndHeal: function() {
                try {
                  const canvas = getActivePageCanvas();
                  if (!canvas) return { healedCount: 0 };
                  const canvasWidth = canvas.clientWidth || window.innerWidth || 360;
                  let healedCount = 0;

                  const candidates = canvas.querySelectorAll(
                    'table, img, svg, video, pre, code'
                  );
                  for (let i = 0; i < candidates.length; i++) {
                    const el = candidates[i];
                    if (!el) continue;
                    if (el.scrollWidth > canvasWidth + 8) {
                      el.style.setProperty('max-width', '100%', 'important');
                      el.style.setProperty('box-sizing', 'border-box', 'important');
                      el.style.setProperty('overflow-wrap', 'anywhere', 'important');
                      el.style.setProperty('word-break', 'break-word', 'important');
                      if (el.style.width && el.style.width.indexOf('px') !== -1) {
                        el.style.setProperty('width', '100%', 'important');
                        el.style.setProperty('min-width', '0', 'important');
                      }
                      healedCount++;
                    }
                  }
                  return { healedCount: healedCount };
                } catch (e) {
                  return { healedCount: 0 };
                }
              }
            };
            window.MaktabatiLayoutGuard = MaktabatiLayoutGuard;

            window.__maktabatiSetFluidZoom = function(percent, fromNative) {
              try {
                let target = Math.round(Number(percent) || 100);
                if (target < 75) target = 75;
                if (target > 200) target = 200;

                currentZoomPercent = target;
                const scale = +(target / 100).toFixed(2);
                const root = document.documentElement;
                const scroller = document.scrollingElement || document.documentElement;
                const oldScrollTop = scroller ? scroller.scrollTop : 0;
                const activePage = getCurrentPageIndex();

                /* One zoom mechanism only: root rem/em reflow. Native WebView textZoom stays at 100%. */
                root.style.setProperty('--reader-font', String(scale));
                root.style.setProperty('--reader-font-size', target + '%');
                root.style.setProperty('--font-scale', String(scale));
                root.style.fontSize = 'calc(16px * ' + scale + ')';

                try {
                  if (typeof state !== 'undefined' && state) state.fontScale = scale;
                  if (window.state) window.state.fontScale = scale;
                } catch (e) {}

                MaktabatiLayoutGuard.inspectAndHeal();
                requestAnimationFrame(function() {
                  try {
                    if (scroller && getCurrentPageIndex() === activePage) scroller.scrollTop = oldScrollTop;
                  } catch (e) {}
                });

                if (!fromNative && window.MaktabatiBridge && window.MaktabatiBridge.onZoomChanged) {
                  window.MaktabatiBridge.onZoomChanged(target);
                }
              } catch (e) {}
            };

            // Zero-reflow check: only acts if the book template left its container completely empty
            function healBlankPageIfNeeded() {
              try {
                purgeLegacyDomArtifacts();
                syncDomActivePageWithBridge();
                let container = document.getElementById('pageCanvas')
                  || document.getElementById('pageInnerContent')
                  || document.getElementById('pageContent')
                  || document.querySelector('.page-canvas, .page.active .page-body, .page.active, .book-page.active');

                if (container && (container.childElementCount > 0 || (container.textContent || '').trim().length > 0)) {
                  MaktabatiLayoutGuard.inspectAndHeal();
                  return;
                }

                const data = getBookData();
                const pages = getPagesArray(data);
                if (!pages || !pages.length) return;
                const idx = Math.max(0, Math.min(pages.length - 1, getCurrentPageIndex()));
                const pageObj = pages[idx];
                if (!pageObj) return;

                if (!container) {
                  const stage = document.createElement('div');
                  stage.style.cssText = 'min-height:100vh;display:flex;flex-direction:column;justify-content:space-between;padding:12px;box-sizing:border-box;';
                  stage.innerHTML =
                    '<div id="pageInnerContent" style="flex:1;background:#FCF9F2;color:#1F1A17;border-radius:16px;padding:20px;box-shadow:0 8px 24px rgba(0,0,0,0.2);"></div>' +
                    '<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 14px;margin-top:8px;background:rgba(0,0,0,0.35);color:#fff;border-radius:12px;">' +
                      '<button id="btnPrevPage" onclick="window.__maktabatiTurn(-1)" style="padding:6px 14px;border-radius:8px;border:0;font-weight:bold;">السابق</button>' +
                      '<span id="pageCounter"></span>' +
                      '<button id="btnNextPage" onclick="window.__maktabatiTurn(1)" style="padding:6px 14px;border-radius:8px;border:0;font-weight:bold;">التالي</button>' +
                    '</div>';
                  document.body.appendChild(stage);
                  container = document.getElementById('pageInnerContent');
                }

                const html = buildPageHtml(pageObj, idx);
                if (html && container) {
                  container.innerHTML = html;
                }
                const counterEl = document.getElementById('pageCounter') || document.getElementById('pageCount');
                if (counterEl && !(counterEl.textContent || '').trim()) {
                  counterEl.textContent = (idx + 1) + ' / ' + pages.length;
                }
                MaktabatiLayoutGuard.inspectAndHeal();
                reportPageToBridge(idx + 1, pages.length);
              } catch (e) {}
            }

            window.__maktabatiTurn = function(delta) {
              const data = getBookData();
              const pages = getPagesArray(data);
              if (pages && pages.length) {
                const curIdx = getCurrentPageIndex();
                const nextIdx = Math.max(0, Math.min(pages.length - 1, curIdx + delta));
                if (nextIdx === curIdx) return;
                setCurrentPageIndex(nextIdx);
                const container = document.getElementById('pageInnerContent') || document.getElementById('pageContent') || document.getElementById('pageCanvas');
                if (container) {
                  container.innerHTML = buildPageHtml(pages[nextIdx], nextIdx);
                }
                const counterEl = document.getElementById('pageCounter') || document.getElementById('pageCount');
                if (counterEl) counterEl.textContent = (nextIdx + 1) + ' / ' + pages.length;
                animateSubtlePageTransition(delta);
                MaktabatiLayoutGuard.inspectAndHeal();
                reportPageToBridge(nextIdx + 1, pages.length);
                return;
              }
              const domPages = getDomPageElements();
              if (domPages.length > 0) {
                const curIdx = getCurrentPageIndex();
                const nextIdx = Math.max(0, Math.min(domPages.length - 1, curIdx + delta));
                if (nextIdx === curIdx) return;
                for (let i = 0; i < domPages.length; i++) {
                  if (i === nextIdx) domPages[i].classList.add('active');
                  else domPages[i].classList.remove('active');
                }
                const counterEl = document.getElementById('pageCount') || document.getElementById('pageCounter');
                if (counterEl) counterEl.textContent = (nextIdx + 1) + ' / ' + domPages.length;
                animateSubtlePageTransition(delta);
                MaktabatiLayoutGuard.inspectAndHeal();
                reportPageToBridge(nextIdx + 1, domPages.length);
              }
            };

            window.__maktabatiGoToDomPage = function(targetZeroBasedIdx) {
              try {
                if (typeof window.goToPage === 'function') {
                  window.goToPage(targetZeroBasedIdx);
                  setTimeout(syncDomActivePageWithBridge, 30);
                  return;
                }
                const domPages = getDomPageElements();
                if (domPages.length > 0) {
                  const target = Math.max(0, Math.min(domPages.length - 1, Number(targetZeroBasedIdx) || 0));
                  const cur = getCurrentPageIndex();
                  const diff = target - cur;
                  if (diff !== 0) {
                    const btnSelector = diff > 0
                      ? '#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                      : '#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
                    const btn = document.querySelector(btnSelector);
                    if (btn) {
                      const steps = Math.abs(diff);
                      for (let s = 0; s < steps; s++) {
                        if (!btn.disabled) btn.click();
                      }
                      animateSubtlePageTransition(diff);
                      setTimeout(syncDomActivePageWithBridge, 30);
                      return;
                    }
                  }
                  for (let i = 0; i < domPages.length; i++) {
                    if (i === target) domPages[i].classList.add('active');
                    else domPages[i].classList.remove('active');
                  }
                  const counterEl = document.getElementById('pageCount') || document.getElementById('pageCounter');
                  if (counterEl) counterEl.textContent = (target + 1) + ' / ' + domPages.length;
                  if (diff !== 0) animateSubtlePageTransition(diff);
                  reportPageToBridge(target + 1, domPages.length);
                }
              } catch (e) {}
            };

            let domSyncObserverAttached = false;
            let domSyncDebounceTimer = null;
            function scheduleDomPageSync() {
              if (domSyncDebounceTimer) clearTimeout(domSyncDebounceTimer);
              domSyncDebounceTimer = setTimeout(function() {
                syncDomActivePageWithBridge();
              }, 25);
            }

            function attachLiveDomPageObserver() {
              if (domSyncObserverAttached || !document.body) return;
              domSyncObserverAttached = true;
              try {
                const observer = new MutationObserver(function() {
                  scheduleDomPageSync();
                });
                observer.observe(document.body, {
                  attributes: true,
                  attributeFilter: ['class'],
                  subtree: true,
                  childList: true,
                  characterData: true
                });
              } catch (e) {}
              document.addEventListener('click', function() {
                scheduleDomPageSync();
              }, { passive: true });
              scheduleDomPageSync();
            }

            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', function() {
                purgeLegacyDomArtifacts();
                attachLiveDomPageObserver();
                setTimeout(healBlankPageIfNeeded, 20);
              });
            } else {
              purgeLegacyDomArtifacts();
              attachLiveDomPageObserver();
              setTimeout(healBlankPageIfNeeded, 20);
            }

            function triggerUniversalPageTurn(delta) {
              try {
                const beforePage = getCurrentPageIndex();
                if (delta > 0 && typeof window.nextPage === 'function') {
                  window.nextPage();
                  animateSubtlePageTransition(delta);
                  setTimeout(function() {
                    syncDomActivePageWithBridge();
                    MaktabatiLayoutGuard.inspectAndHeal();
                  }, 25);
                  return true;
                }
                if (delta < 0 && typeof window.prevPage === 'function') {
                  window.prevPage();
                  animateSubtlePageTransition(delta);
                  setTimeout(function() {
                    syncDomActivePageWithBridge();
                    MaktabatiLayoutGuard.inspectAndHeal();
                  }, 25);
                  return true;
                }
                if (typeof window.changePage === 'function') {
                  window.changePage(delta);
                  animateSubtlePageTransition(delta);
                  setTimeout(function() {
                    syncDomActivePageWithBridge();
                    MaktabatiLayoutGuard.inspectAndHeal();
                  }, 25);
                  return true;
                }
                if (typeof window.goToPage === 'function') {
                  window.goToPage(beforePage + delta);
                  animateSubtlePageTransition(delta);
                  setTimeout(function() {
                    syncDomActivePageWithBridge();
                    MaktabatiLayoutGuard.inspectAndHeal();
                  }, 25);
                  return true;
                }
                const btnSelector = delta > 0
                  ? '#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                  : '#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
                const btn = document.querySelector(btnSelector);
                if (btn && !btn.disabled) {
                  btn.click();
                  animateSubtlePageTransition(delta);
                  setTimeout(function() {
                    syncDomActivePageWithBridge();
                    MaktabatiLayoutGuard.inspectAndHeal();
                  }, 25);
                  return true;
                }
                window.__maktabatiTurn(delta);
                return true;
              } catch (e) {}
              return false;
            }

            // =========================================================================
            // 2. CLEAN TOUCH NAVIGATION (SUBTLE SMOOTH FADE & MICRO-SLIDE)
            //    + 2-FINGER FLUID REFLOW PINCH-TO-ZOOM
            // =========================================================================
            const INTERACTIVE_PROTECTED_SELECTOR =
              'input,textarea,select,button,a,label,audio,video,.flip-card,.quiz-option,.toc-drawer,.drawer,.modal,.overlay';

            function isProtectedInteractiveTarget(tgt) {
              if (!tgt || !tgt.closest) return false;
              return Boolean(tgt.closest(INTERACTIVE_PROTECTED_SELECTOR));
            }

            function getScrollMetrics(el) {
              let node = el;
              while (node && node !== document.body && node !== document.documentElement) {
                if (node.scrollHeight > node.clientHeight + 6) {
                  return {
                    top: node.scrollTop <= 4,
                    bottom: node.scrollTop + node.clientHeight >= node.scrollHeight - 6
                  };
                }
                node = node.parentElement;
              }
              const docEl = document.scrollingElement || document.documentElement;
              const winTop = window.scrollY || docEl.scrollTop || 0;
              const maxScroll = Math.max(0, (docEl.scrollHeight || 0) - (window.innerHeight || docEl.clientHeight || 0));
              return { top: winTop <= 4, bottom: winTop >= maxScroll - 6 };
            }

            let tStartX = 0, tStartY = 0, tStartPage = 0, tAtTop = true, tAtBottom = true, tIgnore = false;
            let isPinching = false, pinchStartDist = 0, pinchStartZoom = 100;

            function getTouchDist(t0, t1) {
              const dx = t0.clientX - t1.clientX;
              const dy = t0.clientY - t1.clientY;
              return Math.hypot(dx, dy);
            }

            document.addEventListener('touchstart', function(e) {
              if (!e.touches) return;

              // 2-Finger Pinch-to-Zoom (Fluid Reflow Zoom)
              if (e.touches.length >= 2) {
                tIgnore = true;
                isPinching = true;
                pinchStartDist = getTouchDist(e.touches[0], e.touches[1]);
                pinchStartZoom = currentZoomPercent;
                return;
              }

              isPinching = false;
              if (e.touches.length !== 1) { tIgnore = true; return; }
              const tgt = e.target;
              if (isProtectedInteractiveTarget(tgt)) {
                tIgnore = true;
                return;
              }

              tIgnore = false;
              tStartX = e.touches[0].clientX;
              tStartY = e.touches[0].clientY;
              tStartPage = getCurrentPageIndex();
              const m = getScrollMetrics(tgt);
              tAtTop = m.top;
              tAtBottom = m.bottom;
            }, { passive: true });

            document.addEventListener('touchmove', function(e) {
              if (!e.touches) return;

              if (isPinching && e.touches.length >= 2) {
                const dist = getTouchDist(e.touches[0], e.touches[1]);
                if (pinchStartDist > 20) {
                  const ratio = dist / pinchStartDist;
                  const nextZoom = Math.round(pinchStartZoom * ratio);
                  if (Math.abs(nextZoom - currentZoomPercent) >= 2) {
                    window.__maktabatiSetFluidZoom(nextZoom, false);
                  }
                }
              }
            }, { passive: true });

            document.addEventListener('touchend', function(e) {
              if (isPinching) {
                if (!e.touches || e.touches.length < 2) {
                  isPinching = false;
                }
                return;
              }

              if (tIgnore || !e.changedTouches || e.changedTouches.length !== 1) {
                return;
              }

              const dx = e.changedTouches[0].clientX - tStartX;
              const dy = e.changedTouches[0].clientY - tStartY;
              const absX = Math.abs(dx), absY = Math.abs(dy);
              const vw = Math.max(240, window.innerWidth || 360);
              const swipeThreshold = Math.min(vw * 0.22, 64);

              const completedHorizontalSwipe = absX >= swipeThreshold && absX >= absY * 0.8;
              const completedVerticalSwipe = absY > absX * 1.2 && absY >= 58 && ((dy < 0 && tAtBottom) || (dy > 0 && tAtTop));

              if (!completedHorizontalSwipe && !completedVerticalSwipe) {
                return;
              }

              setTimeout(function() {
                if (getCurrentPageIndex() !== tStartPage) {
                  // Book template handled the swipe itself; apply subtle micro-slide feedback
                  const dirAttr0 = (document.documentElement.getAttribute('dir') || document.body.getAttribute('dir') || 'rtl').toLowerCase();
                  const isRtl0 = dirAttr0 === 'rtl';
                  const delta0 = completedHorizontalSwipe ? (isRtl0 ? (dx > 0 ? 1 : -1) : (dx < 0 ? 1 : -1)) : (dy < 0 ? 1 : -1);
                  animateSubtlePageTransition(delta0);
                  return;
                }
                const dirAttr = (document.documentElement.getAttribute('dir') || document.body.getAttribute('dir') || 'rtl').toLowerCase();
                const isRtl = dirAttr === 'rtl';
                if (completedHorizontalSwipe) {
                  const delta = isRtl ? (dx > 0 ? 1 : -1) : (dx < 0 ? 1 : -1);
                  triggerUniversalPageTurn(delta);
                } else if (completedVerticalSwipe) {
                  if (dy < 0 && tAtBottom) {
                    triggerUniversalPageTurn(1);
                  } else if (dy > 0 && tAtTop) {
                    triggerUniversalPageTurn(-1);
                  }
                }
              }, 16);
            }, { passive: true });
          } catch (e) {}
        })();
        </script>
        """.trimIndent()
    }

    /**
     * Fast O(N) tag stripper by element id without backtracking regexes.
     */
    private fun stripElementById(html: String, tagName: String, idPrefix: String): String {
        val idIndex = html.indexOf(idPrefix, ignoreCase = true)
        if (idIndex < 0) return html
        val openStart = html.lastIndexOf("<$tagName", idIndex, ignoreCase = true)
        if (openStart < 0) return html
        val closeTag = "</$tagName>"
        val closeEnd = html.indexOf(closeTag, idIndex, ignoreCase = true)
        if (closeEnd < 0) return html
        return html.substring(0, openStart) + html.substring(closeEnd + closeTag.length)
    }

    fun injectIntoHtml(rawHtml: String, bookId: String, pageCount: Int): String {
        var cleanedHtml = stripElementById(rawHtml, "style", "maktabati-perf-booster")
        cleanedHtml = stripElementById(cleanedHtml, "script", "maktabati-storage-isolation")

        // Normalize dvh/svh/lvh units inside <style> blocks to standard vh so all Android System WebViews render 100% height
        val headEndIdx = cleanedHtml.indexOf("</head>", ignoreCase = true)
        if (headEndIdx > 0) {
            val headPart = cleanedHtml.substring(0, headEndIdx).replace(DVH_UNIT_REGEX, "$1vh")
            cleanedHtml = headPart + cleanedHtml.substring(headEndIdx)
        } else {
            cleanedHtml = cleanedHtml.replace(DVH_UNIT_REGEX, "$1vh")
        }

        val snippet = buildInjectedSnippet(bookId, pageCount)

        // Inject right before </head> so <meta charset="utf-8"> and <meta name="viewport"> remain at byte 0
        val targetHeadClose = cleanedHtml.indexOf("</head>", ignoreCase = true)
        if (targetHeadClose >= 0) {
            return cleanedHtml.substring(0, targetHeadClose) + "\n" + snippet + "\n" + cleanedHtml.substring(targetHeadClose)
        }

        val headOpenMatch = Regex("<head[^>]*>", RegexOption.IGNORE_CASE).find(cleanedHtml)
        return if (headOpenMatch != null) {
            val p = headOpenMatch.range.last + 1
            cleanedHtml.substring(0, p) + "\n" + snippet + "\n" + cleanedHtml.substring(p)
        } else {
            snippet + "\n" + cleanedHtml
        }
    }
}
