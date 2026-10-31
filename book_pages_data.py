# -*- coding: utf-8 -*-
"""
book_pages_data.py
Contains all 75 pages of «أنا فتاة ولي رسالة» with high editorial refinement.
"""

PAGES = [
    # 1. الغلاف (Page 1)
    {
        "tag": "الغلاف",
        "title": "أنا فتاة ولي رسالة",
        "type": "cover",
        "html": """
<div class="cover-page">
  <div class="eyebrow">✦ إِصْدَارَاتُ مَكْتَبَتِي الفَاخِرَة • كُتَيِّبٌ تَرْبَوِيٌّ إِيمَانِيٌّ ✦</div>
  __COVER_SVG__
  <h1>أَنَا فَتَاةٌ وَلِي رِسَالَة</h1>
  <div class="subtitle">رِحْلَةٌ إِلَى أَعْمَاقِ الذَّات... مِنْ مَعْرِفَةِ النَّفْسِ إِلَى بِنَاءِ الشَّخْصِيَّةِ وَصِنَاعَةِ الأَثَر</div>
  <div class="ornament">❖ ❖ ❖</div>
  <div class="author">إِعْدَادٌ وَصِيَاغَةٌ تَرْبَوِيَّةٌ وِجْدَانِيَّة • الإِصْدَارُ الأَوَّل</div>
  <button class="primary-btn" onclick="window.goToPage(1)">ابدئي رحلتكِ المباركة 🌸</button>
</div>
"""
    },

    # 2. الإهداء (Page 2)
    {
        "tag": "الإهداء",
        "title": "إلى الفتاة التي تبحث عن ذاتها",
        "type": "reading",
        "html": """
<div class="eyebrow">إِهْدَاءٌ مِنَ القَلْب</div>
<h1>إِلَى كُلِّ فَتَاةٍ تَحْمِلُ فِي قَلْبِهَا حُلْماً...</h1>
<div class="ornament">❀ ❀ ❀</div>

<p>إلى التي تبحث عن ذاتها وسط ضجيج الحياة، وتحاول أن تفهم نفسها، وتكتشف قدراتها، وتبني شخصيتها، وتجد الطريق الذي يليق برسالتها النبيلة.</p>

<p>إلى الفتاة التي ربما سمعت كثيراً عما ينبغي أن تكون عليه، لكنها تحتاج إلى من يأخذ بيدها بحب ورفق، لتكتشف بنفسها ما تستطيع أن تكونه من خير وإحسان.</p>

<p>إلى الفتاة التي تتعثر أحياناً، وتضعف أحياناً، وتشعر أن الطريق طويل وشاق، لكنها لا تزال تحاول بصدق وعزيمة.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">همسة الإهداء</span>
  هذا الكتاب لكِ... لتعرفي أن قيمتكِ ليست رهينة إعجاب الناس، وأن مستقبلكِ لا تصنعه الأمنيات وحدها، بل يصنعه قربكِ من الله، ووعيكِ برسالتكِ، واستثماركِ لنعم الله فيكِ.
</div>

<div class="interactive-box" style="margin-top: 24px;">
  <h3 style="margin-bottom: 6px;">🌸 ركن القارئة المباركة</h3>
  <p style="font-size: .88em; margin-bottom: 10px;">اكتبي اسمكِ الكريم لنخاطبكِ به في صفحات الكتاب:</p>
  <div style="display: flex; gap: 8px;">
    <input id="readerNameInput" class="field-input" type="text" placeholder="اكتبي اسمكِ هنا (مثال: مريم، سارة، فاطمة)..." oninput="saveReaderName(this.value)"/>
    <button class="primary-btn" style="padding: 6px 16px; font-size: .75em;" onclick="alertSavedName()">حفظ</button>
  </div>
</div>
"""
    },

    # 3. الفهرس (Page 3)
    {
        "tag": "الفهرس",
        "title": "فهرس محطات الرحلة",
        "type": "reading",
        "html": """
<div class="eyebrow">دَلِيلُ الرِّحْلَة</div>
<h1>فِهْرِسُ مَحَطَّاتِ الكِتَاب</h1>
<p style="text-align: center; color: var(--text-secondary); font-size: .9em;">عشرة أبواب تأخذكِ خطوة بخطوة إلى صناعة الأثر وبناء الذات</p>

<div class="toc-list">
  <div class="toc-item" onclick="window.goToPage(3)"><span>المقدمة: قبل أن تبدئي الرحلة</span><small>صفحة ٤</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(5)"><span>الباب الأول: أنا لستُ فتاة عادية</span><small>صفحة ٦</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(11)"><span>الباب الثاني: أنا أعرف لماذا خُلقت</span><small>صفحة ١٢</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(16)"><span>الباب الثالث: علاقتي بالله سر قوتي</span><small>صفحة ١٧</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(22)"><span>الباب الرابع: أحب نفسي وأبنيها</span><small>صفحة ٢٣</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(28)"><span>الباب الخامس: عقلي ثروتي</span><small>صفحة ٢٩</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(34)"><span>الباب السادس: شخصيتي هويتي</span><small>صفحة ٣٥</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(40)"><span>الباب السابع: قلبي أمانة</span><small>صفحة ٤١</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(46)"><span>الباب الثامن: أنوثتي وكرامتي وهويتي</span><small>صفحة ٤٧</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(52)"><span>الباب التاسع: أنا صانعة أثر</span><small>صفحة ٥٣</small></div>
  <div class="toc-item toc-part" onclick="window.goToPage(58)"><span>الباب العاشر: خطتي لحياة تستحق</span><small>صفحة ٥٩</small></div>
  <div class="toc-item" onclick="window.goToPage(64)"><span>الخاتمة وميثاق الفتاة صاحبة الرسالة</span><small>صفحة ٦٥</small></div>
  <div class="toc-item" onclick="window.goToPage(67)"><span>الملاحق العملية وتحدي الثلاثين يوماً</span><small>صفحة ٦٨</small></div>
</div>
"""
    },

    # 4. المقدمة (1) (Page 4)
    {
        "tag": "المقدمة",
        "title": "قبل أن تبدئي الرحلة (١)",
        "type": "reading",
        "html": """
<div class="eyebrow">المُقَدِّمَة</div>
<h1>قَبْلَ أَنْ تَبْدَئِي الرِّحْلَة</h1>
<div class="ornament">❖ ❖ ❖</div>

<p>يا ابنتي المباركة <span class="reader-name-slot"></span>...</p>

<p>هل جلستِ يوماً مع نفسكِ، بعيداً عن الهاتف، وبعيداً عن أحاديث الناس وضجيج منصات التواصل، وسألتِها بصدق وهدوء:</p>

<p><strong>من أنا حقاً؟ ما الذي أريده من حياتي؟ ما الذي أستطيع تقديمه للعالم؟ هل أسير في الطريق الذي أتمناه؟ وهل أعيش بما يرضي الله تعالى عني؟</strong></p>

<p>ربما مرت عليكِ أيام كثيرة دون أن تتوقفي لتطرحي هذه الأسئلة العميقة؛ أيام تتشابه، وساعات تمضي، وأحلام تتراكم في الخيال، ومهام تؤجل إلى الغد، وأوقات تستهلكها أشياء عابرة لا تضيف إلى عقلكِ ولا إلى روحكِ شيئاً حقيقياً.</p>

<p>وربما وجدتِ نفسكِ أحياناً تقارنين حياتكِ بحياة غيركِ، وتنظرين إلى إنجازاتهم الظاهرة ومظاهرهم، ثم تنسين أن لكل إنسان رحلة متفردة خطها الله له بحكمة بالغة.</p>
"""
    },

    # 5. المقدمة (2) (Page 5)
    {
        "tag": "المقدمة",
        "title": "قبل أن تبدئي الرحلة (٢)",
        "type": "reading",
        "html": """
<div class="eyebrow">المُقَدِّمَة</div>
<h2>طَرِيقُ الوَعْيِ وَالإِصْلاَحِ المُسْتَمِرّ</h2>

<p>اليوم نريد أن نبدأ بطريقة مختلفة؛ نريد أن نقترب من أنفسنا، لا لننغلق عليها في أنانية، بل لنعرف كيف نستثمرها فيما ينفع ديننا ومجتمعنا وأمتنا.</p>

<p>نريد أن نبني فتاة تعرف ربها، وتفهم دينها، وتقدر ذاتها، وتحترم عقلها، وتضبط مشاعرها، وتحسن علاقاتها، وتخطط لمستقبلها بوعي وثقة.</p>

<div class="quran-box">
  <p>﴿إِنَّ اللَّهَ لَا يُغَيِّرُ مَا بِقَوْمٍ حَتَّىٰ يُغَيِّرُوا مَا بِأَنْفُسِهِمْ﴾</p>
  <div class="ayah-source">[الرعد: ١١]</div>
</div>

<p>هذه الرحلة ليست دعوة إلى المثالية؛ فلا أحد يعيش بلا أخطاء، ولا أحد يبلغ الكمال. إنها دعوة إلى الوعي، والسعي، والمراجعة، والتعلم، والإصلاح المستمر.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">قاعدة الانطلاق</span>
  تذكري دائماً: أنتِ لا تحتاجين إلى أن تصبحي إنسانة أخرى، بل تحتاجين إلى أن تكتشفي أفضل ما فيكِ، وأن تعملي على تنميته، وأن تصلحي ما يحتاج إلى إصلاح. هيا بنا نبدأ...
</div>
"""
    },

    # 6. الباب الأول: فاتحة (Page 6)
    {
        "tag": "الباب الأول",
        "title": "أنا لست فتاة عادية",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الأَوَّل</div>
  <h1>أَنَا لَسْتُ فَتَاةً عَادِيَّة</h1>
  <div class="subtitle">من معرفة النفس واكتشاف الكنوز الداخلية إلى التصالح الصادق مع الذات</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «أنتِ لستِ رقماً عابراً في هذا الوجود؛ بل إنسانة كرمها خالقها وحملها أمانة الإعمار والنفع.»
  </div>
</div>
"""
    },

    # 7. الباب الأول: الفصل 1 (Page 7)
    {
        "tag": "الباب الأول",
        "title": "الفصل الأول: من أنا؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الأَوَّل • الفَصْلُ الأَوَّل</div>
<h1>مَنْ أَنَا؟</h1>

<p>قبل أن تعرفي ماذا تريدين أن تصبحي، عليكِ أن تتعرفي إلى الفتاة التي أنتِ عليها الآن. من أنتِ؟ ما الأشياء التي تحبينها بصدق؟ ما المبادئ التي تؤمنين بها؟ ما الأمور التي تتقنينها؟ وما الجوانب التي تحتاج منكِ إلى رعاية وتطوير؟</p>

<p>إن معرفة الذات ليست أن تجلسي لتعددي مميزاتكِ فقط، بل أن تنظري إلى نفسكِ بصدق وتجرد: تعرفين مواطن قوتكِ فتشكرين الله عليها، وتعترفين بجوانب ضعفكِ فتسعين في إصلاحها، وتفهمين مشاعركِ واحتياجاتكِ.</p>

<div class="quran-box">
  <p>﴿وَلَقَدْ كَرَّمْنَا بَنِي آدَمَ﴾</p>
  <div class="ayah-source">[الإسراء: ٧٠]</div>
</div>

<p>هذا التكريم الرباني أصل عظيم تتذكرينه دائماً: كرامتكِ الإنسانية ثابتة، لا تمنحها لكِ تعليقات الناس وإعجابهم، ولا يسلبونها منكِ بانتقادهم.</p>

<p>أنتِ إنسانة لكِ عقل وروح ومشاعر وقدرات، ولستِ مجرد مظهر، أو درجات دراسية، أو رأي عابر يقوله الآخرون عنكِ. لا تنتظري أن يخبركِ الآخرون من أنتِ؛ ابدئي أنتِ باكتشاف نفسكِ.</p>
"""
    },

    # 8. الباب الأول: تطبيق 1 (Page 8)
    {
        "tag": "الباب الأول",
        "title": "بطاقة التعرف إلى الذات",
        "type": "exercise",
        "html": """
<div class="eyebrow">تَطْبِيقٌ عَمَلِيّ</div>
<h2>بِطَاقَةُ التَّعَرُّفِ إِلَى الذَّات</h2>
<p style="font-size: .88em; color: var(--text-secondary);">املئي هذه البطاقة بصدق، وتأملي إجاباتكِ بهدوء (تُحفظ تلقائياً في جهازكِ):</p>

<div class="interactive-box">
  <div class="field-group">
    <label>ثلاث صفات جميلة أعتز بها في شخصيتي:</label>
    <input class="field-input" type="text" placeholder="١. الصدق، الرحمة، حُسن الإنصات..." id="self_trait_1" oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>أشياء وأنشطة أجد فيها راحتي وشغفي:</label>
    <input class="field-input" type="text" placeholder="القراءة، الكتابة، الترتيب، مساعدة الآخرين..." id="self_hobbies" oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>مهارات أتقنها حالياً:</label>
    <input class="field-input" type="text" placeholder="التصميم، الإلقاء، تنظيم الوقت..." id="self_skills" oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>مهارات جديدة أرغب في تعلمها:</label>
    <input class="field-input" type="text" placeholder="لغة أجنبية، البرمجة، إدارة الأمور المالية..." id="self_learn" oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>أكثر جانب شخصي يحتاج إلى تطوير وإصلاح:</label>
    <input class="field-input" type="text" placeholder="علاج التسويف، ضبط الانفعال، إدارة وقت الشاشة..." id="self_improve" oninput="saveField(this.id, this.value)"/>
  </div>
</div>
"""
    },

    # 9. الباب الأول: الفصل 2 (Page 9)
    {
        "tag": "الباب الأول",
        "title": "الفصل الثاني: قيمتي لا يحددها الآخرون",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الأَوَّل • الفَصْلُ الثَّانِي</div>
<h1>قِيمَتِي لاَ يُحَدِّدُهَا الآخَرُون</h1>

<p>كم من فتاة أفسدت فرحتها بكلمة عابرة! وكم من فتاة تراجعت عن حلم نافع لأنها سمعت من يقلل من قدرتها أو يسخر من همتها!</p>

<p>إن جعل قيمتكِ مرتبطة بمدح الناس وإعجابهم يجعلكِ أسيرة دائمة لتقلب أمزجتهم وآرائهم؛ إن أثنوا عليكِ شعرتِ بأنكِ عظيمة، وإن انتقدوكِ شعرتِ بأنكِ بلا قيمة. وهذا ميزان مرهق وظالم لنفسكِ.</p>

<p>استمعي إلى النصيحة الصادقة النافعة، وتقبلي النقد البنّاء الذي يبصركِ بعيوبكِ، لكن لا تجعلي كل تعليق حكماً نهائياً على مستقبلكِ أو كرامتكِ.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">ميزان الوعي</span>
  هناك فرق عظيم بين أن تخطئي وبين أن تكوني فاشلة؛ وهناك فرق بين أن تحتاجي إلى تطوير مهاراتكِ وبين أن تكرهي نفسكِ. التواضع أن تعرفي قدر نفسكِ دون تعالٍ، والثقة أن تعترفي بما وهبكِ الله دون غرور.
</div>

<div class="interactive-box">
  <label style="font-weight: 700; color: var(--accent-rose-dark); font-size: .85em;">وقفة تأمل شخصية: هل جعلتِ إعجاب الآخرين هدفاً لكِ يوماً؟ اكتبي ما تريدين تحرير قلبكِ منه:</label>
  <textarea class="field-textarea" id="self_liberation" placeholder="اكتبي خواطركِ هنا بصراحة..." oninput="saveField(this.id, this.value)"></textarea>
</div>
"""
    },

    # 10. الباب الأول: الفصل 3 (Page 10)
    {
        "tag": "الباب الأول",
        "title": "الفصل الثالث: اكتشفي كنوزك الداخلية",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الأَوَّل • الفَصْلُ الثَّالِث</div>
<h1>اكْتَشِفِي كُنُوزَكِ الدَّاخِلِيَّة</h1>

<p>كل إنسانة تحمل في داخلها حزمة من الاستعدادات والقدرات التي أودعها الله فيها؛ لكن كثيراً من هذه المواهب تظل حبيسة لأن صاحبتها لم تخض التجربة، أو خافت من الفشل، أو انشغلت بمراقبة نجاحات غيرها.</p>

<p>اسألي نفسكِ بوعي: ما الأمور التي أتعلمها بسرعة؟ ما الأشياء التي يستشيرني الآخرون فيها؟ ما الأعمال التي أشعر بالبركة والبهجة حين أنجزها؟</p>

<div class="interactive-box">
  <h3 style="margin-bottom: 8px;">🗺️ خريطة مواهبي واستثمارها</h3>
  <div class="field-group">
    <label>الموهبة الأولى وكيف أنميها:</label>
    <input class="field-input" id="talent_1" placeholder="مثال: الكتابة والتعبير -> قراءة كتاب أسبوعياً والتدريب..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>الموهبة الثانية وكيف أنميها:</label>
    <input class="field-input" id="talent_2" placeholder="مثال: التصميم والتنظيم -> دراسة دورة تطبيقية وتطبيقها..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>الموهبة الثالثة وكيف أنميها:</label>
    <input class="field-input" id="talent_3" placeholder="مثال: تعليم الأطفال -> إعداد دروس مبسطة وتطبيقها..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>

<p style="font-size: .9em; color: var(--text-secondary);">تذكري دائماً: الموهبة بذرة صغيرة؛ لا تثمر ثمارها اليانعة إلا بالسقاية، والتدريب المستمر، والصبر، والانضباط.</p>
"""
    },

    # 11. الباب الأول: الفصل 4 (Page 11)
    {
        "tag": "الباب الأول",
        "title": "الفصل الرابع: تصالحي مع ذاتك",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الأَوَّل • الفَصْلُ الرَّابِع</div>
<h1>تَصَالَحِي مَعَ ذَاتِك</h1>

<p>قد تحملين في ذاكرتكِ مواقف لم تكوني راضية عنها، أو قرارات تمنيتِ لو أنكِ اتخذتِ غيرها، أو ذنوباً تشعرين بالندم على اقترافها. لكن تذكري دائماً: الماضي ليس سجناً مؤبداً!</p>

<p>إن أخطأتِ فاعترفي بضعفكِ البشري، واستغفري ربكِ، وأصلحي ما تستطيعين إصلاحه، وتعلمي من الدرس حتى لا يتكرر. إياكِ أن تحولي الندم إلى سياط تجلدين بها روحكِ ليل نهار فيقعدكِ ذلك عن العمل.</p>

<div class="quran-box">
  <p>﴿قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا﴾</p>
  <div class="ayah-source">[الزمر: ٥٣]</div>
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">رسالة التصالح</span>
  سامحي نفسكِ على ما تعلمتِ منه وتجاوزتِه، ولا تسامحيها على التمادي في الخطأ دون محاولة جادة للإصلاح. التوبة ميلاد متجدد لكل قلب صادق.
</div>
"""
    },

    # 12. الباب الثاني: فاتحة (Page 12)
    {
        "tag": "الباب الثاني",
        "title": "أنا أعرف لماذا خُلقت",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الثَّانِي</div>
  <h1>أَنَا أَعْرِفُ لِمَاذَا خُلِقْت</h1>
  <div class="subtitle">من عبثية التيه إلى وضوح البوصلة واكتشاف الرسالة والنجاح الحقيقي</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «أعظم لحظة في حياة الإنسان حين يدرك لماذا جاء إلى هذا الوجود، وما الأثر الذي سيرحل ويتركه خلفه ناصعاً.»
  </div>
</div>
"""
    },

    # 13. الباب الثاني: الفصل 1 (Page 13)
    {
        "tag": "الباب الثاني",
        "title": "الفصل الأول: وجودي ليس عبثاً",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّانِي • الفَصْلُ الأَوَّل</div>
<h1>وُجُودِي لَيْسَ عَبَثاً</h1>

<p>تخيلي إنساناً يعيش سنوات طويلة يدرس، ويكد، ويجمع الأموال، ويلاحق الأمنيات، ثم لا يتوقف لحظة واحدة ليسأل نفسه: إلى أين أمضي؟ ولماذا أفعل كل هذا؟</p>

<p>إن الحياة أعظم من أن تكون مجرد سلسلة رتيبة من المهام، وأشرف من أن تختزل في البحث عن المتعة الفانية أو كسب ثناء الناس. لقد خلقنا الله تعالى لعبادته وحده، وجعل لنا في هذه الأرض رسالة إعمار وإصلاح وإحسان.</p>

<div class="quran-box">
  <p>﴿وَمَا خَلَقْتُ الْجِنَّ وَالْإِنْسَ إِلَّا لِيَعْبُدُونِ﴾</p>
  <div class="ayah-source">[الذاريات: ٥٦]</div>
</div>

<p>والعبادة في ديننا مفهوم واسع رحيب لا ينحصر في المحراب فقط؛ بل كل عمل صالح تنوين به وجه الله، وتخدمين به خلقه، هو عبادة تُثابين عليها: دراستكِ، بركِ بوالديكِ، إتقانكِ لمهنتكِ، وحتى ابتسامتكِ في وجه أختكِ.</p>
"""
    },

    # 14. الباب الثاني: الفصل 2 (Page 14)
    {
        "tag": "الباب الثاني",
        "title": "الفصل الثاني: ما رسالتي في الحياة؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّانِي • الفَصْلُ الثَّانِي</div>
<h1>مَا رِسَالَتِي فِي الحَيَاة؟</h1>

<p>الرسالة هي المعنى العميق الذي توجهين سفينة حياتكِ نحوه، والقيم التي تدافعين عنها، والخير الذي تصنعينه فيمن حولكِ. ليست الرسالة بالضرورة منصباً سياسياً أو شهرة واسعة بين الناس.</p>

<p>قد تكون رسالتكِ أن تكوني معلمة مخلصة تبني أجيالاً واعية، أو طبيبة رحيمة تداوي جراح المتألمين، أو أماً صالحة تخرج قادة للأمة، أو باحثة تطور نفعاً للناس. المهم أن يكون سعيكِ موصولاً برضا الله ونفع عباده.</p>

<div class="interactive-box">
  <h3 style="margin-bottom: 8px;">✍️ صياغة جملة رسالتي</h3>
  <div class="field-group">
    <label>أريد أن أكون فتاة تمتاز بـ:</label>
    <input class="field-input" id="mission_identity" placeholder="الإيمان، الصدق، العلم، والأثر النافع..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>وأريد أن أتعلم وأتقن:</label>
    <input class="field-input" id="mission_learn" placeholder="العلوم الشرعية، تخصصي الأكاديمي، مهارات التواصل..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>وأريد أن يكون أثري في الحياة سبباً في:</label>
    <input class="field-input" id="mission_impact" placeholder="تفريج كرب، نشر فضيلة، وإصلاح في أسرتي ومجتمعي..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>
"""
    },

    # 15. الباب الثاني: الفصل 3 (Page 15)
    {
        "tag": "الباب الثاني",
        "title": "الفصل الثالث: كيف أكتشف هدفي؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّانِي • الفَصْلُ الثَّالِث</div>
<h1>كَيْفَ أَكْتَشِفُ هَدَفِي؟</h1>

<p>لا تتوقعي أن تجدي هدف حياتكِ مكتوباً في ورقة تسقط عليكِ في يوم واحد! اكتشاف الهدف رحلة نمو مستمرة تجمع بين التجربة والملاحظة والاستشارة.</p>

<p>سيري وفق هذه الخطوات السبع المحكمة:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>اكتشفي اهتماماتكِ:</strong> ما القضايا التي تشغلكِ وتتمنين حلها؟</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>تعرفي على قدراتكِ:</strong> ما المهارات التي تملكينها بالفطرة أو الممارسة؟</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>اقرئي ووسعي مدارككِ:</strong> طالعي تجارب وسير الصالحات والناجحات.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>جربي ميادين جديدة:</strong> لا تحكمي على شيء قبل خوض تجربته.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>استشيري أهل الحكمة:</strong> اسألي من يجمع بين الصلاح والخبرة.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content"><strong>حددي أولويات مرحلتكِ:</strong> ما الأهم لكِ الآن في هذا العمر؟</div></div>
<div class="step-card"><div class="step-num">٧</div><div class="step-content"><strong>ابدئي بخطوات ملموسة:</strong> قسّمي الهدف الكبير إلى مهام يومية صغيرة.</div></div>
"""
    },

    # 16. الباب الثاني: الفصل 4 (Page 16)
    {
        "tag": "الباب الثاني",
        "title": "الفصل الرابع: النجاح الذي يستحق",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّانِي • الفَصْلُ الرَّابِع</div>
<h1>النَّجَاحُ الَّذِي يَسْتَحِقّ</h1>

<p>ليس النجاح الحقيقي أن تصلي إلى أرفع الدرجات الدنيوية وتخسري صلتكِ بربكِ؛ وليس النجاح أن تحققي شهرة واسعة على حساب عفتكِ وكرامتكِ وصحتكِ النفسية.</p>

<p>النجاح المتزن الصادق هو أن تسعي إلى التفوق فيما ينفعكِ وينفع الناس، مع المحافظة التامة على دينكِ وقيمكِ ومبادئكِ، وأن تجعلي كل إنجاز سبباً للتواضع والشكر، لا للتكبر والخيلاء.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">رسالة الباب الثاني</span>
  لا تعيشي بلا غاية، ولا تجعلي غايتكِ فقط أن تكوني أفضل من فلانة أو علانة؛ بل اجعلي غايتكِ أن تكوني أقرب إلى الله، وأصدق في خدمته، وأثقل ميزاناً يوم تلقينه.
</div>
"""
    },

    # 17. الباب الثالث: فاتحة (Page 17)
    {
        "tag": "الباب الثالث",
        "title": "علاقتي بالله سر قوتي",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الثَّالِث</div>
  <h1>عَلاَقَتِي بِاللَّهِ سِرُّ قُوَّتِي</h1>
  <div class="subtitle">سكينة المعرفة، حلاوة الصلاة، ربيع القرآن، والنهوض عند الفتور والضعف</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «إذا استند القلب إلى الحي القيوم، هانت أمامه كل الصعاب، وأشرقت في جنباته أنوار الطمأنينة واليقين.»
  </div>
</div>
"""
    },

    # 18. الباب الثالث: الفصل 1 (Page 18)
    {
        "tag": "الباب الثالث",
        "title": "الفصل الأول: أعرف ربي فأطمئن",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّالِث • الفَصْلُ الأَوَّل</div>
<h1>أَعْرِفُ رَبِّي فَأَطْمَئِنّ</h1>

<p>يا فتاة الإسلام المباركة... قبل أن تبحثي عن الأمان في كلام الناس، أو تبحثي عن القوة في إعجابهم، ابحثي عنها في صلتكِ العميقة بالخالق جل وعلا.</p>

<p>اعرفي ربكِ بأسمائه الحسنى وصفاته العلى: تأملي رحمته التي وسعت كل شيء، وعلمه الذي لا يعزب عنه مثقال ذرة، وحكمته البالغة فيما يقدر ويقضي، ولطفه الخفي بكِ في كل لحظة.</p>

<div class="quran-box">
  <p>﴿وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ﴾</p>
  <div class="ayah-source">[البقرة: ١٨٦]</div>
</div>

<p>حين يستقر في يقينكِ أن الله يراكِ، ويسمع نجواكِ، ويعلم صدق سعيكِ، فلن تشعري بالوحشة أبداً، ولن تكوني مضطرة إلى أن تحملي هموم الحياة وحدكِ.</p>
"""
    },

    # 19. الباب الثالث: الفصل 2 (Page 19)
    {
        "tag": "الباب الثالث",
        "title": "الفصل الثاني: الصلاة موعدي مع الله",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّالِث • الفَصْلُ الثَّانِي</div>
<h1>الصَّلاَةُ مَوْعِدِي مَعَ اللَّه</h1>

<p>إياكِ أن تجعلي الصلاة آخر ما تفكرين فيه وسط زحام يومكِ؛ فالصلاة ليست واجباً ثقيلاً يؤدى على عجل ليفرغ الإنسان منه، بل هي أعظم محطة استراحة يتزود منها القلب بالسكينة والنور.</p>

<p>حين تقفين في صلاتكِ، تذكري أنكِ بين يدي ملك الملوك، تناجينه بكلامه، وتسألينه من واسع فضله، وتسجدين له خضوعاً وحباً.</p>

<div class="hadith-box">
  <p>«أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ»</p>
  <div class="hadith-source">متفق عليه</div>
</div>

<div class="interactive-box">
  <h3 style="margin-bottom: 6px;">🕌 خطتي لتحسين صلاتي وخشوعي</h3>
  <div class="checkbox-item"><input type="checkbox" id="pray_time" onchange="saveCheck(this.id, this.checked)"/><span>المحافظة على الصلاة في أول وقتها دون تأخير.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="pray_wudu" onchange="saveCheck(this.id, this.checked)"/><span>إسباغ الوضوء واستحضار نيته المباركة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="pray_phone" onchange="saveCheck(this.id, this.checked)"/><span>إبعاد الهاتف وتصفية الذهن قبل تكبيرة الإحرام.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="pray_tadabbur" onchange="saveCheck(this.id, this.checked)"/><span>تدبر معاني الفاتحة والآيات والتسبيح في الركوع والسجود.</span></div>
</div>
"""
    },

    # 20. الباب الثالث: الفصل 3 (Page 20)
    {
        "tag": "الباب الثالث",
        "title": "الفصل الثالث: القرآن ربيع قلبي",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّالِث • الفَصْلُ الثَّالِث</div>
<h1>القُرْآنُ رَبِيعُ قَلْبِي</h1>

<p>القرآن العظيم ليس كتاباً نضعه زينة على الرفوف، ولا مجرد حروف نمر عليها دون وعي أو تدبر؛ إنه كتاب هداية ونور وشفاء لما في الصدور، نتعلم منه أسرار الحياة وموازين الفلاح.</p>

<div class="quran-box">
  <p>﴿كِتَابٌ أَنْزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِيَدَّبَّرُوا آيَاتِهِ وَلِيَتَذَكَّرَ أُولُو الْأَلْبَابِ﴾</p>
  <div class="ayah-source">[ص: ٢٩]</div>
</div>

<p>اجعلي لنفسكِ ورداً قرآنياً يومياً ثابتاً لا تفرطين فيه مهما تكاثرت عليكِ الشواغل، ولو كان صفحة واحدة بتأنٍ وتفسير ميسر.</p>

<div class="interactive-box">
  <h3 style="margin-bottom: 8px;">📖 تحدي التدبر القرآني الشخصي</h3>
  <div class="field-group">
    <label>وردي اليومي الثابت المفضل لدي:</label>
    <input class="field-input" id="quran_wird" placeholder="مثال: حزب يومي، أو ٤ صفحات مع التفسير..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>سورة عقدتُ العزم على تدبرها هذا الشهر:</label>
    <input class="field-input" id="quran_surah" placeholder="مثال: سورة مريم، سورة النور، سورة الحجرات..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>
"""
    },

    # 21. الباب الثالث: الفصل 4 (Page 21)
    {
        "tag": "الباب الثالث",
        "title": "الفصل الرابع: ماذا أفعل عندما أضعف؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّالِث • الفَصْلُ الرَّابِع</div>
<h1>مَاذَا أَفْعَلُ عِنْدَمَا أَضْعُف؟</h1>

<p>قد تمر عليكِ أيام تشعرين فيها بالفتور، وتثقل عليكِ بعض الطاعات، وتضعف همتكِ؛ وهذا أمر يعتري كل إنسان. لكن الخطر يكمن في أن تجعلي الفتور المؤقت سبباً للانقطاع التام!</p>

<p>إذا ضعفتِ فلا تيأسي: عودي فوراً إلى مصلاكِ، واستغفري ربكِ، وافتحي مصحفكِ، وأكثري من الاستغفار، واختاري عملاً يسيراً تداومين عليه حتى تعود شعلة القلب للإيقاد.</p>

<div class="quran-box">
  <p>﴿لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ﴾</p>
  <div class="ayah-source">[الزمر: ٥٣]</div>
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">بلسم الفتور</span>
  لا تنتظري هطول الحماس حتى تعودي إلى الله؛ فالطاعة الصادقة جهاد وصبر، ومن جاهد نفسه في الله هداه سبحانه سبل السكينة والثبات.
</div>
"""
    },

    # 22. الباب الثالث: الفصل 5 (Page 22)
    {
        "tag": "الباب الثالث",
        "title": "الفصل الخامس: الخلوة الصادقة مع الله",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّالِث • الفَصْلُ الخَامِس</div>
<h1>الخَلْوَةُ الصَّادِقَةُ مَعَ اللَّه</h1>

<p>خصصي في كل أسبوع وقتاً هادئاً تجلسين فيه وحدكِ مع الله: تراجعين فيه حساباتكِ، وتستغفرين من زلاتكِ، وتشكرينه على آلائه ونعمه التي لا تعد ولا تحصى.</p>

<p>اطرحي على نفسكِ هذه الأسئلة الستة بصدق وتجرد:</p>

<div class="interactive-box">
  <div class="checkbox-item"><input type="checkbox" id="review_pray" onchange="saveCheck(this.id, this.checked)"/><span>هل صلاتي تؤدى بخشوع وحضور قلب؟</span></div>
  <div class="checkbox-item"><input type="checkbox" id="review_tongue" onchange="saveCheck(this.id, this.checked)"/><span>هل حفظتُ لساني من الغيبة والنميمة والقيل والقال؟</span></div>
  <div class="checkbox-item"><input type="checkbox" id="review_justice" onchange="saveCheck(this.id, this.checked)"/><span>هل سلم الناس من ظني وكلامي ولم أظلم أحداً؟</span></div>
  <div class="checkbox-item"><input type="checkbox" id="review_parents" onchange="saveCheck(this.id, this.checked)"/><span>هل أديتُ حق والديّ بالبر والرحمة والإحسان؟</span></div>
  <div class="checkbox-item"><input type="checkbox" id="review_thanks" onchange="saveCheck(this.id, this.checked)"/><span>ما النعمة العظمى التي تحتاج مني إلى شكر متجدد؟</span></div>
  <div class="checkbox-item"><input type="checkbox" id="review_action" onchange="saveCheck(this.id, this.checked)"/><span>ما العمل الإيماني الذي أحتاج إلى تحسينه هذا الأسبوع؟</span></div>
</div>
"""
    },

    # 23. الباب الرابع: فاتحة (Page 23)
    {
        "tag": "الباب الرابع",
        "title": "أحب نفسي وأبنيها",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الرَّابِع</div>
  <h1>أُحِبُّ نَفْسِي وَأَبْنِيهَا</h1>
  <div class="subtitle">التقدير الصحي للذات، التحرر من فخ المقارنة، وفن التعامل مع العثرات</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «حب الذات الصادق ليس أنانية ولا تعالياً، بل رعاية أمانة أودعها الله بين جنبيكِ لتصلحيها وتجعليها منارة خير.»
  </div>
</div>
"""
    },

    # 24. الباب الرابع: الفصل 1 (Page 24)
    {
        "tag": "الباب الرابع",
        "title": "الفصل الأول: التقدير الصحي للذات",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الرَّابِع • الفَصْلُ الأَوَّل</div>
<h1>التَّقْدِيرُ الصِّحِّيُّ لِلذَّات</h1>

<p>أن تحترمي نفسكِ لا يعني أن تري نفسكِ فوق الناس؛ وأن تعترفي بقدراتكِ لا يعني أن تنكري فضل الله ومنته عليكِ في كل خطوة.</p>

<p>التقدير الصحي للذات هو أن تعاملي نفسكِ بإنصاف: تدركين كرامتكِ وحقوقكِ، وتعرفين حدودكِ، وتتقبلين ضعفكِ البشري، وتسعين في التطور الدائم دون جلد أو احتقار لنفسكِ.</p>

<p>لا تربطي قيمتكِ أبداً بشكل جسدكِ، أو ماركة ثيابكِ، أو عدد متابعيكِ، أو تصفيق العابرين؛ فكل هذه قشور زائلة تتغير في لحظة، أما كرامتكِ وجوهركِ فيقومان على إيمانكِ، وعقلكِ، ونقائكِ، وأخلاقكِ.</p>
"""
    },

    # 25. الباب الرابع: الفصل 2 (Page 25)
    {
        "tag": "الباب الرابع",
        "title": "الفصل الثاني: لا تقارني رحلتك برحلة غيرك",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الرَّابِع • الفَصْلُ الثَّانِي</div>
<h1>لاَ تُقَارِنِي رِحْلَتَكِ بِرِحْلَةِ غَيْرِك</h1>

<p>قد ترين في محيطكِ فتاة تسبقكِ في دراسة، أو تملك موهبة تلفت الأنظار، أو حققت إنجازاً تمنيتِ لو كان لكِ؛ فافرحي لها بصدق ونقاء، واستلهمي من نجاحها، وإياكِ والحسد أو احتقار ذاتكِ.</p>

<p>أنتِ لا ترين إلا الظاهر المعروض، ولا تعلمين ما تحمله النفوس من أوجاع وابتلاءات. المقارنة العادلة الوحيدة هي أن تنظري إلى نفسكِ اليوم وتقارنيها بنفسكِ بالأمس:</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">ميزان النمو الشخصي</span>
  هل أصبحتِ اليوم أكثر علماً مما كنتِ؟ هل تحسن خلقكِ؟ هل صرتِ أكثر صبراً وانضباطاً؟ هل اقتربتِ خطوة من أهدافكِ؟ إن كان الجواب نعم، فأنتِ في قمة النجاح والتقدم، فلا تلتفتي!
</div>
"""
    },

    # 26. الباب الرابع: الفصل 3 (Page 26)
    {
        "tag": "الباب الرابع",
        "title": "الفصل الثالث: كيف أتعامل مع أخطائي؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الرَّابِع • الفَصْلُ الثَّالِث</div>
<h1>كَيْفَ أَتَعَامَلُ مَعَ أَخْطَائِي؟</h1>

<p>الخطأ قدر بشري لا ينفك عن أحد؛ لكن تكراره بعناد وتبرير هو الذي يحول الزلة إلى عادة مدمرة. عندما تقعين في الخطأ، اتبعي هذه القواعد الست الحكيمة:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>الاعتراف دون مراوغة:</strong> كوني شجاعة واعترفي بالخطأ لنفسكِ دون اختلاق مبررات واهية.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>الاعتذار الراقي:</strong> إذا تضرر أحد من تصرفكِ فبادري بالاعتذار الصادق.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>إصلاح الآثار:</strong> اجتهدي في جبر ما انكسر قدر وسعكِ.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>فهم السبب الجذري:</strong> لماذا حدث ذلك؟ وما الباب الذي دخل منه الخطأ؟</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>وضع حواجز حماية:</strong> صممي إجراءً يمنع تكرار الموقف في المستقبل.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content"><strong>إغلاق ملف اللوم:</strong> لا تحولي الخطأ إلى وسم دائم يلاحقكِ مدى الحياة.</div></div>
"""
    },

    # 27. الباب الرابع: الفصل 4 (Page 27)
    {
        "tag": "الباب الرابع",
        "title": "الفصل الرابع: الثقة المتزنة بالنفس",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الرَّابِع • الفَصْلُ الرَّابِع</div>
<h1>الثِّقَةُ المُتَّزِنَةُ بِالنَّفْس</h1>

<p>الثقة الحقيقية ليست ادعاء معرفة كل شيء، بل الاستعداد الدائم للتعلم والتواضع؛ وليست الثقة علو الصوت في كل مجلس، بل معرفة متى تتحدثين بحكمة ومتى تصغين بأدب.</p>

<p>كيف تبنين ثقتكِ المتزنة كل يوم؟</p>

<div class="interactive-box">
  <div class="checkbox-item"><input type="checkbox" id="conf_learn" onchange="saveCheck(this.id, this.checked)"/><span>أتعلم باستمرار وأوسع دائرة معارفي.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="conf_promise" onchange="saveCheck(this.id, this.checked)"/><span>أفي بوعودي لنفسي وللآخرين مهما صغرت.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="conf_resp" onchange="saveCheck(this.id, this.checked)"/><span>أتحمل كامل المسؤولية عن قراراتي وسلوكي.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="conf_fear" onchange="saveCheck(this.id, this.checked)"/><span>أواجه مخاوفي بالتدرج والثقة في معية الله.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="conf_stop" onchange="saveCheck(this.id, this.checked)"/><span>أتوقف عن التماس رضا الجميع ومطاردة الاستحسان.</span></div>
</div>
"""
    },

    # 28. الباب الرابع: الفصل 5 (Page 28)
    {
        "tag": "الباب الرابع",
        "title": "الفصل الخامس: أنا أستطيع أن أتغير",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الرَّابِع • الفَصْلُ الخَامِس</div>
<h1>أَنَا أَسْتَطِيعُ أَنْ أَتَغَيَّر</h1>

<p>إياكِ أن تقولي: «هذه طبيعتي ولن أتغير!»؛ فالعادات تكتسب بالممارسة، والمشاعر تهذب بالوعي، والطباع ترتقي بالمجاهدة والصبر.</p>

<div class="step-card"><div class="step-num">✦</div><div class="step-content"><strong>إذا كنتِ كثيرة التأجيل:</strong> ابدئي فوراً بمهمة صغيرة لمدة خمس دقائق فقط دون انقطاع.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content"><strong>إذا كنتِ تخافين الحديث:</strong> تدربي على التعبير عن رأيكِ في محيطكِ الأسري الآمن.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content"><strong>إذا كنتِ سريعة الغضب:</strong> تدربي على الصمت والتنفس العميق قبل إصدار أي حكم.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content"><strong>إذا كنتِ مشتتة الذهن:</strong> قنني وقت الهاتف واجعلي بيئة عملكِ نظيفة ومنظمة.</div></div>

<div class="interactive-box">
  <label style="font-weight: 700; color: var(--accent-rose-dark); font-size: .85em;">صفة قررتُ تغييرها اليوم، والخطوة العملية الأولى للبدء:</label>
  <input class="field-input" id="habit_change_plan" placeholder="مثال: الاستيقاظ مبكراً -> وضع المنبه بعيداً عن السرير..." oninput="saveField(this.id, this.value)"/>
</div>
"""
    },

    # 29. الباب الخامس: فاتحة (Page 29)
    {
        "tag": "الباب الخامس",
        "title": "عقلي ثروتي",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الخَامِس</div>
  <h1>عَقْلِي ثَرْوَتِي</h1>
  <div class="subtitle">شغف القراءة، بناء الفكر الناقد، إدارة الوقت، ومهارات المستقبل</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «العقل زينة الإنسان وبوابته إلى النفع؛ وفتاة تقرأ وتبني وعيها هي شمس تضيء درب أسرتها وأمتها.»
  </div>
</div>
"""
    },

    # 30. الباب الخامس: الفصل 1 (Page 30)
    {
        "tag": "الباب الخامس",
        "title": "الفصل الأول: كوني فتاة تقرأ",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الخَامِس • الفَصْلُ الأَوَّل</div>
<h1>كُونِي فَتَاةً تَقْرَأ</h1>

<p>هناك فرق شاسع بين فتاة تمضي الساعات الطوال تستهلك ما يلقيه الآخرون في وسائل التواصل، وبين فتاة تقتطع من يومها وقتاً لتغذية عقلها بقراءة نافعة واعية.</p>

<p>القراءة تصقل فكركِ، وتوسع آفاقكِ، وترتقي بلسانكِ، وتمنحكِ تجارب أجيال سبقتكِ. اجعلي لكِ مكتبة شخصية مباركة تضم هذه العناوين المختارة:</p>

<div class="interactive-box">
  <div class="checkbox-item"><input type="checkbox" id="book_saadi" onchange="saveCheck(this.id, this.checked)"/><span><strong>تفسير السعدي (تيسير الكريم الرحمن):</strong> لفهم وتدبر آيات القرآن.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="book_nawawi40" onchange="saveCheck(this.id, this.checked)"/><span><strong>الأربعون النووية للإمام النووي:</strong> معالم الدين وقواعد الإيمان.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="book_riyad" onchange="saveCheck(this.id, this.checked)"/><span><strong>رياض الصالحين للإمام النووي:</strong> تهذيب الأخلاق والعبادات.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="book_raheeq" onchange="saveCheck(this.id, this.checked)"/><span><strong>الرحيق المختوم لصفي الرحمن المباركفوري:</strong> في السيرة النبوية العطرة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="book_fawaid" onchange="saveCheck(this.id, this.checked)"/><span><strong>الفوائد لابن قيم الجوزية:</strong> لطائف التدبر وتزكية الروح.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="book_ghazali" onchange="saveCheck(this.id, this.checked)"/><span><strong>جدد حياتك لمحمد الغزالي:</strong> ربط الدين بالحياة والتفكير الإيجابي.</span></div>
</div>
"""
    },

    # 31. الباب الخامس: الفصل 2 (Page 31)
    {
        "tag": "الباب الخامس",
        "title": "الفصل الثاني: العلم يصنع الفرق",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الخَامِس • الفَصْلُ الثَّانِي</div>
<h1>العِلْمُ يَصْنَعُ الفَرْق</h1>

<p>إياكِ أن تحصري مفهوم العلم في الشهادة الدراسية التي تعلق على الجدار؛ فالعلم الحقيقي هو ما غير نظرتكِ للحياة، وهذب سلوككِ، وعلمكِ كيف تعبدين الله على بصيرة وكيف تنفعين خلقه.</p>

<p>أتقني تخصصكِ، واكتسبي المهارات التي يحتاج إليها مجتمعكِ، وتعلمي كيف تبحثين وتستوثقين قبل أن تتكلمي أو تنشري. العلم الذي لا يصحبه عمل يبقى حجة على صاحبه، أما العلم النافع فيرفع قدركِ في الدنيا والآخرة.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">نور البصيرة</span>
  ليست العبرة بكثرة ما تجمعين من معلومات؛ بل بعمق ما تفهمين، وصدق ما تطبقين، وجمال ما تورثينه في نفوس الآخرين من هدى ونفع.
</div>
"""
    },

    # 32. الباب الخامس: الفصل 3 (Page 32)
    {
        "tag": "الباب الخامس",
        "title": "الفصل الثالث: كيف أبني فكري؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الخَامِس • الفَصْلُ الثَّالِث</div>
<h1>كَيْفَ أَبْنِي فِكْرِي؟</h1>

<p>لا تكوني كالإسفنجة التي تمتص كل سائل يعرض عليها دون تمييز! في زمن السيولة الفكرية، تحتاج الفتاة إلى حصانة عقلية وتفكير ناقد أصيل يحمي هويتها وقيمها.</p>

<p>كلما صادفكِ رأي أو منشور أو فكرة، اسألي نفسكِ هذه الأسئلة الخمسة:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>من قائل هذه المعلومة؟</strong> وهل هو متخصص موثوق أم مجرد متكلم عابر؟</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>ما دليله وبرهانه؟</strong> هل استند إلى أصل محكم أم مجرد هوى وظن؟</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>هل يوافق ثوابت ديني وقيمي؟</strong> الميزان الحاكم هو الوحي المعصوم.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>ما الهدف والغاية من هذا الطرح؟</strong> ومن المستفيد من انتشاره؟</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>هل الصورة كاملة؟</strong> ابحثي عن الزوايا الغائبة قبل الحكم.</div></div>
"""
    },

    # 33. الباب الخامس: الفصل 4 (Page 33)
    {
        "tag": "الباب الخامس",
        "title": "الفصل الرابع: إدارة الوقت",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الخَامِس • الفَصْلُ الرَّابِع</div>
<h1>إِدَارَةُ الوَقْتِ هِيَ إِدَارَةُ العُمْر</h1>

<p>الوقت ليس ساعات تمر على عقارب الساعة، بل هو أنفاسكِ وعمركِ ورأس مالكِ الحقيقي الذي إن ضاع فلن يعود أبداً.</p>

<div class="hadith-box">
  <p>«نِعْمَتَانِ مَغْبُونٌ فِيهِمَا كَثِيرٌ مِنَ النَّاسِ: الصِّحَّةُ وَالفَرَاغُ»</p>
  <div class="hadith-source">رواه البخاري</div>
</div>

<p>نموذج مقترح ليوم متوازن مبارك:</p>

<div class="interactive-box">
  <div style="font-size: .88em; line-height: 1.8;">
    <p>🌅 <strong>الفجر:</strong> الصلاة في وقتها، أذكار الصباح، ورد القرآن، وانطلاقة بنشاط.</p>
    <p>☀️ <strong>الصباح:</strong> الدراسة، العمل، وإنجاز المهام التي تحتاج إلى تركيز ذهني عالٍ.</p>
    <p>🌤️ <strong>الظهر:</strong> صلاة الظهر، قسط راحة خفيف، وتغذية متوازنة.</p>
    <p>🌇 <strong>العصر:</strong> تنمية المهارات، القراءة، والرياضة أو الحركة البدنية.</p>
    <p>🌙 <strong>المغرب:</strong> صلة الرحم، الجلوس مع الأسرة، وإنجاز الواجبات المنزلية.</p>
    <p>🌌 <strong>العشاء:</strong> صلاة العشاء والوتر، مراجعة إنجازات اليوم، والنوم المبكر.</p>
  </div>
</div>
"""
    },

    # 34. الباب الخامس: الفصل 5 (Page 34)
    {
        "tag": "الباب الخامس",
        "title": "الفصل الخامس: مهارات تصنع مستقبلي",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الخَامِس • الفَصْلُ الخَامِس</div>
<h1>مَهَارَاتٌ تَصْنَعُ مُسْتَقْبَلِي</h1>

<p>استثمري سنوات شبابكِ في تعلم مهارات تبقى معكِ وتبني استقلالكِ وثقتكِ ونفعكِ للمجتمع:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content">مهارة التواصل الراقي والحوار الفعال.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content">الكتابة والتعبير وصياغة الأفكار.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content">إدارة الوقت والمهام والانضباط الذاتي.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content">استخدام التقنية والذكاء الاصطناعي بوعي وأمان.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content">البحث والتحقق والتدقيق المعرفي.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content">التفكير وحل المشكلات والتخطيط العملي.</div></div>
<div class="step-card"><div class="step-num">٧</div><div class="step-content">لغة ثانية تفتح لكِ نوافذ العلم والدعوة.</div></div>
<div class="step-card"><div class="step-num">٨</div><div class="step-content">الإدارة المالية الشخصية وثقافة الادخار.</div></div>

<div class="interactive-box">
  <label style="font-weight: 700; color: var(--accent-rose-dark); font-size: .85em;">المهارة التي سأبدأ في تعلمها هذا الشهر ومصدرها:</label>
  <input class="field-input" id="skill_monthly_focus" placeholder="اسم المهارة + الدورة أو الكتاب + ساعتان أسبوعياً..." oninput="saveField(this.id, this.value)"/>
</div>
"""
    },

    # 35. الباب السادس: فاتحة (Page 35)
    {
        "tag": "الباب السادس",
        "title": "شخصيتي هويتي",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ السَّادِس</div>
  <h1>شَخْصِيَّتِي هُوِيَّتِي</h1>
  <div class="subtitle">قوة الشخصية المتزنة، بهاء الحياء، فن الحوار، والحدود الصحية والصحبة الصالحة</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «الجمال الحقيقي للشخصية أن تجمع الفتاة بين عزة النفس ولين الجانب، وقوة الحجة وعذوبة اللسان.»
  </div>
</div>
"""
    },

    # 36. الباب السادس: الفصل 1 (Page 36)
    {
        "tag": "الباب السادس",
        "title": "الفصل الأول: قوة الشخصية ليست قسوة",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّادِس • الفَصْلُ الأَوَّل</div>
<h1>قُوَّةُ الشَّخْصِيَّةِ لَيْسَتْ قَسْوَة</h1>

<p>تخطئ بعض الفتيات حين يظنن أن قوة الشخصية تقتضي رفع الصوت، أو التعنت في الرأي، أو رفض الاعتذار، أو الرد الجاف على كل كلمة. هذا تشوه للمفهوم وضعف متستر بالقسوة!</p>

<p>قوة الشخصية الحقيقية تنبع من معرفتكِ لمبادئكِ، وثباتكِ على قيمكِ، وقدرتكِ على التعبير عن رأيكِ بأدب واحترام بالغين، وتحملكِ الكامل لمسؤولية أفعالكِ.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">معادلة الشخصية الراقية</span>
  أن تكوني لطيفة لا يعني أن تكوني مهزومة أو مستباحة الحقوق؛ وأن تكوني حازمة لا يعني أن تكوني فظة أو قاسية. كوني قوية في الحق، رحيمة في التعامل، واضحة في مواقفكِ دون ضجيج.
</div>
"""
    },

    # 37. الباب السادس: الفصل 2 (Page 37)
    {
        "tag": "الباب السادس",
        "title": "الفصل الثاني: الحياء والثقة",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّادِس • الفَصْلُ الثَّانِي</div>
<h1>الحَيَاءُ وَالثِّقَة</h1>

<p>الحياء خلق الإسلام الرفيع، وهو تاج جمال الفتاة المسلمة. وهو بريء تماماً من الضعف أو الخوف أو التنازل عن الحق أو العجز عن النطق بالصواب.</p>

<div class="hadith-box">
  <p>«الحَيَاءُ لاَ يَأْتِي إِلاَّ بِخَيْرٍ»</p>
  <div class="hadith-source">متفق عليه</div>
</div>

<p>يمكنكِ أن تكوني فتاة شديدة الحياء وفي الوقت ذاته واثقة من خطاكِ، متواضعة لله لكنها ذات همة تناطح السحاب، مهذبة الألفاظ وحازمة في رفض المنكر والابتذال.</p>
"""
    },

    # 38. الباب السادس: الفصل 3 (Page 38)
    {
        "tag": "الباب السادس",
        "title": "الفصل الثالث: فن الحوار",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّادِس • الفَصْلُ الثَّالِث</div>
<h1>فَنُّ الحِوَارِ الرَّاقِي</h1>

<p>الحوار ليس معركة لتسجيل النقاط والانتصار للنفس؛ بل وسيلة للتفاهم وتوضيح الرؤى وتقريب القلوب. عندما تتحاورين مع أي إنسان، التزمي بهذه الآداب السبعة:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>الإنصات الواعي:</strong> استمعي لتفهمي ما يقال، لا لتردي فقط.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>عدم المقاطعة:</strong> دعي المتكلم يكمل فكرته باحترام.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>انتقاء الكلمات:</strong> اختاري اللفظ الألطف والأبين.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>احترام المخالف:</strong> ناقشي الفكرة وابتعدي عن نقد الشخص.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>الاعتراف بقلة العلم:</strong> قولي «لا أعلم» إن كنتِ لا تعرفين.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content"><strong>سرعة الاعتذار:</strong> إن بدرت منكِ زلة فسارعي لجبرها.</div></div>
<div class="step-card"><div class="step-num">٧</div><div class="step-content"><strong>رد الإساءة بالإحسان:</strong> ارفضي الباطل دون أن تسقطي في مستنقع الإساءة.</div></div>
"""
    },

    # 39. الباب السادس: الفصل 4 (Page 39)
    {
        "tag": "الباب السادس",
        "title": "الفصل الرابع: كيف أضع حدوداً صحية؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّادِس • الفَصْلُ الرَّابِع</div>
<h1>كَيْفَ أَضَعُ حُدُوداً صِحِّيَّة؟</h1>

<p>من حقكِ الشرعي والنفسي أن تحمي خصوصيتكِ، وأن ترفضي ما يتعارض مع قيمكِ ومبادئكِ، وأن تبتعدي عن كل علاقة تستنزف طاقتكِ أو تدفعكِ إلى معصية.</p>

<p>ليس مطلوباً منكِ أن تقولي «نعم» لكل طلب حتى تكسبين حب الناس؛ فـ«نعم» التي تقال على حساب دينكِ وراحتكِ ثمنها باهظ جداً!</p>

<div class="interactive-box">
  <h3 style="margin-bottom: 8px;">💬 عبارات راقية لوضع الحدود:</h3>
  <p>• «أعتذر، هذا الأمر لا يناسب قناعاتي ومبادئي.»</p>
  <p>• «أفضل عدم مشاركة هذه المعلومة الشخصية، مع احترامي لكِ.»</p>
  <p>• «أحتاج وقتاً للتفكير قبل إعطاء جواب نهائي.»</p>
  <p>• «أقدر رأيكِ، لكنني لستُ مرتاحة لهذا التصرف.»</p>
</div>
"""
    },

    # 40. الباب السادس: الفصل 5 (Page 40)
    {
        "tag": "الباب السادس",
        "title": "الفصل الخامس: اختاري صديقاتك بعناية",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّادِس • الفَصْلُ الخَامِس</div>
<h1>اخْتَارِي صَدِيقَاتِكِ بِعِنَايَة</h1>

<p>الصداقة ليست بكثرة المتابعين والأسماء المحفوظة في الهاتف؛ بل بوجود أرواح نقية تعينكِ على طاعة الله وتأخذ بيدكِ إلى الفلاح.</p>

<div class="hadith-box">
  <p>«المَرْءُ عَلَى دِينِ خَلِيلِهِ، فَلْيَنْظُرْ أَحَدُكُمْ مَنْ يُخَالِلُ»</p>
  <div class="hadith-source">رواه أبو داود والترمذي</div>
</div>

<p>اختاري الصديقة التي:</p>
<div class="step-card"><div class="step-num">🌸</div><div class="step-content">تذكركِ بالله وتعينكِ على الصلاة وتفرح لقربكِ من الطاعة.</div></div>
<div class="step-card"><div class="step-num">🌸</div><div class="step-content">تحفظ سركِ وتذب عن عرضكِ في غيابكِ.</div></div>
<div class="step-card"><div class="step-num">🌸</div><div class="step-content">تفرح لنجاحكِ بصدق دون غيرة، وتنصحكِ برفق إن أخطأتِ.</div></div>
<p style="font-size: .9em; color: var(--text-secondary); text-align: center;">وكوني أنتِ أيضاً تلك الصديقة الوفية الصالحة لمن يصاحبكِ.</p>
"""
    },

    # 41. الباب السابع: فاتحة (Page 41)
    {
        "tag": "الباب السابع",
        "title": "قلبي أمانة",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ السَّابِع</div>
  <h1>قَلْبِي أَمَانَة</h1>
  <div class="subtitle">فهم المشاعر، التعامل مع الحزن والغضب والخذلان، وبلوغ السلام الداخلي</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «القلب ملك الجوارح؛ إذا صلح بطمأنينة الإيمان والرضا، استقامت كل أركان الحياة.»
  </div>
</div>
"""
    },

    # 42. الباب السابع: الفصل 1 (Page 42)
    {
        "tag": "الباب السابع",
        "title": "الفصل الأول: افهمي مشاعرك",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّابِع • الفَصْلُ الأَوَّل</div>
<h1>افْهَمِي مَشَاعِرَك</h1>

<p>المشاعر موجات طبيعية تعتري النفس البشرية: فرح، حزن، خوف، غيرة، توتر، حماس؛ ووجود الشعور في صدركِ لا يعني أبداً أنكِ سيئة، لكن المحك هو كيف تفهمينه وتتعاملين معه بحكمة.</p>

<p>إياكِ أن تتخذي قراراً مصيرياً وأنتِ في فورة الغضب، وإياكِ أن تفسري كل موقف على أنه استهداف شخصي لكِ. اسألي نفسكِ دائماً:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content">ما الذي أشعر به بدقة في هذه اللحظة؟ (تسمية الشعور نصف الحل).</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content">ما الموقف أو الفكرة التي أوقدت هذا الشعور؟</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content">ما الذي أحتاجه الآن لتهدأ نفسي؟ (وضوء، مشي، حديث مع حكيمة).</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content">ما التصرف الأرقى والأكثر حكمة وموافقة لشرع الله؟</div></div>
"""
    },

    # 43. الباب السابع: الفصل 2 (Page 43)
    {
        "tag": "الباب السابع",
        "title": "الفصل الثاني: حين تحزنين",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّابِع • الفَصْلُ الثَّانِي</div>
<h1>حِينَ تَحْزَنِين</h1>

<p>يا بنيتي... ليس مطلوباً منكِ أن تتصنعي الابتسامة طوال الوقت، ولا أن تتظاهري بأن كل شيء على ما يرام وأنتِ تتألمين في داخلكِ؛ فالحزن عاطفة بشرية بكى منها خير البشر صلى الله عليه وسلم حين فاضت عيناه رحمة.</p>

<p>عندما يطرق الحزن باب قلبكِ:</p>
<div class="step-card"><div class="step-num">✦</div><div class="step-content">امنحي نفسكِ وقتاً هادئاً لتفريغ المشاعر والتحدث مع الله في سجود طويل.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content">شاركي من تثقين بحكمتها وديانتها من والدتكِ أو مربيتكِ.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content">دوني مشاعركِ في أوراق لتتضح أبعاد المشكلة أمامكِ.</div></div>
<div class="step-card"><div class="step-num">✦</div><div class="step-content">حافظي على طعامكِ ونومكِ وحركتكِ، ولا تستسلمي للعزلة القاتمة.</div></div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">تنبيه تربوي ونفسي</span>
  إذا استمر الحزن لأسابيع وأعاق دراستكِ ونومكِ ونشاطكِ، فلا تترددي في طلب مساعدة مختصة نفسية مؤهلة؛ فطلب العلاج والرعاية أخذ بالأسباب المشروعة وليس ضعفاً في الإيمان.
</div>
"""
    },

    # 44. الباب السابع: الفصل 3 (Page 44)
    {
        "tag": "الباب السابع",
        "title": "الفصل الثالث: حين تغضبين",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّابِع • الفَصْلُ الثَّالِث</div>
<h1>حِينَ تَغْضَبِين</h1>

<p>الغضب نار تلتهب في الصدر، تدفع الإنسان إلى كلمات قاسية وقرارات طائشة يندم عليها دهراً طويلاً بعد أن تنطفئ ثورته.</p>

<div class="hadith-box">
  <p>«لاَ تَغْضَبْ»</p>
  <div class="hadith-source">رواه البخاري (كررها النبي ﷺ مراراً لمن استوصاه)</div>
</div>

<p>تدربي على خطة الإطفاء النبوية الخماسية:</p>
<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>الصمت التام:</strong> عودي لسانكِ ألا ينطق بحرف واحد في أول لحظات الغضب.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>الاستعاذة بالله:</strong> قولي بقلبكِ ولسانكِ: «أعوذ بالله من الشيطان الرجيم».</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>تغيير الهيئة:</strong> إن كنتِ واقفة فاجلسي، وإن كنتِ جالسة فاضطجعي.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>الوضوء بالماء البارد:</strong> فإن الماء يطفئ لهيب الغضب.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>تأجيل النقاش:</strong> قولي بهدوء: «سنتحدث في هذا الأمر لاحقاً بعد أن نهدأ».</div></div>
"""
    },

    # 45. الباب السابع: الفصل 4 (Page 45)
    {
        "tag": "الباب السابع",
        "title": "الفصل الرابع: حين يخذلك أحد",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّابِع • الفَصْلُ الرَّابِع</div>
<h1>حِينَ يَخْذُلُكِ أَحَد</h1>

<p>أشد الآلام وطأة على القلب الرقيق أن يأتيه الجفاء أو الخذلان من صديقة وثق بها، أو قريبة انتظر منها الدعم والوفاء. إذا مررتِ بهذا الموقف فتذكري:</p>

<p><strong>أولاً:</strong> ليس كل من خالف توقعاتكِ عدواً يتربص بكِ؛ فالبشر يعتريهم القصور والضعف والنسيان وسوء التقدير.</p>

<p><strong>ثانياً:</strong> انتهاء علاقة معينة لا يعني أنكِ فشلتِ أو أنكِ غير جديرة بالحب والاحترام؛ بل قد يكون صرفاً إلهياً حكيماً لشر لم تكوني تعلمينه.</p>

<p><strong>ثالثاً:</strong> سامحي في قلبكِ لوجه الله لتتحرري من سموم الحقد، ولكن ضعي الحدود الواعية التي تحفظ كرامتكِ وتمنع تكرار الأذى.</p>
"""
    },

    # 46. الباب السابع: الفصل 5 (Page 46)
    {
        "tag": "الباب السابع",
        "title": "الفصل الخامس: السلام الداخلي",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ السَّابِع • الفَصْلُ الخَامِس</div>
<h1>السَّلاَمُ الدَّاخِلِيّ</h1>

<p>السلام الداخلي ليس انعدام المشكلات في الحياة؛ بل هو القدرة على خوض أمواجها بسكينة ورضا ويقين بأن كل ما يقدره الله هو الخير المحض.</p>

<div class="quran-box">
  <p>﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾</p>
  <div class="ayah-source">[الرعد: ٢٨]</div>
</div>

<p>أبواب السلام الثمانية لقلبكِ:</p>
<div class="step-card"><div class="step-num">١</div><div class="step-content">اليقين التام بعدل الله ورحمته في قضائه وقدره.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content">المداومة على ذكر الله والدعاء في كل آن.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content">إغلاق باب المقارنات والتطلع لما في أيدي الآخرين.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content">العفو عن الزلات وتصفية الصدور من الشحناء.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content">ترتيب شؤون الحياة وإبعاد الفوضى.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content">العناية بصحة الجسد والنوم الهادئ الكافي.</div></div>
<div class="step-card"><div class="step-num">٧</div><div class="step-content">بذل المعروف ومساعدة المحتاجين دون مقابل.</div></div>
<div class="step-card"><div class="step-num">٨</div><div class="step-content">الرضا بما قسم الله مع مواصلة السعي والإحسان.</div></div>
"""
    },

    # 47. الباب الثامن: فاتحة (Page 47)
    {
        "tag": "الباب الثامن",
        "title": "أنوثتي وكرامتي وهويتي",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ الثَّامِن</div>
  <h1>أُنُوثَتِي وَكَرَامَتِي وَهُوِيَّتِي</h1>
  <div class="subtitle">الاعتزاز بالهوية، بهاء الحياء والعفة، الجمال الشامل، والطموح الراقي</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «الأنوثة في ميزان الإسلام وقار، ورقة، وحياء، وحضور إيجابي يبني الحياة دون ابتذال أو انسحاق.»
  </div>
</div>
"""
    },

    # 48. الباب الثامن: الفصل 1 (Page 48)
    {
        "tag": "الباب الثامن",
        "title": "الفصل الأول: أعتز بكوني مسلمة",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّامِن • الفَصْلُ الأَوَّل</div>
<h1>أَعْتَزُّ بِكَوْنِي مُسْلِمَة</h1>

<p>هويتكِ الإسلامية ليست ثوباً تلبسينه في المسجد وتخلعينه خارجه؛ بل هي جوهر وجودكِ، وبوصلة فكركِ، وسر عزتكِ وكرامتكِ في هذا الوجود.</p>

<p>كوني معتزة بدينكِ، فخورة بحجابكِ وعفتكِ، مدركة لحكمة تشريعاته التي صانت المرأة ورفعت مكانتها وكرمتها بنتاً وأختاً وزوجة وأماً ومربية.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">عزة الإيمان</span>
  لا تجعلي التزامكِ مجرد مظهر تقليدي؛ بل اجعليه عقيدة تنبض في الصدر، وخلقاً يتجلى في المعاملات، ورحمة تسع الجميع، وصدقاً في كل قول وفعل.
</div>
"""
    },

    # 49. الباب الثامن: الفصل 2 (Page 49)
    {
        "tag": "الباب الثامن",
        "title": "الفصل الثاني: الحياء جمال",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّامِن • الفَصْلُ الثَّانِي</div>
<h1>الحَيَاءُ جَمَالٌ وَبَهَاء</h1>

<p>الحياء هو الزينة التي لا تبلى، والنور الذي يضفي على ملامح الفتاة هيبة ووقاراً ومحبة في قلوب الصالحين والصالحات.</p>

<p>الحياء ليس ضعفاً ولا تردداً ولا انسحاباً من ميادين العلم والإبداع؛ بل هو السياج الحصين الذي يحمي جوهركِ من أن يبتذل، ويحفظ صوتكِ وكلماتكِ وأفعالكِ في أبهى وأرفع مقام.</p>

<p>كوني جريئة في طلب المعرفة النافعة، واثقة في قول الحق، عفيفة في خطاكِ وحركاتكِ، حريصة على ما يرضي الله تعالى عنكِ في السر والعلن.</p>
"""
    },

    # 50. الباب الثامن: الفصل 3 (Page 50)
    {
        "tag": "الباب الثامن",
        "title": "الفصل الثالث: جمالي أوسع من مظهري",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّامِن • الفَصْلُ الثَّالِث</div>
<h1>جَمَالِي أَوْسَعُ مِنْ مَظْهَرِي</h1>

<p>من الجميل أن تعتني الفتاة بمظهرها وأناقتها ونظافتها وحسن ثيابها؛ فهذا من الفطرة السليمة والذوق الراقي الذي يحبه الله تعالى، لكن الخلل يقع حين تختزل قيمتها كلها في مقاييس المظهر الخارجي!</p>

<p>هناك جمال في عفة لسانكِ وطيب كلامكِ، وجمال في رحمة قلبكِ بوالديكِ وإخوتكِ، وجمال في اتساع عقلكِ وحكمتكِ، وجمال في وقاركِ واعتزازكِ بحجابكِ.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">الحقيقة الغائبة</span>
  لا تقارني شكلكِ بصور المنصات المصقولة بالمؤثرات والعدسات الزائفة؛ فمعظمها أقنعة لا تمت للواقع بصلة. جمالكِ الحقيقي هو نتاج طهارة قلبكِ وصدق روحكِ.
</div>
"""
    },

    # 51. الباب الثامن: الفصل 4 (Page 51)
    {
        "tag": "الباب الثامن",
        "title": "الفصل الرابع: العناية بنفسي",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّامِن • الفَصْلُ الرَّابِع</div>
<h1>العِنَايَةُ الشَّامِلَةُ بِنَفْسِي</h1>

<p>جسدكِ ونفسكِ أمانة عندكِ من الله تعالى، وحسن رعايتهما عبادة وشكر للمنعم سبحانه. احرصي على هذه الأركان الثمانية:</p>

<div class="interactive-box">
  <div class="checkbox-item"><input type="checkbox" id="care_hygiene" onchange="saveCheck(this.id, this.checked)"/><span><strong>النظافة التامة:</strong> السواك، والتطيب في البيت، وحسن الهندام.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_food" onchange="saveCheck(this.id, this.checked)"/><span><strong>الغذاء الصحي:</strong> تناول الأطعمة الطيبة الطبيعية والابتعاد عن المصنعات.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_water" onchange="saveCheck(this.id, this.checked)"/><span><strong>شرب الماء:</strong> ترطيب الجسد باستمرار على مدار اليوم.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_sleep" onchange="saveCheck(this.id, this.checked)"/><span><strong>النوم الكافي:</strong> النوم ليلاً لتجديد خلايا الدماغ والجسد.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_move" onchange="saveCheck(this.id, this.checked)"/><span><strong>الحركة والنشاط:</strong> ممارسة المشي والتمارين الخفيفة لتنشيط الدورة الدموية.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_skin" onchange="saveCheck(this.id, this.checked)"/><span><strong>العناية بالبشرة والشعر:</strong> بالزيوت والمكونات الطبيعية المعتدلة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_mind" onchange="saveCheck(this.id, this.checked)"/><span><strong>الراحة الذهنية:</strong> أخذ فترات استرخاء بعيداً عن الشاشات والأخبار.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="care_doctor" onchange="saveCheck(this.id, this.checked)"/><span><strong>المتابعة الصحية:</strong> استشارة الطبيبة عند الشعور بأي تعب دون إهمال.</span></div>
</div>
"""
    },

    # 52. الباب الثامن: الفصل 5 (Page 52)
    {
        "tag": "الباب الثامن",
        "title": "الفصل الخامس: طموحي لا يتعارض مع عفتي",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ الثَّامِن • الفَصْلُ الخَامِس</div>
<h1>طُمُوحِي لاَ يَتَعَارَضُ مَعَ عِفَّتِي</h1>

<p>كوني صاحبة طموح عالٍ، وفكر متقد، ورغبة صادقة في التفوق العلمي والمهني والريادي. فالإسلام لم يقيد المرأة عن التعلم والإنجاز والنفع، بل فتح لها أبواب المجد تحت ظلال الكرامة والعفة.</p>

<p>تأملي سيرة أمهات المؤمنين والصحابيات الجليلات: كيف كن عالمات، وفقيهات، وطبيبات، وتاجرات، ومربيات لأعظم جيل عرفه التاريخ، دون أن يفرطن في حياء أو يساومن على ستر.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وصية الباب</span>
  اجمعي بين العلم النافع والخلق الرفيع، وبين الطموح العالي والالتزام الصادق، وكوني برهاناً ساطعاً على أن الفتاة المسلمة قادرة على بلوغ القمم دون أن تخسر حياءها.
</div>
"""
    },

    # 53. الباب التاسع: فاتحة (Page 53)
    {
        "tag": "الباب التاسع",
        "title": "أنا صانعة أثر",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ التَّاسِع</div>
  <h1>أَنَا صَانِعَةُ أَثَر</h1>
  <div class="subtitle">نفع الأسرة، خدمة المجتمع، القدوة بالأفعال، وأجور عظيمة في أعمال صغيرة</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «ليس المهم كم عشتِ من السنين؛ بل كم زرعتِ في تلك السنين من بذور الخير التي ستظل تورق وتؤتي أكلها بعد رحيلكِ.»
  </div>
</div>
"""
    },

    # 54. الباب التاسع: الفصل 1 (Page 54)
    {
        "tag": "الباب التاسع",
        "title": "الفصل الأول: ماذا سأترك خلفي؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ التَّاسِع • الفَصْلُ الأَوَّل</div>
<h1>مَاذَا سَأَتْرُكُ خَلْفِي؟</h1>

<p>تخيلي أنكِ بعد عشرين أو ثلاثين سنة تنظرين إلى شريط حياتكِ المنقضي... فما الذي تحبين أن تتذكريه؟ هل تحبين أن تتذكري آلاف الساعات التي ضاعت في تصفح سفاسف الأمور؟</p>

<p>أم تحبين أن تتذكري علماً علمتِه لمن يجهله، وقلباً كسراً جبرتِه بكلمة طيبة، ووالدين قرت أعينهما ببركِ وإحسانكِ، ومشروعاً نافعاً غرستِ بذرته فأورق شجراً ظليلاً؟</p>

<div class="hadith-box">
  <p>«مَنْ دَلَّ عَلَى خَيْرٍ فَلَهُ مِثْلُ أَجْرِ فَاعِلِهِ»</p>
  <div class="hadith-source">رواه مسلم</div>
</div>

<p>الأثر الصادق لا يشترط أن يكون عملاقاً تهتف له الجماهير؛ بل كلمة حانية في وقت شدة قد تنقذ روحاً وتغير مسار حياة إنسان بالكامل.</p>
"""
    },

    # 55. الباب التاسع: الفصل 2 (Page 55)
    {
        "tag": "الباب التاسع",
        "title": "الفصل الثاني: فتاة تنفع أسرتها",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ التَّاسِع • الفَصْلُ الثَّانِي</div>
<h1>فَتَاةٌ تَنْفَعُ أُسْرَتَهَا</h1>

<p>لا تنتظري أن تخرجي إلى العالم لتكوني نافعة ومؤثرة؛ فميدان الأثر الأول والأعظم هو بيتكِ الذي نشأتِ فيه، وبين والديكِ وإخوتكِ.</p>

<div class="quran-box">
  <p>﴿وَقَضَىٰ رَبُّكَ أَلَّا تَعْبُدُوا إِلَّا إِيَّاهُ وَبِالْوَالِدَيْنِ إِحْسَانًا﴾</p>
  <div class="ayah-source">[الإسراء: ٢٣]</div>
</div>

<p>ساعدي أمكِ في أعباء البيت دون تضجر، وأدخلي السرور على قلب أبيكِ بكلمة حانية ودعاء طيب، وكوني أذناً صاغية لإخوتكِ وأخواتكِ ومصدر أمان ودفء لهم. النجاح في الخارج لا يبرر أبداً التقصير والجفاء في الداخل.</p>
"""
    },

    # 56. الباب التاسع: الفصل 3 (Page 56)
    {
        "tag": "الباب التاسع",
        "title": "الفصل الثالث: فتاة تنفع مجتمعها",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ التَّاسِع • الفَصْلُ الثَّالِث</div>
<h1>فَتَاةٌ تَنْفَعُ مُجْتَمَعَهَا</h1>

<p>مجتمعكِ وأمتكِ في حاجة ماسة إلى فتاة واعية، متعلمة، أمينة، صاحبة مبادرة وهمة. مجالات النفع واسعة تنتظر عطاءكِ:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content">تعليم الأطفال القراءة والكتابة والقرآن والآداب.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content">المشاركة في الأعمال التطوعية والخيرية المنضبطة.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content">نشر الوعي الصحي والتربوي السليم بين الفتيات.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content">إعانة المحتاجين والأرامل والأيتام وسد حاجاتهم.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content">استثمار مهاراتكِ الرقمية والتصميمية في خدمة قضايا الخير.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content">صناعة مبادرات نوعية تعالج مشكلات محيطكِ.</div></div>
"""
    },

    # 57. الباب التاسع: الفصل 4 (Page 57)
    {
        "tag": "الباب التاسع",
        "title": "الفصل الرابع: القدوة بالأفعال",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ التَّاسِع • الفَصْلُ الرَّابِع</div>
<h1>القُدْوَةُ بِالأَفْعَالِ لاَ بِالأَقْوَال</h1>

<p>قد ينسى الناس خطباً رنانة ومنشورات طويلة قرؤوها، لكنهم لا ينسون أبداً موقفاً صادقاً رأوه منكِ، أو خلقاً رفيعاً التزمتِ به في ساعة غضب، أو أمانة تمسكتِ بها حين غاب الرقيب.</p>

<p>كوني صادقة في قولكِ، وفية لعهدكِ، متواضعة في نجاحكِ، رحيمة في قدرتكِ. إياكِ أن تكون نصائحكِ للآخرين أكبر من أفعالكِ في الخلوات؛ فالقدوة الصامتة بأفعالكِ أبلغ ألف مرة من المواعظ النظرية المجردة.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">ميزان التأثير</span>
  أصلحي ما بينكِ وبين الله يصلح الله ما بينكِ وبين خلقه؛ واجعلي عملكِ خالصاً لوجهه الكريم يطرح الله في أثركِ البركة والقبول.
</div>
"""
    },

    # 58. الباب التاسع: الفصل 5 (Page 58)
    {
        "tag": "الباب التاسع",
        "title": "الفصل الخامس: أعمال صغيرة وأجور عظيمة",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ التَّاسِع • الفَصْلُ الخَامِس</div>
<h1>أَعْمَالٌ صَغِيرَةٌ وَأُجُورٌ عَظِيمَة</h1>

<p>لا تحتقري أي معروف مهما بدا بسيطاً في عينكِ؛ فقد يكون هو المنجي لكِ يوم القيامة:</p>

<div class="hadith-box">
  <p>«كُلُّ مَعْرُوفٍ صَدَقَةٌ»</p>
  <div class="hadith-source">متفق عليه</div>
</div>

<div class="interactive-box">
  <p>🌸 <strong>ابتسامتكِ</strong> في وجوه أهلكِ وصديقاتكِ صدقة تطيب الخواطر.</p>
  <p>🌸 <strong>إماطة الأذى</strong> عن الطريق وحفظ نظافة الأماكن العامة.</p>
  <p>🌸 <strong>كف اللسان</strong> عن الغيبة والهمز واللمز جهاد عظيم.</p>
  <p>🌸 <strong>دعوة صادقة بظهر الغيب</strong> لأخت مكروبة أو مسلم مستضعف.</p>
  <p>🌸 <strong>كلمة طيبة</strong> تشجعين بها فتاة متعثرة على النهوض مجدداً.</p>
  <p>🌸 <strong>إصلاح ذات البين</strong> بين صديقتين اختلفتا أو قريبتين تباعدتا.</p>
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">رسالة الباب التاسع</span>
  لا تقولي أبداً: ماذا أستطيع أن أفعل وأنا مجرد فتاة واحدة؟ بل اسألي: ما الخير الذي أستطيع أن أبدأ به اليوم بما أملكُ من وقت وطاقة؟
</div>
"""
    },

    # 59. الباب العاشر: فاتحة (Page 59)
    {
        "tag": "الباب العاشر",
        "title": "خطتي لحياة تستحق",
        "type": "opener",
        "html": """
<div class="opener">
  <div class="chapter-number">البَابُ العَاشِر</div>
  <h1>خُطَّتِي لِحَيَاةٍ تَسْتَحِقّ</h1>
  <div class="subtitle">تحويل الأحلام إلى أهداف، قهر التسويف، التعلم من التعثر، وخطة الثلاثين يوماً</div>
  <div class="ornament">❀ ❀ ❀</div>
  <div class="wisdom-pearl" style="max-width: 86%;">
    «الأمنيات رأس مال المفاليس؛ أما صاحبة الرسالة فتبني أحلامها على خطط محكمة وسعي يومي لا يفتر.»
  </div>
</div>
"""
    },

    # 60. الباب العاشر: الفصل 1 (Page 60)
    {
        "tag": "الباب العاشر",
        "title": "الفصل الأول: أحلامي تتحول إلى أهداف",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ العَاشِر • الفَصْلُ الأَوَّل</div>
<h1>أَحْلاَمِي تَتَحَوَّلُ إِلَى أَهْدَاف</h1>

<p>الأحلام والأمنيات جميلة تبعث الأمل في النفوس؛ لكنها لا تصنع واقعاً وحدها إن لم تدعم بخطة محكمة وإجراءات تنفيذية يومية دقيقة.</p>

<p>تعلمي تحويل العبارات العامة الفضفاضة إلى أهداف واضحة ومقاسة:</p>

<div class="step-card"><div class="step-num">✘</div><div class="step-content"><strong>أمنية عامة:</strong> «أريد أن أكون فتاة مثقفة وقارئة.»</div></div>
<div class="step-card"><div class="step-num">✔</div><div class="step-content"><strong>هدف ذكي محكم:</strong> «سأقرأ كتاباً واحداً كل أسبوعين بمعدل ١٥ صفحة يومياً بعد صلاة العصر وألخص أهم فوائده.»</div></div>

<div class="step-card"><div class="step-num">✘</div><div class="step-content"><strong>أمنية عامة:</strong> «أريد أن أحسن صلاتي وقربي من الله.»</div></div>
<div class="step-card"><div class="step-num">✔</div><div class="step-content"><strong>هدف ذكي محكم:</strong> «سألتزم بصلاة السنن الرواتب يومياً، وأقرأ تفسير صفحة من القرآن مع حضور القلب.»</div></div>
"""
    },

    # 61. الباب العاشر: الفصل 2 (Page 61)
    {
        "tag": "الباب العاشر",
        "title": "الفصل الثاني: كيف أخطط؟",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ العَاشِر • الفَصْلُ الثَّانِي</div>
<h1>كَيْفَ أُخَطِّطُ لِحَيَاتِي؟</h1>

<p>وزعي أهداف حياتكِ على خمسة مسارات أساسية لتضمني نمواً متوازناً دون أن يطغى جانب على جانب:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>المسار الإيماني:</strong> الصلاة في وقتها، الورد القرآني، الأذكار، وحسن الصلة بالله.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>المسار العلمي والمهني:</strong> التفوق الدراسي، إتقان مهارة، والقراءة الواعية.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>المسار الشخصي والنفسي:</strong> ضبط الانفعالات، الثقة بالنفس، وممارسة هواية نافعة.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>المسار الصحي والبدني:</strong> تنظيم النوم، الغذاء السليم، والحركة اليومية.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>المسار الأسري والمجتمعي:</strong> بر الوالدين، صلة الرحم، وبذل الخير للناس.</div></div>

<p style="font-size: .88em; color: var(--text-secondary); text-align: center;">لا تضعي في كل مسار أكثر من هدفين في وقت واحد حتى لا تشتتي تركيزكِ.</p>
"""
    },

    # 62. الباب العاشر: الفصل 3 (Page 62)
    {
        "tag": "الباب العاشر",
        "title": "الفصل الثالث: أتغلب على التسويف",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ العَاشِر • الفَصْلُ الثَّالِث</div>
<h1>أَتَغَلَّبُ عَلَى التَّسْوِيف</h1>

<p>التسويف هو سارق الأعمار؛ تأجيل العمل المهم رغم معرفتكِ بضرورته، وللتغلب عليه اعتمدي هذه الوسائل السبع المجربة:</p>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>قاعدة الخمس دقائق:</strong> ألزمي نفسكِ بالبدء في المهمة لخمس دقائق فقط؛ وغالباً ستستمرين بعدها.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>تفتيت المهام الكبيرة:</strong> قسّمي المشروع الضخم إلى خطوات صغيرة يسهل إنجازها.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>عزل الهاتف والمشتتات:</strong> ضعي الهاتف في غرفة أخرى أثناء فترات التركيز.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>البدء بالمهمة الأثقل:</strong> أنجزي أصعب مهمة في الصباح لترتاحي بقية يومكِ.</div></div>
<div class="step-card"><div class="step-num">٥</div><div class="step-content"><strong>مكافأة الإنجاز:</strong> كافئي نفسكِ باستراحة لطيفة بعد كل عمل مكتمل.</div></div>
<div class="step-card"><div class="step-num">٦</div><div class="step-content"><strong>إسقاط انتظار الحماس:</strong> الانضباط يعني إنجاز ما يجب حتى وإن لم تشعري بالرغبة.</div></div>
<div class="step-card"><div class="step-num">٧</div><div class="step-content"><strong>الخطوة الصغيرة المنفذة:</strong> خير من أعظم خطة تبقى حبراً على ورق!</div></div>
"""
    },

    # 63. الباب العاشر: الفصل 4 (Page 63)
    {
        "tag": "الباب العاشر",
        "title": "الفصل الرابع: أتعلم من التعثر",
        "type": "reading",
        "html": """
<div class="eyebrow">البَابُ العَاشِر • الفَصْلُ الرَّابِع</div>
<h1>أَتَعَلَّمُ مِنَ التَّعَثُّر</h1>

<p>قد تخفقين في امتحان دراسي، أو تتراجع همتكِ عن برنامج وضعتِه، أو لا يوفق مشروع بذلتِ فيه جهدكِ؛ فإياكِ أن تعتبري ذلك نهاية المطاف!</p>

<div class="quran-box">
  <p>﴿فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا﴾</p>
  <div class="ayah-source">[الشرح: ٥-٦]</div>
</div>

<p>كل تعثر هو درس ثمين يمنحكِ خبرة وبصيرة، فاسألي نفسكِ بهدوء:</p>
<div class="step-card"><div class="step-num">؟</div><div class="step-content">ما الذي لم ينجح في خطتي بدقة؟</div></div>
<div class="step-card"><div class="step-num">؟</div><div class="step-content">ما المهارة أو الأداة التي كنتُ أفتقر إليها؟</div></div>
<div class="step-card"><div class="step-num">؟</div><div class="step-content">من أصحاب الخبرة الذين يمكنني استشارتهم؟</div></div>
<div class="step-card"><div class="step-num">؟</div><div class="step-content">ما هي الخطوة الأولى التي سأنهض بها اليوم؟</div></div>
"""
    },

    # 64. الباب العاشر: الفصل 5 (Page 64)
    {
        "tag": "الباب العاشر",
        "title": "الفصل الخامس: خطتي للثلاثين يوماً القادمة",
        "type": "exercise",
        "html": """
<div class="eyebrow">تَطْبِيقُ الانْطِلاَق</div>
<h2>خُطَّتِي لِلثَّلاَثِينَ يَوْماً القَادِمَة</h2>
<p style="font-size: .88em; color: var(--text-secondary);">حددي هدفاً واحداً واضحاً ومحدداً في كل مجال (تُحفظ تلقائياً في جهازكِ):</p>

<div class="interactive-box">
  <div class="field-group">
    <label>١. هدفي الإيماني الأساسي:</label>
    <input class="field-input" id="plan30_faith" placeholder="مثال: المحافظة على صلاة الضحى والوتر يومياً..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>٢. هدفي العلمي والمعرفي:</label>
    <input class="field-input" id="plan30_study" placeholder="مثال: قراءة كتابين وتلخيصهما..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>٣. هدفي الشخصي لتطوير ذاتي:</label>
    <input class="field-input" id="plan30_self" placeholder="مثال: التدرب على الحديث أمام الآخرين..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>٤. هدفي الصحي والبدني:</label>
    <input class="field-input" id="plan30_health" placeholder="مثال: شرب لترين ماء والمشي نصف ساعة..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>٥. هدفي في نفع أسرتي والآخرين:</label>
    <input class="field-input" id="plan30_service" placeholder="مثال: مساعدة أمي يومياً وتعليم أختي الصغيرة..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>
"""
    },

    # 65. الخاتمة (1) (Page 65)
    {
        "tag": "الخاتمة",
        "title": "إلى الفتاة التي ستصنع الفرق (١)",
        "type": "reading",
        "html": """
<div class="eyebrow">الخَاتِمَة</div>
<h1>إِلَى الفَتَاةِ الَّتِي سَتَصْنَعُ الفَرْق</h1>
<div class="ornament">❀ ❀ ❀</div>

<p>يا ابنتي المباركة <span class="reader-name-slot"></span>...</p>

<p>ها أنتِ تصلين إلى نهاية صفحات هذا الكتاب، لكنها ليست نهاية المطاف؛ بل هي لحظة الانطلاق الحقيقية إلى واقعكِ وحياتكِ!</p>

<p>ربما اكتشفتِ خلال القراءة جوانب لم تنتبهي إليها في نفسكِ من قبل، وربما أدركتِ أنكِ بحاجة إلى تغيير بعض العادات، أو مراجعة بعض الأفكار، أو إعادة ترتيب أولوياتكِ في الحياة.</p>

<p>لا تجعلي هذه الكلمات مجرد عبارات عاطفية تقرئينها ثم تطوين الصفحة؛ بل خذي منها فكرة واحدة واعملي بها، وعادة حسنة وداومي عليها، وخطأً تعلمتِ كيف تتجنبينه، وهدفاً عقدتِ العزم على بلوغه بعون الله.</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">نداء القلب</span>
  لن تكون كل الأيام سهلة، ولن يصفق لكِ الجميع، ولن تتحقق كل الأمنيات في غمضة عين؛ لكنكِ قادرة على أن تجعلي كل صباح جديد فرصة للتعلم، والإصلاح، والتقرب إلى الله.
</div>
"""
    },

    # 66. الخاتمة (2) (Page 66)
    {
        "tag": "الخاتمة",
        "title": "إلى الفتاة التي ستصنع الفرق (٢)",
        "type": "reading",
        "html": """
<div class="eyebrow">مِسْكُ الخِتَام</div>
<h2>أَنَا فَتَاةٌ وَلِي رِسَالَة</h2>

<p>إذا تعثرتِ فقومي، وإذا أخطأتِ فتوبي وأصلحي، وإذا جهلتِ فتعلمي، وإذا خفتِ فاستعيني بالله وتوكلي عليه، وإذا وفقتِ ونجحتِ فاحمديه وتواضعي.</p>

<p>كوني فتاة تقرأ، وتفكر، وتبني، وتعمل، وتعتذر حين تخطئ، وتنهض شامخة حين تعصف بها رياح الأيام. كوني فتاة تحفظ كرامتها، وتحترم عقلها، وتحمل قلباً رحيماً معلقاً بالسماء.</p>

<div class="quran-box" style="margin: 22px 0;">
  <p style="font-size: 1.25em; font-family: var(--font-headings);">«أَنَا فَتَاةٌ مُسْلِمَةٌ، أَعْبُدُ اللَّهَ وَأَسْعَى لِرِضَاهُ، أَتَعَلَّمُ لِأَنْتَفِعَ وَأَنْفَعَ، وَأَبْنِي نَفْسِي لِأَصْنَعَ الأَثَرَ الطَّيِّبَ فِي العَالَمِ»</p>
</div>

<p style="text-align: center; font-weight: 700; color: var(--accent-rose-dark); font-size: 1.1em;">
  أنا لستُ كاملة، لكني أسعى إلى الإحسان...<br/>
  أنا لا أملك تغيير العالم كله، لكني أبدأ بنفسي...<br/>
  🌸 أنا فتاة ولي رسالة 🌸
</p>
"""
    },

    # 67. ميثاق الفتاة صاحبة الرسالة (Page 67)
    {
        "tag": "الميثاق",
        "title": "ميثاق الفتاة صاحبة الرسالة",
        "type": "exercise",
        "html": """
<div class="eyebrow">عَهْدٌ وَمِيثَاق</div>
<h1>مِيثَاقُ الفَتَاةِ صَاحِبَةِ الرِّسَالَة</h1>

<div class="interactive-box" style="background: linear-gradient(145deg, var(--bg-soft-rose), var(--bg-secondary)); border: 1.5px solid var(--accent-gold);">
  <p>✦ <strong>أعاهد نفسي</strong> أن أجعل رضا الله تعالى غايتي في سري وعلني.</p>
  <p>✦ <strong>أعاهد نفسي</strong> أن أحافظ على كرامتي وعفتي، ولا أقيس قيمتي بآراء الناس.</p>
  <p>✦ <strong>أعاهد نفسي</strong> أن أتعلم ما ينفعني، وأستثمر مواهبي، وأحترم عقلي.</p>
  <p>✦ <strong>أعاهد نفسي</strong> أن أحفظ لساني، وأبر والديّ، وأبذل الإحسان لأسرتي.</p>
  <p>✦ <strong>أعاهد نفسي</strong> ألا أستسلم للمقارنة، وألا أجعل التعثر سبباً للتوقف.</p>
  <p>✦ <strong>أعاهد نفسي</strong> أن أكون صديقة وفية صالحة، وأختار صحبتي بعناية.</p>
  <p>✦ <strong>أعاهد نفسي</strong> أن أكون صانعة أثر، وأزرع الخير حيثما حللت.</p>

  <div class="covenant-sign-box">
    <div class="field-group">
      <label>اسم القارئة المعاهدة (توقيع الشرف):</label>
      <input class="field-input" id="covenant_signature" placeholder="اكتبي توقيعكِ الكريم هنا..." oninput="saveField(this.id, this.value)"/>
    </div>
    <div class="field-group">
      <label>تاريخ إتمام وقبول الميثاق:</label>
      <input class="field-input" id="covenant_date" placeholder="اليوم والتاريخ المبارك..." oninput="saveField(this.id, this.value)"/>
    </div>
    <button class="primary-btn" onclick="saveCovenant()">اعتماد وتوقيع الميثاق ✍️</button>
  </div>
</div>
"""
    },

    # 68. الملحق الأول: مرآة الذات وخريطة القيم (Page 68)
    {
        "tag": "الملحق الأول",
        "title": "مرآة اكتشاف الذات وخريطة القيم",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الأَوَّل</div>
<h1>مِرْآةُ اكْتِشَافِ الذَّاتِ وَخَرِيطَةُ القِيَم</h1>

<div class="interactive-box">
  <h3 style="margin-bottom: 6px;">🧭 أولاً: من أنا في عمقي؟</h3>
  <div class="field-group">
    <label>أكثر أمر يشتتني ويضيع وقتي:</label>
    <input class="field-input" id="ref_distraction" placeholder="اكتبي الصدق هنا..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>أكثر أمر يخيفني وكيف سأتجاوزه:</label>
    <input class="field-input" id="ref_fear_plan" placeholder="الخوف من الفشل -> التوكل على الله وحسن الاستعداد..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>

<div class="interactive-box" style="margin-top: 14px;">
  <h3 style="margin-bottom: 6px;">💎 ثانياً: اختاري أهم خمس قيم تقود حياتكِ:</h3>
  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 4px;">
    <div class="checkbox-item"><input type="checkbox" id="v_faith" onchange="saveCheck(this.id, this.checked)"/><span>الإيمان واليقين</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_truth" onchange="saveCheck(this.id, this.checked)"/><span>الصدق والأمانة</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_mercy" onchange="saveCheck(this.id, this.checked)"/><span>الرحمة والرفق</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_chastity" onchange="saveCheck(this.id, this.checked)"/><span>العفة والحياء</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_science" onchange="saveCheck(this.id, this.checked)"/><span>العلم والحكمة</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_duty" onchange="saveCheck(this.id, this.checked)"/><span>المسؤولية والإتقان</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_discipline" onchange="saveCheck(this.id, this.checked)"/><span>الانضباط والصبر</span></div>
    <div class="checkbox-item"><input type="checkbox" id="v_cooperate" onchange="saveCheck(this.id, this.checked)"/><span>التعاون ونفع الناس</span></div>
  </div>
</div>
"""
    },

    # 69. الملحق الثاني: لوحة أهدافي (Page 69)
    {
        "tag": "الملحق الثاني",
        "title": "لوحة أهدافي الكبرى",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الثَّانِي</div>
<h1>لَوْحَةُ أَهْدَافِي الكُبْرَى</h1>
<p style="font-size: .88em; color: var(--text-secondary);">خططي لهدفكِ الأكبر لهذا العام بوضوح تام وعزيمة ماضية:</p>

<div class="interactive-box">
  <div class="field-group">
    <label>هدفي الكبير لهذا العام:</label>
    <input class="field-input" id="goal_title" placeholder="مثال: حفظ خمسة أجزاء من القرآن، أو نيل الامتياز الأكاديمي..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>لماذا أريد تحقيق هذا الهدف بالذات؟ (الدافع الحقيقي):</label>
    <input class="field-input" id="goal_why" placeholder="لأنه يرضي ربي ويرفع قدري وينفع أمتي..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>ما المهارات التي أحتاج إلى اكتسابها لبلوغه؟</label>
    <input class="field-input" id="goal_skills" placeholder="التنظيم اليومي، مهارات الاستذكار والتركيز..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>العقبات المحتملة وكيف سأتعامل معها بحكمة:</label>
    <input class="field-input" id="goal_obstacles" placeholder="التسويف والمشتتات -> تخصيص بيئة عمل خالية من الهاتف..." oninput="saveField(this.id, this.value)"/>
  </div>
  <div class="field-group">
    <label>الخطوة العملية الأولى التي سأبدأ بها فوراً:</label>
    <input class="field-input" id="goal_first_step" placeholder="التسجيل في الدورة، أو فتح الكتاب وبدء الصفحة الأولى..." oninput="saveField(this.id, this.value)"/>
  </div>
</div>
"""
    },

    # 70. الملحق الثالث: تحدي الـ 30 يوماً (1) (Page 70)
    {
        "tag": "الملحق الثالث",
        "title": "تحدي الثلاثين يوماً (١-١٥)",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الثَّالِث</div>
<h1>تَحَدِّي الثَّلاَثِينَ يَوْماً (النِّصْفُ الأَوَّل)</h1>
<p style="font-size: .88em; color: var(--text-secondary);">فرصة مباركة لتكوين عادات طيبة؛ ضعي علامة صح عند إنجاز مهمة كل يوم:</p>

<div class="interactive-box" style="line-height: 1.6;">
  <div class="checkbox-item"><input type="checkbox" id="ch_day_1" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١: اكتبي أهدافكِ لهذا الشهر بوضوح.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_2" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢: نظمي ورتبي مكان دراستكِ وغرفتكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_3" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٣: اقرئي عشر صفحات من كتاب نافع.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_4" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٤: تعلمي معلومة جديدة في تخصصكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_5" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٥: ساعدي والدتكِ في عمل دون أن تطلب.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_6" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٦: احذفي تطبيقاً واحداً يضيع وقتكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_7" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٧: اكتبي عشر نعم من نعم الله واشكريه.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_8" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٨: ابدئي في تدريب مهارة جديدة تحبينها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_9" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٩: حددي عادة سلبية وقرري استبدالها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_10" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٠: أرسلي رسالة امتنان ودعاء لوالديكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_11" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١١: أنجزي مهمة صعبة مؤجلة منذ مدة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_12" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٢: اقرئي سيرة صحابية جليلة وتأمليها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_13" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٣: صلي رحمكِ بمكالمة أو زيارة طيبة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_14" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٤: راجعي ما أنجزتِ في الأسبوعين وكافئي نفسكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_15" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٥: خططي للمرحلة القادمة بهمة متجددة.</span></div>
</div>
"""
    },

    # 71. الملحق الثالث: تحدي الـ 30 يوماً (2) (Page 71)
    {
        "tag": "الملحق الثالث",
        "title": "تحدي الثلاثين يوماً (١٦-٣٠)",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الثَّالِث</div>
<h1>تَحَدِّي الثَّلاَثِينَ يَوْماً (النِّصْفُ الثَّانِي)</h1>

<div class="interactive-box" style="line-height: 1.6;">
  <div class="checkbox-item"><input type="checkbox" id="ch_day_16" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٦: تدربي على التعبير عن رأيكِ بأدب وثقة.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_17" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٧: تخلصي من مشتت واحد في محيطكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_18" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٨: اكتبي ثلاث نقاط قوة حباكِ الله بها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_19" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ١٩: ابدئي مشروعاً أو عملاً صغيراً نافعاً.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_20" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٠: قدمي مساعدة لمحتاجة دون انتظار مقابل.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_21" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢١: راجعي خطتكِ الشهرية وقومي مسارها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_22" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٢: تعلمي مسألة شرعية في فقه العبادات.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_23" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٣: مارسي المشي واستنشقي الهواء النقي.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_24" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٤: واجهي مهمة كنتِ تتهربين من إنجازها.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_25" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٥: دوني ثلاثة دروس تعلمتِها هذا الشهر.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_26" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٦: حسني خلقاً واحداً في تعاملكِ مع من حولكِ.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_27" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٧: شاركي في عمل تطوعي أو نفع مجتمعي.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_28" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٨: قنني وقت الشاشات ليكون أقل من ساعتين.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_29" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٢٩: اكتبي قائمة إنجازاتكِ واحمدي الله.</span></div>
  <div class="checkbox-item"><input type="checkbox" id="ch_day_30" onchange="saveCheck(this.id, this.checked)"/><span>اليوم ٣٠: ضعي خطة الشهر القادم بعزيمة أرسخ!</span></div>
</div>
"""
    },

    # 72. الملحق الرابع: بطاقات إشراق (1) (Page 72)
    {
        "tag": "الملحق الرابع",
        "title": "رسائل قصيرة لقلبك (١-٦)",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الرَّابِع</div>
<h1>رَسَائِلُ قَصِيرَةٌ لِقَلْبِكِ (١)</h1>
<p style="font-size: .88em; color: var(--text-secondary); text-align: center;">همسات رقيقة تضيء دربكِ كلما احتجتِ إلى استراحة وسكينة:</p>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ١</span>
  لا تنتظري أن يراكِ الجميع؛ يكفي أن يكون عملكِ صالحاً، وأن يعلم الله صدق سعيكِ في خلواتكِ.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٢</span>
  أنتِ لا تحتاجين إلى التشبه بغيركِ حتى تكوني مميزة؛ تميزكِ الحقيقي في أصالتكِ وقيمكِ وحيائكِ.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٣</span>
  لا تجعلي خطأً واحداً يقنعكِ بأنكِ فاشلة؛ التوبة باب مفتوح يغسل الماضي ويبني المستقبل.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٤</span>
  كل مهارة نافعة تتقنينها اليوم بعناء وصبر، قد تصبح غداً باباً واسعاً من أبواب تفريج الكربات.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٥</span>
  لا تستهيني بالكلمة الطيبة والابتسامة؛ فقد تكونان بلسماً يشفي قلباً كسره حزن دفين.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٦</span>
  احفظي وقتكِ فهو عمركِ الذي لن يعوض؛ وما يمضي من الساعات لا تشتريه كنوز الأرض.
</div>
"""
    },

    # 73. الملحق الرابع: بطاقات إشراق (2) (Page 73)
    {
        "tag": "الملحق الرابع",
        "title": "رسائل قصيرة لقلبك (٧-١٢)",
        "type": "appendix",
        "html": """
<div class="eyebrow">المُلْحَقُ الرَّابِع</div>
<h1>رَسَائِلُ قَصِيرَةٌ لِقَلْبِكِ (٢)</h1>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٧</span>
  لا تجعلي رضا الناس غايتكِ القصوى؛ فرضاهم غاية لا تدرك، ورضا الله هو الأصل والمنجى.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٨</span>
  كوني قوية دون قسوة، ومتواضعة دون ذلة، وواثقة دون غرور، وحازمة دون فظاظة.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ٩</span>
  لا تتوقفي عن التعلم لمجرد أنكِ نلتِ شهادة؛ فالعلم بحر لا ساحل له، وصاحبته ترتقي دائماً.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ١٠</span>
  اجعلي كل نجاح تحرزينه سبباً للتواضع والشكر، لا سبباً للنظر إلى الآخرين بتعالٍ وعجب.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ١١</span>
  لا تؤجلي التوبة الصادقة، ولا تؤجلي العمل الصالح؛ فالبداية الحقيقية هي الآن.
</div>

<div class="wisdom-pearl">
  <span class="wisdom-badge">وردة ١٢</span>
  كوني صانعة أثر مبارك، ولو كان في عين الناس صغيراً؛ فإن عند الله ما لا تراه عيون البشر.
</div>
"""
    },

    # 74. مصادر الاستزادة (Page 74)
    {
        "tag": "المصادر",
        "title": "مصادر الاستزادة والمراجع",
        "type": "reading",
        "html": """
<div class="eyebrow">مَصَادِرُ الاسْتِزَادَة</div>
<h1>المَصَادِرُ وَالمَرَاجِعُ المُوَثَّقَة</h1>
<div class="ornament">❖ ❖ ❖</div>

<div class="step-card"><div class="step-num">١</div><div class="step-content"><strong>القرآن الكريم وعلومه:</strong> المصحف الشريف، تفسير السعدي (تيسير الكريم الرحمن)، والتفسير الميسر لنخبة من العلماء.</div></div>
<div class="step-card"><div class="step-num">٢</div><div class="step-content"><strong>الحديث النبوي الشريف:</strong> صحيح البخاري، صحيح مسلم، رياض الصالحين للإمام النووي، والأربعون النووية.</div></div>
<div class="step-card"><div class="step-num">٣</div><div class="step-content"><strong>السيرة النبوية والتاريخ:</strong> الرحيق المختوم لصفي الرحمن المباركفوري، وصور من حياة الصحابيات لعبد الرحمن رأفت الباشا.</div></div>
<div class="step-card"><div class="step-num">٤</div><div class="step-content"><strong>تزكية النفس وبناء الشخصية:</strong> الفوائد والوابل الصيب لابن قيم الجوزية، مختصر منهاج القاصدين لابن قدامة، وجدد حياتك لمحمد الغزالي، وشخصية المرأة المسلمة لمحمد علي الهاشمي.</div></div>

<div class="wisdom-pearl" style="margin-top: 20px;">
  <span class="wisdom-badge">ملاحظة علمية وتوثيقية</span>
  الآيات القرآنية الكريمة والأحاديث النبوية الشريفة الواردة في هذا الكتاب عُزيت بدقة إلى مصادرها المعتمدة في كتب السنة والتفسير، لتكون على بصيرة ويقين تام.
</div>
"""
    },

    # 75. كلمة أخيرة والشعار الختامي (Page 75)
    {
        "tag": "كلمة أخيرة",
        "title": "كلمة أخيرة وشعار الكتاب",
        "type": "closing",
        "html": """
<div class="closing">
  <div class="eyebrow">كَلِمَةٌ أَخِيرَة</div>
  <h1 style="color: var(--accent-rose-dark); margin-bottom: 8px;">إِلَى كُلِّ فَتَاةٍ أَغْلَقَتْ هَذَا الكِتَاب...</h1>
  <div class="ornament">❀ ❀ ❀</div>

  <p style="font-size: 1.05em; line-height: 2.1; margin-bottom: 18px;">
    لا تغلقي معه باب التغيير المبارك!<br/>
    خذي من هذه الصفحات قراراً واحداً صادقاً، وحوّليه اليوم إلى عمل.<br/>
    اقتربي من الله، وابني عقلكِ، وزكي قلبكِ، واحفظي كرامتكِ، وطوري قدراتكِ، وانفعي من حولكِ...<br/>
    فأنتِ لا تعيشين لتملئي أيامكِ كيفما اتفق؛ بل لتعبدي الله، وتصنعي في الحياة أثراً طيباً يخلد ذكركِ.
  </p>

  <div class="quran-box" style="margin: 16px auto; max-width: 90%;">
    <div style="font-size: 1.3em; font-family: var(--font-headings); color: var(--accent-rose-dark); font-weight: 700;">🌸 أَنَا فَتَاةٌ وَلِي رِسَالَة 🌸</div>
    <div style="font-size: 1.05em; font-family: var(--font-body); color: var(--accent-sage-dark); margin-top: 6px;">«أَتَعَلَّمُ لِأَرْتَقِيَ، وَأَعْمَلُ لِأَنْفَعَ، وَأَعِيشُ لِأَبْتَغِيَ رِضَا اللَّهِ»</div>
  </div>

  <div class="ornament">❖ تَمَّ بِحَمْدِ اللَّهِ وَتَوْفِيقِهِ ❖</div>
  <button class="primary-btn" onclick="window.goToPage(0)">العودة إلى الغلاف الأول ↩</button>
</div>
"""
    }
]
