package com.example.data.sandbox

/**
 * High-speed, non-intrusive preprocessor for standalone HTML/CSS/JS books:
 * 1. Preserves `<meta charset="utf-8">` and `<meta name="viewport">` at byte 0 by injecting right before `</head>`.
 * 2. Normalizes `dvh`/`svh`/`lvh` viewport units to `vh` and enforces 100vh fallback on `.app` so embedded Android
 *    WebViews (including Huawei/Honor/older System WebViews) never collapse `.app` to 0px height.
 * 3. Provides crash-proof per-book `localStorage` isolation + full `window.localStorage` polyfill if origin blocks DOMStorage.
 * 4. Zero-reflow self-healing `BOOK_DATA` renderer + 4-way swipe fallback that stays completely out of the way
 *    when the book's own native JS engine is active.
 */
object SandboxHtmlInjector {

    private val DVH_UNIT_REGEX = Regex("(\\d+(?:\\.\\d+)?)[dsl]vh\\b", RegexOption.IGNORE_CASE)

    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
          html, body {
            width: 100%;
            height: 100%;
            min-height: 100vh;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
          }
          .app {
            width: 100%;
            height: 100vh;
            min-height: 100vh;
            max-height: 100vh;
            box-sizing: border-box;
          }
          .book-stage, .stage {
            width: 100%;
            box-sizing: border-box;
            -webkit-overflow-scrolling: touch;
          }
          .page-canvas, .page, .book-page, .page-container {
            box-sizing: border-box;
          }
          .maktabati-gpu-hint {
            will-change: transform, opacity;
            transform: translateZ(0);
          }
          #pageInnerContent, .page-body, .page-content {
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
            box-sizing: border-box;
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
          if (window.__maktabatiV4Initialized) return;
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

            function buildPageHtml(pageObj, idx) {
              if (pageHtmlCache[idx]) return pageHtmlCache[idx];
              if (!pageObj) return '';
              const parts = [];
              const badge = pageObj.chapterTag || pageObj.chapterNumber || pageObj.sectionTitle || pageObj.badge || ('صفحة ' + (idx + 1));
              const heading = pageObj.title || pageObj.heading || pageObj.name || '';
              const sub = pageObj.subtitle || '';

              if (badge) {
                parts.push('<div style="display:inline-block;align-self:center;padding:3px 12px;border-radius:99px;font-size:12px;font-weight:bold;opacity:0.88;border:1px solid currentColor;margin-bottom:8px;">' + badge + '</div>');
              }
              if (heading) {
                parts.push('<h2 style="margin:0 0 8px 0;line-height:1.45;text-align:center;font-size:21px;">' + heading + '</h2>');
              }
              if (sub && sub !== heading) {
                parts.push('<div style="text-align:center;opacity:0.8;font-size:14px;margin-bottom:10px;">' + sub + '</div>');
              }
              if (pageObj.ayah || pageObj.verse) {
                parts.push('<div style="margin:8px 0;padding:10px 14px;border-right:3px solid #D97706;background:rgba(217,119,6,0.08);border-radius:10px;text-align:center;font-weight:bold;font-size:16px;line-height:1.8;">' + (pageObj.ayah || pageObj.verse) + '</div>');
              }
              if (pageObj.hadith) {
                parts.push('<div style="margin:8px 0;padding:9px 13px;border-right:3px solid #047857;background:rgba(4,120,87,0.07);border-radius:10px;font-size:14.5px;line-height:1.7;">' + pageObj.hadith + '</div>');
              }
              if (typeof pageObj.content === 'string' && pageObj.content.trim()) {
                parts.push('<div style="line-height:1.85;font-size:15.5px;text-align:justify;">' + pageObj.content + '</div>');
              } else if (typeof pageObj.html === 'string' && pageObj.html.trim()) {
                parts.push('<div style="line-height:1.85;font-size:15.5px;">' + pageObj.html + '</div>');
              } else if (typeof pageObj.body === 'string' && pageObj.body.trim()) {
                parts.push('<p style="line-height:1.85;font-size:15.5px;text-align:justify;">' + pageObj.body + '</p>');
              } else if (typeof pageObj.text === 'string' && pageObj.text.trim()) {
                parts.push('<p style="line-height:1.85;font-size:15.5px;text-align:justify;">' + pageObj.text + '</p>');
              }
              if (Array.isArray(pageObj.paragraphs)) {
                pageObj.paragraphs.forEach(function(p) {
                  if (typeof p === 'string' && p.trim()) {
                    parts.push('<p style="line-height:1.85;font-size:15.5px;margin:0 0 10px 0;text-align:justify;">' + p + '</p>');
                  } else if (p && typeof p.text === 'string') {
                    parts.push('<p style="line-height:1.85;font-size:15.5px;margin:0 0 10px 0;text-align:justify;">' + p.text + '</p>');
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

            // Zero-reflow check: only acts if the book template left its container completely empty
            function healBlankPageIfNeeded() {
              try {
                let container = document.getElementById('pageCanvas')
                  || document.getElementById('pageInnerContent')
                  || document.getElementById('pageContent')
                  || document.querySelector('.page-canvas, .page.active .page-body, .page.active, .book-page.active');

                if (container && (container.childElementCount > 0 || (container.textContent || '').trim().length > 0)) {
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
              if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                window.MaktabatiBridge.onPageChanged(nextIdx + 1, pages.length);
              }
            };

            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', function() {
                setTimeout(healBlankPageIfNeeded, 20);
              });
            } else {
              setTimeout(healBlankPageIfNeeded, 20);
            }

            function triggerUniversalPageTurn(delta) {
              try {
                const beforePage = getCurrentPageIndex();
                if (delta > 0 && typeof window.nextPage === 'function') {
                  window.nextPage();
                  return true;
                }
                if (delta < 0 && typeof window.prevPage === 'function') {
                  window.prevPage();
                  return true;
                }
                if (typeof window.changePage === 'function') {
                  window.changePage(delta);
                  return true;
                }
                if (typeof window.goToPage === 'function') {
                  window.goToPage(beforePage + delta);
                  return true;
                }
                const btnSelector = delta > 0
                  ? '#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                  : '#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
                const btn = document.querySelector(btnSelector);
                if (btn && !btn.disabled) {
                  btn.click();
                  return true;
                }
                window.__maktabatiTurn(delta);
                return true;
              } catch (e) {}
              return false;
            }

            let tStartX = 0, tStartY = 0, tStartPage = 0, tAtTop = true, tAtBottom = true, tIgnore = false;
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

            document.addEventListener('touchstart', function(e) {
              if (!e.touches || e.touches.length !== 1) { tIgnore = true; return; }
              const tgt = e.target;
              if (tgt && tgt.closest && tgt.closest('button,a,input,textarea,select,label,.toc-drawer,.drawer,.modal,.overlay')) {
                tIgnore = true; return;
              }
              tIgnore = false;
              tStartX = e.touches[0].clientX;
              tStartY = e.touches[0].clientY;
              tStartPage = getCurrentPageIndex();
              const m = getScrollMetrics(tgt);
              tAtTop = m.top;
              tAtBottom = m.bottom;
            }, { passive: true });

            document.addEventListener('touchend', function(e) {
              if (tIgnore || !e.changedTouches || e.changedTouches.length !== 1) return;
              const dx = e.changedTouches[0].clientX - tStartX;
              const dy = e.changedTouches[0].clientY - tStartY;
              const absX = Math.abs(dx), absY = Math.abs(dy);
              if (absX < 48 && absY < 55) return;

              setTimeout(function() {
                if (getCurrentPageIndex() !== tStartPage) {
                  return;
                }
                const dirAttr = (document.documentElement.getAttribute('dir') || document.body.getAttribute('dir') || 'rtl').toLowerCase();
                const isRtl = dirAttr === 'rtl';
                if (absX > absY * 1.15 && absX >= 48) {
                  const delta = isRtl ? (dx > 0 ? 1 : -1) : (dx < 0 ? 1 : -1);
                  triggerUniversalPageTurn(delta);
                } else if (absY > absX * 1.15 && absY >= 55) {
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
