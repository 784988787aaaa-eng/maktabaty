package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private data class StatusOption(
    val type: FilterType,
    val label: String,
    val testTag: String
)

/**
 * Sleek 3-Button Dropdown Control Bar in a single fixed 40.dp row (no horizontal scrolling):
 * 1. Status Dropdown Button (الكل ▼)
 * 2. VerticalDivider (22.dp)
 * 3. Shelves Dropdown Button (جميع الرفوف ▼) with custom shelf icons, colors, checkmark & delete button
 * 4. Add Shelf Button (+ رف جديد)
 */
@Composable
fun UnifiedShelfAndFilterRow(
    shelves: List<Shelf>,
    selectedShelfId: String?,
    filterType: FilterType,
    onSelectFilter: (FilterType) -> Unit,
    onSelectShelf: (String?) -> Unit,
    onAddShelfClick: () -> Unit,
    onDeleteShelf: (Shelf) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val defaultShelfIds = setOf("shelf_literature", "shelf_science", "shelf_tech")
    val userShelves = shelves.filter { it.id !in defaultShelfIds }
    val selectedShelf = userShelves.find { it.id == selectedShelfId }

    var statusMenuExpanded by remember { mutableStateOf(false) }
    var shelvesMenuExpanded by remember { mutableStateOf(false) }

    val statusOptions = remember {
        listOf(
            StatusOption(FilterType.ALL, "الكل", "filter_all"),
            StatusOption(FilterType.READING, "قيد القراءة", "filter_reading"),
            StatusOption(FilterType.COMPLETED, "المكتملة", "filter_completed"),
            StatusOption(FilterType.FAVORITES, "المفضلة ★", "filter_favorites")
        )
    }

    val activeStatusLabel = statusOptions.find { it.type == filterType }?.label ?: "الكل"
    val isFilterHighlighted = filterType != FilterType.ALL

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. الزر الأول: زر حالة القراءة المدمج بسهم منسدل (Status Dropdown Button)
        Box(modifier = Modifier.weight(0.95f)) {
            Surface(
                onClick = { statusMenuExpanded = true },
                shape = RoundedCornerShape(10.dp),
                color = if (isFilterHighlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isFilterHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .testTag("status_dropdown_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = if (isFilterHighlighted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = activeStatusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isFilterHighlighted) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "فتح قائمة حالة القراءة",
                        tint = if (isFilterHighlighted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = statusMenuExpanded,
                onDismissRequest = { statusMenuExpanded = false }
            ) {
                statusOptions.forEach { option ->
                    val isSelected = filterType == option.type
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "محدد",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            statusMenuExpanded = false
                            if (option.type == FilterType.ALL) {
                                onSelectShelf(null)
                            }
                            onSelectFilter(option.type)
                        },
                        modifier = Modifier.testTag(option.testTag)
                    )
                }
            }
        }

        // ب. الفاصل الرأسي الاحترافي (VerticalDivider)
        VerticalDivider(
            modifier = Modifier.height(22.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // ج. الزر الثاني: زر الرفوف المدمج بسهم منسدل (Shelves Dropdown Button)
        Box(modifier = Modifier.weight(1.15f)) {
            val isShelfSelected = selectedShelf != null
            val activeShelfColor = if (selectedShelf != null) {
                parseSafeColor(selectedShelf.colorHex, MaterialTheme.colorScheme.primary)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Surface(
                onClick = { shelvesMenuExpanded = true },
                shape = RoundedCornerShape(10.dp),
                color = if (isShelfSelected) {
                    activeShelfColor.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isShelfSelected) {
                        activeShelfColor
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .testTag("shelves_dropdown_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = if (selectedShelf != null) {
                                getShelfIcon(selectedShelf.iconName)
                            } else {
                                Icons.Default.Category
                            },
                            contentDescription = null,
                            tint = if (isShelfSelected) {
                                activeShelfColor
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = selectedShelf?.name ?: "جميع الرفوف",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isShelfSelected) {
                                activeShelfColor
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "فتح قائمة الرفوف",
                        tint = if (isShelfSelected) {
                            activeShelfColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = shelvesMenuExpanded,
                onDismissRequest = { shelvesMenuExpanded = false }
            ) {
                // 1. خيار "جميع الرفوف"
                val allShelvesSelected = selectedShelfId == null
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "جميع الرفوف",
                            fontWeight = if (allShelvesSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (allShelvesSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = if (allShelvesSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = if (allShelvesSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "محدد",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        shelvesMenuExpanded = false
                        onSelectShelf(null)
                    },
                    modifier = Modifier.testTag("shelf_option_all")
                )

                if (userShelves.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }

                // 2. جميع الرفوف التي أضافها المستخدم بنفسه مع أيقونتها ولونها المميز وعلامة صح وزر حذف
                userShelves.forEach { shelf ->
                    val isSelected = selectedShelfId == shelf.id
                    val shelfColor = parseSafeColor(shelf.colorHex, MaterialTheme.colorScheme.primary)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = shelf.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) shelfColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "محدد",
                                        tint = shelfColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getShelfIcon(shelf.iconName),
                                contentDescription = null,
                                tint = shelfColor,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onDeleteShelf(shelf)
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_shelf_${shelf.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف الرف ${shelf.name}",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        onClick = {
                            shelvesMenuExpanded = false
                            onSelectShelf(shelf.id)
                        },
                        modifier = Modifier.testTag("shelf_chip_${shelf.id}")
                    )
                }
            }
        }

        // د. الزر الثالث: زر "+ رف جديد" (Add Shelf Button)
        OutlinedButton(
            onClick = onAddShelfClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxHeight()
                .testTag("add_shelf_button")
        ) {
            Text(
                text = "+ رف جديد",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
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
