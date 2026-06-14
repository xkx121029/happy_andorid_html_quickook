package com.example.htmlquickview.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.htmlquickview.model.HtmlFile
import java.io.BufferedReader
import java.io.InputStreamReader

class ShareReceiverService(private val context: Context) {
    private val fileStorageService = FileStorageService(context)
    private val TAG = ShareReceiverService::class.java.simpleName

    fun processShareIntent(intent: Intent): ShareResult {
        return try {
            when (intent.action) {
                Intent.ACTION_SEND -> processSendIntent(intent)
                Intent.ACTION_SEND_MULTIPLE -> processSendMultipleIntent(intent)
                else -> ShareResult.Error("不支持的分享类型")
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理分享失败", e)
            ShareResult.Error("处理分享失败: ${e.message}")
        }
    }

    private fun processSendIntent(intent: Intent): ShareResult {
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        return when {
            text != null -> processTextContent(text, subject)
            uri != null -> processUriContent(uri)
            else -> ShareResult.Error("未找到可处理的分享内容")
        }
    }

    private fun processSendMultipleIntent(intent: Intent): ShareResult {
        val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
        if (uris.isNullOrEmpty()) {
            return ShareResult.Error("未找到可处理的分享内容")
        }

        val results = mutableListOf<ShareResult.Success>()
        uris.forEach { uri ->
            val result = processUriContent(uri)
            if (result is ShareResult.Success) {
                results.add(result)
            }
        }

        return if (results.isNotEmpty()) {
            ShareResult.Success(results.first().htmlFile, "${results.size} 个文件已保存")
        } else {
            ShareResult.Error("未找到有效的HTML文件")
        }
    }

    private fun processTextContent(text: String, subject: String?): ShareResult {
        if (!isValidHtml(text)) {
            return ShareResult.Error("无效的HTML内容")
        }

        val fileName = subject?.let { sanitizeFileName(it) } ?: fileStorageService.generateFileName()
        val htmlFile = fileStorageService.createHtmlFile(text, fileName)
        return ShareResult.Success(htmlFile, "HTML内容已保存")
    }

    private fun processUriContent(uri: Uri): ShareResult {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    if (!isValidHtml(content)) {
                        return ShareResult.Error("无效的HTML内容")
                    }

                    val fileName = getFileNameFromUri(uri) ?: fileStorageService.generateFileName()
                    val htmlFile = fileStorageService.createHtmlFile(content, fileName)
                    ShareResult.Success(htmlFile, "HTML文件已保存")
                }
            } ?: ShareResult.Error("无法读取文件内容")
        } catch (e: Exception) {
            ShareResult.Error("读取文件失败: ${e.message}")
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("_display_name")
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    sanitizeFileName(name)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeFileName(name: String): String {
        // 保留中文、字母、数字、点、下划线和连字符，替换其他字符为下划线
        val sanitized = name.replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9._-]"), "_")
        return if (sanitized.endsWith(".html", ignoreCase = true)) {
            sanitized
        } else {
            "${sanitized}.html"
        }
    }

    fun isValidHtml(content: String): Boolean {
        val trimmed = content.trim()
        return trimmed.startsWith("<") && (
            trimmed.contains("<html", ignoreCase = true) ||
            trimmed.contains("<body", ignoreCase = true) ||
            trimmed.contains("<div", ignoreCase = true) ||
            trimmed.contains("<p", ignoreCase = true)
        )
    }

    fun extractTitleFromHtml(content: String): String? {
        val titlePattern = Regex("<title[^>]*>([^<]*)</title>", RegexOption.IGNORE_CASE)
        val matchResult = titlePattern.find(content)
        return matchResult?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    sealed class ShareResult {
        data class Success(val htmlFile: HtmlFile, val message: String) : ShareResult()
        data class Error(val message: String) : ShareResult()
    }
}