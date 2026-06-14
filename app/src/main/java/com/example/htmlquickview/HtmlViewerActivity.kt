package com.example.htmlquickview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.htmlquickview.service.FileStorageService
import java.io.File

@androidx.compose.material3.ExperimentalMaterial3Api
class HtmlViewerActivity : ComponentActivity() {
    private lateinit var fileStorageService: FileStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileStorageService = FileStorageService(this)

        val filePath = intent.getStringExtra("filePath")
        val fileId = intent.getLongExtra("fileId", -1)

        if (filePath.isNullOrEmpty()) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HtmlViewerScreen(
                        filePath = filePath,
                        fileStorageService = fileStorageService,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun HtmlViewerScreen(
    filePath: String,
    fileStorageService: FileStorageService,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("HTML预览") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    // 安全设置 - 禁用不必要的功能
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    settings.allowFileAccess = false
                    settings.allowFileAccessFromFileURLs = false
                    settings.allowUniversalAccessFromFileURLs = false
                    settings.mediaPlaybackRequiresUserGesture = true
                    
                    // 设置混合内容模式为仅安全
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    }
                    
                    // 设置WebViewClient限制URL加载
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return true
                            if (url.startsWith("file://")) {
                                val filePathParent = File(filePath).parent
                                return !url.startsWith("file://$filePathParent")
                            }
                            return true
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url?.startsWith("file://") == true) {
                                val filePathParent = File(filePath).parent
                                return !url.startsWith("file://$filePathParent")
                            }
                            return true
                        }
                    }
                    
                    try {
                        val htmlContent = fileStorageService.loadHtmlContent(filePath)
                        loadDataWithBaseURL(
                            "about:blank",
                            htmlContent,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    } catch (e: Exception) {
                        settings.allowFileAccess = true
                        loadUrl("file:///$filePath")
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
