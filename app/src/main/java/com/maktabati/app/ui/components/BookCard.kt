package com.maktabati.app.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maktabati.app.data.importer.HtmlBookParser
import com.maktabati.app.data.importer.HtmlCoverSnapshotGenerator
import com.maktabati.app.data.model.Book
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.theme.RoyalIndigo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object CoverBitmapCache {
    private val lruCache = object : LruCache<String, ImageBitmap>(64) {}

    fun get(key: String): ImageBitmap? = synchronized(lruCache) { lruCache.get(key) }

    fun put(key: String, bitmap: ImageBitmap) = synchronized(lruCache) { lruCache.put(key, bitmap) }
}

@Composable
private fun rememberCachedCoverBitmap(
    context: Context,
    folderPath: String,
    snapshotVersion: Int
): ImageBitmap? {
    val snapshotFile = remember(context, folderPath) {
        File(File(context.filesDir, "books/$folderPath"), HtmlCoverSnapshotGenerator.SNAPSHOT_FILE_NAME)
    }
    val snapshotPath = snapshotFile.absolutePath
    val cacheKey = "$snapshotPath#$snapshotVersion"
    val cachedBitmap = remember(cacheKey) { CoverBitmapCache.get(cacheKey) }

    val loadedBitmap by produceState<ImageBitmap?>(
        initialValue = cachedBitmap,
        key1 = snapshotPath,
        key2 = snapshotVersion
    ) {
        if (cachedBitmap != null) {
            value = cachedBitmap
            return@produceState
        }
        val decoded = withContext(Dispatchers.IO) {
            if (snapshotFile.isFile && snapshotFile.length() > 128L) {
                runCatching { BitmapFactory.decodeFile(snapshotPath)?.asImageBitmap() }.getOrNull()
            } else {
                null
            }
        }
        if (decoded != null) {
            CoverBitmapCache.put(cacheKey, decoded)
        }
        value = decoded
    }
    return loadedBitmap
}

@Composable
fun LuxuryGeneratedBookCover(
    book: Book,
    scaleFactor: Float,
    coverFrameColor: Color,
    coverEmblemColor: Color,
    coverTitleColor: Color,
    coverSubtitleColor: Color,
    modifier: Modifier = Modifier
) {
    val s = scaleFactor.coerceIn(0.68f, 1.25f)
    val cleanTitle = remember(book.title) { HtmlCoverSnapshotGenerator.sanitizeCoverTitle(book.title) }
    val cleanSubtitle = remember(book.subtitle, cleanTitle) {
        HtmlCoverSnapshotGenerator.sanitizeCoverSubtitle(book.subtitle, cleanTitle)
    }
    val cleanAuthor = remember(book.author, cleanTitle, cleanSubtitle) {
        HtmlBookParser.sanitizeExtractedAuthor(book.author, cleanTitle, cleanSubtitle)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Outer & Inner Frame matching 544x800 geometry
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (11f * s).dp,
                    end = (8f * s).dp,
                    top = (8f * s).dp,
                    bottom = (9f * s).dp
                )
                .border(
                    width = 1.2.dp,
                    color = coverFrameColor,
                    shape = RoundedCornerShape((7f * s).dp)
                )
                .padding((3.5f * s).dp)
                .border(
                    width = 0.7.dp,
                    color = coverFrameColor.copy(alpha = 0.65f),
                    shape = RoundedCornerShape((5f * s).dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = (18f * s).dp,
                    end = (14f * s).dp,
                    top = (16f * s).dp,
                    bottom = (16f * s).dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (book.isRtl) "✦ طبعة رقمية فاخرة ✦" else "✦ LUXURY EDITION ✦",
                color = coverEmblemColor.copy(alpha = 0.85f),
                fontSize = (7.5f * s).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = coverEmblemColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    coverEmblemColor.copy(alpha = 0.55f)
                ),
                modifier = Modifier.size((36f * s).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getBookIcon(book.coverIcon),
                        contentDescription = null,
                        tint = coverEmblemColor,
                        modifier = Modifier.size((18f * s).dp)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = cleanTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = coverTitleColor,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = (12.5f * s).sp,
                    lineHeight = (16f * s).sp
                )

                if (cleanSubtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height((3f * s).dp))
                    Text(
                        text = cleanSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = coverSubtitleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = (9f * s).sp,
                        lineHeight = (12f * s).sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "✦   ✦   ✦",
                    color = coverEmblemColor.copy(alpha = 0.75f),
                    fontSize = (7f * s).sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height((2f * s).dp))
                Text(
                    text = cleanAuthor.ifBlank { if (book.isRtl) "إصدارات مكتبتي الفاخرة" else "Maktabati Editions" },
                    color = coverEmblemColor.copy(alpha = 0.9f),
                    fontSize = (7.5f * s).sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

fun getBookIcon(name: String): ImageVector {
    return when (name) {
        "history_edu" -> Icons.Default.HistoryEdu
        "public" -> Icons.Default.Public
        "terminal" -> Icons.Default.Terminal
        "code" -> Icons.Default.Code
        "auto_stories" -> Icons.Default.AutoStories
        else -> Icons.Default.MenuBook
    }
}

fun parseSafeColor(hex: String, fallback: Color): Color {
    return try {
        Color(HtmlBookParser.parseHexToColorLong(hex, 0xFFD97706))
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun BookGridItem(
    book: Book,
    shelfName: String?,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDetailsClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    scaleFactor: Float = 1f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isEnUi = strings.language == AppLanguage.EN
    val snapshotVersion by HtmlCoverSnapshotGenerator.snapshotVersion.collectAsStateWithLifecycle()
    val coverBitmap = rememberCachedCoverBitmap(
        context = context,
        folderPath = book.folderPath,
        snapshotVersion = snapshotVersion
    )

    var menuExpanded by remember { mutableStateOf(false) }
    val s = scaleFactor.coerceIn(0.68f, 1.25f)
    val goldColor = parseSafeColor(book.accentGoldHex, Color(0xFFF59E0B))
    val roseColor = parseSafeColor(book.accentRoseHex, Color(0xFF5B3A29))
    val bgColor = parseSafeColor(book.bgPrimaryHex, Color(book.coverGradientStart))

    val isLightCover = bgColor.luminance() > 0.48f
    val coverGradientColors = if (isLightCover) {
        listOf(
            bgColor,
            lerp(bgColor, roseColor, 0.08f),
            lerp(bgColor, roseColor, 0.15f)
        )
    } else {
        listOf(
            bgColor,
            lerp(bgColor, roseColor, 0.42f),
            lerp(bgColor, roseColor, 0.65f)
        )
    }

    val coverTitleColor = if (isLightCover) {
        if (roseColor.luminance() < 0.58f) roseColor else Color(0xFF2D1A22)
    } else {
        Color.White
    }
    val coverSubtitleColor = if (isLightCover) {
        Color(0xFF2D1A22).copy(alpha = 0.78f)
    } else {
        Color.White.copy(alpha = 0.82f)
    }
    val coverFrameColor = if (isLightCover) {
        roseColor.copy(alpha = 0.38f)
    } else {
        goldColor.copy(alpha = 0.45f)
    }
    val coverEmblemColor = if (isLightCover) roseColor else goldColor

    val bookDirection = if (book.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val langLabel = if (book.language == "en" || !book.isRtl) "EN" else "عربي"
    val pagesLabel = if (book.isRtl && !isEnUi) "${book.pageCount} صفحة" else "${book.pageCount} Pages"

    Column(
        modifier = modifier
            .testTag("book_card_${book.id}")
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = (7f * s).dp,
                    shape = RoundedCornerShape(
                        topStart = (10f * s).dp,
                        topEnd = (14f * s).dp,
                        bottomStart = (6f * s).dp,
                        bottomEnd = (10f * s).dp
                    )
                )
                .clickable { onClick() },
            shape = RoundedCornerShape(
                topStart = (10f * s).dp,
                topEnd = (14f * s).dp,
                bottomStart = (6f * s).dp,
                bottomEnd = (10f * s).dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides bookDirection) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .background(
                                Brush.linearGradient(colors = coverGradientColors)
                            )
                    ) {
                        if (coverBitmap != null) {
                            Image(
                                bitmap = coverBitmap,
                                contentDescription = book.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("book_cover_snapshot_${book.id}")
                            )
                        } else {
                            LuxuryGeneratedBookCover(
                                book = book,
                                scaleFactor = s,
                                coverFrameColor = coverFrameColor,
                                coverEmblemColor = coverEmblemColor,
                                coverTitleColor = coverTitleColor,
                                coverSubtitleColor = coverSubtitleColor
                            )
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width((7f * s).dp)
                                    .fillMaxHeight()
                                    .background(
                                        Color.Black.copy(alpha = if (isLightCover) 0.18f else 0.35f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .width((1.8f * s).dp)
                                    .fillMaxHeight()
                                    .background(goldColor.copy(alpha = 0.55f))
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = (11f * s).dp,
                                    end = (8f * s).dp,
                                    top = (8f * s).dp
                                ),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            IconButton(
                                onClick = onFavoriteToggle,
                                modifier = Modifier
                                    .size((26f * s).dp)
                                    .testTag("favorite_button_${book.id}")
                            ) {
                                Icon(
                                    imageVector = if (book.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isEnUi) "Favorite" else "تفضيل",
                                    tint = if (book.isFavorite) {
                                        Color(0xFFFBBF24)
                                    } else if (isLightCover && coverBitmap == null) {
                                        coverTitleColor.copy(alpha = 0.75f)
                                    } else {
                                        Color.White.copy(alpha = 0.85f)
                                    },
                                    modifier = Modifier.size((16f * s).dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { book.readingProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height((4.5f * s).dp),
                            color = goldColor,
                            trackColor = Color.Black.copy(alpha = 0.30f)
                        )
                    }
                }

                val cleanTitle = remember(book.title) {
                    HtmlCoverSnapshotGenerator.sanitizeCoverTitle(book.title)
                }
                val cleanAuthor = remember(book.author, cleanTitle) {
                    HtmlBookParser.sanitizeExtractedAuthor(book.author, cleanTitle, book.subtitle)
                }
                val primaryFooterText = cleanTitle.ifBlank { cleanAuthor }
                val secondaryFooterText = when {
                    !shelfName.isNullOrBlank() -> "$shelfName • $pagesLabel"
                    cleanAuthor.isNotBlank() && !cleanAuthor.equals(cleanTitle, ignoreCase = true) -> "$cleanAuthor • $pagesLabel"
                    else -> pagesLabel
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = (8f * s).dp,
                            vertical = (6f * s).dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = primaryFooterText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (11.5f * s).sp
                        )
                        Text(
                            text = secondaryFooterText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = (9.5f * s).sp
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size((24f * s).dp)
                                .testTag("book_menu_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = if (isEnUi) "Book Options" else "خيارات الكتاب",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size((16f * s).dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isEnUi) "Customize Book & Shelf" else "تخصيص الكتاب والرف") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDetailsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isEnUi) "Share Book File" else "مشاركة ملف الكتاب") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onShareClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (book.isFavorite) {
                                            if (isEnUi) "Remove from Favorites" else "إزالة من المفضلة"
                                        } else {
                                            if (isEnUi) "Add to Favorites" else "إضافة للمفضلة"
                                        }
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onFavoriteToggle()
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isEnUi) "Delete Book" else "حذف الكتاب",
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error
                                ),
                                onClick = {
                                    menuExpanded = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((7f * s).dp)
                .padding(horizontal = (3.5f * s).dp)
                .clip(RoundedCornerShape(bottomStart = (5f * s).dp, bottomEnd = (5f * s).dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            goldColor.copy(alpha = 0.65f),
                            RoyalIndigo
                        )
                    )
                )
        )
    }
}

@Composable
fun BookListItem(
    book: Book,
    shelfName: String?,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDetailsClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isEnUi = strings.language == AppLanguage.EN
    val snapshotVersion by HtmlCoverSnapshotGenerator.snapshotVersion.collectAsStateWithLifecycle()
    val coverBitmap = rememberCachedCoverBitmap(
        context = context,
        folderPath = book.folderPath,
        snapshotVersion = snapshotVersion
    )

    var menuExpanded by remember { mutableStateOf(false) }
    val goldColor = parseSafeColor(book.accentGoldHex, Color(0xFFF59E0B))
    val roseColor = parseSafeColor(book.accentRoseHex, Color(0xFF5B3A29))
    val bgColor = parseSafeColor(book.bgPrimaryHex, Color(book.coverGradientStart))
    val isLightCover = bgColor.luminance() > 0.48f
    val coverGradientColors = if (isLightCover) {
        listOf(
            bgColor,
            lerp(bgColor, roseColor, 0.08f),
            lerp(bgColor, roseColor, 0.15f)
        )
    } else {
        listOf(
            bgColor,
            lerp(bgColor, roseColor, 0.42f),
            lerp(bgColor, roseColor, 0.65f)
        )
    }
    val langLabel = if (book.language == "en" || !book.isRtl) "EN" else "عربي"
    val pagesLabel = if (book.isRtl && !isEnUi) "${book.pageCount} صفحة" else "${book.pageCount} Pages"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}")
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 62.dp, height = 91.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(coverGradientColors))
                    .border(
                        1.dp,
                        if (isLightCover) roseColor.copy(alpha = 0.4f) else goldColor.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap,
                        contentDescription = book.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("book_cover_snapshot_${book.id}")
                    )
                } else {
                    Icon(
                        imageVector = getBookIcon(book.coverIcon),
                        contentDescription = null,
                        tint = if (isLightCover) roseColor else goldColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = if (isLightCover) 0.16f else 0.3f))
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            val cleanAuthor = remember(book.author) {
                HtmlBookParser.sanitizeExtractedAuthor(book.author)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (book.subtitle.isNotBlank()) {
                    Text(
                        text = book.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (cleanAuthor.isNotBlank()) "$cleanAuthor • $pagesLabel" else (shelfName?.let { "$it • $pagesLabel" } ?: pagesLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { book.readingProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (book.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isEnUi) "Favorite" else "تفضيل",
                    tint = if (book.isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("book_menu_${book.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = if (isEnUi) "Book Options" else "خيارات الكتاب",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isEnUi) "Customize Book & Shelf" else "تخصيص الكتاب والرف") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDetailsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isEnUi) "Share Book File" else "مشاركة ملف الكتاب") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onShareClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (book.isFavorite) {
                                    if (isEnUi) "Remove from Favorites" else "إزالة من المفضلة"
                                } else {
                                    if (isEnUi) "Add to Favorites" else "إضافة للمفضلة"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onFavoriteToggle()
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isEnUi) "Delete Book" else "حذف الكتاب",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error
                        ),
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}
