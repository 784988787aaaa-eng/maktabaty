package com.maktabati.app.ui.screens.library.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maktabati.app.ui.localization.AppLanguage
import com.maktabati.app.ui.localization.LocalAppStrings
import com.maktabati.app.ui.viewmodel.ViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryTopAppBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onActivateSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewMode: ViewMode,
    onToggleViewMode: () -> Unit,
    appLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    useCustomTypography: Boolean,
    onOpenTypographySettings: () -> Unit,
    connectedGoogleEmail: String?,
    lastBackupTimeText: String?,
    onGoogleSignInClick: () -> Unit,
    onCloudBackupNow: () -> Unit,
    onCloudRestoreNow: () -> Unit,
    onCloudSignOut: () -> Unit,
    onOpenAboutDialog: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isEn = strings.language == AppLanguage.EN
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            if (isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(strings.searchPlaceholder) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onCloseSearch) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = strings.closeSearchContentDescription)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_search_input")
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.appTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(
                    onClick = onActivateSearch,
                    modifier = Modifier.testTag("search_icon_button")
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = strings.searchBooksContentDescription)
                }
            }

            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier.testTag("toggle_theme_button")
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = strings.toggleThemeModeContentDescription
                )
            }

            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier.testTag("toggle_view_mode_button")
            ) {
                Icon(
                    imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = strings.toggleViewModeContentDescription
                )
            }

            IconButton(
                onClick = onToggleLanguage,
                modifier = Modifier.testTag("language_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = strings.toggleLanguageContentDescription
                )
            }

            IconButton(
                onClick = onOpenTypographySettings,
                modifier = Modifier.testTag("typography_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.typographyStudioContentDescription,
                    tint = if (useCustomTypography) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            Box {
                IconButton(
                    onClick = { showMoreOptionsMenu = true },
                    modifier = Modifier.testTag("about_icon_button")
                ) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = strings.cloudAndBackupContentDescription
                        )
                        if (connectedGoogleEmail != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = showMoreOptionsMenu,
                    onDismissRequest = { showMoreOptionsMenu = false },
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 14.dp,
                    modifier = Modifier
                        .width(290.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("more_options_dropdown_menu")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (connectedGoogleEmail != null) {
                                        Icons.Default.CloudDone
                                    } else {
                                        Icons.Default.Cloud
                                    },
                                    contentDescription = null,
                                    tint = if (connectedGoogleEmail != null) {
                                        Color(0xFF10B981)
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isEn) "Cloud & Backup" else "السحابة والنسخ الاحتياطي",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (connectedGoogleEmail != null) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF10B981).copy(alpha = 0.14f),
                                    border = BorderStroke(0.8.dp, Color(0xFF10B981).copy(alpha = 0.45f)),
                                    modifier = Modifier.testTag("cloud_connected_badge")
                                ) {
                                    Text(
                                        text = if (isEn) "Connected ☁️" else "متصل بالسحابة ☁️",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (connectedGoogleEmail == null) {
                            Text(
                                text = if (isEn) {
                                    "Link your Google account to automatically back up your books, shelves, and reading progress."
                                } else {
                                    "اربط حساب Google لحفظ كتبك ورفوفك وتقدم قراءتك تلقائياً في سحابتك الخاصة."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Button(
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onGoogleSignInClick()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_sign_in_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Sign in with Google" else "تسجيل الدخول بحساب Google",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = connectedGoogleEmail,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (!lastBackupTimeText.isNullOrBlank()) {
                                            if (isEn) "Last backup: $lastBackupTimeText" else "آخر نسخة احتياطية: $lastBackupTimeText"
                                        } else {
                                            if (isEn) "Auto-backup active when adding books" else "النسخ التلقائي مفعل عند إضافة أي كتاب"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onCloudBackupNow()
                                },
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cloud_backup_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Save Backup Now" else "حفظ نسخة احتياطية الآن",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    showMoreOptionsMenu = false
                                    onCloudRestoreNow()
                                },
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cloud_restore_backup_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isEn) "Restore Backup" else "استرداد النسخة الاحتياطية",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        showMoreOptionsMenu = false
                                        onCloudSignOut()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .testTag("cloud_sign_out_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Logout,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isEn) "Sign out or switch account" else "تسجيل الخروج أو تبديل الحساب",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isEn) "Switch Language (${if (appLanguage == AppLanguage.AR) "English" else "العربية"})" else "تبديل اللغة (${if (appLanguage == AppLanguage.AR) "English" else "العربية"})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMoreOptionsMenu = false
                            onToggleLanguage()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isEn) "About Maktabati Platform" else "عن منصة مكتبتي",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        onClick = {
                            showMoreOptionsMenu = false
                            onOpenAboutDialog()
                        },
                        modifier = Modifier.testTag("about_menu_item")
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
