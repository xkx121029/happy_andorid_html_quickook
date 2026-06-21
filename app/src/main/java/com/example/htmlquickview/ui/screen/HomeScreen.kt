package com.example.htmlquickview.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.model.Folder
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.Tag
import com.example.htmlquickview.ui.component.*
import com.example.htmlquickview.viewmodel.HtmlFileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun HomeScreen(
    viewModel: HtmlFileViewModel,
    onOpenFile: (HtmlFile) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }
    var isFullTextSearch by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(HtmlFileViewModel.Category.ALL) }
    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFolderManager by remember { mutableStateOf(false) }
    var showTagManager by remember { mutableStateOf(false) }
    var moveFileToFolder by remember { mutableStateOf<HtmlFile?>(null) }
    var fileToDelete by remember { mutableStateOf<HtmlFile?>(null) }
    var fileToRename by remember { mutableStateOf<HtmlFile?>(null) }
    var renameText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val currentSortType by viewModel.sortType.observeAsState(HtmlFileViewModel.SortType.DATE_DESC)

    val files by viewModel.allFiles.observeAsState(emptyList())
    val folders by viewModel.allFolders.observeAsState(emptyList())
    val tags by viewModel.allTags.observeAsState(emptyList())
    val currentFiles by viewModel.currentFiles.observeAsState(emptyList())

    // 全文本搜索结果
    var fullTextResults by remember { mutableStateOf<List<HtmlFile>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 文件标签映射
    var fileTagsMap by remember { mutableStateOf<Map<Long, List<Tag>>>(emptyMap()) }
    
    // 编辑文件标签状态
    var editingFileTags by remember { mutableStateOf<HtmlFile?>(null) }
    var editingFileCurrentTags by remember { mutableStateOf<List<Tag>>(emptyList()) }

    // 加载文件标签
    LaunchedEffect(files) {
        val tagsMap = mutableMapOf<Long, List<Tag>>()
        files.forEach { file ->
            tagsMap[file.id] = viewModel.getTagsForFile(file.id)
        }
        fileTagsMap = tagsMap
    }

    // 加载编辑文件的标签
    LaunchedEffect(editingFileTags) {
        if (editingFileTags != null) {
            editingFileCurrentTags = viewModel.getTagsForFile(editingFileTags!!.id)
        }
    }

    // 全文本搜索
    LaunchedEffect(searchQuery) {
        if (isFullTextSearch && searchQuery.isNotBlank()) {
            isSearching = true
            viewModel.searchFilesFullText(searchQuery).collectLatest { results ->
                fullTextResults = results
                isSearching = false
            }
        } else {
            fullTextResults = emptyList()
        }
    }

    // 显示的文件列表
    val displayedFiles = if (isFullTextSearch && searchQuery.isNotBlank()) {
        fullTextResults
    } else {
        currentFiles
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isFullTextSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.hint_fulltext_search)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        isFullTextSearch = false
                                    }) {
                                        Icon(Icons.Default.Close, stringResource(R.string.action_back))
                                    }
                                }
                            },
                            leadingIcon = {
                                IconButton(onClick = {
                                    isFullTextSearch = false
                                    searchQuery = ""
                                }) {
                                    Icon(Icons.Default.ArrowBack, stringResource(R.string.action_back))
                                }
                            }
                        )
                    } else {
                        Text(stringResource(R.string.app_name))
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    // 全文本搜索按钮
                    IconButton(onClick = { isFullTextSearch = true }) {
                        Icon(Icons.Default.Search, stringResource(R.string.action_fulltext_search))
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.action_more))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_sort)) },
                            onClick = {
                                showMenu = false
                                showSortMenu = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Sort, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_folder_manager)) },
                            onClick = {
                                showMenu = false
                                showFolderManager = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Folder, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_tag_manager)) },
                            onClick = {
                                showMenu = false
                                showTagManager = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LocalOffer, null)
                            }
                        )
                    }

                    // 排序子菜单
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_date_desc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.DATE_DESC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.DATE_DESC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_date_asc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.DATE_ASC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.DATE_ASC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_name_asc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.NAME_ASC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.NAME_ASC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_name_desc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.NAME_DESC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.NAME_DESC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_size_desc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.SIZE_DESC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.SIZE_DESC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_size_asc)) },
                            onClick = {
                                showSortMenu = false
                                viewModel.setSortType(HtmlFileViewModel.SortType.SIZE_ASC)
                            },
                            leadingIcon = {
                                if (currentSortType == HtmlFileViewModel.SortType.SIZE_ASC) {
                                    Icon(Icons.Default.Check, null)
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { showPasteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_paste),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 全文本搜索模式时隐藏原来的搜索栏和分类
            if (!isFullTextSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.setSearchQuery(it)
                    }
                )

                CategoryTabs(
                    selectedCategory = selectedCategory,
                    onCategoryChange = {
                        selectedCategory = it
                        viewModel.setCategory(it)
                    }
                )
            } else if (isSearching) {
                // 搜索中提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.padding(8.dp))
                        Text(stringResource(R.string.msg_searching))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = displayedFiles,
                    key = { it.id }
                ) { file ->
                    HtmlFileItem(
                        htmlFile = file,
                        tags = fileTagsMap[file.id] ?: emptyList(),
                        onOpen = { onOpenFile(file) },
                        onDelete = { fileToDelete = file },
                        onRename = { fileToRename = it; renameText = it.fileName },
                        onToggleFavorite = { viewModel.toggleFavorite(file.id) },
                        onEditTags = { editingFileTags = file },
                        onMoveToFolder = { moveFileToFolder = file }
                    )
                }

                if (displayedFiles.isEmpty()) {
                    item {
                        if (isFullTextSearch && searchQuery.isNotBlank()) {
                            EmptySearchState(
                                query = searchQuery,
                                modifier = Modifier.fillParentMaxSize()
                            )
                        } else {
                            EmptyFilesState(
                                onAddFile = { showPasteDialog = true },
                                modifier = Modifier.fillParentMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPasteDialog) {
        PasteHtmlDialog(
            onDismiss = { showPasteDialog = false },
            onSave = { content, fileName -> viewModel.insertFile(content, fileName) }
        )
    }

    if (showFolderManager) {
        FolderManagerDialog(
            folders = folders,
            onDismiss = { showFolderManager = false },
            onCreateFolder = { name -> viewModel.createFolder(name) },
            onDeleteFolder = { folder -> viewModel.deleteFolder(folder) },
            onRenameFolder = { folder, name ->
                viewModel.updateFolder(folder.copy(name = name))
            }
        )
    }

    if (showTagManager) {
        TagManagerDialog(
            tags = tags,
            onDismiss = { showTagManager = false },
            onCreateTag = { name, color -> viewModel.createTag(name, color) },
            onDeleteTag = { tag -> viewModel.deleteTag(tag) }
        )
    }

    if (editingFileTags != null) {
        FileTagDialog(
            fileTags = editingFileCurrentTags,
            allTags = tags,
            onDismiss = { editingFileTags = null },
            onAddTag = { tagId -> 
                viewModel.addTagToFile(editingFileTags!!.id, tagId) 
            },
            onRemoveTag = { tagId -> 
                viewModel.removeTagFromFile(editingFileTags!!.id, tagId)
            },
            onCreateTag = { name, color ->
                viewModel.createTag(name, color)
            }
        )
    }

    moveFileToFolder?.let { file ->
        FileFolderPickerDialog(
            folders = folders,
            currentFolderId = file.folderId,
            onDismiss = { moveFileToFolder = null },
            onSelectFolder = { folderId ->
                viewModel.moveFileToFolder(file.id, folderId)
                moveFileToFolder = null
            }
        )
    }

    // 删除确认对话框
    fileToDelete?.let { file ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_message, file.fileName),
            onConfirm = {
                viewModel.deleteFile(file)
                fileToDelete = null
            },
            onCancel = { fileToDelete = null }
        )
    }

    // 重命名对话框
    fileToRename?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToRename = null },
            title = { Text(stringResource(R.string.dialog_title_rename)) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text(stringResource(R.string.hint_new_name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameFile(file.id, renameText)
                        }
                        fileToRename = null
                    }
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRename = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun FileFolderPickerDialog(
    folders: List<Folder>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onSelectFolder: (Long?) -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
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
                Text(
                    text = stringResource(R.string.dialog_title_move_to_folder),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.padding(8.dp))

                // 根目录选项
                TextButton(
                    onClick = { onSelectFolder(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Folder, null)
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Text(
                        text = stringResource(R.string.label_root_folder),
                        modifier = Modifier.weight(1f)
                    )
                    if (currentFolderId == null) {
                        Icon(
                            Icons.Default.Check,
                            stringResource(R.string.label_current),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LazyColumn {
                    items(folders) { folder ->
                        TextButton(
                            onClick = { onSelectFolder(folder.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, null)
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(
                                text = folder.name,
                                modifier = Modifier.weight(1f)
                            )
                            if (currentFolderId == folder.id) {
                                Icon(
                                    Icons.Default.Check,
                                    stringResource(R.string.label_current),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            }
        }
    }
}
