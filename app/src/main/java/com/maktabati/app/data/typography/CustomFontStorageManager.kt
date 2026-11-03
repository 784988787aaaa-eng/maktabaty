package com.maktabati.app.data.typography

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import com.maktabati.app.R
import com.maktabati.app.data.typography.model.FontCategory
import com.maktabati.app.data.typography.model.RegisteredArabicFont
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Manages user-imported custom fonts (.ttf / .otf / .woff2) persisted on device
 * in `filesDir/user_fonts` and `SharedPreferences`.
 */
object CustomFontStorageManager {

    private const val USER_FONTS_PREFS_KEY = "typo_user_custom_fonts_json"
    private val HYPHEN_UNDERSCORE_REGEX = Regex("[_\\-]+")
    private val SLUG_SANITIZE_REGEX = Regex("[^a-z0-9\\u0600-\\u06FF]+")
    private val WHITESPACE_REGEX = Regex("\\s+")

    internal val userCustomFonts = mutableListOf<RegisteredArabicFont>()

    fun normalizeFamilyKey(name: String): String {
        return name.trim().lowercase()
            .replace(HYPHEN_UNDERSCORE_REGEX, " ")
            .replace(WHITESPACE_REGEX, " ")
    }

    fun importUserCustomFont(
        context: Context,
        prefs: SharedPreferences,
        uri: Uri,
        customDisplayName: String? = null
    ): Result<RegisteredArabicFont> {
        return runCatching {
            val rawName = queryDisplayName(context, uri) ?: "custom_font_${System.currentTimeMillis()}.ttf"
            val ext = rawName.substringAfterLast('.', "ttf").lowercase()
            require(ext in setOf("ttf", "otf", "woff2", "woff")) {
                "يرجى اختيار ملف خط صالح بصيغة TTF أو OTF أو WOFF2"
            }

            val baseTitle = customDisplayName?.trim()?.takeIf { it.isNotEmpty() }
                ?: rawName.substringBeforeLast('.')
                    .replace(HYPHEN_UNDERSCORE_REGEX, " ")
                    .trim()
                    .ifEmpty { "خط مخصص ${userCustomFonts.size + 1}" }

            val sanitizedSlug = baseTitle.lowercase()
                .replace(SLUG_SANITIZE_REGEX, "_")
                .trim('_')
                .ifEmpty { "user_font_${System.currentTimeMillis()}" }

            val userFontsDir = File(context.filesDir, "user_fonts").apply { mkdirs() }
            val fileName = "${sanitizedSlug}.$ext"
            val destFile = File(userFontsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: error("تعذر قراءة ملف الخط المحدد")

            require(destFile.length() > 256) { "ملف الخط فارغ أو غير صالح" }

            val cssFamily = "UserFont_$sanitizedSlug"
            val newFont = RegisteredArabicFont(
                familyName = cssFamily,
                arabicDisplayName = "$baseTitle (خطي المضاف)",
                cssFamilyName = cssFamily,
                folderName = "user-$sanitizedSlug",
                weights = emptyList(),
                resFontId = R.font.noto_naskh_arabic,
                ttfFileName = fileName,
                category = FontCategory.USER_CUSTOM,
                editorialUsage = "خط مخصص أضافه القارئ من جهازه",
                fallbackStack = "\"$cssFamily\", \"Noto Naskh Arabic\", \"Amiri\", serif",
                isUserCustom = true,
                customFilePath = destFile.absolutePath
            )

            userCustomFonts.removeAll { normalizeFamilyKey(it.familyName) == normalizeFamilyKey(cssFamily) }
            userCustomFonts.add(0, newFont)
            persistUserCustomFonts(prefs)
            newFont
        }
    }

    fun removeUserCustomFont(
        prefs: SharedPreferences,
        familyName: String
    ) {
        val target = userCustomFonts.firstOrNull { it.familyName == familyName } ?: return
        target.customFilePath?.let { runCatching { File(it).delete() } }
        userCustomFonts.remove(target)
        persistUserCustomFonts(prefs)
    }

    fun loadUserCustomFonts(context: Context, prefs: SharedPreferences) {
        val rawJson = prefs.getString(USER_FONTS_PREFS_KEY, null) ?: return
        runCatching {
            val arr = JSONArray(rawJson)
            val loaded = mutableListOf<RegisteredArabicFont>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val path = obj.optString("customFilePath", "")
                val file = File(path)
                if (path.isNotBlank() && file.isFile) {
                    val cssFamily = obj.getString("familyName")
                    val display = obj.getString("arabicDisplayName")
                    val fileName = obj.getString("ttfFileName")
                    loaded.add(
                        RegisteredArabicFont(
                            familyName = cssFamily,
                            arabicDisplayName = display,
                            cssFamilyName = cssFamily,
                            folderName = obj.optString("folderName", "user-font"),
                            weights = emptyList(),
                            resFontId = R.font.noto_naskh_arabic,
                            ttfFileName = fileName,
                            category = FontCategory.USER_CUSTOM,
                            editorialUsage = "خط مخصص أضافه القارئ من جهازه",
                            fallbackStack = "\"$cssFamily\", \"Noto Naskh Arabic\", \"Amiri\", serif",
                            isUserCustom = true,
                            customFilePath = file.absolutePath
                        )
                    )
                }
            }
            userCustomFonts.clear()
            userCustomFonts.addAll(loaded)
        }
    }

    private fun persistUserCustomFonts(prefs: SharedPreferences) {
        val arr = JSONArray()
        for (f in userCustomFonts) {
            val obj = JSONObject()
            obj.put("familyName", f.familyName)
            obj.put("arabicDisplayName", f.arabicDisplayName)
            obj.put("folderName", f.folderName)
            obj.put("ttfFileName", f.ttfFileName ?: "")
            obj.put("customFilePath", f.customFilePath ?: "")
            arr.put(obj)
        }
        prefs.edit().putString(USER_FONTS_PREFS_KEY, arr.toString()).apply()
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
            }
        }.getOrNull() ?: uri.lastPathSegment
    }
}
