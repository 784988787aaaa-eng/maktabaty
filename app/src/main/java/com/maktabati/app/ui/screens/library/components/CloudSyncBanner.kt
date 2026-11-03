package com.maktabati.app.ui.screens.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maktabati.app.data.backup.CloudSyncState

@Composable
fun CloudSyncBanner(
    cloudSyncState: CloudSyncState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = cloudSyncState !is CloudSyncState.Idle,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        if (cloudSyncState !is CloudSyncState.Idle) {
            val containerColor = when (cloudSyncState) {
                is CloudSyncState.Syncing -> MaterialTheme.colorScheme.secondaryContainer
                is CloudSyncState.Success -> Color(0xFFECFDF5)
                is CloudSyncState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val contentColor = when (cloudSyncState) {
                is CloudSyncState.Syncing -> MaterialTheme.colorScheme.onSecondaryContainer
                is CloudSyncState.Success -> Color(0xFF065F46)
                is CloudSyncState.Error -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val messageText = when (cloudSyncState) {
                is CloudSyncState.Syncing -> cloudSyncState.message
                is CloudSyncState.Success -> cloudSyncState.message
                is CloudSyncState.Error -> cloudSyncState.message
                else -> ""
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = containerColor,
                border = BorderStroke(
                    0.8.dp,
                    contentColor.copy(alpha = 0.28f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("cloud_sync_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        when (cloudSyncState) {
                            is CloudSyncState.Syncing -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = contentColor
                                )
                            }
                            is CloudSyncState.Success -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            is CloudSyncState.Error -> {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            else -> Unit
                        }
                        Text(
                            text = messageText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }

                    if (cloudSyncState !is CloudSyncState.Syncing) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إغلاق التنبيه",
                                tint = contentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
