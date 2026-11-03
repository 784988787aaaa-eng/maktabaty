package com.maktabati.app.ui.screens.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings

@Composable
fun ReaderEdgeControlPill(
    pillCollapsed: Boolean,
    onExpandPill: () -> Unit,
    onCollapsePill: () -> Unit,
    onBackClick: () -> Unit,
    onToggleTools: () -> Unit,
    onOpenTypography: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    Row(
        modifier = modifier
            .padding(start = if (pillCollapsed) 0.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!pillCollapsed) {
            Surface(
                onClick = onBackClick,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(50))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .testTag("reader_back_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.readerBackToLibrary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(strings.appTitle, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                onClick = onToggleTools,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(38.dp)
                    .shadow(6.dp, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                    .testTag("reader_tools_toggle_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = if (isEn) "Toggle reading tools" else "فتح أو إغلاق أدوات القراءة",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Surface(
                onClick = onOpenTypography,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(38.dp)
                    .shadow(6.dp, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape)
                    .testTag("reader_pill_typography_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.TextFormat,
                        contentDescription = strings.readerTypographySettings,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Surface(
                onClick = onCollapsePill,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("reader_pill_collapse_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = if (isEn) "Collapse pill" else "طي الكبسولة",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        } else {
            Surface(
                onClick = onExpandPill,
                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier
                    .size(width = 18.dp, height = 56.dp)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(10.dp)
                    )
                    .testTag("reader_pill_expand_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.65f))
                    )
                }
            }
        }
    }
}
