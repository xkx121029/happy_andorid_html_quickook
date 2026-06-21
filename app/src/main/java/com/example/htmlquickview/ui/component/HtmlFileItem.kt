package com.example.htmlquickview.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HtmlFileItem(
    htmlFile: HtmlFile,
    tags: List<Tag> = emptyList(),
    onOpen: (HtmlFile) -> Unit,
    onDelete: (HtmlFile) -> Unit,
    onRename: ((HtmlFile) -> Unit)? = null,
    onToggleFavorite: ((HtmlFile) -> Unit)? = null,
    onEditTags: ((HtmlFile) -> Unit)? = null,
    onMoveToFolder: ((HtmlFile) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onOpen(htmlFile) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 主要内容
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文件图标
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 文件信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = htmlFile.fileName.ifEmpty { "未命名" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (htmlFile.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = stringResource(R.string.action_remove_favorite),
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFF4081)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = FileUtils.formatFileSize(htmlFile.fileSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateFormat.format(htmlFile.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 更多按钮
                Box {
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            stringResource(R.string.action_more),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        if (onToggleFavorite != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (htmlFile.isFavorite) stringResource(R.string.action_remove_favorite)
                                        else stringResource(R.string.action_add_favorite)
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onToggleFavorite(htmlFile)
                                },
                                leadingIcon = {
                                    Icon(
                                        if (htmlFile.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        null,
                                        tint = Color(0xFFFF4081)
                                    )
                                }
                            )
                        }
                        if (onRename != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_rename)) },
                                onClick = {
                                    expanded = false
                                    onRename(htmlFile)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, null)
                                }
                            )
                        }
                        if (onEditTags != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_edit_tags)) },
                                onClick = {
                                    expanded = false
                                    onEditTags(htmlFile)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Label, null)
                                }
                            )
                        }
                        if (onMoveToFolder != null) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_move_to_folder)) },
                                onClick = {
                                    expanded = false
                                    onMoveToFolder(htmlFile)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Folder, null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                onDelete(htmlFile)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            // 标签区域
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (tags.size > 3) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "+${tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colorInt = try {
        android.graphics.Color.parseColor(tag.color)
    } catch (e: Exception) {
        android.graphics.Color.parseColor("#FF6200EE")
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(colorInt).copy(alpha = 0.15f),
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = Color(colorInt),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun SwipeableFileItem(
    htmlFile: HtmlFile,
    tags: List<Tag>,
    onOpen: (HtmlFile) -> Unit,
    onDelete: (HtmlFile) -> Unit,
    onRename: ((HtmlFile) -> Unit)? = null,
    onToggleFavorite: ((HtmlFile) -> Unit)? = null,
    onEditTags: ((HtmlFile) -> Unit)? = null,
    onMoveToFolder: ((HtmlFile) -> Unit)? = null
) {
    SwipeableItem(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        swipeThreshold = 120f,
        leftActions = {
            // 左滑显示：收藏和编辑
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = { onToggleFavorite(htmlFile) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (htmlFile.isFavorite) Color(0xFFFF4081).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                    ) {
                        Icon(
                            imageVector = if (htmlFile.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (htmlFile.isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (onEditTags != null) {
                    IconButton(
                        onClick = { onEditTags(htmlFile) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = stringResource(R.string.action_edit_tags),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        },
        rightActions = {
            // 右滑显示：删除
            IconButton(
                onClick = { onDelete(htmlFile) },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        HtmlFileItem(
            htmlFile = htmlFile,
            tags = tags,
            onOpen = onOpen,
            onDelete = onDelete,
            onRename = onRename,
            onToggleFavorite = onToggleFavorite,
            onEditTags = onEditTags,
            onMoveToFolder = onMoveToFolder
        )
    }
}
