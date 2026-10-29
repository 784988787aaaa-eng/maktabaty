package com.example.data.sandbox

/**
 * Preprocesses imported and sample HTML books with:
 * 1. Safe-Area Viewport CSS (without GPU stacking/containment overrides that hide child layers on WebView).
 * 2. Crash-proof per-book localStorage isolation with in-memory fallback + Android progress bridge.
 * 3. Lightning Progressive Page Engine: renders initial pages immediately (<30ms) and pre-warms remaining pages in background chunks.
 * 4. Self-Healing BOOK_DATA Renderer: guarantees no blank/solid-color screen ever occurs across hundreds of AI book templates.
 * 5. Universal 4-Way Swipe Engine: smooth horizontal (RTL/LTR) and vertical boundary swipe page turns.
 */
object SandboxHtmlInjector {

    private val OLD_STYLE_REGEX = Regex(
        "<style\\s+id=[\"']maktabati-perf-booster(?:-v\\d+)?[\"'][^>]*>[\\s\\S]*?</style>",
        RegexOption.IGNORE_CASE
    )
    private val OLD_SCRIPT_REGEX = Regex(
        "<script\\s+id=[\"']maktabati-storage-isolation(?:-v\\d+)?[\"'][^>]*>[\\s\\S]*?</script>",
        RegexOption.IGNORE_CASE
    )

    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
          html, body {
            min-height: 100vh;
            width: 100%;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            -webkit-tap-highlight-color: transparent;
          }
          .book-stage, .stage {
            min-height: 100vh;
            width: 100%;
            box-sizing: border-box;
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
          .bottom-nav-dock, .controls-bar, .page-footer {
            flex-shrink: 0;
            z-index: 25;
          }
          .maktabati-progressive-chunk-hidden {
            display: none !important;
          }
        </style>
        <script id="maktabati-storage-isolation">
        (function(){
          if (window.__maktabatiV3Initialized) return;
          window.__maktabatiV3Initialized = true;
          try {
            const P = "$safePrefix", TOTAL = ${pageCount.coerceAtLeast(1)};
            const memStore = {};
            const key = k => { k = String(k); return k.startsWith(P) ? k : P + k; };

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
                try {
                  const d = JSON.parse(sv);
                  const data = getBookData();
                  const pages = getPagesArray(data);
                  const pNum = d ? (typeof d.currentPage === 'number' ? d.currentPage : (typeof d.page === 'number' ? d.page : null)) : null;
                  if (pNum !== null && window.MaktabatiBridge) {
                    const n = pages ? pages.length : TOTAL;
                    window.MaktabatiBridge.onPageChanged(pNum + 1, n);
                  }
                } catch (e) {}
              };

              Storage.prototype.removeItem = function(k) {
                const sk = key(k);
                delete memStore[sk];
                try { origRem.call(this, sk); } catch (e) {}
              };
            } catch (storageErr) {}

            document.addEventListener('input', function(e) {
              const target = e.target;
              if (!target || !target.matches || !target.matches('#bookSearchInput,#searchInput,.search-input') || target.__maktabatiDispatch) return;
              e.stopImmediatePropagation();
              clearTimeout(target.__maktabatiTimer);
              target.__maktabatiTimer = setTimeout(function() {
                target.__maktabatiDispatch = true;
                target.dispatchEvent(new Event('input', { bubbles: true }));
                target.__maktabatiDispatch = false;
              }, 160);
            }, true);

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
              if (typeof window.__maktabatiFallbackPage === 'number') return window.__maktabatiFallbackPage;
              return 0;
            }

            function setCurrentPageIndex(newIdx) {
              window.__maktabatiFallbackPage = newIdx;
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

            function buildPageHtml(pageObj, idx, totalPages) {
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
              if (Array.isArray(pageObj.sections)) {
                pageObj.sections.forEach(function(sec) {
                  if (sec && (sec.title || sec.heading)) {
                    parts.push('<h3 style="margin:10px 0 4px 0;font-size:16.5px;">' + (sec.title || sec.heading) + '</h3>');
                  }
                  if (sec && (sec.content || sec.text || sec.body)) {
                    parts.push('<p style="line-height:1.8;font-size:15px;margin:0 0 8px 0;">' + (sec.content || sec.text || sec.body) + '</p>');
                  }
                });
              }
              if (Array.isArray(pageObj.items)) {
                parts.push('<ul style="margin:8px 0;padding-right:20px;line-height:1.8;">' +
                  pageObj.items.map(function(it) { return '<li>' + (typeof it === 'string' ? it : (it.title || it.text || '')) + '</li>'; }).join('') +
                  '</ul>');
              }
              const quoteText = pageObj.quote || pageObj.epigraph || pageObj.reflection || '';
              if (quoteText) {
                parts.push('<blockquote style="margin:10px 0;padding:10px 14px;border-right:3px solid currentColor;opacity:0.9;font-style:italic;border-radius:8px;background:rgba(128,128,128,0.07);">' + quoteText + '</blockquote>');
              }
              const actionText = pageObj.action || pageObj.takeaway || pageObj.exercise || '';
              if (actionText) {
                parts.push('<div style="margin-top:8px;padding:8px 12px;border:1px dashed currentColor;border-radius:10px;font-size:13.5px;opacity:0.92;">✦ ' + actionText + '</div>');
              }

              const built = parts.join('');
              pageHtmlCache[idx] = built;
              return built;
            }

            // Pre-warm pages progressively in background chunks of 4 so initial open is instant (<30ms)
            function startProgressivePrewarm() {
              const data = getBookData();
              const pages = getPagesArray(data);
              if (pages && pages.length > 0) {
                const cur = Math.max(0, Math.min(pages.length - 1, getCurrentPageIndex()));
                buildPageHtml(pages[cur], cur, pages.length);
                if (cur + 1 < pages.length) buildPageHtml(pages[cur + 1], cur + 1, pages.length);
                if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                  window.MaktabatiBridge.onPageChanged(cur + 1, pages.length);
                }

                let cursor = 0;
                function warmChunk() {
                  const end = Math.min(pages.length, cursor + 4);
                  for (let i = cursor; i < end; i++) {
                    if (!pageHtmlCache[i]) buildPageHtml(pages[i], i, pages.length);
                  }
                  cursor = end;
                  if (cursor < pages.length) {
                    setTimeout(warmChunk, 24);
                  }
                }
                setTimeout(warmChunk, 30);
              }
            }

            function healBlankPageIfNeeded() {
              try {
                const data = getBookData();
                const pages = getPagesArray(data);
                if (!pages || !pages.length) return;
                const idx = Math.max(0, Math.min(pages.length - 1, getCurrentPageIndex()));
                const pageObj = pages[idx];
                if (!pageObj) return;

                let container = document.getElementById('pageInnerContent')
                  || document.getElementById('pageContent')
                  || document.getElementById('pageCanvas')
                  || document.querySelector('.page.active .page-body, .page.active, .book-page.active, #pageContainer .page-body, .page-canvas');

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

                const visibleText = (container.innerText || '').trim();
                if (visibleText.length === 0) {
                  const html = buildPageHtml(pageObj, idx, pages.length);
                  if (html) {
                    container.innerHTML = html;
                  }
                }

                const counterEl = document.getElementById('pageCounter') || document.getElementById('pageCount');
                if (counterEl && !(counterEl.innerText || '').trim()) {
                  counterEl.innerText = 'صفحة ' + (idx + 1) + ' من ' + pages.length;
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
              const container = document.getElementById('pageInnerContent') || document.getElementById('pageContent');
              if (container) {
                container.__maktabatiSelfHealed = true;
                container.innerHTML = buildPageHtml(pages[nextIdx], nextIdx, pages.length);
              }
              const counterEl = document.getElementById('pageCounter') || document.getElementById('pageCount');
              if (counterEl) counterEl.innerText = 'صفحة ' + (nextIdx + 1) + ' من ' + pages.length;
              if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                window.MaktabatiBridge.onPageChanged(nextIdx + 1, pages.length);
              }
            };

            function hookRenderFunctions() {
              ['renderCurrentPage', 'renderPage', 'goToPage'].forEach(function(fnName) {
                const orig = window[fnName];
                if (typeof orig === 'function' && !orig.__maktabatiHooked) {
                  const wrapped = function() {
                    const res = orig.apply(this, arguments);
                    setTimeout(healBlankPageIfNeeded, 10);
                    return res;
                  };
                  wrapped.__maktabatiHooked = true;
                  window[fnName] = wrapped;
                }
              });
            }

            if (document.readyState === 'loading') {
              document.addEventListener('DOMContentLoaded', function() {
                hookRenderFunctions();
                startProgressivePrewarm();
                setTimeout(healBlankPageIfNeeded, 16);
              });
            } else {
              hookRenderFunctions();
              startProgressivePrewarm();
              setTimeout(healBlankPageIfNeeded, 16);
            }

            window.addEventListener('load', function() {
              hookRenderFunctions();
              setTimeout(healBlankPageIfNeeded, 30);
            });

            function triggerUniversalPageTurn(delta) {
              try {
                const beforePage = getCurrentPageIndex();
                if (delta > 0 && typeof window.nextPage === 'function') {
                  window.nextPage();
                  setTimeout(healBlankPageIfNeeded, 16);
                  return true;
                }
                if (delta < 0 && typeof window.prevPage === 'function') {
                  window.prevPage();
                  setTimeout(healBlankPageIfNeeded, 16);
                  return true;
                }
                if (typeof window.changePage === 'function') {
                  window.changePage(delta);
                  setTimeout(healBlankPageIfNeeded, 16);
                  return true;
                }
                if (typeof window.goToPage === 'function') {
                  window.goToPage(beforePage + delta);
                  setTimeout(healBlankPageIfNeeded, 16);
                  return true;
                }
                const btnSelector = delta > 0
                  ? '#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                  : '#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
                const btn = document.querySelector(btnSelector);
                if (btn && !btn.disabled) {
                  btn.click();
                  setTimeout(healBlankPageIfNeeded, 16);
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
              if (tgt && tgt.closest && tgt.closest('button,a,input,textarea,select,label,.toc-drawer,.drawer,.modal')) {
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
              if (absX < 40 && absY < 48) return;

              setTimeout(function() {
                if (getCurrentPageIndex() !== tStartPage) {
                  healBlankPageIfNeeded();
                  return;
                }
                const dirAttr = (document.documentElement.getAttribute('dir') || document.body.getAttribute('dir') || 'rtl').toLowerCase();
                const isRtl = dirAttr === 'rtl';
                if (absX > absY * 1.1 && absX >= 40) {
                  const delta = isRtl ? (dx > 0 ? 1 : -1) : (dx < 0 ? 1 : -1);
                  triggerUniversalPageTurn(delta);
                } else if (absY > absX * 1.1 && absY >= 48) {
                  if (dy < 0 && tAtBottom) {
                    triggerUniversalPageTurn(1);
                  } else if (dy > 0 && tAtTop) {
                    triggerUniversalPageTurn(-1);
                  }
                }
              }, 12);
            }, { passive: true });
          } catch (e) {}
        })();
        </script>
        """.trimIndent()
    }

    fun injectIntoHtml(rawHtml: String, bookId: String, pageCount: Int): String {
        val cleanedHtml = rawHtml
            .replace(OLD_STYLE_REGEX, "")
            .replace(OLD_SCRIPT_REGEX, "")
        val snippet = buildInjectedSnippet(bookId, pageCount)
        val match = Regex("<head[^>]*>", RegexOption.IGNORE_CASE).find(cleanedHtml)
        return if (match != null) {
            val p = match.range.last + 1
            cleanedHtml.substring(0, p) + "\n" + snippet + "\n" + cleanedHtml.substring(p)
        } else {
            snippet + "\n" + cleanedHtml
        }
    }
}
