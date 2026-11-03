package com.maktabati.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings

@Immutable
data class CoverPaletteOption(
    val name: String,
    val bgPrimary: String,
    val accentGold: String,
    val accentRose: String
)

private val LUXURY_COVER_PALETTES = listOf(
    CoverPaletteOption("ورق كريمي وياقوت", "#FAF6F0", "#C69A4E", "#732E41"),
    CoverPaletteOption("بردي زمردي", "#F4F1EA", "#B8860B", "#234F3E"),
    CoverPaletteOption("عاجي وذهب عتيق", "#FDFBF7", "#D97706", "#5B3A29"),
    CoverPaletteOption("عنبر ملكي", "#2D1606", "#F59E0B", "#9A3412"),
    CoverPaletteOption("ياقوت قرمزي", "#3B0722", "#FDE047", "#E11D48"),
    CoverPaletteOption("زمرد كلاسيكي", "#06392F", "#FBBF24", "#0D9488"),
    CoverPaletteOption("كحلي فلكي", "#0F172A", "#38BDF8", "#4F46E5"),
    CoverPaletteOption("أرجواني إمبراطوري", "#1E1B4B", "#F59E0B", "#7C3AED")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailDialog(
    book: Book,
    shelves: List<Shelf>,
    onDismiss: () -> Unit,
    onOpenBook: () -> Unit = {},
    onSaveMetadataAndStyle: (
        newTitle: String,
        newSubtitle: String,
        newAuthor: String,
        newShelfId: String?,
        bgPrimaryHex: String,
        accentGoldHex: String,
        accentRoseHex: String
    ) -> Unit,
    onDeleteBook: () -> Unit,
    onShareBook: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    var title by remember { mutableStateOf(book.title) }
    var subtitle by remember { mutableStateOf(book.subtitle) }
    var author by remember { mutableStateOf(book.author) }
    var selectedShelfId by remember { mutableStateOf(book.shelfId) }
    var bgPrimaryHex by remember { mutableStateOf(book.bgPrimaryHex) }
    var accentGoldHex by remember { mutableStateOf(book.accentGoldHex) }
    var accentRoseHex by remember { mutableStateOf(book.accentRoseHex) }

    var shelfDropdownExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val currentShelf = shelves.find { it.id == selectedShelfId }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isEn) "Confirm Book Deletion" else "تأكيد حذف الكتاب من مكتبتي") },
            text = {
                Text(
                    if (isEn) {
                        "Are you sure you want to delete \"${book.title}\" from your library? Its saved copy and reading progress will be permanently removed without affecting your original device files."
                    } else {
                        "هل أنت متأكد من حذف «${book.title}» من المكتبة؟ سيتم إزالة نسخته المحفوظة وسجل القراءة نهائياً دون المساس بملفاتك الأصلية في الجهاز."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteBook()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_book_button")
                ) {
                    Text(if (isEn) "Yes, Delete Book" else "نعم، احذف الكتاب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(if (isEn) "Cancel" else "تراجع")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEn) "Customize Book" else "تخصيص الكتاب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onShareBook() }
                                .testTag("share_book_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = if (isEn) "Share Book" else "مشاركة الكتاب",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isEn) "Share" else "مشاركة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { showDeleteConfirm = true }
                                .testTag("delete_book_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = if (isEn) "Delete Book" else "حذف الكتاب",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isEn) "Delete" else "حذف",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = if (isEn) {
                                "${book.pageCount} pages  •  ${maxOf(1L, book.totalSizeBytes / 1024)} KB  •  Progress ${(book.readingProgress * 100).toInt()}%"
                            } else {
                                "${book.pageCount} صفحة  •  ${maxOf(1L, book.totalSizeBytes / 1024)} ك.ب  •  التقدم ${(book.readingProgress * 100).toInt()}%"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isEn) "Book Title" else "عنوان الكتاب", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_title_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text(if (isEn) "Author" else "اسم المؤلف", fontSize = 11.sp, maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_author_input")
                    )

                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text(if (isEn) "Subtitle" else "العنوان الفرعي", fontSize = 11.sp, maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_subtitle_input")
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isEn) "Cover Color:" else "لون الغلاف:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (palette in LUXURY_COVER_PALETTES) {
                            val isSelected = bgPrimaryHex.equals(palette.bgPrimary, ignoreCase = true) &&
                                accentRoseHex.equals(palette.accentRose, ignoreCase = true)
                            val cStart = parseSafeColor(palette.bgPrimary, Color(0xFF1E293B))
                            val cEnd = parseSafeColor(palette.accentRose, Color(0xFFE11D48))
                            val cGold = parseSafeColor(palette.accentGold, Color(0xFFF59E0B))

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(cStart, cEnd)))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else cGold,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        bgPrimaryHex = palette.bgPrimary
                                        accentGoldHex = palette.accentGold
                                        accentRoseHex = palette.accentRose
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(cGold)
                                )
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = shelfDropdownExpanded,
                    onExpandedChange = { shelfDropdownExpanded = !shelfDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val defaultShelfLabel = if (isEn) "General Shelf (All Books)" else "الرف العام (جميع الكتب)"
                    OutlinedTextField(
                        value = currentShelf?.name ?: defaultShelfLabel,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text(if (isEn) "Shelf in Library" else "الرف في مكتبتي", fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shelfDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = shelfDropdownExpanded,
                        onDismissRequest = { shelfDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(defaultShelfLabel) },
                            onClick = {
                                selectedShelfId = null
                                shelfDropdownExpanded = false
                            }
                        )
                        for (shelf in shelves) {
                            DropdownMenuItem(
                                text = { Text(shelf.name) },
                                onClick = {
                                    selectedShelfId = shelf.id
                                    shelfDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSaveMetadataAndStyle(
                            title.trim(),
                            subtitle.trim(),
                            author.trim(),
                            selectedShelfId,
                            bgPrimaryHex,
                            accentGoldHex,
                            accentRoseHex
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_book_metadata_button")
            ) {
                Text(if (isEn) "Save Changes" else "حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "إلغاء")
            }
        }
    )
}
