package com.example.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.importer.BookTemplateCategory
import com.example.data.importer.BookVisualTheme
import com.example.data.importer.ConvertibleBookProposal
import com.example.data.importer.SmartBookConverter
import com.example.ui.theme.fontFamilyForName

@Composable
fun ConvertTextBookDialog(
    proposal: ConvertibleBookProposal,
    onConfirmConvert: (title: String, subtitle: String, author: String, theme: BookVisualTheme) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(proposal) { mutableStateOf(proposal.detectedTitle) }
    var subtitle by remember(proposal) { mutableStateOf(proposal.detectedSubtitle) }
    var author by remember(proposal) { mutableStateOf(proposal.detectedAuthor) }
    var selectedTheme by remember(proposal) { mutableStateOf(proposal.recommendedTheme) }
    var selectedCategory by remember { mutableStateOf(BookTemplateCategory.ALL) }

    val filteredThemes = remember(selectedCategory) {
        if (selectedCategory == BookTemplateCategory.ALL) {
            BookVisualTheme.entries.toList()
        } else {
            BookVisualTheme.entries.filter { it.category == selectedCategory }
        }
    }

    val previewExcerpt = remember(proposal) {
        proposal.rawExtractedText.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.length > 25 && !it.startsWith("تأليف") && !it.startsWith("بقلم") }
            ?.take(150)
            ?: "بسم الله الرحمن الرحيم.. تزداد الكلمات جمالاً وأثراً حين تُصاغ في قوالب تليق بمعانيها وتُريح عين القارئ."
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 750.dp)
                .testTag("convert_text_book_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Row
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "استوديو قوالب النشر والتحويل الفاخر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "صياغة كتاب HTML5 تفاعلي من ${proposal.sourceFormatLabel} • ${BookVisualTheme.entries.size} قالباً احترافياً",
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

                // Document Stats Pill Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = proposal.originalFileName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            BadgePill(text = proposal.sourceFormatLabel)
                            BadgePill(text = "${proposal.estimatedPageCount} صفحة")
                            BadgePill(text = if (proposal.isRtl) "عربي RTL" else "EN LTR")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Body: Live Preview + Metadata + Categorized 26-Template Gallery
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // 1. Live Miniature Book Page & Cover Preview
                    item {
                        LiveBookTemplatePreviewCard(
                            theme = selectedTheme,
                            bookTitle = title.ifBlank { proposal.detectedTitle },
                            bookAuthor = author.ifBlank { proposal.detectedAuthor },
                            excerpt = previewExcerpt
                        )
                    }

                    // 2. Book Metadata Inputs
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("عنوان الكتاب") },
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

                    // 3. Category Filter Bar
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
                            onClick = { selectedTheme = themeOption }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Footer Action Buttons
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
                            onConfirmConvert(
                                title.trim().ifEmpty { proposal.detectedTitle },
                                subtitle.trim().ifEmpty { proposal.detectedSubtitle },
                                author.trim().ifEmpty { proposal.detectedAuthor },
                                selectedTheme
                            )
                        },
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
                            text = "تحويل وإضافة إلى مكتبتي ✦",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }
                }
            }
        }
    }
}

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

    // Live smart analysis of the raw text
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
    var subtitle by remember { mutableStateOf(liveProposal?.detectedSubtitle ?: "نسخة تفاعلية فاخرة • مكتبتي") }
    var author by remember { mutableStateOf(liveProposal?.detectedAuthor ?: "مكتبتي الرقمية") }
    var selectedTheme by remember {
        mutableStateOf(liveProposal?.recommendedTheme ?: BookVisualTheme.CREAM_BEIGE_PAPER)
    }
    var userManuallyPickedTheme by remember { mutableStateOf(false) }

    // Sync auto-detected metadata when text is pasted and title was still blank/default
    LaunchedEffect(liveProposal) {
        if (liveProposal != null) {
            if (title.isBlank() || title == "مقتطفات من الحافظة") {
                title = liveProposal.detectedTitle
            }
            if (author.isBlank() || author == "مكتبتي الرقمية") {
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
        if (rawText.isBlank()) 0 else rawText.trim().split(Regex("\\s+")).size
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
                // Studio Header
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

                // Scrollable Studio Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    // 1. Elegant Clipboard Action & Editor Card
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
                                // Quick Clipboard Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Paste from Clipboard (Replace)
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

                                    // Append from Clipboard as a new chapter/section
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

                                // Live Stats & Collapse/Expand Editor Toggle Row
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

                    // 2. Live Miniature Book Page & Cover Preview
                    item {
                        LiveBookTemplatePreviewCard(
                            theme = selectedTheme,
                            bookTitle = title.ifBlank { liveProposal?.detectedTitle ?: "عنوان الكتاب الفاخر" },
                            bookAuthor = author.ifBlank { "مكتبتي الرقمية" },
                            excerpt = previewExcerpt
                        )
                    }

                    // 3. Book Metadata Inputs (Title, Author, Subtitle)
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

                    // 4. Category Filter Bar + 26 Publishing Templates
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

                // Footer Action Buttons
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
                                liveProposal?.detectedSubtitle ?: "نسخة تفاعلية فاخرة • مكتبتي"
                            }
                            val finalAuthor = author.trim().ifEmpty {
                                liveProposal?.detectedAuthor ?: "مكتبتي الرقمية"
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

@Composable
private fun TemplateCategoryFilterRow(
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
private fun LiveBookTemplatePreviewCard(
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
            // Miniature Book Cover (Right side in RTL)
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

            // Miniature Open Book Page (Left side in RTL) showing true Paper Color & Typography
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
private fun BadgePill(text: String) {
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
private fun ThemeOptionCard(
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
                // Mini Book Cover & Paper Swatch Preview
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

private fun readClipboardText(context: Context): String {
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

private fun parseHexColorSafe(hex: String, fallback: Color): Color {
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
