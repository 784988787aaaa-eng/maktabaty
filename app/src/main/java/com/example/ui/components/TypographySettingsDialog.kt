package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.typography.BookTypographyProfile
import com.example.data.typography.MaktabatiFontRegistry
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

private val FONT_ARABIC_NAMES = mapOf(
    "Amiri" to "الأميري التراثي (Amiri)",
    "Noto Naskh Arabic" to "نوتو نسخ السردي (Noto Naskh)",
    "Aref Ruqaa" to "عارف رقعة الأدبي (Aref Ruqaa)",
    "Reem Kufi" to "ريم كوفي الهندسي (Reem Kufi)",
    "IBM Plex Sans Arabic" to "آي بي إم بليكس المعاصر (IBM Plex)",
    "Noto Sans Arabic" to "نوتو سانس الواضح (Noto Sans)",
    "Noto Kufi Arabic" to "نوتو كوفي الحديث (Noto Kufi)"
)

@Composable
fun TypographySettingsDialog(
    currentPreferences: UserTypographyPreferences,
    onUpdatePreferences: (UserTypographyPreferences) -> Unit,
    onDismiss: () -> Unit
) {
    var draftPrefs by remember(currentPreferences) { mutableStateOf(currentPreferences) }
    var selectedRoleIndex by remember { mutableIntStateOf(0) }

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
                labelAr = "العناوين الرئيسية",
                descriptionAr = "خط عناوين الصفحات والمباحث والافتتاحيات الفرعية (H1, H2, H3)",
                samplePhrase = "خَيْمَةٌ فِي القَلْبِ — إِشْرَاقَةُ البِنَاءِ",
                getSelectedFont = { it.headings },
                updateFont = { prof, f -> prof.copy(headings = f) }
            ),
            TypographyRoleItem(
                index = 2,
                labelAr = "الأبواب والفصول",
                descriptionAr = "خط شارات الأبواب وعناوين الفصول الكبرى وفواصل الأقسام",
                samplePhrase = "البَابُ الأَوَّلُ: مَلاذُ السَّكِينَةِ وَاليَقِينِ",
                getSelectedFont = { it.chapterTitles },
                updateFont = { prof, f -> prof.copy(chapterTitles = f) }
            ),
            TypographyRoleItem(
                index = 3,
                labelAr = "الاقتباسات والدرر",
                descriptionAr = "خط بطاقات الاقتباس ودرر الحكمة والخواطر والأبيات الشعرية",
                samplePhrase = "«إِنَّ القُلُوبَ إِذَا اتَّصَلَتْ بِبَارِئِهَا أَزْهَرَتْ وَلَوْ أَحَاطَ بِهَا الرُّكَامُ»",
                getSelectedFont = { it.quotes },
                updateFont = { prof, f -> prof.copy(quotes = f) }
            ),
            TypographyRoleItem(
                index = 4,
                labelAr = "الفهارس والواجهة",
                descriptionAr = "خط الشريط العلوي، الفهرس، البحث، الشارات، وأزرار التنقل",
                samplePhrase = "فهرس الرحلة • البحث في الكتاب • حفظ علامة مرجعية",
                getSelectedFont = { it.interfaceFont },
                updateFont = { prof, f -> prof.copy(interfaceFont = f) }
            ),
            TypographyRoleItem(
                index = 5,
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 700.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("typography_settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // 1. Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                text = "تخصيص خطوط العناوين والمتن والتفاصيل في الكتب والتطبيق",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("typography_dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق استوديو الخطوط"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Custom vs Book-Default Toggle Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (draftPrefs.useCustomTypography) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (draftPrefs.useCustomTypography) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                                    "يتم تطبيق خطوطك المختارة أدناه فوراً على العناوين والمتن والاقتباسات"
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

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Live Interactive Book Page Preview Card
                LiveBookTypographyPreviewCard(profile = draftPrefs.profile)

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Curated 1-Tap Editorial Presets
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
                        val isPresetSelected = draftPrefs.useCustomTypography && draftPrefs.presetId == preset.id
                        val borderColor by animateColorAsState(
                            if (isPresetSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            label = "presetBorder"
                        )
                        Surface(
                            onClick = {
                                val updated = UserTypographyPreferences(
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
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(if (isPresetSelected) 1.5.dp else 1.dp, borderColor),
                            modifier = Modifier
                                .width(172.dp)
                                .testTag("typography_preset_${preset.id}")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
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
                                            modifier = Modifier.size(16.dp)
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

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Granular Role Selector Tabs (المتن، العناوين، الفصول، الاقتباسات، الواجهة، الأرقام)
                Text(
                    text = "تخصيص الخط لكل تفصيل وعنوان في الكتاب:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

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
                            shape = RoundedCornerShape(10.dp),
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = activeRole.descriptionAr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 6. Font Cards for the Active Role (All 7 Approved Offline Arabic Font Families)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MaktabatiFontRegistry.registeredFonts.forEach { fontSpec ->
                        val isFontSelected = selectedFontForRole.equals(fontSpec.familyName, ignoreCase = true)
                        val fontComposeFamily = fontFamilyForName(fontSpec.familyName)
                        Surface(
                            onClick = {
                                val nextProfile = activeRole.updateFont(draftPrefs.profile, fontSpec.familyName)
                                val updated = UserTypographyPreferences(
                                    useCustomTypography = true,
                                    presetId = "custom",
                                    profile = nextProfile
                                )
                                draftPrefs = updated
                                onUpdatePreferences(updated)
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFontSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                            },
                            border = BorderStroke(
                                width = if (isFontSelected) 1.5.dp else 0.8.dp,
                                color = if (isFontSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("typography_font_option_${fontSpec.folderName}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = FONT_ARABIC_NAMES[fontSpec.familyName] ?: fontSpec.familyName,
                                            fontFamily = fontComposeFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "• ${fontSpec.editorialUsage}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = activeRole.samplePhrase,
                                        fontFamily = fontComposeFamily,
                                        fontSize = 15.sp,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isFontSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
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
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7. Footer Actions (Reset to Book Default & Save/Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val resetPrefs = UserTypographyPreferences(
                                useCustomTypography = false,
                                presetId = "editorial_narrative",
                                profile = BookTypographyProfile()
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
                        Text("اعتماد التفضيلات", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveBookTypographyPreviewCard(profile: BookTypographyProfile) {
    val chapterFont = fontFamilyForName(profile.chapterTitles)
    val headingsFont = fontFamilyForName(profile.headings)
    val bodyFont = fontFamilyForName(profile.body)
    val quotesFont = fontFamilyForName(profile.quotes)
    val interfaceFont = fontFamilyForName(profile.interfaceFont)
    val numbersFont = fontFamilyForName(profile.numbers)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFAFC)
        ),
        border = BorderStroke(1.dp, Color(0xFFE5CDD8)),
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
            // Top mini bar (Interface + Chapter Title)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFF9DFE9),
                    border = BorderStroke(0.8.dp, Color(0xFFD47D9E).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "✦ الباب الأول • ${profile.chapterTitles}",
                        fontFamily = chapterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF91405F),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "معاينة حية للكتاب",
                    fontFamily = interfaceFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB94F7A)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Heading
            Text(
                text = "خَيْمَةٌ فِي القَلْبِ: مَلاذُ السَّكِينَةِ",
                fontFamily = headingsFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF91405F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body Text
            Text(
                text = "هكذا يظهر نص المتن السردي بتشكيله الكامل وانسيابيته المريحة للعين داخل صفحات الكتب في تطبيق مكتبتي.",
                fontFamily = bodyFont,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = Color(0xFF6B2D46),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quote Pearl Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8E8EE),
                border = BorderStroke(1.dp, Color(0xFFB88945).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "«إِنَّ البِنَاءَ الحَقِيقِيَّ يَبْدَأُ مِنْ دَاخِلِ الرُّوحِ حِينَ تَأْوِي إِلَى خَيْمَةِ يَقِينِهَا»",
                    fontFamily = quotesFont,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF91405F),
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
                    text = "المتن: ${profile.body} • العناوين: ${profile.headings}",
                    fontFamily = interfaceFont,
                    fontSize = 10.sp,
                    color = Color(0xFFA36D82),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "12 / 105",
                    fontFamily = numbersFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB88945)
                )
            }
        }
    }
}
