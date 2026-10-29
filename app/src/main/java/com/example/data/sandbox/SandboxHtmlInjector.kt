package com.example.data.sandbox

/** Preprocesses trusted imported book HTML once, before it is opened in WebView. */
object SandboxHtmlInjector {
    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
          .page-canvas,.page,.book-page,.page-container,.page-transition-enter {
            will-change: transform, opacity; transform: translateZ(0); backface-visibility: hidden; contain: layout style;
          }
          .top-bar,.bottom-nav-dock,.backdrop,.drawer,.Popover-panel,.book-header,.controls-bar,.toc-drawer {
            backdrop-filter:blur(4px)!important; -webkit-backdrop-filter:blur(4px)!important;
          }
        </style>
        <script id="maktabati-storage-isolation">
        (function(){try{
          const P="$safePrefix", TOTAL=${pageCount.coerceAtLeast(1)};
          const get=Storage.prototype.getItem, set=Storage.prototype.setItem, rem=Storage.prototype.removeItem;
          const key=k=>{k=String(k);return k.startsWith(P)?k:P+k};
          Storage.prototype.getItem=function(k){return get.call(this,key(k));};
          Storage.prototype.setItem=function(k,v){const r=set.call(this,key(k),v);try{
            const d=JSON.parse(v), data=(typeof BOOK_DATA!=='undefined'?BOOK_DATA:window.BOOK_DATA);
            if(d&&typeof d.currentPage==='number'&&window.MaktabatiBridge){const n=data&&Array.isArray(data.pages)?data.pages.length:TOTAL;window.MaktabatiBridge.onPageChanged(d.currentPage+1,n);}
          }catch(e){} return r;};
          Storage.prototype.removeItem=function(k){return rem.call(this,key(k));};
          document.addEventListener('input',function(e){const target=e.target;if(!target.matches('#bookSearchInput,#searchInput,.search-input')||target.__maktabatiDispatch)return;
            e.stopImmediatePropagation();clearTimeout(target.__maktabatiTimer);
            target.__maktabatiTimer=setTimeout(function(){target.__maktabatiDispatch=true;target.dispatchEvent(new Event('input',{bubbles:true}));target.__maktabatiDispatch=false;},180);
          },true);
        }catch(e){}}
        )();</script>
        """.trimIndent()
    }
    fun injectIntoHtml(rawHtml: String, bookId: String, pageCount: Int): String {
        if (rawHtml.contains("id=\"maktabati-perf-booster\"")) return rawHtml
        val snippet=buildInjectedSnippet(bookId,pageCount)
        val match=Regex("<head[^>]*>",RegexOption.IGNORE_CASE).find(rawHtml)
        return if(match!=null){val p=match.range.last+1;rawHtml.substring(0,p)+"\n"+snippet+"\n"+rawHtml.substring(p)} else snippet+"\n"+rawHtml
    }
}
