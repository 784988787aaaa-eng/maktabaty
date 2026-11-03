package com.maktabati.app.ui.screens.library.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.maktabati.app.data.model.Book
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
