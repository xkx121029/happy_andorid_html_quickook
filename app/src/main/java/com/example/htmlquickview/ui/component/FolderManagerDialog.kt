package com.example.htmlquickview.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.model.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderManagerDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onRenameFolder: (Folder, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingFolder by remember { mutableStateOf<Folder?>(null) }
    var newFolderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "文件夹管理",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "新建文件夹")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (folders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无文件夹",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(folders) { folder ->
                            FolderItem(
                                folder = folder,
                                onRename = { editingFolder = folder },
                                onDelete = { onDeleteFolder(folder) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }

    // 创建文件夹对话框
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("文件夹名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName)
                            newFolderName = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑文件夹对话框
    editingFolder?.let { folder ->
        var renameText by remember { mutableStateOf(folder.name) }
        AlertDialog(
            onDismissRequest = { editingFolder = null },
            title = { Text("重命名文件夹") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("文件夹名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameFolder(folder, renameText)
                            editingFolder = null
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFolder = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun FolderItem(
    folder: Folder,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRename)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Row {
            IconButton(onClick = onRename) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "重命名",
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
