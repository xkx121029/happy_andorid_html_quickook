package com.example.htmlquickview.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HistoryRecord
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.ui.component.ConfirmDialog
import com.example.htmlquickview.ui.component.SearchBar
import com.example.htmlquickview.viewmodel.HtmlFileViewModel

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun HistoryScreen(
    viewModel: HtmlFileViewModel,
    onOpenFile: (HtmlFile) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }
    
    val history by viewModel.allHistory.observeAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_history)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(history) { record ->
                    HistoryItem(
                        record = record,
                        onOpen = {
                            coroutineScope.launch {
                                viewModel.getFileById(record.htmlFileId)?.let { file ->
                                    onOpenFile(file)
                                }
                            }
                        },
                        onDelete = { viewModel.deleteHistory(it) }
                    )
                }

                if (history.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.msg_no_history),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    record: HistoryRecord,
    onOpen: () -> Unit,
    onDelete: (HistoryRecord) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onOpen() },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = record.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatDateTime(record.accessedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
            }

            androidx.compose.material3.IconButton(
                onClick = { onDelete(record) }
            ) {
                Icon(Icons.Default.Delete, stringResource(R.string.action_delete))
            }
        }
    }
}

fun formatDateTime(date: java.util.Date): String {
    val now = java.util.Date()
    val diff = now.time - date.time
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "刚刚"
        minutes < 60 -> "${minutes}分钟前"
        hours < 24 -> "${hours}小时前"
        days < 7 -> "${days}天前"
        else -> {
            // 使用线程安全的DateTimeFormatter替代SimpleDateFormat
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .format(date.toInstant().atZone(java.time.ZoneId.systemDefault()))
        }
    }
}