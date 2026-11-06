package com.maktabati.app.data.converter.model

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.maktabati.app.data.converter.theme.BookVisualTheme
import java.io.File

/**
 * Categories of visual book publishing templates covering all genres and reader tastes.
 */
@Immutable
enum class BookTemplateCategory(
    val id: String,
    val titleAr: String,
    val emoji: String
) {
    ALL("all", "جميع القوالب", "✦"),
    RELIGIOUS("religious", "دينية وإيمانية", "🕌"),
    BEIGE_CLASSIC("beige_classic", "ورق بيج وكلاسيكي", "📜"),
    GIRLS_AESTHETIC("girls_aesthetic", "ذوق الفتيات", "🌸"),
    STORIES_LITERATURE("stories_literature", "قصصية وأدبية", "📖"),
    SELF_DEVELOPMENT("self_development", "تنمية الذات", "🚀"),
    FAMILY_SOCIAL("family_social", "أسرية واجتماعية", "🏡"),
    EDUCATIONAL_ACADEMIC("educational_academic", "تعليمية وبحوث", "🎓"),
    MAGAZINE_MINIMAL("magazine_minimal", "مجلات وبساطة", "📰")
}

/**
 * Defines the structural and editorial CSS styling of a publishing template beyond colors.
 */
@Immutable
enum class PublishingLayoutStyle {
    HERITAGE_ORNATE,
    SOFT_BOTANICAL,
    DAWAH_MOTIVATIONAL,
    CLASSIC_BOOK_PRESS,
    NARRATIVE_STORY,
    ACADEMIC_STRUCTURED,
    EDITORIAL_MAGAZINE,
    PURE_MINIMAL
}

/**
 * Holds the pre-analyzed result of inspecting a text/office document before final HTML generation.
 * For large documents, the full normalized text can be backed by [stagedTextFile] in cacheDir
 * so UI state only holds a lightweight preview excerpt.
 */
@Immutable
data class ConvertibleBookProposal(
    val uri: Uri?,
    val originalFileName: String,
    val formatBadge: String,
    val detectedTitle: String,
    val detectedSubtitle: String,
    val detectedAuthor: String,
    val rawExtractedText: String,
    val isRtl: Boolean,
    val language: String,
    val wordCount: Int,
    val estimatedPages: Int,
    val recommendedTheme: BookVisualTheme = BookVisualTheme.ROSE_HEART,
    val stagedTextFile: File? = null,
    val previewExcerpt: String = ""
) {
    val sourceFormatLabel: String get() = formatBadge.substringBefore(" •").trim()
    val estimatedPageCount: Int get() = estimatedPages

    fun resolveFullExtractedText(): String {
        val file = stagedTextFile
        if (file != null && file.isFile && file.canRead()) {
            runCatching {
                return file.readText(Charsets.UTF_8)
            }
        }
        return rawExtractedText
    }

    fun cleanupStagedFile() {
        runCatching {
            if (stagedTextFile != null && stagedTextFile.exists()) {
                stagedTextFile.delete()
            }
        }
    }
}

@Immutable
data class ConvertedPageItem(
    val tag: String,
    val title: String,
    val type: String, // "cover", "opener", "reading"
    val html: String
)

@Immutable
data class ExtractedDocumentData(
    val bodyText: String,
    val explicitTitle: String? = null,
    val explicitAuthor: String? = null
)
