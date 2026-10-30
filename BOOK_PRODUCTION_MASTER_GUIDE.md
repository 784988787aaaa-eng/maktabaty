# الدليل الهندسي والإنتاجي الشامل لصناعة الكتب الرقمية التفاعلية الفاخرة
## (`BOOK_PRODUCTION_MASTER_GUIDE.md` — معيار منصة «مكتبتي» V6.0)

> **الإصدار الهندسي:** `V6.0 Fixed Page Count, Fluid Reflow & 4-Corner Page-Curl Specification`  
> **المنصة المستهدفة:** تطبيق قارئ ومكتبة الكتب الرقمية التفاعلية **«مكتبتي» (Maktabati)** على الهواتف الذكية والأجهزة اللوحية  
> **إشراف وتصميم المعمارية:** م/ منصور قطينه (`774004399`)

---

## ١. الفلسفة الهندسية للكتاب التفاعلي المتدفق (Anti-PDF Fluid Interactive Book)

صُممت منصة **«مكتبتي»** للقضاء نهائياً على جمود ملفات الـ PDF التقليدية التي تخرج نصوصها وعناصرها خارج الشاشة عند التكبير. في معيار **«مكتبتي V6.0»**، يعمل كل كتاب كـ **تطبيق HTML5/CSS3/JS تفاعلي متجاوب بنسبة ١٠٠٪** (ملف `.html` مستقل أو حزمة `.zip` غنية بالصور والصوتيات وملفات `styles/book.css` و `scripts/book.js`) يجمع بين:
1. **ثبات عدد الصفحات الدقيق (`Fixed Exact Page Count`):** يُحسب عدد صفحات الكتاب الفعلي بدقة ١٠٠٪ من بنية الكتاب الحقيقية (`BOOK_DATA.pages` أو وسوم `data-page="0..N"` أو وسوم `<article class="page">` المستقلة أو عداد `#pageCount`)، ويظل عدد الصفحات **ثابتاً تماماً** لا يتغير أبداً عند التكبير أو التصغير، وعند الوصول لآخر صفحة يسجل الكتاب نسبة إتمام **`100%`** على رف المكتبة.
2. **التدفق المتجاوب الذكي (`Fluid Reflow Zoom`):** عند التكبير أو التصغير بإصبعين (`Pinch-to-Zoom`) أو عبر أزرار (`+` / `-` / `100%`)، تعيد جميع النصوص والعناوين والبطاقات التفاعلية والاقتباسات والزخارف ترتيب وتكييف نفسها تلقائياً داخل عرض الورقة (`max-width: 100%; overflow-x: hidden;`) دون أي قصّ أفقي ودون تغيير عدد صفحات الكتاب.
3. **حارس تناسق الصفحة (`MaktabatiLayoutGuard`):** فاحص تلقائي يصلح أي عنصر أعرض من بطاقة الصفحة، وينبّه القارئ برسالة ذكية (`Smart Toast Banner`) عند الوصول لأقصى تكبير يحفظ جمال القالب مع زر فوري للعودة إلى `100% الوضع المثالي`.
4. **انحناء الورقة الفيزيائي من الزوايا الأربع (`4-Corner Page-Curl Engine`):** استجابة حية (60fps) لحركة إصبع القارئ من أي زاوية من الزوايا الأربع (العلوية اليمنى، السفلية اليمنى، العلوية اليسرى، السفلية اليسرى) مع الوجه الخلفي للورقة والظلال المتحركة وصوت تقليب الورق الطبيعي المولّد عبر `Web Audio API`.

---

## ٢. المواصفات القياسية المضادة لجمود الـ PDF وقواعد هيكلة الصفحات

لضمان عمل الكتاب بتناسق تام مع محلل الصفحات الدقيق (`HtmlBookParser`) ومحرك `Fluid Reflow Zoom` وحارس `MaktabatiLayoutGuard`، التزم بالقواعد الهندسية الصارمة التالية:

### أ. متغيرات المقياس الديناميكي والهوية البصرية في `:root`
يدعم محلل «مكتبتي» قراءة متغيرات الألوان سواء من وسم `<style>` داخل ملف الـ HTML أو من ملفات CSS المرفقة (`styles/book.css`):

```css
:root {
  --bg-primary: #FBF7F0; /* أو --paper: #FFFDF9 */
  --bg-card: #FFFDF9;
  --text-main: #231815;
  --text-muted: #6E5A53;
  --accent-rose: #9E3B5A;
  --accent-gold: #C49148;
  --border-ornate: rgba(196, 145, 72, 0.35);

  /* متغيرات محرك التكبير المتجاوب Fluid Reflow Zoom */
  --reader-font: 1;
  --reader-font-size: 100%;
  --font-scale: 1;
}
```

### ب. القواعد القياسية لعدّ الصفحات ومزامنة التقدم (`100%` عند الصفحة الأخيرة)
يعتمد محرك «مكتبتي V6.0» سلم أولويات صارماً لتحديد العدد الفعلي لصفحات الكتاب:
1. **الأولوية الأولى (`BOOK_DATA.pages`):** مصفوفة كائنات الصفحات في الجافاسكربت (`BOOK_DATA = { pages: [ ... ] }`).
2. **الأولوية الثانية (`data-page="0..N"`):** وسوم الصفحات المرقمة صراحةً مثل `<article class="page active" data-page="0">...` حتى `data-page="5"`.
3. **الأولوية الثالثة (الكلاس المستقل `page` أو `book-page`):** وسوم `<article class="page">` أو `<section class="page">` أو `<div class="page">`، مع عدم خلطها أبداً بالكلاسات الفرعية ذات الشرطات مثل `page-heading`, `page-flower`, `page-number`, `page-canvas`, `page-footer`.
4. **الأولوية الرابعة (عداد الصفحات الصريح):** `<span id="pageCount">1 / 6</span>` أو `<span id="pageCounter">1 من 6</span>`.

### ج. الوحدات النسبية المرنة ومنع الأبعاد الصلبة (Zero Rigid Pixel Widths)
- **يُمنع منعاً باتاً** إعطاء عرض ثابت بالبكسل (`width: 500px` أو `min-width: 420px`) لأي بطاقة أو جدول أو صورة أو صندوق اقتباس.
- استخدم دائماً الوحدات النسبية المرنة (`em`, `rem`, `clamp()`, `%`, `100dvh` مع بديل `100vh`):
  - حجم خط متن الصفحة: `font-size: calc(clamp(1.02rem, 2.8vw, 1.22rem) * var(--reader-font, 1));`
  - حجم العناوين الرئيسية: `font-size: calc(clamp(1.25rem, 4vw, 1.6rem) * var(--reader-font, 1));`
  - حجم البطاقات والصناديق التفاعلية: `width: 100%; max-width: 100%; padding: clamp(0.65em, 2.5vw, 1.15em); box-sizing: border-box; overflow-wrap: anywhere;`

### د. التوافق مع محرك انحناء الزوايا الأربع وحماية العناصر التفاعلية
- امنح العناصر التفاعلية داخل الصفحة الأصناف القياسية المعتمدة في محرك «مكتبتي» حتى يحميها المحرك تلقائياً من تداخل سحب الصفحة:
  - البطاقات القابلة للقلب: `.flip-card`
  - خيارات الاختبارات التفاعلية: `.quiz-option`
  - حقول إدخال اسم القارئ والتدوين: `input`, `textarea`, `select`, `button`

---

## ٣. الأمر الهندسي الشامل الجاهز للنسخ (Master Universal Copy-Paste Prompt)

انسخ هذا الأمر وأرفق معه أي ملف نصي (`.txt`) في أي نموذج ذكاء اصطناعي لإنتاج كتاب تفاعلي متكامل وفق معيار **«مكتبتي V6.0»**:

```text
أنت مصمم ومبرمج كتب رقمية تفاعلية فاخرة بصيغة HTML5/CSS3/JS في ملف واحد مستقل (Self-Contained Interactive Book) مخصص للعمل داخل تطبيق قارئ الكتب "مكتبتي" على الهواتف الذكية.

المهمة:
اقرأ ملف النص المرفق (.txt) بالكامل، وحوّله إلى كتاب رقمي تفاعلي فاخر وموزون الصفحات بصيغة .html وفق القواعد الهندسية الصارمة التالية:

1. الأمانة النصية الكاملة وتقسيم الصفحات الذكي الثابت (Zero-Omission Fixed Pagination):
- استخرج تلقائياً: عنوان الكتاب، العنوان الفرعي، اسم المؤلف، الإهداء، الفصول، والخاتمة.
- ممنوع الحذف أو التلخيص: أدرج كامل نصوص وفقرات الملف المرفق بالتتابع داخل مصفوفة BOOK_DATA.pages (أو داخل وسوم <article class="page" data-page="0..N">).
- قسّم النص إلى صفحات موزونة (70 إلى 105 كلمات في الصفحة الواحدة) تنتهي دائماً بنهاية جملة تامة، بحيث يظل عدد الصفحات الإجمالي ثابتاً تماماً ولا يتغير عند تكبير أو تصغير الخط.

2. التصميم المتجاوب المضاد لجمود الـ PDF (Fluid Reflow Zoom Standard):
- عرّف في :root المتغيرات: --bg-primary, --bg-card, --text-main, --text-muted, --accent-rose, --accent-gold, --reader-font: 1, --reader-font-size: 100%, --font-scale: 1.
- استخدم الوحدات المرنة (em, rem, clamp(), %, 100vh/100dvh) مع max-width: 100%; box-sizing: border-box; overflow-x: hidden; overflow-wrap: anywhere; حتى تعيد النصوص والبطاقات والزخارف ترتيب نفسها بانسيابية داخل الصفحة الحالية عند التكبير من 75% إلى 200%.

3. التفاعلات الحية المدمجة ومزامنة التقدم (Live Interactive Components & Bridge Sync):
- حقل تخصيص اسم القارئ/القارئة في بداية الكتاب يحفظ الاسم في localStorage ويخاطب القارئ باسمه في فصول الكتاب.
- بطاقات قابلة للقلب (.flip-card) للخلاصات والفوائد تتقلب عند اللمس.
- مساحة تدوين وتأمل شخصي (textarea) لكل فصل تُحفظ تلقائياً في localStorage.
- سؤال أو اختبار تفاعلي (.quiz-option) في نهاية كل فصل مع تغذية راجعة فورية.
- دعم الدوال القياسية window.goToPage(index) و window.nextPage() و window.prevPage() وتحديث عداد الصفحات (#pageCount أو #pageCounter) بصيغة "1 / N".
```

---

## ٤. قوالب الأوامر المخصصة لكل فئة مستهدفة (Audience-Specific Copy-Paste Prompts)

فيما يلي القوالب الهندسية العشرة المخصصة لكل فئة مستهدفة، مع لوحات الألوان (`:root`)، والخطوط، والتفاعلات المصممة نفسياً وبصرياً لكل فئة:

---

### ١. فئة الشباب (`Youth / Young Men Prompt`)
- **الطابع البصري:** حيوي، عصري، كحلي عميق مع فيروزي مشرق وذهبي مضيء.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (الشباب) بهوية بصرية حماسية عصرية:
- لوحة الألوان في :root:
  --bg-primary: #0F172A; --bg-card: #1E293B; --text-main: #F8FAFC; --text-muted: #94A3B8; --accent-rose: #0EA5E9; --accent-gold: #F59E0B; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Cairo", "Tajawal", "Amiri", sans-serif؛ مع التزام كامل بمعيار Fluid Reflow (وحدات rem و em و clamp() ومنع أي عرض ثابت بالبكسل).
- التفاعلات:
  1) حقل إدخال اسم الشاب القارئ في الصفحة الافتتاحية ("أهلاً بك يا بطل: ...") يُحفظ في localStorage.
  2) بطاقات تحدي اليوم قابلة للقلب (.flip-card) تعرض الفكرة على الوجه الأول والخطوة العملية على الوجه الخلفي.
  3) صندوق خطة العمل الشخصي (textarea محفوظة) + اختبار تفاعلي سريع (.quiz-option) بنهاية كل فصل.
```

---

### ٢. فئة الفتيات (`Young Women / Girls Prompt`)
- **الطابع البصري:** وردي لؤلؤي ناعم، بنفسجي حالم، وزخارف زهرية رقيقة.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (الفتيات) بهوية بصرية أنيقة وملهمة:
- لوحة الألوان في :root:
  --bg-primary: #FDF2F8; --bg-card: #FFFFFF; --text-main: #3B0764; --text-muted: #7E22CE; --accent-rose: #EC4899; --accent-gold: #D97706; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Tajawal", "Amiri", serif؛ مع وحدات مرنة (em, rem, clamp) تعيد ترتيب العناصر بنعومة عند التكبير والتصغير دون أي قصّ أفقي.
- التفاعلات:
  1) ترحيب شخصي باسم القارئة ("صديقتي الغالية ...") يُحفظ في localStorage.
  2) بطاقات إشراقة ملهمة قابلة للقلب (.flip-card) وزخارف SVG ناعمة.
  3) ركن "مذكرتي اللطيفة" لتدوين المشاعر والأفكار (textarea محفوظة) + سؤال تفاعلي لطيف (.quiz-option).
```

---

### ٣. فئة النساء (`Women General Prompt`)
- **الطابع البصري:** ملكي دافئ يجمع بين العنابي المخملي والذهبي الوردي والكريمي الفاخر.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (النساء) بطابع ملكي دافئ:
- لوحة الألوان في :root:
  --bg-primary: #FAF5F0; --bg-card: #FFFDFB; --text-main: #2D161E; --text-muted: #6E4756; --accent-rose: #9E3B5A; --accent-gold: #C49148; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Noto Naskh Arabic", serif؛ بتصميم متدفق متجاوب (Fluid Reflow Zoom) يتكيف مع أي حجم خط من 75% إلى 200%.
- التفاعلات:
  1) تخصيص اسم القارئة في البطاقة الترحيبية وحفظه في localStorage.
  2) بطاقات حكمة وإلهام قابلة للقلب (.flip-card) + صناديق اقتباس مذهّبة (.quote-box).
  3) مساحة تأملات خاصة للقارئة + تقييم ذاتي تفاعلي (.quiz-option) في ختام كل فصل.
```

---

### ٤. فئة العازبات (`Single Women Prompt`)
- **الطابع البصري:** بنفسجي خزامى راقٍ (`Lavender & Rose Gold`) يبعث على التفاؤل وبناء الذات والاستعداد الواعي للمستقبل.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (العازبات) بهوية بصرية تبعث على السكينة والطموح:
- لوحة الألوان في :root:
  --bg-primary: #F8F5FF; --bg-card: #FFFFFF; --text-main: #2E1065; --text-muted: #6B21A8; --accent-rose: #C026D3; --accent-gold: #D97706; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Tajawal", serif؛ مع التزام صارم بالوحدات النسبية المرنة (em, rem, clamp) ومنع جمود الـ PDF.
- التفاعلات:
  1) حقل اسم القارئة لتخصيص الرسائل التوجيهية داخل الفصول.
  2) بطاقات "خطوة نحو مستقبلي" القابلة للقلب (.flip-card) + قائمة أهداف شخصية تفاعلية.
  3) دفتر تدوين الطموحات والدعوات (محفوظ في localStorage) + أسئلة وعي ذاتي (.quiz-option).
```

---

### ٥. فئة المتزوجات (`Married Women Prompt`)
- **الطابع البصري:** مودة وسكينة، عاجي دافئ مع ياقوتي عميق وذهبي هادئ.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (المتزوجات) بهوية بصرية دافئة تفيض بالمودة والسكينة:
- لوحة الألوان في :root:
  --bg-primary: #FFF9F5; --bg-card: #FFFFFF; --text-main: #3B1219; --text-muted: #7F3A47; --accent-rose: #BE123C; --accent-gold: #B45309; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Scheherazade New", serif؛ مع تصميم متجاوب يعيد ترتيب البطاقات والنصوص بانسيابية عند التكبير والتصغير.
- التفاعلات:
  1) تخصيص اسم الزوجة القارئة في مقدمة الكتاب.
  2) بطاقات "همسة مودة" و"مفتاح السكينة الأسرية" القابلة للقلب (.flip-card).
  3) مفكرة "تطبيقات عملية في بيتي" (textarea محفوظة في localStorage) + مواقف زوجية تفاعلية (.quiz-option).
```

---

### ٦. فئة الرجال (`Men Prompt`)
- **الطابع البصري:** وقار وفخامة كلاسيكية، بني جلدي عتيق أو كحلي داكن مع ذهبي نحاسي وورق كريمي.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (الرجال) بطابع فخم يتسم بالوقار والرصانة:
- لوحة الألوان في :root:
  --bg-primary: #F5F2EB; --bg-card: #FCF9F2; --text-main: #1C1917; --text-muted: #57534E; --accent-rose: #1E3A8A; --accent-gold: #B45309; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Noto Naskh Arabic", serif؛ مع وحدات نسبية مرنة (em, rem, clamp) وتوافق كامل مع محرك انحناء الورقة من الزوايا الأربع.
- التفاعلات:
  1) حقل اسم القارئ الكريم لحفظه ومخاطبته في الخلاصات القيادية.
  2) بطاقات "خلاصة الحكمة والقيادة" القابلة للقلب (.flip-card) + اقتباسات مؤطرة (.quote-box).
  3) سجل القرارات والملاحظات العملية (محفوظ في localStorage) + اختبارات مواقف عملية (.quiz-option).
```

---

### ٧. فئة الملتزمين والكتب الإسلامية (`Committed & Islamic Books Prompt`)
- **الطابع البصري:** روحاني مهيب على طراز المصاحف والمخطوطات الأندلسية (زمردي إسلامي، ذهبي مملوكي، ورق مخطوطات عاجي).
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب إسلامي رقمي تفاعلي فاخر بصيغة HTML واحد لمنصة "مكتبتي" بهوية المخطوطات الإسلامية الملكية:
- لوحة الألوان في :root:
  --bg-primary: #F8F4E8; --bg-card: #FFFDF7; --text-main: #1B241E; --text-muted: #4A5D50; --accent-rose: #065F46; --accent-gold: #B47B26; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Scheherazade New", "Traditional Arabic", serif؛ مع إبراز الآيات القرآنية في صناديق مذهّبة والأحاديث النبوية في إطارات زمردية تتدفق بمرونة تامة عند أي تكبير.
- التفاعلات:
  1) حقل اسم القارئ/القارئة في استفتاح الكتاب مع دعاء مخصص باسمه.
  2) بطاقات "فقه العمل بالعلم" و"لطائف التدبر" القابلة للقلب (.flip-card).
  3) سجل "محاسبة النفس والعمل اليومي" (محفوظ في localStorage) + أسئلة مراجعة الفوائد الشرعية (.quiz-option).
```

---

### ٨. فئة المراهقون (`Teens Prompt`)
- **الطابع البصري:** تفاعلي مشوّق بألوان نابضة ومتدرجة، فقاعات حوارية، وتحديات نقاط.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب رقمي تفاعلي مشوّق بصيغة HTML واحد لمنصة "مكتبتي" موجّه لفئة (المراهقين):
- لوحة الألوان في :root:
  --bg-primary: #EEF2FF; --bg-card: #FFFFFF; --text-main: #1E1B4B; --text-muted: #4338CA; --accent-rose: #4F46E5; --accent-gold: #F59E0B; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Tajawal", "Cairo", sans-serif؛ مع وحدات مرنة (em, rem, clamp) تمنع أي خروج للعناصر عن حدود الصفحة.
- التفاعلات:
  1) بطاقة "ملف البطل/البطلة" لإدخال اسم القارئ وحفظه في localStorage.
  2) بطاقات أسرار واكتشافات قابلة للقلب (.flip-card) + شارات إنجاز عند إنهاء الفصول.
  3) تحديات أسئلة تفاعلية (.quiz-option) مع عداد نقاط فوري ومساحة كتابة أفكار إبداعية.
```

---

### ٩. الكتب التعليمية والأكاديمية (`Educational & Academic Books Prompt`)
- **الطابع البصري:** منهجي واضح عالي التباين، أزرق أكاديمي مع أخضر معرفي لتسهيل الفهم والاستذكار.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب تعليمي تفاعلي بصيغة HTML واحد لمنصة "مكتبتي" يدعم الفهم العميق والمراجعة الذكية:
- لوحة الألوان في :root:
  --bg-primary: #F8FAFC; --bg-card: #FFFFFF; --text-main: #0F172A; --text-muted: #475569; --accent-rose: #0284C7; --accent-gold: #059669; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Noto Naskh Arabic", "Tajawal", sans-serif؛ مع جداول وصناديق مفاهيم مرنة (max-width: 100%; overflow-wrap: anywhere;) تتكيف مع محرك Fluid Reflow Zoom.
- التفاعلات:
  1) تسجيل اسم الطالب/المتعلم في بداية الكتاب ومتابعة نسبة إتقانه.
  2) بطاقات استذكار سريعة (Flashcards - .flip-card) للمصطلحات والقواعد الأساسية (السؤال على الوجه الأول والجواب على الوجه الخلفي).
  3) ملخص نقاط ذكي + اختبار تقييمي تفاعلي (.quiz-option) في نهاية كل وحدة دراسية مع تصحيح فوري، ودفتر ملاحظات دراسية محفوظ في localStorage.
```

---

### ١٠. كتب بناء الذات وتطوير الشخصية (`Self-Development & Growth Books Prompt`)
- **الطابع البصري:** ملهم، صافٍ، يجمع بين الأخضر الزيتوني الهادئ والذهبي الدافئ والورق العاجي.
- **الأمر الجاهز للنسخ:**

```text
صمّم الملف النصي المرفق ككتاب تفاعلي لبناء الذات وتطوير الشخصية بصيغة HTML واحد لمنصة "مكتبتي":
- لوحة الألوان في :root:
  --bg-primary: #F7F8F4; --bg-card: #FFFFFF; --text-main: #1E2621; --text-muted: #526358; --accent-rose: #0F766E; --accent-gold: #D97706; --reader-font: 1; --reader-font-size: 100%;
- الخطوط: "Amiri", "Tajawal", serif؛ بتصميم متدفق متجاوب (Fluid Reflow) يضمن إعادة ترتيب جميع البطاقات والتمارين بجمال عند أي تكبير.
- التفاعلات:
  1) ميثاق البداية الشخصي باسم القارئ/القارئة يُحفظ في localStorage.
  2) بطاقات "تحويل القناعات" القابلة للقلب (.flip-card: العادة القديمة على الوجه الأول والعادة الإيجابية البديلة على الوجه الخلفي).
  3) تمارين كتابية للتطبيق العملي ومتتبع عادات (محفوظة في localStorage) + مقياس وعي تفاعلي (.quiz-option) في ختام كل فصل.
```

---

## ٥. القالب المرجعي المتوافق مع `Fluid Reflow Zoom` و `4-Corner Page-Curl`

```html
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>عنوان الكتاب التفاعلي الفاخر</title>
  <style>
    :root {
      --bg-primary: #FBF7F0;
      --bg-card: #FFFDF9;
      --text-main: #231815;
      --text-muted: #6E5A53;
      --accent-rose: #9E3B5A;
      --accent-gold: #C49148;
      --border-ornate: rgba(196, 145, 72, 0.35);
      --reader-font: 1;
      --reader-font-size: 100%;
      --font-scale: 1;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      max-width: 100%;
      overflow-wrap: anywhere;
      -webkit-tap-highlight-color: transparent;
    }

    html, body {
      width: 100%;
      height: 100%;
      min-height: 100vh;
      overflow-x: hidden;
      background: var(--bg-primary);
      color: var(--text-main);
      font-family: "Amiri", "Noto Naskh Arabic", serif;
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
      padding: clamp(6px, 2vw, 12px);
      gap: 6px;
    }

    .page-canvas {
      flex: 1;
      min-height: 0;
      background: var(--bg-card);
      border: 1.5px solid var(--border-ornate);
      border-radius: 18px;
      padding: clamp(12px, 3.5vw, 20px);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      overflow-x: hidden;
      overflow-y: auto;
    }

    .page-body {
      flex: 1;
      font-size: calc(clamp(1.05rem, 2.8vw, 1.2rem) * var(--reader-font, 1));
      line-height: 1.88;
      text-align: justify;
    }

    .flip-card {
      width: 100%;
      padding: 0.9em 1.1em;
      border-radius: 12px;
      border: 1px solid var(--accent-gold);
      background: rgba(196, 145, 72, 0.08);
      cursor: pointer;
      margin: 0.6em 0;
    }

    .quiz-option {
      display: block;
      width: 100%;
      text-align: right;
      padding: 0.7em 1em;
      margin: 0.4em 0;
      border-radius: 10px;
      border: 1px solid var(--border-ornate);
      background: var(--bg-primary);
      color: var(--text-main);
      font-size: 0.95em;
      cursor: pointer;
    }
  </style>
</head>
<body>
  <!-- محتوى الكتاب التفاعلي -->
</body>
</html>
```
