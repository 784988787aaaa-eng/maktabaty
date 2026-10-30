package com.example.data.sandbox

/**
 * High-speed, non-intrusive preprocessor & runtime engine for standalone HTML/CSS/JS books:
 * 1. Preserves `<meta charset="utf-8">` and `<meta name="viewport">` at byte 0 by injecting right before `</head>`.
 * 2. Normalizes `dvh`/`svh`/`lvh` viewport units to `vh` and enforces 100vh fallback on `.app`.
 * 3. Provides crash-proof per-book `localStorage` isolation + full `window.localStorage` polyfill.
 * 4. Fluid Reflow Zoom Engine (`Fluid Reflow Zoom`): pinch-to-zoom & button zoom (`75%`–`200%`) that updates
 *    `--reader-font`, `--reader-font-size`, and `--font-scale` so text, cards, quotes, and ornaments reflow
 *    inside the page width without rigid PDF-like horizontal clipping, plus a sleek floating top badge with
 *    a quick `"100% الوضع المثالي"` reset button.
 * 5. Layout Distortion Guard (`MaktabatiLayoutGuard`): auto-heals elements wider than `.page-canvas`
 *    (`el.scrollWidth > canvas.clientWidth + 8`) and shows a Smart Warning Toast Banner when reaching maximum
 *    harmonious zoom.
 * 6. 4-Corner Interactive Page-Curl Physics Engine (`MaktabatiPageCurlEngine`): 60fps physical paper curl from
 *    any of the 4 corners (Top-Right, Bottom-Right, Top-Left, Bottom-Left) or edges with backside fold gradient,
 *    dynamic crease/drop shadow, spring-back or quarter-width completion, Web Audio API natural paper flip sound,
 *    and interactive element protection (`input`, `textarea`, `select`, `button`, `.flip-card`, `.quiz-option`).
 */
object SandboxHtmlInjector {

    private val DVH_UNIT_REGEX = Regex("(\\d+(?:\\.\\d+)?)[dsl]vh\\b", RegexOption.IGNORE_CASE)

    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
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
            max-width: 100% !important;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
            -webkit-overflow-scrolling: touch;
          }
          .page-canvas, .page, .book-page, .page-container {
            max-width: 100% !important;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
            position: relative;
            transform-origin: center center;
          }
          .maktabati-gpu-hint {
            will-change: transform, opacity;
            transform: translateZ(0);
          }
          #pageInnerContent, .page-body, .page-content {
            max-width: 100% !important;
            overflow-x: hidden !important;
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
            box-sizing: border-box !important;
            overflow-wrap: anywhere;
            word-break: break-word;
            font-size: calc(1em * var(--reader-font, 1));
          }
          #pageInnerContent *, .page-body *, .page-content *, .page-canvas * {
            max-width: 100%;
            box-sizing: border-box;
            overflow-wrap: anywhere;
          }
          #pageInnerContent img, .page-body img, .page-content img,
          #pageInnerContent svg, .page-body svg, .page-content svg,
          #pageInnerContent video, .page-body video, .page-content video {
            max-width: 100% !important;
            height: auto !important;
          }
          #pageInnerContent table, .page-body table, .page-content table {
            display: block;
            max-width: 100% !important;
            overflow-x: auto;
            box-sizing: border-box !important;
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

          /* Floating Fluid Reflow Zoom Badge (Top) */
          #maktabati-zoom-badge {
            position: fixed;
            top: 12px;
            left: 50%;
            transform: translateX(-50%) translateY(-16px);
            z-index: 9998;
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 6px 14px;
            border-radius: 999px;
            background: rgba(20, 18, 24, 0.90);
            color: #FFFDF9;
            border: 1px solid rgba(217, 155, 67, 0.55);
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.28);
            font-family: system-ui, -apple-system, sans-serif;
            font-size: 13px;
            font-weight: 700;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.22s ease, transform 0.22s ease;
            direction: rtl;
          }
          #maktabati-zoom-badge.visible {
            opacity: 1;
            pointer-events: auto;
            transform: translateX(-50%) translateY(0);
          }
          #maktabati-zoom-badge button {
            border: 0;
            border-radius: 999px;
            padding: 3px 10px;
            background: linear-gradient(135deg, #D97706, #B45309);
            color: #FFFFFF;
            font-size: 11.5px;
            font-weight: 700;
            cursor: pointer;
          }

          /* Smart Layout Distortion Warning Toast Banner (Bottom) */
          #maktabati-distortion-toast {
            position: fixed;
            bottom: 58px;
            left: 12px;
            right: 12px;
            max-width: 520px;
            margin: 0 auto;
            z-index: 9999;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 10px;
            padding: 10px 14px;
            border-radius: 14px;
            background: rgba(28, 20, 17, 0.95);
            color: #FEF3C7;
            border: 1.5px solid #F59E0B;
            box-shadow: 0 10px 28px rgba(0, 0, 0, 0.38);
            font-family: system-ui, -apple-system, sans-serif;
            font-size: 12.5px;
            line-height: 1.5;
            direction: rtl;
            opacity: 0;
            pointer-events: none;
            transform: translateY(20px);
            transition: opacity 0.25s ease, transform 0.25s ease;
          }
          #maktabati-distortion-toast.visible {
            opacity: 1;
            pointer-events: auto;
            transform: translateY(0);
          }
          #maktabati-distortion-toast button {
            flex-shrink: 0;
            border: 0;
            border-radius: 10px;
            padding: 6px 12px;
            background: #F59E0B;
            color: #1C1411;
            font-weight: 800;
            font-size: 12px;
            cursor: pointer;
          }

          /* 4-Corner Interactive Page-Curl Physics Overlay */
          #maktabati-page-curl-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            pointer-events: none;
            z-index: 9990;
            overflow: hidden;
            opacity: 0;
            transition: opacity 0.12s ease;
          }
          #maktabati-page-curl-overlay.active {
            opacity: 1;
          }
          #maktabati-curl-shadow {
            position: absolute;
            pointer-events: none;
            will-change: transform, opacity, width, height;
            background: radial-gradient(ellipse at center, rgba(0,0,0,0.34) 0%, rgba(0,0,0,0.16) 45%, rgba(0,0,0,0) 78%);
            filter: blur(6px);
          }
          #maktabati-curl-backside {
            position: absolute;
            pointer-events: none;
            will-change: transform, width, height, border-radius;
            background: linear-gradient(
              135deg,
              rgba(255, 253, 248, 0.98) 0%,
              rgba(244, 235, 218, 0.97) 48%,
              rgba(222, 206, 180, 0.94) 82%,
              rgba(255, 255, 255, 0.96) 100%
            );
            border: 1px solid rgba(196, 145, 72, 0.42);
            box-shadow:
              -8px 8px 22px rgba(0, 0, 0, 0.28),
              inset 0 0 18px rgba(196, 145, 72, 0.16);
          }
          [data-theme="night"] #maktabati-curl-backside,
          [data-theme="dark"] #maktabati-curl-backside {
            background: linear-gradient(
              135deg,
              rgba(38, 34, 48, 0.98) 0%,
              rgba(28, 24, 36, 0.96) 55%,
              rgba(18, 15, 24, 0.94) 85%,
              rgba(55, 48, 68, 0.95) 100%
            );
            border-color: rgba(226, 176, 94, 0.35);
          }
        </style>
        <script id="maktabati-storage-isolation">
        (function(){
          if (window.__maktabatiV5Initialized) return;
          window.__maktabatiV5Initialized = true;
          window.__maktabatiV4Initialized = true;
          window.__maktabatiV3Initialized = true;
          try {
            const P = "$safePrefix", TOTAL = ${pageCount.coerceAtLeast(1)};
            const memStore = {};
            const key = function(k) { k = String(k); return k.indexOf(P) === 0 ? k : P + k; };

            function notifyBridgeFromJson(sv) {
              try {
                if (!sv || sv.charAt(0) !== '{') return;
                const d = JSON.parse(sv);
                if (!d) return;
                const pNum = typeof d.currentPage === 'number' ? d.currentPage : (typeof d.page === 'number' ? d.page : null);
                if (pNum !== null) {
                  window.__maktabatiLastSavedPage = pNum;
                  if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                    const data = getBookData();
                    const pages = getPagesArray(data);
                    const n = pages ? pages.length : TOTAL;
                    window.MaktabatiBridge.onPageChanged(pNum + 1, n);
                  }
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
            // 1. FLUID REFLOW ZOOM ENGINE & 2. LAYOUT DISTORTION GUARD (MaktabatiLayoutGuard)
            // =========================================================================
            let currentZoomPercent = 100;
            let zoomBadgeTimer = null;
            let distortionToastTimer = null;
            const DISTORTION_WARNING_TEXT = "تنبيه: وصلتَ لأقصى حجم يحفظ جمال وتناسق عناصر هذا الكتاب — اضغط (100%) للعودة للعرض المثالي";

            function ensureHudElements() {
              if (!document.body) return;
              if (!document.getElementById('maktabati-zoom-badge')) {
                const badge = document.createElement('div');
                badge.id = 'maktabati-zoom-badge';
                badge.innerHTML =
                  '<span id="maktabati-zoom-label">100%</span>' +
                  '<button type="button" id="maktabati-zoom-reset-btn">100% الوضع المثالي</button>';
                document.body.appendChild(badge);
                const resetBtn = document.getElementById('maktabati-zoom-reset-btn');
                if (resetBtn) {
                  resetBtn.addEventListener('click', function(ev) {
                    ev.stopPropagation();
                    window.__maktabatiSetFluidZoom(100, false);
                  });
                }
              }
              if (!document.getElementById('maktabati-distortion-toast')) {
                const toast = document.createElement('div');
                toast.id = 'maktabati-distortion-toast';
                toast.innerHTML =
                  '<span>' + DISTORTION_WARNING_TEXT + '</span>' +
                  '<button type="button" id="maktabati-toast-reset-btn">100%</button>';
                document.body.appendChild(toast);
                const toastBtn = document.getElementById('maktabati-toast-reset-btn');
                if (toastBtn) {
                  toastBtn.addEventListener('click', function(ev) {
                    ev.stopPropagation();
                    toast.classList.remove('visible');
                    window.__maktabatiSetFluidZoom(100, false);
                  });
                }
              }
            }

            function showZoomBadge(percent) {
              ensureHudElements();
              const badge = document.getElementById('maktabati-zoom-badge');
              const label = document.getElementById('maktabati-zoom-label');
              if (!badge || !label) return;
              label.textContent = percent + '%';
              badge.classList.add('visible');
              if (zoomBadgeTimer) clearTimeout(zoomBadgeTimer);
              zoomBadgeTimer = setTimeout(function() {
                badge.classList.remove('visible');
              }, 2200);
            }

            const MaktabatiLayoutGuard = {
              showDistortionToast: function() {
                ensureHudElements();
                const toast = document.getElementById('maktabati-distortion-toast');
                if (toast) {
                  toast.classList.add('visible');
                  if (distortionToastTimer) clearTimeout(distortionToastTimer);
                  distortionToastTimer = setTimeout(function() {
                    toast.classList.remove('visible');
                  }, 5200);
                }
                if (window.MaktabatiBridge && window.MaktabatiBridge.onLayoutDistortionWarning) {
                  window.MaktabatiBridge.onLayoutDistortionWarning(DISTORTION_WARNING_TEXT);
                }
              },
              hideDistortionToast: function() {
                const toast = document.getElementById('maktabati-distortion-toast');
                if (toast) toast.classList.remove('visible');
              },
              inspectAndHeal: function(zoomPct) {
                try {
                  const canvas = getActivePageCanvas();
                  if (!canvas) return { healedCount: 0, distorted: false };
                  const canvasWidth = canvas.clientWidth || window.innerWidth || 360;
                  let healedCount = 0;
                  let unhealedOverflowCount = 0;

                  const candidates = canvas.querySelectorAll(
                    'table, img, svg, video, pre, code, blockquote, .quote-box, .flip-card, .insight-card, .quiz-card, .quiz-option, div, p, section, article, h1, h2, h3, h4'
                  );
                  for (let i = 0; i < candidates.length; i++) {
                    const el = candidates[i];
                    if (!el || el.id === 'maktabati-zoom-badge' || el.id === 'maktabati-distortion-toast') continue;
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
                      if (el.scrollWidth > canvasWidth + 14) {
                        unhealedOverflowCount++;
                      }
                    }
                  }

                  const activeZoom = typeof zoomPct === 'number' ? zoomPct : currentZoomPercent;
                  const isCrowdedAtHighZoom = activeZoom >= 170 || (activeZoom >= 145 && unhealedOverflowCount > 0);
                  if (isCrowdedAtHighZoom) {
                    MaktabatiLayoutGuard.showDistortionToast();
                    return { healedCount: healedCount, distorted: true };
                  } else if (activeZoom <= 115) {
                    MaktabatiLayoutGuard.hideDistortionToast();
                  }
                  return { healedCount: healedCount, distorted: false };
                } catch (e) {
                  return { healedCount: 0, distorted: false };
                }
              }
            };
            window.MaktabatiLayoutGuard = MaktabatiLayoutGuard;

            window.__maktabatiSetFluidZoom = function(percent, fromNative) {
              try {
                let target = Math.round(Number(percent) || 100);
                if (target < 75) target = 75;
                if (target > 200) target = 200;

                // Check if zooming up past safe layout limit causes crowding/distortion
                if (target > 165) {
                  MaktabatiLayoutGuard.showDistortionToast();
                  target = Math.min(target, 165);
                }

                currentZoomPercent = target;
                const scale = +(target / 100).toFixed(2);
                const root = document.documentElement;
                root.style.setProperty('--reader-font', String(scale));
                root.style.setProperty('--reader-font-size', target + '%');
                root.style.setProperty('--font-scale', String(scale));

                try {
                  if (typeof state !== 'undefined' && state) {
                    state.fontScale = scale;
                  }
                  if (window.state) {
                    window.state.fontScale = scale;
                  }
                } catch (e) {}

                const bodyContainers = document.querySelectorAll('#pageInnerContent, #pageBody, .page-body, .page-content');
                for (let i = 0; i < bodyContainers.length; i++) {
                  bodyContainers[i].style.fontSize = 'calc(1.08rem * ' + scale + ')';
                  bodyContainers[i].style.lineHeight = scale > 1.25 ? '1.95' : '1.85';
                }

                showZoomBadge(target);
                MaktabatiLayoutGuard.inspectAndHeal(target);

                if (!fromNative && window.MaktabatiBridge && window.MaktabatiBridge.onZoomChanged) {
                  window.MaktabatiBridge.onZoomChanged(target);
                }
              } catch (e) {}
            };

            // Zero-reflow check: only acts if the book template left its container completely empty
            function healBlankPageIfNeeded() {
              try {
                let container = document.getElementById('pageCanvas')
                  || document.getElementById('pageInnerContent')
                  || document.getElementById('pageContent')
                  || document.querySelector('.page-canvas, .page.active .page-body, .page.active, .book-page.active');

                if (container && (container.childElementCount > 0 || (container.textContent || '').trim().length > 0)) {
                  MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent);
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
                MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent);
                if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                  window.MaktabatiBridge.onPageChanged(idx + 1, pages.length);
                }
              } catch (e) {}
            }

            window.__maktabatiTurn = function(delta) {
              const data = getBookData();
              const pages = getPagesArray(data);
              if (!pages || !pages.length) return;
              const nextIdx = Math.max(0, Math.min(pages.length - 1, getCurrentPageIndex() + delta));
              setCurrentPageIndex(nextIdx);
              const container = document.getElementById('pageInnerContent') || document.getElementById('pageContent') || document.getElementById('pageCanvas');
              if (container) {
                container.innerHTML = buildPageHtml(pages[nextIdx], nextIdx);
              }
              const counterEl = document.getElementById('pageCounter') || document.getElementById('pageCount');
              if (counterEl) counterEl.textContent = (nextIdx + 1) + ' / ' + pages.length;
              MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent);
              if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                window.MaktabatiBridge.onPageChanged(nextIdx + 1, pages.length);
              }
            };

            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', function() {
                ensureHudElements();
                setTimeout(healBlankPageIfNeeded, 20);
              });
            } else {
              ensureHudElements();
              setTimeout(healBlankPageIfNeeded, 20);
            }

            function triggerUniversalPageTurn(delta) {
              try {
                const beforePage = getCurrentPageIndex();
                if (delta > 0 && typeof window.nextPage === 'function') {
                  window.nextPage();
                  setTimeout(function() { MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent); }, 25);
                  return true;
                }
                if (delta < 0 && typeof window.prevPage === 'function') {
                  window.prevPage();
                  setTimeout(function() { MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent); }, 25);
                  return true;
                }
                if (typeof window.changePage === 'function') {
                  window.changePage(delta);
                  setTimeout(function() { MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent); }, 25);
                  return true;
                }
                if (typeof window.goToPage === 'function') {
                  window.goToPage(beforePage + delta);
                  setTimeout(function() { MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent); }, 25);
                  return true;
                }
                const btnSelector = delta > 0
                  ? '#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                  : '#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
                const btn = document.querySelector(btnSelector);
                if (btn && !btn.disabled) {
                  btn.click();
                  setTimeout(function() { MaktabatiLayoutGuard.inspectAndHeal(currentZoomPercent); }, 25);
                  return true;
                }
                window.__maktabatiTurn(delta);
                return true;
              } catch (e) {}
              return false;
            }

            // =========================================================================
            // 3. 4-CORNER INTERACTIVE PAGE-CURL PHYSICS ENGINE (MaktabatiPageCurlEngine)
            //    + 2-FINGER FLUID REFLOW PINCH-TO-ZOOM
            // =========================================================================
            let audioCtx = null;
            function playPaperFlipSound() {
              try {
                const AudioContextClass = window.AudioContext || window.webkitAudioContext;
                if (!AudioContextClass) return;
                if (!audioCtx) audioCtx = new AudioContextClass();
                if (audioCtx.state === 'suspended') audioCtx.resume();

                const duration = 0.11;
                const sampleRate = audioCtx.sampleRate || 44100;
                const frameCount = Math.floor(sampleRate * duration);
                const buffer = audioCtx.createBuffer(1, frameCount, sampleRate);
                const data = buffer.getChannelData(0);
                for (let i = 0; i < frameCount; i++) {
                  const env = Math.sin((i / frameCount) * Math.PI);
                  data[i] = (Math.random() * 2 - 1) * env * 0.22;
                }

                const noise = audioCtx.createBufferSource();
                noise.buffer = buffer;

                const filter = audioCtx.createBiquadFilter();
                filter.type = 'bandpass';
                filter.frequency.setValueAtTime(920, audioCtx.currentTime);
                filter.frequency.exponentialRampToValueAtTime(340, audioCtx.currentTime + duration);
                filter.Q.value = 1.4;

                const gain = audioCtx.createGain();
                gain.gain.setValueAtTime(0.01, audioCtx.currentTime);
                gain.gain.linearRampToValueAtTime(0.18, audioCtx.currentTime + 0.025);
                gain.gain.exponentialRampToValueAtTime(0.005, audioCtx.currentTime + duration);

                noise.connect(filter);
                filter.connect(gain);
                gain.connect(audioCtx.destination);
                noise.start();
              } catch (e) {}
            }

            function ensureCurlOverlay() {
              if (!document.body) return null;
              let overlay = document.getElementById('maktabati-page-curl-overlay');
              if (!overlay) {
                overlay = document.createElement('div');
                overlay.id = 'maktabati-page-curl-overlay';
                overlay.innerHTML =
                  '<div id="maktabati-curl-shadow"></div>' +
                  '<div id="maktabati-curl-backside"></div>';
                document.body.appendChild(overlay);
              }
              return overlay;
            }

            const INTERACTIVE_PROTECTED_SELECTOR =
              'input,textarea,select,button,a,label,audio,video,.flip-card,.quiz-option,.toc-drawer,.drawer,.modal,.overlay,#maktabati-zoom-badge,#maktabati-distortion-toast';

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

            const MaktabatiPageCurlEngine = {
              activeCorner: 'NONE', // 'TOP_RIGHT', 'BOTTOM_RIGHT', 'TOP_LEFT', 'BOTTOM_LEFT'
              startX: 0,
              startY: 0,
              curX: 0,
              curY: 0,
              isDragging: false,
              rafId: null,

              detectCorner: function(x, y, vw, vh) {
                const isRight = x >= vw * 0.5;
                const isTop = y < vh * 0.5;
                if (isRight && isTop) return 'TOP_RIGHT';
                if (isRight && !isTop) return 'BOTTOM_RIGHT';
                if (!isRight && isTop) return 'TOP_LEFT';
                return 'BOTTOM_LEFT';
              },

              renderFrame: function() {
                MaktabatiPageCurlEngine.rafId = null;
                if (!MaktabatiPageCurlEngine.isDragging) return;
                const overlay = ensureCurlOverlay();
                if (!overlay) return;
                const backside = document.getElementById('maktabati-curl-backside');
                const shadow = document.getElementById('maktabati-curl-shadow');
                if (!backside || !shadow) return;

                const vw = Math.max(280, window.innerWidth || 360);
                const vh = Math.max(400, window.innerHeight || 640);
                const dx = MaktabatiPageCurlEngine.curX - MaktabatiPageCurlEngine.startX;
                const dy = MaktabatiPageCurlEngine.curY - MaktabatiPageCurlEngine.startY;
                const absDx = Math.abs(dx);
                const absDy = Math.abs(dy);

                if (absDx < 10 && absDy < 10) {
                  overlay.classList.remove('active');
                  return;
                }

                overlay.classList.add('active');
                const corner = MaktabatiPageCurlEngine.activeCorner;
                const isTopCorner = corner === 'TOP_RIGHT' || corner === 'TOP_LEFT';
                const isRightCorner = corner === 'TOP_RIGHT' || corner === 'BOTTOM_RIGHT';

                const curlProgress = Math.min(1, Math.hypot(dx, dy) / (vw * 0.72));
                const foldSize = Math.max(24, Math.min(vw * 0.65, curlProgress * vw * 0.58));
                const angleRad = Math.atan2(dy, dx);
                const angleDeg = (angleRad * 180 / Math.PI);
                const clampedTilt = Math.max(-28, Math.min(28, (dy / vh) * 42 * (isTopCorner ? 1 : -1)));

                backside.style.transition = 'none';
                shadow.style.transition = 'none';
                backside.style.width = foldSize + 'px';
                backside.style.height = (foldSize * 1.12) + 'px';

                shadow.style.width = (foldSize * 1.35) + 'px';
                shadow.style.height = (foldSize * 1.45) + 'px';
                shadow.style.opacity = String(Math.min(0.85, 0.25 + curlProgress * 0.65));

                // Anchor curl geometry specifically to the grabbed corner (TR, BR, TL, BL)
                backside.style.top = isTopCorner ? '0px' : 'auto';
                backside.style.bottom = isTopCorner ? 'auto' : '0px';
                backside.style.right = isRightCorner ? '0px' : 'auto';
                backside.style.left = isRightCorner ? 'auto' : '0px';

                shadow.style.top = isTopCorner ? '0px' : 'auto';
                shadow.style.bottom = isTopCorner ? 'auto' : '0px';
                shadow.style.right = isRightCorner ? '0px' : 'auto';
                shadow.style.left = isRightCorner ? 'auto' : '0px';

                if (corner === 'TOP_RIGHT') {
                  backside.style.borderRadius = '0 0 0 90%';
                  backside.style.transformOrigin = 'top right';
                  backside.style.transform = 'translate3d(' + (-foldSize * 0.18) + 'px, ' + (foldSize * 0.12) + 'px, 0) rotate(' + (clampedTilt - 8) + 'deg)';
                } else if (corner === 'BOTTOM_RIGHT') {
                  backside.style.borderRadius = '90% 0 0 0';
                  backside.style.transformOrigin = 'bottom right';
                  backside.style.transform = 'translate3d(' + (-foldSize * 0.18) + 'px, ' + (-foldSize * 0.12) + 'px, 0) rotate(' + (clampedTilt + 8) + 'deg)';
                } else if (corner === 'TOP_LEFT') {
                  backside.style.borderRadius = '0 0 90% 0';
                  backside.style.transformOrigin = 'top left';
                  backside.style.transform = 'translate3d(' + (foldSize * 0.18) + 'px, ' + (foldSize * 0.12) + 'px, 0) rotate(' + (-clampedTilt + 8) + 'deg)';
                } else {
                  backside.style.borderRadius = '0 90% 0 0';
                  backside.style.transformOrigin = 'bottom left';
                  backside.style.transform = 'translate3d(' + (foldSize * 0.18) + 'px, ' + (-foldSize * 0.12) + 'px, 0) rotate(' + (-clampedTilt - 8) + 'deg)';
                }

                const pageCard = getActivePageCanvas();
                if (pageCard && pageCard !== document.body) {
                  const rotY = (dx / vw) * 14;
                  const rotX = isTopCorner ? -(Math.abs(dx) / vw) * 4 : (Math.abs(dx) / vw) * 4;
                  pageCard.style.transition = 'none';
                  pageCard.style.transform = 'perspective(1200px) rotateY(' + rotY.toFixed(2) + 'deg) rotateX(' + rotX.toFixed(2) + 'deg)';
                }
              },

              releaseCurl: function(completed) {
                MaktabatiPageCurlEngine.isDragging = false;
                if (MaktabatiPageCurlEngine.rafId) {
                  cancelAnimationFrame(MaktabatiPageCurlEngine.rafId);
                  MaktabatiPageCurlEngine.rafId = null;
                }
                const overlay = document.getElementById('maktabati-page-curl-overlay');
                const backside = document.getElementById('maktabati-curl-backside');
                const shadow = document.getElementById('maktabati-curl-shadow');
                const pageCard = getActivePageCanvas();

                if (pageCard && pageCard !== document.body) {
                  pageCard.style.transition = 'transform 0.24s cubic-bezier(0.22, 1, 0.36, 1)';
                  pageCard.style.transform = 'none';
                }

                if (!overlay || !backside || !shadow) return;
                backside.style.transition = 'all 0.24s cubic-bezier(0.22, 1, 0.36, 1)';
                shadow.style.transition = 'all 0.24s cubic-bezier(0.22, 1, 0.36, 1)';

                if (completed) {
                  playPaperFlipSound();
                  const vw = Math.max(280, window.innerWidth || 360);
                  backside.style.width = (vw * 0.95) + 'px';
                  backside.style.height = (vw * 0.95) + 'px';
                  shadow.style.opacity = '0';
                } else {
                  backside.style.width = '0px';
                  backside.style.height = '0px';
                  shadow.style.opacity = '0';
                }

                setTimeout(function() {
                  if (overlay) overlay.classList.remove('active');
                }, 220);
              },

              playPaperFlipSound: playPaperFlipSound
            };
            window.MaktabatiPageCurlEngine = MaktabatiPageCurlEngine;

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
                MaktabatiPageCurlEngine.releaseCurl(false);
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

              const vw = window.innerWidth || 360;
              const vh = window.innerHeight || 640;
              MaktabatiPageCurlEngine.activeCorner = MaktabatiPageCurlEngine.detectCorner(tStartX, tStartY, vw, vh);
              MaktabatiPageCurlEngine.startX = tStartX;
              MaktabatiPageCurlEngine.startY = tStartY;
              MaktabatiPageCurlEngine.curX = tStartX;
              MaktabatiPageCurlEngine.curY = tStartY;
              MaktabatiPageCurlEngine.isDragging = true;
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
                return;
              }

              if (tIgnore || !MaktabatiPageCurlEngine.isDragging || e.touches.length !== 1) return;
              MaktabatiPageCurlEngine.curX = e.touches[0].clientX;
              MaktabatiPageCurlEngine.curY = e.touches[0].clientY;
              if (!MaktabatiPageCurlEngine.rafId) {
                MaktabatiPageCurlEngine.rafId = requestAnimationFrame(MaktabatiPageCurlEngine.renderFrame);
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
                MaktabatiPageCurlEngine.releaseCurl(false);
                return;
              }

              const dx = e.changedTouches[0].clientX - tStartX;
              const dy = e.changedTouches[0].clientY - tStartY;
              const absX = Math.abs(dx), absY = Math.abs(dy);
              const vw = Math.max(240, window.innerWidth || 360);
              const quarterWidth = vw * 0.25;

              const completedHorizontalCurl = absX >= Math.min(quarterWidth, 72) && absX >= absY * 0.75;
              const completedVerticalSwipe = absY > absX * 1.2 && absY >= 58 && ((dy < 0 && tAtBottom) || (dy > 0 && tAtTop));

              if (!completedHorizontalCurl && !completedVerticalSwipe) {
                // Spring-back to original position
                MaktabatiPageCurlEngine.releaseCurl(false);
                return;
              }

              MaktabatiPageCurlEngine.releaseCurl(true);

              setTimeout(function() {
                if (getCurrentPageIndex() !== tStartPage) {
                  return;
                }
                const dirAttr = (document.documentElement.getAttribute('dir') || document.body.getAttribute('dir') || 'rtl').toLowerCase();
                const isRtl = dirAttr === 'rtl';
                if (completedHorizontalCurl) {
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
