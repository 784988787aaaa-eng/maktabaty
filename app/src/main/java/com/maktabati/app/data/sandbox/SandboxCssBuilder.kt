package com.maktabati.app.data.sandbox

import com.maktabati.app.data.typography.BookTypographyProfile
import com.maktabati.app.data.typography.MaktabatiFontRegistry

/**
 * Builds the injected `<style id="maktabati-perf-booster">` CSS rules for WebView book sandboxing,
 * layout containment, offline `@font-face` declarations, and micro-slide transitions.
 */
object SandboxCssBuilder {

    fun buildPerfBoosterStyleBlock(
        typographyProfile: BookTypographyProfile? = null
    ): String {
        val fontFacesCss = MaktabatiFontRegistry.buildOfflineFontFacesCss("https://maktabati.app/assets")
        val typographyCss = if (typographyProfile != null) {
            MaktabatiFontRegistry.buildTypographyProfileCss(typographyProfile)
        } else {
            ""
        }

        return """
        <style id="maktabati-perf-booster">
$fontFacesCss
$typographyCss
          :root {
            --reader-font-size: 100%;
            --font-scale: 1;
          }
          html, body {
            width: 100%;
            max-width: 100% !important;
            height: 100%;
            min-height: 100vh;
            margin: 0;
            padding: 0;
            overflow-x: hidden !important;
            overscroll-behavior-x: none !important;
            touch-action: pan-y !important;
            box-sizing: border-box !important;
            -webkit-tap-highlight-color: transparent;
            -webkit-text-size-adjust: 100%;
            text-size-adjust: 100%;
          }
          .app, #bookStage, .book-stage, .stage, .page-canvas, .page, .book-page, .page-container, .panel, .overlay {
            touch-action: pan-y !important;
            overscroll-behavior-x: none !important;
          }
          .app {
            width: 100%;
            max-width: 100% !important;
            height: 100vh;
            min-height: 100vh;
            max-height: 100vh;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
          }
          .top-bar, .running-header, .bottom-nav-dock, .controls-bar, .page-footer {
            flex-shrink: 0;
            max-width: 100% !important;
            overflow-x: hidden !important;
            touch-action: manipulation !important;
            -webkit-text-size-adjust: none !important;
            text-size-adjust: none !important;
          }
          .book-stage, .stage {
            width: 100%;
            max-width: 100% !important;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
            -webkit-overflow-scrolling: touch;
          }
          .page-canvas, .page, .book-page, .page-container {
            max-width: 100% !important;
            overflow-x: hidden !important;
            box-sizing: border-box !important;
            position: relative;
            transform-origin: center center;
          }
          .maktabati-gpu-hint {
            will-change: transform, opacity;
            transform: translateZ(0);
          }
          @keyframes maktabatiMicroSlideNext {
            0% {
              opacity: 0.5;
              transform: translateX(-10px);
            }
            100% {
              opacity: 1;
              transform: translateX(0);
            }
          }
          @keyframes maktabatiMicroSlidePrev {
            0% {
              opacity: 0.5;
              transform: translateX(10px);
            }
            100% {
              opacity: 1;
              transform: translateX(0);
            }
          }
          .maktabati-transition-next {
            animation: maktabatiMicroSlideNext 180ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
          }
          .maktabati-transition-prev {
            animation: maktabatiMicroSlidePrev 180ms cubic-bezier(0.22, 1, 0.36, 1) forwards;
          }
          #pageInnerContent, .page-body, .page-content {
            max-width: 100% !important;
            overflow-x: hidden !important;
            overflow-y: auto;
            -webkit-overflow-scrolling: touch;
            box-sizing: border-box !important;
            overflow-wrap: anywhere;
            word-break: break-word;
            font-size: calc(1em * var(--font-scale, 1));
          }
          #pageInnerContent *, .page-body *, .page-content *, .page-canvas * {
            max-width: 100%;
            box-sizing: border-box;
            overflow-wrap: anywhere;
          }
          #pageInnerContent img, .page-body img, .page-content img,
          #pageInnerContent svg, .page-body svg, .page-content svg,
          #pageInnerContent video, .page-body video, .page-content video {
            max-width: 100% !important;
            height: auto !important;
          }
          #pageInnerContent table, .page-body table, .page-content table {
            display: block;
            max-width: 100% !important;
            overflow-x: auto;
            box-sizing: border-box !important;
          }
          .quote-box, blockquote, .insight-card, .flip-card, .quiz-card, .quiz-option {
            max-width: 100% !important;
            box-sizing: border-box !important;
            overflow-wrap: anywhere !important;
          }
          .overlay {
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            z-index: 40;
          }
          .bottom-nav-dock, .controls-bar, .page-footer {
            flex-shrink: 0;
            z-index: 4;
          }
        </style>
        """.trimIndent()
    }
}
