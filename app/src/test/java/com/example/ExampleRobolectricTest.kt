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
        assertTrue(injected.contains("healBlankPageIfNeeded"))
        assertTrue(injected.contains("triggerUniversalPageTurn"))
    }

    @Test
    fun testSampleBooksGeneratesSingleKhaymaMasterpieceBook() {
        val sampleBooks = SampleBooksProvider.generateSampleBooks(context)
        assertEquals(1, sampleBooks.size)

        val khaymaBook = sampleBooks.first()
        assertEquals("khayma_fi_alqalb_masterpiece", khaymaBook.id)
        assertEquals("خَيْمَةٌ فِي القَلْب", khaymaBook.title)
        assertEquals("رحلةُ فتاةٍ من ركامِ الصراخ إلى سكينةِ البناء", khaymaBook.subtitle)
        assertEquals(105, khaymaBook.pageCount)
        assertEquals("ar", khaymaBook.language)
        assertTrue(khaymaBook.isRtl)
        assertEquals("#9E3B5A", khaymaBook.accentRoseHex)
        assertEquals("#C49148", khaymaBook.accentGoldHex)

        val khaymaHtml = File(File(File(context.filesDir, "books"), khaymaBook.folderPath), khaymaBook.entryHtmlPath)
        assertTrue(khaymaHtml.isFile)
        val khaymaSource = khaymaHtml.readText(Charsets.UTF_8)
        assertTrue(khaymaSource.contains("--bg-primary"))
        assertTrue(khaymaSource.contains("--accent-rose"))
        assertTrue(khaymaSource.contains("--accent-gold"))
        assertTrue(khaymaSource.contains("top-bar"))
        assertTrue(khaymaSource.contains("page-canvas"))
        assertTrue(khaymaSource.contains("bottom-nav-dock"))
        assertTrue(khaymaSource.contains("pageCanvas"))
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
        assertNotNull(SandboxHtmlInjector::class.java)
        assertTrue(SandboxHtmlInjector.buildInjectedSnippet("book_test", 4).contains("page-canvas"))
        assertTrue(SandboxHtmlInjector.buildInjectedSnippet("book_test", 4).contains("typeof BOOK_DATA"))
    }

    @Test
    fun testInitializeCleansSampleBooksAndDefaultShelvesAndInsertsKhaymaMasterpiece() = runBlocking {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)

        // Insert a default shelf and a user shelf
        dao.insertShelf(Shelf(id = "shelf_literature", name = "الأدب والبلاغة", iconName = "auto_stories", colorHex = "#92400E"))
        dao.insertShelf(Shelf(id = "shelf_user_custom", name = "كتبي الخاصة", iconName = "bookmark", colorHex = "#0284C7"))

        // Insert legacy sample books and a user book
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
                id = "islamic_sakina_masterpiece",
                title = "كتاب قديم",
                author = "مؤلف",
                folderPath = "islamic_sakina_masterpiece",
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

        assertEquals(2, allBooks.size)
        assertTrue(allBooks.any { it.id == "book_user_1" })
        assertTrue(allBooks.any { it.id == "khayma_fi_alqalb_masterpiece" })
        assertFalse(allBooks.any { it.id.startsWith("sample_") })
        assertFalse(allBooks.any { it.id == "islamic_sakina_masterpiece" })

        assertEquals(1, allShelves.size)
        assertEquals("shelf_user_custom", allShelves.first().id)
    }

    @Test
    fun testInjectorPreservesCharsetAtTopAndNormalizesDvhUnits() {
        val rawHtml = """<!DOCTYPE html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>.app{height:100dvh;max-height:100dvh}.cover-page{min-height:72dvh}.panel{max-height:85dvh}</style></head><body><div class="app"></div></body></html>"""
        val injected = SandboxHtmlInjector.injectIntoHtml(rawHtml, "book_fast_test", 105)

        // <meta charset="utf-8"> must remain within the first 200 characters (never pushed behind injected script)
        val charsetIndex = injected.indexOf("<meta charset=\"utf-8\">")
        val scriptIndex = injected.indexOf("maktabati-storage-isolation")
        assertTrue(charsetIndex in 0..200)
        assertTrue(scriptIndex > charsetIndex)

        // All dvh viewport units in <head> must be normalized to universal vh units
        assertFalse(injected.contains("100dvh"))
        assertFalse(injected.contains("72dvh"))
        assertFalse(injected.contains("85dvh"))
        assertTrue(injected.contains("100vh"))
        assertTrue(injected.contains("72vh"))
        assertTrue(injected.contains("85vh"))
    }

}



