package com.example.htmlquickview.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Outlined.FolderOpen,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 动画图标容器
        Surface(
            modifier = Modifier.size(120.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

@Composable
fun EmptyFilesState(
    onAddFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.Description,
        title = stringResource(R.string.msg_no_files),
        subtitle = stringResource(R.string.msg_tap_to_add_file),
        action = {
            Button(onClick = onAddFile) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_add_file))
            }
        },
        modifier = modifier
    )
}

@Composable
fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = stringResource(R.string.msg_no_fulltext_results),
        subtitle = stringResource(R.string.msg_try_other_keywords),
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCategoryChips(
    categories: List<Pair<String, Boolean>>,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.second },
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = {}
    ) {
        categories.forEachIndexed { index, (name, isSelected) ->
            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(index) },
                label = {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = unselectedColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ModernFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    LargeFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(28.dp)
        )
    }
}
