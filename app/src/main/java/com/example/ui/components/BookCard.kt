package com.example.ui.components

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.RoyalIndigo
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.importer.HtmlBookParser
import com.example.data.model.Book

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
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val goldColor = parseSafeColor(book.accentGoldHex, Color(0xFFF59E0B))
    val roseColor = parseSafeColor(book.accentRoseHex, Color(0xFFE11D48))
    val bgColor = parseSafeColor(book.bgPrimaryHex, Color(book.coverGradientStart))
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
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 10.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 10.dp))
                .clickable { onClick() },
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 14.dp, bottomStart = 6.dp, bottomEnd = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides bookDirection) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        bgColor,
                                        roseColor.copy(alpha = 0.88f),
                                        Color(book.coverGradientEnd)
                                    )
                                )
                            )
                    ) {
                        // 3D Book Spine Crease & Binding Highlight on start edge
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .fillMaxHeight()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(goldColor.copy(alpha = 0.55f))
                            )
                        }

                        // Decorative Gold Foil Inner Frame
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 12.dp)
                                .border(
                                    width = 1.dp,
                                    color = goldColor.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )

                        // Top Row: Favorite Star & Language Badge (عربي / EN)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 14.dp, top = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, goldColor.copy(alpha = 0.7f))
                            ) {
                                Text(
                                    text = langLabel,
                                    color = goldColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = onFavoriteToggle,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("favorite_button_${book.id}")
                            ) {
                                Icon(
                                    imageVector = if (book.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "تفضيل",
                                    tint = if (book.isFavorite) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Center Luxury Cover Medallion, Title, Subtitle, Author
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(start = 20.dp, end = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = goldColor.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldColor.copy(alpha = 0.65f)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getBookIcon(book.coverIcon),
                                        contentDescription = null,
                                        tint = goldColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )

                            if (book.subtitle.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
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
                                    .padding(start = 18.dp, end = 14.dp, bottom = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = pagesLabel,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${(book.readingProgress * 100).toInt()}%",
                                    color = goldColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            LinearProgressIndicator(
                                progress = { book.readingProgress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp),
                                color = goldColor,
                                trackColor = Color.Black.copy(alpha = 0.45f)
                            )
                        }
                    }
                }

                // Under-cover Metadata Bar (Author, Shelf, Quick Actions Menu)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
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
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = shelfName ?: pagesLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("book_menu_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات الكتاب",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("تعديل الاسم والمؤلف ولون الغلاف") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDetailsClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (book.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة") },
                                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onFavoriteToggle()
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
                .height(8.dp)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
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
    modifier: Modifier = Modifier
) {
    val goldColor = parseSafeColor(book.accentGoldHex, Color(0xFFF59E0B))
    val roseColor = parseSafeColor(book.accentRoseHex, Color(0xFFE11D48))
    val bgColor = parseSafeColor(book.bgPrimaryHex, Color(book.coverGradientStart))
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
                    .background(
                        Brush.linearGradient(
                            listOf(bgColor, roseColor, Color(book.coverGradientEnd))
                        )
                    )
                    .border(1.dp, goldColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getBookIcon(book.coverIcon),
                    contentDescription = null,
                    tint = goldColor,
                    modifier = Modifier.size(26.dp)
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

            IconButton(onClick = onDetailsClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تعديل الكتاب",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
