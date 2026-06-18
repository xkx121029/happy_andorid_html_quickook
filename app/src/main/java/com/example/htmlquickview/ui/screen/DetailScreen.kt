package com.example.htmlquickview.ui.screen

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.htmlquickview.R
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.service.FileStorageService
import com.example.htmlquickview.ui.component.AnnotationPanel
import com.example.htmlquickview.ui.component.ExportDialog
import com.example.htmlquickview.ui.component.SaveWebPageDialog
import com.example.htmlquickview.viewmodel.HtmlFileViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    htmlFile: HtmlFile,
    fileStorageService: FileStorageService,
    viewModel: HtmlFileViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态管理
    var showAnnotationPanel by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var annotations by remember { mutableStateOf<List<Annotation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 加载批注
    LaunchedEffect(htmlFile.id) {
        scope.launch {
            annotations = viewModel.getAnnotationsList(htmlFile.id)
        }
    }

    // 分享文件
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            try {
                val content = fileStorageService.loadHtmlContent(htmlFile.filePath)
                context.contentResolver.openOutputStream(it)?.use { os ->
                    os.write(content.toByteArray())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = htmlFile.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (htmlFile.sourceUrl != null) {
                                Text(
                                    text = htmlFile.sourceUrl,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.action_back))
                        }
                    },
                    actions = {
                        // 全屏按钮
                        IconButton(
                            onClick = { isFullscreen = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Fullscreen, stringResource(R.string.action_fullscreen))
                        }

                        // 批注按钮
                        BadgedBox(
                            badge = {
                                if (annotations.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Text(annotations.size.toString())
                                    }
                                }
                            }
                        ) {
                            IconButton(
                                onClick = { showAnnotationPanel = true },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(Icons.Default.Edit, stringResource(R.string.action_annotate))
                            }
                        }

                        // 更多菜单
                        IconButton(
                            onClick = { showMoreMenu = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.MoreVert, stringResource(R.string.action_more))
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_save_webpage)) },
                                onClick = {
                                    showMoreMenu = false
                                    showSaveDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.CloudDownload, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_export)) },
                                onClick = {
                                    showMoreMenu = false
                                    showExportDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_share)) },
                                onClick = {
                                    showMoreMenu = false
                                    shareLauncher.launch(htmlFile.fileName)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Share, null)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (!isFullscreen) {
                // 底部批注快捷入口
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (annotations.isNotEmpty()) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ) {
                                            Text(annotations.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notes, stringResource(R.string.action_annotate))
                            }
                        },
                        label = {
                            Text(
                                stringResource(R.string.label_annotations),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = showAnnotationPanel,
                        onClick = { showAnnotationPanel = !showAnnotationPanel },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.FileDownload, stringResource(R.string.action_export)) },
                        label = {
                            Text(
                                stringResource(R.string.label_export),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = showExportDialog,
                        onClick = { showExportDialog = !showExportDialog },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.CloudDownload, stringResource(R.string.action_save_webpage)) },
                        label = {
                            Text(
                                stringResource(R.string.label_save_offline),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = showSaveDialog,
                        onClick = { showSaveDialog = !showSaveDialog },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // WebView预览
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        settings.domStorageEnabled = false
                        settings.allowFileAccess = false
                        settings.allowFileAccessFromFileURLs = false
                        settings.allowUniversalAccessFromFileURLs = false
                        settings.mediaPlaybackRequiresUserGesture = true

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return true
                                if (url.startsWith("file://")) {
                                    val filePathParent = File(htmlFile.filePath).parent
                                    return !url.startsWith("file://$filePathParent")
                                }
                                return true
                            }
                        }

                        try {
                            val htmlContent = fileStorageService.loadHtmlContent(htmlFile.filePath)
                            loadDataWithBaseURL(
                                "about:blank",
                                htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                            isLoading = false
                        } catch (e: Exception) {
                            settings.allowFileAccess = true
                            loadUrl("file:///${htmlFile.filePath}")
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 加载指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
            }

            // 全屏退出按钮
            if (isFullscreen) {
                FloatingActionButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(Icons.Default.FullscreenExit, stringResource(R.string.action_exit_fullscreen))
                }
            }
        }
    }

    // 批注面板
    if (showAnnotationPanel) {
        AnnotationPanel(
            annotations = annotations,
            onDismiss = { showAnnotationPanel = false },
            onAddHighlight = {
                // 添加高亮
                scope.launch {
                    viewModel.addHighlight(
                        htmlFile.id,
                        "选中的文本",
                        "{\"x\":0,\"y\":0}",
                        "#FFFF00"
                    )
                    annotations = viewModel.getAnnotationsList(htmlFile.id)
                }
            },
            onAddNote = { content ->
                scope.launch {
                    viewModel.addNote(htmlFile.id, content)
                    annotations = viewModel.getAnnotationsList(htmlFile.id)
                }
            },
            onAddBookmark = { title ->
                scope.launch {
                    viewModel.addBookmark(htmlFile.id, title, "{\"x\":0,\"y\":0}")
                    annotations = viewModel.getAnnotationsList(htmlFile.id)
                }
            },
            onDeleteAnnotation = { annotation ->
                scope.launch {
                    viewModel.deleteAnnotation(annotation)
                    annotations = viewModel.getAnnotationsList(htmlFile.id)
                }
            },
            onExportAnnotations = {
                scope.launch {
                    val exportedHtml = viewModel.exportAnnotationsToHtml(htmlFile.id)
                    val outputFile = File(context.cacheDir, "annotations_${System.currentTimeMillis()}.html")
                    outputFile.writeText(exportedHtml)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        outputFile
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/html"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "分享批注"))
                }
            }
        )
    }

    // 导出对话框
    if (showExportDialog) {
        ExportDialog(
            htmlFile = htmlFile,
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                scope.launch {
                    val result = viewModel.exportToPdf(htmlFile)
                    result.onSuccess { file ->
                        // 打开PDF
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val openIntent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(openIntent)
                        } catch (e: Exception) {
                            // 没有PDF阅读器
                        }
                    }
                    showExportDialog = false
                }
            },
            onExportEpub = {
                scope.launch {
                    val result = viewModel.exportToEpub(htmlFile)
                    result.onSuccess { file ->
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/epub+zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "分享EPUB"))
                    }
                    showExportDialog = false
                }
            },
            onExportHtml = {
                scope.launch {
                    val result = viewModel.exportToHtml(htmlFile)
                    result.onSuccess { file ->
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/html"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "分享HTML"))
                    }
                    showExportDialog = false
                }
            }
        )
    }

    // 保存网页对话框
    if (showSaveDialog) {
        SaveWebPageDialog(
            htmlFile = htmlFile,
            onDismiss = { showSaveDialog = false },
            onSave = { sourceUrl, fileName ->
                viewModel.saveWebPage(
                    htmlFile = htmlFile,
                    sourceUrl = sourceUrl,
                    onProgress = { progress, status ->
                        // 更新进度
                    },
                    onComplete = { result ->
                        result.onSuccess {
                            // 显示成功提示
                        }
                        showSaveDialog = false
                    }
                )
            }
        )
    }
}
