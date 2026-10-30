package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.model.Book
import com.example.data.model.Shelf

data class CoverPaletteOption(
    val name: String,
    val bgPrimary: String,
    val accentGold: String,
    val accentRose: String
)

private val LUXURY_COVER_PALETTES = listOf(
    CoverPaletteOption("عنبر ملكي", "#2D1606", "#F59E0B", "#9A3412"),
    CoverPaletteOption("ياقوت قرمزي", "#3B0722", "#FDE047", "#E11D48"),
    CoverPaletteOption("زمرد كلاسيكي", "#06392F", "#FBBF24", "#0D9488"),
    CoverPaletteOption("كحلي فلكي", "#0F172A", "#38BDF8", "#4F46E5"),
    CoverPaletteOption("أرجواني إمبراطوري", "#1E1B4B", "#F59E0B", "#7C3AED"),
    CoverPaletteOption("بني أندلسي", "#291811", "#FCD34D", "#78350F")
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
            title = { Text("تأكيد حذف الكتاب من مكتبتي") },
            text = {
                Text("هل أنت متأكد من حذف «${book.title}» من المكتبة؟ سيتم إزالة نسخته المحفوظة وسجل القراءة نهائياً دون المساس بملفاتك الأصلية في الجهاز.")
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
                    Text("نعم، احذف الكتاب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("تراجع")
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
                        text = "تخصيص الكتاب",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Share Book Capsule Button
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
                                    contentDescription = "مشاركة الكتاب",
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "مشاركة",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Delete Book Capsule Button
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
                                    contentDescription = "حذف الكتاب",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "حذف",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Single-line micro info bar (28.dp)
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
                            text = "${book.pageCount} صفحة  •  ${maxOf(1L, book.totalSizeBytes / 1024)} ك.ب  •  التقدم ${(book.readingProgress * 100).toInt()}%",
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
                    label = { Text("عنوان الكتاب", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_title_input")
                )

                // Author and Subtitle side-by-side in one compact row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("اسم المؤلف", fontSize = 11.sp, maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_author_input")
                    )

                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("العنوان الفرعي", fontSize = 11.sp, maxLines = 1) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_subtitle_input")
                    )
                }

                // Cover color selector in a single compact row (32.dp circles)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "لون الغلاف:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
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

                // Compact Shelf Selector
                ExposedDropdownMenuBox(
                    expanded = shelfDropdownExpanded,
                    onExpandedChange = { shelfDropdownExpanded = !shelfDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentShelf?.name ?: "الرف العام (جميع الكتب)",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("الرف في مكتبتي", fontSize = 11.sp) },
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
                            text = { Text("الرف العام (جميع الكتب)") },
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
                Text("حفظ التعديلات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
