package com.example.htmlquickview.ui.screen

import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.example.htmlquickview.R
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.service.FileStorageService
import com.example.htmlquickview.service.LocalHttpServerService
import com.example.htmlquickview.service.ServerErrorType
import com.example.htmlquickview.ui.component.AnnotationPanel
import com.example.htmlquickview.ui.component.ExportDialog
import com.example.htmlquickview.ui.component.PreviewMode
import com.example.htmlquickview.ui.component.PreviewModeDialog
import com.example.htmlquickview.ui.component.SaveWebPageDialog
import com.example.htmlquickview.viewmodel.HtmlFileViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
private fun SolutionItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

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

    var showAnnotationPanel by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var annotations by remember { mutableStateOf<List<Annotation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var previewMode by remember { mutableStateOf(PreviewMode.LOCAL_PREVIEW) }
    var showPreviewModeDialog by remember { mutableStateOf(false) }
    var httpServerService by remember { mutableStateOf<LocalHttpServerService?>(null) }
    var serverUrl by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf(false) }
    var serverErrorMessage by remember { mutableStateOf("") }
    var serverErrorType by remember { mutableStateOf(ServerErrorType.NONE) }
    var webViewLoadError by remember { mutableStateOf(false) }
    var webViewErrorMessage by remember { mutableStateOf("") }

    LaunchedEffect(htmlFile.id) {
        scope.launch {
            annotations = viewModel.getAnnotationsList(htmlFile.id)
        }
    }

    DisposableEffect(Unit) {
        httpServerService = LocalHttpServerService(context)
        onDispose {
            httpServerService?.stopServer()
        }
    }

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

    fun startLocalServer() {
        serverError = false
        serverErrorMessage = ""
        serverErrorType = ServerErrorType.NONE
        try {
            val htmlContent = fileStorageService.loadHtmlContent(htmlFile.filePath)
            val server = httpServerService ?: return

            var port = 8080
            var success = false
            var lastErrorMsg = ""
            var lastErrorTyp = ServerErrorType.NONE

            // 尝试端口 8080-8099
            while (port < 8100) {
                if (server.startServer(htmlContent, port)) {
                    success = true
                    break
                }
                lastErrorMsg = server.getLastError()
                lastErrorTyp = server.getLastErrorType()
                port++
                // 短暂等待后重试下一个端口
                Thread.sleep(50)
            }

            if (success && server.isRunning()) {
                serverUrl = server.getServerUrl()
            } else {
                serverError = true
                serverErrorType = lastErrorTyp
                if (lastErrorMsg.isNotEmpty()) {
                    serverErrorMessage = lastErrorMsg
                } else {
                    serverErrorMessage = "无法启动HTTP服务器，所有端口（8080-8099）均被占用"
                    serverErrorType = ServerErrorType.PORT_IN_USE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            serverError = true
            serverErrorType = ServerErrorType.FILE_LOAD_ERROR
            serverErrorMessage = "加载文件失败: ${e.message ?: "未知错误"}"
        }
    }

    fun stopLocalServer() {
        httpServerService?.stopServer()
        serverUrl = null
        serverError = false
        serverErrorMessage = ""
        serverErrorType = ServerErrorType.NONE
        webViewLoadError = false
        webViewErrorMessage = ""
    }

    fun refreshServer() {
        webViewLoadError = false
        webViewErrorMessage = ""
        stopLocalServer()
        startLocalServer()
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
                        IconButton(
                            onClick = { showPreviewModeDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Public, stringResource(R.string.action_open))
                        }

                        IconButton(
                            onClick = { isFullscreen = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(Icons.Default.Fullscreen, stringResource(R.string.action_fullscreen))
                        }

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
            when (previewMode) {
                PreviewMode.LOCAL_PREVIEW -> {
                    // 本地预览 - 直接加载本地HTML文件，无需服务器
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
                }

                PreviewMode.CHROME_PREVIEW -> {
                    // Chrome预览 - 启动服务器，使用Chrome Custom Tabs打开
                    LaunchedEffect(Unit) {
                        if (serverUrl == null && !serverError) {
                            startLocalServer()
                        }
                    }

                    if (serverUrl != null) {
                        LaunchedEffect(serverUrl) {
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                            customTabsIntent.launchUrl(context, Uri.parse(serverUrl))
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Chrome浏览器已打开",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "正在使用Chrome浏览器预览HTML内容",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { previewMode = PreviewMode.LOCAL_PREVIEW }
                                    ) {
                                        Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("切换本地预览")
                                    }
                                    Button(
                                        onClick = {
                                            val customTabsIntent = CustomTabsIntent.Builder()
                                                .setShowTitle(true)
                                                .build()
                                            customTabsIntent.launchUrl(context, Uri.parse(serverUrl))
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("重新打开")
                                    }
                                }
                            }
                        }
                    } else if (serverError) {
                        // 服务器错误界面
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = when (serverErrorType) {
                                        ServerErrorType.PORT_IN_USE -> Icons.Default.Router
                                        ServerErrorType.NETWORK_ERROR -> Icons.Default.WifiOff
                                        ServerErrorType.FILE_LOAD_ERROR -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (serverErrorType) {
                                        ServerErrorType.PORT_IN_USE -> "端口被占用"
                                        ServerErrorType.NETWORK_ERROR -> "网络连接失败"
                                        ServerErrorType.FILE_LOAD_ERROR -> "文件加载失败"
                                        else -> "服务器启动失败"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { previewMode = PreviewMode.LOCAL_PREVIEW }
                                    ) {
                                        Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("本地预览")
                                    }
                                    Button(
                                        onClick = { refreshServer() }
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("重试")
                                    }
                                }
                            }
                        }
                    } else {
                        // 服务器启动中
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "正在启动本地服务器...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                PreviewMode.WEBVIEW_PREVIEW -> {
                    // WebView预览 - 启动服务器，使用WebView内嵌访问
                    LaunchedEffect(Unit) {
                        if (serverUrl == null && !serverError) {
                            startLocalServer()
                        }
                    }

                    if (serverUrl != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val currentUrl = serverUrl!!

                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            allowFileAccess = true
                                            allowFileAccessFromFileURLs = true
                                            allowUniversalAccessFromFileURLs = true
                                            mediaPlaybackRequiresUserGesture = false
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            builtInZoomControls = true
                                            displayZoomControls = false
                                            cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                                            databaseEnabled = true
                                            setSupportZoom(true)
                                            setSupportMultipleWindows(false)

                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                            }
                                        }

                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?
                                            ): Boolean {
                                                val url = request?.url?.toString() ?: return true
                                                return !(url.startsWith("http://localhost") ||
                                                        url.startsWith("http://127.0.0.1") ||
                                                        url.startsWith("http://10.0.2.2"))
                                            }

                                            override fun onPageFinished(view: WebView?, url: String?) {
                                                super.onPageFinished(view, url)
                                                webViewLoadError = false
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: android.webkit.WebResourceError?
                                            ) {
                                                if (request?.isForMainFrame == true) {
                                                    webViewLoadError = true
                                                    webViewErrorMessage = "网页加载失败 (错误码: ${error?.errorCode})"
                                                }
                                            }
                                        }

                                        setDownloadListener { url, _, _, _, _ ->
                                            // 阻止下载
                                        }

                                        postDelayed({
                                            loadUrl(currentUrl)
                                        }, 500)
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // 刷新按钮
                            FloatingActionButton(
                                onClick = { refreshServer() },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(Icons.Default.Refresh, "刷新")
                            }

                            // WebView加载错误覆盖层
                            if (webViewLoadError) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Card(
                                        modifier = Modifier.padding(24.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "加载失败",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = webViewErrorMessage,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { previewMode = PreviewMode.LOCAL_PREVIEW }
                                                ) {
                                                    Text("本地预览")
                                                }
                                                Button(
                                                    onClick = { refreshServer() }
                                                ) {
                                                    Icon(Icons.Default.Refresh, null)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("重试")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (serverError) {
                        // 服务器错误界面
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = when (serverErrorType) {
                                        ServerErrorType.PORT_IN_USE -> Icons.Default.Router
                                        ServerErrorType.NETWORK_ERROR -> Icons.Default.WifiOff
                                        ServerErrorType.FILE_LOAD_ERROR -> Icons.Default.ErrorOutline
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = when (serverErrorType) {
                                        ServerErrorType.PORT_IN_USE -> "端口被占用"
                                        ServerErrorType.NETWORK_ERROR -> "网络连接失败"
                                        ServerErrorType.FILE_LOAD_ERROR -> "文件加载失败"
                                        else -> "服务器启动失败"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                if (serverErrorMessage.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = serverErrorMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { previewMode = PreviewMode.LOCAL_PREVIEW }
                                    ) {
                                        Icon(Icons.Default.Description, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("本地预览")
                                    }
                                    Button(
                                        onClick = { refreshServer() }
                                    ) {
                                        Icon(Icons.Default.Refresh, null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("重试")
                                    }
                                }
                            }
                        }
                    } else {
                        // 服务器启动中
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "正在启动本地服务器...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }

    if (showAnnotationPanel) {
        AnnotationPanel(
            annotations = annotations,
            onDismiss = { showAnnotationPanel = false },
            onAddHighlight = {
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

    if (showExportDialog) {
        ExportDialog(
            htmlFile = htmlFile,
            onDismiss = { showExportDialog = false },
            onExportPdf = {
                scope.launch {
                    val result = viewModel.exportToPdf(htmlFile)
                    result.onSuccess { file ->
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

    if (showSaveDialog) {
        SaveWebPageDialog(
            htmlFile = htmlFile,
            onDismiss = { showSaveDialog = false },
            onSave = { sourceUrl, fileName ->
                viewModel.saveWebPage(
                    htmlFile = htmlFile,
                    sourceUrl = sourceUrl,
                    onProgress = { progress, status ->
                    },
                    onComplete = { result ->
                        result.onSuccess {
                        }
                        showSaveDialog = false
                    }
                )
            }
        )
    }

    if (showPreviewModeDialog) {
        PreviewModeDialog(
            currentMode = previewMode,
            onDismiss = { showPreviewModeDialog = false },
            onSelectMode = { mode ->
                if (mode != previewMode) {
                    // 切换模式时停止服务器
                    stopLocalServer()
                    previewMode = mode
                }
            }
        )
    }
}
