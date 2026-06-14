package com.example.htmlquickview.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.model.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTagDialog(
    fileTags: List<Tag>,
    allTags: List<Tag>,
    onDismiss: () -> Unit,
    onAddTag: (Long) -> Unit,
    onRemoveTag: (Long) -> Unit,
    onCreateTag: (String, String) -> Unit
) {
    var showCreateTag by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(tagColors.first()) }

    val tagIds = fileTags.map { it.id }.toSet()
    val availableTags = allTags.filter { it.id !in tagIds }

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
                        text = "编辑标签",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { showCreateTag = true }) {
                        Icon(Icons.Default.Add, "新建标签")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 当前文件的标签
                if (fileTags.isNotEmpty()) {
                    Text(
                        text = "当前标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(fileTags) { tag ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(tag.color))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            InputChip(
                                onClick = { onRemoveTag(tag.id) },
                                label = { Text(tag.name) },
                                selected = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "移除",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.2f),
                                    selectedLabelColor = color,
                                    selectedTrailingIconColor = color
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 可添加的标签
                if (availableTags.isNotEmpty()) {
                    Text(
                        text = "添加标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTags) { tag ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(tag.color))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            AssistChip(
                                onClick = { onAddTag(tag.id) },
                                label = { Text(tag.name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = color.copy(alpha = 0.1f),
                                    labelColor = color,
                                    leadingIconContentColor = color
                                )
                            )
                        }
                    }
                } else if (fileTags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "暂无标签",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            TextButton(onClick = { showCreateTag = true }) {
                                Text("创建新标签")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("完成")
                    }
                }
            }
        }
    }

    // 创建标签对话框
    if (showCreateTag) {
        AlertDialog(
            onDismissRequest = { showCreateTag = false },
            title = { Text("新建标签") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text("标签名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 简化的颜色选择
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tagColors.take(8)) { colorHex ->
                            ColorItem(
                                colorHex = colorHex,
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            onCreateTag(newTagName, selectedColor)
                            newTagName = ""
                            showCreateTag = false
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTag = false }) {
                    Text("取消")
                }
            }
        )
    }
}
