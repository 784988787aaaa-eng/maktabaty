package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Shelf

fun getShelfIcon(iconName: String): ImageVector {
    return when (iconName) {
        "history_edu" -> Icons.Default.HistoryEdu
        "science" -> Icons.Default.Science
        "code" -> Icons.Default.Code
        "star" -> Icons.Default.Star
        else -> Icons.Default.Category
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "الكل" - All Books
        FilterChip(
            selected = selectedShelfId == null,
            onClick = { onSelectShelf(null) },
            label = { Text("جميع الكتب") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("shelf_chip_all")
        )

        // "المفضلة" - Favorites
        FilterChip(
            selected = selectedShelfId == "favorites",
            onClick = { onSelectShelf("favorites") },
            label = { Text("المفضلة") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("shelf_chip_favorites")
        )

        // User Defined Shelves
        for (shelf in shelves) {
            FilterChip(
                selected = selectedShelfId == shelf.id,
                onClick = { onSelectShelf(shelf.id) },
                label = { Text(shelf.name) },
                leadingIcon = {
                    Icon(
                        imageVector = getShelfIcon(shelf.iconName),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("shelf_chip_${shelf.id}")
            )
        }

        // Add Shelf Button
        OutlinedButton(
            onClick = onAddShelfClick,
            modifier = Modifier.testTag("add_shelf_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "إضافة رف",
                modifier = Modifier.size(16.dp)
            )
            Text(text = " رف جديد", modifier = Modifier.padding(start = 4.dp))
        }
    }
}
