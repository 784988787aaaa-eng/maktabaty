package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey val id: String,
    val bookId: String,
    val title: String,
    val scrollY: Int,
    val progress: Float,
    val excerpt: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
