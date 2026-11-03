package com.maktabati.app.ui.components.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maktabati.app.data.importer.BookTemplateCategory
import com.maktabati.app.data.importer.BookVisualTheme
import com.maktabati.app.data.importer.SmartBookConverter

private val WHITESPACE_SPLIT_REGEX = Regex("\\s+")

/**
 * Clipboard-to-Masterpiece Studio ("استوديو صياغة نصوص الحافظة إلى تحف فنية"):
 * Receives text from the Android Clipboard (or typed/shared text), analyzes its structure live,
 * and transforms it into a multi-page interactive HTML5 book using any of the 26 publishing templates.
 */
@Composable
fun ClipboardBookStudioDialog(
    initialText: String,
    onConfirmCreateBook: (
        rawText: String,
        title: String,
        subtitle: String,
        author: String,
        theme: BookVisualTheme
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var rawText by remember(initialText) { mutableStateOf(initialText) }
    var isEditorExpanded by remember { mutableStateOf(initialText.isBlank() || initialText.length < 350) }
    var selectedCategory by remember { mutableStateOf(BookTemplateCategory.ALL) }

    val liveProposal = remember(rawText) {
        if (rawText.isBlank()) {
            null
        } else {
            runCatching {
                SmartBookConverter.inspectRawText(rawText)
            }.getOrNull()
        }
    }

    var title by remember { mutableStateOf(liveProposal?.detectedTitle ?: "") }
    var subtitle by remember { mutableStateOf(liveProposal?.detectedSubtitle ?: "") }
    var author by remember { mutableStateOf(liveProposal?.detectedAuthor ?: "") }
    var selectedTheme by remember {
        mutableStateOf(liveProposal?.recommendedTheme ?: BookVisualTheme.CREAM_BEIGE_PAPER)
    }
    var userManuallyPickedTheme by remember { mutableStateOf(false) }

    LaunchedEffect(liveProposal) {
        if (liveProposal != null) {
            if (title.isBlank() || title == "مقتطفات من الحافظة") {
                title = liveProposal.detectedTitle
            }
            if (author.isBlank()) {
                author = liveProposal.detectedAuthor
            }
            if (!userManuallyPickedTheme) {
                selectedTheme = liveProposal.recommendedTheme
            }
        }
    }

    val filteredThemes = remember(selectedCategory) {
        if (selectedCategory == BookTemplateCategory.ALL) {
            BookVisualTheme.entries.toList()
        } else {
            BookVisualTheme.entries.filter { it.category == selectedCategory }
        }
    }

    val wordCount = remember(rawText) {
        if (rawText.isBlank()) 0 else rawText.trim().split(WHITESPACE_SPLIT_REGEX).size
    }

    val previewExcerpt = remember(rawText, liveProposal) {
        liveProposal?.rawExtractedText?.lineSequence()
            ?.map { it.trim() }
            ?.firstOrNull { it.length > 25 && !it.startsWith("تأليف") && !it.startsWith("بقلم") }
            ?.take(160)
            ?: rawText.trim().lineSequence().firstOrNull { it.trim().length > 20 }?.take(160)
            ?: "انسخ أي مقال أو خاطرة أو رسالة أو فصول كتاب من الحافظة، وسيقوم الاستوديو بتنسيقها فوراً في صفحات مرقمة وفهرس ذكي."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp)
                .testTag("clipboard_book_studio_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "استوديو تحويل نصوص الحافظة لتحفة فنية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "الصق أي نص أو مقال أو خاطرة وأخرجه بأرقى قوالب دور النشر العالمية",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            val clipText = readClipboardText(context)
                                            if (clipText.isNotBlank()) {
                                                rawText = clipText
                                                title = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("paste_clipboard_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "لصق من الحافظة",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipText = readClipboardText(context)
                                            if (clipText.isNotBlank()) {
                                                rawText = if (rawText.isBlank()) {
                                                    clipText
                                                } else {
                                                    "${rawText.trim()}\n\n* * *\n\n$clipText"
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("append_clipboard_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "إلحاق فصل للحالي",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (rawText.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                rawText = ""
                                                title = ""
                                                isEditorExpanded = true
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("clear_clipboard_text_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "مسح النص",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BadgePill(text = "$wordCount كلمة")
                                        BadgePill(text = "${liveProposal?.estimatedPageCount ?: 1} صفحة")
                                        BadgePill(text = if (liveProposal?.isRtl != false) "عربي RTL" else "EN LTR")
                                    }

                                    TextButton(
                                        onClick = { isEditorExpanded = !isEditorExpanded },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditNote,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isEditorExpanded) "طي محرر النص" else "عرض وتعديل النص",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = if (isEditorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = isEditorExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    OutlinedTextField(
                                        value = rawText,
                                        onValueChange = { rawText = it },
                                        label = { Text("نص الكتاب (الصق من الحافظة أو اكتب بحرية)") },
                                        placeholder = {
                                            Text(
                                                "الصق هنا أي نص، مقال، مذكرات، قصائد، أو فصول كتاب... يتعرف المحرك الذكي تلقائياً على الفصول والآيات والأبيات الشعرية والاقتباسات."
                                            )
                                        },
                                        minLines = 4,
                                        maxLines = 7,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("clipboard_raw_text_input")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        LiveBookTemplatePreviewCard(
                            theme = selectedTheme,
                            bookTitle = title.ifBlank { liveProposal?.detectedTitle ?: "عنوان الكتاب الفاخر" },
                            bookAuthor = author.trim().ifBlank { liveProposal?.detectedAuthor.orEmpty() },
                            excerpt = previewExcerpt
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان الكتاب") },
                            placeholder = { Text("يُستنتج تلقائياً من أول سطر أو اكتبه هنا...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("convert_book_title_input")
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = author,
                                onValueChange = { author = it },
                                label = { Text("المؤلف / الكاتب") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("convert_book_author_input")
                            )

                            OutlinedTextField(
                                value = subtitle,
                                onValueChange = { subtitle = it },
                                label = { Text("العنوان الفرعي") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("convert_book_subtitle_input")
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اختر قالب دار النشر وهوية الكتاب:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${filteredThemes.size} قالب",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        TemplateCategoryFilterRow(
                            selectedCategory = selectedCategory,
                            onSelectCategory = { selectedCategory = it }
                        )
                    }

                    items(filteredThemes, key = { it.id }) { themeOption ->
                        val isSelected = selectedTheme == themeOption
                        ThemeOptionCard(
                            theme = themeOption,
                            isSelected = isSelected,
                            onClick = {
                                selectedTheme = themeOption
                                userManuallyPickedTheme = true
                            }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.32f)
                            .height(48.dp)
                            .testTag("cancel_convert_book_button")
                    ) {
                        Text(
                            text = "إلغاء",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifEmpty {
                                liveProposal?.detectedTitle ?: "مقتطفات من الحافظة"
                            }
                            val finalSubtitle = subtitle.trim().ifEmpty {
                                liveProposal?.detectedSubtitle.orEmpty()
                            }
                            val finalAuthor = author.trim().ifEmpty {
                                liveProposal?.detectedAuthor.orEmpty()
                            }
                            onConfirmCreateBook(
                                rawText,
                                finalTitle,
                                finalSubtitle,
                                finalAuthor,
                                selectedTheme
                            )
                        },
                        enabled = rawText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(0.68f)
                            .height(48.dp)
                            .testTag("confirm_convert_book_button")
                    ) {
                        Text(
                            text = "صياغة الكتاب وإضافته لمكتبتي ✦",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}
