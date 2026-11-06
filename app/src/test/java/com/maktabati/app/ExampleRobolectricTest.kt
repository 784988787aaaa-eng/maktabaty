package com.maktabati.app

import android.content.Context
import androidx.compose.ui.unit.LayoutDirection
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.maktabati.app.data.converter.extractor.DocumentTextExtractor
import com.maktabati.app.data.converter.normalizer.ArabicTextNormalizer
import com.maktabati.app.data.converter.paginator.SemanticBookPaginator
import com.maktabati.app.data.converter.renderer.LuxuryHtmlTemplateRenderer
import com.maktabati.app.data.db.AppDatabase
import com.maktabati.app.data.importer.BookImporter
import com.maktabati.app.data.importer.BookTemplateCategory
import com.maktabati.app.data.importer.BookVisualTheme
import com.maktabati.app.data.importer.HtmlBookParser
import com.maktabati.app.data.importer.HtmlCoverSnapshotGenerator
import com.maktabati.app.data.importer.SampleBooksProvider
import com.maktabati.app.data.importer.SmartBookConverter
import com.maktabati.app.data.importer.ZipInspectionResult
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.data.repository.BookRepository
import com.maktabati.app.data.sandbox.SandboxCssBuilder
import com.maktabati.app.data.sandbox.SandboxHtmlInjector
import com.maktabati.app.data.sandbox.SandboxJsRuntimeBuilder
import com.maktabati.app.data.typography.BookTypographyProfile
import com.maktabati.app.data.typography.BuiltInArabicFontsCatalog
import com.maktabati.app.data.typography.CustomFontStorageManager
import com.maktabati.app.data.typography.MaktabatiFontRegistry
import com.maktabati.app.data.typography.UserTypographyPreferences
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.ArabicStrings
import com.maktabati.app.ui.localization.EnglishStrings
import com.maktabati.app.ui.localization.stringsForLanguage
import com.maktabati.app.ui.reader.MaktabatiJsBridge
import com.maktabati.app.ui.viewmodel.BookImportQueueManager
import com.maktabati.app.ui.viewmodel.CloudSyncUiDelegate
import com.maktabati.app.ui.viewmodel.LibraryViewModel
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
        val repository = BookRepository(dao, context)

        dao.insertShelf(Shelf(id = "shelf_literature", name = "الأدب والبلاغة", iconName = "auto_stories", colorHex = "#92400E"))
        dao.insertShelf(Shelf(id = "shelf_user_custom", name = "كتبي الخاصة", iconName = "bookmark", colorHex = "#0284C7"))

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

        val charsetIndex = injected.indexOf("<meta charset=\"utf-8\">")
        val scriptIndex = injected.indexOf("maktabati-storage-isolation")
        assertTrue(charsetIndex in 0..200)
        assertTrue(scriptIndex > charsetIndex)

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
        val repository = BookRepository(dao, context)

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
        val repository = BookRepository(dao, context)

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

        val inspection = BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection is ZipInspectionResult.SingleBook)
        val importedBook = (inspection as ZipInspectionResult.SingleBook).book

        assertEquals("السيرة النبوية المصورة", importedBook.title)
        assertTrue(importedBook.fileCount > 1)
        assertEquals(3, importedBook.fileCount)

        val bookDir = repository.getBookDirectory(importedBook)
        assertTrue(File(bookDir, "index.html").exists())
        assertTrue(File(bookDir, "images/pic.png").exists())
        assertTrue(File(bookDir, "audio/clip.mp3").exists())

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
        val repository = BookRepository(dao, context)

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

        val inspection1 = BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection1 is ZipInspectionResult.MultiBookArchive)
        val proposal1 = (inspection1 as ZipInspectionResult.MultiBookArchive).proposal

        assertEquals("سلسلة القصص القرآني", proposal1.zipBaseName)
        assertEquals(3, proposal1.archiveBooksCount)

        val (createdShelf, shelfBooks) = repository.commitMultiBookProposalToNewShelf(proposal1)
        assertEquals("سلسلة القصص القرآني", createdShelf.name)
        assertEquals(3, shelfBooks.size)
        assertTrue(shelfBooks.all { it.shelfId == createdShelf.id })

        val dbShelfBooks = dao.getBooksByShelf(createdShelf.id).first()
        assertEquals(3, dbShelfBooks.size)

        for (b in shelfBooks) {
            repository.deleteBook(b.id)
        }
        repository.deleteShelf(createdShelf)

        val inspection2 = BookImporter.inspectZipFile(context, zipFile)
        assertTrue(inspection2 is ZipInspectionResult.MultiBookArchive)
        val proposal2 = (inspection2 as ZipInspectionResult.MultiBookArchive).proposal

        val standaloneBooks = repository.commitMultiBookProposalStandalone(proposal2)
        assertEquals(3, standaloneBooks.size)
        assertTrue(standaloneBooks.all { it.shelfId == null })

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

        assertTrue(injected.contains("__maktabatiV7Initialized"))
        assertTrue(injected.contains("--reader-font"))
        assertTrue(injected.contains("--reader-font-size"))
        assertTrue(injected.contains("__maktabatiSetFluidZoom"))
        assertTrue(injected.contains("overflow-x: hidden !important"))
        assertTrue(injected.contains("max-width: 100% !important"))
        assertTrue(injected.contains("MaktabatiLayoutGuard"))
        assertTrue(injected.contains("inspectAndHeal"))
        assertTrue(injected.contains("el.scrollWidth > canvasWidth + 8"))

        assertFalse(injected.contains("#maktabati-zoom-badge"))
        assertFalse(injected.contains("showZoomBadge"))
        assertFalse(injected.contains("#maktabati-distortion-toast"))
        assertFalse(injected.contains("showDistortionToast"))
        assertFalse(injected.contains("onLayoutDistortionWarning"))
        assertFalse(injected.contains("#maktabati-page-curl-overlay"))
        assertFalse(injected.contains("MaktabatiPageCurlEngine"))

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
        val bridge = MaktabatiJsBridge(
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

        context.resources.openRawResource(R.font.amiri).use { stream ->
            assertTrue("R.font.amiri must be non-empty", stream.available() > 1000)
        }
        context.resources.openRawResource(R.font.noto_naskh_arabic).use { stream ->
            assertTrue("R.font.noto_naskh_arabic must be non-empty", stream.available() > 1000)
        }
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

        val legacyCover = File(bookWithCoverDir, "cover_snapshot.png").apply {
            writeText("legacy_zoomed_snapshot")
        }
        assertTrue(legacyCover.exists())

        val generated = HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
            context = context,
            bookDir = bookWithCoverDir,
            entryHtmlFile = coverHtmlFile,
            fallbackTitle = "خيمة في القلب"
        )
        assertTrue("Should generate cover_v3.png for book with cover page", generated)
        assertTrue(File(bookWithCoverDir, "cover_v3.png").exists())
        assertFalse("Legacy zoomed cover_snapshot.png must be cleaned up automatically", legacyCover.exists())

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

        val generatedNoCover = HtmlCoverSnapshotGenerator.ensureCoverSnapshotForBook(
            context = context,
            bookDir = bookWithoutCoverDir,
            entryHtmlFile = noCoverHtmlFile,
            fallbackTitle = "مذكرة نصية"
        )
        assertTrue("Should NOT generate cover_v3.png when book has no cover page", !generatedNoCover)
        assertTrue(!File(bookWithoutCoverDir, "cover_v3.png").exists())

        tempRoot.deleteRecursively()
        Unit
    }

    @Test
    fun testMaktabatiTypographyRegistryAndOfflineWoff2Assets() {
        val families = MaktabatiFontRegistry.builtInFonts
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
            assertNotNull("Family $name must be registered", MaktabatiFontRegistry.findFont(name))
        }
        assertEquals(null, MaktabatiFontRegistry.findFont("Unknown External Font"))
        assertEquals(null, MaktabatiFontRegistry.findFont("https://fonts.googleapis.com/css"))

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

        val profile = MaktabatiFontRegistry.parseManifestTypographyJson(
            """{"typography":{"body":"Noto Naskh Arabic","headings":"Aref Ruqaa","chapterTitles":"Reem Kufi","quotes":"Amiri","quran":"Amiri Quran","interface":"Noto Sans Arabic","numbers":"Noto Sans Arabic"}}"""
        )
        assertNotNull(profile)
        assertEquals("Noto Naskh Arabic", profile!!.body)
        assertEquals("Aref Ruqaa", profile.headings)
        assertEquals("Reem Kufi", profile.chapterTitles)
        assertEquals("Amiri", profile.quotes)
        assertEquals("Amiri Quran", profile.quran)
        assertEquals("Noto Sans Arabic", profile.interfaceFont)
        assertEquals("Noto Sans Arabic", profile.numbers)

        val fallbackProfile = MaktabatiFontRegistry.parseManifestTypographyJson(
            """{"typography":{"body":"NonExistentFont","headings":"Aref Ruqaa"}}"""
        )
        assertNotNull(fallbackProfile)
        assertEquals("Noto Naskh Arabic", fallbackProfile!!.body)
        assertEquals("Aref Ruqaa", fallbackProfile.headings)

        val woff2Res = MaktabatiFontRegistry.interceptFontRequest(
            context,
            "/assets/fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2"
        )
        assertNotNull(woff2Res)
        assertEquals("font/woff2", woff2Res!!.mimeType)

        val quranTtfRes = MaktabatiFontRegistry.interceptFontRequest(
            context,
            "/fonts/amiri_quran.ttf"
        )
        assertNotNull(quranTtfRes)
        assertEquals("font/ttf", quranTtfRes!!.mimeType)
    }

    @Test
    fun testViewportAntiImageZoomLockAndUserTypographyPreferencesStudio() {
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

        val prefs = context.getSharedPreferences("test_typo_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val defaultPrefs = MaktabatiFontRegistry.loadUserTypographyPreferences(prefs)
        assertFalse(defaultPrefs.useCustomTypography)
        assertEquals("Noto Naskh Arabic", defaultPrefs.profile.body)
        assertEquals("", defaultPrefs.customPreviewText)

        prefs.edit().putString(
            "typo_custom_preview_text",
            UserTypographyPreferences.LEGACY_PREFILLED_PREVIEW_TEXT
        ).commit()
        val sanitizedPrefs = MaktabatiFontRegistry.loadUserTypographyPreferences(prefs)
        assertEquals("", sanitizedPrefs.customPreviewText)

        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_BODY.contains("الحِكْمَةِ"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_QURAN.contains("﴿"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_HADITH_QUOTES.contains("ﷺ"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_TOC_INTERFACE.contains("الفهرس"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_NUMBERS.contains("٠١٢٣٤٥٦٧٨٩"))

        assertTrue(injected.contains("--font-body:"))
        assertTrue(injected.contains("--font-quran:"))
        assertTrue(injected.contains("--font-quotes:"))
        assertTrue(injected.contains("--font-numbers:"))
        assertTrue(injected.contains("__maktabatiUpgradeWeakBookTypography"))

        val customPrefs = UserTypographyPreferences(
            useCustomTypography = true,
            presetId = "classical_diwan",
            profile = BookTypographyProfile(
                body = "Amiri",
                headings = "Aref Ruqaa",
                chapterTitles = "Reem Kufi",
                quotes = "Amiri",
                interfaceFont = "IBM Plex Sans Arabic",
                numbers = "Noto Sans Arabic"
            ),
            customPreviewText = "نص خاص يكتبه القارئ للمعاينة"
        )
        MaktabatiFontRegistry.saveUserTypographyPreferences(prefs, customPrefs)

        val loadedPrefs = MaktabatiFontRegistry.loadUserTypographyPreferences(prefs)
        assertTrue(loadedPrefs.useCustomTypography)
        assertEquals("classical_diwan", loadedPrefs.presetId)
        assertEquals("Amiri", loadedPrefs.profile.body)
        assertEquals("Aref Ruqaa", loadedPrefs.profile.headings)
        assertEquals("Reem Kufi", loadedPrefs.profile.chapterTitles)
        assertEquals("IBM Plex Sans Arabic", loadedPrefs.profile.interfaceFont)
        assertEquals("نص خاص يكتبه القارئ للمعاينة", loadedPrefs.customPreviewText)

        val liveJs = MaktabatiFontRegistry.buildLiveTypographyOverrideJs(loadedPrefs)
        assertTrue(liveJs.contains("maktabati-user-typography-override"))
        assertTrue(liveJs.contains("--font-body: \"Amiri\""))
        assertTrue(liveJs.contains("!important"))
    }

    @Test
    fun testSmartBookConverterArabicVocalizedAndEnglishTextToLuxuryHtml() {
        val arabicRawText = """
            نُورُ السَّكِينَةِ وَإِشْرَاقُ الْفُؤَادِ
            تأليف: د. عَبْدُ الرَّحْمَنِ الأَمِينُ

            مقدمة الكتاب
            إِنَّ الْقَلْبَ إِذَا أَقْبَلَ عَلَى اللَّهِ بِصِدْقٍ وَإِخْلَاصٍ، أَشْرَقَتْ فِيهِ أَنْوَارُ السَّكِينَةِ وَالطُّمَأْنِينَةِ، وَتَبَدَّدَتْ ظُلُمَاتُ الْحَيْرَةِ وَالشَّتَاتِ. وَالمُؤْمِنُ يَسْتَضِيءُ بِآيَاتِ الوَحْيِ فِي كُلِّ شَأْنٍ مِنْ شُؤُونِهِ.

            قال تعالى: ﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾

            الفصل الأول: بِنَاءُ الرُّوحِ وَتَزْكِيَةُ النَّفْسِ
            قَالَ رَسُولُ اللَّهِ ﷺ: « إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى »

            همسة للقلب: لَا تَسْتَوْحِشْ طَرِيقَ الْحَقِّ لِقِلَّةِ السَّالِكِينَ، وَلَا تَغْتَرَّ بِكَثْرَةِ الهَالِكِينَ، فَإِنَّ العِبْرَةَ بِخَوَاتِيمِ الأَعْمَالِ وَصِدْقِ السَّرَائِرِ.
        """.trimIndent()

        val arabicHtml = SmartBookConverter.convertRawTextToLuxuryHtml(
            rawText = arabicRawText,
            fallbackFileName = "نور_السكينة.txt",
            theme = BookVisualTheme.ISLAMIC_EMERALD
        )

        assertTrue(arabicHtml.contains("dir=\"rtl\""))
        assertTrue(arabicHtml.contains("lang=\"ar\""))
        assertTrue(arabicHtml.contains("نُورُ السَّكِينَةِ وَإِشْرَاقُ الْفُؤَادِ"))
        assertTrue(arabicHtml.contains("عَبْدُ الرَّحْمَنِ الأَمِينُ"))
        assertTrue(arabicHtml.contains("أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"))
        assertTrue(arabicHtml.contains("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"))
        assertTrue(arabicHtml.contains("بِآيَاتِ"))
        assertTrue(arabicHtml.contains("شُؤُونِهِ"))
        assertTrue(arabicHtml.contains("امْرِئٍ"))
        assertTrue(arabicHtml.contains("quran-box"))
        assertTrue(arabicHtml.contains("hadith-box"))
        assertTrue(arabicHtml.contains("heart-keepsake-card"))
        assertTrue(arabicHtml.contains("chapter-opener"))
        assertTrue(arabicHtml.contains("normalizeArabicWithMap"))
        assertTrue(arabicHtml.contains("__maktabatiSetFluidZoom"))

        val parsedArabicMeta = HtmlBookParser.parseContent(arabicHtml, "افتراضي", quickMode = false)
        assertTrue(parsedArabicMeta.isRtl)
        assertEquals("ar", parsedArabicMeta.language)
        assertTrue(parsedArabicMeta.pageCount >= 3)
        assertEquals("#065F46", parsedArabicMeta.accentRoseHex)
        assertEquals("#B47B26", parsedArabicMeta.accentGoldHex)
        assertEquals("#F8F4E8", parsedArabicMeta.bgPrimaryHex)

        val isolatedPdfForms = "\uFEFB\uFE91\uFE8E"
        val normalizedForms = SmartBookConverter.normalizeAndCleanExtractedText(isolatedPdfForms)
        assertEquals("لابا", normalizedForms)

        val englishRawText = """
            The Art of Deep Reading
            By: Prof. Arthur Pendelton

            Chapter 1: Foundations of Focus
            Reading deeply in a distracted world requires deliberate stillness, intellectual curiosity, and a quiet sanctuary for the mind. Every great book is a conversation across centuries.

            Summary: Cultivate daily moments of uninterrupted reflection to transform information into lasting wisdom.
        """.trimIndent()

        val englishHtml = SmartBookConverter.convertRawTextToLuxuryHtml(
            rawText = englishRawText,
            fallbackFileName = "deep_reading.md",
            theme = BookVisualTheme.ROYAL_ACADEMIC
        )
        assertTrue(englishHtml.contains("dir=\"ltr\""))
        assertTrue(englishHtml.contains("lang=\"en\""))
        assertTrue(englishHtml.contains("The Art of Deep Reading"))
        assertTrue(englishHtml.contains("Prof. Arthur Pendelton"))
        assertTrue(englishHtml.contains("Foundations of Focus"))

        val parsedEnglishMeta = HtmlBookParser.parseContent(englishHtml, "Default", quickMode = false)
        assertFalse(parsedEnglishMeta.isRtl)
        assertEquals("en", parsedEnglishMeta.language)
        assertTrue(parsedEnglishMeta.pageCount >= 2)
    }

    @Test
    fun testSmartBookConverterExtractsAndImportsDocxTxtAndRtfIntoLibrary(): Unit = runBlocking {
        val dao = database.bookDao()
        val repository = BookRepository(dao, context)

        val txtFile = File(context.cacheDir, "رسالة_القلوب.txt")
        txtFile.writeText(
            """
            رِسَالَةُ الْقُلُوبِ الصَّادِقَةِ
            بقلم: مَنْصُور قَطِينَه

            الفصل الأول: صَفَاءُ النِّيَّةِ
            قال تعالى: ﴿ وَمَا أُمِرُوا إِلَّا لِيَعْبُدُوا اللَّهَ مُخْلِصِينَ لَهُ الدِّينَ ﴾
            إِنَّ صَفَاءَ الْقَلْبِ هُوَ مِفْتَاحُ كُلِّ خَيْرٍ وَبَرَكَةٍ فِي الدُّنْيَا وَالآخِرَةِ.
            """.trimIndent(),
            Charsets.UTF_8
        )
        val txtUri = android.net.Uri.fromFile(txtFile)
        val txtProposalResult = repository.inspectConvertibleDocument(txtUri)
        assertTrue(txtProposalResult.isSuccess)
        val txtProposal = txtProposalResult.getOrThrow()
        assertEquals("TXT", txtProposal.sourceFormatLabel)
        assertTrue(txtProposal.detectedTitle.contains("رِسَالَةُ الْقُلُوبِ"))
        assertTrue(txtProposal.detectedAuthor.contains("مَنْصُور"))

        val importedTxtBook = repository.convertAndImportProposal(
            proposal = txtProposal,
            customTitle = txtProposal.detectedTitle,
            customSubtitle = txtProposal.detectedSubtitle,
            customAuthor = txtProposal.detectedAuthor,
            theme = BookVisualTheme.ROSE_HEART
        ).getOrThrow()
        assertTrue(importedTxtBook.title.contains("رِسَالَةُ الْقُلُوبِ"))
        assertTrue(importedTxtBook.pageCount >= 2)

        val docxFile = File(context.cacheDir, "آفاق_المعرفة.docx")
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(docxFile)).use { zos ->
            zos.putNextEntry(java.util.zip.ZipEntry("word/document.xml"))
            val docXml = """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
                  <w:body>
                    <w:p><w:r><w:t>آفَاقُ الْمَعْرِفَةِ وَالْإِبْدَاعِ</w:t></w:r></w:p>
                    <w:p><w:r><w:t>تأليف: د. خَالِد الْمَنْصُور</w:t></w:r></w:p>
                    <w:p><w:r><w:t>الفصل الأول: صناعة الأثر</w:t></w:r></w:p>
                    <w:p><w:r><w:t>قال تعالى: ﴿ وَقُلْ رَبِّ زِدْنِي عِلْمًا ﴾</w:t></w:r></w:p>
                    <w:p><w:r><w:t>الْعِلْمُ نُورٌ يَهْدِي الْعُقُولَ وَيَبْنِي الْحَضَارَاتِ الرَّاسِخَةَ.</w:t></w:r></w:p>
                  </w:body>
                </w:document>
            """.trimIndent()
            zos.write(docXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
        val docxUri = android.net.Uri.fromFile(docxFile)
        val docxProposal = repository.inspectConvertibleDocument(docxUri).getOrThrow()
        assertEquals("DOCX", docxProposal.sourceFormatLabel)
        assertTrue(docxProposal.detectedTitle.contains("آفَاقُ الْمَعْرِفَةِ"))
        val importedDocxBook = repository.convertAndImportProposal(
            proposal = docxProposal,
            theme = BookVisualTheme.YOUTH_HORIZON
        ).getOrThrow()
        assertTrue(importedDocxBook.title.contains("آفَاقُ الْمَعْرِفَةِ"))
        assertEquals("#0284C7", importedDocxBook.accentRoseHex)

        val rtfFile = File(context.cacheDir, "درر_الحكمة.rtf")
        val rtfArabicLine1 = "دُرَرُ الْحِكْمَةِ وَالْبَيَانِ".map { "\\u${it.code}?" }.joinToString("")
        val rtfArabicLine2 = "الفصل الأول: أدب الحوار والكلمة الطيبة".map { "\\u${it.code}?" }.joinToString("")
        val rtfArabicLine3 = "« الْكَلِمَةُ الطَّيِّبَةُ صَدَقَةٌ »".map { "\\u${it.code}?" }.joinToString("")
        val rtfContent = "{\\rtf1\\ansi\\deff0 $rtfArabicLine1\\par $rtfArabicLine2\\par $rtfArabicLine3\\par}"
        rtfFile.writeText(rtfContent, Charsets.US_ASCII)

        val rtfUri = android.net.Uri.fromFile(rtfFile)
        val rtfProposal = repository.inspectConvertibleDocument(rtfUri).getOrThrow()
        assertEquals("RTF", rtfProposal.sourceFormatLabel)
        assertTrue(rtfProposal.detectedTitle.contains("دُرَرُ الْحِكْمَةِ"))
        val importedRtfBook = repository.convertAndImportProposal(
            proposal = rtfProposal,
            theme = BookVisualTheme.VELVET_TENT
        ).getOrThrow()
        assertTrue(importedRtfBook.title.contains("دُرَرُ الْحِكْمَةِ"))

        val allBooksInDb = dao.getAllBooks().first()
        assertEquals(3, allBooksInDb.size)
        val booksRoot = File(context.filesDir, "books")
        for (book in allBooksInDb) {
            val htmlFile = File(File(booksRoot, book.folderPath), book.entryHtmlPath)
            assertTrue("Generated HTML file must exist for ${book.title}", htmlFile.exists())
            val htmlText = htmlFile.readText(Charsets.UTF_8)
            assertTrue(htmlText.contains("const BOOK_DATA ="))
            assertTrue(htmlText.contains("maktabati-storage-isolation"))
        }

        txtFile.delete()
        docxFile.delete()
        rtfFile.delete()
    }

    @Test
    fun testPublishingTemplatesCatalogAndClipboardStudioConversion(): Unit = runBlocking {
        val allThemes = BookVisualTheme.entries
        assertTrue("Catalog must contain at least 26 diverse publishing templates", allThemes.size >= 26)

        for (theme in allThemes) {
            assertFalse("Template ${theme.id} should not use old book name رسائل القلب", theme.arabicName.contains("رسائل القلب"))
            assertFalse("Template ${theme.id} should not use old book name خيمة السكينة", theme.arabicName.contains("خيمة السكينة"))
            assertTrue("Template ${theme.id} must have a clear Arabic name", theme.arabicName.isNotBlank())
            assertTrue("Template ${theme.id} must have a clear subtitle description", theme.subtitleDescription.isNotBlank())
        }

        val nonAllCategories = BookTemplateCategory.entries.filter {
            it != BookTemplateCategory.ALL
        }
        assertEquals(8, nonAllCategories.size)
        for (cat in nonAllCategories) {
            val countInCat = allThemes.count { it.category == cat }
            assertTrue("Category ${cat.titleAr} must have templates", countInCat >= 2)
        }

        val dao = database.bookDao()
        val repository = BookRepository(dao, context)

        val clipboardText = """
            خُطُوَاتٌ نَحْوَ السَّكِينَةِ وَالنَّجَاحِ
            تأليف: د. سَامِي العُمَرِي

            الفصل الأول: صِنَاعَةُ الأَثَرِ وَتَجْدِيدُ الإِيمَانِ
            قال تعالى: ﴿ إِنَّ اللَّهَ لَا يُغَيِّرُ مَا بِقَوْمٍ حَتَّى يُغَيِّرُوا مَا بِأَنْفُسِهِمْ ﴾

            خطوة عملية: ابدأ يومك بدقائق من التدبر والتخطيط الهادئ قبل الانشغال بهاتفك، فالبدايات الصادقة تصنع النهايات المشرقة.

            إِذَا غَامَرْتَ فِي شَرَفٍ مَرُومِ ... فَلَا تَقْنَعْ بِمَا دُونَ النُّجُومِ
        """.trimIndent()

        val proposal = repository.inspectRawText(clipboardText).getOrThrow()
        assertTrue(proposal.detectedTitle.contains("خُطُوَاتٌ نَحْوَ السَّكِينَةِ"))
        assertTrue(proposal.detectedAuthor.contains("سَامِي العُمَرِي"))
        assertTrue(proposal.isRtl)

        val beigeBook = repository.convertAndImportRawText(
            rawText = clipboardText,
            customTitle = proposal.detectedTitle,
            customSubtitle = "طبعة ورق بيج مريح للعين",
            customAuthor = proposal.detectedAuthor,
            theme = BookVisualTheme.CREAM_BEIGE_PAPER
        ).getOrThrow()

        assertEquals("#F2E9D8", beigeBook.bgPrimaryHex)
        val beigeHtmlFile = repository.getBookAbsoluteFile(beigeBook)
        assertTrue(beigeHtmlFile.exists())
        val beigeHtml = beigeHtmlFile.readText(Charsets.UTF_8)
        assertTrue(beigeHtml.contains("action-callout"))
        assertTrue(beigeHtml.contains("poetry-stanza"))
        assertTrue(beigeHtml.contains("--font-body: \"Noto Naskh Arabic\""))

        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboardManager.setPrimaryClip(android.content.ClipData.newPlainText("book", clipboardText))

        val viewModel = LibraryViewModel(context as android.app.Application)
        viewModel.pasteFromClipboardAsBook()

        val activeProposal = viewModel.pendingConversionProposal.value
        assertNotNull("Pasting from clipboard must directly populate pendingConversionProposal to show template dialog", activeProposal)
        assertEquals("الحافظة", activeProposal?.sourceFormatLabel)
        assertTrue(activeProposal!!.detectedTitle.contains("خُطُوَاتٌ نَحْوَ السَّكِينَةِ"))
    }

    @Test
    fun testModularArchitectureAndBilingualLocalizationEngine() {
        // 1. Verify decomposed single-responsibility modules exist and are accessible under com.maktabati.app
        assertNotNull(DocumentTextExtractor::class.java)
        assertNotNull(ArabicTextNormalizer::class.java)
        assertNotNull(SemanticBookPaginator::class.java)
        assertNotNull(LuxuryHtmlTemplateRenderer::class.java)
        assertNotNull(SandboxCssBuilder::class.java)
        assertNotNull(SandboxJsRuntimeBuilder::class.java)
        assertNotNull(BuiltInArabicFontsCatalog::class.java)
        assertNotNull(CustomFontStorageManager::class.java)
        assertNotNull(CloudSyncUiDelegate::class.java)
        assertNotNull(BookImportQueueManager::class.java)

        // 2. Verify Bilingual (AR / EN) & BiDi (RTL / LTR) Localization Engine
        assertEquals(LayoutDirection.Rtl, AppLanguage.AR.layoutDirection)
        assertEquals(LayoutDirection.Ltr, AppLanguage.EN.layoutDirection)
        assertEquals("مكتبتي", ArabicStrings.appTitle)
        assertEquals("Maktabati", EnglishStrings.appTitle)
        assertEquals(ArabicStrings, stringsForLanguage(AppLanguage.AR))
        assertEquals(EnglishStrings, stringsForLanguage(AppLanguage.EN))

        val viewModel = LibraryViewModel(context as android.app.Application)
        viewModel.setAppLanguage(AppLanguage.AR)
        assertEquals(AppLanguage.AR, viewModel.appLanguage.value)
        viewModel.toggleAppLanguage()
        assertEquals(AppLanguage.EN, viewModel.appLanguage.value)
        viewModel.toggleAppLanguage()
        assertEquals(AppLanguage.AR, viewModel.appLanguage.value)
    }

    @Test
    fun testCleanPastedBookWithoutFakeMetadataAndSinglePageTurnAndLtrCounter() {
        val rawTextWithoutAuthorOrChapters = """
            رِسَالَةٌ إِلَى القَلْبِ السَّلِيمِ
            إِنَّ السَّكِينَةَ نُورٌ يُلْقِيهِ اللَّهُ فِي قَلْبِ العَبْدِ المُؤْمِنِ حِينَ يَصْدُقُ فِي تَوَكُّلِهِ وَإِخْلَاصِهِ، فَيَرَى الجَمَالَ فِي كُلِّ قَضَاءٍ وَقَدَرٍ.
            وَمِنْ أَعْظَمِ أَسْبَابِ انْشِرَاحِ الصَّدْرِ كَثْرَةُ الذِّكْرِ وَالإِحْسَانُ إِلَى الخَلْقِ وَالتَّغَافُلُ عَنِ الزَّلَّاتِ.
        """.trimIndent()

        val extracted = ArabicTextNormalizer.extractTitleSubtitleAndAuthor(
            rawText = rawTextWithoutAuthorOrChapters,
            explicitTitle = null,
            explicitAuthor = null,
            fallbackTitle = "رسالة"
        )
        assertEquals("رِسَالَةٌ إِلَى القَلْبِ السَّلِيمِ", extracted.first)
        assertEquals("Subtitle must be empty when not explicitly specified", "", extracted.second)
        assertEquals("Author must be empty when not explicitly specified", "", extracted.third)

        val generatedHtml = SmartBookConverter.convertRawTextToLuxuryHtml(
            rawText = rawTextWithoutAuthorOrChapters,
            fallbackFileName = "رسالة.txt",
            theme = BookVisualTheme.ROSE_HEART
        )

        // Verify no fake author, subtitle, eyebrow, chapter tag, or closing reflection page
        assertFalse(generatedHtml.contains("إصدارات مكتبتي الفاخرة"))
        assertFalse(generatedHtml.contains("طبعة رقمية تفاعلية منسقة"))
        assertFalse(generatedHtml.contains("طَبْعَةٌ رَقْمِيَّةٌ تَفَاعُلِيَّةٌ فَاخِرَة"))
        assertFalse(generatedHtml.contains("مدخل الكتاب"))
        assertFalse(generatedHtml.contains("دفتر التأملات والخلاصة الشخصية"))
        assertFalse(generatedHtml.contains("(2)</h2>"))

        // Verify LTR page counter & single-page turn guard
        assertTrue(generatedHtml.contains("id=\"pageCounter\" class=\"page-counter\" dir=\"ltr\""))
        assertTrue(generatedHtml.contains("unicode-bidi: isolate"))
        assertTrue(generatedHtml.contains("__maktabatiLastPageTurnAt"))

        val parsedMeta = HtmlBookParser.parseContent(generatedHtml, "افتراضي", quickMode = false)
        assertEquals("", parsedMeta.author)
        assertEquals("Cover + 1 content page = 2 pages total", 2, parsedMeta.pageCount)
    }

    @Test
    fun testGeneratorAndAppHarmonyZeroTitleAndTocDuplication() {
        val sampleReadyHtml = """
            <!DOCTYPE html>
            <html lang="ar" dir="rtl">
            <head>
              <meta charset="utf-8"/>
              <meta name="author" content="إصدارات مكتبتي الفاخرة"/>
              <meta name="description" content="🌸 عَلَى مَهْل... عُودِي إِلَى نُورِكِ 🌸"/>
              <title>🌸 عَلَى مَهْل... عُودِي إِلَى نُورِكِ 🌸</title>
            </head>
            <body>
              <div class="cover-page">
                <h1>عَلَى مَهْل... عُودِي إِلَى نُورِكِ</h1>
                <div class="subtitle">رِحْلَةٌ هَادِئَةٌ إِلَى سَكِينَةِ الفِطْرَة</div>
              </div>
              <script>
                const BOOK_DATA = {
                  title: "عَلَى مَهْل... عُودِي إِلَى نُورِكِ",
                  pages: [
                    { tag: "", title: "على مهل... عودي إلى نوركِ", type: "cover", html: "<div class='cover-page'><h1>على مهل</h1></div>" },
                    { tag: "المحطة الأولى", title: "عندما يتعب القلب بصمت", type: "opener", html: "<h1>عندما يتعب القلب بصمت</h1><p>نص أول</p>" },
                    { tag: "المحطة الأولى", title: "عندما يتعب القلب بصمت", type: "reading", html: "<p>استرسال المحطة الأولى دون تكرار في الفهرس</p>" },
                    { tag: "المحطة الأولى", title: "وهم المقارنات الصامتة", type: "reading", html: "<h2>وهم المقارنات الصامتة</h2><p>محور فرعي جديد</p>" }
                  ]
                };
              </script>
            </body>
            </html>
        """.trimIndent()

        val parsed = HtmlBookParser.parseContent(sampleReadyHtml, "افتراضي", quickMode = false)
        assertEquals("عَلَى مَهْل... عُودِي إِلَى نُورِكِ", parsed.title)
        assertEquals("رِحْلَةٌ هَادِئَةٌ إِلَى سَكِينَةِ الفِطْرَة", parsed.subtitle)
        assertEquals("Synthetic publisher author must be stripped", "", parsed.author)
        assertEquals("All 4 pages in BOOK_DATA.pages must be counted", 4, parsed.pageCount)
        assertEquals("Consecutive continuation pages with identical (tag, title) must be deduplicated in TOC", 3, parsed.tableOfContents.size)
        assertEquals(1, parsed.tableOfContents[0].level) // cover
        assertEquals(1, parsed.tableOfContents[1].level) // opener
        assertEquals("عندما يتعب القلب بصمت", parsed.tableOfContents[1].title)
        assertEquals(2, parsed.tableOfContents[2].level) // reading sub-topic
        assertEquals("وهم المقارنات الصامتة", parsed.tableOfContents[2].title)
    }

    @Test
    fun testBookCardCleanLayoutAndTypographySpecimens() {
        assertEquals("", HtmlBookParser.sanitizeExtractedAuthor("إصدارات مكتبتي الفاخرة"))
        assertEquals("", HtmlBookParser.sanitizeExtractedAuthor("طبعة رقمية تفاعلية"))
        assertEquals("ابن القيم", HtmlBookParser.sanitizeExtractedAuthor("ابن القيم"))

        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_QURAN.contains("﴿"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_BODY.contains("الحِكْمَةِ"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_HADITH_QUOTES.contains("ﷺ"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_TOC_INTERFACE.contains("الفهرس"))
        assertTrue(BookTypographyProfile.DEFAULT_SAMPLE_NUMBERS.contains("٠١٢٣٤٥٦٧٨٩"))

        val bookCardFile = listOf(
            File("/app/src/main/java/com/maktabati/app/ui/components/BookCard.kt"),
            File("../app/src/main/java/com/maktabati/app/ui/components/BookCard.kt"),
            File("src/main/java/com/maktabati/app/ui/components/BookCard.kt")
        ).firstOrNull { it.exists() }
        assertNotNull("BookCard.kt must exist", bookCardFile)
        val bookCardSource = bookCardFile!!.readText(Charsets.UTF_8)
        assertFalse("BookCard must not render language badge text on cover", bookCardSource.contains("text = langLabel"))
        assertFalse("BookCard must not render numeric progress percentage text", bookCardSource.contains("(book.readingProgress * 100).toInt()}%"))
        assertTrue("BookCard must keep silent LinearProgressIndicator", bookCardSource.contains("LinearProgressIndicator"))

        val typoDialogFile = listOf(
            File("/app/src/main/java/com/maktabati/app/ui/components/TypographySettingsDialog.kt"),
            File("../app/src/main/java/com/maktabati/app/ui/components/TypographySettingsDialog.kt"),
            File("src/main/java/com/maktabati/app/ui/components/TypographySettingsDialog.kt")
        ).firstOrNull { it.exists() }
        assertNotNull("TypographySettingsDialog.kt must exist", typoDialogFile)
        val typoDialogSource = typoDialogFile!!.readText(Charsets.UTF_8)
        assertTrue(typoDialogSource.contains("typography_specimen_chip_"))
        assertTrue(typoDialogSource.contains("typography_edit_preview_toggle"))
        assertTrue(typoDialogSource.contains("تحرير يدوي"))
        assertTrue(typoDialogSource.contains("﴿ الآية ﴾"))
    }

    @Test
    fun testLargeSingleNewlineArabicTextBookConversionAndImportMemorySafety(): Unit = runBlocking {
        val dao = database.bookDao()
        val repository = BookRepository(dao, context)

        // Build a large >35,000-word (>230,000 chars) Arabic text file using ONLY single '\n' line breaks (no '\n\n')
        val largeBookBuilder = StringBuilder(260_000)
        largeBookBuilder.append("مَوْسُوعَةُ السَّكِينَةِ وَالبَيَانِ الكُبْرَى\n")
        largeBookBuilder.append("تأليف: د. عَبْدُ الرَّحْمَنِ المَنْصُور\n")
        val sampleLine = "إِنَّ القَلْبَ السَّلِيمَ يَجِدُ أُنْسَهُ فِي ذِكْرِ اللَّهِ وَالتَّأَمُّلِ فِي آيَاتِ الكَوْنِ وَالحِكْمَةِ البَالِغَةِ التِي تَهْدِي النُّفُوسَ إِلَى سَوَاءِ السَّبِيلِ."
        for (chapter in 1..12) {
            largeBookBuilder.append("الفصل $chapter: مَعَالِمُ الهِدَايَةِ وَالبَصِيرَةِ فِي المَرْحَلَةِ $chapter\n")
            largeBookBuilder.append("قال تعالى: ﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ [الرعد: 28]\n")
            for (lineNum in 1..150) {
                largeBookBuilder.append(sampleLine).append('\n')
            }
            largeBookBuilder.append("1. التَّثَبُّتُ فِي الفَهْمِ وَالعَمَلِ الصَّالِحِ الدَّائِمِ.\n")
        }

        val largeTxtFile = File(context.cacheDir, "موسوعة_السكينة_الكبرى.txt")
        largeTxtFile.bufferedWriter(Charsets.UTF_8).use { it.write(largeBookBuilder.toString()) }
        assertTrue("Large text file must exceed 200,000 characters", largeTxtFile.length() > 200_000L)

        val uri = android.net.Uri.fromFile(largeTxtFile)
        val proposalResult = repository.inspectConvertibleDocument(uri)
        assertTrue("Large document inspection must succeed without OOM", proposalResult.isSuccess)
        val proposal = proposalResult.getOrThrow()

        // Verify disk staging activated so UI state rawExtractedText stays small (< 5000 chars)
        assertNotNull("Large document proposal must use stagedTextFile on disk", proposal.stagedTextFile)
        assertTrue("Staged file must exist before conversion", proposal.stagedTextFile!!.exists())
        assertTrue("In-memory rawExtractedText in UI state must be capped to 4096 chars", proposal.rawExtractedText.length <= 4096)
        assertTrue("Word count must exceed 35,000 words", proposal.wordCount > 35_000)
        assertTrue("Preview excerpt must be populated", proposal.previewExcerpt.isNotBlank())

        // Convert and import the large proposal
        val importedResult = repository.convertAndImportProposal(
            proposal = proposal,
            theme = BookVisualTheme.ISLAMIC_EMERALD
        )
        assertTrue("Converting and importing large proposal must succeed", importedResult.isSuccess)
        val book = importedResult.getOrThrow()

        // Verify single-\n lines were properly split into 300+ balanced pages (not 1 giant paragraph)
        assertTrue("Large 35,000+ word book must paginate into at least 300 pages, got ${book.pageCount}", book.pageCount >= 300)

        // Verify staged temporary file was cleaned up after import
        assertFalse("Staged temporary file must be deleted after import", proposal.stagedTextFile!!.exists())

        // Verify TOC JSON in Room database is capped at <= 300 entries for CursorWindow safety
        val parsedToc = HtmlBookParser.jsonToToc(book.tableOfContentsJson)
        assertTrue("Serialized TOC in Room DB must be capped at <= 300 entries", parsedToc.size in 12..300)

        // Verify cover snapshot was rendered via Canvas without crashing
        val bookDir = repository.getBookDirectory(book)
        assertTrue("Native Canvas cover snapshot must exist", HtmlCoverSnapshotGenerator.hasSavedCoverSnapshot(bookDir))

        val htmlFile = repository.getBookAbsoluteFile(book)
        assertTrue("Generated HTML book file must exist", htmlFile.exists())
        assertTrue("Generated HTML book file must exceed 200KB", htmlFile.length() > 200_000L)

        repository.deleteBook(book.id)
        largeTxtFile.delete()
    }

    @Test
    fun testUnifiedCoverDesignFastHtmlCheckAndResponsibleFormatRestrictions() {
        // 1. Verify unified cover_v3.png dimensions (544x800) and markdown/dot sanitization
        assertEquals("cover_v3.png", HtmlCoverSnapshotGenerator.SNAPSHOT_FILE_NAME)
        assertEquals("الإستغفار مفتاح نجاة", HtmlCoverSnapshotGenerator.sanitizeCoverTitle("*الإستغفار مفتاح نجاة* 💫🤔💫"))
        assertEquals("", HtmlCoverSnapshotGenerator.sanitizeCoverSubtitle(".....", "صلاح الآباء"))
        assertEquals("والحرص عليها", HtmlCoverSnapshotGenerator.sanitizeCoverSubtitle("والحرص عليها", "الدعوة"))

        val coverTarget = File(context.cacheDir, "test_unified_cover_v3.png")
        val rendered = HtmlCoverSnapshotGenerator.renderHtmlCoverPageSnapshot(
            htmlContent = "",
            metadata = com.maktabati.app.data.importer.ParsedHtmlMetadata(
                title = "*الإستغفار مفتاح نجاة*",
                subtitle = ".....",
                author = "إصدارات مكتبتي الفاخرة",
                description = "",
                language = "ar",
                isRtl = true,
                pageCount = 12,
                accentRoseHex = "#6B2D46",
                accentGoldHex = "#C59B27",
                bgPrimaryHex = "#FAF3EB",
                coverGradientStart = 0xFFFAF3EB,
                coverGradientEnd = 0xFFF3E6DA,
                hasExternalCss = false,
                hasExternalJs = false,
                tableOfContents = emptyList(),
                hasCoverPage = true
            ),
            targetFile = coverTarget
        )
        assertTrue(rendered)
        assertTrue(coverTarget.exists())
        val decodedBmp = android.graphics.BitmapFactory.decodeFile(coverTarget.absolutePath)
        assertNotNull(decodedBmp)
        assertEquals(544, decodedBmp!!.width)
        assertEquals(800, decodedBmp.height)
        coverTarget.delete()

        // 2. Verify RandomAccessFile fast check isHtmlAlreadyInjected without reading entire file
        val uninjectedFile = File(context.cacheDir, "uninjected_test.html")
        uninjectedFile.writeText("<!DOCTYPE html><html><head><title>Test</title></head><body><p>Body</p></body></html>")
        assertFalse(com.maktabati.app.ui.screens.reader.isHtmlAlreadyInjected(uninjectedFile))

        val injectedHtml = SandboxHtmlInjector.injectIntoHtml(uninjectedFile.readText(), "book_raf_test", 10)
        uninjectedFile.writeText(injectedHtml)
        assertTrue(com.maktabati.app.ui.screens.reader.isHtmlAlreadyInjected(uninjectedFile))
        uninjectedFile.delete()

        // 3. Verify responsible format restrictions: only 100%-reliable formats supported, PDF & binary DOC rejected
        assertEquals(
            setOf("txt", "md", "markdown", "docx", "odt", "rtf", "epub"),
            SmartBookConverter.SUPPORTED_TEXT_EXTENSIONS
        )
        assertFalse(SmartBookConverter.isConvertibleFileName("book.pdf"))
        assertFalse(SmartBookConverter.isConvertibleFileName("legacy.doc"))
        assertFalse(SmartBookConverter.isConvertibleMimeType("application/pdf"))
        assertFalse(SmartBookConverter.isConvertibleMimeType("application/msword"))
        assertTrue(SmartBookConverter.isConvertibleFileName("book.docx"))
        assertTrue(SmartBookConverter.isConvertibleFileName("book.txt"))
        assertTrue(SmartBookConverter.isConvertibleFileName("book.epub"))

        val fakePdfBytes = "%PDF-1.7 fake pdf content".toByteArray(Charsets.US_ASCII)
        val pdfThrown = runCatching {
            DocumentTextExtractor.extractStructuredDocument(fakePdfBytes, "sample.pdf")
        }.exceptionOrNull()
        assertNotNull("PDF must be rejected with clear guidance", pdfThrown)
        assertTrue(pdfThrown!!.message!!.contains("PDF غير مدعومة"))
    }
}


