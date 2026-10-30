package com.example.data.typography

import android.content.Context
import android.webkit.WebResourceResponse
import com.example.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream

data class FontWeightAsset(
    val weight: Int,
    val styleName: String,
    val woff2AssetPath: String
)

data class RegisteredArabicFont(
    val familyName: String,
    val cssFamilyName: String,
    val folderName: String,
    val weights: List<FontWeightAsset>,
    val resFontId: Int,
    val license: String,
    val licenseAssetPath: String,
    val editorialUsage: String,
    val fallbackStack: String
)

data class BookTypographyProfile(
    val body: String = DEFAULT_BODY_FONT,
    val headings: String = DEFAULT_HEADINGS_FONT,
    val chapterTitles: String = DEFAULT_CHAPTER_TITLES_FONT,
    val quotes: String = DEFAULT_QUOTES_FONT,
    val interfaceFont: String = DEFAULT_INTERFACE_FONT,
    val numbers: String = DEFAULT_NUMBERS_FONT
) {
    companion object {
        const val DEFAULT_BODY_FONT = "Noto Naskh Arabic"
        const val DEFAULT_HEADINGS_FONT = "Aref Ruqaa"
        const val DEFAULT_CHAPTER_TITLES_FONT = "Reem Kufi"
        const val DEFAULT_QUOTES_FONT = "Amiri"
        const val DEFAULT_INTERFACE_FONT = "Noto Sans Arabic"
        const val DEFAULT_NUMBERS_FONT = "Noto Sans Arabic"
    }
}

/**
 * Central Arabic Typography Registry for the Maktabati («مكتبتي») platform.
 *
 * Defines the 7 approved offline Arabic font families, their exact static WOFF2 weights,
 * SIL Open Font License 1.1 paths, editorial roles, and fallback stacks.
 * Validates per-book `manifest.json` typography profiles and rejects any unregistered or external font.
 */
object MaktabatiFontRegistry {

    const val LICENSE_SIL_OFL = "SIL Open Font License 1.1"

    val registeredFonts: List<RegisteredArabicFont> = listOf(
        RegisteredArabicFont(
            familyName = "Amiri",
            cssFamilyName = "Amiri",
            folderName = "amiri",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/amiri/amiri-400.woff2"),
                FontWeightAsset(700, "Bold", "fonts/amiri/amiri-700.woff2")
            ),
            resFontId = R.font.amiri,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/amiri/OFL.txt",
            editorialUsage = "النصوص الأدبية والكتب الكلاسيكية والاقتباسات",
            fallbackStack = "\"Amiri\", \"Noto Naskh Arabic\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Noto Naskh Arabic",
            cssFamilyName = "Noto Naskh Arabic",
            folderName = "noto-naskh-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-naskh-arabic/noto-naskh-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-naskh-arabic/noto-naskh-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-naskh-arabic/noto-naskh-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-naskh-arabic/noto-naskh-arabic-700.woff2")
            ),
            resFontId = R.font.noto_naskh_arabic,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/noto-naskh-arabic/OFL.txt",
            editorialUsage = "نصوص المتن الطويلة والقراءة السردية",
            fallbackStack = "\"Noto Naskh Arabic\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Aref Ruqaa",
            cssFamilyName = "Aref Ruqaa",
            folderName = "aref-ruqaa",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/aref-ruqaa/aref-ruqaa-400.woff2"),
                FontWeightAsset(700, "Bold", "fonts/aref-ruqaa/aref-ruqaa-700.woff2")
            ),
            resFontId = R.font.aref_ruqaa,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/aref-ruqaa/OFL.txt",
            editorialUsage = "العناوين الأدبية والاقتباسات",
            fallbackStack = "\"Aref Ruqaa\", \"Amiri\", serif"
        ),
        RegisteredArabicFont(
            familyName = "Reem Kufi",
            cssFamilyName = "Reem Kufi",
            folderName = "reem-kufi",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/reem-kufi/reem-kufi-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/reem-kufi/reem-kufi-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/reem-kufi/reem-kufi-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/reem-kufi/reem-kufi-700.woff2")
            ),
            resFontId = R.font.reem_kufi,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/reem-kufi/OFL.txt",
            editorialUsage = "عناوين الفصول والأبواب",
            fallbackStack = "\"Reem Kufi\", \"Noto Kufi Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "IBM Plex Sans Arabic",
            cssFamilyName = "IBM Plex Sans Arabic",
            folderName = "ibm-plex-sans-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/ibm-plex-sans-arabic/ibm-plex-sans-arabic-700.woff2")
            ),
            resFontId = R.font.ibm_plex_sans_arabic,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/ibm-plex-sans-arabic/OFL.txt",
            editorialUsage = "الكتب الفكرية والمعرفية المعاصرة",
            fallbackStack = "\"IBM Plex Sans Arabic\", \"Noto Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Noto Sans Arabic",
            cssFamilyName = "Noto Sans Arabic",
            folderName = "noto-sans-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-sans-arabic/noto-sans-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-sans-arabic/noto-sans-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-sans-arabic/noto-sans-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-sans-arabic/noto-sans-arabic-700.woff2")
            ),
            resFontId = R.font.noto_sans_arabic,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/noto-sans-arabic/OFL.txt",
            editorialUsage = "واجهة التطبيق والفهارس والبيانات الوصفية والأرقام",
            fallbackStack = "\"Noto Sans Arabic\", \"IBM Plex Sans Arabic\", sans-serif"
        ),
        RegisteredArabicFont(
            familyName = "Noto Kufi Arabic",
            cssFamilyName = "Noto Kufi Arabic",
            folderName = "noto-kufi-arabic",
            weights = listOf(
                FontWeightAsset(400, "Regular", "fonts/noto-kufi-arabic/noto-kufi-arabic-400.woff2"),
                FontWeightAsset(500, "Medium", "fonts/noto-kufi-arabic/noto-kufi-arabic-500.woff2"),
                FontWeightAsset(600, "SemiBold", "fonts/noto-kufi-arabic/noto-kufi-arabic-600.woff2"),
                FontWeightAsset(700, "Bold", "fonts/noto-kufi-arabic/noto-kufi-arabic-700.woff2")
            ),
            resFontId = R.font.noto_kufi_arabic,
            license = LICENSE_SIL_OFL,
            licenseAssetPath = "fonts/noto-kufi-arabic/OFL.txt",
            editorialUsage = "العناوين الحديثة والتصنيفات",
            fallbackStack = "\"Noto Kufi Arabic\", \"Reem Kufi\", sans-serif"
        )
    )

    private val fontsByNormalizedName: Map<String, RegisteredArabicFont> =
        registeredFonts.associateBy { normalizeFamilyKey(it.familyName) }

    private fun normalizeFamilyKey(name: String): String {
        return name.trim().lowercase().replace(Regex("[_\\-]+"), " ").replace(Regex("\\s+"), " ")
    }

    fun findFont(familyName: String?): RegisteredArabicFont? {
        if (familyName.isNullOrBlank()) return null
        val trimmed = familyName.trim()
        // Reject external URLs or paths
        if (trimmed.contains("http://", ignoreCase = true) ||
            trimmed.contains("https://", ignoreCase = true) ||
            trimmed.contains("url(", ignoreCase = true) ||
            trimmed.contains("/")
        ) {
            return null
        }
        return fontsByNormalizedName[normalizeFamilyKey(trimmed)]
    }

    fun isApprovedFont(familyName: String?): Boolean = findFont(familyName) != null

    fun validateOrFallback(familyName: String?, defaultFamily: String): String {
        return findFont(familyName)?.cssFamilyName ?: defaultFamily
    }

    fun resolveFallbackStack(familyName: String, defaultFamily: String = BookTypographyProfile.DEFAULT_BODY_FONT): String {
        return (findFont(familyName) ?: findFont(defaultFamily))?.fallbackStack
            ?: "\"Noto Naskh Arabic\", \"Amiri\", serif"
    }

    /**
     * Parses and validates a book's `manifest.json` typography profile if present in `bookDir`,
     * or from raw JSON text. Rejects any unknown or external font and substitutes the safe role default.
     */
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

    /**
     * Builds exact `@font-face` declarations for all 7 registered Arabic font families
     * using their real WOFF2 weights (never mapping multiple weights to a single static file).
     */
    fun buildOfflineFontFacesCss(baseUrlPrefix: String = "https://maktabati.app/assets"): String {
        val sb = StringBuilder()
        val cleanPrefix = baseUrlPrefix.trimEnd('/')
        for (font in registeredFonts) {
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
        }
        return sb.toString()
    }

    /**
     * Builds the CSS custom property block and role bindings for a book's validated `BookTypographyProfile`.
     */
    fun buildTypographyProfileCss(profile: BookTypographyProfile): String {
        val bodyStack = resolveFallbackStack(profile.body, BookTypographyProfile.DEFAULT_BODY_FONT)
        val headingsStack = resolveFallbackStack(profile.headings, BookTypographyProfile.DEFAULT_HEADINGS_FONT)
        val chapterStack = resolveFallbackStack(profile.chapterTitles, BookTypographyProfile.DEFAULT_CHAPTER_TITLES_FONT)
        val quotesStack = resolveFallbackStack(profile.quotes, BookTypographyProfile.DEFAULT_QUOTES_FONT)
        val interfaceStack = resolveFallbackStack(profile.interfaceFont, BookTypographyProfile.DEFAULT_INTERFACE_FONT)
        val numbersStack = resolveFallbackStack(profile.numbers, BookTypographyProfile.DEFAULT_NUMBERS_FONT)

        return """
            :root {
              --font-body: $bodyStack;
              --font-headings: $headingsStack;
              --font-chapter-titles: $chapterStack;
              --font-quotes: $quotesStack;
              --font-interface: $interfaceStack;
              --font-numbers: $numbersStack;
              --reader-family: var(--font-body);
            }
            html, body, .page, .reading p, .dedication p, .closing p {
              font-family: var(--font-body);
            }
            h1, h2, h3, .reading .section-opening, .cover-page h1, .dedication h1, .closing h1 {
              font-family: var(--font-headings);
            }
            .opener h1, .chapter-number, .toc-item.toc-chapter {
              font-family: var(--font-chapter-titles);
            }
            .quote-card, .quote-text, .pullquote, .wisdom-pearl, .manal-voice, blockquote {
              font-family: var(--font-quotes);
            }
            .top-bar, .brand, .icon-btn, .nav-btn, .running-header, .eyebrow, .primary-btn,
            .panel-head h2, .field, #searchInput, .search-result, .toc-item, .wisdom-badge,
            .manal-badge, .quote-caption, .cover-page .author {
              font-family: var(--font-interface);
            }
            .page-counter, .toc-item small, .search-result small {
              font-family: var(--font-numbers);
              font-variant-numeric: tabular-nums;
            }
        """.trimIndent()
    }

    /**
     * Serializes the complete Font Registry into JSON for inspection and documentation.
     */
    fun toJsonRegistry(): String {
        val root = JSONObject()
        root.put("version", "1.0.0")
        root.put("OfflineOnly", true)
        val fontsArr = JSONArray()
        for (f in registeredFonts) {
            val fObj = JSONObject()
            fObj.put("familyName", f.familyName)
            fObj.put("cssFamilyName", f.cssFamilyName)
            fObj.put("folder", "assets/fonts/${f.folderName}/")
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

    /**
     * Intercepts font requests from WebView (for both `https://maktabati.app/assets/fonts/...`,
     * `https://maktabati.app/fonts/...`, and book-relative `/books/<bookId>/assets/fonts/...`)
     * and serves them 100% offline from the book's local folder or the app's bundled `assets/fonts/`.
     */
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

        // 1. Direct WOFF2 or OFL.txt under /assets/fonts/... or /books/<id>/assets/fonts/...
        val fontsSubPath = when {
            requestPath.startsWith("/assets/fonts/") -> requestPath.removePrefix("/assets/")
            requestPath.startsWith("/fonts/") && requestPath.endsWith(".woff2") -> requestPath.removePrefix("/")
            requestPath.contains("/assets/fonts/") -> "fonts/" + requestPath.substringAfter("/assets/fonts/")
            else -> null
        }

        if (fontsSubPath != null) {
            // Check book-local assets/fonts/... first if bookDir is provided
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
            // Serve from bundled Android app assets/fonts/...
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

        // 2. TTF resource requests under /fonts/<name>.ttf
        if (requestPath.startsWith("/fonts/") && requestPath.endsWith(".ttf", ignoreCase = true)) {
            val fileName = requestPath.substringAfterLast("/").lowercase()
            val fontResId = when (fileName) {
                "amiri.ttf" -> R.font.amiri
                "cairo.ttf" -> R.font.cairo
                "noto_naskh_arabic.ttf", "notonaskharabic.ttf" -> R.font.noto_naskh_arabic
                "aref_ruqaa.ttf", "arefruqaa.ttf" -> R.font.aref_ruqaa
                "reem_kufi.ttf", "reemkufi.ttf" -> R.font.reem_kufi
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
