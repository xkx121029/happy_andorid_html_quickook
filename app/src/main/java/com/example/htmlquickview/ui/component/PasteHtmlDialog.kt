package com.example.htmlquickview.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.service.ShareReceiverService

@Composable
fun PasteHtmlDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var htmlContent by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var hasExtractedTitle by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val shareReceiverService = remember { ShareReceiverService(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_title_paste),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text(stringResource(R.string.label_file_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.hint_file_name)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
                OutlinedTextField(
                    value = htmlContent,
                    onValueChange = { newContent ->
                        htmlContent = newContent
                        if (!hasExtractedTitle && fileName.isBlank()) {
                            shareReceiverService.extractTitleFromHtml(newContent)?.let { extractedTitle ->
                                fileName = extractedTitle
                                hasExtractedTitle = true
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.hint_paste_html)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    placeholder = { Text("<html><body>...</body></html>") },
                    minLines = 6,
                    maxLines = 10,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shareReceiverService.isValidHtml(htmlContent)) {
                        onSave(htmlContent, fileName.takeIf { it.isNotBlank() })
                        onDismiss()
                    }
                },
                enabled = htmlContent.isNotEmpty() && shareReceiverService.isValidHtml(htmlContent),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.btn_save),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}
