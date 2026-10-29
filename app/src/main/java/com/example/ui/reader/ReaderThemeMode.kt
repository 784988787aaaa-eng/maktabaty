package com.example.ui.reader

enum class ReaderThemeMode(val label: String, val bgHex: String, val textHex: String) {
    ORIGINAL("تصميم الكتاب الأصلي", "", ""),
    SEPIA("ورقي دافئ (Sepia)", "#FBF0D9", "#3D2B1F"),
    NIGHT("ليلي مريح (Dark)", "#121212", "#E0E0E0"),
    LIGHT("نهاري نقي (Light)", "#FFFFFF", "#1E293B")
}

