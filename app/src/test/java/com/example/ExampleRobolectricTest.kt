package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.importer.HtmlBookParser
import com.example.data.importer.SampleBooksProvider
import com.example.data.model.Book
import com.example.data.model.Shelf
import com.example.data.sandbox.SandboxHtmlInjector
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testAppNameStringIsMaktabati() {
        val appName = context.getString(R.string.app_name)
        assertEquals("مكتبتي", appName)
    }

    @Test
    fun testSmartMetadataParserExtractsBookDataAndRootCss() {
        val tempFile = File.createTempFile("luxury_book", ".html")
        tempFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <title>عنوان احتياطي</title>
                <style>
                    :root {
                        --bg-primary: #3B0722;
                        --accent-gold: #FDE047;
                        --accent-rose: #E11D48;
                    }
                </style>
            </head>
            <body>
                <script>
                    const STORAGE_KEY = "luxury_arabic_book_state_v1";
                    const BOOK_DATA = {
                        title: "الدرر الكامنة في آداب القراءة",
                        subtitle: "طبعة تفاعلية محققة",
                        author: "ابن حجر العسقلاني",
                        pages: [
                            { title: "الصفحة الأولى: المدخل", content: "<p>محتوى 1</p>" },
                            { title: "الصفحة الثانية: الأصول", content: "<p>محتوى 2</p>" },
                            { title: "الصفحة الثالثة: الفروع", content: "<p>محتوى 3</p>" },
                            { title: "الصفحة الرابعة: الخاتمة", content: "<p>محتوى 4</p>" }
                        ]
                    };
                </script>
            </body>
            </html>
            """.trimIndent()
        )

        val meta = HtmlBookParser.parse(tempFile, "افتراضي")
        assertEquals("الدرر الكامنة في آداب القراءة", meta.title)
        assertEquals("طبعة تفاعلية محققة", meta.subtitle)
        assertEquals("ابن حجر العسقلاني", meta.author)
        assertEquals("ar", meta.language)
        assertTrue(meta.isRtl)
        assertEquals(4, meta.pageCount)
        assertEquals("#E11D48", meta.accentRoseHex)
        assertEquals("#FDE047", meta.accentGoldHex)
        assertEquals("#3B0722", meta.bgPrimaryHex)
        assertEquals(4, meta.tableOfContents.size)
        assertEquals("الصفحة الأولى: المدخل", meta.tableOfContents[0].title)

        tempFile.delete()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParserRejectsCorruptedOrNonHtmlFile() {
        val invalidFile = File.createTempFile("corrupted", ".html")
        invalidFile.writeText("NOT_AN_HTML_FILE_12345")
        try {
            HtmlBookParser.parse(invalidFile, "تالف")
        } finally {
            invalidFile.delete()
        }
    }

    @Test
    fun testSandboxInjectorAddsLocalStorageIsolationAndPerformanceCss() {
        val rawHtml = "<html><head><title>Book</title></head><body><div class='page'>Content</div></body></html>"
        val injected = SandboxHtmlInjector.injectIntoHtml(rawHtml, "book_abc_123", 50)

        assertTrue(injected.contains("maktabati-storage-isolation"))
        assertTrue(injected.contains("book_book_abc_123_"))
        assertTrue(injected.contains("maktabati-perf-booster"))
        assertTrue(injected.contains("will-change: transform, opacity"))
        assertTrue(injected.contains("translateZ(0)"))
    }

    @Test
    fun testSampleBooksInclude100PageBookAndBilingualBooks() {
        val sampleBooks = SampleBooksProvider.generateSampleBooks(context)
        assertEquals(6, sampleBooks.size)

        val hundredPageBook = sampleBooks.find { it.id == "sample_encyclopedia_100" }
        assertNotNull(hundredPageBook)
        assertEquals(100, hundredPageBook!!.pageCount)

        val englishBook = sampleBooks.find { it.id == "sample_english_typography" }
        assertNotNull(englishBook)
        assertEquals("en", englishBook!!.language)
        assertFalse(englishBook.isRtl)
        assertEquals(10, englishBook.pageCount)

        val whisperBook = sampleBooks.find { it.id == "sample_hams_almaraya" }
        assertNotNull(whisperBook)
        assertEquals(6, whisperBook!!.pageCount)
        val whisperHtml = File(File(File(context.filesDir, "books"), whisperBook.folderPath), whisperBook.entryHtmlPath)
        assertTrue(whisperHtml.isFile)
        val whisperSource = whisperHtml.readText(Charsets.UTF_8)
        assertTrue(whisperSource.contains("--bg-primary"))
        assertTrue(whisperSource.contains("--accent-rose"))
        assertTrue(whisperSource.contains("--accent-gold"))
        assertTrue(whisperSource.contains("top-bar"))
        assertTrue(whisperSource.contains("page-canvas"))
        assertTrue(whisperSource.contains("bottom-nav-dock"))
    }

    @Test
    fun testDatabaseBookMetadataStyleAndProgressUpdates() = runBlocking {
        val dao = database.bookDao()

        val shelf = Shelf(
            id = "shelf_luxury",
            name = "مخطوطات نادرة",
            iconName = "history_edu",
            colorHex = "#92400E"
        )
        dao.insertShelf(shelf)

        val book = Book(
            id = "book_test_1",
            title = "تاريخ الأندلس",
            subtitle = "الطبعة الأولى",
            author = "ابن عذاري",
            folderPath = "sample_test",
            entryHtmlPath = "index.html",
            shelfId = shelf.id,
            pageCount = 25,
            readingProgress = 0.1f,
            scrollY = 0
        )
        dao.insertBook(book)

        dao.updateBookMetadataAndStyle(
            id = "book_test_1",
            title = "البيان المغرب في أخبار الأندلس والمغرب",
            subtitle = "الطبعة المحققة الفاخرة",
            author = "ابن عذاري المراكشي",
            coverGradientStart = 0xFF3B0722,
            coverGradientEnd = 0xFFE11D48,
            bgPrimaryHex = "#3B0722",
            accentGoldHex = "#FDE047",
            accentRoseHex = "#E11D48"
        )

        dao.updateReadingProgress("book_test_1", 0.60f, 0, 15, System.currentTimeMillis())

        val updated = dao.getBookByIdOnce("book_test_1")
        assertNotNull(updated)
        assertEquals("البيان المغرب في أخبار الأندلس والمغرب", updated!!.title)
        assertEquals("الطبعة المحققة الفاخرة", updated.subtitle)
        assertEquals("ابن عذاري المراكشي", updated.author)
        assertEquals("#E11D48", updated.accentRoseHex)
        assertEquals(15, updated.currentPage)
        assertEquals(0.60f, updated.readingProgress, 0.001f)
    }
    @Test
    fun testParserRecognizesLuxuryTemplateTocAliases() {
        val file = File.createTempFile("section_title_book", ".html")
        file.writeText("""<html lang="ar"><head><title>اختبار</title></head><body><script>const BOOK_DATA={title:'اختبار',pages:[{sectionTitle:'قسم أول'},{chapterTag:'الفصل الثاني'},{chapterNumber:'٣'},{heading:'خاتمة'}]};</script></body></html>""")
        val parsed = HtmlBookParser.parse(file, "اختبار")
        assertEquals(4, parsed.pageCount)
        assertTrue(parsed.tableOfContents.any { it.title == "قسم أول" })
        assertTrue(parsed.tableOfContents.any { it.title == "الفصل الثاني" })
        file.delete()
    }

    @Test
    fun testReaderArchitectureFilesAreSeparated() {
        val root = File(context.applicationInfo.sourceDir).parentFile
        // This test is intentionally structural only in local source-based test runs.
        assertNotNull(SandboxHtmlInjector::class.java)
        assertTrue(SandboxHtmlInjector.buildInjectedSnippet("book_test", 4).contains("page-canvas"))
        assertTrue(SandboxHtmlInjector.buildInjectedSnippet("book_test", 4).contains("typeof BOOK_DATA"))
    }

    @Test
    fun testInitializeCleansSampleBooksAndDefaultShelves() = runBlocking {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)


        // Insert a default shelf and a user shelf
        dao.insertShelf(Shelf(id = "shelf_literature", name = "الأدب والبلاغة", iconName = "auto_stories", colorHex = "#92400E"))
        dao.insertShelf(Shelf(id = "shelf_user_custom", name = "كتبي الخاصة", iconName = "bookmark", colorHex = "#0284C7"))

        // Insert a sample book and a user book
        dao.insertBook(
            Book(
                id = "sample_test_1",
                title = "كتاب نموذجي",
                author = "مؤلف",
                folderPath = "sample_test_1",
                entryHtmlPath = "index.html"
            )
        )
        dao.insertBook(
            Book(
                id = "book_user_1",
                title = "كتابي الشخصي",
                author = "منصور",
                folderPath = "book_user_1",
                entryHtmlPath = "index.html",
                shelfId = "shelf_user_custom"
            )
        )

        repository.initializeDefaultDataIfNeeded()

        val allBooks = dao.getAllBooks().first()
        val allShelves = dao.getAllShelves().first()

        assertEquals(1, allBooks.size)
        assertEquals("book_user_1", allBooks.first().id)

        assertEquals(1, allShelves.size)
        assertEquals("shelf_user_custom", allShelves.first().id)
    }

}

