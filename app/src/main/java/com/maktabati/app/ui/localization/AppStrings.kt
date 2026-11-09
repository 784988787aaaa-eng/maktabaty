package com.maktabati.app.ui.localization

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
interface AppStrings {
    val language: AppLanguage

    // Top App Bar & Navigation
    val appTitle: String
    val appSubtitleTemplate: (totalBooks: Int, completedBooks: Int) -> String
    val searchPlaceholder: String
    val clearSearchContentDescription: String
    val closeSearchContentDescription: String
    val searchBooksContentDescription: String
    val typographyStudioContentDescription: String
    val toggleViewModeContentDescription: String
    val toggleThemeModeContentDescription: String
    val toggleLanguageContentDescription: String
    val cloudAndBackupContentDescription: String
    val aboutMaktabatiTitle: String

    // Shelves
    val shelfAll: String
    val shelfFavorites: String
    val shelfReadingNow: String
    val shelfCompleted: String
    val createNewShelfContentDescription: String

    // Reading Stats Header
    val statsTotalBooksLabel: String
    val statsCompletedLabel: String
    val statsReadingTimeLabel: String
    val statsLastReadLabel: String
    val statsHideDetails: String
    val statsShowDetails: String

    // Empty Library State
    val emptyLibraryTitle: String
    val emptyLibrarySubtitle: String
    val emptyLibraryPrimaryButton: String
    val emptyLibraryPasteButton: String

    // FAB & Import Menu
    val fabAddBook: String
    val menuPasteFromClipboardTitle: String
    val menuPasteFromClipboardSubtitle: String
    val menuImportLocalFilesTitle: String
    val menuImportLocalFilesSubtitle: String
    val clipboardEmptyToast: String

    // Book Card & Book Details
    val bookCardCompletedBadge: String
    val bookCardPageProgress: (current: Int, total: Int) -> String
    val bookDetailAuthorFallback: String
    val bookDetailOpenReader: String
    val bookDetailChangeShelf: String
    val bookDetailTypographyStudio: String
    val bookDetailDeleteBook: String
    val bookDetailDeleteConfirmTitle: String
    val bookDetailDeleteConfirmMessage: (title: String) -> String
    val bookDetailCancel: String
    val bookDetailConfirmDelete: String
    val bookMenuSelectMultiple: String
    val batchSelectedCount: (count: Int) -> String
    val batchSelectAll: String
    val batchDeselectAll: String
    val batchMoveToShelf: String
    val batchShare: String
    val batchDelete: String
    val batchClearSelection: String
    val batchMoveDialogTitle: String
    val batchMoveGeneralShelf: String
    val batchMoveCreateNewShelf: String
    val batchDeleteConfirmTitle: String
    val batchDeleteConfirmMessage: (count: Int) -> String

    // Cloud Sync Menu & Banner
    val cloudAccountFallback: String
    val cloudSyncReadyStatus: String
    val cloudBackupNow: String
    val cloudRestoreNow: String
    val cloudSignOut: String
    val cloudSignInWithGoogle: String
    val cloudSignInRequiredToast: String
    val localBackupExportZip: String
    val localBackupImportZip: String
    val restoreSamplesTitle: String

    // Reader Screen
    val readerBackToLibrary: String
    val readerTableOfContents: String
    val readerInBookSearch: String
    val readerBookmarks: String
    val readerTypographySettings: String
    val readerThemeMode: String
    val readerShareOrExport: String
    val readerPageIndicator: (current: Int, total: Int) -> String
    val readerSearchPlaceholder: String
    val readerAddBookmark: String
    val readerBookmarkAddedToast: String
    val readerNoTocEntries: String
    val readerNoBookmarksYet: String

    // Typography Studio
    val typographyStudioTitle: String
    val typographyScopeGlobal: String
    val typographyScopeCurrentBook: String
    val typographyRoleQuran: String
    val typographyRoleHeadings: String
    val typographyRoleBody: String
    val typographyRoleQuotes: String
    val typographyRoleToc: String
    val typographyRolePageNumbers: String
    val typographyCustomPreviewPlaceholder: String
    val typographyImportFontButton: String
    val typographySaveAndApply: String
    val typographyResetDefaults: String

    // Conversion Studio
    val studioConvertFileTitle: String
    val studioClipboardTitle: String
    val studioConfirmConvert: String
    val studioCancel: String
}

@Immutable
object ArabicStrings : AppStrings {
    override val language = AppLanguage.AR

    override val appTitle = "مكتبتي"
    override val appSubtitleTemplate: (Int, Int) -> String = { total, completed ->
        "$total كتاب • $completed مكتمل"
    }
    override val searchPlaceholder = "ابحث بالعنوان أو الكاتب أو التصنيف..."
    override val clearSearchContentDescription = "مسح"
    override val closeSearchContentDescription = "إغلاق البحث"
    override val searchBooksContentDescription = "بحث في الكتب"
    override val typographyStudioContentDescription = "استوديو الخطوط الطباعية"
    override val toggleViewModeContentDescription = "تبديل طريقة العرض"
    override val toggleThemeModeContentDescription = "تبديل المظهر الليلي/النهاري"
    override val toggleLanguageContentDescription = "تبديل اللغة / Switch Language"
    override val cloudAndBackupContentDescription = "المزامنة السحابية والنسخ الاحتياطي"
    override val aboutMaktabatiTitle = "حول مكتبتي والدليل"

    override val shelfAll = "الكل"
    override val shelfFavorites = "المفضلة"
    override val shelfReadingNow = "قيد القراءة"
    override val shelfCompleted = "مكتملة"
    override val createNewShelfContentDescription = "إضافة رف جديد"

    override val statsTotalBooksLabel = "الكتب"
    override val statsCompletedLabel = "مكتمل"
    override val statsReadingTimeLabel = "وقت القراءة"
    override val statsLastReadLabel = "آخر قراءة"
    override val statsHideDetails = "طيّ التفاصيل"
    override val statsShowDetails = "عرض التفاصيل"

    override val emptyLibraryTitle = "لا توجد كتب في هذا الرف"
    override val emptyLibrarySubtitle = "أضف ملفات كتب (.html, .txt, .md, .docx, .epub, .rtf, .odt, .zip) أو الصق نصاً من الحافظة لتحويله فوراً إلى كتاب فاخر."
    override val emptyLibraryPrimaryButton = "استيراد ملفات كتب"
    override val emptyLibraryPasteButton = "لصق نص من الحافظة"

    override val fabAddBook = "إضافة"
    override val menuPasteFromClipboardTitle = "لصق من الحافظة"
    override val menuPasteFromClipboardSubtitle = "تحويل النص المنسوخ فوراً إلى كتاب فاخر"
    override val menuImportLocalFilesTitle = "ملفات محلية"
    override val menuImportLocalFilesSubtitle = "HTML, TXT, MD, DOCX, EPUB, RTF, ODT, ZIP"
    override val clipboardEmptyToast = "الحافظة فارغة! انسخ نص الكتاب أولاً ثم اضغط لصق من الحافظة."

    override val bookCardCompletedBadge = "مكتمل"
    override val bookCardPageProgress: (Int, Int) -> String = { current, total ->
        "ص $current من $total"
    }
    override val bookDetailAuthorFallback = "طبعة خاصة • مكتبتي"
    override val bookDetailOpenReader = "فتح الكتاب للقراءة"
    override val bookDetailChangeShelf = "نقل إلى رف آخر"
    override val bookDetailTypographyStudio = "تخصيص خطوط الكتاب"
    override val bookDetailDeleteBook = "حذف الكتاب"
    override val bookDetailDeleteConfirmTitle = "تأكيد حذف الكتاب"
    override val bookDetailDeleteConfirmMessage: (String) -> String = { title ->
        "هل أنت متأكد من حذف «$title» نهائياً من مكتبتك؟"
    }
    override val bookDetailCancel = "إلغاء"
    override val bookDetailConfirmDelete = "حذف نهائي"
    override val bookMenuSelectMultiple = "تحديد متعدد"
    override val batchSelectedCount: (Int) -> String = { count -> "$count محددة" }
    override val batchSelectAll = "تحديد الكل"
    override val batchDeselectAll = "إلغاء الكل"
    override val batchMoveToShelf = "نقل لرف"
    override val batchShare = "مشاركة"
    override val batchDelete = "حذف"
    override val batchClearSelection = "إلغاء التحديد"
    override val batchMoveDialogTitle = "نقل الكتب المحددة إلى رف"
    override val batchMoveGeneralShelf = "الرف العام (بدون رف مخصص)"
    override val batchMoveCreateNewShelf = "+ إنشاء رف جديد ونقل الكتب إليه"
    override val batchDeleteConfirmTitle = "تأكيد الحذف الجماعي"
    override val batchDeleteConfirmMessage: (Int) -> String = { count ->
        "هل أنت متأكد من حذف ($count) كتب محددة من مكتبتي؟ سيتم إزالة نسخها المحفوظة وسجل قراءتها نهائياً دون المساس بملفاتك الأصلية في الجهاز."
    }

    override val cloudAccountFallback = "حساب جوجل"
    override val cloudSyncReadyStatus = "متصل بالسحابة وجاهز للمزامنة"
    override val cloudBackupNow = "رفع نسخة احتياطية للسحابة الآن"
    override val cloudRestoreNow = "استعادة مكتبتي من السحابة"
    override val cloudSignOut = "تسجيل الخروج من السحابة"
    override val cloudSignInWithGoogle = "تسجيل الدخول بحساب Google للمزامنة"
    override val cloudSignInRequiredToast = "يرجى تسجيل الدخول بحساب Google أولاً"
    override val localBackupExportZip = "تصدير ملف نسخ احتياطي شامل (.zip)"
    override val localBackupImportZip = "استعادة من ملف نسخ احتياطي (.zip)"
    override val restoreSamplesTitle = "استعادة الكتب النموذجية"

    override val readerBackToLibrary = "العودة للمكتبة"
    override val readerTableOfContents = "فهرس الكتاب"
    override val readerInBookSearch = "بحث في الكتاب"
    override val readerBookmarks = "العلامات المرجعية"
    override val readerTypographySettings = "استوديو الخطوط"
    override val readerThemeMode = "سمة القراءة"
    override val readerShareOrExport = "مشاركة الكتاب"
    override val readerPageIndicator: (Int, Int) -> String = { current, total ->
        "صفحة $current من $total"
    }
    override val readerSearchPlaceholder = "ابحث داخل صفحات الكتاب..."
    override val readerAddBookmark = "إضافة علامة مرجعية"
    override val readerBookmarkAddedToast = "تم حفظ العلامة المرجعية"
    override val readerNoTocEntries = "لا يوجد فهرس فصول مسجل لهذا الكتاب"
    override val readerNoBookmarksYet = "لا توجد علامات مرجعية محفوظة بعد"

    override val typographyStudioTitle = "استوديو الخطوط الطباعية"
    override val typographyScopeGlobal = "تعميم على المكتبة"
    override val typographyScopeCurrentBook = "لهذا الكتاب فقط"
    override val typographyRoleQuran = "خط الآيات القرآنية"
    override val typographyRoleHeadings = "خط العناوين والفصول"
    override val typographyRoleBody = "خط المتن والفقرات"
    override val typographyRoleQuotes = "خط الاقتباسات والأحاديث"
    override val typographyRoleToc = "خط الفهرس"
    override val typographyRolePageNumbers = "خط أرقام الصفحات"
    override val typographyCustomPreviewPlaceholder = "اكتب نصك الخاص هنا لمعاينته بالخط المختار..."
    override val typographyImportFontButton = "استيراد خط خارجي"
    override val typographySaveAndApply = "حفظ وتطبيق"
    override val typographyResetDefaults = "استعادة الافتراضي"

    override val studioConvertFileTitle = "استوديو تحويل الكتب الفاخرة"
    override val studioClipboardTitle = "تحويل نص الحافظة إلى كتاب فاخر"
    override val studioConfirmConvert = "تحويل وإضافة للمكتبة"
    override val studioCancel = "إلغاء"
}

@Immutable
object EnglishStrings : AppStrings {
    override val language = AppLanguage.EN

    override val appTitle = "Maktabati"
    override val appSubtitleTemplate: (Int, Int) -> String = { total, completed ->
        "$total books • $completed completed"
    }
    override val searchPlaceholder = "Search by title, author, or shelf..."
    override val clearSearchContentDescription = "Clear"
    override val closeSearchContentDescription = "Close search"
    override val searchBooksContentDescription = "Search books"
    override val typographyStudioContentDescription = "Typography Studio"
    override val toggleViewModeContentDescription = "Toggle Grid/List View"
    override val toggleThemeModeContentDescription = "Toggle Dark/Light Theme"
    override val toggleLanguageContentDescription = "Switch Language / تبديل اللغة"
    override val cloudAndBackupContentDescription = "Cloud Sync & Backup"
    override val aboutMaktabatiTitle = "About Maktabati & Guide"

    override val shelfAll = "All"
    override val shelfFavorites = "Favorites"
    override val shelfReadingNow = "Reading"
    override val shelfCompleted = "Completed"
    override val createNewShelfContentDescription = "Create New Shelf"

    override val statsTotalBooksLabel = "Books"
    override val statsCompletedLabel = "Done"
    override val statsReadingTimeLabel = "Reading Time"
    override val statsLastReadLabel = "Last Read"
    override val statsHideDetails = "Hide Details"
    override val statsShowDetails = "Show Details"

    override val emptyLibraryTitle = "No books in this shelf"
    override val emptyLibrarySubtitle = "Import book files (.html, .txt, .md, .docx, .epub, .rtf, .odt, .zip) or paste text from your clipboard to craft an interactive luxury book."
    override val emptyLibraryPrimaryButton = "Import Book Files"
    override val emptyLibraryPasteButton = "Paste from Clipboard"

    override val fabAddBook = "Add"
    override val menuPasteFromClipboardTitle = "Paste from Clipboard"
    override val menuPasteFromClipboardSubtitle = "Transform copied text into a luxury book"
    override val menuImportLocalFilesTitle = "Local Files"
    override val menuImportLocalFilesSubtitle = "HTML, TXT, MD, DOCX, EPUB, RTF, ODT, ZIP"
    override val clipboardEmptyToast = "Clipboard is empty! Copy your book text first, then tap Paste from Clipboard."

    override val bookCardCompletedBadge = "Done"
    override val bookCardPageProgress: (Int, Int) -> String = { current, total ->
        "P. $current of $total"
    }
    override val bookDetailAuthorFallback = "Special Edition • Maktabati"
    override val bookDetailOpenReader = "Open Book"
    override val bookDetailChangeShelf = "Move to Shelf"
    override val bookDetailTypographyStudio = "Book Typography Studio"
    override val bookDetailDeleteBook = "Delete Book"
    override val bookDetailDeleteConfirmTitle = "Confirm Book Deletion"
    override val bookDetailDeleteConfirmMessage: (String) -> String = { title ->
        "Are you sure you want to permanently remove \"$title\" from your library?"
    }
    override val bookDetailCancel = "Cancel"
    override val bookDetailConfirmDelete = "Delete"
    override val bookMenuSelectMultiple = "Select Books"
    override val batchSelectedCount: (Int) -> String = { count -> "$count Selected" }
    override val batchSelectAll = "Select All"
    override val batchDeselectAll = "Deselect All"
    override val batchMoveToShelf = "Move to Shelf"
    override val batchShare = "Share"
    override val batchDelete = "Delete"
    override val batchClearSelection = "Clear Selection"
    override val batchMoveDialogTitle = "Move Selected Books to Shelf"
    override val batchMoveGeneralShelf = "General Library (No Custom Shelf)"
    override val batchMoveCreateNewShelf = "+ Create New Shelf & Move Books"
    override val batchDeleteConfirmTitle = "Confirm Batch Deletion"
    override val batchDeleteConfirmMessage: (Int) -> String = { count ->
        "Are you sure you want to delete ($count) selected books from your library? Their saved copies and reading progress will be permanently removed without affecting your original device files."
    }

    override val cloudAccountFallback = "Google Account"
    override val cloudSyncReadyStatus = "Connected & ready to sync"
    override val cloudBackupNow = "Backup Library to Cloud Now"
    override val cloudRestoreNow = "Restore Library from Cloud"
    override val cloudSignOut = "Sign Out of Cloud"
    override val cloudSignInWithGoogle = "Sign in with Google for Cloud Sync"
    override val cloudSignInRequiredToast = "Please sign in with Google first"
    override val localBackupExportZip = "Export Full Library Backup (.zip)"
    override val localBackupImportZip = "Restore from Backup File (.zip)"
    override val restoreSamplesTitle = "Restore Sample Books"

    override val readerBackToLibrary = "Back to Library"
    override val readerTableOfContents = "Table of Contents"
    override val readerInBookSearch = "Search in Book"
    override val readerBookmarks = "Bookmarks"
    override val readerTypographySettings = "Typography Studio"
    override val readerThemeMode = "Reading Theme"
    override val readerShareOrExport = "Share Book"
    override val readerPageIndicator: (Int, Int) -> String = { current, total ->
        "Page $current of $total"
    }
    override val readerSearchPlaceholder = "Search inside book pages..."
    override val readerAddBookmark = "Add Bookmark"
    override val readerBookmarkAddedToast = "Bookmark saved"
    override val readerNoTocEntries = "No table of contents entries found"
    override val readerNoBookmarksYet = "No bookmarks saved yet"

    override val typographyStudioTitle = "Typography Studio"
    override val typographyScopeGlobal = "Apply to Library"
    override val typographyScopeCurrentBook = "This Book Only"
    override val typographyRoleQuran = "Quranic Verses Font"
    override val typographyRoleHeadings = "Headings & Chapters Font"
    override val typographyRoleBody = "Body Text Font"
    override val typographyRoleQuotes = "Quotes & Hadith Font"
    override val typographyRoleToc = "Table of Contents Font"
    override val typographyRolePageNumbers = "Page Numbers Font"
    override val typographyCustomPreviewPlaceholder = "Type custom text here to preview with the selected font..."
    override val typographyImportFontButton = "Import Custom Font"
    override val typographySaveAndApply = "Save & Apply"
    override val typographyResetDefaults = "Reset Defaults"

    override val studioConvertFileTitle = "Luxury Book Conversion Studio"
    override val studioClipboardTitle = "Convert Clipboard Text to Luxury Book"
    override val studioConfirmConvert = "Convert & Add to Library"
    override val studioCancel = "Cancel"
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { ArabicStrings }

fun stringsForLanguage(language: AppLanguage): AppStrings {
    return when (language) {
        AppLanguage.AR -> ArabicStrings
        AppLanguage.EN -> EnglishStrings
    }
}
