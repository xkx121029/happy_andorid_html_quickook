package com.example.htmlquickview.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.htmlquickview.R
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.AnnotationType
import java.text.SimpleDateFormat
import java.util.Locale

val annotationColors = listOf(
    "#FFFF00", // 黄色
    "#FF9800", // 橙色
    "#4CAF50", // 绿色
    "#2196F3", // 蓝色
    "#9C27B0", // 紫色
    "#E91E63", // 粉色
    "#F44336", // 红色
    "#795548"  // 棕色
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationPanel(
    annotations: List<Annotation>,
    onDismiss: () -> Unit,
    onAddHighlight: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddBookmark: (String) -> Unit,
    onDeleteAnnotation: (Annotation) -> Unit,
    onExportAnnotations: () -> Unit
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(annotationColors.first()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, "批注", tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "批注 (${annotations.size})",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 操作按钮
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = onAddHighlight,
                            label = { Text("高亮") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Highlight,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showAddNoteDialog = true },
                            label = { Text("笔记") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Note,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showAddBookmarkDialog = true },
                            label = { Text("书签") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Bookmark,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = onExportAnnotations,
                            label = { Text("导出") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Upload,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 批注列表
                if (annotations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Notes,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "暂无批注",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(annotations) { annotation ->
                            AnnotationItem(
                                annotation = annotation,
                                onDelete = { onDeleteAnnotation(annotation) }
                            )
                        }
                    }
                }
            }
        }
    }

    // 添加笔记对话框
    if (showAddNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("添加笔记") },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("笔记内容") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("选择颜色", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(annotationColors) { colorHex ->
                            val color = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { selectedColor = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddNote(noteText)
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 添加书签对话框
    if (showAddBookmarkDialog) {
        var bookmarkTitle by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            title = { Text("添加书签") },
            text = {
                OutlinedTextField(
                    value = bookmarkTitle,
                    onValueChange = { bookmarkTitle = it },
                    label = { Text("书签标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (bookmarkTitle.isNotBlank()) {
                            onAddBookmark(bookmarkTitle)
                            showAddBookmarkDialog = false
                        }
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun AnnotationItem(
    annotation: Annotation,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val color = try {
        Color(android.graphics.Color.parseColor(annotation.color))
    } catch (e: Exception) {
        Color.Yellow
    }

    val icon = when (annotation.type) {
        AnnotationType.HIGHLIGHT -> Icons.Default.Highlight
        AnnotationType.NOTE -> Icons.Default.Note
        AnnotationType.BOOKMARK -> Icons.Default.Bookmark
        AnnotationType.UNDERLINE -> Icons.Default.FormatUnderlined
        AnnotationType.COMMENT -> Icons.Default.Comment
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = annotation.type.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = annotation.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(annotation.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AnnotationColorPicker(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(annotationColors) { colorHex ->
            val color = Color(android.graphics.Color.parseColor(colorHex))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .border(
                        2.dp,
                        if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onColorSelected(colorHex) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColor == colorHex) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
