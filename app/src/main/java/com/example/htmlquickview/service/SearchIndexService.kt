package com.example.htmlquickview.service

import android.content.Context
import android.os.Build
import android.util.Patterns
import com.example.htmlquickview.database.AppDatabase
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.SearchIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.regex.Pattern

/**
 * 搜索索引服务 - 提供全文搜索功能
 */
class SearchIndexService(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val fileStorageService = FileStorageService(context)

    /**
     * 为HTML文件创建或更新搜索索引
     */
    suspend fun indexHtmlFile(htmlFile: HtmlFile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val content = fileStorageService.loadHtmlContent(htmlFile.filePath)
            val textContent = extractTextContent(content)
            val title = extractTitle(content) ?: htmlFile.fileName

            val searchIndex = SearchIndex(
                htmlFileId = htmlFile.id,
                content = textContent,
                title = title
            )

            db.searchIndexDao().insertSearchIndex(searchIndex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从HTML内容中提取纯文本
     */
    private fun extractTextContent(html: String): String {
        // 移除脚本和样式
        var text = html.replace(Regex("""<script[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("""<style[^>]*>[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")

        // 移除HTML标签
        text = text.replace(Regex("""<[^>]+>"""), " ")

        // 清理多余空白
        text = text.replace(Regex("""\s+"""), " ").trim()

        // 移除特殊字符
        text = text.replace(Regex("""&[a-zA-Z]+;"""), " ")

        return text
    }

    /**
     * 从HTML中提取标题
     */
    private fun extractTitle(html: String): String? {
        val titlePattern = Pattern.compile("""<title[^>]*>([^<]+)</title>""", Pattern.CASE_INSENSITIVE)
        val matcher = titlePattern.matcher(html)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    /**
     * 搜索HTML文件
     */
    fun search(query: String): Flow<List<SearchIndex>> {
        val searchQuery = "%$query%"
        return db.searchIndexDao().searchIndex(searchQuery)
    }

    /**
     * 搜索并返回HtmlFile列表
     */
    fun searchHtmlFiles(query: String): Flow<List<HtmlFile>> {
        val searchQuery = "%$query%"
        return db.searchIndexDao().searchIndexWithFiles(searchQuery).map { indices ->
            indices.mapNotNull { index ->
                db.htmlFileDao().getFileById(index.htmlFileId)
            }
        }
    }

    /**
     * 删除索引
     */
    suspend fun deleteIndex(htmlFileId: Long) {
        db.searchIndexDao().deleteSearchIndexByHtmlFileId(htmlFileId)
    }

    /**
     * 重建所有索引
     */
    suspend fun rebuildAllIndex(htmlFiles: List<HtmlFile>, onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        withContext(Dispatchers.IO) {
            val total = htmlFiles.size
            htmlFiles.forEachIndexed { index, htmlFile ->
                try {
                    indexHtmlFile(htmlFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                onProgress(index + 1, total)
            }
        }
    }

    /**
     * 获取搜索建议
     */
    suspend fun getSuggestions(query: String, limit: Int = 5): List<String> = withContext(Dispatchers.IO) {
        val searchQuery = "%$query%"
        // 这里可以添加更复杂的建议逻辑
        emptyList()
    }

    /**
     * 提取关键词
     */
    fun extractKeywords(text: String, limit: Int = 10): List<String> {
        // 简单的关键词提取（可以改用更复杂的算法如TF-IDF）
        val stopWords = setOf(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人",
            "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去",
            "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "个"
        )

        return text.split(Regex("""[，。！？、；：""''""《》（）\s,.!?;:'"()]+"""))
            .filter { it.length >= 2 && it !in stopWords }
            .groupBy { it }
            .map { (word, occurrences) -> word to occurrences.size }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }

    /**
     * 高亮搜索结果
     */
    fun highlightSearchResults(text: String, query: String): String {
        if (query.isBlank()) return text

        val pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        return matcher.replaceAll { match ->
            "<mark>${match.group()}</mark>"
        }
    }

    /**
     * 获取搜索结果摘要
     */
    fun getSearchSnippet(text: String, query: String, contextLength: Int = 50): String {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        val index = lowerText.indexOf(lowerQuery)

        if (index == -1) {
            return text.take(contextLength * 2) + if (text.length > contextLength * 2) "..." else ""
        }

        val start = (index - contextLength).coerceAtLeast(0)
        val end = (index + query.length + contextLength).coerceAtMost(text.length)

        val snippet = StringBuilder()
        if (start > 0) snippet.append("...")
        snippet.append(text.substring(start, end))
        if (end < text.length) snippet.append("...")

        return snippet.toString()
    }
}
