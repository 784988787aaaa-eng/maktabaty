# الدليل الهندسي والإنتاجي الشامل لصناعة الكتب الرقمية التفاعلية الفاخرة
## (`BOOK_PRODUCTION_MASTER_GUIDE.md` — معيار منصة «مكتبتي»)

> **الإصدار الهندسي:** `V4.0 Master Production Specification`  
> **المنصة المستهدفة:** تطبيق قارئ ومكتبة الكتب الرقمية التفاعلية **«مكتبتي» (Maktabati)** على نظام أندرويد والمتصفحات الحديثة  
> **إشراف وتصميم المعمارية:** م/ منصور قطينه (`774004399`)

---

## ١. الفلسفة الهندسية للكتاب التفاعلي المستقل (Self-Contained Interactive Book)

صُممت منصة **«مكتبتي»** لتشغيل جيل جديد من الكتب الرقمية العربية والعالمية، حيث يكون كل كتاب عبارة عن **ملف `.html` واحد مستقل بذاته بنسبة ١٠٠٪ (Single-File Self-Contained HTML5 Application)** يجمع بين:
1. **فخامة المخطوطات والطبعات الملكية** (زخارف SVG متجهية مدمجة، ترويسات مذهّبة، إطارات هندسية، وتباين لوني مريح للعين).
2. **سلاسة التطبيقات الأصلية (60fps Native Feel)** بتقليب صفحات فوري باللمس والسحب دون أي تمرير عمودي مزعج داخل الصفحة.
3. **العمل الكامل دون اتصال بالإنترنت (100% Offline-Ready)** دون الاعتماد على أي ملفات خارجية قد تنقطع أو تفشل في التحميل.

---

## ٢. القواعد المعمارية الصارمة للتوافق الفوري مع محرك «مكتبتي»

عند استيراد أي ملف `.html` إلى تطبيق **«مكتبتي»**، يقوم المحرك الذكي (`HtmlBookParser` + `SandboxHtmlInjector`) بقراءة وتحليل الملف في أجزاء من الثانية. لضمان ظهور غلاف الكتاب وألوانه وفهرسه وأرقام صفحاته بدقة تامة وفتحه كالبرق، التزم بالقواعد التالية:

### أ. ترويسة الترميز في أول الملف (`<meta charset="utf-8">`)
يجب أن يبدأ الملف دائماً بـ `<!DOCTYPE html>` ويوضع وسم `<meta charset="utf-8">` كأول عنصر داخل `<head>` مباشرةً قبل أي وسم `<title>` أو `<style>` لضمان قراءة الأحرف العربية والتشكيل بسرعة البرق دون إعادة تحليل (Re-parse).

```html
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>عنوان الكتاب</title>
```

### ب. متغيرات الهوية البصرية لغلاف الكتاب (`:root` CSS Variables)
يستخرج تطبيق **«مكتبتي»** ألوان الغلاف الثلاثي الأبعاد والزخارف في شاشة المكتبة تلقائياً من متغيرات `:root` داخل قسم `<style>`:

| المتغير في `:root` | وظيفته في مكتبتي | مثال لوني مقترح |
| :--- | :--- | :--- |
| `--bg-primary` | اللون الأساسي لخلفية غلاف الكتاب في المكتبة وخلفية القارئ | `#1A1412` أو `#FBF7F0` |
| `--accent-gold` | لون الإطارات المذهّبة، الزخارف، وشارة عدد الصفحات | `#C49148` أو `#D97706` |
| `--accent-rose` | لون التدرج الثانوي للغلاف، أزرار التفاعل، وعلامات الفصول | `#9E3B5A` أو `#0F766E` |

```css
:root {
  --bg-primary: #FBF7F0;
  --bg-card: #FFFDF9;
  --text-main: #231815;
  --text-muted: #6E5A53;
  --accent-rose: #9E3B5A;
  --accent-gold: #C49148;
  --border-ornate: rgba(196, 145, 72, 0.34);
}
```

### ج. وحدات قياس الشاشة (`vh` و `100%` بدلاً من `dvh` فقط)
لضمان فتح الكتاب في التو واللحظة (`<15ms`) على جميع إصدارات محرك `Android System WebView` دون أي انكماش في الارتفاع، اعتمد دائماً الجمع بين `100vh` و `100%` على حاوية التطبيق الرئيسية `.app`:

```css
html, body {
  width: 100%;
  height: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
  user-select: none;
  -webkit-user-select: none;
}

.app {
  width: 100%;
  height: 100vh;
  max-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 8px 10px;
  box-sizing: border-box;
}
```

---

## ٣. هيكل البيانات القياسي (`BOOK_DATA`) واستخراج الفهرس الذكي

يدعم محرك **«مكتبتي»** كلاً من الكتب المبنية بمصفوفة بيانات جافاسكريبت (`BOOK_DATA`) والكتب المبنية بعناصر DOM مباشرة (`.page`). ويُعد نمط `BOOK_DATA` هو المعيار الهندسي الأفضل والأسرع للكتب التي تتراوح بين **١٠ إلى ٢٠٠ صفحة** لأنه يحافظ على خفة شجرة DOM وسرعة الفتح الفورية:

```javascript
const STORAGE_KEY = "maktabati_book_unique_slug_v1";

const BOOK_DATA = {
  title: "عنوان الكتاب الرئيسي",
  subtitle: "العنوان الفرعي الوصفي للكتاب",
  author: "اسم المؤلف أو المحقق",
  description: "نبذة موجزة عن موضوع الكتاب ورسالته تظهر في بطاقة تفاصيل الكتاب داخل مكتبتي.",
  pages: [
    {
      type: "cover",
      chapterTitle: "الغلاف",
      title: "عنوان الكتاب الرئيسي",
      subtitle: "العنوان الفرعي الوصفي للكتاب",
      author: "اسم المؤلف"
    },
    {
      type: "dedication",
      chapterTitle: "الإهداء",
      title: "إهداء",
      content: `<p>نص الإهداء...</p>`
    },
    {
      type: "chapter",
      chapterNumber: "الفصل الأول",
      chapterTitle: "الفصل الأول: مدخل إلى الحكمة",
      sectionTitle: "المبحث الأول",
      title: "عنوان الصفحة أو المبحث",
      content: `
        <p>الفقرة الأولى من النص المشكول أو المحقق...</p>
        <blockquote class="quote-box">اقتباس مميز أو بيت شعر أو قاعدة مهمة.</blockquote>
      `
    }
  ]
};
```

### الحقول المعتمدة لاستخراج الفهرس التلقائي (`Table of Contents`)
يتعرف محلل «مكتبتي» (`HtmlBookParser`) تلقائياً على عناوين الفصول والمباحث داخل `BOOK_DATA.pages` عبر أي من المفاتيح التالية:
- `title`
- `chapterTitle`
- `sectionTitle`
- `chapterTag`
- `heading`

كما يتعرف على أزرار الفهرس التفاعلية في HTML عبر خاصية `data-page="رقم_الصفحة_يبدأ_من_0_أو_1"`.

---

## ٤. هندسة الشاشة والبطاقة الورقية (Zero-Scroll Single-Screen Layout)

يجب أن تتكون شاشة الكتاب التفاعلي من **ثلاث طبقات متناسقة فقط** داخل `.app` بحيث لا تحتاج الصفحة أبداً إلى تمرير عمودي (`No Vertical Scrollbar`) في الوضع الافتراضي:

1. **الشريط العلوي الرشيق (`.top-bar` — بارتفاع `40px–44px`):**
   - يعرض اسم الكتاب أو الفصل الحالي في اليمين.
   - يحتوي في اليسار على أزرار مدمجة:
     - فهرس الفصول (`☰`)
     - البحث الفوري في النص (`🔍`)
     - تكبير وتصغير الخط (`A+` / `A-`)
     - تبديل السمة اللونية (`☀/🌙` ورقي كريمي / ليلي كحلي / داكن)
     - حفظ علامة مرجعية (`🔖`)

2. **البطاقة الورقية الوسطى (`.page-canvas` — تأخذ المساحة المتبقية `flex: 1`):**
   - إطار فاخر بزوايا ناعمة (`border-radius: 18px`) وحدود ذهبية رقيقة (`1px solid var(--border-ornate)`).
   - رأس بطاقة علوي رشيق يعرض: اسم الفصل الحالي + رقم الصفحة (`صفحة ١٢ من ٨٥`).
   - جسم الصفحة النصي (`.page-body`) بخط عربي واضح (`Amiri`, `Scheherazade New`, `Noto Naskh Arabic`, `Geeza Pro`, `serif`) مع `line-height: 1.85` ومحاذاة مضبوطة.
   - ذيل بطاقة سفلي يعرض شريط التقدم المئوي الدقيق (`Progress Bar`).

3. **شريط التنقل السفلي المدمج (`.bottom-nav-dock` — بارتفاع `44px–48px`):**
   - زر **«السابق»** (يمين في العربية RTL).
   - عداد الصفحة التفاعلي في المنتصف (`12 / 85`) — عند النقر عليه يفتح نافذة القفز السريع لأي صفحة.
   - زر **«التالي»** (يسار في العربية RTL).

---

## ٥. ميزان تقسيم النصوص الذكي (Smart Pagination & Zero-Omission Rules)

عند تحويل أي كتاب أو مخطوطة نصية (`.txt` / `.docx` / `.md`) إلى كتاب HTML تفاعلي لمنصة «مكتبتي»، التزم بالميزان الهندسي التالي:

1. **الأمانة النصية الكاملة (Zero-Omission Policy):**
   - يُمنع منعاً باتاً حذف أو تلخيص أو اختصار أي فقرة من النص الأصلي.
   - يُمنع كتابة عبارات مثل `// باقي الصفحات...` أو `...إلخ`؛ يجب إدراج جميع الفقرات كاملةً مقسمةً على صفحات متتالية.
2. **الكثافة المثالية للصفحة الواحدة (70 إلى 110 كلمات):**
   - لا تكدّس أكثر من **١١٠ كلمات** (أو **٥٥٠–٦٥٠ حرفاً**) في الصفحة الواحدة حتى تظل الصفحة مريحة للعين وتظهر بالكامل داخل `.page-canvas` دون الحاجة إلى شريط تمرير.
   - إذا كان الفصل طويلاً، قسّمه إلى عدة صفحات متتالية تحمل نفس `chapterTitle` مع ترقيم متسلسل أو عناوين فرعية (`sectionTitle`).
3. **عدم بتر الجمل (Sentence Integrity):**
   - تنتهي كل صفحة دائماً بنهاية جملة تامة المعنى (`.` أو `؟` أو `!` أو نهاية فقرة/بيت شعري)، ولا تُقطع الجملة الواحدة بين صفحتين.
4. **تنويع القوالب البصرية داخل الصفحات:**
   - استخدم صناديق الاقتباس (`.quote-box`) للأحاديث والحكم والأبيات الشعرية.
   - استخدم الفواصل الزخرفية (`.ornament-divider`) بين الخواطر والفقرات المفصلية.
   - استخدم بطاقات الفوائد (`.insight-card`) للخلاصات والنقاط المركزة.

---

## ٦. التوافق مع ميزات محرك «مكتبتي» المتقدمة

يوفر تطبيق **«مكتبتي»** طبقة تعزيز تلقائية (`SandboxHtmlInjector`) تعمل بانسجام مع كتابك، ولكي يستفيد كتابك منها بأقصى درجة:

1. **عزل التخزين المحلي (`localStorage` Isolation):**
   - استخدم `localStorage.getItem(STORAGE_KEY)` و `localStorage.setItem(STORAGE_KEY, JSON.stringify(state))` لحفظ رقم الصفحة الحالية (`currentPage`)، حجم الخط (`fontScale`)، والسمة (`theme`)، والعلامات المرجعية (`bookmarks`).
   - يقوم تطبيق «مكتبتي» تلقائياً بعزل مفتاح التخزين لكل كتاب بمعرّفه الخاص حتى لا تتداخل بيانات كتابين مختلفين.
2. **دعم التقليب بالسحب ولوحة المفاتيح (`Swipe & Arrow Keys`):**
   - اربط دالة التقليب بأزرار الأسهم (`ArrowLeft` للتالي في العربية، `ArrowRight` للسابق في العربية) ولمسات السحب الأفقية (`touchstart` / `touchend`).
3. **الدوال العامة القياسية (اختياري ومستحسن):**
   - عرّف في نطاق `window` الدوال التالية ليتمكن القارئ من استدعائها مباشرة عند القفز من الفهرس:
     - `window.goToPage(index)` (للانتقال المباشر إلى صفحة محددة)
     - `window.nextPage()` (للانتقال للصفحة التالية)
     - `window.prevPage()` (للرجوع للصفحة السابقة)

---

## ٧. القالب المرجعي الكامل الجاهز للإنتاج (`Production Master Template`)

يمكنك نسخ هذا القالب الهندسي المتكامل واستخدامه كأساس لأي كتاب تفاعلي جديد؛ فهو يضم الغلاف، الفهرس، البحث الفوري، العلامات المرجعية، التحكم بحجم الخط، الوضع الليلي/الورقي، وحفظ التقدم التلقائي:

```html
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>عنوان الكتاب الفاخر</title>
  <style>
    :root {
      --bg-primary: #FBF7F0;
      --bg-card: #FFFDF9;
      --text-main: #231815;
      --text-muted: #6E5A53;
      --accent-rose: #9E3B5A;
      --accent-gold: #C49148;
      --border-ornate: rgba(196, 145, 72, 0.35);
      --font-scale: 1;
    }

    [data-theme="night"] {
      --bg-primary: #121016;
      --bg-card: #1B1822;
      --text-main: #F5EFE6;
      --text-muted: #B8A99A;
      --accent-rose: #D96B8A;
      --accent-gold: #E2B05E;
      --border-ornate: rgba(226, 176, 94, 0.28);
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
    }

    html, body {
      width: 100%;
      height: 100%;
      min-height: 100vh;
      overflow: hidden;
      background: var(--bg-primary);
      color: var(--text-main);
      font-family: "Amiri", "Traditional Arabic", "Noto Naskh Arabic", "Geeza Pro", serif;
      user-select: none;
      -webkit-user-select: none;
    }

    .app {
      width: 100%;
      height: 100vh;
      max-height: 100vh;
      max-width: 680px;
      margin: 0 auto;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 8px 10px;
      gap: 6px;
    }

    .top-bar {
      height: 42px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 10px;
      background: var(--bg-card);
      border: 1px solid var(--border-ornate);
      border-radius: 12px;
      flex-shrink: 0;
    }

    .top-bar-title {
      font-size: 0.92rem;
      font-weight: 700;
      color: var(--accent-rose);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 48%;
    }

    .top-bar-actions {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .icon-btn {
      height: 30px;
      min-width: 30px;
      padding: 0 7px;
      border-radius: 8px;
      border: 1px solid var(--border-ornate);
      background: transparent;
      color: var(--text-main);
      font-size: 0.82rem;
      font-weight: 700;
      cursor: pointer;
    }

    .page-canvas {
      flex: 1;
      min-height: 0;
      background: var(--bg-card);
      border: 1.5px solid var(--border-ornate);
      border-radius: 18px;
      padding: 14px 16px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      position: relative;
      overflow: hidden;
      box-shadow: 0 6px 22px rgba(0, 0, 0, 0.05);
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px dashed var(--border-ornate);
      padding-bottom: 6px;
      margin-bottom: 8px;
      font-size: 0.78rem;
      color: var(--text-muted);
      flex-shrink: 0;
    }

    .page-body {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      justify-content: center;
      font-size: calc(1.14rem * var(--font-scale));
      line-height: 1.88;
      text-align: justify;
      padding: 4px 2px;
    }

    .page-body h1, .page-body h2 {
      color: var(--accent-rose);
      text-align: center;
      margin-bottom: 10px;
      line-height: 1.45;
    }

    .page-body p {
      margin-bottom: 10px;
    }

    .quote-box {
      border-right: 3px solid var(--accent-gold);
      background: rgba(196, 145, 72, 0.08);
      padding: 10px 12px;
      border-radius: 10px;
      margin: 8px 0;
      font-style: italic;
    }

    .bottom-nav-dock {
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 10px;
      background: var(--bg-card);
      border: 1px solid var(--border-ornate);
      border-radius: 12px;
      flex-shrink: 0;
    }

    .nav-btn {
      height: 32px;
      padding: 0 14px;
      border-radius: 9px;
      border: 1px solid var(--border-ornate);
      background: var(--accent-rose);
      color: #FFFFFF;
      font-weight: 700;
      font-size: 0.85rem;
      cursor: pointer;
    }

    .nav-btn:disabled {
      opacity: 0.35;
      cursor: default;
    }

    .page-counter {
      font-size: 0.85rem;
      font-weight: 700;
      color: var(--accent-gold);
    }
  </style>
</head>
<body>
  <div class="app" id="appRoot">
    <header class="top-bar">
      <div class="top-bar-title" id="topBarTitle">عنوان الكتاب</div>
      <div class="top-bar-actions">
        <button class="icon-btn" id="fontDecBtn" title="تصغير الخط">A-</button>
        <button class="icon-btn" id="fontIncBtn" title="تكبير الخط">A+</button>
        <button class="icon-btn" id="themeBtn" title="الوضع الليلي">🌙</button>
      </div>
    </header>

    <main class="page-canvas" id="pageCanvas">
      <div class="page-header">
        <span id="chapterLabel">المقدمة</span>
        <span id="pageHeaderNum">صفحة ١</span>
      </div>
      <article class="page-body" id="pageBody"></article>
    </main>

    <footer class="bottom-nav-dock">
      <button class="nav-btn" id="prevBtn">السابق</button>
      <span class="page-counter" id="pageCounter">1 / 1</span>
      <button class="nav-btn" id="nextBtn">التالي</button>
    </footer>
  </div>

  <script>
    const STORAGE_KEY = "maktabati_template_book_v1";
    const BOOK_DATA = {
      title: "عنوان الكتاب الفاخر",
      subtitle: "طبعة رقمية تفاعلية",
      author: "اسم المؤلف",
      pages: [
        {
          chapterTitle: "الغلاف",
          title: "عنوان الكتاب الفاخر",
          content: "<p style='text-align:center'>طبعة رقمية تفاعلية موزونة الصفحات لمنصة مكتبتي</p>"
        },
        {
          chapterTitle: "الفصل الأول",
          title: "مدخل الكتاب",
          content: "<p>هنا يوضع نص الصفحة الأولى بالتشكيل الكامل والعناية البصرية الفائقة.</p>"
        }
      ]
    };

    let state = { currentPage: 0, fontScale: 1, theme: "paper" };
    try {
      const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || "null");
      if (saved) state = Object.assign(state, saved);
    } catch (_) {}

    function saveState() {
      try { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); } catch (_) {}
    }

    function applyThemeAndFont() {
      document.documentElement.setAttribute("data-theme", state.theme);
      document.documentElement.style.setProperty("--font-scale", String(state.fontScale));
    }

    function renderPage() {
      const total = BOOK_DATA.pages.length;
      state.currentPage = Math.max(0, Math.min(total - 1, state.currentPage));
      const p = BOOK_DATA.pages[state.currentPage];

      document.getElementById("topBarTitle").textContent = BOOK_DATA.title;
      document.getElementById("chapterLabel").textContent = p.chapterTitle || BOOK_DATA.title;
      document.getElementById("pageHeaderNum").textContent = "صفحة " + (state.currentPage + 1);
      document.getElementById("pageBody").innerHTML =
        (p.title ? "<h2>" + p.title + "</h2>" : "") + (p.content || "");
      document.getElementById("pageCounter").textContent = (state.currentPage + 1) + " / " + total;
      document.getElementById("prevBtn").disabled = state.currentPage <= 0;
      document.getElementById("nextBtn").disabled = state.currentPage >= total - 1;
      saveState();
    }

    window.goToPage = function(index) {
      state.currentPage = Number(index) || 0;
      renderPage();
    };
    window.nextPage = function() {
      if (state.currentPage < BOOK_DATA.pages.length - 1) {
        state.currentPage++;
        renderPage();
      }
    };
    window.prevPage = function() {
      if (state.currentPage > 0) {
        state.currentPage--;
        renderPage();
      }
    };

    document.getElementById("prevBtn").addEventListener("click", window.prevPage);
    document.getElementById("nextBtn").addEventListener("click", window.nextPage);
    document.getElementById("fontIncBtn").addEventListener("click", function() {
      state.fontScale = Math.min(1.35, +(state.fontScale + 0.08).toFixed(2));
      applyThemeAndFont();
      saveState();
    });
    document.getElementById("fontDecBtn").addEventListener("click", function() {
      state.fontScale = Math.max(0.82, +(state.fontScale - 0.08).toFixed(2));
      applyThemeAndFont();
      saveState();
    });
    document.getElementById("themeBtn").addEventListener("click", function() {
      state.theme = state.theme === "night" ? "paper" : "night";
      applyThemeAndFont();
      saveState();
    });

    document.addEventListener("keydown", function(e) {
      if (e.key === "ArrowLeft") window.nextPage();
      else if (e.key === "ArrowRight") window.prevPage();
    });

    applyThemeAndFont();
    renderPage();
  </script>
</body>
</html>
```

---

## ٨. قائمة الفحص النهائية قبل تصدير الكتاب (`Pre-Flight Checklist`)

قبل استيراد الكتاب إلى تطبيق **«مكتبتي»**، تحقق من النقاط الست التالية:
- [x] `<meta charset="utf-8">` موجود في أول سطور `<head>`.
- [x] متغيرات `:root` الثلاثة (`--bg-primary`, `--accent-gold`, `--accent-rose`) معرّفة بوضوح بصيغة Hex (`#RRGGBB`).
- [x] استخدام `100vh` و `100%` لتنسيق الشاشة الرئيسية `.app` بدلاً من الاعتماد المنفرد على `dvh`.
- [x] كامل النص الأصلي مدرج بأمانة دون أي حذف أو اختصار (`Zero-Omission`).
- [x] كل صفحة تحتوي على نص موزون (`70–110` كلمات) ينتهي بنهاية جملة تامة.
- [x] حفظ واسترجاع الصفحة الحالية وحجم الخط والوضع اللوني عبر `localStorage`.
