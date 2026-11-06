package com.maktabati.app

import com.maktabati.app.data.converter.SmartBookConverter
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.converter.theme.BookVisualTheme
import com.maktabati.app.data.sandbox.SandboxHtmlInjector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testWhatsAppAndTelegramClipboardTextConversionAndCleanOutput() {
        val whatsappRaw = """
            تمت إعادة التوجيه
            [10:30 م، 2026/11/05] أحمد: *الإستغفار مفتاح نجاة*
            💫🤔💫
            *....
            *أثر الاستغفار في القلوب*
            إن *الكلمة الطيبة* والاستغفار يفتحان الأقفال، ويورثان _السكينة والطمأنينة_ في الصدور.
            ══════════
            *همسة للقلب:* أكثروا من الاستغفار في الأسحار وفي كل حين.
        """.trimIndent()

        val proposal = SmartBookConverter.inspectRawText(whatsappRaw)
        assertEquals("الإستغفار مفتاح نجاة", proposal.detectedTitle)
        assertEquals("", proposal.detectedSubtitle)
        assertFalse(proposal.previewExcerpt.contains("*"))
        assertFalse(proposal.previewExcerpt.contains("💫"))

        val pages = SmartBookConverter.buildProposalPages(
            proposal = proposal,
            theme = BookVisualTheme.ISLAMIC_EMERALD
        )
        val combinedPagesHtml = pages.joinToString("\n") { it.html }
        assertTrue(combinedPagesHtml.contains("<h2>أثر الاستغفار في القلوب</h2>"))
        assertTrue(combinedPagesHtml.contains("<strong>الكلمة الطيبة</strong>"))
        assertTrue(combinedPagesHtml.contains("<em>السكينة والطمأنينة</em>"))
        assertTrue(combinedPagesHtml.contains("<div class=\"ornament\" aria-hidden=\"true\">✦ ✦ ✦</div>"))
        assertTrue(combinedPagesHtml.contains("heart-keepsake-card"))
        assertTrue(combinedPagesHtml.contains("همسة للقلب"))

        val html = SmartBookConverter.convertProposalToLuxuryHtml(
            proposal = proposal,
            theme = BookVisualTheme.ISLAMIC_EMERALD
        )

        // Verify clean title, subheading, inline bold/italic, callout, and ornamental divider in complete HTML
        assertTrue(html.contains("<title>الإستغفار مفتاح نجاة</title>"))
        assertFalse(html.contains("*الإستغفار مفتاح نجاة*"))
        assertFalse(html.contains("*...."))
        assertFalse(html.contains("💫🤔💫"))
        assertFalse(html.contains("تمت إعادة التوجيه"))
        assertFalse(html.contains("[10:30"))
        assertTrue(html.contains("<h2>أثر الاستغفار في القلوب<\\/h2>"))
        assertTrue(html.contains("<strong>الكلمة الطيبة<\\/strong>"))
        assertTrue(html.contains("<em>السكينة والطمأنينة<\\/em>"))
        assertTrue(html.contains("heart-keepsake-card"))
        assertTrue(html.contains("همسة للقلب"))
    }

    @Test
    fun testNoDuplicateFontFacesWhenInjectedHeadSnippetProvided() {
        val proposal = SmartBookConverter.inspectRawText("عنوان الكتاب\nفقرة أولى للتجربة والتحقق من عدم تكرار الخطوط.")
        val pages = SmartBookConverter.buildProposalPages(proposal)
        val injectedSnippet = SandboxHtmlInjector.buildInjectedSnippet(
            bookId = "book_font_dedup_test",
            pageCount = pages.size,
            typographyProfile = null
        )

        val renderedWithoutInjection = LuxuryHtmlTemplateRenderer.renderCompleteLuxuryHtml(
            bookTitle = proposal.detectedTitle,
            bookSubtitle = proposal.detectedSubtitle,
            bookAuthor = proposal.detectedAuthor,
            theme = proposal.recommendedTheme,
            isRtl = true,
            language = "ar",
            pages = pages,
            injectedHeadSnippet = ""
        )

        val renderedWithInjection = LuxuryHtmlTemplateRenderer.renderCompleteLuxuryHtml(
            bookTitle = proposal.detectedTitle,
            bookSubtitle = proposal.detectedSubtitle,
            bookAuthor = proposal.detectedAuthor,
            theme = proposal.recommendedTheme,
            isRtl = true,
            language = "ar",
            pages = pages,
            injectedHeadSnippet = injectedSnippet
        )

        val countWithoutInjection = Regex("""font-family:\s*"Amiri Quran"""").findAll(renderedWithoutInjection).count()
        val countWithInjection = Regex("""font-family:\s*"Amiri Quran"""").findAll(renderedWithInjection).count()
        assertEquals(
            "Injecting SandboxHtmlInjector snippet must not duplicate the @font-face declarations",
            countWithoutInjection,
            countWithInjection
        )
        assertEquals("الإستغفار مفتاح نجاة", ArabicTextNormalizer.stripFormattingAndNoiseFromHeading("*الإستغفار مفتاح نجاة* 💫🤔💫"))
        assertEquals("", ArabicTextNormalizer.stripFormattingAndNoiseFromHeading("*...."))
    }
}

