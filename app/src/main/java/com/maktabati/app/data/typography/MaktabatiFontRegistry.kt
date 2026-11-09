package com.maktabati.app.data.typography

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.WebResourceResponse
import com.maktabati.app.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

typealias FontCategory = com.maktabati.app.data.typography.model.FontCategory
typealias FontWeightAsset = com.maktabati.app.data.typography.model.FontWeightAsset
typealias RegisteredArabicFont = com.maktabati.app.data.typography.model.RegisteredArabicFont
typealias BookTypographyProfile = com.maktabati.app.data.typography.model.BookTypographyProfile
typealias EditorialTypographyPreset = com.maktabati.app.data.typography.model.EditorialTypographyPreset
typealias UserTypographyPreferences = com.maktabati.app.data.typography.model.UserTypographyPreferences

/**
 * Central Arabic Typography Registry & Offline WebView Font Server for the Maktabati («مكتبتي») platform.
 * Delegates static font catalog to [BuiltInArabicFontsCatalog] and custom user font storage to [CustomFontStorageManager].
 */
object MaktabatiFontRegistry {

    const val LICENSE_SIL_OFL = "SIL Open Font License 1.1"

    val builtInFonts: List<RegisteredArabicFont>
        get() = BuiltInArabicFontsCatalog.builtInFonts

    val editorialPresets: List<EditorialTypographyPreset>
        get() = BuiltInArabicFontsCatalog.editorialPresets

    val registeredFonts: List<RegisteredArabicFont>
        get() = BuiltInArabicFontsCatalog.builtInFonts + CustomFontStorageManager.userCustomFonts

    private fun normalizeFamilyKey(name: String): String {
        return CustomFontStorageManager.normalizeFamilyKey(name)
    }

    fun findFont(familyName: String?): RegisteredArabicFont? {
        if (familyName.isNullOrBlank()) return null
        val trimmed = familyName.trim()
        if (trimmed.contains("http://", ignoreCase = true) ||
            trimmed.contains("https://", ignoreCase = true) ||
            trimmed.contains("url(", ignoreCase = true) ||
            trimmed.contains("/")
        ) {
            return null
        }
        val key = normalizeFamilyKey(trimmed)
        return registeredFonts.firstOrNull { normalizeFamilyKey(it.familyName) == key }
    }

    fun isApprovedFont(familyName: String?): Boolean = findFont(familyName) != null

    fun validateOrFallback(familyName: String?, defaultFamily: String): String {
        return findFont(familyName)?.cssFamilyName ?: defaultFamily
    }

    fun resolveFallbackStack(
        familyName: String,
        defaultFamily: String = BookTypographyProfile.DEFAULT_BODY_FONT
    ): String {
        return (findFont(familyName) ?: findFont(defaultFamily))?.fallbackStack
            ?: "\"Noto Naskh Arabic\", \"Amiri\", serif"
    }

    fun importUserCustomFont(
        context: Context,
        prefs: SharedPreferences,
        uri: Uri,
        customDisplayName: String? = null
    ): Result<RegisteredArabicFont> {
        return CustomFontStorageManager.importUserCustomFont(context, prefs, uri, customDisplayName)
    }

    fun removeUserCustomFont(
        prefs: SharedPreferences,
        familyName: String
    ) {
        CustomFontStorageManager.removeUserCustomFont(prefs, familyName)
    }

    fun loadUserCustomFonts(context: Context, prefs: SharedPreferences) {
        CustomFontStorageManager.loadUserCustomFonts(context, prefs)
    }

    fun parseManifestTypography(manifestFile: File?): BookTypographyProfile? {
        if (manifestFile == null || !manifestFile.isFile) return null
        return runCatching {
            parseManifestTypographyJson(manifestFile.readText(Charsets.UTF_8))
        }.getOrNull()
    }

    fun parseManifestTypographyJson(jsonText: String): BookTypographyProfile? {
        if (jsonText.isBlank()) return null
        return runCatching {
            val root = JSONObject(jsonText)
            val typoObj = root.optJSONObject("typography") ?: return null
            BookTypographyProfile(
                body = validateOrFallback(
                    typoObj.optString("body", ""),
                    BookTypographyProfile.DEFAULT_BODY_FONT
                ),
                headings = validateOrFallback(
                    typoObj.optString("headings", ""),
                    BookTypographyProfile.DEFAULT_HEADINGS_FONT
                ),
                chapterTitles = validateOrFallback(
                    typoObj.optString("chapterTitles", ""),
                    BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT
                ),
                quotes = validateOrFallback(
                    typoObj.optString("quotes", ""),
                    BookTypographyProfile.DEFAULT_QUOTES_FONT
                ),
                quran = validateOrFallback(
                    typoObj.optString("quran", ""),
                    BookTypographyProfile.DEFAULT_QURAN_FONT
                ),
                interfaceFont = validateOrFallback(
                    typoObj.optString("interface", ""),
                    BookTypographyProfile.DEFAULT_INTERFACE_FONT
                ),
                numbers = validateOrFallback(
                    typoObj.optString("numbers", ""),
                    BookTypographyProfile.DEFAULT_NUMBERS_FONT
                )
            )
        }.getOrNull()
    }

    fun buildOfflineFontFacesCss(baseUrlPrefix: String = "https://maktabati.app/assets"): String {
        val sb = StringBuilder()
        val cleanPrefix = baseUrlPrefix.trimEnd('/')
        val rootDomain = if (cleanPrefix.contains("/assets")) cleanPrefix.substringBefore("/assets") else "https://maktabati.app"

        for (font in registeredFonts) {
            if (font.isUserCustom && !font.ttfFileName.isNullOrBlank()) {
                val ext = font.ttfFileName.substringAfterLast('.', "ttf").lowercase()
                val fmt = when (ext) {
                    "woff2" -> "woff2"
                    "woff" -> "woff"
                    "otf" -> "opentype"
                    else -> "truetype"
                }
                sb.appendLine(
                    """
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/user-fonts/${font.ttfFileName}") format("$fmt");
                      font-weight: 400;
                      font-style: normal;
                      font-display: swap;
                    }
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/user-fonts/${font.ttfFileName}") format("$fmt");
                      font-weight: 700;
                      font-style: normal;
                      font-display: swap;
                    }
                    """.trimIndent()
                )
            } else if (font.weights.isNotEmpty()) {
                for (weightSpec in font.weights) {
                    sb.appendLine(
                        """
                        @font-face {
                          font-family: "${font.cssFamilyName}";
                          src: url("$cleanPrefix/${weightSpec.woff2AssetPath}") format("woff2");
                          font-weight: ${weightSpec.weight};
                          font-style: normal;
                          font-display: swap;
                        }
                        """.trimIndent()
                    )
                }
            } else if (!font.ttfFileName.isNullOrBlank()) {
                sb.appendLine(
                    """
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/fonts/${font.ttfFileName}") format("truetype");
                      font-weight: 400;
                      font-style: normal;
                      font-display: swap;
                    }
                    @font-face {
                      font-family: "${font.cssFamilyName}";
                      src: url("$rootDomain/fonts/${font.ttfFileName}") format("truetype");
                      font-weight: 700;
                      font-style: normal;
                      font-display: swap;
                    }
                    """.trimIndent()
                )
            }
        }
        return sb.toString()
    }

    fun loadUserTypographyPreferences(
        prefs: SharedPreferences,
        context: Context? = null
    ): UserTypographyPreferences {
        if (context != null) {
            loadUserCustomFonts(context, prefs)
        }
        val useCustom = prefs.getBoolean("typo_use_custom", false)
        val presetId = prefs.getString("typo_preset_id", "editorial_narrative") ?: "editorial_narrative"
        val rawSavedPreview = prefs.getString(
            "typo_custom_preview_text",
            UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT
        )?.trim().orEmpty()
        val customPreview = if (rawSavedPreview.isBlank() ||
            rawSavedPreview == UserTypographyPreferences.LEGACY_PREFILLED_PREVIEW_TEXT
        ) {
            UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT
        } else {
            rawSavedPreview
        }

        val profile = BookTypographyProfile(
            body = validateOrFallback(prefs.getString("typo_body", null), BookTypographyProfile.DEFAULT_BODY_FONT),
            headings = validateOrFallback(prefs.getString("typo_headings", null), BookTypographyProfile.DEFAULT_HEADINGS_FONT),
            chapterTitles = validateOrFallback(prefs.getString("typo_chapter_titles", null), BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT),
            quotes = validateOrFallback(prefs.getString("typo_quotes", null), BookTypographyProfile.DEFAULT_QUOTES_FONT),
            quran = validateOrFallback(prefs.getString("typo_quran", null), BookTypographyProfile.DEFAULT_QURAN_FONT),
            interfaceFont = validateOrFallback(prefs.getString("typo_interface", null), BookTypographyProfile.DEFAULT_INTERFACE_FONT),
            numbers = validateOrFallback(prefs.getString("typo_numbers", null), BookTypographyProfile.DEFAULT_NUMBERS_FONT)
        )
        return UserTypographyPreferences(
            useCustomTypography = useCustom,
            presetId = presetId,
            profile = profile,
            customPreviewText = customPreview
        )
    }

    fun saveUserTypographyPreferences(
        prefs: SharedPreferences,
        userPrefs: UserTypographyPreferences
    ) {
        prefs.edit()
            .putBoolean("typo_use_custom", userPrefs.useCustomTypography)
            .putString("typo_preset_id", userPrefs.presetId)
            .putString("typo_custom_preview_text", userPrefs.customPreviewText)
            .putString("typo_body", validateOrFallback(userPrefs.profile.body, BookTypographyProfile.DEFAULT_BODY_FONT))
            .putString("typo_headings", validateOrFallback(userPrefs.profile.headings, BookTypographyProfile.DEFAULT_HEADINGS_FONT))
            .putString("typo_chapter_titles", validateOrFallback(userPrefs.profile.chapterTitles, BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT))
            .putString("typo_quotes", validateOrFallback(userPrefs.profile.quotes, BookTypographyProfile.DEFAULT_QUOTES_FONT))
            .putString("typo_quran", validateOrFallback(userPrefs.profile.quran, BookTypographyProfile.DEFAULT_QURAN_FONT))
            .putString("typo_interface", validateOrFallback(userPrefs.profile.interfaceFont, BookTypographyProfile.DEFAULT_INTERFACE_FONT))
            .putString("typo_numbers", validateOrFallback(userPrefs.profile.numbers, BookTypographyProfile.DEFAULT_NUMBERS_FONT))
            .apply()
    }

    fun buildTypographyProfileCss(profile: BookTypographyProfile, important: Boolean = false): String {
        val imp = if (important) " !important" else ""
        val bodyStack = resolveFallbackStack(profile.body, BookTypographyProfile.DEFAULT_BODY_FONT)
        val headingsStack = resolveFallbackStack(profile.headings, BookTypographyProfile.DEFAULT_HEADINGS_FONT)
        val chapterStack = resolveFallbackStack(profile.chapterTitles, BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT)
        val quotesStack = resolveFallbackStack(profile.quotes, BookTypographyProfile.DEFAULT_QUOTES_FONT)
        val quranStack = resolveFallbackStack(profile.quran, BookTypographyProfile.DEFAULT_QURAN_FONT)
        val interfaceStack = resolveFallbackStack(profile.interfaceFont, BookTypographyProfile.DEFAULT_INTERFACE_FONT)
        val numbersStack = resolveFallbackStack(profile.numbers, BookTypographyProfile.DEFAULT_NUMBERS_FONT)

        return """
            :root {
              --font-body: $bodyStack$imp;
              --font-headings: $headingsStack$imp;
              --font-chapter-titles: $chapterStack$imp;
              --font-quotes: $quotesStack$imp;
              --font-quran: $quranStack$imp;
              --font-interface: $interfaceStack$imp;
              --font-numbers: $numbersStack$imp;
              --reader-family: var(--font-body)$imp;
            }
            html, body, .page, .book-page, .page-body, .page-content, #pageInnerContent,
            p, li, .reading p, .dedication p, .closing p, .subtitle, .step-content {
              font-family: var(--font-body)$imp;
            }
            h1, h2, h3, h4, h5, h6, .page-heading, .section-title, .reading .section-opening,
            .cover-page h1, .cover-title, .dedication h1, .closing h1, .letter-greeting {
              font-family: var(--font-headings)$imp;
            }
            .opener h1, .chapter-opener h1, .chapter-title, .chapter-number, .chapter-badge,
            .toc-item.toc-chapter, .toc-item.toc-part, .letter-envelope-title {
              font-family: var(--font-chapter-titles)$imp;
            }
            .quote-card, .quote-text, .quote-box, .pullquote, .wisdom-pearl, .manal-voice,
            .heart-keepsake-card, .heart-keepsake-card p, .hadith-box, .hadith-box p,
            .hadith-quote, .poetry-stanza, .poetry-shatr, blockquote, q {
              font-family: var(--font-quotes)$imp;
            }
            .quran-verse, .ayah-box, .mushaf-verse, .quran-ayah, .quran-box, .quran-box p, [data-role="quran"] {
              font-family: var(--font-quran)$imp;
            }
            .top-bar, .topbar, .brand, .icon-btn, .nav-btn, .running-header, .eyebrow, .primary-btn,
            .panel-head h2, .field, #searchInput, .search-result, .search-result strong,
            .toc-list, #tocPanelList, .toc-item, .wisdom-badge, .heart-badge, .manal-badge,
            .quote-caption, .ayah-source, .cover-page .author {
              font-family: var(--font-interface)$imp;
            }
            .page-counter, .page-number, .search-result-page, .toc-item small, .search-result small,
            .step-num, .num-badge, [data-role="numbers"] {
              font-family: var(--font-numbers)$imp;
              font-variant-numeric: tabular-nums;
            }
        """.trimIndent()
    }

    fun buildLiveTypographyOverrideJs(userPrefs: UserTypographyPreferences): String {
        if (!userPrefs.useCustomTypography) {
            return """
                (function() {
                    const el = document.getElementById('maktabati-user-typography-override');
                    if (el) el.remove();
                })();
            """.trimIndent()
        }
        val fontFaces = buildOfflineFontFacesCss("https://maktabati.app/assets")
        val css = (fontFaces + "\n" + buildTypographyProfileCss(userPrefs.profile, important = true))
            .replace("\\", "\\\\")
            .replace("`", "\\`")
        return """
            (function() {
                let el = document.getElementById('maktabati-user-typography-override');
                if (!el) {
                    el = document.createElement('style');
                    el.id = 'maktabati-user-typography-override';
                    (document.head || document.documentElement).appendChild(el);
                }
                el.textContent = `$css`;
                if (window.MaktabatiLayoutGuard && typeof window.MaktabatiLayoutGuard.inspectAndHeal === 'function') {
                    window.MaktabatiLayoutGuard.inspectAndHeal();
                }
            })();
        """.trimIndent()
    }

    fun toJsonRegistry(): String {
        val root = JSONObject()
        root.put("version", "2.0.0")
        root.put("OfflineOnly", true)
        val fontsArr = JSONArray()
        for (f in registeredFonts) {
            val fObj = JSONObject()
            fObj.put("familyName", f.familyName)
            fObj.put("arabicDisplayName", f.arabicDisplayName)
            fObj.put("cssFamilyName", f.cssFamilyName)
            fObj.put("folder", "assets/fonts/${f.folderName}/")
            fObj.put("category", f.category.name)
            fObj.put("license", f.license)
            fObj.put("licensePath", "assets/${f.licenseAssetPath}")
            fObj.put("editorialUsage", f.editorialUsage)
            fObj.put("fallbackStack", f.fallbackStack)
            val wArr = JSONArray()
            for (w in f.weights) {
                val wObj = JSONObject()
                wObj.put("weight", w.weight)
                wObj.put("styleName", w.styleName)
                wObj.put("woff2Path", "assets/${w.woff2AssetPath}")
                wArr.put(wObj)
            }
            fObj.put("weights", wArr)
            fontsArr.put(fObj)
        }
        root.put("fonts", fontsArr)
        return root.toString(2)
    }

    fun interceptFontRequest(
        context: Context,
        requestPath: String?,
        bookDir: File? = null
    ): WebResourceResponse? {
        if (requestPath.isNullOrBlank()) return null

        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Cache-Control" to "public, max-age=31536000"
        )

        if (requestPath.startsWith("/user-fonts/")) {
            val fileName = requestPath.substringAfterLast("/")
            val userFontsDir = File(context.filesDir, "user_fonts")
            val targetFile = File(userFontsDir, fileName)
            if (targetFile.isFile && targetFile.canonicalPath.startsWith(userFontsDir.canonicalPath)) {
                val mime = when {
                    fileName.endsWith(".woff2", true) -> "font/woff2"
                    fileName.endsWith(".woff", true) -> "font/woff"
                    fileName.endsWith(".otf", true) -> "font/otf"
                    else -> "font/ttf"
                }
                return runCatching {
                    WebResourceResponse(
                        mime,
                        null,
                        200,
                        "OK",
                        headers,
                        BufferedInputStream(FileInputStream(targetFile))
                    )
                }.getOrNull()
            }
        }

        val fontsSubPath = when {
            requestPath.startsWith("/assets/fonts/") -> requestPath.removePrefix("/assets/")
            requestPath.startsWith("/fonts/") && requestPath.endsWith(".woff2") -> requestPath.removePrefix("/")
            requestPath.contains("/assets/fonts/") -> "fonts/" + requestPath.substringAfter("/assets/fonts/")
            else -> null
        }

        if (fontsSubPath != null) {
            if (bookDir != null && bookDir.isDirectory) {
                val localBookFont = File(bookDir, "assets/$fontsSubPath")
                if (localBookFont.isFile && localBookFont.canonicalPath.startsWith(bookDir.canonicalPath)) {
                    val mime = if (localBookFont.name.endsWith(".woff2", ignoreCase = true)) "font/woff2" else "text/plain"
                    return runCatching {
                        WebResourceResponse(
                            mime,
                            if (mime == "text/plain") "UTF-8" else null,
                            200,
                            "OK",
                            headers,
                            BufferedInputStream(FileInputStream(localBookFont))
                        )
                    }.getOrNull()
                }
            }
            return runCatching {
                val mime = if (fontsSubPath.endsWith(".woff2", ignoreCase = true)) "font/woff2" else "text/plain"
                val stream = context.assets.open(fontsSubPath)
                WebResourceResponse(
                    mime,
                    if (mime == "text/plain") "UTF-8" else null,
                    200,
                    "OK",
                    headers,
                    stream
                )
            }.getOrNull()
        }

        if (requestPath.startsWith("/fonts/") && requestPath.endsWith(".ttf", ignoreCase = true)) {
            val fileName = requestPath.substringAfterLast("/").lowercase()
            val fontResId = when (fileName) {
                "amiri.ttf" -> R.font.amiri
                "amiri_quran.ttf", "amiriquran.ttf" -> R.font.amiri_quran
                "scheherazade_new.ttf", "scheherazadenew.ttf" -> R.font.scheherazade_new
                "lateef.ttf" -> R.font.lateef
                "harmattan.ttf" -> R.font.harmattan
                "cairo.ttf" -> R.font.cairo
                "tajawal.ttf" -> R.font.tajawal
                "almarai.ttf" -> R.font.almarai
                "readex_pro.ttf", "readexpro.ttf" -> R.font.readex_pro
                "markazi_text.ttf", "markazitext.ttf" -> R.font.markazi_text
                "noto_naskh_arabic.ttf", "notonaskharabic.ttf" -> R.font.noto_naskh_arabic
                "aref_ruqaa.ttf", "arefruqaa.ttf" -> R.font.aref_ruqaa
                "marhey.ttf" -> R.font.marhey
                "lemonada.ttf" -> R.font.lemonada
                "mirza.ttf" -> R.font.mirza
                "katibeh.ttf" -> R.font.katibeh
                "vibes.ttf" -> R.font.vibes
                "el_messiri.ttf", "elmessiri.ttf" -> R.font.el_messiri
                "changa.ttf" -> R.font.changa
                "rakkas.ttf" -> R.font.rakkas
                "lalezar.ttf" -> R.font.lalezar
                "ibm_plex_sans_arabic.ttf", "ibmplexsansarabic.ttf" -> R.font.ibm_plex_sans_arabic
                "noto_sans_arabic.ttf", "notosansarabic.ttf" -> R.font.noto_sans_arabic
                "noto_kufi_arabic.ttf", "notokufiarabic.ttf" -> R.font.noto_kufi_arabic
                else -> null
            }
            if (fontResId != null) {
                return runCatching {
                    WebResourceResponse(
                        "font/ttf",
                        null,
                        200,
                        "OK",
                        headers,
                        context.resources.openRawResource(fontResId)
                    )
                }.getOrNull()
            }
        }

        return null
    }
}
