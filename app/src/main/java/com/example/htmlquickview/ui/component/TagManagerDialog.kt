package com.example.htmlquickview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.model.Tag

val tagColors = listOf(
    "#F44336", "#E91E63", "#9C27B0", "#673AB7",
    "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
    "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
    "#FFC107", "#FF9800", "#FF5722", "#795548"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagerDialog(
    tags: List<Tag>,
    onDismiss: () -> Unit,
    onCreateTag: (String, String) -> Unit,
    onDeleteTag: (Tag) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(tagColors.first()) }

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
                        text = "标签管理",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "新建标签")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (tags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无标签",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            TagItem(
                                tag = tag,
                                onDelete = { onDeleteTag(tag) }
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

    // 创建标签对话框
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
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

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier.height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(tagColors) { colorHex ->
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
}

@Composable
fun TagItem(
    tag: Tag,
    onDelete: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(tag.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ColorItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "选中",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
