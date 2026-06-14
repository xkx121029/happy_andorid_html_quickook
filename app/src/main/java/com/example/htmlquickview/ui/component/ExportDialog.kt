package com.example.htmlquickview.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.util.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    htmlFile: HtmlFile,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportEpub: () -> Unit,
    onExportHtml: () -> Unit
) {
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0) }
    var exportStatus by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isExporting) onDismiss() }) {
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
                        Icons.Default.FileDownload,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "导出文件",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = htmlFile.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!isExporting) {
                    // 导出选项
                    ExportOption(
                        icon = Icons.Default.PictureAsPdf,
                        title = "导出为 PDF",
                        description = "转换为PDF文档，便于阅读和分享",
                        onClick = onExportPdf
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExportOption(
                        icon = Icons.Default.MenuBook,
                        title = "导出为 EPUB",
                        description = "转换为电子书格式，可在阅读器中打开",
                        onClick = onExportEpub
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExportOption(
                        icon = Icons.Default.Code,
                        title = "导出为 HTML",
                        description = "保持原始HTML格式",
                        onClick = onExportHtml
                    )
                } else {
                    // 导出进度
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { exportProgress / 100f },
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "$exportProgress%",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = exportStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isExporting) "取消" else "关闭")
                    }
                }
            }
        }
    }
}

@Composable
fun ExportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ExportedFilesList(
    files: List<File>,
    onOpenFile: (File) -> Unit,
    onShareFile: (File) -> Unit,
    onDeleteFile: (File) -> Unit
) {
    if (files.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FolderOpen,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "暂无导出的文件",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        LazyColumn {
            items(files) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when {
                                file.name.endsWith(".pdf") -> Icons.Default.PictureAsPdf
                                file.name.endsWith(".epub") -> Icons.Default.MenuBook
                                else -> Icons.Default.Code
                            },
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = FileUtils.formatFileSize(file.length()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = { onOpenFile(file) }) {
                            Icon(Icons.Default.OpenInNew, "打开")
                        }
                        IconButton(onClick = { onShareFile(file) }) {
                            Icon(Icons.Default.Share, "分享")
                        }
                        IconButton(onClick = { onDeleteFile(file) }) {
                            Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
