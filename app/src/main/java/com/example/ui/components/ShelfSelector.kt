package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Shelf
import com.example.ui.viewmodel.FilterType

fun getShelfIcon(iconName: String): ImageVector {
    return when (iconName) {
        "history_edu" -> Icons.Default.HistoryEdu
        "science" -> Icons.Default.Science
        "code" -> Icons.Default.Code
        "star" -> Icons.Default.Star
        else -> Icons.Default.Category
    }
}

/**
 * Unified single-row horizontally scrollable bar combining status filters and user shelves:
 * [الكل] • [قيد القراءة] • [المكتملة] • [★ المفضلة] | [رفوف المستخدم] • [+ رف جديد]
 */
@Composable
fun UnifiedShelfAndFilterRow(
    shelves: List<Shelf>,
    selectedShelfId: String?,
    filterType: FilterType,
    onSelectFilter: (FilterType) -> Unit,
    onSelectShelf: (String?) -> Unit,
    onAddShelfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultShelfIds = setOf("shelf_literature", "shelf_science", "shelf_tech")
    val userShelves = shelves.filter { it.id !in defaultShelfIds }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. [الكل]
        FilterChip(
            selected = selectedShelfId == null && filterType == FilterType.ALL,
            onClick = {
                onSelectShelf(null)
                onSelectFilter(FilterType.ALL)
            },
            label = {
                Text(
                    text = "الكل",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = chipColors,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("filter_all")
        )

        // 2. [قيد القراءة]
        FilterChip(
            selected = selectedShelfId == null && filterType == FilterType.READING,
            onClick = {
                onSelectShelf(null)
                onSelectFilter(FilterType.READING)
            },
            label = {
                Text(
                    text = "قيد القراءة",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = chipColors,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("filter_reading")
        )

        // 3. [المكتملة]
        FilterChip(
            selected = selectedShelfId == null && filterType == FilterType.COMPLETED,
            onClick = {
                onSelectShelf(null)
                onSelectFilter(FilterType.COMPLETED)
            },
            label = {
                Text(
                    text = "المكتملة",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = chipColors,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("filter_completed")
        )

        // 4. [★ المفضلة]
        FilterChip(
            selected = filterType == FilterType.FAVORITES || selectedShelfId == "favorites",
            onClick = {
                onSelectShelf(null)
                onSelectFilter(FilterType.FAVORITES)
            },
            label = {
                Text(
                    text = "★ المفضلة",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            },
            colors = chipColors,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("filter_favorites")
        )

        // Thin vertical separator before user shelves & "+ رف جديد"
        VerticalDivider(
            modifier = Modifier
                .height(20.dp)
                .padding(horizontal = 2.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // 5. User Custom Shelves (if any)
        for (shelf in userShelves) {
            FilterChip(
                selected = selectedShelfId == shelf.id,
                onClick = {
                    onSelectFilter(FilterType.ALL)
                    onSelectShelf(shelf.id)
                },
                label = {
                    Text(
                        text = shelf.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = getShelfIcon(shelf.iconName),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = chipColors,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("shelf_chip_${shelf.id}")
            )
        }

        // 6. [+ رف جديد]
        OutlinedButton(
            onClick = onAddShelfClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier
                .height(36.dp)
                .testTag("add_shelf_button")
        ) {
            Text(
                text = "+ رف جديد",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ShelfSelector(
    shelves: List<Shelf>,
    selectedShelfId: String?,
    onSelectShelf: (String?) -> Unit,
    onAddShelfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UnifiedShelfAndFilterRow(
        shelves = shelves,
        selectedShelfId = selectedShelfId,
        filterType = FilterType.ALL,
        onSelectFilter = {},
        onSelectShelf = onSelectShelf,
        onAddShelfClick = onAddShelfClick,
        modifier = modifier
    )
}
