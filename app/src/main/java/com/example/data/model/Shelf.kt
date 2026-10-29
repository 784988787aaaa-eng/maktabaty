package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shelves")
data class Shelf(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String = "shelves",
    val colorHex: String = "#3B82F6",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
