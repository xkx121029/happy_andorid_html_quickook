package com.example.htmlquickview.service

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.webkit.WebView
import com.example.htmlquickview.database.AppDatabase
import com.example.htmlquickview.model.ResourceType
import com.example.htmlquickview.model.WebArchive
import com.example.htmlquickview.model.WebResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Date
import java.util.concurrent.Executors
import java.util.regex.Pattern

/**
 * 网页存档服务 - 下载并保存完整网页
 */
class WebArchiveService(private val context: Context) {
    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val db = AppDatabase.getDatabase(context)

    private val archiveDir: File by lazy {
        File(context.filesDir, "web_archives").apply { mkdirs() }
    }

    private val resourceDir: File by lazy {
        File(context.filesDir, "web_resources").apply { mkdirs() }
    }

    /**
     * 保存网页为离线存档
     */
    suspend fun saveWebPage(
        htmlFileId: Long,
        sourceUrl: String,
        htmlContent: String,
        title: String?,
        onProgress: (Int, Int, String) -> Unit = { _, _, _ -> }
    ): Result<WebArchive> = withContext(Dispatchers.IO) {
        try {
            // 创建存档记录
            val archive = WebArchive(
                htmlFileId = htmlFileId,
                sourceUrl = sourceUrl,
                title = title,
                isComplete = false
            )
            val archiveId = db.webArchiveDao().insertArchive(archive)

            val archiveDir = File(this@WebArchiveService.archiveDir, archiveId.toString())
            archiveDir.mkdirs()

            val resources = mutableListOf<WebResource>()
            var processedCount = 0

            // 提取资源URL
            val resourceUrls = extractResourceUrls(htmlContent, sourceUrl)
            val totalCount = resourceUrls.size

            // 下载HTML
            val localHtmlPath = File(archiveDir, "index.html").absolutePath
            FileOutputStream(localHtmlPath).use { it.write(htmlContent.toByteArray()) }
            processedCount++
            onProgress(processedCount, totalCount, "保存HTML...")

            // 下载资源文件
            for (resource in resourceUrls) {
                try {
                    val webResource = downloadResource(
                        archiveId = archiveId,
                        url = resource.url,
                        baseDir = archiveDir,
                        type = resource.type,
                        htmlContent = if (resource.type == ResourceType.CSS) htmlContent else null
                    )
                    webResource?.let { resources.add(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                processedCount++
                onProgress(processedCount, totalCount, "下载资源: ${resource.url}")
            }

            // 更新存档状态
            val totalSize = resources.sumOf { it.size } + File(localHtmlPath).length()
            db.webArchiveDao().updateArchiveStatus(
                archiveId = archiveId,
                isComplete = true,
                resourceCount = resources.size + 1,
                totalSize = totalSize
            )

            Result.success(archive.copy(id = archiveId, resourceCount = resources.size + 1, totalSize = totalSize, isComplete = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 提取HTML中的资源URL
     */
    private fun extractResourceUrls(htmlContent: String, baseUrl: String): List<ResourceInfo> {
        val resources = mutableSetOf<ResourceInfo>()
        val baseUri = URL(baseUrl)
        val baseHost = baseUri.host

        // CSS文件
        val cssPattern = Pattern.compile("""href=["']([^"']+\.css[^"']*)["']""", Pattern.CASE_INSENSITIVE)
        val cssMatcher = cssPattern.matcher(htmlContent)
        while (cssMatcher.find()) {
            val url = resolveUrl(cssMatcher.group(1)!!, baseUrl)
            if (url != null) resources.add(ResourceInfo(url, ResourceType.CSS))
        }

        // JavaScript文件
        val jsPattern = Pattern.compile("""src=["']([^"']+\.js[^"']*)["']""", Pattern.CASE_INSENSITIVE)
        val jsMatcher = jsPattern.matcher(htmlContent)
        while (jsMatcher.find()) {
            val url = resolveUrl(jsMatcher.group(1)!!, baseUrl)
            if (url != null) resources.add(ResourceInfo(url, ResourceType.JAVASCRIPT))
        }

        // 图片文件
        val imgPattern = Pattern.compile("""src=["']([^"']+\.(jpg|jpeg|png|gif|svg|webp|bmp)[^"']*)["']""", Pattern.CASE_INSENSITIVE)
        val imgMatcher = imgPattern.matcher(htmlContent)
        while (imgMatcher.find()) {
            val url = resolveUrl(imgMatcher.group(1)!!, baseUrl)
            if (url != null) resources.add(ResourceInfo(url, ResourceType.IMAGE))
        }

        // 背景图片
        val bgPattern = Pattern.compile("""url\(["']?([^"')]+)["']?\)""", Pattern.CASE_INSENSITIVE)
        val bgMatcher = bgPattern.matcher(htmlContent)
        while (bgMatcher.find()) {
            val url = resolveUrl(bgMatcher.group(1)!!, baseUrl)
            if (url != null && !url.contains(".css")) {
                resources.add(ResourceInfo(url, ResourceType.IMAGE))
            }
        }

        // 字体文件
        val fontPattern = Pattern.compile("""url\(["']?([^"')]+\.(woff2?|ttf|otf|eot)[^"']*)["']?\)""", Pattern.CASE_INSENSITIVE)
        val fontMatcher = fontPattern.matcher(htmlContent)
        while (fontMatcher.find()) {
            val url = resolveUrl(fontMatcher.group(1)!!, baseUrl)
            if (url != null) resources.add(ResourceInfo(url, ResourceType.FONT))
        }

        return resources.toList()
    }

    /**
     * 解析相对URL为绝对URL
     */
    private fun resolveUrl(relativeUrl: String, baseUrl: String): String? {
        return try {
            if (relativeUrl.startsWith("data:")) return null // 跳过data URI
            if (relativeUrl.startsWith("//")) return "https:$relativeUrl"
            if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
                relativeUrl
            } else {
                URL(URL(baseUrl), relativeUrl).toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 下载单个资源
     */
    private suspend fun downloadResource(
        archiveId: Long,
        url: String,
        baseDir: File,
        type: ResourceType,
        htmlContent: String? = null
    ): WebResource? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream: InputStream = connection.inputStream
                val bytes = inputStream.readBytes()
                inputStream.close()

                // 生成文件名
                val fileName = generateFileName(url, type)
                val localFile = File(baseDir, fileName)
                FileOutputStream(localFile).use { it.write(bytes) }

                WebResource(
                    archiveId = archiveId,
                    originalUrl = url,
                    localPath = localFile.absolutePath,
                    resourceType = type,
                    mimeType = connection.contentType,
                    size = bytes.size.toLong()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 生成资源文件名
     */
    private fun generateFileName(url: String, type: ResourceType): String {
        val path = try {
            URL(url).path ?: ""
        } catch (e: Exception) {
            ""
        }

        val extension = when (type) {
            ResourceType.HTML -> ".html"
            ResourceType.CSS -> ".css"
            ResourceType.JAVASCRIPT -> ".js"
            ResourceType.IMAGE -> {
                val ext = path.substringAfterLast(".", "").lowercase()
                if (ext in listOf("jpg", "jpeg", "png", "gif", "webp", "svg", "bmp")) ".$ext" else ".img"
            }
            ResourceType.FONT -> {
                val ext = path.substringAfterLast(".", "").lowercase()
                if (ext in listOf("woff2", "woff", "ttf", "otf", "eot")) ".$ext" else ".font"
            }
            ResourceType.OTHER -> ".bin"
        }

        // 使用URL的hash作为文件名，避免特殊字符问题
        val hash = url.hashCode().toString(16)
        return "$hash$extension"
    }

    /**
     * 获取网页存档
     */
    suspend fun getArchive(htmlFileId: Long): WebArchive? {
        return db.webArchiveDao().getArchiveByHtmlFileId(htmlFileId)
    }

    /**
     * 获取存档的资源列表
     */
    suspend fun getArchiveResources(archiveId: Long): List<WebResource> {
        return db.webResourceDao().getResourcesByArchiveId(archiveId)
    }

    /**
     * 删除网页存档
     */
    suspend fun deleteArchive(archiveId: Long) {
        val archive = db.webArchiveDao().getArchiveByHtmlFileId(archiveId) ?: return
        val archiveDir = File(this.archiveDir, archiveId.toString())
        archiveDir.deleteRecursively()
        db.webArchiveDao().deleteArchive(archive)
    }

    /**
     * 从WebView保存页面
     */
    fun savePageFromWebView(
        webView: WebView,
        htmlFileId: Long,
        sourceUrl: String,
        onComplete: (Result<WebArchive>) -> Unit
    ) {
        webView.post {
            val htmlContent = webView.createHtmlString()
            val title = webView.title

            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                val result = saveWebPage(htmlFileId, sourceUrl, htmlContent, title)
                mainHandler.post { onComplete(result) }
            }
        }
    }

    private fun WebView.createHtmlString(): String {
        var result = ""
        evaluateJavascript("document.documentElement.outerHTML") { value ->
            result = value as String
        }
        return result.removeSurrounding("\"")
    }

    data class ResourceInfo(
        val url: String,
        val type: ResourceType
    )
}
