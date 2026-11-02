package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.data.typography.BookTypographyProfile
import com.example.data.typography.FontCategory
import com.example.data.typography.MaktabatiFontRegistry
import com.example.data.typography.RegisteredArabicFont
import com.example.data.typography.UserTypographyPreferences
import com.example.ui.theme.fontFamilyForName

private data class TypographyRoleItem(
    val index: Int,
    val labelAr: String,
    val samplePhrase: String,
    val preferredCategory: FontCategory,
    val getSelectedFont: (BookTypographyProfile) -> String,
    val updateFont: (BookTypographyProfile, String) -> BookTypographyProfile
)

@Composable
fun TypographySettingsDialog(
    currentPreferences: UserTypographyPreferences,
    onUpdatePreferences: (UserTypographyPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("maktabati_preferences", Context.MODE_PRIVATE)
    }

    var draftPrefs by remember(currentPreferences) { mutableStateOf(currentPreferences) }
    var selectedRoleIndex by remember { mutableIntStateOf(0) }
    var isEditingPreviewText by remember { mutableStateOf(false) }
    var importFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var fontRegistryRefreshTick by remember { mutableIntStateOf(0) }

    val roles = remember {
        listOf(
            TypographyRoleItem(
                index = 0,
                labelAr = "المتن والسرد",
                samplePhrase = "وَمَا السَّكِينَةُ إِلَّا خَيْمَةٌ تُضْرَبُ فِي أَعْمَاقِ القَلْبِ",
                preferredCategory = FontCategory.BOOKS_NARRATIVE,
                getSelectedFont = { it.body },
                updateFont = { prof, f -> prof.copy(body = f) }
            ),
            TypographyRoleItem(
                index = 1,
                labelAr = "آيات القرآن",
                samplePhrase = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                preferredCategory = FontCategory.QURAN_HERITAGE,
                getSelectedFont = { it.quran },
                updateFont = { prof, f -> prof.copy(quran = f) }
            ),
            TypographyRoleItem(
                index = 2,
                labelAr = "العناوين",
                samplePhrase = "خَيْمَةٌ فِي القَلْبِ — إِشْرَاقَةُ اليَقِينِ",
                preferredCategory = FontCategory.HEADINGS_DISPLAY,
                getSelectedFont = { it.headings },
                updateFont = { prof, f -> prof.copy(headings = f) }
            ),
            TypographyRoleItem(
                index = 3,
                labelAr = "الأبواب والفصول",
                samplePhrase = "البَابُ الأَوَّلُ: مَلاذُ السَّكِينَةِ",
                preferredCategory = FontCategory.HEADINGS_DISPLAY,
                getSelectedFont = { it.chapterTitles },
                updateFont = { prof, f -> prof.copy(chapterTitles = f) }
            ),
            TypographyRoleItem(
                index = 4,
                labelAr = "المذكرات والدرر",
                samplePhrase = "«إِنَّ القُلُوبَ إِذَا اتَّصَلَتْ بِبَارِئِهَا أَزْهَرَتْ»",
                preferredCategory = FontCategory.DIARIES_CALLIGRAPHY,
                getSelectedFont = { it.quotes },
                updateFont = { prof, f -> prof.copy(quotes = f) }
            ),
            TypographyRoleItem(
                index = 5,
                labelAr = "الفهارس والواجهة",
                samplePhrase = "فهرس الكتاب • البحث الفوري • العلامات",
                preferredCategory = FontCategory.BOOKS_NARRATIVE,
                getSelectedFont = { it.interfaceFont },
                updateFont = { prof, f -> prof.copy(interfaceFont = f) }
            ),
            TypographyRoleItem(
                index = 6,
                labelAr = "الأرقام",
                samplePhrase = "صفحة ١٢ من ١٠٥ • 100%",
                preferredCategory = FontCategory.BOOKS_NARRATIVE,
                getSelectedFont = { it.numbers },
                updateFont = { prof, f -> prof.copy(numbers = f) }
            )
        )
    }

    val activeRole = roles[selectedRoleIndex.coerceIn(0, roles.lastIndex)]
    val selectedFontForRole = activeRole.getSelectedFont(draftPrefs.profile)

    val customFontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val result = MaktabatiFontRegistry.importUserCustomFont(context, sharedPrefs, uri)
            result.onSuccess { importedFont ->
                fontRegistryRefreshTick++
                val nextProfile = activeRole.updateFont(draftPrefs.profile, importedFont.familyName)
                val updated = draftPrefs.copy(
                    useCustomTypography = true,
                    presetId = "custom",
                    profile = nextProfile
                )
                draftPrefs = updated
                onUpdatePreferences(updated)
                importFeedbackMessage = "تمت إضافة الخط «${cleanArabicFontTitle(importedFont.arabicDisplayName)}» وتطبيقه ✨"
            }.onFailure { err ->
                importFeedbackMessage = err.message ?: "تعذر استيراد ملف الخط"
            }
        }
    }

    val allFonts = remember(fontRegistryRefreshTick) {
        MaktabatiFontRegistry.registeredFonts
    }

    // Automatically sort fonts so user-imported fonts and fonts matching the active role appear first
    val orderedFonts = remember(allFonts, activeRole) {
        allFonts.sortedWith(
            compareByDescending<RegisteredArabicFont> { it.isUserCustom }
                .thenByDescending { it.category == activeRole.preferredCategory }
        )
    }

    val customPreviewActive = draftPrefs.customPreviewText.isNotBlank() &&
        draftPrefs.customPreviewText.trim() != UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("typography_settings_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // =========================================================================
                // 1. Simple, Calm Header (Title + Master Switch + Close)
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "استوديو الخطوط",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (draftPrefs.useCustomTypography) {
                                    "التخصيص مفعّل"
                                } else {
                                    "الخط الأصلي للكتاب"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (draftPrefs.useCustomTypography) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Switch(
                            checked = draftPrefs.useCustomTypography,
                            onCheckedChange = { enabled ->
                                val updated = draftPrefs.copy(useCustomTypography = enabled)
                                draftPrefs = updated
                                onUpdatePreferences(updated)
                            },
                            modifier = Modifier.testTag("typography_custom_switch")
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("typography_dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق استوديو الخطوط"
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // =========================================================================
                // 2. Pinned Top Control Area (Focused Live Preview + Single Clean Role Bar)
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Focused Live Preview Card (Always visible, calm & uncluttered)
                    FocusedRolePreviewCard(
                        roleLabel = activeRole.labelAr,
                        selectedFontName = selectedFontForRole,
                        previewText = if (customPreviewActive) {
                            draftPrefs.customPreviewText
                        } else {
                            activeRole.samplePhrase
                        },
                        customPreviewText = draftPrefs.customPreviewText,
                        isEditingPreview = isEditingPreviewText,
                        onToggleEditPreview = { isEditingPreviewText = !isEditingPreviewText },
                        onCustomPreviewTextChange = { newText ->
                            val updated = draftPrefs.copy(customPreviewText = newText)
                            draftPrefs = updated
                            onUpdatePreferences(updated)
                        },
                        onResetPreviewText = {
                            val updated = draftPrefs.copy(
                                customPreviewText = UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT
                            )
                            draftPrefs = updated
                            onUpdatePreferences(updated)
                        },
                        onImportCustomFont = {
                            customFontPickerLauncher.launch(
                                arrayOf(
                                    "font/ttf",
                                    "font/otf",
                                    "font/woff2",
                                    "application/x-font-ttf",
                                    "application/x-font-opentype",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        }
                    )

                    if (importFeedbackMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = importFeedbackMessage!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { importFeedbackMessage = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Single Clean Role Selector Bar (No duplicate category filter bars!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        roles.forEach { role ->
                            val isSelected = role.index == selectedRoleIndex
                            Surface(
                                onClick = { selectedRoleIndex = role.index },
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                contentColor = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.testTag("typography_role_tab_${role.index}")
                            ) {
                                Text(
                                    text = role.labelAr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                // =========================================================================
                // 3. Calm, Easy-to-Scan Font List (Only fonts scroll here)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = orderedFonts,
                        key = { it.familyName }
                    ) { fontSpec ->
                        val isFontSelected =
                            selectedFontForRole.equals(fontSpec.familyName, ignoreCase = true)
                        FontOptionSimpleCard(
                            fontSpec = fontSpec,
                            sampleText = if (customPreviewActive) {
                                draftPrefs.customPreviewText
                            } else {
                                activeRole.samplePhrase
                            },
                            isSelected = isFontSelected,
                            dimmed = !draftPrefs.useCustomTypography,
                            onSelect = {
                                val nextProfile =
                                    activeRole.updateFont(draftPrefs.profile, fontSpec.familyName)
                                val updated = draftPrefs.copy(
                                    useCustomTypography = true,
                                    presetId = "custom",
                                    profile = nextProfile
                                )
                                draftPrefs = updated
                                onUpdatePreferences(updated)
                            },
                            onDeleteCustomFont = if (fontSpec.isUserCustom) {
                                {
                                    MaktabatiFontRegistry.removeUserCustomFont(
                                        sharedPrefs,
                                        fontSpec.familyName
                                    )
                                    fontRegistryRefreshTick++
                                }
                            } else null
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // =========================================================================
                // 4. Pinned Bottom Action Bar
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val resetPrefs = UserTypographyPreferences(
                                useCustomTypography = false,
                                presetId = "editorial_narrative",
                                profile = BookTypographyProfile(),
                                customPreviewText = draftPrefs.customPreviewText
                            )
                            draftPrefs = resetPrefs
                            onUpdatePreferences(resetPrefs)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("typography_reset_default_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("هوية الكتاب الأصلية", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            onUpdatePreferences(draftPrefs)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("typography_apply_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "اعتماد التفضيلات",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusedRolePreviewCard(
    roleLabel: String,
    selectedFontName: String,
    previewText: String,
    customPreviewText: String,
    isEditingPreview: Boolean,
    onToggleEditPreview: () -> Unit,
    onCustomPreviewTextChange: (String) -> Unit,
    onResetPreviewText: () -> Unit,
    onImportCustomFont: () -> Unit
) {
    val activeFontFamily = fontFamilyForName(selectedFontName)
    val selectedFontSpec = remember(selectedFontName) {
        MaktabatiFontRegistry.findFont(selectedFontName)
    }
    val arabicFontName = selectedFontSpec?.let { cleanArabicFontTitle(it.arabicDisplayName) }
        ?: selectedFontName

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF6F0)),
        border = BorderStroke(1.dp, Color(0xFFDDD1C4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typography_live_preview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar inside preview: Role + Current Font on right, Edit Text + Import Font on left
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$roleLabel: $arabicFontName",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF732E41),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onToggleEditPreview,
                        shape = RoundedCornerShape(50),
                        color = if (isEditingPreview) Color(0xFF732E41) else Color(0xFFEDE4D8),
                        modifier = Modifier.testTag("typography_edit_preview_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "نص معاينة حر",
                                tint = if (isEditingPreview) Color.White else Color(0xFF732E41),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (isEditingPreview) "إغلاق" else "نص معاينة",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEditingPreview) Color.White else Color(0xFF732E41)
                            )
                        }
                    }

                    Surface(
                        onClick = onImportCustomFont,
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFEDE4D8),
                        modifier = Modifier.testTag("typography_import_user_font_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "إضافة خط من جهازي",
                                tint = Color(0xFF732E41),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "خط من جهازي",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF732E41)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isEditingPreview,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    OutlinedTextField(
                        value = customPreviewText,
                        onValueChange = onCustomPreviewTextChange,
                        label = {
                            Text("اكتب عبارة لمعاينتها:", fontSize = 11.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFFFDF9),
                            unfocusedContainerColor = Color(0xFFFFFDF9),
                            focusedTextColor = Color(0xFF38272C),
                            unfocusedTextColor = Color(0xFF38272C),
                            focusedBorderColor = Color(0xFFBD6B80),
                            unfocusedBorderColor = Color(0xFFDDD1C4)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("typography_custom_preview_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onResetPreviewText,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(
                                text = "استعادة النص الافتراضي",
                                fontSize = 10.sp,
                                color = Color(0xFF732E41)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Single, calm, clear live sample rendered in the selected font
            Text(
                text = previewText,
                fontFamily = activeFontFamily,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                color = Color(0xFF2C1E23),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FontOptionSimpleCard(
    fontSpec: RegisteredArabicFont,
    sampleText: String,
    isSelected: Boolean,
    dimmed: Boolean,
    onSelect: () -> Unit,
    onDeleteCustomFont: (() -> Unit)? = null
) {
    val fontComposeFamily = fontFamilyForName(fontSpec.familyName)
    val cleanTitle = remember(fontSpec.arabicDisplayName) {
        cleanArabicFontTitle(fontSpec.arabicDisplayName)
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.7.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.88f else 1f)
            .testTag("typography_font_option_${fontSpec.folderName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = cleanTitle,
                        fontFamily = fontComposeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = fontSpec.category.labelAr,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = sampleText,
                    fontFamily = fontComposeFamily,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (onDeleteCustomFont != null) {
                    IconButton(
                        onClick = onDeleteCustomFont,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الخط المخصص",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "محدد",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun cleanArabicFontTitle(rawTitle: String): String {
    return rawTitle.substringBefore("(").trim().ifEmpty { rawTitle }
}
