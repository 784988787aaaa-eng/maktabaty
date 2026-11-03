package com.maktabati.app.ui.screens.library.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maktabati.app.data.importer.MultiBookArchiveProposal
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings

@Composable
fun MultiBookZipDialog(
    proposal: MultiBookArchiveProposal,
    onAddInNewShelf: () -> Unit,
    onAddStandalone: () -> Unit,
    onCancel: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN
    val archiveBooksCount = proposal.archiveBooksCount
    val zipBaseName = proposal.zipBaseName

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier.testTag("multi_book_zip_dialog"),
        title = {
            Text(
                text = if (isEn) {
                    "Multi-Book Archive ($archiveBooksCount books)"
                } else {
                    "أرشيف يضم عدة كتب ($archiveBooksCount كتب)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEn) {
                        "Detected $archiveBooksCount books inside \"$zipBaseName\". How would you like to add them to your library?"
                    } else {
                        "تم اكتشاف $archiveBooksCount كتب داخل الملف المضغوط «$zipBaseName». كيف تود إضافتها إلى مكتبتي؟"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onAddInNewShelf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multi_book_new_shelf_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "Add in a new shelf named \"$zipBaseName\""
                        } else {
                            "إضافتها في رف جديد باسم «$zipBaseName»"
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(
                    onClick = onAddStandalone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("multi_book_standalone_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isEn) {
                            "Add as standalone books (no new shelf)"
                        } else {
                            "إضافتها ككتب مستقلة (بدون رف جديد)"
                        },
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("multi_book_cancel_button")
            ) {
                Text(strings.bookDetailCancel)
            }
        }
    )
}
