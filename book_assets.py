# -*- coding: utf-8 -*-
"""
book_assets.py
Styles, fonts, and SVG artwork for «أنا فتاة ولي رسالة».
"""

FONTS_CSS = """
  @font-face {
    font-family: "Amiri";
    src: url("assets/fonts/amiri/amiri-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/amiri/amiri-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Amiri";
    src: url("assets/fonts/amiri/amiri-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/amiri/amiri-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Naskh Arabic";
    src: url("assets/fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Naskh Arabic";
    src: url("assets/fonts/noto-naskh-arabic/noto-naskh-arabic-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-naskh-arabic/noto-naskh-arabic-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Aref Ruqaa";
    src: url("assets/fonts/aref-ruqaa/aref-ruqaa-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/aref-ruqaa/aref-ruqaa-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Aref Ruqaa";
    src: url("assets/fonts/aref-ruqaa/aref-ruqaa-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/aref-ruqaa/aref-ruqaa-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Reem Kufi";
    src: url("assets/fonts/reem-kufi/reem-kufi-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/reem-kufi/reem-kufi-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Reem Kufi";
    src: url("assets/fonts/reem-kufi/reem-kufi-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/reem-kufi/reem-kufi-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "IBM Plex Sans Arabic";
    src: url("assets/fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "IBM Plex Sans Arabic";
    src: url("assets/fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Sans Arabic";
    src: url("assets/fonts/noto-sans-arabic/noto-sans-arabic-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-sans-arabic/noto-sans-arabic-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Sans Arabic";
    src: url("assets/fonts/noto-sans-arabic/noto-sans-arabic-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-sans-arabic/noto-sans-arabic-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Kufi Arabic";
    src: url("assets/fonts/noto-kufi-arabic/noto-kufi-arabic-400.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-kufi-arabic/noto-kufi-arabic-400.woff2") format("woff2");
    font-weight: 400;
    font-style: normal;
    font-display: swap;
  }
  @font-face {
    font-family: "Noto Kufi Arabic";
    src: url("assets/fonts/noto-kufi-arabic/noto-kufi-arabic-700.woff2") format("woff2"),
         url("https://maktabati.app/assets/fonts/noto-kufi-arabic/noto-kufi-arabic-700.woff2") format("woff2");
    font-weight: 700;
    font-style: normal;
    font-display: swap;
  }
"""

STYLES = """
:root {
  --bg-primary: #FAF6F0;
  --bg-secondary: #FFFDF9;
  --bg-soft-rose: #FCF2F5;
  --bg-soft-sage: #EFF5F0;
  --bg-pearl-gold: #FDF7EC;
  
  --accent-rose: #BD6B80;
  --accent-rose-dark: #732E41;
  --accent-rose-light: #D98C9F;
  --accent-sage: #6B856E;
  --accent-sage-dark: #445946;
  --accent-gold: #C69A4E;
  --accent-gold-soft: rgba(198,154,78,0.22);
  
  --text-primary: #38272C;
  --text-secondary: #6B565C;
  --line: rgba(189,107,128,0.18);
  --border-ornate: rgba(198,154,78,0.32);
  --paper-edge: #DDD1C4;
  
  --shadow-card: 0 16px 42px rgba(92,35,51,0.07);
  --reader-font: 21px;
  --reader-font-size: 100%;
  --font-scale: 1;
  
  --font-body: "Noto Naskh Arabic", "Amiri", serif;
  --font-headings: "Aref Ruqaa", "Amiri", serif;
  --font-chapter-titles: "Reem Kufi", "Noto Kufi Arabic", sans-serif;
  --font-quotes: "Amiri", "Noto Naskh Arabic", serif;
  --font-interface: "Noto Sans Arabic", "IBM Plex Sans Arabic", sans-serif;
  --font-numbers: "Noto Sans Arabic", "IBM Plex Sans Arabic", sans-serif;
  --reader-family: var(--font-body);
  
  color-scheme: light;
}

* {
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
}

html, body, .app {
  touch-action: pan-y !important;
}

button, input, select, textarea {
  touch-action: manipulation;
  font: inherit;
}

#bookStage, .panel {
  touch-action: pan-y;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100%;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-body);
}

body {
  overflow: hidden;
}

.app {
  height: 100vh;
  max-height: 100vh;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: linear-gradient(155deg, var(--bg-primary), #F7EDE7 55%, var(--bg-soft-rose) 100%);
}

.top-bar {
  height: 58px;
  min-height: 58px;
  flex: 0 0 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 clamp(12px, 3vw, 22px);
  gap: 10px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--line);
  z-index: 10;
  box-shadow: 0 3px 14px rgba(92,35,51,0.05);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brand-gem {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  color: #fff;
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  font-size: 16px;
  box-shadow: 0 3px 9px rgba(189,107,128,0.28);
}

.tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
}

.icon-btn, .nav-btn {
  border: 1px solid var(--line);
  background: var(--bg-primary);
  color: var(--accent-rose-dark);
  border-radius: 12px;
  min-width: 40px;
  min-height: 40px;
  padding: 6px 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-interface);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background .18s, border-color .18s, transform .15s;
}

.icon-btn svg {
  width: 18px;
  height: 18px;
  stroke: currentColor;
  fill: none;
  stroke-width: 2.1;
  stroke-linecap: round;
  stroke-linejoin: round;
  pointer-events: none;
}

.icon-btn:active, .nav-btn:active {
  transform: scale(0.97);
}

.icon-btn:focus-visible, .nav-btn:focus-visible, .primary-btn:focus-visible, .toc-item:focus-visible {
  outline: 3px solid var(--accent-gold);
  outline-offset: 2px;
}

.icon-btn.bookmarked {
  background: var(--bg-pearl-gold);
  color: var(--accent-gold);
  border-color: var(--accent-gold);
}

.running-header {
  height: 36px;
  min-height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  padding: 2px 14px;
  background: var(--accent-rose-dark);
  color: #FAF4EF;
  border-bottom: 2px solid var(--accent-gold);
  font-family: var(--font-interface);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .02em;
  text-align: center;
}

.running-header[hidden] {
  display: none;
}

.running-header-mark {
  color: #E6C88B;
  font-size: 12px;
}

.book-stage {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  padding: clamp(10px, 2.7vw, 22px) clamp(9px, 2vw, 16px) 18px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  scrollbar-width: thin;
  scrollbar-color: rgba(189,107,128,0.28) transparent;
}

.page-canvas {
  width: 100%;
  max-width: 760px;
  min-height: 100%;
  height: auto;
  margin: 0 auto;
  padding: clamp(20px, 4.4vw, 38px) clamp(18px, 5vw, 44px) clamp(28px, 5vw, 44px);
  position: relative;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  box-shadow: var(--shadow-card);
  display: block;
  overflow: visible;
}

.page-canvas:before {
  content: "";
  position: absolute;
  inset: 7px;
  border: 1px solid rgba(198,154,78,0.18);
  border-radius: 12px;
  pointer-events: none;
}

.page {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 100%;
  font-family: var(--font-body);
  font-size: var(--reader-font);
  line-height: 2.05;
  overflow-wrap: break-word;
  word-wrap: break-word;
  animation: pageIn .22s ease both;
}

@keyframes pageIn {
  from { opacity: .45; transform: translateY(4px); }
  to { opacity: 1; transform: none; }
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--accent-rose-dark);
  background: var(--bg-soft-rose);
  border: 1px solid var(--line);
  border-radius: 99px;
  padding: 5px 16px;
  font-family: var(--font-interface);
  font-size: .61em;
  font-weight: 700;
  letter-spacing: .025em;
  margin: 0 auto 16px;
}

h1, h2, h3, p {
  margin-top: 0;
  max-width: 100%;
}

h1 {
  font-family: var(--font-headings);
  font-size: 1.62em;
  line-height: 1.55;
  color: var(--accent-rose-dark);
  margin: 0 0 16px;
  font-weight: 700;
}

h2 {
  font-family: var(--font-headings);
  font-size: 1.2em;
  line-height: 1.65;
  color: var(--accent-rose);
  margin: 0 0 14px;
  font-weight: 700;
}

h3 {
  font-family: var(--font-headings);
  font-size: 1.02em;
  color: var(--accent-sage-dark);
  margin: 20px 0 9px;
  font-weight: 700;
}

p {
  font-family: var(--font-body);
  margin: 0 0 1.02em;
  text-align: justify;
  white-space: normal;
  overflow-wrap: break-word;
  hyphens: none;
}

.reading p:not(.subtitle):not(.no-indent) {
  text-indent: 1.15em;
}

.cover-page {
  min-height: clamp(520px, 72vh, 820px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  position: relative;
  isolation: isolate;
  padding: clamp(28px, 6vw, 54px) 14px;
  background: radial-gradient(ellipse at 50% 38%, #FFFFFF 0%, #FFF8F9 52%, #F6ECE5 100%);
  border: 1.5px solid rgba(198,154,78,0.36);
  border-radius: 14px;
  overflow: hidden;
}

.cover-page:before, .cover-page:after {
  content: "";
  position: absolute;
  z-index: -1;
  border: 1px solid rgba(198,154,78,0.28);
  border-radius: 46% 46% 14px 14px;
  width: 78%;
  height: 80%;
  top: 10%;
  left: 11%;
  pointer-events: none;
}

.cover-page:after {
  width: 71%;
  height: 73%;
  top: 13.5%;
  left: 14.5%;
  border-color: rgba(189,107,128,0.22);
  border-style: dashed;
}

.cover-art-svg {
  width: min(58%, 230px);
  height: auto;
  margin: 0 auto 16px;
  filter: drop-shadow(0 8px 16px rgba(115,46,65,0.14));
}

.cover-page .eyebrow {
  margin-bottom: 16px;
  background: rgba(255,255,255,0.85);
}

.cover-page h1 {
  font-family: var(--font-headings);
  font-size: clamp(2.1em, 7.5vw, 3.2em);
  line-height: 1.38;
  letter-spacing: .005em;
  color: var(--accent-rose-dark);
  margin: 0 0 12px;
  text-shadow: 0 2px 0 rgba(255,255,255,0.7);
}

.cover-page .subtitle {
  font-family: var(--font-body);
  font-size: 1.02em;
  line-height: 1.95;
  color: var(--text-secondary);
  max-width: 88%;
  margin: 0 auto 14px;
  text-align: center;
}

.cover-page .author {
  font-family: var(--font-interface);
  font-size: .68em;
  line-height: 1.8;
  color: var(--text-secondary);
  margin: 6px 0 24px;
  max-width: 90%;
}

.primary-btn {
  border: 1px solid rgba(255,255,255,0.3);
  border-radius: 99px;
  background: linear-gradient(135deg, var(--accent-rose), var(--accent-rose-dark));
  color: #fff;
  padding: 11px 28px;
  font-family: var(--font-interface);
  font-size: .7em;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 7px 18px rgba(115,46,65,0.22);
  transition: transform .18s, box-shadow .18s;
}

.primary-btn:active {
  transform: scale(0.97);
}

.ornament {
  color: var(--accent-gold);
  letter-spacing: .32em;
  font-size: .8em;
  margin: 16px 0;
  text-align: center;
}

.quran-box {
  margin: 18px 0;
  padding: 16px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-rose), var(--bg-pearl-gold));
  border: 1px solid var(--accent-gold-soft);
  border-right: 4px solid var(--accent-rose);
  font-family: var(--font-quotes);
  text-align: center;
  box-shadow: 0 4px 14px rgba(92,35,51,0.04);
}

.quran-box p {
  font-family: var(--font-quotes);
  font-size: 1.15em;
  line-height: 2.1;
  color: var(--accent-rose-dark);
  margin: 0 0 6px;
  text-align: center;
}

.quran-box .ayah-source {
  font-family: var(--font-interface);
  font-size: .7em;
  color: var(--accent-gold);
  font-weight: 700;
}

.hadith-box {
  margin: 18px 0;
  padding: 15px 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, var(--bg-soft-sage), var(--bg-secondary));
  border: 1px solid rgba(107,133,110,0.24);
  border-right: 4px solid var(--accent-sage);
  font-family: var(--font-quotes);
  text-align: center;
}

.hadith-box p {
  font-family: var(--font-quotes);
  font-size: 1.1em;
  line-height: 2.05;
  color: var(--accent-sage-dark);
  margin: 0 0 6px;
  text-align: center;
}

.hadith-box .hadith-source {
  font-family: var(--font-interface);
  font-size: .68em;
  color: var(--accent-sage-dark);
  font-weight: 700;
}

.wisdom-pearl {
  margin: 18px 0;
  padding: 15px 18px;
  border-radius: 12px;
  background: var(--bg-pearl-gold);
  border: 1px solid var(--accent-gold-soft);
  border-right: 3px solid var(--accent-gold);
  font-family: var(--font-quotes);
  font-size: .98em;
  line-height: 1.95;
}

.wisdom-badge {
  display: block;
  font-family: var(--font-interface);
  font-size: .63em;
  font-weight: 700;
  color: var(--accent-gold);
  margin-bottom: 6px;
}

.interactive-box {
  margin: 18px 0;
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--bg-soft-rose);
  border: 1.5px solid var(--line);
}

.interactive-box h3 {
  color: var(--accent-rose-dark);
  margin-top: 0;
  margin-bottom: 12px;
}

.field-group {
  margin-bottom: 12px;
}

.field-group label {
  display: block;
  font-family: var(--font-interface);
  font-size: .74em;
  font-weight: 700;
  color: var(--accent-rose-dark);
  margin-bottom: 4px;
}

.field-input, .field-textarea {
  width: 100%;
  padding: 9px 12px;
  border: 1.5px solid var(--line);
  border-radius: 10px;
  background: #FFF;
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: .8em;
  outline: none;
  transition: border-color .18s, box-shadow .18s;
}

.field-textarea {
  min-height: 72px;
  resize: vertical;
  line-height: 1.7;
}

.field-input:focus, .field-textarea:focus {
  border-color: var(--accent-rose);
  box-shadow: 0 0 0 3px rgba(189,107,128,0.16);
}

.opener {
  min-height: clamp(440px, 65vh, 720px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 28px 12px;
  position: relative;
}

.opener:before {
  content: "";
  position: absolute;
  inset: 6% 6%;
  border: 1px solid var(--accent-gold-soft);
  border-radius: 46% 46% 14px 14px;
  pointer-events: none;
}

.chapter-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--accent-gold);
  background: var(--bg-pearl-gold);
  border: 1px solid var(--accent-gold-soft);
  border-radius: 99px;
  padding: 6px 22px;
  font-family: var(--font-chapter-titles);
  font-size: .7em;
  font-weight: 700;
  margin-bottom: 18px;
}

.opener h1 {
  font-family: var(--font-chapter-titles);
  font-size: clamp(1.65em, 5.2vw, 2.3em);
  line-height: 1.65;
  max-width: 90%;
  margin: 0 auto 16px;
  color: var(--accent-rose-dark);
}

.opener .subtitle {
  font-family: var(--font-body);
  font-size: .98em;
  line-height: 1.95;
  color: var(--text-secondary);
  max-width: 86%;
  margin: 0 auto 14px;
  text-align: center;
}

.toc-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}

.toc-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  text-align: start;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 10px 14px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: .74em;
  line-height: 1.6;
  cursor: pointer;
  transition: border-color .18s, transform .15s;
}

.toc-item:active {
  transform: scale(0.99);
}

.toc-item.toc-part {
  font-family: var(--font-chapter-titles);
  background: var(--bg-soft-rose);
  border-color: var(--accent-rose-light);
  font-weight: 700;
  color: var(--accent-rose-dark);
  margin-top: 6px;
}

.toc-item small {
  color: var(--accent-gold);
  font-family: var(--font-numbers);
  font-variant-numeric: tabular-nums;
  font-size: .78em;
  font-weight: 700;
  white-space: nowrap;
  background: var(--bg-secondary);
  padding: 2px 8px;
  border-radius: 99px;
  border: 1px solid var(--accent-gold-soft);
}

.bottom-nav-dock {
  min-height: 66px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 14px calc(env(safe-area-inset-bottom, 8px) + 8px);
  background: var(--bg-secondary);
  border-top: 1px solid var(--line);
  box-shadow: 0 -4px 16px rgba(92,35,51,0.045);
  z-index: 4;
}

.nav-btn {
  min-width: 94px;
  border-radius: 12px;
  background: var(--accent-rose-dark);
  color: #fff;
  border-color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 12px;
  padding: 9px 15px;
}

.nav-btn:disabled {
  opacity: .35;
  cursor: default;
}

.page-counter {
  font-family: var(--font-numbers);
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 99px;
  padding: 6px 14px;
}

.overlay {
  position: fixed;
  inset: 0;
  z-index: 45;
  background: rgba(77,28,42,0.42);
  backdrop-filter: blur(3px);
  display: none;
  align-items: flex-end;
  justify-content: center;
  padding: 12px;
}

#searchPanel.overlay {
  align-items: flex-start;
  padding-top: 64px;
  padding-bottom: 76px;
}

.overlay.open {
  display: flex;
}

.panel {
  width: min(100%, 650px);
  max-height: min(78vh, 680px);
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  background: var(--bg-secondary);
  border: 1px solid var(--paper-edge);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 20px 60px rgba(77,28,42,0.22);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 11px;
  border-bottom: 1px solid var(--line);
}

.panel-head h2 {
  margin: 0;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 16px;
  font-weight: 700;
}

.search-bar-wrap {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

#searchInput {
  width: 100%;
  min-height: 46px;
  padding: 10px 14px 10px 42px;
  border: 1.5px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  font-size: 14px;
  outline: none;
  transition: border-color .18s, box-shadow .18s;
}

#searchInput:focus {
  border-color: var(--accent-rose);
  box-shadow: 0 0 0 3px rgba(189,107,128,0.16);
}

.search-clear-btn {
  position: absolute;
  inset-inline-end: 8px;
  top: 50%;
  transform: translateY(-50%);
  min-width: 30px;
  min-height: 30px;
  border-radius: 8px;
  border: 1px solid var(--line);
  background: var(--bg-secondary);
  color: var(--accent-rose);
  font-family: var(--font-interface);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
}

.search-clear-btn[hidden] {
  display: none;
}

.search-status {
  font-family: var(--font-interface);
  font-size: 12px;
  color: var(--text-secondary);
  margin: 4px 2px 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-result {
  width: 100%;
  display: block;
  text-align: start;
  padding: 12px 14px;
  margin: 8px 0;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-interface);
  cursor: pointer;
  transition: border-color .18s, background .18s;
}

.search-result:hover, .search-result:focus-visible {
  border-color: var(--accent-rose);
  background: var(--bg-soft-rose);
}

.search-result-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 5px;
}

.search-result strong {
  display: block;
  color: var(--accent-rose-dark);
  font-family: var(--font-interface);
  font-size: 13px;
  font-weight: 700;
}

.search-result-page {
  font-family: var(--font-numbers);
  font-variant-numeric: tabular-nums;
  font-size: 11px;
  font-weight: 700;
  color: var(--accent-gold);
  background: var(--bg-secondary);
  border: 1px solid var(--accent-gold-soft);
  border-radius: 99px;
  padding: 2px 9px;
  white-space: nowrap;
}

.search-result small {
  display: block;
  color: var(--text-secondary);
  font-family: var(--font-body);
  font-size: 13.5px;
  line-height: 1.85;
  margin-top: 4px;
}

mark.search-hit-mark {
  background: rgba(189,107,128,0.18);
  color: var(--accent-rose-dark);
  padding: 1px 5px;
  border-radius: 5px;
  font-weight: 700;
}

mark.page-search-highlight {
  background: linear-gradient(135deg, rgba(252,242,245,0.96), rgba(253,247,236,0.96));
  color: var(--accent-rose-dark);
  padding: 1px 6px;
  border-radius: 6px;
  border-bottom: 2px solid var(--accent-rose);
  box-shadow: 0 2px 8px rgba(189,107,128,0.15);
  font-weight: inherit;
}

.empty {
  color: var(--text-secondary);
  text-align: center;
  padding: 18px;
  font-family: var(--font-interface);
  font-size: 14px;
}

.checkbox-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 7px 0;
  font-family: var(--font-interface);
  font-size: .8em;
  color: var(--text-primary);
  cursor: pointer;
}

.checkbox-item input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin-top: 3px;
  accent-color: var(--accent-rose);
  cursor: pointer;
}

.covenant-sign-box {
  margin: 18px 0;
  padding: 16px;
  background: #FFF;
  border: 1.5px dashed var(--accent-gold);
  border-radius: 12px;
  text-align: center;
}

.step-card {
  margin: 12px 0;
  padding: 12px 14px;
  background: var(--bg-primary);
  border: 1px solid var(--line);
  border-radius: 10px;
  display: flex;
  gap: 10px;
}

.step-num {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  border-radius: 50%;
  background: var(--accent-rose);
  color: #fff;
  display: grid;
  place-items: center;
  font-family: var(--font-numbers);
  font-weight: 700;
  font-size: .8em;
}

.step-content {
  flex: 1;
}

@media(min-width: 700px) {
  .book-stage { padding: 22px; }
  .page-canvas { padding: 40px 50px 48px; }
  .top-bar { padding-inline: 24px; }
  .running-header { font-size: 12px; }
}

@media(max-width: 380px) {
  .brand { font-size: 12px; }
  .icon-btn { min-width: 36px; min-height: 36px; padding: 5px 8px; }
  .tools { gap: 4px; }
  .page-canvas { padding: 18px 14px 26px; }
  .page { line-height: 1.95; }
  .nav-btn { min-width: 82px; padding-inline: 10px; }
}

@media(prefers-reduced-motion: reduce) {
  *, *:before, *:after {
    animation-duration: .01ms !important;
    transition-duration: .01ms !important;
    scroll-behavior: auto !important;
  }
}
"""

COVER_SVG = """
<svg class="cover-art-svg" viewBox="0 0 240 240" fill="none" xmlns="http://www.w3.org/2000/svg" aria-label="شعار الغلاف الفني">
  <circle cx="120" cy="120" r="110" fill="#FAF4F0" stroke="#C69A4E" stroke-width="1.8" stroke-dasharray="4 3"/>
  <circle cx="120" cy="120" r="98" fill="#FFFDF9" stroke="#BD6B80" stroke-width="1.5"/>
  <circle cx="120" cy="120" r="88" fill="url(#coverRoseGlow)"/>
  
  <!-- Foliage Vines & Olive Leaves -->
  <path d="M60 140 C 50 110, 70 75, 120 60 C 170 75, 190 110, 180 140" stroke="#6B856E" stroke-width="2.2" stroke-linecap="round"/>
  <path d="M75 125 C 65 120, 62 110, 70 105 C 78 100, 85 110, 75 125 Z" fill="#6B856E"/>
  <path d="M165 125 C 175 120, 178 110, 170 105 C 162 100, 155 110, 165 125 Z" fill="#6B856E"/>
  <path d="M92 90 C 85 82, 86 72, 95 72 C 102 72, 103 82, 92 90 Z" fill="#445946"/>
  <path d="M148 90 C 155 82, 154 72, 145 72 C 138 72, 137 82, 148 90 Z" fill="#445946"/>

  <!-- Golden Sprigs & Filigree -->
  <path d="M80 160 C 90 185, 150 185, 160 160" stroke="#C69A4E" stroke-width="1.8" stroke-linecap="round"/>
  <circle cx="95" cy="176" r="3.5" fill="#C69A4E"/>
  <circle cx="120" cy="182" r="4.5" fill="#C69A4E"/>
  <circle cx="145" cy="176" r="3.5" fill="#C69A4E"/>
  
  <!-- Central Bloom - Dusty Rose -->
  <g transform="translate(120, 124)">
    <path d="M0 -34 C 18 -34, 30 -16, 26 4 C 20 22, -20 22, -26 4 C -30 -16, -18 -34, 0 -34 Z" fill="#FCF2F5" stroke="#BD6B80" stroke-width="1.5"/>
    <path d="M-18 -12 C -24 4, -12 24, 0 28 C 12 24, 24 4, 18 -12 C 12 -24, -12 -24, -18 -12 Z" fill="#F4D8E2"/>
    <path d="M-12 -6 C -16 6, -8 18, 0 20 C 8 18, 16 6, 12 -6 C 8 -14, -8 -14, -12 -6 Z" fill="#E8B8C8"/>
    <circle cx="0" cy="4" r="11" fill="#BD6B80"/>
    <circle cx="0" cy="4" r="6" fill="#732E41"/>
    <polygon points="0,-2 2,2 6,4 2,6 0,10 -2,6 -6,4 -2,2" fill="#FAF6F0"/>
  </g>
  
  <defs>
    <radialGradient id="coverRoseGlow" cx="50%" cy="45%" r="50%">
      <stop offset="0%" stop-color="#FFFFFF"/>
      <stop offset="65%" stop-color="#FCF2F5"/>
      <stop offset="100%" stop-color="#F6ECE5"/>
    </radialGradient>
  </defs>
</svg>
"""
