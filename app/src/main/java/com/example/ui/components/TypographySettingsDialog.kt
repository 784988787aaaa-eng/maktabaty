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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.Settings
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
    val descriptionAr: String,
    val samplePhrase: String,
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

    // Use custom user preview text if non-blank, otherwise role's sample phrase
    val effectiveSamplePhrase = remember(draftPrefs.customPreviewText, activeRole) {
        val custom = draftPrefs.customPreviewText.trim()
        if (custom.isNotEmpty() && custom != UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT) {
            custom
        } else {
            activeRole.samplePhrase
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
                .fillMaxHeight(0.92f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("typography_settings_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // =========================================================================
                // 1. Pinned Clean Header Bar
                // =========================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 14.dp, bottom = 10.dp),
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
                                .size(38.dp)
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
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "استوديو الخطوط الطباعية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "٢٥ خطاً عربياً مدمجاً للقرآن والكتب والمذكرات + إضافة خطوطك الخاصة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

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

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // =========================================================================
                // 2. Scrollable Studio Body (LazyColumn for silky-smooth performance)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // A. Custom vs Book-Default Toggle + Import Font Button
                    item(key = "header_controls") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (draftPrefs.useCustomTypography) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (draftPrefs.useCustomTypography) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    }
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
                                            text = if (draftPrefs.useCustomTypography) {
                                                "تفضيلاتي الخطية مفعّلة في جميع الكتب"
                                            } else {
                                                "استخدام الهوية الأصلية المدمجة لكل كتاب"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (draftPrefs.useCustomTypography) {
                                                "يتم تطبيق خطوطك المختارة فوراً على المتن والقرآن والعناوين والمذكرات"
                                            } else {
                                                "فعّل المفتاح لتطبيق خطوطك المفضلة على كافة الكتب والتفاصيل"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = draftPrefs.useCustomTypography,
                                        onCheckedChange = { enabled ->
                                            val updated = draftPrefs.copy(useCustomTypography = enabled)
                                            draftPrefs = updated
                                            onUpdatePreferences(updated)
                                        },
                                        modifier = Modifier.testTag("typography_custom_switch")
                                    )
                                }
                            }

                            // Interactive Preview Card + Free-Form User Preview Text Input
                            LiveBookTypographyPreviewCard(
                                profile = draftPrefs.profile,
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
                    }

                    // B. Curated 1-Tap Editorial Presets
                    item(key = "editorial_presets") {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "باقات خطية احترافية جاهزة",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "بلمسة واحدة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(MaktabatiFontRegistry.editorialPresets) { preset ->
                                    val isPresetSelected =
                                        draftPrefs.useCustomTypography && draftPrefs.presetId == preset.id
                                    val borderColor by animateColorAsState(
                                        if (isPresetSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.30f),
                                        label = "presetBorder"
                                    )
                                    Surface(
                                        onClick = {
                                            val updated = draftPrefs.copy(
                                                useCustomTypography = true,
                                                presetId = preset.id,
                                                profile = preset.profile
                                            )
                                            draftPrefs = updated
                                            onUpdatePreferences(updated)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPresetSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                                        },
                                        border = BorderStroke(
                                            if (isPresetSelected) 1.5.dp else 1.dp,
                                            borderColor
                                        ),
                                        modifier = Modifier
                                            .width(168.dp)
                                            .testTag("typography_preset_${preset.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = preset.titleAr,
                                                    fontFamily = fontFamilyForName(preset.profile.headings),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isPresetSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = preset.subtitleAr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // C. Role Selector Tabs + Category Filter + Import User Font Button
                    item(key = "role_and_category_selectors") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تخصيص الخط لكل تفصيل وعنوان في الكتاب:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                // Add Custom Font from Device Button
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
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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
                                        text = "إضافة خط من جهازي",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

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

                            // Role Tabs (7 roles including Quran)
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
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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

                            // Category Filter Chips (الكل • القرآن والتراث • المتون والكتب • المذكرات والرقعة • العناوين والكوفي)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

                            Text(
                                text = activeRole.descriptionAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // D. Clean, Multi-Line Font Cards (Zero Awkward Truncation!)
                    items(
                        items = filteredFonts,
                        key = { it.familyName }
                    ) { fontSpec ->
                        val isFontSelected = selectedFontForRole.equals(fontSpec.familyName, ignoreCase = true)
                        FontOptionCleanCard(
                            fontSpec = fontSpec,
                            isSelected = isFontSelected,
                            samplePhrase = effectiveSamplePhrase,
                            onSelect = {
                                val nextProfile = activeRole.updateFont(draftPrefs.profile, fontSpec.familyName)
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
                                    MaktabatiFontRegistry.removeUserCustomFont(sharedPrefs, fontSpec.familyName)
                                    fontRegistryRefreshTick++
                                }
                            } else null
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // =========================================================================
                // 3. Pinned Bottom Action Bar (Always accessible without scrolling)
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
private fun FontOptionCleanCard(
    fontSpec: RegisteredArabicFont,
    isSelected: Boolean,
    samplePhrase: String,
    onSelect: () -> Unit,
    onDeleteCustomFont: (() -> Unit)? = null
) {
    val fontComposeFamily = fontFamilyForName(fontSpec.familyName)

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.6.dp else 0.8.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typography_font_option_${fontSpec.folderName}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Row: Full Font Name + Category Badge + Selection Checkmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fontSpec.arabicDisplayName,
                        fontFamily = fontComposeFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = fontSpec.editorialUsage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        lineHeight = 15.sp
                    )
                }

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
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Full Sample Text Box (Multi-line, never awkwardly cut off)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = samplePhrase,
                    fontFamily = fontComposeFamily,
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveBookTypographyPreviewCard(
    profile: BookTypographyProfile,
    customPreviewText: String,
    isEditingPreview: Boolean,
    onToggleEditPreview: () -> Unit,
    onCustomPreviewTextChange: (String) -> Unit,
    onResetPreviewText: () -> Unit
) {
    val chapterFont = fontFamilyForName(profile.chapterTitles)
    val headingsFont = fontFamilyForName(profile.headings)
    val bodyFont = fontFamilyForName(profile.body)
    val quotesFont = fontFamilyForName(profile.quotes)
    val quranFont = fontFamilyForName(profile.quran)
    val interfaceFont = fontFamilyForName(profile.interfaceFont)
    val numbersFont = fontFamilyForName(profile.numbers)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFAF6F0)
        ),
        border = BorderStroke(1.dp, Color(0xFFDDD1C4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("typography_live_preview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top mini bar: Chapter Badge + Free-form Edit Toggle Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF3E4EA),
                    border = BorderStroke(0.8.dp, Color(0xFFBD6B80).copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "✦ الباب الأول • ${profile.chapterTitles}",
                        fontFamily = chapterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF732E41),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    onClick = onToggleEditPreview,
                    shape = RoundedCornerShape(50),
                    color = if (isEditingPreview) Color(0xFF732E41) else Color(0xFFEDE4D8),
                    modifier = Modifier.testTag("typography_edit_preview_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "كتابة نص معاينة حر",
                            tint = if (isEditingPreview) Color.White else Color(0xFF732E41),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isEditingPreview) "إخفاء المحرر" else "اكتب نصك للمعاينة ✍️",
                            fontFamily = interfaceFont,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEditingPreview) Color.White else Color(0xFF732E41)
                        )
                    }
                }
            }

            // Expandable Free-Form User Preview Text Input
            AnimatedVisibility(
                visible = isEditingPreview,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = customPreviewText,
                        onValueChange = onCustomPreviewTextChange,
                        label = {
                            Text(
                                "اكتب أي آية أو خاطرة أو جملة لمعاينتها بجميع الخطوط بحرية:",
                                fontSize = 11.sp
                            )
                        },
                        placeholder = {
                            Text("اكتب نصك المفضل هنا ليظهر فوراً في بطاقة المعاينة وقائمة الخطوط...")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFFFDF9),
                            unfocusedContainerColor = Color(0xFFFFFDF9),
                            focusedTextColor = Color(0xFF38272C),
                            unfocusedTextColor = Color(0xFF38272C),
                            focusedBorderColor = Color(0xFFBD6B80),
                            unfocusedBorderColor = Color(0xFFDDD1C4)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
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
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "استعادة النص الافتراضي",
                                fontSize = 11.sp,
                                color = Color(0xFF732E41)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Heading
            Text(
                text = "مِنَ القَلْبِ: مَلاذُ السَّكِينَةِ وَالبَيَانِ",
                fontFamily = headingsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF732E41),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Quranic Ayah Preview Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFFFDF9),
                border = BorderStroke(1.dp, Color(0xFFC69A4E).copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾",
                    fontFamily = quranFont,
                    fontSize = 16.sp,
                    lineHeight = 25.sp,
                    color = Color(0xFF445946),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Live User Custom Text / Body Text
            Text(
                text = customPreviewText.ifBlank { UserTypographyPreferences.DEFAULT_CUSTOM_PREVIEW_TEXT },
                fontFamily = bodyFont,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF38272C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quote / Diary Pearl Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF6ECEF),
                border = BorderStroke(1.dp, Color(0xFFBD6B80).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "«إِنَّ البِنَاءَ الحَقِيقِيَّ يَبْدَأُ مِنْ دَاخِلِ الرُّوحِ حِينَ تَأْوِي إِلَى خَيْمَةِ يَقِينِهَا»",
                    fontFamily = quotesFont,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF732E41),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Counter & Role summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المتن: ${profile.body} • القرآن: ${profile.quran} • العناوين: ${profile.headings}",
                    fontFamily = interfaceFont,
                    fontSize = 10.sp,
                    color = Color(0xFF6B565C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "12 / 105",
                    fontFamily = numbersFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC69A4E)
                )
            }
        }
    }
}
