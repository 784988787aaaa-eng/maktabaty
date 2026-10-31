package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.typography.EditorialTypographyPreset
import com.example.data.typography.FontCategory
import com.example.data.typography.MaktabatiFontRegistry
import com.example.data.typography.RegisteredArabicFont
import com.example.data.typography.UserTypographyPreferences
import com.example.ui.theme.fontFamilyForName

private data class TypographyRoleItem(
    val index: Int,
    val labelAr: String,
    val descriptionAr: String,
    val samplePhrase: String,
    val getSelectedFont: (BookTypographyProfile) -> String,
    val updateFont: (BookTypographyProfile, String) -> BookTypographyProfile
)

private enum class StudioTabMode {
    PRESETS,
    FINE_TUNE
}

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
    var activeTabMode by remember {
        mutableStateOf(
            if (currentPreferences.presetId == "custom") StudioTabMode.FINE_TUNE
            else StudioTabMode.PRESETS
        )
    }
    var selectedRoleIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(FontCategory.ALL) }
    var isEditingPreviewText by remember { mutableStateOf(false) }
    var importFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var fontRegistryRefreshTick by remember { mutableIntStateOf(0) }

    val roles = remember {
        listOf(
            TypographyRoleItem(
                index = 0,
                labelAr = "المتن والسرد",
                descriptionAr = "خط الفقرات والنصوص السردية الطويلة داخل صفحات الكتاب",
                samplePhrase = "وَمَا السَّكِينَةُ إِلَّا خَيْمَةٌ تُضْرَبُ فِي أَعْمَاقِ القَلْبِ فَتَسْكُنُ إِلَيْهَا الرُّوحُ.",
                getSelectedFont = { it.body },
                updateFont = { prof, f -> prof.copy(body = f) }
            ),
            TypographyRoleItem(
                index = 1,
                labelAr = "آيات القرآن",
                descriptionAr = "خط الرسم المصحفي والآيات القرآنية الكريمة والتشكيل الكامل",
                samplePhrase = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾ • ﴿ وَقُلْ رَبِّ زِدْنِي عِلْمًا ﴾",
                getSelectedFont = { it.quran },
                updateFont = { prof, f -> prof.copy(quran = f) }
            ),
            TypographyRoleItem(
                index = 2,
                labelAr = "العناوين الرئيسية",
                descriptionAr = "خط عناوين الصفحات والمباحث والرسائل الفرعية (H1, H2, H3)",
                samplePhrase = "خَيْمَةٌ فِي القَلْبِ — إِشْرَاقَةُ البِنَاءِ وَاليَقِينِ",
                getSelectedFont = { it.headings },
                updateFont = { prof, f -> prof.copy(headings = f) }
            ),
            TypographyRoleItem(
                index = 3,
                labelAr = "الأبواب والفصول",
                descriptionAr = "خط شارات الأبواب وعناوين الفصول الكبرى وفواصل الأقسام",
                samplePhrase = "البَابُ الأَوَّلُ: مَلاذُ السَّكِينَةِ وَطُمَأْنِينَةُ الرُّوحِ",
                getSelectedFont = { it.chapterTitles },
                updateFont = { prof, f -> prof.copy(chapterTitles = f) }
            ),
            TypographyRoleItem(
                index = 4,
                labelAr = "المذكرات والدرر",
                descriptionAr = "خط بطاقات الاقتباس والمذكرات اليومية والهمسات والأبيات الشعرية",
                samplePhrase = "«إِنَّ القُلُوبَ إِذَا اتَّصَلَتْ بِبَارِئِهَا أَزْهَرَتْ وَلَوْ أَحَاطَ بِهَا الرُّكَامُ»",
                getSelectedFont = { it.quotes },
                updateFont = { prof, f -> prof.copy(quotes = f) }
            ),
            TypographyRoleItem(
                index = 5,
                labelAr = "الفهارس والواجهة",
                descriptionAr = "خط الشريط العلوي، زر الفهرس المنبثق، البحث، وأزرار التنقل",
                samplePhrase = "فهرس الكتاب • البحث الفوري • حفظ علامة مرجعية",
                getSelectedFont = { it.interfaceFont },
                updateFont = { prof, f -> prof.copy(interfaceFont = f) }
            ),
            TypographyRoleItem(
                index = 6,
                labelAr = "الأرقام والعدادات",
                descriptionAr = "خط أرقام الصفحات وعدادات التقدم والفهارس المجدولة",
                samplePhrase = "صفحة ١٢ من ١٠٥ • 105 / 12 • 100%",
                getSelectedFont = { it.numbers },
                updateFont = { prof, f -> prof.copy(numbers = f) }
            )
        )
    }

    val activeRole = roles[selectedRoleIndex.coerceIn(0, roles.lastIndex)]
    val selectedFontForRole = activeRole.getSelectedFont(draftPrefs.profile)

    // Launcher for importing user custom font (.ttf / .otf / .woff2)
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
                selectedCategory = FontCategory.ALL
                activeTabMode = StudioTabMode.FINE_TUNE
                importFeedbackMessage = "تمت إضافة الخط «${importedFont.arabicDisplayName}» وتطبيقه بنجاح ✨"
            }.onFailure { err ->
                importFeedbackMessage = err.message ?: "تعذر استيراد ملف الخط"
            }
        }
    }

    val allFonts = remember(fontRegistryRefreshTick) {
        MaktabatiFontRegistry.registeredFonts
    }

    val filteredFonts = remember(allFonts, selectedCategory) {
        if (selectedCategory == FontCategory.ALL) {
            allFonts
        } else {
            allFonts.filter { it.category == selectedCategory }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("typography_settings_dialog")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // =========================================================================
                // ZONE 1: Merged Top Header + Master Switch + Close Button
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
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
                                .size(36.dp)
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
                                modifier = Modifier.size(18.dp)
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
                                    "تخصيصك مفعل على جميع الكتب"
                                } else {
                                    "الهوية الأصلية المدمجة لكل كتاب"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (draftPrefs.useCustomTypography) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // =========================================================================
                // ZONE 2 & ZONE 3 inside a smooth LazyColumn
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Calm informational banner when useCustomTypography == false
                    if (!draftPrefs.useCustomTypography) {
                        item(key = "original_identity_notice") {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "التطبيق يعرض حالياً الهوية الخطية الأصلية لكل كتاب",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "اختر أي باقة أو خط بالأسفل لتفعيل تخصيصك تلقائياً",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = {
                                            val updated = draftPrefs.copy(useCustomTypography = true)
                                            draftPrefs = updated
                                            onUpdatePreferences(updated)
                                        },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "تفعيل الآن",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ZONE 2: Focused Live Preview Card (Compact <= 140dp in normal mode)
                    item(key = "focused_live_preview") {
                        FocusedLivePreviewCard(
                            profile = draftPrefs.profile,
                            activeTabMode = activeTabMode,
                            activeRole = activeRole,
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
                            }
                        )
                    }

                    // ZONE 3: Segmented Mode Switcher («الباقات الجاهزة ✨» | «تخصيص دقيق 🎨»)
                    item(key = "mode_segmented_tabs") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val isPresetsTab = activeTabMode == StudioTabMode.PRESETS
                                Surface(
                                    onClick = { activeTabMode = StudioTabMode.PRESETS },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isPresetsTab) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Transparent
                                    },
                                    contentColor = if (isPresetsTab) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "الباقات الجاهزة ✨",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                val isFineTuneTab = activeTabMode == StudioTabMode.FINE_TUNE
                                Surface(
                                    onClick = { activeTabMode = StudioTabMode.FINE_TUNE },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isFineTuneTab) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        Color.Transparent
                                    },
                                    contentColor = if (isFineTuneTab) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تخصيص دقيق 🎨",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1 CONTENT: Curated Editorial Presets (Clean Vertical Cards)
                    if (activeTabMode == StudioTabMode.PRESETS) {
                        items(
                            items = MaktabatiFontRegistry.editorialPresets,
                            key = { "preset_${it.id}" }
                        ) { preset ->
                            val isSelected =
                                draftPrefs.useCustomTypography && draftPrefs.presetId == preset.id
                            EditorialPresetVerticalCard(
                                preset = preset,
                                isSelected = isSelected,
                                dimmed = !draftPrefs.useCustomTypography,
                                onSelect = {
                                    val updated = draftPrefs.copy(
                                        useCustomTypography = true,
                                        presetId = preset.id,
                                        profile = preset.profile
                                    )
                                    draftPrefs = updated
                                    onUpdatePreferences(updated)
                                },
                                onCustomizePreset = {
                                    val updated = draftPrefs.copy(
                                        useCustomTypography = true,
                                        presetId = preset.id,
                                        profile = preset.profile
                                    )
                                    draftPrefs = updated
                                    onUpdatePreferences(updated)
                                    activeTabMode = StudioTabMode.FINE_TUNE
                                }
                            )
                        }
                    } else {
                        // TAB 2 CONTENT: Fine-Tuning by Role + Category Filter + Compact Font Rows
                        item(key = "fine_tune_controls") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.alpha(if (draftPrefs.useCustomTypography) 1f else 0.82f)
                            ) {
                                // 7 Role Tabs
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    roles.forEach { role ->
                                        val isSelected = role.index == selectedRoleIndex
                                        val roleFontName = role.getSelectedFont(draftPrefs.profile)
                                        Surface(
                                            onClick = { selectedRoleIndex = role.index },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                            },
                                            contentColor = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.testTag("typography_role_tab_${role.index}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 6.dp
                                                ),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = role.labelAr,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = roleFontName,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Category Filter Chips + "+ خط من جهازي" Button in a clean bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
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
                                        },
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("typography_import_user_font_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "خط من جهازي",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    FontCategory.entries.forEach { category ->
                                        val count = if (category == FontCategory.ALL) {
                                            allFonts.size
                                        } else {
                                            allFonts.count { it.category == category }
                                        }
                                        if (category != FontCategory.USER_CUSTOM || count > 0) {
                                            val selected = selectedCategory == category
                                            FilterChip(
                                                selected = selected,
                                                onClick = { selectedCategory = category },
                                                label = {
                                                    Text(
                                                        text = "${category.labelAr} ($count)",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }

                                if (importFeedbackMessage != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 6.dp
                                            ),
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
                                                modifier = Modifier.size(22.dp)
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
                            }
                        }

                        // Compact, Calm Font Selector Rows (56dp-64dp height, no repetitive sample wall)
                        items(
                            items = filteredFonts,
                            key = { it.familyName }
                        ) { fontSpec ->
                            val isFontSelected =
                                selectedFontForRole.equals(fontSpec.familyName, ignoreCase = true)
                            CompactFontSelectorRow(
                                fontSpec = fontSpec,
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
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // =========================================================================
                // Pinned Bottom Action Bar
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
private fun FocusedLivePreviewCard(
    profile: BookTypographyProfile,
    activeTabMode: StudioTabMode,
    activeRole: TypographyRoleItem,
    customPreviewText: String,
    isEditingPreview: Boolean,
    onToggleEditPreview: () -> Unit,
    onCustomPreviewTextChange: (String) -> Unit,
    onResetPreviewText: () -> Unit
) {
    val headingsFont = fontFamilyForName(profile.headings)
    val bodyFont = fontFamilyForName(profile.body)
    val quranFont = fontFamilyForName(profile.quran)
    val interfaceFont = fontFamilyForName(profile.interfaceFont)

    val hasCustomUserText = customPreviewText.isNotBlank() &&
        customPreviewText.trim() != UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAF6F0)
        ),
        border = BorderStroke(1.dp, Color(0xFFDDD1C4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!isEditingPreview) Modifier.heightIn(max = 140.dp) else Modifier
            )
            .testTag("typography_live_preview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: Context Pill + Free-form Preview Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF3E4EA),
                    border = BorderStroke(0.8.dp, Color(0xFFBD6B80).copy(alpha = 0.40f))
                ) {
                    val badgeText = if (activeTabMode == StudioTabMode.PRESETS) {
                        "معاينة الصفحة • ${profile.body}"
                    } else {
                        "${activeRole.labelAr} • ${activeRole.getSelectedFont(profile)}"
                    }
                    Text(
                        text = badgeText,
                        fontFamily = interfaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF732E41),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    onClick = onToggleEditPreview,
                    shape = RoundedCornerShape(50),
                    color = if (isEditingPreview) Color(0xFF732E41) else Color(0xFFEDE4D8),
                    modifier = Modifier.testTag("typography_edit_preview_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "نص معاينة حر",
                            tint = if (isEditingPreview) Color.White else Color(0xFF732E41),
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (isEditingPreview) "إغلاق المحرر" else "✍️ نص معاينة حر",
                            fontFamily = interfaceFont,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEditingPreview) Color.White else Color(0xFF732E41)
                        )
                    }
                }
            }

            // Expandable Free-Form Text Input (Only visible when toggled)
            AnimatedVisibility(
                visible = isEditingPreview,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp)
                ) {
                    OutlinedTextField(
                        value = customPreviewText,
                        onValueChange = onCustomPreviewTextChange,
                        label = {
                            Text(
                                "اكتب أي آية أو عبارة لمعاينتها فوراً بالخطوط:",
                                fontSize = 11.sp
                            )
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
                            modifier = Modifier.height(26.dp)
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

            Spacer(modifier = Modifier.height(5.dp))

            if (activeTabMode == StudioTabMode.PRESETS) {
                // Cohesive 3-element mini-page preview (Quranic Ayah + Heading + 1 Body Line)
                Text(
                    text = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                    fontFamily = quranFont,
                    fontSize = 15.sp,
                    color = Color(0xFF445946),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "مِنَ القَلْبِ: مَلاذُ السَّكِينَةِ وَالبَيَانِ",
                    fontFamily = headingsFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF732E41),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = customPreviewText.ifBlank { UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT },
                    fontFamily = bodyFont,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFF38272C),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                // Fine-Tuning Mode: Highlight & enlarge the active role being customized
                val roleFontName = activeRole.getSelectedFont(profile)
                val roleFontFamily = fontFamilyForName(roleFontName)
                val previewPhrase = if (hasCustomUserText) {
                    customPreviewText
                } else {
                    activeRole.samplePhrase
                }

                Text(
                    text = previewPhrase,
                    fontFamily = roleFontFamily,
                    fontWeight = if (activeRole.index in listOf(2, 3)) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 17.sp,
                    lineHeight = 25.sp,
                    color = if (activeRole.index == 1) Color(0xFF445946) else Color(0xFF38272C),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = activeRole.descriptionAr,
                    fontFamily = interfaceFont,
                    fontSize = 10.sp,
                    color = Color(0xFF732E41).copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EditorialPresetVerticalCard(
    preset: EditorialTypographyPreset,
    isSelected: Boolean,
    dimmed: Boolean,
    onSelect: () -> Unit,
    onCustomizePreset: () -> Unit
) {
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
        label = "presetCardBorder"
    )
    val headingFont = fontFamilyForName(preset.profile.headings)
    val bodyFont = fontFamilyForName(preset.profile.body)
    val quranFont = fontFamilyForName(preset.profile.quran)

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(if (isSelected) 1.6.dp else 0.9.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (dimmed) 0.85f else 1f)
            .testTag("typography_preset_${preset.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.titleAr,
                        fontFamily = headingFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = preset.subtitleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextButton(
                        onClick = onCustomizePreset,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "تخصيص دقيق",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
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

            Spacer(modifier = Modifier.height(6.dp))

            // Clean single-row live font specimen for the preset
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAF6F0),
                border = BorderStroke(0.7.dp, Color(0xFFDDD1C4)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المتن: ${preset.profile.body}",
                        fontFamily = bodyFont,
                        fontSize = 12.sp,
                        color = Color(0xFF38272C),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "القرآن: ${preset.profile.quran}",
                        fontFamily = quranFont,
                        fontSize = 12.sp,
                        color = Color(0xFF445946),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactFontSelectorRow(
    fontSpec: RegisteredArabicFont,
    isSelected: Boolean,
    dimmed: Boolean,
    onSelect: () -> Unit,
    onDeleteCustomFont: (() -> Unit)? = null
) {
    val fontComposeFamily = fontFamilyForName(fontSpec.familyName)

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.7.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp, max = 64.dp)
            .alpha(if (dimmed) 0.85f else 1f)
            .testTag("typography_font_option_${fontSpec.folderName}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font Display Name rendered in its own actual FontFamily + short editorial usage subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = fontSpec.arabicDisplayName,
                    fontFamily = fontComposeFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = fontSpec.editorialUsage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Left side: Delete button (for custom fonts) + Selection Checkmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
