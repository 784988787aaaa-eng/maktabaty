package com.maktabati.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.viewmodel.FilterType

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
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    val defaultShelfIds = setOf("shelf_literature", "shelf_science", "shelf_tech")
    val userShelves = shelves.filter { it.id !in defaultShelfIds }
    val selectedShelf = userShelves.find { it.id == selectedShelfId }

    var statusMenuExpanded by remember { mutableStateOf(false) }
    var shelvesMenuExpanded by remember { mutableStateOf(false) }
    val goldAccent = Color(0xFFC89B3C)

    val statusOptions = remember(isEn) {
        if (isEn) {
            listOf(
                StatusOption(FilterType.ALL, "All", "filter_all"),
                StatusOption(FilterType.READING, "Reading", "filter_reading"),
                StatusOption(FilterType.COMPLETED, "Completed", "filter_completed"),
                StatusOption(FilterType.FAVORITES, "Favorites ★", "filter_favorites")
            )
        } else {
            listOf(
                StatusOption(FilterType.ALL, "الكل", "filter_all"),
                StatusOption(FilterType.READING, "قيد القراءة", "filter_reading"),
                StatusOption(FilterType.COMPLETED, "المكتملة", "filter_completed"),
                StatusOption(FilterType.FAVORITES, "المفضلة ★", "filter_favorites")
            )
        }
    }

    val activeStatusLabel = statusOptions.find { it.type == filterType }?.label ?: if (isEn) "All" else "الكل"
    val isFilterHighlighted = filterType != FilterType.ALL

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(0.92f)) {
            Surface(
                onClick = { statusMenuExpanded = true },
                shape = RoundedCornerShape(9.dp),
                color = if (isFilterHighlighted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
                },
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (isFilterHighlighted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        goldAccent.copy(alpha = 0.35f)
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
                        .padding(horizontal = 8.dp),
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
                                goldAccent
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeStatusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.5.sp,
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
                        contentDescription = if (isEn) "Open Reading Status Menu" else "فتح قائمة حالة القراءة",
                        tint = if (isFilterHighlighted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = statusMenuExpanded,
                onDismissRequest = { statusMenuExpanded = false },
                shape = RoundedCornerShape(13.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(175.dp)
                    .border(0.8.dp, goldAccent.copy(alpha = 0.38f), RoundedCornerShape(13.dp))
            ) {
                statusOptions.forEach { option ->
                    val isSelected = filterType == option.type
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                fontSize = 12.sp,
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
                                    contentDescription = if (isEn) "Selected" else "محدد",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        } else null,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        onClick = {
                            statusMenuExpanded = false
                            if (option.type == FilterType.ALL) {
                                onSelectShelf(null)
                            }
                            onSelectFilter(option.type)
                        },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag(option.testTag)
                    )
                }
            }
        }

        VerticalDivider(
            modifier = Modifier.height(18.dp),
            thickness = 0.8.dp,
            color = goldAccent.copy(alpha = 0.32f)
        )

        Box(modifier = Modifier.weight(1.15f)) {
            val isShelfSelected = selectedShelf != null
            val activeShelfColor = if (selectedShelf != null) {
                parseSafeColor(selectedShelf.colorHex, MaterialTheme.colorScheme.primary)
            } else {
                MaterialTheme.colorScheme.primary
            }

            Surface(
                onClick = { shelvesMenuExpanded = true },
                shape = RoundedCornerShape(9.dp),
                color = if (isShelfSelected) {
                    activeShelfColor.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
                },
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (isShelfSelected) {
                        activeShelfColor
                    } else {
                        goldAccent.copy(alpha = 0.35f)
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
                        .padding(horizontal = 8.dp),
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
                                goldAccent
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedShelf?.name ?: if (isEn) "All Shelves" else "جميع الرفوف",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.5.sp,
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
                        contentDescription = if (isEn) "Open Shelves Menu" else "فتح قائمة الرفوف",
                        tint = if (isShelfSelected) {
                            activeShelfColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = shelvesMenuExpanded,
                onDismissRequest = { shelvesMenuExpanded = false },
                shape = RoundedCornerShape(13.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(195.dp)
                    .border(0.8.dp, goldAccent.copy(alpha = 0.38f), RoundedCornerShape(13.dp))
            ) {
                val allShelvesSelected = selectedShelfId == null
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isEn) "All Shelves" else "جميع الرفوف",
                            fontSize = 12.sp,
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
                                goldAccent
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = if (allShelvesSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = if (isEn) "Selected" else "محدد",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else null,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    onClick = {
                        shelvesMenuExpanded = false
                        onSelectShelf(null)
                    },
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("shelf_option_all")
                )

                if (userShelves.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        thickness = 0.6.dp,
                        color = goldAccent.copy(alpha = 0.25f)
                    )
                }

                userShelves.forEach { shelf ->
                    val isSelected = selectedShelfId == shelf.id
                    val shelfColor = parseSafeColor(shelf.colorHex, MaterialTheme.colorScheme.primary)

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = shelf.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) shelfColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = if (isEn) "Selected" else "محدد",
                                        tint = shelfColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = getShelfIcon(shelf.iconName),
                                contentDescription = null,
                                tint = shelfColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    onDeleteShelf(shelf)
                                },
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("delete_shelf_${shelf.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = if (isEn) "Delete shelf ${shelf.name}" else "حذف الرف ${shelf.name}",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        onClick = {
                            shelvesMenuExpanded = false
                            onSelectShelf(shelf.id)
                        },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("shelf_chip_${shelf.id}")
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onAddShelfClick,
            shape = RoundedCornerShape(9.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(0.8.dp, goldAccent.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxHeight()
                .testTag("add_shelf_button")
        ) {
            Text(
                text = if (isEn) "+ Shelf" else "+ رف جديد",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.5.sp,
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
