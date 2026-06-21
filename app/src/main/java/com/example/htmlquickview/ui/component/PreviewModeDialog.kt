package com.example.htmlquickview.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R

enum class PreviewMode {
    LOCAL_PREVIEW,
    CHROME_PREVIEW,
    WEBVIEW_PREVIEW
}

@Composable
fun PreviewModeDialog(
    currentMode: PreviewMode,
    onDismiss: () -> Unit,
    onSelectMode: (PreviewMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_preview_mode)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.msg_select_preview_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreviewModeItem(
                        mode = PreviewMode.LOCAL_PREVIEW,
                        icon = Icons.Default.Description,
                        title = stringResource(R.string.label_local_preview),
                        description = stringResource(R.string.desc_local_preview),
                        isSelected = currentMode == PreviewMode.LOCAL_PREVIEW,
                        onSelect = {
                            onSelectMode(PreviewMode.LOCAL_PREVIEW)
                            onDismiss()
                        }
                    )

                    PreviewModeItem(
                        mode = PreviewMode.CHROME_PREVIEW,
                        icon = Icons.Default.OpenInBrowser,
                        title = stringResource(R.string.label_chrome_preview),
                        description = stringResource(R.string.desc_chrome_preview),
                        isSelected = currentMode == PreviewMode.CHROME_PREVIEW,
                        onSelect = {
                            onSelectMode(PreviewMode.CHROME_PREVIEW)
                            onDismiss()
                        }
                    )

                    PreviewModeItem(
                        mode = PreviewMode.WEBVIEW_PREVIEW,
                        icon = Icons.Default.CloudQueue,
                        title = stringResource(R.string.label_webview_preview),
                        description = stringResource(R.string.desc_webview_preview),
                        isSelected = currentMode == PreviewMode.WEBVIEW_PREVIEW,
                        onSelect = {
                            onSelectMode(PreviewMode.WEBVIEW_PREVIEW)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_ok))
            }
        }
    )
}

@Composable
fun PreviewModeItem(
    mode: PreviewMode,
    icon: ImageVector,
    title: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder()
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
