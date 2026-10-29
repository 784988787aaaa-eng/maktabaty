package com.example.data.sandbox

/**
 * Preprocesses imported and sample HTML books with:
 * 1. GPU Layer & Safe-Area Viewport CSS (prevents footer/page-number clipping on font zoom).
 * 2. Per-book localStorage isolation + Android progress bridge.
 * 3. Self-Healing BOOK_DATA Renderer (prevents blank pages when templates use varied field names).
 * 4. Universal 4-Way Swipe Engine (horizontal RTL/LTR + vertical boundary swipe).
 */
object SandboxHtmlInjector {

    fun buildInjectedSnippet(bookId: String, pageCount: Int): String {
        val safePrefix = "book_${bookId.replace(Regex("[^a-zA-Z0-9_]"), "_")}_"
        return """
        <style id="maktabati-perf-booster">
          html, body {
            min-height: 100dvh;
            box-sizing: border-box;
          }
          .book-stage, .stage, body {
            min-height: 100dvh;
            box-sizing: border-box;
          }
          .page-canvas,.page,.book-page,.page-container,.page-transition-enter {
            will-change: transform, opacity; transform: translateZ(0); backface-visibility: hidden; contain: layout style;
            box-sizing: border-box;
          }
          #pageInnerContent, .page-body, .page-content {
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
            box-sizing: border-box;
          }
          .bottom-nav-dock, .controls-bar, .page-footer {
            flex-shrink: 0;
            z-index: 20;
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

          function getBookData(){
            try{if(typeof BOOK_DATA!=='undefined'&&BOOK_DATA)return BOOK_DATA;}catch(e){}
            return window.BOOK_DATA||null;
          }

          function getCurrentPageIndex(){
            try{if(typeof state!=='undefined'&&state&&typeof state.currentPage==='number')return state.currentPage;}catch(e){}
            try{if(typeof currentPage==='number')return currentPage;}catch(e){}
            if(window.state&&typeof window.state.currentPage==='number')return window.state.currentPage;
            if(typeof window.currentPage==='number')return window.currentPage;
            return 0;
          }

          function healBlankPageIfNeeded(){
            try{
              const data=getBookData();
              if(!data||!Array.isArray(data.pages)||!data.pages.length)return;
              const idx=Math.max(0,Math.min(data.pages.length-1,getCurrentPageIndex()));
              const pageObj=data.pages[idx];
              if(!pageObj)return;

              const container=document.getElementById('pageInnerContent')
                ||document.getElementById('pageContent')
                ||document.querySelector('.page.active .page-body, .page.active, .book-page.active, #pageContainer .page-body');
              if(!container)return;

              const visibleText=(container.innerText||'').trim();
              if(visibleText.length>0)return;

              const parts=[];
              const badge=pageObj.chapterTag||pageObj.chapterNumber||pageObj.sectionTitle||'';
              const heading=pageObj.title||pageObj.heading||pageObj.subtitle||'';
              if(badge){
                parts.push('<div style="opacity:0.8;font-size:0.88em;font-weight:bold;margin-bottom:6px;">'+badge+'</div>');
              }
              if(heading){
                parts.push('<h2 style="margin:0 0 12px 0;line-height:1.5;">'+heading+'</h2>');
              }
              if(typeof pageObj.content==='string'&&pageObj.content.trim()){
                parts.push('<div>'+pageObj.content+'</div>');
              }else if(typeof pageObj.html==='string'&&pageObj.html.trim()){
                parts.push('<div>'+pageObj.html+'</div>');
              }else if(typeof pageObj.body==='string'&&pageObj.body.trim()){
                parts.push('<p style="line-height:1.9;">'+pageObj.body+'</p>');
              }else if(typeof pageObj.text==='string'&&pageObj.text.trim()){
                parts.push('<p style="line-height:1.9;">'+pageObj.text+'</p>');
              }
              if(Array.isArray(pageObj.paragraphs)){
                pageObj.paragraphs.forEach(function(p){
                  if(typeof p==='string'&&p.trim())parts.push('<p style="line-height:1.9;margin:0 0 10px 0;">'+p+'</p>');
                });
              }
              const quoteText=pageObj.quote||pageObj.epigraph||pageObj.ayah||pageObj.hadith||'';
              if(quoteText){
                parts.push('<blockquote style="margin:14px 0;padding:12px 16px;border-right:3px solid currentColor;opacity:0.92;font-style:italic;">'+quoteText+'</blockquote>');
              }
              if(parts.length>0){
                container.innerHTML=parts.join('');
              }
            }catch(e){}
          }

          function hookRenderFunctions(){
            ['renderCurrentPage','renderPage','goToPage'].forEach(function(fnName){
              const orig=window[fnName];
              if(typeof orig==='function'&&!orig.__maktabatiHooked){
                const wrapped=function(){
                  const res=orig.apply(this,arguments);
                  setTimeout(healBlankPageIfNeeded,16);
                  return res;
                };
                wrapped.__maktabatiHooked=true;
                window[fnName]=wrapped;
              }
            });
          }

          window.addEventListener('DOMContentLoaded',function(){
            hookRenderFunctions();
            setTimeout(healBlankPageIfNeeded,50);
          });
          window.addEventListener('load',function(){
            hookRenderFunctions();
            setTimeout(healBlankPageIfNeeded,80);
          });

          function triggerUniversalPageTurn(delta){
            try{
              const targetEl=document.querySelector('#pageInnerContent,.page.active,#pageContainer,.book-page,.page-canvas');
              if(targetEl&&targetEl.style){
                targetEl.style.transition='opacity 0.16s ease, transform 0.16s ease';
              }
              if(delta>0&&typeof window.nextPage==='function'){window.nextPage();setTimeout(healBlankPageIfNeeded,25);return true;}
              if(delta<0&&typeof window.prevPage==='function'){window.prevPage();setTimeout(healBlankPageIfNeeded,25);return true;}
              if(typeof window.changePage==='function'){window.changePage(delta);setTimeout(healBlankPageIfNeeded,25);return true;}
              if(typeof window.goToPage==='function'){
                const cur=getCurrentPageIndex();
                window.goToPage(cur+delta);
                setTimeout(healBlankPageIfNeeded,25);
                return true;
              }
              const btnSelector=delta>0
                ?'#btnNextPage,#nextBtn,.btn-next,[data-action="next"]'
                :'#btnPrevPage,#prevBtn,.btn-prev,[data-action="prev"]';
              const btn=document.querySelector(btnSelector);
              if(btn&&!btn.disabled){btn.click();setTimeout(healBlankPageIfNeeded,25);return true;}
            }catch(e){}
            return false;
          }

          let tStartX=0,tStartY=0,tStartPage=0,tAtTop=true,tAtBottom=true,tIgnore=false;
          function getScrollMetrics(el){
            let node=el;
            while(node&&node!==document.body&&node!==document.documentElement){
              if(node.scrollHeight>node.clientHeight+6){
                return {top:node.scrollTop<=4,bottom:node.scrollTop+node.clientHeight>=node.scrollHeight-6};
              }
              node=node.parentElement;
            }
            const docEl=document.scrollingElement||document.documentElement;
            const winTop=window.scrollY||docEl.scrollTop||0;
            const maxScroll=Math.max(0,(docEl.scrollHeight||0)-(window.innerHeight||docEl.clientHeight||0));
            return {top:winTop<=4,bottom:winTop>=maxScroll-6};
          }

          document.addEventListener('touchstart',function(e){
            if(!e.touches||e.touches.length!==1){tIgnore=true;return;}
            const tgt=e.target;
            if(tgt&&tgt.closest&&tgt.closest('button,a,input,textarea,select,label,.toc-drawer,.drawer,.modal')){
              tIgnore=true;return;
            }
            tIgnore=false;
            tStartX=e.touches[0].clientX;
            tStartY=e.touches[0].clientY;
            tStartPage=getCurrentPageIndex();
            const m=getScrollMetrics(tgt);
            tAtTop=m.top;
            tAtBottom=m.bottom;
          },{passive:true});

          document.addEventListener('touchend',function(e){
            if(tIgnore||!e.changedTouches||e.changedTouches.length!==1)return;
            const dx=e.changedTouches[0].clientX-tStartX;
            const dy=e.changedTouches[0].clientY-tStartY;
            const absX=Math.abs(dx),absY=Math.abs(dy);
            if(absX<44&&absY<52)return;

            setTimeout(function(){
              if(getCurrentPageIndex()!==tStartPage){
                healBlankPageIfNeeded();
                return;
              }
              const dirAttr=(document.documentElement.getAttribute('dir')||document.body.getAttribute('dir')||'rtl').toLowerCase();
              const isRtl=dirAttr==='rtl';
              if(absX>absY*1.15&&absX>=44){
                const delta=isRtl?(dx>0?1:-1):(dx<0?1:-1);
                triggerUniversalPageTurn(delta);
              }else if(absY>absX*1.15&&absY>=52){
                if(dy<0&&tAtBottom){
                  triggerUniversalPageTurn(1);
                }else if(dy>0&&tAtTop){
                  triggerUniversalPageTurn(-1);
                }
              }
            },18);
          },{passive:true});
        }catch(e){}}
        )();</script>
        """.trimIndent()
    }

    fun injectIntoHtml(rawHtml: String, bookId: String, pageCount: Int): String {
        if (rawHtml.contains("id=\"maktabati-perf-booster\"")) return rawHtml
        val snippet = buildInjectedSnippet(bookId, pageCount)
        val match = Regex("<head[^>]*>", RegexOption.IGNORE_CASE).find(rawHtml)
        return if (match != null) {
            val p = match.range.last + 1
            rawHtml.substring(0, p) + "\n" + snippet + "\n" + rawHtml.substring(p)
        } else {
            snippet + "\n" + rawHtml
        }
    }
}
