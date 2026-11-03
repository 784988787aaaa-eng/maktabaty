package com.maktabati.app.ui.screens.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maktabati.app.data.model.Book
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings

@Composable
fun ReadingStatsHeaderBar(
    allRawBooks: List<Book>,
    isScrollingDown: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN
    val readingBooksCount = allRawBooks.count { it.readingProgress > 0f && it.readingProgress < 0.95f }
    val averageProgress = if (allRawBooks.isEmpty()) {
        0f
    } else {
        allRawBooks.map { it.readingProgress.coerceIn(0f, 1f) }.average().toFloat()
    }

    AnimatedVisibility(
        visible = allRawBooks.isNotEmpty() && !isScrollingDown,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .height(32.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isEn) "Your Reading Journey" else "رحلتك مع القراءة",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isEn) "Books: ${allRawBooks.size}" else "الكتب: ${allRawBooks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (isEn) "Reading: $readingBooksCount" else "تقرأ: $readingBooksCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    LinearProgressIndicator(
                        progress = { averageProgress },
                        modifier = Modifier
                            .width(44.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}
