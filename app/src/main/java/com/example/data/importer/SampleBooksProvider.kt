package com.example.data.importer

import android.content.Context
import com.example.data.model.Book
import com.example.data.sandbox.SandboxHtmlInjector
import java.io.File

object SampleBooksProvider {

    private fun buildInteractiveBookHtml(
        lang: String,
        dir: String,
        title: String,
        subtitle: String,
        author: String,
        bgPrimary: String,
        accentGold: String,
        accentRose: String,
        pagesJsonArray: String
    ): String {
        val isRtl = dir == "rtl"
        val prevLabel = if (isRtl) "السابق" else "Previous"
        val nextLabel = if (isRtl) "التالي" else "Next"
        val searchPlaceholder = if (isRtl) "ابحث في صفحات الكتاب..." else "Search inside book..."
        val bookmarkLabel = if (isRtl) "حفظ علامة" else "Bookmark"
        val tocLabel = if (isRtl) "الفهرس" else "Contents"
        val pageWord = if (isRtl) "صفحة" else "Page"
        val ofWord = if (isRtl) "من" else "of"

        return """
        <!DOCTYPE html>
        <html lang="$lang" dir="$dir">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta name="author" content="$author">
            <meta name="description" content="$subtitle">
            <title>$title</title>
            <style>
                :root {
                    --bg-primary: $bgPrimary;
                    --accent-gold: $accentGold;
                    --accent-rose: $accentRose;
                    --paper-bg: #FCF9F2;
                    --paper-text: #1F1A17;
                }
                * { box-sizing: border-box; }
                body {
                    margin: 0;
                    padding: 0;
                    font-family: 'Cairo', 'Amiri', system-ui, -apple-system, serif;
                    background: radial-gradient(circle at top, var(--accent-rose), var(--bg-primary) 75%);
                    color: var(--paper-text);
                    min-height: 100vh;
                    display: flex;
                    flex-direction: column;
                }
                .book-header {
                    background: rgba(10, 14, 24, 0.78);
                    border-bottom: 1px solid var(--accent-gold);
                    color: #fff;
                    padding: 12px 16px;
                    display: flex;
                    flex-wrap: wrap;
                    align-items: center;
                    justify-content: space-between;
                    gap: 8px;
                }
                .book-header-title {
                    font-size: 16px;
                    font-weight: bold;
                    color: var(--accent-gold);
                }
                .book-header-sub {
                    font-size: 12px;
                    color: #cbd5e1;
                }
                .search-bar {
                    display: flex;
                    gap: 6px;
                    align-items: center;
                }
                .search-bar input {
                    background: rgba(255,255,255,0.12);
                    border: 1px solid rgba(255,255,255,0.25);
                    color: #fff;
                    padding: 6px 10px;
                    border-radius: 8px;
                    font-size: 13px;
                    width: 150px;
                }
                .btn-tool {
                    background: rgba(255,255,255,0.14);
                    color: var(--accent-gold);
                    border: 1px solid var(--accent-gold);
                    padding: 6px 10px;
                    border-radius: 8px;
                    font-size: 12px;
                    cursor: pointer;
                    font-weight: bold;
                }
                .btn-tool.active {
                    background: var(--accent-gold);
                    color: #111;
                }
                .stage {
                    flex: 1;
                    display: flex;
                    justify-content: center;
                    align-items: stretch;
                    padding: 16px 12px;
                }
                .page {
                    background: var(--paper-bg);
                    color: var(--paper-text);
                    width: 100%;
                    max-width: 720px;
                    border-radius: 14px;
                    padding: 28px 24px;
                    box-shadow: 0 16px 40px rgba(0,0,0,0.45), inset 0 0 0 2px var(--accent-gold);
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    position: relative;
                    will-change: transform, opacity;
                    transform: translateZ(0);
                    transition: opacity 0.18s ease, transform 0.18s ease;
                }
                .ornament-top {
                    text-align: center;
                    margin-bottom: 12px;
                }
                .chapter-badge {
                    display: inline-block;
                    background: var(--bg-primary);
                    color: var(--accent-gold);
                    padding: 4px 12px;
                    border-radius: 99px;
                    font-size: 12px;
                    font-weight: bold;
                    margin-bottom: 10px;
                }
                .page h2 {
                    margin: 0 0 14px 0;
                    color: var(--bg-primary);
                    font-size: 23px;
                    border-bottom: 2px solid var(--accent-gold);
                    padding-bottom: 10px;
                }
                .page-body {
                    font-size: 18px;
                    line-height: 1.95;
                    text-align: justify;
                }
                .page-quote {
                    margin: 18px 0;
                    padding: 14px 18px;
                    background: rgba(217, 119, 6, 0.09);
                    border-${if (isRtl) "right" else "left"}: 4px solid var(--accent-rose);
                    border-radius: 8px;
                    font-style: italic;
                }
                .page-footer {
                    margin-top: 24px;
                    padding-top: 12px;
                    border-top: 1px dashed #d6d3d1;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    font-size: 13px;
                    color: #57534e;
                }
                .controls-bar {
                    background: rgba(10, 14, 24, 0.9);
                    padding: 12px 18px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    border-top: 1px solid rgba(255,255,255,0.15);
                }
                .nav-btn {
                    background: var(--accent-gold);
                    color: #0f172a;
                    border: none;
                    padding: 10px 20px;
                    border-radius: 10px;
                    font-size: 14px;
                    font-weight: bold;
                    cursor: pointer;
                }
                .nav-btn:disabled {
                    opacity: 0.4;
                    cursor: default;
                }
                .page-indicator {
                    color: #fff;
                    font-size: 14px;
                    font-weight: bold;
                }
                .toc-drawer {
                    display: none;
                    background: #1e293b;
                    color: #f8fafc;
                    padding: 16px;
                    max-height: 260px;
                    overflow-y: auto;
                    border-bottom: 2px solid var(--accent-gold);
                }
                .toc-item {
                    padding: 8px 12px;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    border-bottom: 1px solid rgba(255,255,255,0.06);
                }
                .toc-item:hover { background: rgba(255,255,255,0.1); }
                mark {
                    background: #fde047;
                    color: #000;
                    padding: 0 3px;
                    border-radius: 3px;
                }
            </style>
        </head>
        <body>
            <header class="book-header">
                <div>
                    <div class="book-header-title" id="headerTitle">$title</div>
                    <div class="book-header-sub">$author — $subtitle</div>
                </div>
                <div class="search-bar">
                    <input type="text" id="searchInput" placeholder="$searchPlaceholder" oninput="onSearchInput(this.value)">
                    <button class="btn-tool" id="tocBtn" onclick="toggleToc()">$tocLabel</button>
                    <button class="btn-tool" id="bmBtn" onclick="togglePageBookmark()">★ $bookmarkLabel</button>
                </div>
            </header>

            <div class="toc-drawer" id="tocDrawer"></div>

            <main class="stage">
                <article class="page" id="pageContainer">
                    <div>
                        <div class="ornament-top">
                            <svg width="120" height="22" viewBox="0 0 120 22">
                                <path d="M10,11 L48,11 M72,11 L110,11" stroke="$accentGold" stroke-width="1.5"/>
                                <polygon points="60,3 67,11 60,19 53,11" fill="$accentRose"/>
                            </svg>
                        </div>
                        <span class="chapter-badge" id="pageBadge"></span>
                        <h2 id="pageTitle"></h2>
                        <div class="page-body" id="pageContent"></div>
                        <div class="page-quote" id="pageQuote"></div>
                    </div>
                    <div class="page-footer">
                        <span id="footerAuthor">$author</span>
                        <span id="footerBookmarkStatus"></span>
                    </div>
                </article>
            </main>

            <footer class="controls-bar">
                <button class="nav-btn" id="prevBtn" onclick="changePage(-1)">$prevLabel</button>
                <span class="page-indicator" id="pageCounter"></span>
                <button class="nav-btn" id="nextBtn" onclick="changePage(1)">$nextLabel</button>
            </footer>

            <script>
                const STORAGE_KEY = "luxury_arabic_book_state_v1";
                const BOOK_DATA = {
                    title: "$title",
                    subtitle: "$subtitle",
                    author: "$author",
                    pages: $pagesJsonArray
                };

                let state = {
                    currentPage: 0,
                    bookmarks: []
                };

                function loadState() {
                    try {
                        const raw = localStorage.getItem(STORAGE_KEY);
                        if (raw) {
                            const parsed = JSON.parse(raw);
                            if (typeof parsed.currentPage === 'number') {
                                state.currentPage = Math.max(0, Math.min(BOOK_DATA.pages.length - 1, parsed.currentPage));
                            }
                            if (Array.isArray(parsed.bookmarks)) {
                                state.bookmarks = parsed.bookmarks;
                            }
                        }
                    } catch (e) {}
                }

                function saveState() {
                    try {
                        localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
                        if (window.MaktabatiBridge && window.MaktabatiBridge.onPageChanged) {
                            window.MaktabatiBridge.onPageChanged(state.currentPage + 1, BOOK_DATA.pages.length);
                        }
                    } catch (e) {}
                }

                function renderPage(searchTerm) {
                    const p = BOOK_DATA.pages[state.currentPage];
                    if (!p) return;
                    document.getElementById('pageBadge').innerText = "$pageWord " + (state.currentPage + 1);
                    document.getElementById('pageTitle').innerText = p.title;
                    let bodyHtml = p.content;
                    if (searchTerm && searchTerm.trim().length > 1) {
                        const reg = new RegExp("(" + searchTerm.replace(/[.*+?^${'$'}{}()|[\]\\]/g, '\\${'$'}&') + ")", "gi");
                        bodyHtml = bodyHtml.replace(reg, "<mark>${'$'}1</mark>");
                    }
                    document.getElementById('pageContent').innerHTML = bodyHtml;
                    const quoteEl = document.getElementById('pageQuote');
                    if (p.quote) {
                        quoteEl.style.display = 'block';
                        quoteEl.innerText = p.quote;
                    } else {
                        quoteEl.style.display = 'none';
                    }
                    document.getElementById('pageCounter').innerText = (state.currentPage + 1) + " $ofWord " + BOOK_DATA.pages.length;
                    document.getElementById('prevBtn').disabled = state.currentPage === 0;
                    document.getElementById('nextBtn').disabled = state.currentPage === BOOK_DATA.pages.length - 1;

                    const isBm = state.bookmarks.includes(state.currentPage);
                    const bmBtn = document.getElementById('bmBtn');
                    bmBtn.className = isBm ? "btn-tool active" : "btn-tool";
                    document.getElementById('footerBookmarkStatus').innerText = isBm ? "★" : "";
                    saveState();
                }

                function changePage(delta) {
                    const next = state.currentPage + delta;
                    if (next >= 0 && next < BOOK_DATA.pages.length) {
                        state.currentPage = next;
                        renderPage(document.getElementById('searchInput').value);
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    }
                }

                function goToPage(index) {
                    if (index >= 0 && index < BOOK_DATA.pages.length) {
                        state.currentPage = index;
                        document.getElementById('tocDrawer').style.display = 'none';
                        renderPage(document.getElementById('searchInput').value);
                    }
                }

                function togglePageBookmark() {
                    const idx = state.bookmarks.indexOf(state.currentPage);
                    if (idx >= 0) {
                        state.bookmarks.splice(idx, 1);
                    } else {
                        state.bookmarks.push(state.currentPage);
                    }
                    renderPage(document.getElementById('searchInput').value);
                }

                function toggleToc() {
                    const drawer = document.getElementById('tocDrawer');
                    if (drawer.style.display === 'block') {
                        drawer.style.display = 'none';
                    } else {
                        drawer.innerHTML = BOOK_DATA.pages.map((p, i) =>
                            `<div class="toc-item" onclick="goToPage(${'$'}{i})">${'$'}{i + 1}. ${'$'}{p.title} ${'$'}{state.bookmarks.includes(i) ? '★' : ''}</div>`
                        ).join('');
                        drawer.style.display = 'block';
                    }
                }

                function onSearchInput(query) {
                    const q = (query || '').trim().toLowerCase();
                    if (q.length > 1) {
                        const foundIdx = BOOK_DATA.pages.findIndex(p =>
                            p.title.toLowerCase().includes(q) || p.content.toLowerCase().includes(q)
                        );
                        if (foundIdx >= 0 && foundIdx !== state.currentPage) {
                            state.currentPage = foundIdx;
                        }
                    }
                    renderPage(query);
                }

                window.addEventListener('hashchange', function() {
                    const h = window.location.hash || '';
                    if (h.startsWith('#page-')) {
                        const num = parseInt(h.replace('#page-', ''), 10);
                        if (!isNaN(num)) goToPage(num - 1);
                    }
                });

                loadState();
                renderPage('');
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateSampleBooks(context: Context): List<Book> {
        val booksDir = File(context.filesDir, "books").apply { mkdirs() }
        val books = mutableListOf<Book>()

        // 1. مقدمة ابن خلدون (12 Interactive Pages, Arabic RTL, Warm Amber/Rose theme)
        val book1Id = "sample_ibn_khaldun"
        val dir1 = File(booksDir, book1Id).apply { mkdirs() }
        val html1 = File(dir1, "index.html")
        val khaldunPages = (1..12).joinToString(",\n") { pageNum ->
            val titles = listOf(
                "في أن الاجتماع الإنساني ضروري",
                "في فضل علم التاريخ وتحقيق مذاهبه",
                "في قسط العمران من الأرض والأقاليم",
                "في أثر الهواء والأقاليم في أخلاق البشر",
                "في أن البدو أقدم من الحضر وسابق عليه",
                "في أن سكنى البدو لا يكون إلا للقبائل أهل العصبية",
                "في أن الغاية التي تجري إليها العصبية هي الملك",
                "في أطوار الدولة واختلاف أحوالها",
                "في أن الظلم مؤذن بخراب العمران",
                "في أصناف العلوم الواقعة في العمران",
                "في الصنائع ووجه معاش الإنسان",
                "خاتمة في قوانين العمران البشري"
            )
            val t = titles[pageNum - 1]
            """{ title: "الفصل $pageNum: $t", content: "<p>اعلم أن هذا المبحث من أمهات مسائل العمران البشري؛ إذ يتناول <strong>$t</strong> بالتفصيل والبرهان العقلي والتاريخي. فالإنسان مدني بالطبع، ولا قوام لحياته إلا بالتعاون مع أبناء جنسه لتدبير المعاش ودفع العاديات وبناء الحضارة.</p><p>وقدرت الأمم المتعاقبة أن صلاح الملك مقترن بالعدل والإنصاف وعمارة الأرض، فإذا اتسع نطاق العمران تكاثرت الصنائع والعلوم وازدهرت الحواضر.</p>", quote: "«الإنسان مدني بالطبع، والاجتماع الإنساني ضروري لعمارة العالم واستخلاف الله للبشر فيه.»" }"""
        }
        html1.writeText(
            buildInteractiveBookHtml(
                lang = "ar",
                dir = "rtl",
                title = "مقدمة ابن خلدون",
                subtitle = "فصول مختارة في طبائع العمران وتأسيس الدول",
                author = "عبد الرحمن بن خلدون",
                bgPrimary = "#2D1606",
                accentGold = "#F59E0B",
                accentRose = "#9A3412",
                pagesJsonArray = "[\n$khaldunPages\n]"
            )
        )
        val meta1 = HtmlBookParser.parse(html1, "مقدمة ابن خلدون")
        books.add(
            Book(
                id = book1Id,
                title = meta1.title,
                subtitle = meta1.subtitle,
                author = meta1.author,
                description = meta1.description,
                folderPath = book1Id,
                entryHtmlPath = "index.html",
                shelfId = "shelf_literature",
                isFavorite = true,
                readingProgress = 0.25f,
                scrollY = 0,
                currentPage = 3,
                pageCount = meta1.pageCount,
                language = meta1.language,
                isRtl = meta1.isRtl,
                accentRoseHex = meta1.accentRoseHex,
                accentGoldHex = meta1.accentGoldHex,
                bgPrimaryHex = meta1.bgPrimaryHex,
                coverGradientStart = meta1.coverGradientStart,
                coverGradientEnd = meta1.coverGradientEnd,
                coverIcon = "history_edu",
                fileCount = 1,
                totalSizeBytes = html1.length(),
                hasExternalCss = true,
                hasExternalJs = true,
                tableOfContentsJson = HtmlBookParser.tocToJson(meta1.tableOfContents)
            )
        )

        // 2. أسرار الكون والفيزياء الفلكية (10 Interactive Pages, Arabic RTL, Deep Space theme)
        val book2Id = "sample_astrophysics"
        val dir2 = File(booksDir, book2Id).apply { mkdirs() }
        val html2 = File(dir2, "index.html")
        val astroPages = (1..10).joinToString(",\n") { pageNum ->
            val titles = listOf(
                "نشأة الكون والانفجار العظيم",
                "ولادة النجوم داخل السدم الكونية",
                "الاندماج النووي وحياة النجوم",
                "المستعرات العظمى (Supernovae)",
                "الثقوب السوداء وأفق الحدث",
                "المجرات الحلزونية ودرب التبانة",
                "الكواكب الخارجية وشروط الحياة",
                "المادة المظلمة والجاذبية الخفية",
                "الطاقة المظلمة وتمدد الزمكان",
                "مستقبل الكون والمصير الكوني"
            )
            val t = titles[pageNum - 1]
            """{ title: "$t", content: "<p>يستعرض هذا الفصل العلمي موضوع <strong>$t</strong> وفق أحدث الأرصاد الفلكية لتلسكوب جيمس ويب الفضائي والمراصد الأرضية. إن فهم بنية الزمكان وتفاعلات الجاذبية مع الضوء يفتح نافذة مذهلة على تاريخ الكون السحيق.</p><p>تتحرك المجرات في نسيج كوني مترابط تحكمه معادلات النسبية العامة وميكانيكا الكم في تناغم بديع.</p>", quote: "«نحن جميعاً مصنوعون من غبار النجوم التي أضاءت الكون قبل مليارات السنين.»" }"""
        }
        html2.writeText(
            buildInteractiveBookHtml(
                lang = "ar",
                dir = "rtl",
                title = "أسرار الكون والفيزياء الفلكية",
                subtitle = "رحلة مصورة وتفاعلية في أعماق المجرات والثقوب السوداء",
                author = "د. سامر كنعان",
                bgPrimary = "#0F172A",
                accentGold = "#38BDF8",
                accentRose = "#4F46E5",
                pagesJsonArray = "[\n$astroPages\n]"
            )
        )
        val meta2 = HtmlBookParser.parse(html2, "أسرار الكون والفيزياء الفلكية")
        books.add(
            Book(
                id = book2Id,
                title = meta2.title,
                subtitle = meta2.subtitle,
                author = meta2.author,
                description = meta2.description,
                folderPath = book2Id,
                entryHtmlPath = "index.html",
                shelfId = "shelf_science",
                isFavorite = true,
                readingProgress = 0.40f,
                scrollY = 0,
                currentPage = 4,
                pageCount = meta2.pageCount,
                language = meta2.language,
                isRtl = meta2.isRtl,
                accentRoseHex = meta2.accentRoseHex,
                accentGoldHex = meta2.accentGoldHex,
                bgPrimaryHex = meta2.bgPrimaryHex,
                coverGradientStart = meta2.coverGradientStart,
                coverGradientEnd = meta2.coverGradientEnd,
                coverIcon = "public",
                fileCount = 1,
                totalSizeBytes = html2.length(),
                hasExternalCss = true,
                hasExternalJs = true,
                tableOfContentsJson = HtmlBookParser.tocToJson(meta2.tableOfContents)
            )
        )

        // 3. The Art of Interactive Typography (10 Pages, English LTR, Emerald & Gold theme)
        val book3Id = "sample_english_typography"
        val dir3 = File(booksDir, book3Id).apply { mkdirs() }
        val html3 = File(dir3, "index.html")
        val enPages = (1..10).joinToString(",\n") { pageNum ->
            val titles = listOf(
                "Origins of Modular Book Design",
                "Proportions, Golden Grids & Margins",
                "Optical Sizing & Serif Harmonies",
                "Contrast & Color Theory in Digital Ink",
                "Interactive State & Micro-Transitions",
                "Bilingual Layout Engineering (RTL & LTR)",
                "Hardware-Accelerated Page Rendering",
                "Sandboxed Storage & Reader Privacy",
                "Crafting Timeless Digital Editions",
                "The Future of Self-Contained Books"
            )
            val t = titles[pageNum - 1]
            """{ title: "Chapter $pageNum: $t", content: "<p>In <strong>$t</strong>, we examine how self-contained HTML books merge classical Renaissance bookbinding proportions with modern reactive web typography. Every page balances negative space, measure, and vertical rhythm.</p><p>By isolating state per book and leveraging GPU compositing layers, digital editions achieve tactile responsiveness across every screen size.</p>", quote: "\"Typography is what language looks like when crafted with mathematical grace.\"" }"""
        }
        html3.writeText(
            buildInteractiveBookHtml(
                lang = "en",
                dir = "ltr",
                title = "The Art of Interactive Typography",
                subtitle = "Designing Self-Contained Digital Editions",
                author = "Julian Vance",
                bgPrimary = "#06392F",
                accentGold = "#FBBF24",
                accentRose = "#0D9488",
                pagesJsonArray = "[\n$enPages\n]"
            )
        )
        val meta3 = HtmlBookParser.parse(html3, "The Art of Interactive Typography")
        books.add(
            Book(
                id = book3Id,
                title = meta3.title,
                subtitle = meta3.subtitle,
                author = meta3.author,
                description = meta3.description,
                folderPath = book3Id,
                entryHtmlPath = "index.html",
                shelfId = "shelf_tech",
                isFavorite = false,
                readingProgress = 0.20f,
                scrollY = 0,
                currentPage = 2,
                pageCount = meta3.pageCount,
                language = meta3.language,
                isRtl = meta3.isRtl,
                accentRoseHex = meta3.accentRoseHex,
                accentGoldHex = meta3.accentGoldHex,
                bgPrimaryHex = meta3.bgPrimaryHex,
                coverGradientStart = meta3.coverGradientStart,
                coverGradientEnd = meta3.coverGradientEnd,
                coverIcon = "code",
                fileCount = 1,
                totalSizeBytes = html3.length(),
                hasExternalCss = true,
                hasExternalJs = true,
                tableOfContentsJson = HtmlBookParser.tocToJson(meta3.tableOfContents)
            )
        )

        // 4. روائع البيان والحكمة العربية (15 Interactive Pages, Arabic RTL, Royal Burgundy & Rose Gold)
        val book4Id = "sample_arabic_wisdom"
        val dir4 = File(booksDir, book4Id).apply { mkdirs() }
        val html4 = File(dir4, "index.html")
        val wisdomPages = (1..15).joinToString(",\n") { pageNum ->
            """{ title: "الباب $pageNum: من عيون الأدب والبلاغة", content: "<p>يجمع هذا الباب مختارات رفيعة من عيون الأدب العربي القديم وفنون الإيجاز والبلاغة التي خلدها الزمان. إن الكلمة البليغة هي التي تجمع بين جزالة اللفظ وعمق المعنى وإشراق الديباجة.</p><p>تأمل كيف صاغ البلغاء والحكماء تجاربهم الإنسانية في عبارات موجزة تسري مسرى الأمثال عبر القرون.</p>", quote: "«البيان سحر حلال، وخير الكلام ما قلّ ودلّ ولم يملّ.»" }"""
        }
        html4.writeText(
            buildInteractiveBookHtml(
                lang = "ar",
                dir = "rtl",
                title = "روائع البيان والحكمة",
                subtitle = "مختارات أدبية مذهبة من تراث البلاغة العربية",
                author = "أبو عثمان الجاحظ (مختارات)",
                bgPrimary = "#3B0722",
                accentGold = "#FDE047",
                accentRose = "#E11D48",
                pagesJsonArray = "[\n$wisdomPages\n]"
            )
        )
        val meta4 = HtmlBookParser.parse(html4, "روائع البيان والحكمة")
        books.add(
            Book(
                id = book4Id,
                title = meta4.title,
                subtitle = meta4.subtitle,
                author = meta4.author,
                description = meta4.description,
                folderPath = book4Id,
                entryHtmlPath = "index.html",
                shelfId = "shelf_literature",
                isFavorite = true,
                readingProgress = 1.0f,
                scrollY = 0,
                currentPage = 15,
                pageCount = meta4.pageCount,
                language = meta4.language,
                isRtl = meta4.isRtl,
                accentRoseHex = meta4.accentRoseHex,
                accentGoldHex = meta4.accentGoldHex,
                bgPrimaryHex = meta4.bgPrimaryHex,
                coverGradientStart = meta4.coverGradientStart,
                coverGradientEnd = meta4.coverGradientEnd,
                coverIcon = "auto_stories",
                fileCount = 1,
                totalSizeBytes = html4.length(),
                hasExternalCss = true,
                hasExternalJs = true,
                tableOfContentsJson = HtmlBookParser.tocToJson(meta4.tableOfContents)
            )
        )

        // 5. موسوعة المعرفة الكبرى (100 Pages Stress Test Book for 60fps Performance Booster)
        val book5Id = "sample_encyclopedia_100"
        val dir5 = File(booksDir, book5Id).apply { mkdirs() }
        val html5 = File(dir5, "index.html")
        val encyclopediaPages = (1..100).joinToString(",\n") { pageNum ->
            """{ title: "المبحث $pageNum: آفاق العلوم الإنسانية والطبيعية", content: "<p>هذه الصفحة رقم <strong>$pageNum من أصل 100 صفحة</strong> في موسوعة المعرفة الكبرى. صُمم هذا الكتاب الضخم لاختبار كفاءة محرك العرض المعزول في تطبيق «مكتبتي»، وسرعة استخراج عدد الصفحات تلقائياً من مصفوفة <code>BOOK_DATA.pages</code>، وسلاسة تقليب الصفحات بسرعة 60 إطاراً في الثانية دون أي تشنج أو بطء.</p><p>كما يختبر عزل مفاتيح <code>localStorage</code> بحيث يحتفظ كل كتاب بموضعه وعلاماته المرجعية الخاصة به بشكل مستقل تماماً.</p>", quote: "«العلم صيد والكتابة قيده، قيد صيودك بالحبال الواثقة.» — الصفحة $pageNum" }"""
        }
        html5.writeText(
            buildInteractiveBookHtml(
                lang = "ar",
                dir = "rtl",
                title = "موسوعة المعرفة الكبرى",
                subtitle = "طبعة المئة صفحة التفاعلية الشاملة",
                author = "هيئة الموسوعة العلمية",
                bgPrimary = "#1E1B4B",
                accentGold = "#F59E0B",
                accentRose = "#7C3AED",
                pagesJsonArray = "[\n$encyclopediaPages\n]"
            )
        )
        val meta5 = HtmlBookParser.parse(html5, "موسوعة المعرفة الكبرى")
        books.add(
            Book(
                id = book5Id,
                title = meta5.title,
                subtitle = meta5.subtitle,
                author = meta5.author,
                description = meta5.description,
                folderPath = book5Id,
                entryHtmlPath = "index.html",
                shelfId = "shelf_science",
                isFavorite = false,
                readingProgress = 0.08f,
                scrollY = 0,
                currentPage = 8,
                pageCount = meta5.pageCount,
                language = meta5.language,
                isRtl = meta5.isRtl,
                accentRoseHex = meta5.accentRoseHex,
                accentGoldHex = meta5.accentGoldHex,
                bgPrimaryHex = meta5.bgPrimaryHex,
                coverGradientStart = meta5.coverGradientStart,
                coverGradientEnd = meta5.coverGradientEnd,
                coverIcon = "menu_book",
                fileCount = 1,
                totalSizeBytes = html5.length(),
                hasExternalCss = true,
                hasExternalJs = true,
                tableOfContentsJson = HtmlBookParser.tocToJson(meta5.tableOfContents)
            )
        )

        // Premium youth-oriented editorial sample demonstrating the real luxury template contract.
        val whisperId = "sample_hams_almaraya"
        val whisperDir = File(booksDir, whisperId).apply { mkdirs() }
        val whisperFile = File(whisperDir, "index.html")
        whisperFile.writeText(
            """<!doctype html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>همسُ المرايا</title>
<style>:root{--bg-primary:#fff8f2;--bg-secondary:#f7e7e8;--accent-rose:#c48b9f;--accent-peach:#f4b79b;--accent-lavender:#c9c1e8;--accent-gold:#c99a45}
*{box-sizing:border-box}body{margin:0;background:var(--bg-primary);font-family:Georgia,serif;color:#43334a}.top-bar{position:sticky;top:0;z-index:2;padding:16px;background:#fff8f2e8;backdrop-filter:blur(4px);display:flex;justify-content:space-between;align-items:center}
.page-canvas{padding:28px 24px;min-height:74vh;max-width:720px;margin:auto}.page{display:none;min-height:60vh}.page.active{display:block}.cover{display:flex;flex-direction:column;justify-content:center;align-items:center;text-align:center;border:1px solid var(--accent-gold);border-radius:28px;padding:28px;background:linear-gradient(145deg,#fff8f2,#f7e7e8,#e9e4f8)}
h1{color:#9d5877;line-height:1.6}blockquote{border-right:3px solid var(--accent-gold);padding:12px 18px;color:#80536c;background:#fff8f2a8;border-radius:12px}.bottom-nav-dock{position:fixed;bottom:12px;left:12px;right:12px;border-radius:24px;padding:12px;background:#c9c1e8ed;display:flex;justify-content:space-around;box-shadow:0 8px 32px #8c66851c}button{border:0;border-radius:18px;padding:10px 15px;background:#fff8f2;color:#71445e;font-weight:bold}</style></head><body>
<header class="top-bar"><button id="btnOpenToc" onclick="goToPage(2)">الفهرس</button><strong>همسُ المرايا</strong><span style="color:var(--accent-gold)">✦</span></header>
<main class="page-canvas">
<article class="page active cover"><div style="font-size:42px;color:var(--accent-gold)">✧</div><h1>همسُ المرايا</h1><p>رسائل صغيرة إلى الذات الكبيرة</p><p>دفتر دافئ لاكتشاف الذات والأحلام والضوء الذي نحمله في الداخل.</p><small>مكتبتي · إصدار تفاعلي</small></article>
<article class="page"><h1>إهداء</h1><p>إلى كل قلب يحاول أن يفهم نفسه برفق، وإلى من يتعلم أن النمو لا يحتاج إلى ضجيج، بل إلى صدق وصبر.</p><blockquote>كن صديقاً لنفسك في الأيام التي لا تعرف فيها الطريق.</blockquote></article>
<article class="page"><h1>فهرس الهمس</h1><p>١. رسائل إلى النسخة التي سأصبحها</p><p>٢. ألوان الداخل</p><p>٣. شجاعة البدايات</p><p>٤. اقتباسات على حافة الضوء</p><button onclick="goToPage(3)">ابدأ القراءة ←</button></article>
<article class="page"><h1>رسائل إلى النسخة التي سأصبحها</h1><p>في كل مرآة حكاية صغيرة، وفي كل انعكاس احتمال جديد. لا تستعجل الوصول؛ فبعض الأشياء الجميلة تنمو بصمت.</p><p>امنح أحلامك وقتاً كي تنضج، ولا تجعل المقارنة تسرق منك بهجة الطريق.</p><blockquote>خطوة صادقة اليوم، أوسع من حلم مؤجل إلى الأبد.</blockquote></article>
<article class="page"><h1>ألوان الداخل</h1><p>ليست الشجاعة غياب الخوف، بل أن نصغي إلى ما نحب رغم الضجيج. اختر لونك اليوم، واترك نافذة صغيرة للدهشة.</p><p>هناك أيام بلون الخوخ، وأخرى بلون البنفسج؛ وكلها أجزاء من لوحة واحدة اسمها أنت.</p><blockquote>لا تحتاج أن تكون مكتملاً كي تكون جديراً بالمحبة.</blockquote></article>
<article class="page"><h1>شجاعة البدايات</h1><p>كل بداية تحمل ارتباكها الخاص، لكن الطريق يتضح حين نخطو. احتفظ بما يلهمك، وتخلّص بلطف مما لم يعد يشبهك.</p><h2 style="color:var(--accent-rose)">اقتباس أخير</h2><blockquote>كن نوراً هادئاً؛ ليس مطلوباً منك أن تضيء العالم كله.</blockquote><p>انتهى هذا الفصل، وما زالت الحكاية مستمرة فيك.</p></article></main>
<nav class="bottom-nav-dock"><button onclick="goToPage(Math.max(0,currentPage-1))">السابق</button><span id="pageCount">١ / ٦</span><button onclick="goToPage(Math.min(5,currentPage+1))">التالي</button></nav>
<script>const BOOK_DATA={title:'همسُ المرايا',author:'مكتبتي',pages:[{title:'الغلاف'},{title:'إهداء'},{title:'فهرس الهمس'},{title:'رسائل إلى النسخة التي سأصبحها'},{sectionTitle:'ألوان الداخل'},{chapterTag:'شجاعة البدايات',heading:'اقتباس أخير'}]};let currentPage=0;function goToPage(n){currentPage=Math.max(0,Math.min(5,n));document.querySelectorAll('.page').forEach((p,i)=>p.classList.toggle('active',i===currentPage));document.getElementById('pageCount').textContent=(currentPage+1)+' / 6';localStorage.setItem('luxury_arabic_book_state_v1',JSON.stringify({currentPage:currentPage}));}</script></body></html>""",
            Charsets.UTF_8
        )
        if (books.none { it.id == whisperId }) {
            val whisperMeta = HtmlBookParser.parse(whisperFile, "همسُ المرايا")
            if (books.isNotEmpty()) {
                books.add(books[0].copy(id=whisperId, title="همسُ المرايا", subtitle="دفتر صغير لاكتشاف الذات", author="مكتبتي", folderPath=whisperId, entryHtmlPath="index.html", pageCount=maxOf(1, whisperMeta.pageCount), readingProgress=0f, scrollY=0, currentPage=1, tableOfContentsJson=HtmlBookParser.tocToJson(whisperMeta.tableOfContents), createdAt=System.currentTimeMillis(), lastReadTimestamp=System.currentTimeMillis(), totalSizeBytes=whisperFile.length(), fileCount=1))
            }
        }

        // Ensure generated samples use the same preprocessed on-disk format as imported books.
        books.forEach { book ->
            val html = File(File(booksDir, book.folderPath), book.entryHtmlPath)
            if (html.isFile) runCatching {
                val raw = html.readText(Charsets.UTF_8)
                if (!raw.contains("maktabati-perf-booster")) html.writeText(SandboxHtmlInjector.injectIntoHtml(raw, book.id, book.pageCount), Charsets.UTF_8)
            }
        }
        return books
    }
}
