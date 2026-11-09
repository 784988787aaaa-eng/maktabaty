package com.maktabati.app.ui.screens.library.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maktabati.app.data.model.Book
import com.maktabati.app.data.model.Shelf
import com.maktabati.app.ui.components.parseSafeColor
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.viewmodel.ImportState

@Composable
fun DeleteBookConfirmDialog(
    book: Book,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEn) "Confirm Book Deletion" else "تأكيد حذف الكتاب من مكتبتي")
        },
        text = {
            Text(
                if (isEn) {
                    "Are you sure you want to delete \"${book.title}\" from your library? Its saved copy and reading progress will be permanently removed without affecting your original device files."
                } else {
                    "هل أنت متأكد من حذف «${book.title}» من المكتبة؟ سيتم إزالة نسخته المحفوظة وسجل القراءة نهائياً دون المساس بملفاتك الأصلية في الجهاز."
                }
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_book_button")
            ) {
                Text(if (isEn) "Yes, Delete Book" else "نعم، احذف الكتاب")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "تراجع")
            }
        }
    )
}

@Composable
fun BatchDeleteConfirmDialog(
    selectedCount: Int,
    onConfirmBatchDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("batch_delete_confirm_dialog"),
        title = {
            Text(strings.batchDeleteConfirmTitle, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(strings.batchDeleteConfirmMessage(selectedCount))
        },
        confirmButton = {
            Button(
                onClick = onConfirmBatchDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_batch_delete_button")
            ) {
                Text(if (isEn) "Delete ($selectedCount)" else "نعم، احذف ($selectedCount)")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_batch_delete_button")
            ) {
                Text(if (isEn) "Cancel" else "تراجع")
            }
        }
    )
}

@Composable
fun BatchMoveToShelfDialog(
    selectedCount: Int,
    shelves: List<Shelf>,
    onSelectShelf: (String?) -> Unit,
    onCreateNewShelfAndMove: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("batch_move_shelf_dialog"),
        title = {
            Column {
                Text(strings.batchMoveDialogTitle, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn) {
                        "Choose a destination shelf for ($selectedCount) selected books"
                    } else {
                        "اختر الرف الذي تود نقل ($selectedCount) كتب محددة إليه"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCreateNewShelfAndMove,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("batch_create_shelf_and_move_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.batchMoveCreateNewShelf, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectShelf(null) }
                                .testTag("batch_move_shelf_item_general")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryBooks,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.batchMoveGeneralShelf,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    items(shelves, key = { it.id }) { shelf ->
                        val shelfColor = parseSafeColor(shelf.colorHex, MaterialTheme.colorScheme.primary)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectShelf(shelf.id) }
                                .testTag("batch_move_shelf_item_${shelf.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = shelfColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = shelf.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEn) "Cancel" else "إلغاء")
            }
        }
    )
}

@Composable
fun ImportStateDialogs(
    importState: ImportState,
    onClearImportState: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN

    when (importState) {
        is ImportState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = { Text(if (isEn) "Importing to Maktabati" else "استيراد إلى مكتبتي") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(importState.message, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            )
        }
        is ImportState.Error -> {
            AlertDialog(
                onDismissRequest = onClearImportState,
                title = { Text(if (isEn) "File Import Notice" else "تنبيه استيراد الملف") },
                text = { Text(importState.message) },
                confirmButton = {
                    TextButton(onClick = onClearImportState) {
                        Text(if (isEn) "OK" else "حسناً")
                    }
                }
            )
        }
        is ImportState.Success -> {
            if (importState.count > 1) {
                AlertDialog(
                    onDismissRequest = onClearImportState,
                    title = { Text(if (isEn) "Import Successful" else "تم الاستيراد بنجاح") },
                    text = {
                        Text(
                            if (isEn) {
                                "Successfully imported and analyzed ${importState.count} books into your library shelves."
                            } else {
                                "تم استيراد وتحليل ${importState.count} كتب بنجاح وإضافتها إلى رفوف مكتبتي."
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = onClearImportState) {
                            Text(if (isEn) "Great" else "رائع")
                        }
                    }
                )
            } else {
                onClearImportState()
            }
        }
        else -> Unit
    }
}
