package com.example.htmlquickview.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HtmlFile

@Composable
fun SearchResultItem(
    htmlFile: HtmlFile,
    snippet: String? = null,
    onOpen: (HtmlFile) -> Unit,
    onDelete: (HtmlFile) -> Unit,
    onToggleFavorite: ((HtmlFile) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onOpen(htmlFile) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Description,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = htmlFile.fileName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (htmlFile.sourceUrl != null) {
                            Text(
                                text = htmlFile.sourceUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (onToggleFavorite != null) {
                    IconButton(onClick = { onToggleFavorite(htmlFile) }) {
                        Icon(
                            if (htmlFile.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "收藏",
                            tint = if (htmlFile.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            if (snippet != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullTextSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean = false,
    placeholder: String = "搜索文件内容和标题..."
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, "搜索")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "清除")
                }
            }
        },
        singleLine = true,
        isError = isSearching
    )
}

@Composable
fun SearchFilters(
    selectedFilter: SearchFilter,
    onFilterChange: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == SearchFilter.ALL,
            onClick = { onFilterChange(SearchFilter.ALL) },
            label = { Text("全部") }
        )
        FilterChip(
            selected = selectedFilter == SearchFilter.TITLE,
            onClick = { onFilterChange(SearchFilter.TITLE) },
            label = { Text("标题") }
        )
        FilterChip(
            selected = selectedFilter == SearchFilter.CONTENT,
            onClick = { onFilterChange(SearchFilter.CONTENT) },
            label = { Text("内容") }
        )
    }
}

enum class SearchFilter {
    ALL, TITLE, CONTENT
}
