package com.maktabati.app.ui.components.studio

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktabati.app.data.importer.BookTemplateCategory
import com.maktabati.app.data.importer.BookVisualTheme
import com.maktabati.app.ui.theme.fontFamilyForName

@Composable
fun TemplateCategoryFilterRow(
    selectedCategory: BookTemplateCategory,
    onSelectCategory: (BookTemplateCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("template_categories_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(BookTemplateCategory.entries, key = { it.id }) { category ->
            val isSelected = selectedCategory == category
            val count = if (category == BookTemplateCategory.ALL) {
                BookVisualTheme.entries.size
            } else {
                BookVisualTheme.entries.count { it.category == category }
            }
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category) },
                label = {
                    Text(
                        text = "${category.emoji} ${category.titleAr} ($count)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("template_category_${category.id}")
            )
        }
    }
}

/**
 * Live miniature preview showing both the 3D-styled Book Cover and an Open Inner Paper Page
 * with the selected template's exact paper tone, ornament accents, and bundled Arabic fonts.
 */
@Composable
fun LiveBookTemplatePreviewCard(
    theme: BookVisualTheme,
    bookTitle: String,
    bookAuthor: String,
    excerpt: String
) {
    val bgPrimary = parseHexColorSafe(theme.bgPrimaryHex, Color(0xFF1E293B))
    val accentPrimary = parseHexColorSafe(theme.accentPrimaryHex, Color(0xFF0284C7))
    val accentGold = parseHexColorSafe(theme.accentGoldHex, Color(0xFFD97706))
    val paperBg = parseHexColorSafe(theme.paperBgHex, Color(0xFFFBF7EE))
    val textDark = parseHexColorSafe(theme.textPrimaryHex, Color(0xFF1E293B))
    val headingFont = fontFamilyForName(theme.fontHeadings)
    val bodyFont = fontFamilyForName(theme.fontBody)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, accentGold.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_template_preview_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .height(118.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(bgPrimary, accentPrimary)
                        )
                    )
                    .border(1.2.dp, accentGold.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = theme.badgeEmoji,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = bookTitle.take(28),
                        fontFamily = headingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(1.5.dp)
                            .background(accentGold.copy(alpha = 0.9f))
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = bookAuthor.take(18),
                        fontFamily = bodyFont,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(118.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(paperBg)
                    .border(1.dp, accentGold.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${theme.badgeEmoji} ${theme.arabicName}",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = accentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = accentPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = theme.category.titleAr,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = excerpt,
                        fontFamily = bodyFont,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = textDark,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الخط: ${theme.fontBody} • ${theme.fontHeadings}",
                            fontSize = 8.5.sp,
                            color = textDark.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "١",
                            fontFamily = headingFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = accentGold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgePill(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ThemeOptionCard(
    theme: BookVisualTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgPrimary = parseHexColorSafe(theme.bgPrimaryHex, Color(0xFF3B0722))
    val accentPrimary = parseHexColorSafe(theme.accentPrimaryHex, Color(0xFFE11D48))
    val accentGold = parseHexColorSafe(theme.accentGoldHex, Color(0xFFF59E0B))
    val paperBg = parseHexColorSafe(theme.paperBgHex, Color(0xFFFFFDF9))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 0.8.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("theme_option_${theme.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 50.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(bgPrimary, accentPrimary)
                            )
                        )
                        .border(1.dp, accentGold.copy(alpha = 0.85f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 22.dp, height = 30.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(paperBg)
                            .border(0.8.dp, accentGold, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme.badgeEmoji,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = theme.arabicName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = theme.category.titleAr,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = theme.subtitleDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "محدد",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

fun readClipboardText(context: Context): String {
    return runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(context)?.toString()?.trim().orEmpty()
        } else {
            ""
        }
    }.getOrDefault("")
}

fun parseHexColorSafe(hex: String, fallback: Color): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val longVal = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000L or longVal)
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
