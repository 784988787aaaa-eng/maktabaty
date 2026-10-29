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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
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
    onOpenBook: () -> Unit,
    onSaveMetadataAndStyle: (
        newTitle: String,
        newSubtitle: String,
        newAuthor: String,
        newShelfId: String?,
        bgPrimaryHex: String,
        accentGoldHex: String,
        accentRoseHex: String
    ) -> Unit,
    onDeleteBook: () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("تخصيص الكتاب والغلاف", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Book Metadata Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "عدد الصفحات: ${book.pageCount}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "اللغة والاتجاه: ${if (book.isRtl) "عربي (RTL)" else "English (LTR)"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "الحجم: ${maxOf(1L, book.totalSizeBytes / 1024)} ك.ب",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "التقدم: ${(book.readingProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الكتاب") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("العنوان الفرعي (اختياري)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_subtitle_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("اسم المؤلف") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_author_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("هوية ولون الغلاف:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (palette in LUXURY_COVER_PALETTES) {
                        val isSelected = bgPrimaryHex.equals(palette.bgPrimary, ignoreCase = true) &&
                            accentRoseHex.equals(palette.accentRose, ignoreCase = true)
                        val cStart = parseSafeColor(palette.bgPrimary, Color(0xFF1E293B))
                        val cEnd = parseSafeColor(palette.accentRose, Color(0xFFE11D48))
                        val cGold = parseSafeColor(palette.accentGold, Color(0xFFF59E0B))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(cStart, cEnd)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cGold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("الرف في مكتبتي:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = shelfDropdownExpanded,
                    onExpandedChange = { shelfDropdownExpanded = !shelfDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentShelf?.name ?: "الرف العام (جميع الكتب)",
                        onValueChange = {},
                        readOnly = true,
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

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .align(Alignment.Start)
                        .testTag("delete_book_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حذف الكتاب من مكتبتي")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text("حفظ")
                }

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
                        }
                        onDismiss()
                        onOpenBook()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("open_book_from_dialog_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فتح الكتاب")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
