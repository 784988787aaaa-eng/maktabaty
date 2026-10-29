package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun CreateShelfDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String, iconName: String) -> Unit
) {
    var shelfName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3B82F6") }
    var selectedIcon by remember { mutableStateOf("category") }

    val colors = listOf(
        "#3B82F6", // Blue
        "#92400E", // Warm Brown
        "#0284C7", // Cyan
        "#4F46E5", // Indigo
        "#059669", // Emerald
        "#DC2626", // Red
        "#7C3AED"  // Purple
    )

    val icons = listOf(
        Pair("category", "عام"),
        Pair("history_edu", "تاريخ"),
        Pair("science", "علوم"),
        Pair("code", "تقنية"),
        Pair("star", "مميز")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء رف جديد للمكتبة", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = shelfName,
                    onValueChange = { shelfName = it },
                    label = { Text("اسم الرف (مثال: فلسفة، روايات، برمجة)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shelf_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("اختر لون الرف:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in colors) {
                        val isSelected = selectedColor == c
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(c)))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    } else Modifier
                                )
                                .clickable { selectedColor = c }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("أيقونة الرف:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for ((iconKey, label) in icons) {
                        val isSelected = selectedIcon == iconKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shelfName.isNotBlank()) {
                        onConfirm(shelfName.trim(), selectedColor, selectedIcon)
                    }
                },
                enabled = shelfName.isNotBlank(),
                modifier = Modifier.testTag("submit_shelf_button")
            ) {
                Text("حفظ الرف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
