package com.example.ui.components

import android.graphics.BitmapFactory
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.data.importer.HtmlBookParser
import com.example.data.importer.HtmlCoverSnapshotGenerator
import com.example.data.model.Book
import com.example.ui.theme.RoyalIndigo
import java.io.File

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
    val snapshotVersion by HtmlCoverSnapshotGenerator.snapshotVersion.collectAsStateWithLifecycle()
    val coverBitmap = remember(book.id, book.folderPath, snapshotVersion) {
        val snapshotFile = File(File(context.filesDir, "books/${book.folderPath}"), "cover_snapshot.png")
        if (snapshotFile.isFile && snapshotFile.length() > 128L) {
            runCatching { BitmapFactory.decodeFile(snapshotFile.absolutePath)?.asImageBitmap() }.getOrNull()
        } else {
            null
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    val s = scaleFactor.coerceIn(0.68f, 1.25f)
    val goldColor = parseSafeColor(book.accentGoldHex, Color(0xFFF59E0B))
    val roseColor = parseSafeColor(book.accentRoseHex, Color(0xFF5B3A29))
    val bgColor = parseSafeColor(book.bgPrimaryHex, Color(book.coverGradientStart))

    // Respect true light/creamy vs dark cover palettes without darkening light backgrounds
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
    val pagesLabel = if (book.isRtl) "${book.pageCount} صفحة" else "${book.pageCount} Pages"

    Column(
        modifier = modifier
            .testTag("book_card_${book.id}")
            .fillMaxWidth()
    ) {
        // 3D Hardcover Book Container
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
                        // 1. Real Cover Snapshot Image (if book has a cover page / cover_snapshot.png)
                        if (coverBitmap != null) {
                            Image(
                                bitmap = coverBitmap,
                                contentDescription = book.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("book_cover_snapshot_${book.id}")
                            )
                        } else {
                            // 2. Fallback Generated Cover (only when book has no cover snapshot)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = (12f * s).dp,
                                        end = (9f * s).dp,
                                        top = (9f * s).dp,
                                        bottom = (10f * s).dp
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = coverFrameColor,
                                        shape = RoundedCornerShape((7f * s).dp)
                                    )
                            )

                            // Center Luxury Cover Medallion, Title, Subtitle, Author
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(start = (16f * s).dp, end = (12f * s).dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = coverEmblemColor.copy(alpha = 0.14f),
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
                                            modifier = Modifier.size((19f * s).dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height((7f * s).dp))

                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = coverTitleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = (13.5f * s).sp,
                                    lineHeight = (17.5f * s).sp
                                )

                                if (book.subtitle.isNotBlank()) {
                                    Spacer(modifier = Modifier.height((3f * s).dp))
                                    Text(
                                        text = book.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = coverSubtitleColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        fontSize = (9.5f * s).sp,
                                        lineHeight = (13f * s).sp
                                    )
                                }
                            }
                        }

                        // 3D Book Spine Crease & Binding Highlight on start edge (preserved on both real & generated covers)
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

                        // Top Row: Language Badge (عربي / EN) & Favorite Star
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = (15f * s).dp,
                                    end = (11f * s).dp,
                                    top = (11f * s).dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.45f),
                                shape = RoundedCornerShape((5f * s).dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, goldColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = langLabel,
                                    color = goldColor,
                                    fontSize = (9.5f * s).sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = (6f * s).dp,
                                        vertical = (1.5f * s).dp
                                    )
                                )
                            }

                            IconButton(
                                onClick = onFavoriteToggle,
                                modifier = Modifier
                                    .size((24f * s).dp)
                                    .testTag("favorite_button_${book.id}")
                            ) {
                                Icon(
                                    imageVector = if (book.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "تفضيل",
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

                        // Bottom Cover Strip: Page Count Pill & Reading Progress Bar
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = (15f * s).dp,
                                        end = (11f * s).dp,
                                        bottom = (11f * s).dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape((4f * s).dp)
                                ) {
                                    Text(
                                        text = pagesLabel,
                                        color = Color.White.copy(alpha = 0.94f),
                                        fontSize = (9.5f * s).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(
                                            horizontal = (5f * s).dp,
                                            vertical = (1.5f * s).dp
                                        )
                                    )
                                }

                                Surface(
                                    color = Color.Black.copy(alpha = 0.38f),
                                    shape = RoundedCornerShape((4f * s).dp)
                                ) {
                                    Text(
                                        text = "${(book.readingProgress * 100).toInt()}%",
                                        color = goldColor,
                                        fontSize = (10f * s).sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(
                                            horizontal = (4.5f * s).dp,
                                            vertical = (1f * s).dp
                                        )
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { book.readingProgress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((4.5f * s).dp),
                                color = goldColor,
                                trackColor = Color.Black.copy(alpha = 0.35f)
                            )
                        }
                    }
                }

                // Under-cover Metadata Bar (Author, Shelf, Quick Actions Menu)
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
                            text = book.author,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (11.5f * s).sp
                        )
                        Text(
                            text = shelfName ?: pagesLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
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
                                contentDescription = "خيارات الكتاب",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size((16f * s).dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("تخصيص الكتاب والرف") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDetailsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة ملف الكتاب") },
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
                                text = { Text(if (book.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة") },
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
                                        "حذف الكتاب",
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

        // Artistic 3D Wooden Bookshelf Ledge under each book
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
    val snapshotVersion by HtmlCoverSnapshotGenerator.snapshotVersion.collectAsStateWithLifecycle()
    val coverBitmap = remember(book.id, book.folderPath, snapshotVersion) {
        val snapshotFile = File(File(context.filesDir, "books/${book.folderPath}"), "cover_snapshot.png")
        if (snapshotFile.isFile && snapshotFile.length() > 128L) {
            runCatching { BitmapFactory.decodeFile(snapshotFile.absolutePath)?.asImageBitmap() }.getOrNull()
        } else {
            null
        }
    }

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
    val pagesLabel = if (book.isRtl) "${book.pageCount} صفحة" else "${book.pageCount} Pages"

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
            // Mini 3D Book Cover
            Box(
                modifier = Modifier
                    .size(width = 62.dp, height = 88.dp)
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
                        contentScale = ContentScale.Crop,
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

                // Subtle 3D Spine Strip on start edge
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = if (isLightCover) 0.16f else 0.3f))
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = langLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

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
                    text = "${book.author} • $pagesLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { book.readingProgress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${(book.readingProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (book.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "تفضيل",
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
                        contentDescription = "خيارات الكتاب",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("تخصيص الكتاب والرف") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDetailsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("مشاركة ملف الكتاب") },
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
                        text = { Text(if (book.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة") },
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
                                "حذف الكتاب",
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

