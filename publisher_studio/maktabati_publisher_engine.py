#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
=========================================================================================
🏛️ محرك واستوديو دار النشر العالمية لمنصة «مكتبتي» (Maktabati World-Class Publishing Engine V11)
=========================================================================================

المهمة الهندسية والفنية لهذا المحرك:
------------------------------------
صُمّم هذا المحرك ليكون «دار النشر الرقمية المتكاملة» لتطبيق «مكتبتي» على جميع هواتف أندرويد بمختلف أحجامها:
1. تنوع بصري عالمي (10 هويات ورقية وطباعية مستوحاة من أرقى دور النشر العالمية):
   - أوراق البيج المريحة للعين (Eye-Comfort Sepia & Warm Cream)
   - أوراق الكتب القديمة والمخطوطات المعتّقة (Aged Vintage Parchment & Andalusian Manuscript)
   - الكتب الأدبية الكلاسيكية والأكاديمية (Oxford Ivory & Classic Literature)
   - الرسائل الوجدانية والمذكرات الأنثوية (Royal Dusty Rose & Olive Sage)
   - الواحات الإيمانية والقرآنية (Emerald Sanctuary & Mushaf Gold)
   - القراءة الليلية الفاخرة المريحة للعين (Velvet Midnight & Warm Amber)

2. الفهرس المنبثق الذكي (Zero-Page Modal Table of Contents):
   - الفهرس لا يستهلك أي صفحة من صفحات الكتاب إطلاقاً!
   - يظهر في نافذة منبثقة أنيقة عند الضغط على زر الفهرس (📑) في الشريط العلوي، مع انتقال فوري للصفحة.

3. عناية فائقة بنصوص وآيات القرآن الكريم والتشكيل الكامل:
   - تغليف تلقائي واحترافي للآيات القرآنية الكريمة ﴿ ... ﴾ داخل إطار مصحفي مذهّب بخط المصحف (`--font-quran: "Amiri Quran", "Scheherazade New"`).
   - دعم كامل للخطوط الـ 25 المدمجة في تطبيق «مكتبتي» (خطوط القرآن، المتون، المذكرات والرقعة، والكوفي).

4. تكبير انسيابي باللمس والإصبعين (Pinch-to-Zoom & Fluid Reflow) مصمم خصيصاً لهواتف أندرويد:
   - جميع البطاقات والآيات والزخارف والفقرات تتمدد وتلتفّ بنعومة (`max-width: 100%; overflow-wrap: anywhere;`)
     عند التكبير بالإصبعين من 75% حتى 200% دون أن يختفي حرف واحد أو يخرج شكل واحد خارج شاشة الهاتف.
"""

import os
import re
import sys
import json
import argparse

# ==============================================================================
# 1. موسوعة هويات دور النشر العالمية وألوان الأوراق المريحة للعين (10 World-Class Themes)
# ==============================================================================
PUBLISHING_HOUSE_THEMES = {
    "vintage_parchment": {
        "name": "أوراق الكتب القديمة والمخطوطات المعتّقة (Vintage Aged Book Paper)",
        "bg_primary": "#F4ECD8",
        "bg_card": "#FBF6E9",
        "paper_gradient": "radial-gradient(circle at 50% 25%, #FDF9EE 0%, #F4ECD8 100%)",
        "text_main": "#2B2118",
        "text_muted": "#6E5A47",
        "accent_primary": "#8C5230",
        "accent_deep": "#593119",
        "accent_secondary": "#4A6B5B",
        "accent_secondary_deep": "#2D473B",
        "accent_gold": "#B8863B",
        "border_warm": "#DECJAF".replace("J", "B"),
        "quran_bg": "#F7EFE0",
        "font_body_default": "Amiri",
        "font_headings_default": "Aref Ruqaa",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Scheherazade New",
        "font_quran_default": "Amiri Quran"
    },
    "eye_comfort_beige": {
        "name": "الورق البيج الكريمي المريح للعين للقراءة الطويلة (Eye-Comfort Warm Beige)",
        "bg_primary": "#F6F0E4",
        "bg_card": "#FCF9F2",
        "paper_gradient": "linear-gradient(180deg, #FCF9F2 0%, #F5EFE2 100%)",
        "text_main": "#26221E",
        "text_muted": "#665E54",
        "accent_primary": "#7C5836",
        "accent_deep": "#4F361E",
        "accent_secondary": "#557153",
        "accent_secondary_deep": "#364C35",
        "accent_gold": "#C29243",
        "border_warm": "#E2D7C5",
        "quran_bg": "#F9F4E8",
        "font_body_default": "Noto Naskh Arabic",
        "font_headings_default": "El Messiri",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Amiri",
        "font_quran_default": "Amiri Quran"
    },
    "royal_rose": {
        "name": "الهوية الوجدانية والرسائل الأنثوية (الوردي الغباري والزيتي والذهب)",
        "bg_primary": "#FAF6F0",
        "bg_card": "#FFFDF9",
        "paper_gradient": "linear-gradient(180deg, #FFFDF9 0%, #FAF5EE 100%)",
        "text_main": "#38272C",
        "text_muted": "#6B565C",
        "accent_primary": "#BD6B80",
        "accent_deep": "#732E41",
        "accent_secondary": "#6B856E",
        "accent_secondary_deep": "#445946",
        "accent_gold": "#C69A4E",
        "border_warm": "#DDD1C4",
        "quran_bg": "#FBF7F0",
        "font_body_default": "Noto Naskh Arabic",
        "font_headings_default": "Aref Ruqaa",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Amiri",
        "font_quran_default": "Amiri Quran"
    },
    "andalusian_manuscript": {
        "name": "الرقّ الأندلسي والزمرد والذهب المعتّق (Andalusian Heritage)",
        "bg_primary": "#F2E9D4",
        "bg_card": "#FAF4E6",
        "paper_gradient": "radial-gradient(ellipse at top, #FBF6EA 0%, #EFE4CA 100%)",
        "text_main": "#211B15",
        "text_muted": "#5E5042",
        "accent_primary": "#1E5E4B",
        "accent_deep": "#123D30",
        "accent_secondary": "#8B5A2B",
        "accent_secondary_deep": "#5C3A1B",
        "accent_gold": "#B58534",
        "border_warm": "#D9CAAE",
        "quran_bg": "#F5EDD8",
        "font_body_default": "Amiri",
        "font_headings_default": "Katibeh",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Lateef",
        "font_quran_default": "Amiri Quran"
    },
    "diaries_warm_notebook": {
        "name": "دفتر المذكرات والخواطر الدافئة (Warm Personal Diary & Journal)",
        "bg_primary": "#F8F3EC",
        "bg_card": "#FFFDFB",
        "paper_gradient": "linear-gradient(180deg, #FFFDFB 0%, #F7F0E6 100%)",
        "text_main": "#2F2626",
        "text_muted": "#6D5B5B",
        "accent_primary": "#A65D57",
        "accent_deep": "#6E3531",
        "accent_secondary": "#6E8274",
        "accent_secondary_deep": "#455449",
        "accent_gold": "#C49553",
        "border_warm": "#E3D6C8",
        "quran_bg": "#FAF5ED",
        "font_body_default": "Tajawal",
        "font_headings_default": "Katibeh",
        "font_chapter_default": "El Messiri",
        "font_quotes_default": "Marhey",
        "font_quran_default": "Amiri Quran"
    },
    "oxford_classic_ivory": {
        "name": "طبعة الدور الأكاديمية والفكرية العريقة (Classic Ivory & Oxford Navy)",
        "bg_primary": "#F4F1EA",
        "bg_card": "#FCFAF5",
        "paper_gradient": "linear-gradient(180deg, #FCFAF5 0%, #F2EEE5 100%)",
        "text_main": "#1E242B",
        "text_muted": "#525D6B",
        "accent_primary": "#26466D",
        "accent_deep": "#162B45",
        "accent_secondary": "#7A583B",
        "accent_secondary_deep": "#523924",
        "accent_gold": "#B88B4A",
        "border_warm": "#DAD3C5",
        "quran_bg": "#F7F3E8",
        "font_body_default": "Markazi Text",
        "font_headings_default": "Noto Kufi Arabic",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Amiri",
        "font_quran_default": "Scheherazade New"
    },
    "emerald_sanctuary": {
        "name": "واحة القرآن والسكينة الإيمانية (Emerald Sanctuary & Cream)",
        "bg_primary": "#F3F6F1",
        "bg_card": "#FBFDF9",
        "paper_gradient": "linear-gradient(180deg, #FBFDF9 0%, #EEF3EB 100%)",
        "text_main": "#1C2921",
        "text_muted": "#4F6356",
        "accent_primary": "#2E6B4E",
        "accent_deep": "#1B4632",
        "accent_secondary": "#8C6D46",
        "accent_secondary_deep": "#5E472B",
        "accent_gold": "#C29648",
        "border_warm": "#CFDCD2",
        "quran_bg": "#F4F8F2",
        "font_body_default": "Noto Naskh Arabic",
        "font_headings_default": "Aref Ruqaa",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Scheherazade New",
        "font_quran_default": "Amiri Quran"
    },
    "velvet_blossom": {
        "name": "هوية خيمة في القلب (الوردي المخملي والذهبي الوردي)",
        "bg_primary": "#FFF1F6",
        "bg_card": "#FFFBFD",
        "paper_gradient": "linear-gradient(180deg, #FFFBFD 0%, #FCEBF2 100%)",
        "text_main": "#3C1F2E",
        "text_muted": "#7D5266",
        "accent_primary": "#B94F7A",
        "accent_deep": "#91405F",
        "accent_secondary": "#A36D82",
        "accent_secondary_deep": "#7A4259",
        "accent_gold": "#B88945",
        "border_warm": "#F0D8E2",
        "quran_bg": "#FFF6F9",
        "font_body_default": "Noto Naskh Arabic",
        "font_headings_default": "Aref Ruqaa",
        "font_chapter_default": "Reem Kufi",
        "font_quotes_default": "Amiri",
        "font_quran_default": "Amiri Quran"
    }
}

# ==============================================================================
# 2. القالب المعماري الشامل للكتاب (Mobile-First Responsive Android Book Template)
# ==============================================================================
MASTER_BOOK_HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover"/>
  <title>__BOOK_TITLE__</title>
  <style id="maktabati-world-publishing-theme">
    :root {
      --bg-primary: __BG_PRIMARY__;
      --bg-card: __BG_CARD__;
      --text-main: __TEXT_MAIN__;
      --text-muted: __TEXT_MUTED__;
      --accent-rose: __ACCENT_PRIMARY__;
      --accent-rose-deep: __ACCENT_DEEP__;
      --accent-olive: __ACCENT_SECONDARY__;
      --accent-olive-deep: __ACCENT_SECONDARY_DEEP__;
      --accent-gold: __ACCENT_GOLD__;
      --beige-warm: __BORDER_WARM__;
      --quran-bg: __QURAN_BG__;

      /* منظومة الخطوط السبعة المتكاملة مع تطبيق مكتبتي */
      --font-body: "__FONT_BODY__", "Noto Naskh Arabic", "Amiri", serif;
      --font-headings: "__FONT_HEADINGS__", "Aref Ruqaa", "Amiri", serif;
      --font-chapter-titles: "__FONT_CHAPTER__", "Reem Kufi", "Cairo", sans-serif;
      --font-quotes: "__FONT_QUOTES__", "Amiri", "Scheherazade New", serif;
      --font-quran: "__FONT_QURAN__", "Amiri Quran", "Scheherazade New", "Amiri", serif;
      --font-interface: "Noto Sans Arabic", "Tajawal", "Cairo", sans-serif;
      --font-numbers: "Noto Sans Arabic", sans-serif;

      /* متغيرات التكبير الانسيابي بالإصبعين والأزرار (Fluid Reflow Zoom) */
      --reader-font: 1;
      --reader-font-size: 100%;
      --font-scale: 1;
    }

    * {
      box-sizing: border-box !important;
      max-width: 100%;
      -webkit-tap-highlight-color: transparent;
    }

    html, body {
      margin: 0;
      padding: 0;
      width: 100%;
      max-width: 100% !important;
      min-height: 100vh;
      background-color: var(--bg-primary);
      color: var(--text-main);
      font-family: var(--font-body);
      font-size: calc(1.06rem * var(--font-scale, 1));
      line-height: 1.98;
      overflow-x: hidden !important;
      overscroll-behavior-x: none !important;
      touch-action: pan-y !important;
      -webkit-text-size-adjust: 100%;
      word-break: break-word;
      overflow-wrap: anywhere;
    }

    /* الشريط العلوي المتجاوب لجميع شاشات هواتف أندرويد */
    .topbar, .top-bar {
      position: sticky;
      top: 0;
      z-index: 90;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: var(--bg-card);
      border-bottom: 1px solid var(--beige-warm);
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
      font-family: var(--font-interface);
      touch-action: manipulation !important;
    }

    .topbar-brand {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 700;
      color: var(--accent-rose-deep);
      font-size: 0.92rem;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 52%;
    }

    .topbar-actions {
      display: flex;
      align-items: center;
      gap: 5px;
      flex-shrink: 0;
    }

    .icon-btn {
      min-width: 38px;
      min-height: 38px;
      padding: 0 8px;
      border-radius: 10px;
      border: 1px solid var(--beige-warm);
      background: var(--bg-primary);
      color: var(--accent-rose-deep);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      font-size: 0.92rem;
      font-family: var(--font-interface);
      font-weight: 700;
    }

    /* مسرح الصفحة المصمم خصيصاً لشاشات الهواتف بمختلف أحجامها */
    .book-stage {
      width: 100%;
      max-width: 640px;
      margin: 10px auto 82px;
      padding: 0 10px;
    }

    .page-card, .page, .book-page {
      background: __PAPER_GRADIENT__;
      background-color: var(--bg-card);
      border: 1px solid var(--beige-warm);
      border-radius: 18px;
      padding: 20px 16px;
      box-shadow: 0 6px 22px rgba(0, 0, 0, 0.05), inset 0 0 0 1px rgba(255, 255, 255, 0.45);
      position: relative;
      min-height: 74vh;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      overflow-x: hidden !important;
    }

    /* ترويسة الصفحة الجارية */
    .running-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 6px;
      border-bottom: 1px dashed var(--beige-warm);
      padding-bottom: 8px;
      margin-bottom: 14px;
      font-family: var(--font-interface);
      font-size: 0.78rem;
      color: var(--text-muted);
    }

    .running-tag {
      background: rgba(198, 154, 78, 0.14);
      color: var(--accent-rose-deep);
      padding: 2px 10px;
      border-radius: 99px;
      font-weight: 700;
    }

    .page-body {
      width: 100%;
      max-width: 100% !important;
      overflow-x: hidden !important;
      overflow-wrap: anywhere;
      word-break: break-word;
    }

    /* العناوين والفقرات المتجاوبة مع التكبير */
    h1, h2, h3 {
      font-family: var(--font-headings);
      color: var(--accent-rose-deep);
      line-height: 1.5;
      margin: 0.5em 0 0.45em;
      overflow-wrap: anywhere;
    }

    .chapter-badge {
      display: inline-block;
      font-family: var(--font-chapter-titles);
      background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-deep));
      color: #fff;
      padding: 4px 14px;
      border-radius: 99px;
      font-size: 0.84em;
      margin-bottom: 8px;
    }

    p {
      margin: 0 0 1em;
      text-align: justify;
      overflow-wrap: anywhere;
    }

    /* إطار الآيات القرآنية الكريمة بالرسم المصحفي المذهّب */
    .quran-verse, .ayah-box, .mushaf-verse {
      font-family: var(--font-quran);
      background: var(--quran-bg);
      border: 1.5px solid var(--accent-gold);
      border-radius: 14px;
      padding: 14px 14px;
      margin: 14px 0;
      text-align: center;
      font-size: 1.16em;
      line-height: 2.15;
      color: var(--accent-olive-deep);
      box-shadow: inset 0 0 12px rgba(198, 154, 78, 0.08);
      position: relative;
      overflow-wrap: anywhere;
    }

    /* البطاقات التذكارية والهمسات والاقتباسات */
    .heart-keepsake-card, .wisdom-pearl {
      background: linear-gradient(180deg, var(--bg-card) 0%, var(--bg-primary) 100%);
      border: 1px solid var(--beige-warm);
      border-right: 4px solid var(--accent-gold);
      border-radius: 14px;
      padding: 14px 16px;
      margin: 14px 0;
      font-family: var(--font-quotes);
      overflow-wrap: anywhere;
    }

    .quote-box, blockquote {
      background: rgba(189, 107, 128, 0.06);
      border-right: 4px solid var(--accent-rose);
      border-radius: 12px;
      padding: 12px 15px;
      margin: 14px 0;
      font-family: var(--font-quotes);
      color: var(--accent-rose-deep);
      overflow-wrap: anywhere;
    }

    .whisper-pill {
      display: inline-block;
      background: rgba(107, 133, 110, 0.14);
      color: var(--accent-olive-deep);
      border: 1px solid rgba(107, 133, 110, 0.3);
      padding: 3px 12px;
      border-radius: 99px;
      font-size: 0.8em;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .interactive-box {
      background: rgba(107, 133, 110, 0.06);
      border: 1px dashed var(--accent-olive);
      border-radius: 14px;
      padding: 12px 14px;
      margin-top: 14px;
    }

    .interactive-box textarea {
      width: 100%;
      min-height: 70px;
      border: 1px solid var(--beige-warm);
      border-radius: 10px;
      padding: 8px 10px;
      font-family: var(--font-body);
      font-size: 0.92em;
      background: var(--bg-card);
      color: var(--text-main);
    }

    /* النوافذ المنبثقة: الفهرس المنبثق (لا يأخذ صفحة في الكتاب) + لوحة البحث */
    .overlay-modal {
      display: none;
      position: fixed;
      inset: 0;
      z-index: 130;
      background: rgba(30, 22, 24, 0.45);
      backdrop-filter: blur(4px);
      padding: 12px;
    }

    .overlay-modal.active {
      display: flex;
      align-items: flex-start;
      justify-content: center;
    }

    .modal-sheet {
      width: 100%;
      max-width: 580px;
      margin-top: 8px;
      background: var(--bg-card);
      border: 1px solid var(--beige-warm);
      border-radius: 18px;
      padding: 14px;
      box-shadow: 0 14px 36px rgba(0, 0, 0, 0.22);
      max-height: 84vh;
      display: flex;
      flex-direction: column;
      font-family: var(--font-interface);
    }

    .modal-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--beige-warm);
      padding-bottom: 10px;
      margin-bottom: 10px;
    }

    .modal-list {
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 7px;
      padding-bottom: 6px;
    }

    .toc-item, .search-result-item {
      padding: 10px 12px;
      border-radius: 12px;
      border: 1px solid var(--beige-warm);
      background: var(--bg-primary);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }

    .toc-item.active {
      border-color: var(--accent-rose);
      background: rgba(189, 107, 128, 0.12);
    }

    mark.calm-search-highlight {
      background: rgba(198, 154, 78, 0.32);
      color: var(--accent-rose-deep);
      padding: 1px 4px;
      border-radius: 4px;
    }

    /* شريط التنقل السفلي */
    .bottom-nav {
      position: fixed;
      bottom: 0;
      right: 0;
      left: 0;
      z-index: 85;
      background: var(--bg-card);
      border-top: 1px solid var(--beige-warm);
      padding: 8px 14px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      max-width: 640px;
      margin: 0 auto;
      font-family: var(--font-interface);
      touch-action: manipulation !important;
    }

    .nav-btn {
      padding: 7px 16px;
      border-radius: 99px;
      border: 1px solid var(--accent-rose);
      background: var(--accent-rose);
      color: #fff;
      font-weight: 700;
      cursor: pointer;
      font-family: var(--font-interface);
      font-size: 0.88rem;
    }

    .nav-btn:disabled {
      opacity: 0.38;
      cursor: default;
    }
  </style>
</head>
<body>
  <header class="topbar">
    <div class="topbar-brand">
      <span>📖</span>
      <span>__BOOK_TITLE__</span>
    </div>
    <div class="topbar-actions">
      <button id="tocBtn" class="icon-btn" aria-label="فهرس الكتاب" title="فهرس الكتاب">📑 الفهرس</button>
      <button id="searchOpen" class="icon-btn" aria-label="البحث في الكتاب" title="بحث">🔍</button>
      <button id="btnZoomOut" class="icon-btn" aria-label="تصغير النص" title="تصغير">A-</button>
      <button id="btnZoomIn" class="icon-btn" aria-label="تكبير النص" title="تكبير">A+</button>
    </div>
  </header>

  <!-- نافذة الفهرس المنبثقة (لا تأخذ صفحة في الكتاب بل تظهر عند الضغط على زر الفهرس) -->
  <div id="tocPanel" class="overlay-modal" aria-hidden="true">
    <div class="modal-sheet">
      <div class="modal-head">
        <strong style="color:var(--accent-rose-deep);font-size:1rem;">📑 فهرس أبواب وفصول الكتاب</strong>
        <button id="tocClose" class="icon-btn" aria-label="إغلاق الفهرس">✕</button>
      </div>
      <div id="tocPanelList" class="modal-list"></div>
    </div>
  </div>

  <!-- نافذة البحث الفوري الذكي -->
  <div id="searchPanel" class="overlay-modal" aria-hidden="true">
    <div class="modal-sheet">
      <div style="display:flex;align-items:center;gap:6px;">
        <input id="searchInput" type="search" style="flex:1;padding:9px 12px;border-radius:10px;border:1px solid var(--accent-rose);font-family:var(--font-interface);" placeholder="ابحث عن كلمة أو آية في الكتاب..."/>
        <button id="searchClearBtn" class="icon-btn" aria-label="مسح البحث">⌫</button>
        <button id="searchClose" class="icon-btn" aria-label="إغلاق البحث">✕</button>
      </div>
      <div id="searchStatus" style="font-size:0.8rem;color:var(--text-muted);margin:6px 0;"></div>
      <div id="searchResults" class="modal-list"></div>
    </div>
  </div>

  <main id="bookStage" class="book-stage">
    <article id="activePageArticle" class="page-card page active" data-page="0"></article>
  </main>

  <nav class="bottom-nav">
    <button id="btnPrevPage" class="nav-btn">السابق</button>
    <span id="pageCounter" class="page-counter" style="font-family:var(--font-numbers);font-weight:700;">1 / __TOTAL_PAGES__</span>
    <button id="btnNextPage" class="nav-btn">التالي</button>
  </nav>

  <script>
    const BOOK_DATA = {
      title: __BOOK_TITLE_JSON__,
      subtitle: __BOOK_SUBTITLE_JSON__,
      totalPages: __TOTAL_PAGES__,
      toc: __TOC_JSON__,
      pages: __PAGES_JSON__
    };

    let currentPageIndex = 0;
    let currentZoomPercent = 100;

    function normalizeArabicWithMap(text) {
      const normChars = [];
      const indexMap = [];
      for (let i = 0; i < text.length; i++) {
        const ch = text[i];
        if (/[\\u064B-\\u065F\\u0670\\u0640]/.test(ch)) continue;
        let n = ch;
        if (/[أإآٱ]/.test(ch)) n = 'ا';
        else if (ch === 'ة') n = 'ه';
        else if (ch === 'ى') n = 'ي';
        normChars.push(n.toLowerCase());
        indexMap.push(i);
      }
      return { normalized: normChars.join(''), map: indexMap };
    }

    function applyCalmPageSearchHighlight(container, rawQuery) {
      if (!rawQuery || !rawQuery.trim()) return;
      const qNorm = normalizeArabicWithMap(rawQuery.trim()).normalized;
      if (!qNorm) return;
      const walker = document.createTreeWalker(container, NodeFilter.SHOW_TEXT, null);
      const nodes = [];
      while (walker.nextNode()) nodes.push(walker.currentNode);
      nodes.forEach(node => {
        const val = node.nodeValue || '';
        const { normalized, map } = normalizeArabicWithMap(val);
        const idx = normalized.indexOf(qNorm);
        if (idx !== -1) {
          const startOrig = map[idx];
          const endOrig = map[idx + qNorm.length - 1] + 1;
          const frag = document.createDocumentFragment();
          frag.appendChild(document.createTextNode(val.slice(0, startOrig)));
          const mark = document.createElement('mark');
          mark.className = 'calm-search-highlight';
          mark.textContent = val.slice(startOrig, endOrig);
          frag.appendChild(mark);
          frag.appendChild(document.createTextNode(val.slice(endOrig)));
          node.parentNode.replaceChild(frag, node);
        }
      });
    }

    function renderTocModal() {
      const listEl = document.getElementById('tocPanelList');
      listEl.innerHTML = '';
      BOOK_DATA.toc.forEach(item => {
        const div = document.createElement('div');
        div.className = 'toc-item' + (item.pageIndex === currentPageIndex ? ' active' : '');
        div.innerHTML = `
          <div>
            <div style="font-size:0.76rem;color:var(--text-muted);">${item.chapterTag || ''}</div>
            <strong style="color:var(--text-main);font-size:0.94rem;">${item.title}</strong>
          </div>
          <small style="font-family:var(--font-numbers);color:var(--accent-rose-deep);font-weight:700;">ص ${item.pageIndex + 1}</small>
        `;
        div.onclick = () => {
          document.getElementById('tocPanel').classList.remove('active');
          goToPage(item.pageIndex);
        };
        listEl.appendChild(div);
      });
    }

    function renderPage(index, highlightQuery) {
      if (index < 0) index = 0;
      if (index >= BOOK_DATA.totalPages) index = BOOK_DATA.totalPages - 1;
      currentPageIndex = index;
      const page = BOOK_DATA.pages[index];
      const article = document.getElementById('activePageArticle');
      article.setAttribute('data-page', String(index));
      const headerHtml = index === 0 ? '' : `
        <div class="running-header">
          <span class="running-tag">${page.chapterTag || 'فصل'}</span>
          <span>${page.sectionTitle || ''}</span>
        </div>`;
      article.innerHTML = `${headerHtml}<div id="pageInnerContent" class="page-body">${page.html}</div>`;
      document.getElementById('pageCounter').textContent = `${index + 1} / ${BOOK_DATA.totalPages}`;
      document.getElementById('btnPrevPage').disabled = (index === 0);
      document.getElementById('btnNextPage').disabled = (index === BOOK_DATA.totalPages - 1);
      if (highlightQuery) applyCalmPageSearchHighlight(article, highlightQuery);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    window.goToPage = function(index) {
      renderPage(Number(index) || 0);
    };

    function executeBookSearch() {
      const q = document.getElementById('searchInput').value.trim();
      const resultsEl = document.getElementById('searchResults');
      const statusEl = document.getElementById('searchStatus');
      resultsEl.innerHTML = '';
      if (!q) { statusEl.textContent = ''; return; }
      const qNorm = normalizeArabicWithMap(q).normalized;
      const hits = [];
      BOOK_DATA.pages.forEach((p, idx) => {
        const plain = p.html.replace(/<[^>]+>/g, ' ');
        const { normalized } = normalizeArabicWithMap(plain);
        if (normalized.includes(qNorm)) {
          hits.push({ pageIndex: idx, title: p.sectionTitle || p.chapterTag || `صفحة ${idx + 1}`, snippet: plain.slice(0, 120) + '...' });
        }
      });
      statusEl.textContent = hits.length ? `تم العثور على ${hits.length} نتيجة` : 'لا توجد نتائج مطابقة';
      hits.forEach(h => {
        const div = document.createElement('div');
        div.className = 'search-result-item';
        div.innerHTML = `<div><strong>صفحة ${h.pageIndex + 1} — ${h.title}</strong><div style="font-size:0.82em;color:var(--text-muted);margin-top:3px;">${h.snippet}</div></div>`;
        div.onclick = () => {
          document.getElementById('searchPanel').classList.remove('active');
          renderPage(h.pageIndex, q);
        };
        resultsEl.appendChild(div);
      });
    }

    window.__maktabatiSetFluidZoom = function(percentOrScale) {
      let pct = Number(percentOrScale) || 100;
      if (pct <= 3) pct = Math.round(pct * 100);
      pct = Math.max(75, Math.min(200, pct));
      currentZoomPercent = pct;
      const scale = +(pct / 100).toFixed(2);
      document.documentElement.style.setProperty('--font-scale', String(scale));
      document.documentElement.style.setProperty('--reader-font', String(scale));
      document.documentElement.style.setProperty('--reader-font-size', pct + '%');
    };

    document.getElementById('btnPrevPage').onclick = () => renderPage(currentPageIndex - 1);
    document.getElementById('btnNextPage').onclick = () => renderPage(currentPageIndex + 1);
    document.getElementById('btnZoomIn').onclick = () => window.__maktabatiSetFluidZoom(currentZoomPercent + 10);
    document.getElementById('btnZoomOut').onclick = () => window.__maktabatiSetFluidZoom(currentZoomPercent - 10);

    document.getElementById('tocBtn').onclick = () => {
      renderTocModal();
      document.getElementById('tocPanel').classList.add('active');
    };
    document.getElementById('tocClose').onclick = () => document.getElementById('tocPanel').classList.remove('active');

    document.getElementById('searchOpen').onclick = () => {
      document.getElementById('searchPanel').classList.add('active');
      document.getElementById('searchInput').focus();
    };
    document.getElementById('searchClose').onclick = () => document.getElementById('searchPanel').classList.remove('active');
    document.getElementById('searchClearBtn').onclick = () => {
      document.getElementById('searchInput').value = '';
      executeBookSearch();
    };
    document.getElementById('searchInput').oninput = executeBookSearch;

    renderPage(0);
  </script>
</body>
</html>
"""


def format_quranic_verses_inline(text: str) -> str:
    """
    يكتشف الآيات القرآنية الكريمة المحاطة بـ ﴿ ... ﴾ ويغلفها تلقائياً بـ `.quran-verse`
    لتظهر بالخط المصحفي المذهّب (`--font-quran`).
    """
    return re.sub(
        r'(﴿[^﴾]+﴾)',
        r'<div class="quran-verse">\1</div>',
        text
    )


def parse_raw_manuscript_to_pages_and_toc(raw_text: str, title: str, subtitle: str) -> tuple:
    """
    يقسم المخطوطة الخام إلى صفحات متوازنة دون المساس بحرف واحد من صياغة المؤلف،
    ويبني فهرساً منبثقاً ذكياً (`toc`) يفتح بالضغط على زر الفهرس دون أن يأخذ صفحة في الكتاب.
    """
    pages = [{
        "pageNumber": 1,
        "chapterTag": "الغلاف الملكي",
        "sectionTitle": title,
        "html": f"""
        <div class="cover-page" style="text-align:center;padding:24px 10px;">
          <div class="whisper-pill">إصدارات دار النشر لمنصة مكتبتي</div>
          <h1 style="font-size:1.85rem;margin:14px 0 8px;">{title}</h1>
          <p style="text-align:center;color:var(--text-muted);font-size:1.02rem;">{subtitle}</p>
        </div>
        """
    }]
    toc = [{
        "pageIndex": 0,
        "chapterTag": "الغلاف",
        "title": title
    }]

    blocks = [b.strip() for b in re.split(r'\n\s*\n', raw_text) if b.strip()]
    current_chunk = []
    current_len = 0
    current_chapter = "فصول الكتاب"
    current_section = title

    for block in blocks:
        lines = [ln.strip() for ln in block.split('\n') if ln.strip()]
        if not lines:
            continue

        # التعرف الذكي على الأبواب والفصول لبناء الفهرس المنبثق تلقائياً
        if len(lines) == 1 and len(lines[0]) < 90 and (
            any(k in lines[0] for k in ("الفصل", "الباب", "الرسالة", "أولاً", "ثانياً", "ثالثاً", "رابعاً", "خامساً", "خاتمة", "مقدمة", "تمهيد", "المحطة"))
        ):
            current_section = lines[0]
            target_page_idx = len(pages) if current_chunk else len(pages)
            toc.append({
                "pageIndex": target_page_idx,
                "chapterTag": current_chapter,
                "title": current_section
            })
            formatted = f"<h2>{lines[0]}</h2>"
        elif block.startswith("﴿") and block.endswith("﴾"):
            formatted = f'<div class="quran-verse">{block}</div>'
        elif block.startswith("قال"):
            formatted = f'<div class="quote-box">{format_quranic_verses_inline(block)}</div>'
        elif block.startswith("✦") or block.startswith("💡") or block.startswith("🌸"):
            formatted = f'<div class="heart-keepsake-card">{format_quranic_verses_inline(block)}</div>'
        else:
            formatted = "".join(f"<p>{format_quranic_verses_inline(ln)}</p>" for ln in lines)

        current_chunk.append(formatted)
        current_len += len(block)

        if current_len >= 920:
            pages.append({
                "pageNumber": len(pages) + 1,
                "chapterTag": current_chapter,
                "sectionTitle": current_section,
                "html": "\n".join(current_chunk)
            })
            current_chunk = []
            current_len = 0

    if current_chunk:
        pages.append({
            "pageNumber": len(pages) + 1,
            "chapterTag": current_chapter,
            "sectionTitle": current_section,
            "html": "\n".join(current_chunk)
        })

    return pages, toc


def build_world_class_book_html(pages: list, toc: list, title: str, subtitle: str, theme_key: str = "eye_comfort_beige") -> str:
    theme = PUBLISHING_HOUSE_THEMES.get(theme_key, PUBLISHING_HOUSE_THEMES["eye_comfort_beige"])
    html = MASTER_BOOK_HTML_TEMPLATE
    replacements = {
        "__BOOK_TITLE__": title,
        "__BOOK_TITLE_JSON__": json.dumps(title, ensure_ascii=False),
        "__BOOK_SUBTITLE_JSON__": json.dumps(subtitle, ensure_ascii=False),
        "__TOTAL_PAGES__": str(len(pages)),
        "__TOC_JSON__": json.dumps(toc, ensure_ascii=False, indent=2),
        "__PAGES_JSON__": json.dumps(pages, ensure_ascii=False, indent=2),
        "__BG_PRIMARY__": theme["bg_primary"],
        "__BG_CARD__": theme["bg_card"],
        "__PAPER_GRADIENT__": theme["paper_gradient"],
        "__TEXT_MAIN__": theme["text_main"],
        "__TEXT_MUTED__": theme["text_muted"],
        "__ACCENT_PRIMARY__": theme["accent_primary"],
        "__ACCENT_DEEP__": theme["accent_deep"],
        "__ACCENT_SECONDARY__": theme["accent_secondary"],
        "__ACCENT_SECONDARY_DEEP__": theme["accent_secondary_deep"],
        "__ACCENT_GOLD__": theme["accent_gold"],
        "__BORDER_WARM__": theme["border_warm"],
        "__QURAN_BG__": theme["quran_bg"],
        "__FONT_BODY__": theme["font_body_default"],
        "__FONT_HEADINGS__": theme["font_headings_default"],
        "__FONT_CHAPTER__": theme["font_chapter_default"],
        "__FONT_QUOTES__": theme["font_quotes_default"],
        "__FONT_QURAN__": theme["font_quran_default"],
    }
    for k, v in replacements.items():
        html = html.replace(k, v)
    return html


def main():
    parser = argparse.ArgumentParser(description="محرك واستوديو دار النشر العالمية لمنصة مكتبتي V11")
    parser.add_argument("--input", required=True, help="مسار ملف المخطوطة النصية (.txt) أو (.json)")
    parser.add_argument("--output", required=True, help="مسار حفظ ملف الكتاب (.html)")
    parser.add_argument("--title", default="كتاب جديد في مكتبتي", help="عنوان الكتاب")
    parser.add_argument("--subtitle", default="طبعة رقمية تفاعلية فاخرة", help="العنوان الفرعي")
    parser.add_argument("--theme", default="eye_comfort_beige", choices=list(PUBLISHING_HOUSE_THEMES.keys()), help="هوية ورق الكتاب ودار النشر")
    args = parser.parse_args()

    with open(args.input, "r", encoding="utf-8") as f:
        content = f.read()

    if args.input.endswith(".json"):
        data = json.loads(content)
        if isinstance(data, dict):
            pages = data.get("pages", [])
            toc = data.get("toc", [])
        else:
            pages = data
            toc = [{"pageIndex": i, "chapterTag": p.get("chapterTag", ""), "title": p.get("sectionTitle", f"صفحة {i+1}")} for i, p in enumerate(pages)]
    else:
        pages, toc = parse_raw_manuscript_to_pages_and_toc(content, args.title, args.subtitle)

    html_out = build_world_class_book_html(pages, toc, args.title, args.subtitle, args.theme)
    os.makedirs(os.path.dirname(os.path.abspath(args.output)), exist_ok=True)
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(html_out)

    print(f"✅ تم إصدار الكتاب بنجاح: {args.output} ({len(pages)} صفحة | الفهرس منبثق: {len(toc)} مدخل)")


if __name__ == "__main__":
    main()
