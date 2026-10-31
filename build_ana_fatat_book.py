#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Builder script for the masterpiece book:
🌸 «أنا فتاة ولي رسالة»
رحلة إلى أعماق الذات... من معرفة النفس إلى بناء الشخصية وصناعة الأثر.
"""

import os
import re
import json
import zipfile

def build_book():
    print("Building «أنا فتاة ولي رسالة»...")
    # Read the fonts list to include in @font-face
    fonts_css = """
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
    return fonts_css

if __name__ == "__main__":
    build_book()
