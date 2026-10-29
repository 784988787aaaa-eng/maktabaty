package com.example.data.importer

import android.content.Context
import com.example.data.model.Book
import com.example.data.sandbox.SandboxHtmlInjector
import java.io.File

object SampleBooksProvider {

    const val ISLAMIC_MASTERPIECE_ID = "islamic_sakina_masterpiece"

    private fun buildIslamicMasterpieceHtml(): String {
        return """
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta name="author" content="مكتبتي — إصدار إسلامي فاخر">
    <meta name="description" content="إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن">
    <title>أنوارُ السكينة | إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن</title>
    <style>
        :root {
            --bg-primary: #064E3B;
            --bg-surface: #FCF9F2;
            --bg-card: #FFFFFF;
            --text-primary: #1C1917;
            --text-secondary: #57534E;
            --accent-gold: #D97706;
            --accent-rose: #047857;
            --border-soft: rgba(217, 119, 6, 0.32);
            --ayah-bg: rgba(4, 120, 87, 0.07);
            --hadith-bg: rgba(217, 119, 6, 0.08);
        }
        [data-theme="ivory"] {
            --bg-primary: #F5EFE6;
            --bg-surface: #FFFDF9;
            --bg-card: #FFFFFF;
            --text-primary: #1F1A17;
            --text-secondary: #57534E;
            --accent-gold: #B45309;
            --accent-rose: #065F46;
            --border-soft: rgba(180, 83, 9, 0.28);
            --ayah-bg: rgba(6, 95, 70, 0.06);
            --hadith-bg: rgba(180, 83, 9, 0.08);
        }
        [data-theme="emerald"] {
            --bg-primary: #064E3B;
            --bg-surface: #F2FBF7;
            --bg-card: #FFFFFF;
            --text-primary: #06392F;
            --text-secondary: #2D5A4C;
            --accent-gold: #D97706;
            --accent-rose: #047857;
            --border-soft: rgba(4, 120, 87, 0.28);
            --ayah-bg: rgba(4, 120, 87, 0.08);
            --hadith-bg: rgba(217, 119, 6, 0.09);
        }
        [data-theme="sand"] {
            --bg-primary: #78350F;
            --bg-surface: #FBF4E6;
            --bg-card: #FFFDF8;
            --text-primary: #291D12;
            --text-secondary: #6B533B;
            --accent-gold: #B45309;
            --accent-rose: #92400E;
            --border-soft: rgba(180, 83, 9, 0.34);
            --ayah-bg: rgba(146, 64, 14, 0.07);
            --hadith-bg: rgba(180, 83, 9, 0.10);
        }
        [data-theme="velvet"] {
            --bg-primary: #0B132B;
            --bg-surface: #131B2E;
            --bg-card: #1C2541;
            --text-primary: #F8FAFC;
            --text-secondary: #CBD5E1;
            --accent-gold: #FBBF24;
            --accent-rose: #34D399;
            --border-soft: rgba(251, 191, 36, 0.30);
            --ayah-bg: rgba(52, 211, 153, 0.10);
            --hadith-bg: rgba(251, 191, 36, 0.10);
        }
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }
        html, body {
            height: 100dvh;
            width: 100%;
            overflow: hidden;
            font-family: 'Amiri', 'Cairo', system-ui, -apple-system, serif;
            background: var(--bg-primary);
            color: var(--text-primary);
        }
        .book-stage {
            height: 100dvh;
            max-height: 100dvh;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 8px 10px;
            gap: 6px;
        }
        .top-bar {
            flex-shrink: 0;
            background: var(--bg-surface);
            border: 1px solid var(--border-soft);
            border-radius: 14px;
            padding: 6px 12px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 6px;
        }
        .top-brand {
            font-size: 13px;
            font-weight: bold;
            color: var(--accent-rose);
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .theme-pills {
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .theme-btn {
            border: 1px solid var(--border-soft);
            background: var(--bg-card);
            color: var(--text-primary);
            border-radius: 99px;
            padding: 3px 8px;
            font-size: 11px;
            cursor: pointer;
            font-weight: 600;
        }
        .theme-btn.active {
            background: var(--accent-gold);
            color: #FFF;
            border-color: var(--accent-gold);
        }
        .page-canvas {
            flex: 1;
            min-height: 0;
            background: var(--bg-surface);
            border: 1.5px solid var(--border-soft);
            border-radius: 18px;
            padding: 16px 16px;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            box-shadow: 0 10px 28px rgba(0,0,0,0.22);
            overflow-y: auto;
        }
        #pageInnerContent {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: center;
            gap: 10px;
            overflow-y: auto;
        }
        .ornament-header {
            text-align: center;
            line-height: 1;
        }
        .chapter-tag {
            align-self: center;
            display: inline-block;
            background: var(--ayah-bg);
            color: var(--accent-rose);
            border: 1px solid var(--border-soft);
            padding: 2px 12px;
            border-radius: 99px;
            font-size: 12px;
            font-weight: bold;
        }
        .page-title {
            text-align: center;
            font-size: 20px;
            color: var(--accent-rose);
            line-height: 1.4;
        }
        .ayah-box {
            background: var(--ayah-bg);
            border-right: 3px solid var(--accent-rose);
            border-radius: 10px;
            padding: 10px 12px;
            text-align: center;
            font-size: 16px;
            line-height: 1.75;
            font-weight: bold;
            color: var(--text-primary);
        }
        .hadith-box {
            background: var(--hadith-bg);
            border-right: 3px solid var(--accent-gold);
            border-radius: 10px;
            padding: 8px 12px;
            font-size: 14px;
            line-height: 1.65;
            color: var(--text-primary);
        }
        .reflection-text {
            font-size: 14.5px;
            line-height: 1.7;
            color: var(--text-primary);
            text-align: justify;
        }
        .action-pill {
            background: var(--bg-card);
            border: 1px dashed var(--accent-gold);
            border-radius: 10px;
            padding: 8px 12px;
            font-size: 13px;
            line-height: 1.55;
            color: var(--text-secondary);
        }
        .toc-grid {
            display: grid;
            grid-template-columns: 1fr;
            gap: 5px;
        }
        .toc-row {
            background: var(--bg-card);
            border: 1px solid var(--border-soft);
            border-radius: 8px;
            padding: 5px 10px;
            font-size: 13px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            cursor: pointer;
            color: var(--text-primary);
        }
        .bottom-nav-dock {
            flex-shrink: 0;
            background: var(--bg-surface);
            border: 1px solid var(--border-soft);
            border-radius: 14px;
            padding: 7px 12px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .nav-btn {
            background: var(--accent-rose);
            color: #FFFFFF;
            border: none;
            border-radius: 10px;
            padding: 6px 14px;
            font-size: 13px;
            font-weight: bold;
            cursor: pointer;
        }
        .nav-btn:disabled {
            opacity: 0.38;
            cursor: default;
        }
        .page-counter {
            font-size: 13px;
            font-weight: bold;
            color: var(--text-primary);
        }
    </style>
</head>
<body data-theme="emerald">
    <div class="book-stage">
        <header class="top-bar">
            <span class="top-brand">✦ أنوارُ السكينة</span>
            <div class="theme-pills">
                <button class="theme-btn" onclick="setBookTheme('ivory', this)">العاجي</button>
                <button class="theme-btn active" onclick="setBookTheme('emerald', this)">الزمردي</button>
                <button class="theme-btn" onclick="setBookTheme('sand', this)">الرملي</button>
                <button class="theme-btn" onclick="setBookTheme('velvet', this)">المسائي</button>
                <button class="theme-btn" id="btnOpenToc" onclick="goToPage(2)">الفهرس</button>
            </div>
        </header>

        <main class="page-canvas">
            <div class="ornament-header">
                <svg width="130" height="18" viewBox="0 0 130 18">
                    <path d="M5,9 L50,9 M80,9 L125,9" stroke="currentColor" stroke-opacity="0.35" stroke-width="1.2"/>
                    <polygon points="65,2 71,9 65,16 59,9" fill="#D97706"/>
                </svg>
            </div>
            <div id="pageInnerContent"></div>
            <div class="ornament-header">
                <svg width="90" height="12" viewBox="0 0 90 12">
                    <circle cx="45" cy="6" r="3" fill="#D97706" fill-opacity="0.65"/>
                </svg>
            </div>
        </main>

        <footer class="bottom-nav-dock">
            <button class="nav-btn" id="btnPrevPage" onclick="prevPage()">السابق</button>
            <span class="page-counter" id="pageCounter">صفحة 1 من 10</span>
            <button class="nav-btn" id="btnNextPage" onclick="nextPage()">التالي</button>
        </footer>
    </div>

    <script>
        const STORAGE_KEY = "islamic_sakina_masterpiece_state_v1";
        const BOOK_DATA = {
            title: "أنوارُ السكينة | إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن",
            subtitle: "إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن",
            author: "مكتبتي — الإصدار الإسلامي الفاخر",
            pages: [
                {
                    type: "cover",
                    chapterTag: "الإصدار النموذجي الفاخر",
                    title: "أنوارُ السكينة",
                    subtitle: "إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن",
                    ayah: "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                    content: "عشرُ محطاتٍ إيمانيةٍ مصممةٍ باتزانٍ بصريٍّ وروحيٍّ، تجمعُ بين جلالِ الآيةِ القرآنيةِ، ونورِ الحديثِ النبويِّ، وعمقِ التدبرِ، ولطيفِ العملِ اليومي.",
                    action: "اسحب بإصبعك يميناً أو يساراً أو للأعلى والأسفل لتقليب الصفحات بانسيابية."
                },
                {
                    type: "dedication",
                    chapterTag: "استفتاح وإهداء",
                    title: "إهداءٌ إلى كلِّ قلبٍ يلتمسُ السكينة",
                    ayah: "﴿ هُوَ الَّذِي أَنْزَلَ السَّكِينَةَ فِي قُلُوبِ الْمُؤْمِنِينَ لِيَزْدَادُوا إِيمَانًا مَعَ إِيمَانِهِمْ ﴾",
                    content: "إلى كلِّ روحٍ أرهقها ضجيجُ الحياةِ فآوت إلى ركنٍ شديد، وإلى كلِّ مؤمنٍ يفتشُ بين آياتِ الكتابِ وسنةِ النبيِّ ﷺ عن بردِ اليقينِ وطمأنينةِ الفؤاد.",
                    action: "اجعل قراءتك هذه خلوةً صادقةً تُجدّد بها عهدَ القلبِ مع الله."
                },
                {
                    type: "toc",
                    chapterTag: "خريطة الكتاب",
                    title: "فهرسُ إشراقاتِ السكينة"
                },
                {
                    chapterTag: "الإشراقة الأولى",
                    title: "السكينةُ تنزلُ بذكرِ الله",
                    ayah: "﴿ الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُمْ بِذِكْرِ اللَّهِ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ [الرعد: 28]",
                    hadith: "قال رسولُ الله ﷺ: «مَثَلُ الذي يَذْكُرُ رَبَّهُ والذي لا يَذْكُرُ رَبَّهُ، مَثَلُ الحَيِّ والمَيِّتِ» رواه البخاري.",
                    content: "الذكرُ ليس مجردَ ألفاظٍ يرددها اللسان، بل هو حضورُ عظمةِ الله في القلبِ حتى تسكنَ خواطرُ القلقِ وتذوبَ وحشةُ الغفلةِ في أنسِ القرب.",
                    action: "عملُ اليوم: خصّص خمسَ دقائقَ بعد كلِّ صلاةٍ لذكرٍ خاشعٍ يواطئُ فيه قلبُك لسانَك."
                },
                {
                    chapterTag: "الإشراقة الثانية",
                    title: "بردُ اليقينِ وحُسنُ الظنِّ بالله",
                    ayah: "﴿ فَمَا ظَنُّكُمْ بِرَبِّ الْعَالَمِينَ ﴾ [الصافات: 87]",
                    hadith: "في الحديث القدسيِّ قال اللهُ تعالى: «أنا عِنْدَ ظَنِّ عَبْدِي بي، وأنا معهُ إذا ذَكَرَنِي» متفقٌ عليه.",
                    content: "حُسنُ الظنِّ باللهِ عبادةٌ قلبيةٌ عظيمة؛ فمَن علمَ أنَّ أزمةَ الأمورِ بيدِ العليمِ الحكيمِ استراحَ من همِّ التدبيرِ، وأيقنَ أنَّ وراءَ كلِّ قدرٍ لطفاً خفياً.",
                    action: "عملُ اليوم: كلما عرضَ لك همٌّ اليوم، ردّد بقلبٍ موقن: «حسبيَ اللهُ ونِعمَ الوكيل»."
                },
                {
                    chapterTag: "الإشراقة الثالثة",
                    title: "أُنسُ السجودِ ومناجاةُ الأسحار",
                    ayah: "﴿ وَاسْجُدْ وَاقْتَرِبْ ﴾ [العلق: 19]",
                    hadith: "قال النبيُّ ﷺ: «أَقْرَبُ ما يَكونُ العَبْدُ مِن رَبِّهِ وهو ساجِدٌ، فأكْثِرُوا الدُّعاءَ» رواه مسلم.",
                    content: "حين يضعُ العبدُ جبهتَه على الأرضِ يرتفعُ بروحِه إلى السماء؛ ففي السجودِ تُسكبُ العبراتُ، وتُجابُ الدعواتُ، ويُجبرُ كسرُ الفؤاد.",
                    action: "عملُ اليوم: أطِلْ سجدةً في صلاتك اليوم، وبُثَّ فيها حاجتَك إلى اللهِ بيقين."
                },
                {
                    chapterTag: "الإشراقة الرابعة",
                    title: "الرضا بالقضاءِ بابُ الفرج",
                    ayah: "﴿ مَا أَصَابَ مِنْ مُصِيبَةٍ إِلَّا بِإِذْنِ اللَّهِ وَمَنْ يُؤْمِنْ بِاللَّهِ يَهْدِ قَلْبَهُ ﴾ [التغابن: 11]",
                    hadith: "قال رسولُ الله ﷺ: «عَجَباً لأَمْرِ المُؤْمِنِ، إنَّ أمْرَهُ كُلَّهُ خَيْرٌ، وليسَ ذاكَ لأَحَدٍ إلَّا لِلْمُؤْمِنِ» رواه مسلم.",
                    content: "قال علقمةُ في قوله تعالى ﴿يَهْدِ قَلْبَهُ﴾: هو الرجلُ تصيبُه المصيبةُ فيعلمُ أنها من عندِ اللهِ فيرضى ويُسلِّم، فيعوضُه اللهُ سكينةً ويقيناً.",
                    action: "عملُ اليوم: استبدل التذمّرَ بالحمد، وتذكّر نعمةً خفيةً أحاطك اللهُ بها اليوم."
                },
                {
                    chapterTag: "الإشراقة الخامسة",
                    title: "سلامةُ الصدرِ ورفقُ اللسان",
                    ayah: "﴿ يَوْمَ لَا يَنْفَعُ مَالٌ وَلَا بَنُونَ * إِلَّا مَنْ أَتَى اللَّهَ بِقَلْبٍ سَلِيمٍ ﴾ [الشعراء: 88-89]",
                    hadith: "قال النبيُّ ﷺ: «لا تَباغَضُوا، ولا تَحاسَدُوا، ولا تَدابَرُوا، وكُونُوا عِبادَ اللَّهِ إخْواناً» متفقٌ عليه.",
                    content: "أعظمُ راحةٍ ينالها المؤمنُ في دنياه قلبٌ نقيٌّ لا يحملُ غلّاً ولا حسداً، ولسانٌ رفيقٌ ينطقُ بالخيرِ أو يصمتُ بحكمة.",
                    action: "عملُ اليوم: اعفُ عمَّن قصّر في حقّك قبل نومك، وادعُ لأخيك بظهرِ الغيب."
                },
                {
                    chapterTag: "الإشراقة السادسة",
                    title: "نورُ القرآنِ ربيعُ القلوب",
                    ayah: "﴿ يَا أَيُّهَا النَّاسُ قَدْ جَاءَتْكُمْ مَوْعِظَةٌ مِنْ رَبِّكُمْ وَشِفَاءٌ لِمَا فِي الصُّدُورِ وَهُدًى وَرَحْمَةٌ لِلْمُؤْمِنِينَ ﴾ [يونس: 57]",
                    hadith: "من دعاءِ النبيِّ ﷺ: «اللَّهُمَّ اجْعَلِ القُرْآنَ رَبِيعَ قَلْبِي، ونُورَ صَدْرِي، وجِلاءَ حُزْنِي، وذَهابَ هَمِّي» رواه أحمد.",
                    content: "القرآنُ يحيي القلوبَ كما يحيي الغيثُ الأرضَ الهامدة؛ فآيةٌ واحدةٌ تتأملُها بقلبٍ حاضرٍ كفيلةٌ بأن تفتحَ لك آفاقاً من البصيرةِ والنور.",
                    action: "عملُ اليوم: اقرأ صفحةً من المصحفِ بتأنٍّ وتدبّر، وتوقّف عند آيةٍ لامست قلبَك."
                },
                {
                    type: "conclusion",
                    chapterTag: "مسكُ الختام",
                    title: "خاتمةٌ ودعاء — عهدُ القلبِ المطمئن",
                    ayah: "﴿ رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِنْ لَدُنْكَ رَحْمَةً إِنَّكَ أَنْتَ الْوَهَّابُ ﴾ [آل عمران: 8]",
                    hadith: "«اللَّهُمَّ إنِّي أسأَلُكَ نَفْساً بكَ مُطْمَئِنَّةً، تُؤْمِنُ بلِقائِكَ، وتَرْضَى بقَضائِكَ، وتَقْنَعُ بعَطائِكَ».",
                    content: "تمّت هذه الإشراقاتُ بعونِ اللهِ وتوفيقِه؛ فاجعلها رفيقاً لأوقاتِك، وارجع إليها كلما اشتاقت روحُك إلى نسائمِ الإيمانِ والسكينة.",
                    action: "تقبّل اللهُ منك صالحَ العمل، وملأ قلبَك بنورِ السكينةِ واليقين."
                }
            ]
        };

        let state = { currentPage: 0, theme: "emerald" };

        function setBookTheme(themeName, btnEl) {
            document.body.setAttribute("data-theme", themeName);
            state.theme = themeName;
            document.querySelectorAll(".theme-btn").forEach(b => {
                if (b.id !== "btnOpenToc") b.classList.remove("active");
            });
            if (btnEl) btnEl.classList.add("active");
            saveState();
        }

        function loadState() {
            try {
                const raw = localStorage.getItem(STORAGE_KEY);
                if (raw) {
                    const parsed = JSON.parse(raw);
                    if (typeof parsed.currentPage === "number") {
                        state.currentPage = Math.max(0, Math.min(BOOK_DATA.pages.length - 1, parsed.currentPage));
                    }
                    if (parsed.theme) {
                        state.theme = parsed.theme;
                        document.body.setAttribute("data-theme", parsed.theme);
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

        function renderCurrentPage() {
            const p = BOOK_DATA.pages[state.currentPage];
            const container = document.getElementById("pageInnerContent");
            if (!p || !container) return;

            if (p.type === "toc") {
                const tocItems = BOOK_DATA.pages.map(function(item, idx) {
                    return '<div class="toc-row" onclick="goToPage(' + idx + ')">' +
                        '<span><strong>' + (idx + 1) + '.</strong> ' + item.title + '</span>' +
                        '<span style="color:var(--accent-gold);font-size:11px;">' + (item.chapterTag || '') + ' ⟵</span>' +
                    '</div>';
                }).join("");
                container.innerHTML =
                    '<span class="chapter-tag">' + p.chapterTag + '</span>' +
                    '<h1 class="page-title">' + p.title + '</h1>' +
                    '<div class="toc-grid">' + tocItems + '</div>';
            } else {
                container.innerHTML =
                    '<span class="chapter-tag">' + (p.chapterTag || '') + '</span>' +
                    '<h1 class="page-title">' + p.title + '</h1>' +
                    (p.ayah ? '<div class="ayah-box">' + p.ayah + '</div>' : '') +
                    (p.hadith ? '<div class="hadith-box">' + p.hadith + '</div>' : '') +
                    (p.content ? '<p class="reflection-text">' + p.content + '</p>' : '') +
                    (p.action ? '<div class="action-pill">✦ ' + p.action + '</div>' : '');
            }

            document.getElementById("pageCounter").textContent = "صفحة " + (state.currentPage + 1) + " من " + BOOK_DATA.pages.length;
            document.getElementById("btnPrevPage").disabled = state.currentPage === 0;
            document.getElementById("btnNextPage").disabled = state.currentPage === BOOK_DATA.pages.length - 1;
            saveState();
        }

        function nextPage() {
            if (state.currentPage < BOOK_DATA.pages.length - 1) {
                state.currentPage++;
                renderCurrentPage();
            }
        }

        function prevPage() {
            if (state.currentPage > 0) {
                state.currentPage--;
                renderCurrentPage();
            }
        }

        function changePage(delta) {
            goToPage(state.currentPage + delta);
        }

        function goToPage(index) {
            if (index >= 0 && index < BOOK_DATA.pages.length) {
                state.currentPage = index;
                renderCurrentPage();
            }
        }

        loadState();
        renderCurrentPage();
    </script>
</body>
</html>
        """.trimIndent()
    }

    fun generateSampleBooks(context: Context): List<Book> {
        val booksDir = File(context.filesDir, "books").apply { mkdirs() }
        val bookDir = File(booksDir, ISLAMIC_MASTERPIECE_ID).apply { mkdirs() }
        val htmlFile = File(bookDir, "index.html")

        val rawHtml = buildIslamicMasterpieceHtml()
        val injectedHtml = SandboxHtmlInjector.injectIntoHtml(rawHtml, ISLAMIC_MASTERPIECE_ID, 10)
        htmlFile.writeText(injectedHtml, Charsets.UTF_8)

        val meta = HtmlBookParser.parse(
            htmlFile,
            "أنوارُ السكينة | إشراقاتٌ قرآنيةٌ وهديٌ نبويٌّ لقلبٍ مطمئن"
        )

        val book = Book(
            id = ISLAMIC_MASTERPIECE_ID,
            title = meta.title,
            subtitle = meta.subtitle,
            author = meta.author,
            description = meta.description,
            folderPath = ISLAMIC_MASTERPIECE_ID,
            entryHtmlPath = "index.html",
            shelfId = null,
            isFavorite = true,
            readingProgress = 0f,
            scrollY = 0,
            currentPage = 1,
            pageCount = meta.pageCount,
            language = meta.language,
            isRtl = meta.isRtl,
            accentRoseHex = meta.accentRoseHex,
            accentGoldHex = meta.accentGoldHex,
            bgPrimaryHex = meta.bgPrimaryHex,
            coverGradientStart = meta.coverGradientStart,
            coverGradientEnd = meta.coverGradientEnd,
            coverIcon = "auto_stories",
            fileCount = 1,
            totalSizeBytes = htmlFile.length(),
            hasExternalCss = true,
            hasExternalJs = true,
            tableOfContentsJson = HtmlBookParser.tocToJson(meta.tableOfContents)
        )

        return listOf(book)
    }
}
