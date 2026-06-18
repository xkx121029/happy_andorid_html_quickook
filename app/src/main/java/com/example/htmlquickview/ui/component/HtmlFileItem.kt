package com.example.htmlquickview.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.Tag
import com.example.htmlquickview.util.FileUtils
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HtmlFileItem(
    htmlFile: HtmlFile,
    tags: List<Tag> = emptyList(),
    onOpen: (HtmlFile) -> Unit,
    onDelete: (HtmlFile) -> Unit,
    onRename: ((HtmlFile) -> Unit)? = null,
    onToggleFavorite: ((HtmlFile) -> Unit)? = null,
    onEditTags: ((HtmlFile) -> Unit)? = null,
    onMoveToFolder: ((HtmlFile) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    SwipeableItem(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        swipeThreshold = 120f,
        leftActions = {
            // 右滑时显示：收藏和重命名
            if (onToggleFavorite != null) {
                IconButton(onClick = { onToggleFavorite(htmlFile) }) {
                    Icon(
                        imageVector = if (htmlFile.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (htmlFile.isFavorite) stringResource(R.string.action_remove_favorite) else stringResource(R.string.action_add_favorite),
                        tint = Color(0xFFFF4081)
                    )
                }
            }
            if (onRename != null) {
                IconButton(onClick = { onRename(htmlFile) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_rename),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        rightActions = {
            // 左滑时显示：删除
            IconButton(onClick = { onDelete(htmlFile) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen(htmlFile) },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = htmlFile.fileName.ifEmpty { "未命名" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = FileUtils.formatFileSize(htmlFile.fileSize),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                            Text(
                                text = dateFormat.format(htmlFile.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                    }
                }

                // 只保留更多按钮
                Box {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.action_more))
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (onEditTags != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_edit_tags)) },
                                onClick = {
                                    expanded = false
                                    onEditTags(htmlFile)
                                }
                            )
                        }
                        if (onMoveToFolder != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_to_folder)) },
                                onClick = {
                                    expanded = false
                                    onMoveToFolder(htmlFile)
                                }
                            )
                        }
                    }
                }
            }

            if (tags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        TagChip(tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: Tag) {
    val colorInt = try {
        android.graphics.Color.parseColor(tag.color)
    } catch (e: Exception) {
        android.graphics.Color.parseColor("#FF6200EE")
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color(colorInt).copy(alpha = 0.1f),
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = Color(colorInt),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
