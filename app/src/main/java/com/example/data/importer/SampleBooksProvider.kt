package com.example.data.importer

import android.content.Context
import com.example.data.model.Book
import com.example.data.sandbox.SandboxHtmlInjector
import java.io.File

object SampleBooksProvider {

    const val DEFAULT_MASTERPIECE_ID = "khayma_fi_alqalb_masterpiece"
    const val LEGACY_ISLAMIC_ID = "islamic_sakina_masterpiece"

    private fun loadKhaymaMasterpieceHtml(context: Context): String {
        return runCatching {
            context.assets.open("khayma_fi_alqalb.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrElse {
            val candidates = listOf(
                File("app/src/main/assets/khayma_fi_alqalb.html"),
                File("src/main/assets/khayma_fi_alqalb.html"),
                File("khayma_fi_alqalb_luxury_edition.html")
            )
            candidates.firstOrNull { it.isFile }?.readText(Charsets.UTF_8)
                ?: error("تعذر العثور على ملف الكتاب الافتراضي khayma_fi_alqalb.html")
        }
    }

    fun generateSampleBooks(context: Context): List<Book> {
        val booksDir = File(context.filesDir, "books").apply { mkdirs() }
        val bookDir = File(booksDir, DEFAULT_MASTERPIECE_ID).apply { mkdirs() }
        val htmlFile = File(bookDir, "index.html")

        val rawHtml = loadKhaymaMasterpieceHtml(context)
        val meta = HtmlBookParser.parseContent(rawHtml, "خَيْمَةٌ فِي القَلْب")
        val injectedHtml = SandboxHtmlInjector.injectIntoHtml(rawHtml, DEFAULT_MASTERPIECE_ID, meta.pageCount)
        htmlFile.writeText(injectedHtml, Charsets.UTF_8)

        val book = Book(
            id = DEFAULT_MASTERPIECE_ID,
            title = meta.title,
            subtitle = meta.subtitle,
            author = meta.author,
            description = meta.description,
            folderPath = DEFAULT_MASTERPIECE_ID,
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
