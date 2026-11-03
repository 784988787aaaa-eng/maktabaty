package com.maktabati.app.ui.reader

enum class ReaderThemeMode(val label: String, val labelEn: String, val bgHex: String, val textHex: String) {
    ORIGINAL("تصميم الكتاب الأصلي", "Original Book Design", "", ""),
    SEPIA("ورقي دافئ (Sepia)", "Warm Sepia Paper", "#FBF0D9", "#3D2B1F"),
    NIGHT("ليلي مريح (Dark)", "Comfortable Night (Dark)", "#121212", "#E0E0E0"),
    LIGHT("نهاري نقي (Light)", "Pure Daylight (Light)", "#FFFFFF", "#1E293B")
}
