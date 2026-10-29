package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String = "",
    val author: String = "غير معروف",
    val description: String = "",
    val folderPath: String, // Directory relative to context.filesDir / "books"
    val entryHtmlPath: String, // Relative path inside folderPath to entry HTML
    val shelfId: String? = null,
    val isFavorite: Boolean = false,
    val readingProgress: Float = 0f, // 0.0 to 1.0
    val scrollY: Int = 0,
    val currentPage: Int = 1,
    val pageCount: Int = 1,
    val language: String = "ar", // "ar" or "en"
    val isRtl: Boolean = true,
    val accentRoseHex: String = "#E11D48",
    val accentGoldHex: String = "#D97706",
    val bgPrimaryHex: String = "#1E293B",
    val coverGradientStart: Long = 0xFF1E293B,
    val coverGradientEnd: Long = 0xFF0F172A,
    val coverIcon: String = "menu_book",
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val fileCount: Int = 1,
    val totalSizeBytes: Long = 0L,
    val hasExternalCss: Boolean = false,
    val hasExternalJs: Boolean = false,
    val tableOfContentsJson: String = "[]" // JSON array of TocEntry
)

data class TocEntry(
    val title: String,
    val href: String,
    val level: Int = 1
)
