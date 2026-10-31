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
    fun testSampleBooksReturnsEmptyListWithNoDefaultBooks() {
        val sampleBooks = SampleBooksProvider.generateSampleBooks(context)
        assertTrue(sampleBooks.isEmpty())
        assertTrue(SampleBooksProvider.isDefaultOrSampleBookId("sample_book_1"))
        assertTrue(SampleBooksProvider.isDefaultOrSampleBookId("islamic_sakina_masterpiece"))
        assertTrue(SampleBooksProvider.isDefaultOrSampleBookId("khayma_fi_alqalb_masterpiece"))
        assertFalse(SampleBooksProvider.isDefaultOrSampleBookId("user_book_123"))
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
    fun testInitializeCleansAllDefaultBooksAndDefaultShelves() = runBlocking {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)

        // Insert a default shelf and a user shelf
        dao.insertShelf(Shelf(id = "shelf_literature", name = "الأدب والبلاغة", iconName = "auto_stories", colorHex = "#92400E"))
        dao.insertShelf(Shelf(id = "shelf_user_custom", name = "كتبي الخاصة", iconName = "bookmark", colorHex = "#0284C7"))

        // Insert legacy sample/default books and a user book
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
                id = "khayma_fi_alqalb_masterpiece",
                title = "خيمة في القلب",
                author = "مؤلف",
                folderPath = "khayma_fi_alqalb_masterpiece",
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
        assertFalse(allBooks.any { it.id.startsWith("sample_") })
        assertFalse(allBooks.any { it.id == "islamic_sakina_masterpiece" })
        assertFalse(allBooks.any { it.id == "khayma_fi_alqalb_masterpiece" })

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

    @Test
    fun testPrepareSharedBookFileUsesRealBookTitle() {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)

        val bookDir = File(File(context.filesDir, "books"), "book_share_test").apply { mkdirs() }
        val entryFile = File(bookDir, "index.html")
        entryFile.writeText("<!DOCTYPE html><html><head><title>خيمة في القلب</title></head><body>محتوى</body></html>")

        val book = Book(
            id = "book_share_test",
            title = "خيمة في القلب: رحلة إيمانية",
            author = "م/منصور قطينه",
            folderPath = "book_share_test",
            entryHtmlPath = "index.html"
        )

        val sharedFile = repository.prepareSharedBookFile(book)
        assertTrue(sharedFile.exists())
        assertEquals("خيمة في القلب_ رحلة إيمانية.html", sharedFile.name)
        assertTrue(sharedFile.readText().contains("خيمة في القلب"))

        sharedFile.delete()
        bookDir.deleteRecursively()
    }

    @Test
    fun testSingleRichMediaBookZipImportPreservesCompanionImagesAndAudioAndSharesAsZip(): Unit = runBlocking {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)

        val zipFile = File(context.cacheDir, "كتاب_السيرة_المصور.zip")
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("index.html"))
            zos.write(
                """
                <!DOCTYPE html>
                <html lang="ar" dir="rtl">
                <head><meta charset="utf-8"><title>السيرة النبوية المصورة</title></head>
                <body>
                    <div class="page">
                        <img src="images/pic.png" />
                        <audio src="audio/clip.mp3"></audio>
                    </div>
                </body>
                </html>
                """.trimIndent().toByteArray(Charsets.UTF_8)
            )
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("images/pic.png"))
            zos.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("audio/clip.mp3"))
            zos.write("FAKE_MP3_AUDIO_CONTENT".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val inspection = com.example.data.importer.BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection is com.example.data.importer.ZipInspectionResult.SingleBook)
        val importedBook = (inspection as com.example.data.importer.ZipInspectionResult.SingleBook).book

        assertEquals("السيرة النبوية المصورة", importedBook.title)
        assertTrue(importedBook.fileCount > 1)
        assertEquals(3, importedBook.fileCount)

        val bookDir = repository.getBookDirectory(importedBook)
        assertTrue(File(bookDir, "index.html").exists())
        assertTrue(File(bookDir, "images/pic.png").exists())
        assertTrue(File(bookDir, "audio/clip.mp3").exists())

        // Verify sharing a rich-media book (fileCount > 1) packages the entire directory into a .zip
        val sharedZip = repository.prepareSharedBookFile(importedBook)
        assertTrue(sharedZip.exists())
        assertEquals("السيرة النبوية المصورة.zip", sharedZip.name)

        val sharedEntries = mutableSetOf<String>()
        java.util.zip.ZipInputStream(java.io.FileInputStream(sharedZip)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                sharedEntries.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        assertTrue(sharedEntries.contains("index.html"))
        assertTrue(sharedEntries.contains("images/pic.png"))
        assertTrue(sharedEntries.contains("audio/clip.mp3"))

        sharedZip.delete()
        bookDir.deleteRecursively()
        zipFile.delete()
        Unit
    }

    @Test
    fun testMultiBookArchiveZipDetectionAndNewShelfOrStandaloneImport(): Unit = runBlocking {
        val dao = database.bookDao()
        val repository = com.example.data.repository.BookRepository(dao, context)

        val zipFile = File(context.cacheDir, "سلسلة القصص القرآني.zip")
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(zipFile)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("book1.html"))
            zos.write("""<!DOCTYPE html><html lang="ar"><head><title>قصة أصحاب الكهف</title></head><body><p>الكتاب الأول</p></body></html>""".toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("book2.html"))
            zos.write("""<!DOCTYPE html><html lang="ar"><head><title>قصة ذي القرنين</title></head><body><p>الكتاب الثاني</p></body></html>""".toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(java.util.zip.ZipEntry("book3.html"))
            zos.write("""<!DOCTYPE html><html lang="ar"><head><title>قصة سفينة نوح</title></head><body><p>الكتاب الثالث</p></body></html>""".toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        // 1. Test Multi-Book Archive Detection + Adding into a New Shelf named after the ZIP
        val inspection1 = com.example.data.importer.BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection1 is com.example.data.importer.ZipInspectionResult.MultiBookArchive)
        val proposal1 = (inspection1 as com.example.data.importer.ZipInspectionResult.MultiBookArchive).proposal

        assertEquals("سلسلة القصص القرآني", proposal1.zipBaseName)
        assertEquals(3, proposal1.archiveBooksCount)

        val (createdShelf, shelfBooks) = repository.commitMultiBookProposalToNewShelf(proposal1)
        assertEquals("سلسلة القصص القرآني", createdShelf.name)
        assertEquals(3, shelfBooks.size)
        assertTrue(shelfBooks.all { it.shelfId == createdShelf.id })

        val dbShelfBooks = dao.getBooksByShelf(createdShelf.id).first()
        assertEquals(3, dbShelfBooks.size)

        // Clean up first batch
        for (b in shelfBooks) {
            repository.deleteBook(b.id)
        }
        repository.deleteShelf(createdShelf)

        // 2. Test Multi-Book Archive Detection + Adding as Standalone Books (shelfId = null)
        val inspection2 = com.example.data.importer.BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection2 is com.example.data.importer.ZipInspectionResult.MultiBookArchive)
        val proposal2 = (inspection2 as com.example.data.importer.ZipInspectionResult.MultiBookArchive).proposal

        val standaloneBooks = repository.commitMultiBookProposalStandalone(proposal2)
        assertEquals(3, standaloneBooks.size)
        assertTrue(standaloneBooks.all { it.shelfId == null })

        // Clean up second batch
        for (b in standaloneBooks) {
            repository.deleteBook(b.id)
        }
        zipFile.delete()
        Unit
    }

    @Test
    fun testSilentFluidReflowZoomAndSubtleSmoothPageTransitionWithoutDomHudOrCurlArtifacts() {
        val rawHtml = """<!DOCTYPE html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><title>كتاب تفاعلي</title></head><body><main class="page-canvas" id="pageCanvas"><div class="page-body">نص متدفق</div></main></body></html>"""
        val injected = SandboxHtmlInjector.injectIntoHtml(rawHtml, "book_fluid_v7", 40)

        // 1. Verify Silent Fluid Reflow Zoom Engine & Silent Layout Containment
        assertTrue(injected.contains("__maktabatiV7Initialized"))
        assertTrue(injected.contains("--reader-font"))
        assertTrue(injected.contains("--reader-font-size"))
        assertTrue(injected.contains("__maktabatiSetFluidZoom"))
        assertTrue(injected.contains("overflow-x: hidden !important"))
        assertTrue(injected.contains("max-width: 100% !important"))
        assertTrue(injected.contains("MaktabatiLayoutGuard"))
        assertTrue(injected.contains("inspectAndHeal"))
        assertTrue(injected.contains("el.scrollWidth > canvasWidth + 8"))

        // 2. Verify NO DOM HUD badges, NO warning toasts, and NO page-curl overlays are created in HTML
        assertFalse(injected.contains("#maktabati-zoom-badge"))
        assertFalse(injected.contains("showZoomBadge"))
        assertFalse(injected.contains("#maktabati-distortion-toast"))
        assertFalse(injected.contains("showDistortionToast"))
        assertFalse(injected.contains("onLayoutDistortionWarning"))
        assertFalse(injected.contains("#maktabati-page-curl-overlay"))
        assertFalse(injected.contains("MaktabatiPageCurlEngine"))

        // 3. Verify Subtle Smooth Fade & Micro-Slide (180ms cubic-bezier(0.22, 1, 0.36, 1), ±10px)
        assertTrue(injected.contains("maktabatiMicroSlideNext"))
        assertTrue(injected.contains("maktabatiMicroSlidePrev"))
        assertTrue(injected.contains("180ms cubic-bezier(0.22, 1, 0.36, 1)"))
        assertTrue(injected.contains("translateX(-10px)"))
        assertTrue(injected.contains("translateX(10px)"))
        assertTrue(injected.contains("animateSubtlePageTransition"))
    }

    @Test
    fun testExactPageCountIgnoresHyphenatedSubclassesAndBridgeReports100PercentOnLastPage() {
        val htmlWithSubclasses = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head><meta charset="utf-8"><title>كتاب من 6 صفحات</title></head>
            <body>
                <main class="page-canvas">
                    <article class="page active" data-page="0"><h1 class="page-heading">غلاف</h1><span class="page-flower">*</span><span class="page-number">1</span></article>
                    <article class="page" data-page="1"><h2 class="page-heading">فصل 1</h2><span class="page-flower">*</span><span class="page-number">2</span></article>
                    <article class="page" data-page="2"><h2 class="page-heading">فصل 2</h2><span class="page-flower">*</span><span class="page-number">3</span></article>
                    <article class="page" data-page="3"><h2 class="page-heading">فصل 3</h2><span class="page-flower">*</span><span class="page-number">4</span></article>
                    <article class="page" data-page="4"><h2 class="page-heading">فصل 4</h2><span class="page-flower">*</span><span class="page-number">5</span></article>
                    <article class="page" data-page="5"><h2 class="page-heading">خاتمة</h2><span class="page-flower">*</span><span class="page-number">6</span></article>
                </main>
                <footer class="page-footer"><span class="page-counter" id="pageCount">1 / 6</span></footer>
            </body>
            </html>
        """.trimIndent()

        val parsed = HtmlBookParser.parseContent(htmlWithSubclasses, "كتاب من 6 صفحات")
        assertEquals("Must count exactly 6 pages and ignore .page-heading, .page-flower, .page-number, .page-canvas, .page-counter", 6, parsed.pageCount)

        var reportedPage = 0
        var reportedTotal = 0
        var reportedProgress = 0f
        val bridge = com.example.ui.reader.MaktabatiJsBridge(
            totalBookPages = 6,
            onPageProgressChanged = { page, total, progress ->
                reportedPage = page
                reportedTotal = total
                reportedProgress = progress
            }
        )
        bridge.onPageChanged(6, 6)
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertEquals(6, reportedPage)
        assertEquals(6, reportedTotal)
        assertEquals(1.0f, reportedProgress, 0.0001f)
    }

    @Test
    fun testOfflineArabicFontsInjectedInSandboxHtmlInjector() {
        val rawHtml = """<!DOCTYPE html><html lang="ar" dir="rtl"><head><meta charset="utf-8"><title>اختبار الخطوط</title></head><body><div class="page">متن عربي فاخر</div></body></html>"""
        val injected = SandboxHtmlInjector.injectIntoHtml(rawHtml, "book_fonts_v8", 12)

        assertTrue(injected.contains("__maktabatiV8Initialized"))
        assertTrue(injected.contains("@font-face"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/amiri/amiri-400.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/aref-ruqaa/aref-ruqaa-700.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/reem-kufi/reem-kufi-700.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-400.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/noto-sans-arabic/noto-sans-arabic-400.woff2"))
        assertTrue(injected.contains("https://maktabati.app/assets/fonts/noto-kufi-arabic/noto-kufi-arabic-700.woff2"))
        assertTrue(injected.contains("font-family: \"Amiri\""))
        assertTrue(injected.contains("font-family: \"Noto Naskh Arabic\""))
        assertTrue(injected.contains("font-family: \"Aref Ruqaa\""))
        assertTrue(injected.contains("font-family: \"Reem Kufi\""))
        assertTrue(injected.contains("font-family: \"IBM Plex Sans Arabic\""))
        assertTrue(injected.contains("font-family: \"Noto Sans Arabic\""))
        assertTrue(injected.contains("font-family: \"Noto Kufi Arabic\""))

        // Verify raw font resources exist and can be opened
        context.resources.openRawResource(R.font.amiri).use { stream ->
            assertTrue("R.font.amiri must be non-empty", stream.available() > 1000)
        }
        context.resources.openRawResource(R.font.noto_naskh_arabic).use { stream ->
            assertTrue("R.font.noto_naskh_arabic must be non-empty", stream.available() > 1000)
        }
    }

    @Test
    fun testBookProductionMasterGuideExistsAndContainsAllPrompts() {
        val rootGuide = File("../BOOK_PRODUCTION_MASTER_GUIDE.md").let {
            if (it.exists()) it else File("BOOK_PRODUCTION_MASTER_GUIDE.md")
        }
        assertTrue("BOOK_PRODUCTION_MASTER_GUIDE.md must exist in project root", rootGuide.exists())
        val content = rootGuide.readText(Charsets.UTF_8)

        assertTrue(content.contains("MASTER SYSTEM PROMPT"))
        assertTrue(content.contains("Fluid Reflow Zoom"))
        assertTrue(content.contains("4-Corner Page-Curl"))
        assertTrue(content.contains("--reader-font"))
        assertTrue(content.contains("The 3-Zone Locked Page Architecture"))
        assertTrue(content.contains("https://maktabati.app/fonts/amiri.ttf"))
        assertTrue(content.contains("https://maktabati.app/fonts/cairo.ttf"))
        assertTrue(content.contains("chapter-opener"))
        assertTrue(content.contains("wisdom-pearl"))
        assertTrue(content.contains("data-reader-name"))
        assertTrue(content.contains("الشباب"))
        assertTrue(content.contains("الفتيات"))
        assertTrue(content.contains("النساء"))
        assertTrue(content.contains("العازبات"))
        assertTrue(content.contains("المتزوجات"))
        assertTrue(content.contains("الرجال"))
        assertTrue(content.contains("الكتب الإسلامية"))
        assertTrue(content.contains("المراهقون"))
        assertTrue(content.contains("الكتب التعليمية"))
        assertTrue(content.contains("بناء الذات"))
    }

    @Test
    fun testUnlimitedPageCountParsingForTwoThousandPageBook() {
        val pagesBuilder = StringBuilder()
        val totalPages = 2000
        for (i in 1..totalPages) {
            if (i > 1) pagesBuilder.append(",")
            pagesBuilder.append("""{title:"صفحة رقم $i",content:"<p>محتوى الصفحة $i</p>"}""")
        }
        val hugeBookHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head><meta charset="utf-8"><title>موسوعة الألفي صفحة</title></head>
            <body>
                <script>
                    const BOOK_DATA = {
                        title: "موسوعة الألفي صفحة",
                        author: "م/ منصور قطينه",
                        pages: [$pagesBuilder]
                    };
                </script>
            </body>
            </html>
        """.trimIndent()

        val parsed = HtmlBookParser.parseContent(hugeBookHtml, "موسوعة")
        assertEquals(2000, parsed.pageCount)
        assertEquals(2000, parsed.tableOfContents.size)
        assertEquals("صفحة رقم 2000", parsed.tableOfContents.last().title)
    }

    @Test
    fun testTruePaletteExtractorLightAndAlternativeCssVariables() {
        // 1. Light/Creamy book using --paper-bg, --primary, --gold
        val creamyBookHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="utf-8">
                <title>كتاب الصباح الكريمي</title>
                <style>
                    :root {
                        --paper-bg: #FAF2EE;
                        --primary: #7C3F4E;
                        --gold: #C69249;
                    }
                </style>
            </head>
            <body>
                <div class="cover-page">
                    <h1 class="cover-title">كتاب الصباح الكريمي</h1>
                </div>
            </body>
            </html>
        """.trimIndent()

        val creamyMeta = HtmlBookParser.parseContent(creamyBookHtml, "افتراضي")
        assertEquals("#FAF2EE", creamyMeta.bgPrimaryHex)
        assertEquals("#7C3F4E", creamyMeta.accentRoseHex)
        assertEquals("#C69249", creamyMeta.accentGoldHex)
        assertTrue("Book with .cover-page must detect hasCoverPage = true", creamyMeta.hasCoverPage)
        assertTrue(
            "Light cover gradient end must stay light and never be darkened to navy",
            HtmlBookParser.computeColorLuminance(creamyMeta.coverGradientEnd) > 0.48f
        )

        // 2. Alternative CSS variables (--bg, --brand, --ornament)
        val altVarsHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <style>
                    :root {
                        --bg: #FDF8F5;
                        --brand: #3E5C47;
                        --ornament: #B8860B;
                    }
                </style>
            </head>
            <body>
                <article><p>محتوى بدون غلاف مخصص</p></article>
            </body>
            </html>
        """.trimIndent()

        val altMeta = HtmlBookParser.parseContent(altVarsHtml, "كتاب بديل")
        assertEquals("#FDF8F5", altMeta.bgPrimaryHex)
        assertEquals("#3E5C47", altMeta.accentRoseHex)
        assertEquals("#B8860B", altMeta.accentGoldHex)
        assertTrue("Must not force crimson #E11D48", altMeta.accentRoseHex != "#E11D48")
        assertTrue("Plain article without cover classes/types must detect hasCoverPage = false", !altMeta.hasCoverPage)

        // 3. Fallback to body background-color, h1 color, and SVG stroke/fill
        val bodyAndSvgHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <style>
                    body { background-color: #FBF7F0; }
                    h1 { color: #2C4A3E; }
                </style>
            </head>
            <body>
                <div data-page="0">
                    <svg viewBox="0 0 100 100"><path stroke="#D4AF37" fill="none" d="M10 10 H 90"/></svg>
                    <h1>عنوان بالغلاف</h1>
                </div>
            </body>
            </html>
        """.trimIndent()

        val svgMeta = HtmlBookParser.parseContent(bodyAndSvgHtml, "كتاب SVG")
        assertEquals("#FBF7F0", svgMeta.bgPrimaryHex)
        assertEquals("#2C4A3E", svgMeta.accentRoseHex)
        assertEquals("#D4AF37", svgMeta.accentGoldHex)
        assertTrue("data-page=0 must detect hasCoverPage = true", svgMeta.hasCoverPage)
    }

    @Test
    fun testCoverSnapshotGeneratorCreatesSnapshotOnlyWhenCoverExists() = kotlinx.coroutines.runBlocking {
        val tempRoot = File(context.cacheDir, "test_cover_snapshots").apply {
            deleteRecursively()
            mkdirs()
        }

        // Case 1: Book with a designed cover page -> generates cover_snapshot.png
        val bookWithCoverDir = File(tempRoot, "book_with_cover").apply { mkdirs() }
        val coverHtmlFile = File(bookWithCoverDir, "index.html")
        coverHtmlFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <title>خيمة في القلب</title>
                <style>:root { --paper-bg: #FAF2EE; --primary: #6D2E46; --gold: #C59B27; }</style>
            </head>
            <body>
                <div class="cover-page"><h1 class="cover-title">خيمة في القلب</h1></div>
            </body>
            </html>
            """.trimIndent(),
            Charsets.UTF_8
        )

        val generated = com.example.data.importer.HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
            context = context,
            bookDir = bookWithCoverDir,
            entryHtmlFile = coverHtmlFile,
            fallbackTitle = "خيمة في القلب"
        )
        assertTrue("Should generate cover_snapshot.png for book with cover page", generated)
        assertTrue(File(bookWithCoverDir, "cover_snapshot.png").exists())

        // Case 2: Book without any cover page or cover image -> does NOT generate cover_snapshot.png
        val bookWithoutCoverDir = File(tempRoot, "book_no_cover").apply { mkdirs() }
        val noCoverHtmlFile = File(bookWithoutCoverDir, "index.html")
        noCoverHtmlFile.writeText(
            """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head><title>مذكرة نصية</title></head>
            <body><p>فقرة نصية عادية بدون صفحة غلاف.</p></body>
            </html>
            """.trimIndent(),
            Charsets.UTF_8
        )

        val generatedNoCover = com.example.data.importer.HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
            context = context,
            bookDir = bookWithoutCoverDir,
            entryHtmlFile = noCoverHtmlFile,
            fallbackTitle = "مذكرة نصية"
        )
        assertTrue("Should NOT generate cover_snapshot.png when book has no cover page", !generatedNoCover)
        assertTrue(!File(bookWithoutCoverDir, "cover_snapshot.png").exists())

        tempRoot.deleteRecursively()
        Unit
    }

    @Test
    fun testMaktabatiTypographyRegistryAndOfflineWoff2Assets() {
        val families = com.example.data.typography.MaktabatiFontRegistry.builtInFonts
        assertEquals(25, families.size)

        val expectedNames = listOf(
            "Amiri",
            "Amiri Quran",
            "Scheherazade New",
            "Lateef",
            "Harmattan",
            "Noto Naskh Arabic",
            "Markazi Text",
            "IBM Plex Sans Arabic",
            "Readex Pro",
            "Almarai",
            "Tajawal",
            "Cairo",
            "Noto Sans Arabic",
            "Aref Ruqaa",
            "Marhey",
            "Lemonada",
            "Mirza",
            "Katibeh",
            "Vibes",
            "Reem Kufi",
            "El Messiri",
            "Noto Kufi Arabic",
            "Changa",
            "Rakkas",
            "Lalezar"
        )
        for (name in expectedNames) {
            assertNotNull("Family $name must be registered", com.example.data.typography.MaktabatiFontRegistry.findFont(name))
        }
        assertEquals(null, com.example.data.typography.MaktabatiFontRegistry.findFont("Unknown External Font"))
        assertEquals(null, com.example.data.typography.MaktabatiFontRegistry.findFont("https://fonts.googleapis.com/css"))

        // Verify all WOFF2 files and OFL.txt licenses exist in app assets with valid wOF2 magic bytes
        for (family in families) {
            if (family.licenseAssetPath.isNotBlank()) {
                val licenseBytes = context.assets.open(family.licenseAssetPath).use { it.readBytes() }
                val licenseText = String(licenseBytes, Charsets.UTF_8)
                assertTrue("OFL.txt for ${family.familyName} must contain SIL OPEN FONT LICENSE", licenseText.contains("SIL OPEN FONT LICENSE"))
            }

            for (weightAsset in family.weights) {
                val assetPath = weightAsset.woff2AssetPath
                val fontBytes = context.assets.open(assetPath).use { it.readBytes() }
                assertTrue("WOFF2 asset $assetPath for weight ${weightAsset.weight} must be > 10KB", fontBytes.size > 10_000)
                val magic = String(fontBytes.sliceArray(0..3), Charsets.US_ASCII)
                assertEquals("WOFF2 asset $assetPath must start with wOF2 header", "wOF2", magic)
            }
        }

        // Verify manifest.json parsing & validation
        val rootManifest = File("../manifest.json").let { if (it.exists()) it else File("manifest.json") }
        assertTrue("Root manifest.json must exist", rootManifest.exists())
        val profile = com.example.data.typography.MaktabatiFontRegistry.parseManifestTypography(rootManifest)
        assertNotNull(profile)
        assertEquals("Noto Naskh Arabic", profile!!.body)
        assertEquals("Aref Ruqaa", profile.headings)
        assertEquals("Reem Kufi", profile.chapterTitles)
        assertEquals("Amiri", profile.quotes)
        assertEquals("Amiri Quran", profile.quran)
        assertEquals("Noto Sans Arabic", profile.interfaceFont)
        assertEquals("Noto Sans Arabic", profile.numbers)

        // Verify fallback when manifest specifies an unapproved font
        val fallbackProfile = com.example.data.typography.MaktabatiFontRegistry.parseManifestTypographyJson(
            """{"typography":{"body":"NonExistentFont","headings":"Aref Ruqaa"}}"""
        )
        assertNotNull(fallbackProfile)
        assertEquals("Noto Naskh Arabic", fallbackProfile!!.body)
        assertEquals("Aref Ruqaa", fallbackProfile.headings)

        // Verify WebView offline font interception for WOFF2 and TTF (including Quranic font)
        val woff2Res = com.example.data.typography.MaktabatiFontRegistry.interceptFontRequest(
            context,
            "/assets/fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2"
        )
        assertNotNull(woff2Res)
        assertEquals("font/woff2", woff2Res!!.mimeType)

        val quranTtfRes = com.example.data.typography.MaktabatiFontRegistry.interceptFontRequest(
            context,
            "/fonts/amiri_quran.ttf"
        )
        assertNotNull(quranTtfRes)
        assertEquals("font/ttf", quranTtfRes!!.mimeType)
    }

    @Test
    fun testKhaymaFiAlQalbReferenceHtmlAndZipAudit() = runBlocking {
        val htmlFile = File("../خَيْمَةٌ فِي القَلْب.html").let { if (it.exists()) it else File("خَيْمَةٌ فِي القَلْب.html") }
        val zipFile = File("../خَيْمَةٌ_فِي_القَلْب.zip").let { if (it.exists()) it else File("خَيْمَةٌ_فِي_القَلْب.zip") }

        assertTrue("Reference HTML must exist", htmlFile.exists())

        val html = htmlFile.readText(Charsets.UTF_8)

        // 1. Verify zero green/bluish-green colors
        val forbiddenGreens = listOf("24,63,58", "36,92,85", "16,35,31", "#047857", "#B8C1BA", "#b8c1ba")
        for (green in forbiddenGreens) {
            assertFalse("HTML must not contain green color $green", html.contains(green))
        }

        // 2. Verify no Aa button in topbar
        assertFalse("HTML must not contain Aa button", html.contains(">Aa<"))

        // 3. Verify rebuilt search & typography variables
        assertTrue(html.contains("normalizeArabicWithMap"))
        assertTrue(html.contains("function executeBookSearch()"))
        assertTrue(html.contains("applyCalmPageSearchHighlight"))
        assertTrue(html.contains("searchClearBtn"))
        assertTrue(html.contains("searchStatus"))
        assertTrue(html.contains("--font-body:\"Noto Naskh Arabic\""))
        assertTrue(html.contains("--font-headings:\"Aref Ruqaa\""))
        assertTrue(html.contains("--font-chapter-titles:\"Reem Kufi\""))
        assertTrue(html.contains("--font-quotes:\"Amiri\""))
        assertTrue(html.contains("--font-interface:\"Noto Sans Arabic\""))
        assertTrue(html.contains("--font-numbers:\"Noto Sans Arabic\""))

        // 4. Verify 105 pages and pink/rose-gold palette
        val parsed = HtmlBookParser.parse(htmlFile, "خيمة في القلب")
        assertEquals(105, parsed.pageCount)
        assertEquals("#FFF1F6", parsed.bgPrimaryHex)
        assertEquals("#B94F7A", parsed.accentRoseHex)
        assertEquals("#B88945", parsed.accentGoldHex)

        // 5. Verify ZIP package if present (optional per user preference)
        if (zipFile.exists()) {
            val inspection = com.example.data.importer.BookImporter.inspectZipFile(context, zipFile)
            assertTrue(inspection is com.example.data.importer.ZipInspectionResult.SingleBook)
            val importedBook = (inspection as com.example.data.importer.ZipInspectionResult.SingleBook).book
            assertEquals(105, importedBook.pageCount)

            val dao = database.bookDao()
            val repository = com.example.data.repository.BookRepository(dao, context)
            val bookDir = repository.getBookDirectory(importedBook)
            assertTrue(File(bookDir, "manifest.json").exists())
            bookDir.deleteRecursively()
        }
        Unit
    }

    @Test
    fun testViewportAntiImageZoomLockAndUserTypographyPreferencesStudio() {
        // 1. Verify viewport anti-image-zoom lock on any book HTML
        val zoomableBookHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=5,user-scalable=yes">
                <style>html,body,.app{touch-action:pan-y pinch-zoom}</style>
            </head>
            <body><div class="page"><p>نص القراءة</p></div></body>
            </html>
        """.trimIndent()

        val injected = SandboxHtmlInjector.injectIntoHtml(zoomableBookHtml, "book_no_image_zoom", 10)
        assertTrue(injected.contains("__maktabatiV9Initialized"))
        assertTrue(injected.contains("user-scalable=no"))
        assertTrue(injected.contains("maximum-scale=1"))
        assertFalse(injected.contains("user-scalable=yes"))
        assertFalse(injected.contains("maximum-scale=5"))
        assertFalse(injected.contains("touch-action:pan-y pinch-zoom"))
        assertTrue(injected.contains("touch-action: pan-y !important"))
        assertTrue(injected.contains("e.preventDefault()"))

        // 2. Verify UserTypographyPreferences persistence & live override JS
        val prefs = context.getSharedPreferences("test_typo_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val defaultPrefs = com.example.data.typography.MaktabatiFontRegistry.loadUserTypographyPreferences(prefs)
        assertFalse(defaultPrefs.useCustomTypography)
        assertEquals("Noto Naskh Arabic", defaultPrefs.profile.body)

        val customPrefs = com.example.data.typography.UserTypographyPreferences(
            useCustomTypography = true,
            presetId = "classical_diwan",
            profile = com.example.data.typography.BookTypographyProfile(
                body = "Amiri",
                headings = "Aref Ruqaa",
                chapterTitles = "Reem Kufi",
                quotes = "Amiri",
                interfaceFont = "IBM Plex Sans Arabic",
                numbers = "Noto Sans Arabic"
            )
        )
        com.example.data.typography.MaktabatiFontRegistry.saveUserTypographyPreferences(prefs, customPrefs)

        val loadedPrefs = com.example.data.typography.MaktabatiFontRegistry.loadUserTypographyPreferences(prefs)
        assertTrue(loadedPrefs.useCustomTypography)
        assertEquals("classical_diwan", loadedPrefs.presetId)
        assertEquals("Amiri", loadedPrefs.profile.body)
        assertEquals("Aref Ruqaa", loadedPrefs.profile.headings)
        assertEquals("Reem Kufi", loadedPrefs.profile.chapterTitles)
        assertEquals("IBM Plex Sans Arabic", loadedPrefs.profile.interfaceFont)

        val liveJs = com.example.data.typography.MaktabatiFontRegistry.buildLiveTypographyOverrideJs(loadedPrefs)
        assertTrue(liveJs.contains("maktabati-user-typography-override"))
        assertTrue(liveJs.contains("--font-body: \"Amiri\""))
        assertTrue(liveJs.contains("!important"))
    }

    @Test
    fun testAnaFatatWaLiRisalahBookMasterpieceAudit() {
        val bookFile = File("/أَنَا فَتَاةٌ وَلِي رِسَالَة.html")
        assertTrue("Book HTML file must exist", bookFile.exists())
        assertTrue("Book HTML file must be substantial", bookFile.length() > 50000)

        val meta = HtmlBookParser.parse(bookFile, "افتراضي")
        assertTrue(meta.title.contains("أَنَا فَتَاةٌ وَلِي رِسَالَة"))
        assertEquals(32, meta.pageCount)
        assertTrue(meta.isRtl)
        assertEquals("#BD6B80", meta.accentRoseHex)
        assertEquals("#C69A4E", meta.accentGoldHex)
        assertEquals("#FAF6F0", meta.bgPrimaryHex)

        val html = bookFile.readText(Charsets.UTF_8)
        // Search IDs & Logic
        assertTrue(html.contains("id=\"searchOpen\""))
        assertTrue(html.contains("id=\"searchPanel\""))
        assertTrue(html.contains("id=\"searchInput\""))
        assertTrue(html.contains("id=\"searchClearBtn\""))
        assertTrue(html.contains("id=\"searchStatus\""))
        assertTrue(html.contains("id=\"searchResults\""))
        assertTrue(html.contains("normalizeArabicWithMap"))
        assertTrue(html.contains("executeBookSearch"))
        assertTrue(html.contains("applyCalmPageSearchHighlight"))

        // Navigation & Fluid Reflow Zoom
        assertTrue(html.contains("id=\"btnPrevPage\""))
        assertTrue(html.contains("id=\"btnNextPage\""))
        assertTrue(html.contains("id=\"pageCounter\""))
        assertTrue(html.contains("__maktabatiSetFluidZoom"))
        assertTrue(html.contains("touch-action: pan-y !important"))

        // Thoroughly cleaned text: No developer messages, preambles, or pre-print notes
        assertFalse(html.contains("هذه النسخة الكاملة للكتيب"))
        assertFalse(html.contains("ملاحظة قبل الطباعة"))
        assertFalse(html.contains("صغتها بحيث تكون الفتاة قارئة"))

        // Verify ZIP package if present (optional per user request)
        val zipFile = File("/أَنَا_فَتَاةٌ_وَلِي_رِسَالَة.zip").let { if (it.exists()) it else File("أَنَا_فَتَاةٌ_وَلِي_رِسَالَة.zip") }
        if (zipFile.exists()) {
            val zip = java.util.zip.ZipFile(zipFile)
            val entries = zip.entries().asSequence().map { it.name }.toSet()
            assertTrue(entries.contains("أَنَا فَتَاةٌ وَلِي رِسَالَة.html"))
            zip.close()
        }
    }

    @Test
    fun testMenAlQalbBookMasterpieceAudit() {
        val bookFile = File("/من القلب.html")
        assertTrue("من القلب.html file must exist", bookFile.exists())
        assertTrue("من القلب.html file must be substantial", bookFile.length() > 50000)

        val meta = HtmlBookParser.parse(bookFile, "افتراضي")
        assertTrue(meta.title.contains("مِنَ القَلْب"))
        assertEquals(32, meta.pageCount)
        assertTrue(meta.isRtl)
        assertEquals("#BD6B80", meta.accentRoseHex)
        assertEquals("#C69A4E", meta.accentGoldHex)
        assertEquals("#FAF6F0", meta.bgPrimaryHex)

        val html = bookFile.readText(Charsets.UTF_8)
        // Search Elements & Functions
        assertTrue(html.contains("id=\"searchOpen\""))
        assertTrue(html.contains("id=\"searchPanel\""))
        assertTrue(html.contains("id=\"searchInput\""))
        assertTrue(html.contains("id=\"searchClearBtn\""))
        assertTrue(html.contains("id=\"searchStatus\""))
        assertTrue(html.contains("id=\"searchResults\""))
        assertTrue(html.contains("normalizeArabicWithMap"))
        assertTrue(html.contains("executeBookSearch"))
        assertTrue(html.contains("applyCalmPageSearchHighlight"))

        // Navigation & Reflow Zoom
        assertTrue(html.contains("id=\"btnPrevPage\""))
        assertTrue(html.contains("id=\"btnNextPage\""))
        assertTrue(html.contains("id=\"pageCounter\""))
        assertTrue(html.contains("__maktabatiSetFluidZoom"))
        assertTrue(html.contains("touch-action: pan-y !important"))

        // Verify Unified books directory structure
        val booksDir = listOf(
            File("/books"),
            File("../books"),
            File("books"),
            File(System.getProperty("user.dir", ".")).parentFile?.resolve("books") ?: File("books")
        ).firstOrNull { it.exists() && it.isDirectory } ?: File("/books")
        assertTrue("books directory must exist", booksDir.exists() && booksDir.isDirectory)
        assertTrue(File(booksDir, "خيمة_في_القلب/خَيْمَةٌ فِي القَلْب.html").exists())
        assertTrue(File(booksDir, "أنا_فتاة_ولي_رسالة/أَنَا فَتَاةٌ وَلِي رِسَالَة.html").exists())
        assertTrue(File(booksDir, "من_القلب/من القلب.html").exists())
        assertTrue(File(booksDir, "index.html").exists())

        // Verify that NO .zip files exist in the books directory (pure HTML format is sufficient)
        val zipsInBooks = booksDir.walkTopDown().filter { it.extension.lowercase() == "zip" }.toList()
        assertEquals("No ZIP archives should exist in the books directory", 0, zipsInBooks.size)
    }
}


