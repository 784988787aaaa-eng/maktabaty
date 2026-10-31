package com.example.data.backup

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.db.BookDao
import com.example.data.model.Book
import com.example.data.model.Bookmark
import com.example.data.model.Shelf
import com.example.data.typography.MaktabatiFontRegistry
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

sealed class CloudSyncState {
    object Idle : CloudSyncState()
    data class Syncing(val message: String) : CloudSyncState()
    data class Success(val message: String) : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()
}

sealed class SignInOutcome {
    data class Success(val email: String) : SignInOutcome()
    object Cancelled : SignInOutcome()
    data class Error(val arabicMessage: String) : SignInOutcome()
}

class CloudBackupManager(
    private val context: Context,
    private val bookDao: BookDao,
    private val preferences: SharedPreferences
) {
    companion object {
        const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        const val BACKUP_FILE_NAME = "maktabati_cloud_backup.zip"
        const val METADATA_JSON_NAME = "library_metadata.json"

        private const val PREF_CONNECTED_EMAIL = "cloud_connected_google_email"
        private const val PREF_LAST_BACKUP_TIME = "cloud_last_backup_time_text"

        const val MSG_NO_INTERNET = "أنت غير متصل بالإنترنت"
        const val MSG_ERROR_10 = "تعذر إتمام التسجيل حالياً (خطأ رقم 10)"
        const val MSG_GENERIC_CLOUD_ERROR = "تعذر الاتصال بالسحابة حالياً، يرجى المحاولة لاحقاً"
        const val MSG_NO_BACKUP_FOUND = "لا توجد نسخة احتياطية محفوظة في السحابة لهذا الحساب"
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun buildSignInIntent(): Intent {
        return getGoogleSignInClient().signInIntent
    }

    fun getConnectedEmail(): String? {
        val lastSignedIn = runCatching { GoogleSignIn.getLastSignedInAccount(context) }.getOrNull()
        val liveEmail = lastSignedIn?.email
        if (!liveEmail.isNullOrBlank()) {
            if (preferences.getString(PREF_CONNECTED_EMAIL, null) != liveEmail) {
                preferences.edit().putString(PREF_CONNECTED_EMAIL, liveEmail).apply()
            }
            return liveEmail
        }
        return preferences.getString(PREF_CONNECTED_EMAIL, null)?.takeIf { it.isNotBlank() }
    }

    fun getLastBackupTimeText(): String? {
        return preferences.getString(PREF_LAST_BACKUP_TIME, null)?.takeIf { it.isNotBlank() }
    }

    fun isInternetAvailable(): Boolean {
        return runCatching {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
        }.getOrDefault(false)
    }

    /**
     * Strict user-friendly Arabic error filter — never exposes stack traces, SHA-1, or developer messages.
     */
    fun toUserFriendlyArabicError(error: Throwable?): String {
        if (!isInternetAvailable()) {
            return MSG_NO_INTERNET
        }
        if (error == null) return MSG_GENERIC_CLOUD_ERROR

        val causes = generateSequence(error) { it.cause }.toList()
        if (causes.any {
                it is UnknownHostException ||
                    it is ConnectException ||
                    it is SocketTimeoutException ||
                    it is NoRouteToHostException
            }
        ) {
            return MSG_NO_INTERNET
        }

        for (cause in causes) {
            if (cause is ApiException) {
                return when (cause.statusCode) {
                    CommonStatusCodes.DEVELOPER_ERROR, // 10
                    10,
                    GoogleSignInStatusCodes.SIGN_IN_FAILED // 12500
                    -> MSG_ERROR_10

                    CommonStatusCodes.NETWORK_ERROR -> MSG_NO_INTERNET
                    else -> MSG_GENERIC_CLOUD_ERROR
                }
            }
            val rawMessage = cause.message.orEmpty()
            if (rawMessage.contains("10:") ||
                rawMessage.contains("DEVELOPER_ERROR", ignoreCase = true) ||
                rawMessage.contains("statusCode=10")
            ) {
                return MSG_ERROR_10
            }
            if (rawMessage.contains("Unable to resolve host", ignoreCase = true) ||
                rawMessage.contains("Network is unreachable", ignoreCase = true) ||
                rawMessage.contains("Failed to connect", ignoreCase = true)
            ) {
                return MSG_NO_INTERNET
            }
        }

        return MSG_GENERIC_CLOUD_ERROR
    }

    fun handleSignInResult(data: Intent?): SignInOutcome {
        if (!isInternetAvailable()) {
            return SignInOutcome.Error(MSG_NO_INTERNET)
        }
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val email = account?.email
            if (!email.isNullOrBlank()) {
                preferences.edit().putString(PREF_CONNECTED_EMAIL, email).apply()
                SignInOutcome.Success(email)
            } else {
                SignInOutcome.Error(MSG_GENERIC_CLOUD_ERROR)
            }
        } catch (apiEx: ApiException) {
            when (apiEx.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED, // 12501
                CommonStatusCodes.CANCELED,
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> SignInOutcome.Cancelled

                CommonStatusCodes.DEVELOPER_ERROR, // 10
                10,
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> SignInOutcome.Error(MSG_ERROR_10)

                CommonStatusCodes.NETWORK_ERROR -> SignInOutcome.Error(MSG_NO_INTERNET)
                else -> SignInOutcome.Error(toUserFriendlyArabicError(apiEx))
            }
        } catch (t: Throwable) {
            SignInOutcome.Error(toUserFriendlyArabicError(t))
        }
    }

    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            runCatching {
                getGoogleSignInClient().signOut()
            }
            preferences.edit().remove(PREF_CONNECTED_EMAIL).apply()
        }
    }

    /**
     * Packages all books (`filesDir/books/`), custom fonts (`filesDir/user_fonts/`),
     * and database + preferences (`library_metadata.json`) into `maktabati_cloud_backup.zip`,
     * and uploads it to Google Drive `appDataFolder`.
     */
    suspend fun createAndUploadBackup(): Result<String> = withContext(Dispatchers.IO) {
        if (!isInternetAvailable()) {
            return@withContext Result.failure(IllegalStateException(MSG_NO_INTERNET))
        }

        val email = getConnectedEmail()
            ?: return@withContext Result.failure(IllegalStateException(MSG_GENERIC_CLOUD_ERROR))

        try {
            val localBackupFile = packageLibraryToZip()
            val accessToken = fetchDriveAccessToken(email)

            if (!accessToken.isNullOrBlank()) {
                uploadZipToDriveAppData(accessToken, localBackupFile)
            } else {
                return@withContext Result.failure(IllegalStateException(MSG_GENERIC_CLOUD_ERROR))
            }

            val formattedNow = formatArabicTimestamp(System.currentTimeMillis())
            preferences.edit().putString(PREF_LAST_BACKUP_TIME, formattedNow).apply()
            Result.success(formattedNow)
        } catch (t: Throwable) {
            val friendly = if (t is IllegalStateException &&
                (t.message == MSG_NO_INTERNET || t.message == MSG_ERROR_10 || t.message == MSG_GENERIC_CLOUD_ERROR)
            ) {
                t.message!!
            } else {
                toUserFriendlyArabicError(t)
            }
            Result.failure(IllegalStateException(friendly))
        }
    }

    /**
     * Downloads `maktabati_cloud_backup.zip` from Google Drive `appDataFolder`
     * and restores all books, shelves, bookmarks, custom fonts, and preferences immediately.
     */
    suspend fun downloadAndRestoreBackup(): Result<Int> = withContext(Dispatchers.IO) {
        if (!isInternetAvailable()) {
            return@withContext Result.failure(IllegalStateException(MSG_NO_INTERNET))
        }

        val email = getConnectedEmail()
            ?: return@withContext Result.failure(IllegalStateException(MSG_GENERIC_CLOUD_ERROR))

        try {
            val accessToken = fetchDriveAccessToken(email)
                ?: return@withContext Result.failure(IllegalStateException(MSG_GENERIC_CLOUD_ERROR))

            val backupDir = File(context.filesDir, "backup_cache").apply { mkdirs() }
            val downloadedZip = File(backupDir, "restore_$BACKUP_FILE_NAME")

            val foundInCloud = downloadZipFromDriveAppData(accessToken, downloadedZip)
            val sourceZip = when {
                foundInCloud && downloadedZip.exists() && downloadedZip.length() > 0L -> downloadedZip
                else -> {
                    val localMirror = File(backupDir, BACKUP_FILE_NAME)
                    if (localMirror.exists() && localMirror.length() > 0L) {
                        localMirror
                    } else {
                        return@withContext Result.failure(IllegalStateException(MSG_NO_BACKUP_FOUND))
                    }
                }
            }

            val restoredBookCount = unpackAndRestoreLibraryFromZip(sourceZip)
            if (downloadedZip.exists()) {
                downloadedZip.delete()
            }

            val formattedNow = formatArabicTimestamp(System.currentTimeMillis())
            if (getLastBackupTimeText() == null) {
                preferences.edit().putString(PREF_LAST_BACKUP_TIME, formattedNow).apply()
            }

            Result.success(restoredBookCount)
        } catch (t: Throwable) {
            val friendly = if (t is IllegalStateException &&
                (t.message == MSG_NO_INTERNET ||
                    t.message == MSG_ERROR_10 ||
                    t.message == MSG_NO_BACKUP_FOUND ||
                    t.message == MSG_GENERIC_CLOUD_ERROR)
            ) {
                t.message!!
            } else {
                toUserFriendlyArabicError(t)
            }
            Result.failure(IllegalStateException(friendly))
        }
    }

    private fun fetchDriveAccessToken(email: String): String? {
        val lastAccount: GoogleSignInAccount? = runCatching {
            GoogleSignIn.getLastSignedInAccount(context)
        }.getOrNull()

        val account: Account = lastAccount?.account ?: Account(email, "com.google")
        val scopeStr = "oauth2:$DRIVE_APPDATA_SCOPE"
        return GoogleAuthUtil.getToken(context, account, scopeStr)
    }

    private fun findBackupFileIdInDrive(accessToken: String): String? {
        val url =
            "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&q=name%3D'$BACKUP_FILE_NAME'%20and%20trashed%3Dfalse&fields=files(id,name,modifiedTime)&pageSize=5"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("Drive list failed: ${response.code}")
            }
            val bodyStr = response.body?.string().orEmpty()
            val json = JSONObject(bodyStr)
            val files = json.optJSONArray("files") ?: return null
            if (files.length() == 0) return null
            return files.optJSONObject(0)?.optString("id")?.takeIf { it.isNotBlank() }
        }
    }

    private fun uploadZipToDriveAppData(accessToken: String, zipFile: File) {
        val existingFileId = findBackupFileIdInDrive(accessToken)
        val zipMediaType = "application/zip".toMediaType()

        if (existingFileId != null) {
            val patchUrl =
                "https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=media"
            val patchRequest = Request.Builder()
                .url(patchUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .patch(zipFile.asRequestBody(zipMediaType))
                .build()

            okHttpClient.newCall(patchRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Drive patch failed: ${response.code}")
                }
            }
        } else {
            val createUrl =
                "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
            val metadataJson = JSONObject().apply {
                put("name", BACKUP_FILE_NAME)
                put("parents", JSONArray().put("appDataFolder"))
            }.toString()

            val multipartBody = MultipartBody.Builder()
                .setType("multipart/related".toMediaType())
                .addPart(
                    metadataJson.toRequestBody("application/json; charset=utf-8".toMediaType())
                )
                .addPart(
                    zipFile.asRequestBody(zipMediaType)
                )
                .build()

            val postRequest = Request.Builder()
                .url(createUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(multipartBody)
                .build()

            okHttpClient.newCall(postRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Drive create failed: ${response.code}")
                }
            }
        }
    }

    private fun downloadZipFromDriveAppData(accessToken: String, targetFile: File): Boolean {
        val fileId = findBackupFileIdInDrive(accessToken) ?: return false
        val downloadUrl = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
        val request = Request.Builder()
            .url(downloadUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return false
            val body = response.body ?: return false
            targetFile.parentFile?.mkdirs()
            body.byteStream().use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            return targetFile.exists() && targetFile.length() > 0L
        }
    }

    /**
     * Packages `books/`, `user_fonts/`, and `library_metadata.json` into `maktabati_cloud_backup.zip`.
     */
    suspend fun packageLibraryToZip(): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backup_cache").apply { mkdirs() }
        val zipFile = File(backupDir, BACKUP_FILE_NAME)

        val books = bookDao.getAllBooksOnce()
        val shelves = bookDao.getAllShelvesOnce()
        val bookmarks = bookDao.getAllBookmarksOnce()

        val metadataJson = buildMetadataJson(books, shelves, bookmarks)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // 1. Write library_metadata.json
            val metaEntry = ZipEntry(METADATA_JSON_NAME)
            zos.putNextEntry(metaEntry)
            zos.write(metadataJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. Write filesDir/books/
            val booksRoot = File(context.filesDir, "books")
            if (booksRoot.exists() && booksRoot.isDirectory) {
                zipDirectoryRecursive(booksRoot, "books", zos)
            }

            // 3. Write filesDir/user_fonts/
            val fontsRoot = File(context.filesDir, "user_fonts")
            if (fontsRoot.exists() && fontsRoot.isDirectory) {
                zipDirectoryRecursive(fontsRoot, "user_fonts", zos)
            }
        }

        zipFile
    }

    /**
     * Unpacks a backup ZIP archive and restores files, database records, and SharedPreferences.
     */
    suspend fun unpackAndRestoreLibraryFromZip(zipFile: File): Int = withContext(Dispatchers.IO) {
        var metadataJsonString: String? = null

        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val entryName = entry.name.replace('\\', '/')
                if (!entry.isDirectory && !entryName.contains("..")) {
                    when {
                        entryName == METADATA_JSON_NAME -> {
                            metadataJsonString = zis.readBytes().toString(Charsets.UTF_8)
                        }
                        entryName.startsWith("books/") || entryName.startsWith("user_fonts/") -> {
                            val outFile = File(context.filesDir, entryName)
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { fos ->
                                zis.copyTo(fos)
                            }
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val jsonStr = metadataJsonString ?: return@withContext 0
        restoreMetadataJson(jsonStr)
    }

    private fun zipDirectoryRecursive(dir: File, basePrefix: String, zos: ZipOutputStream) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            val entryPath = "$basePrefix/${child.name}"
            if (child.isDirectory) {
                zipDirectoryRecursive(child, entryPath, zos)
            } else if (child.isFile) {
                val entry = ZipEntry(entryPath)
                zos.putNextEntry(entry)
                FileInputStream(child).use { fis ->
                    fis.copyTo(zos)
                }
                zos.closeEntry()
            }
        }
    }

    private fun buildMetadataJson(
        books: List<Book>,
        shelves: List<Shelf>,
        bookmarks: List<Bookmark>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val shelvesArray = JSONArray()
        for (shelf in shelves) {
            shelvesArray.put(
                JSONObject().apply {
                    put("id", shelf.id)
                    put("name", shelf.name)
                    put("iconName", shelf.iconName)
                    put("colorHex", shelf.colorHex)
                    put("sortOrder", shelf.sortOrder)
                    put("createdAt", shelf.createdAt)
                }
            )
        }
        root.put("shelves", shelvesArray)

        val booksArray = JSONArray()
        for (book in books) {
            booksArray.put(
                JSONObject().apply {
                    put("id", book.id)
                    put("title", book.title)
                    put("subtitle", book.subtitle)
                    put("author", book.author)
                    put("description", book.description)
                    put("folderPath", book.folderPath)
                    put("entryHtmlPath", book.entryHtmlPath)
                    put("shelfId", book.shelfId ?: JSONObject.NULL)
                    put("isFavorite", book.isFavorite)
                    put("readingProgress", book.readingProgress.toDouble())
                    put("scrollY", book.scrollY)
                    put("currentPage", book.currentPage)
                    put("pageCount", book.pageCount)
                    put("language", book.language)
                    put("isRtl", book.isRtl)
                    put("accentRoseHex", book.accentRoseHex)
                    put("accentGoldHex", book.accentGoldHex)
                    put("bgPrimaryHex", book.bgPrimaryHex)
                    put("coverGradientStart", book.coverGradientStart)
                    put("coverGradientEnd", book.coverGradientEnd)
                    put("coverIcon", book.coverIcon)
                    put("lastReadTimestamp", book.lastReadTimestamp)
                    put("createdAt", book.createdAt)
                    put("fileCount", book.fileCount)
                    put("totalSizeBytes", book.totalSizeBytes)
                    put("hasExternalCss", book.hasExternalCss)
                    put("hasExternalJs", book.hasExternalJs)
                    put("tableOfContentsJson", book.tableOfContentsJson)
                }
            )
        }
        root.put("books", booksArray)

        val bookmarksArray = JSONArray()
        for (bm in bookmarks) {
            bookmarksArray.put(
                JSONObject().apply {
                    put("id", bm.id)
                    put("bookId", bm.bookId)
                    put("title", bm.title)
                    put("scrollY", bm.scrollY)
                    put("progress", bm.progress.toDouble())
                    put("excerpt", bm.excerpt)
                    put("createdAt", bm.createdAt)
                }
            )
        }
        root.put("bookmarks", bookmarksArray)

        val prefsObj = JSONObject().apply {
            put("isDarkTheme", preferences.getBoolean("isDarkTheme", false))
            put("viewMode", preferences.getString("viewMode", "GRID") ?: "GRID")
            put("gridCardWidthDp", preferences.getFloat("gridCardWidthDp", 112f).toDouble())
            put("typo_use_custom", preferences.getBoolean("typo_use_custom", false))
            put("typo_preset_id", preferences.getString("typo_preset_id", "editorial_narrative") ?: "editorial_narrative")
            put("typo_custom_preview_text", preferences.getString("typo_custom_preview_text", "") ?: "")
            put("typo_role_body", preferences.getString("typo_role_body", "Noto Naskh Arabic") ?: "Noto Naskh Arabic")
            put("typo_role_quran", preferences.getString("typo_role_quran", "Amiri Quran") ?: "Amiri Quran")
            put("typo_role_headings", preferences.getString("typo_role_headings", "Noto Kufi Arabic") ?: "Noto Kufi Arabic")
            put("typo_role_chapter", preferences.getString("typo_role_chapter", "Reem Kufi") ?: "Reem Kufi")
            put("typo_role_quotes", preferences.getString("typo_role_quotes", "Aref Ruqaa") ?: "Aref Ruqaa")
            put("typo_role_interface", preferences.getString("typo_role_interface", "Noto Sans Arabic") ?: "Noto Sans Arabic")
            put("typo_role_numbers", preferences.getString("typo_role_numbers", "Readex Pro") ?: "Readex Pro")
            put("user_custom_fonts_json", preferences.getString("user_custom_fonts_json", "[]") ?: "[]")
        }
        root.put("preferences", prefsObj)

        return root.toString()
    }

    private suspend fun restoreMetadataJson(jsonStr: String): Int {
        val root = JSONObject(jsonStr)

        val shelvesArray = root.optJSONArray("shelves") ?: JSONArray()
        for (i in 0 until shelvesArray.length()) {
            val obj = shelvesArray.optJSONObject(i) ?: continue
            val shelf = Shelf(
                id = obj.optString("id"),
                name = obj.optString("name"),
                iconName = obj.optString("iconName", "shelves"),
                colorHex = obj.optString("colorHex", "#3B82F6"),
                sortOrder = obj.optInt("sortOrder", 0),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            if (shelf.id.isNotBlank()) {
                bookDao.insertShelf(shelf)
            }
        }

        val booksArray = root.optJSONArray("books") ?: JSONArray()
        var restoredBooksCount = 0
        for (i in 0 until booksArray.length()) {
            val obj = booksArray.optJSONObject(i) ?: continue
            val shelfIdVal = if (obj.isNull("shelfId")) null else obj.optString("shelfId").takeIf { it.isNotBlank() }
            val book = Book(
                id = obj.optString("id"),
                title = obj.optString("title"),
                subtitle = obj.optString("subtitle", ""),
                author = obj.optString("author", "غير معروف"),
                description = obj.optString("description", ""),
                folderPath = obj.optString("folderPath"),
                entryHtmlPath = obj.optString("entryHtmlPath"),
                shelfId = shelfIdVal,
                isFavorite = obj.optBoolean("isFavorite", false),
                readingProgress = obj.optDouble("readingProgress", 0.0).toFloat(),
                scrollY = obj.optInt("scrollY", 0),
                currentPage = obj.optInt("currentPage", 1),
                pageCount = obj.optInt("pageCount", 1),
                language = obj.optString("language", "ar"),
                isRtl = obj.optBoolean("isRtl", true),
                accentRoseHex = obj.optString("accentRoseHex", "#E11D48"),
                accentGoldHex = obj.optString("accentGoldHex", "#D97706"),
                bgPrimaryHex = obj.optString("bgPrimaryHex", "#1E293B"),
                coverGradientStart = obj.optLong("coverGradientStart", 0xFF1E293B),
                coverGradientEnd = obj.optLong("coverGradientEnd", 0xFF0F172A),
                coverIcon = obj.optString("coverIcon", "menu_book"),
                lastReadTimestamp = obj.optLong("lastReadTimestamp", System.currentTimeMillis()),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                fileCount = obj.optInt("fileCount", 1),
                totalSizeBytes = obj.optLong("totalSizeBytes", 0L),
                hasExternalCss = obj.optBoolean("hasExternalCss", false),
                hasExternalJs = obj.optBoolean("hasExternalJs", false),
                tableOfContentsJson = obj.optString("tableOfContentsJson", "[]")
            )
            if (book.id.isNotBlank()) {
                bookDao.insertBook(book)
                restoredBooksCount++
            }
        }

        val bookmarksArray = root.optJSONArray("bookmarks") ?: JSONArray()
        for (i in 0 until bookmarksArray.length()) {
            val obj = bookmarksArray.optJSONObject(i) ?: continue
            val bookmark = Bookmark(
                id = obj.optString("id"),
                bookId = obj.optString("bookId"),
                title = obj.optString("title"),
                scrollY = obj.optInt("scrollY", 0),
                progress = obj.optDouble("progress", 0.0).toFloat(),
                excerpt = obj.optString("excerpt", ""),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis())
            )
            if (bookmark.id.isNotBlank() && bookmark.bookId.isNotBlank()) {
                bookDao.insertBookmark(bookmark)
            }
        }

        val prefsObj = root.optJSONObject("preferences")
        if (prefsObj != null) {
            preferences.edit().apply {
                if (prefsObj.has("isDarkTheme")) putBoolean("isDarkTheme", prefsObj.optBoolean("isDarkTheme", false))
                if (prefsObj.has("viewMode")) putString("viewMode", prefsObj.optString("viewMode", "GRID"))
                if (prefsObj.has("gridCardWidthDp")) putFloat("gridCardWidthDp", prefsObj.optDouble("gridCardWidthDp", 112.0).toFloat())
                if (prefsObj.has("typo_use_custom")) putBoolean("typo_use_custom", prefsObj.optBoolean("typo_use_custom", false))
                if (prefsObj.has("typo_preset_id")) putString("typo_preset_id", prefsObj.optString("typo_preset_id", "editorial_narrative"))
                if (prefsObj.has("typo_custom_preview_text")) putString("typo_custom_preview_text", prefsObj.optString("typo_custom_preview_text", ""))
                if (prefsObj.has("typo_role_body")) putString("typo_role_body", prefsObj.optString("typo_role_body", "Noto Naskh Arabic"))
                if (prefsObj.has("typo_role_quran")) putString("typo_role_quran", prefsObj.optString("typo_role_quran", "Amiri Quran"))
                if (prefsObj.has("typo_role_headings")) putString("typo_role_headings", prefsObj.optString("typo_role_headings", "Noto Kufi Arabic"))
                if (prefsObj.has("typo_role_chapter")) putString("typo_role_chapter", prefsObj.optString("typo_role_chapter", "Reem Kufi"))
                if (prefsObj.has("typo_role_quotes")) putString("typo_role_quotes", prefsObj.optString("typo_role_quotes", "Aref Ruqaa"))
                if (prefsObj.has("typo_role_interface")) putString("typo_role_interface", prefsObj.optString("typo_role_interface", "Noto Sans Arabic"))
                if (prefsObj.has("typo_role_numbers")) putString("typo_role_numbers", prefsObj.optString("typo_role_numbers", "Readex Pro"))
                if (prefsObj.has("user_custom_fonts_json")) putString("user_custom_fonts_json", prefsObj.optString("user_custom_fonts_json", "[]"))
            }.apply()
            MaktabatiFontRegistry.loadUserCustomFonts(context, preferences)
        }

        return restoredBooksCount
    }

    private fun formatArabicTimestamp(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd • hh:mm a", Locale("ar"))
        return sdf.format(Date(epochMillis))
    }
}
