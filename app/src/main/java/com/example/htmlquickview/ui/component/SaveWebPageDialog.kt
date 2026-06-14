package com.example.htmlquickview.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HtmlFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveWebPageDialog(
    htmlFile: HtmlFile,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var sourceUrl by remember { mutableStateOf(htmlFile.sourceUrl ?: "") }
    var fileName by remember { mutableStateOf(htmlFile.fileName) }
    var isSaving by remember { mutableStateOf(false) }
    var saveProgress by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("准备保存...") }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "保存网页为离线",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isSaving) {
                    // 输入表单
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("文件名") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sourceUrl,
                        onValueChange = { sourceUrl = it },
                        label = { Text("原始URL（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "将下载HTML页面及所有CSS、图片等资源",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isSaving = true
                                onSave(sourceUrl, fileName)
                            }
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存")
                        }
                    }
                } else {
                    // 保存进度
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { saveProgress / 100f },
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$saveProgress%",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaveProgressDialog(
    progress: Int,
    status: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("保存网页中...") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {}
    )
}
