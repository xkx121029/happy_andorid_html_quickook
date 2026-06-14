package com.example.htmlquickview.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.webkit.WebView
import com.example.htmlquickview.model.HtmlFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * 导出服务 - 导出HTML为PDF/EPUB等格式
 */
class ExportService(private val context: Context) {
    private val fileStorageService = FileStorageService(context)

    /**
     * 导出为PDF
     */
    suspend fun exportToPdf(
        htmlFile: HtmlFile,
        outputFile: File? = null,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val htmlContent = fileStorageService.loadHtmlContent(htmlFile.filePath)
            val output = outputFile ?: File(context.getExternalFilesDir("exports"), "${htmlFile.fileName}.pdf")

            output.parentFile?.mkdirs()

            // 创建PDF文档
            val document = PdfDocument()

            // 设置页面大小为A4
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)

            val canvas = page.canvas
            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }

            // 简单渲染HTML文本（实际项目中可以使用WebView截图方式）
            val textContent = extractTextFromHtml(htmlContent)
            val lines = wrapText(textContent, canvas.width - 40f, paint)

            var y = 40f
            for (line in lines) {
                if (y > canvas.height - 40) break
                canvas.drawText(line, 20f, y, paint)
                y += paint.textSize * 1.5f
            }

            document.finishPage(page)

            // 写入文件
            FileOutputStream(output).use { fos ->
                document.writeTo(fos)
            }
            document.close()

            onProgress(100)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 使用WebView渲染并导出PDF（更高质量）
     */
    fun exportToPdfWithWebView(
        webView: WebView,
        htmlFile: HtmlFile,
        outputFile: File? = null,
        onComplete: (Result<File>) -> Unit
    ) {
        val output = outputFile ?: File(context.getExternalFilesDir("exports"), "${htmlFile.fileName}.pdf")
        output.parentFile?.mkdirs()

        val printAdapter = webView.createPrintDocumentAdapter(htmlFile.fileName)
        val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
        
        printManager.print(
            htmlFile.fileName,
            printAdapter,
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        )
    }

    /**
     * 导出为EPUB（电子书格式）
     */
    suspend fun exportToEpub(
        htmlFile: HtmlFile,
        outputFile: File? = null,
        onProgress: (Int) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val htmlContent = fileStorageService.loadHtmlContent(htmlFile.filePath)
            val output = outputFile ?: File(context.getExternalFilesDir("exports"), "${htmlFile.fileName}.epub")

            output.parentFile?.mkdirs()

            // 创建EPUB文件
            val epubContent = buildEpubContent(htmlContent, htmlFile.fileName)

            FileOutputStream(output).use { fos ->
                fos.write(epubContent.toByteArray(Charsets.UTF_8))
            }

            onProgress(100)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 构建EPUB内容
     */
    private fun buildEpubContent(html: String, title: String): String {
        val textContent = extractTextFromHtml(html)
        val content = escapeXml(textContent)

        return buildString {
            // MIMETYPE
            appendLine("application/epub+zip")

            // 简化处理，实际EPUB需要更复杂的结构
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\">")
            appendLine("  <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">")
            appendLine("    <dc:title>${escapeXml(title)}</dc:title>")
            appendLine("    <dc:language>zh-CN</dc:language>")
            appendLine("    <dc:identifier>urn:uuid:${java.util.UUID.randomUUID()}</dc:identifier>")
            appendLine("  </metadata>")
            appendLine("  <manifest>")
            appendLine("    <item id=\"chapter1\" href=\"chapter1.xhtml\" media-type=\"application/xhtml+xml\"/>")
            appendLine("  </manifest>")
            appendLine("  <spine>")
            appendLine("    <itemref idref=\"chapter1\"/>")
            appendLine("  </spine>")
            appendLine("</package>")
            appendLine()
            appendLine("<html xmlns=\"http://www.w3.org/1999/xhtml\">")
            appendLine("<head><title>${escapeXml(title)}</title></head>")
            appendLine("<body>")
            appendLine("<h1>${escapeXml(title)}</h1>")
            textContent.split("\n\n").forEach { paragraph ->
                if (paragraph.isNotBlank()) {
                    appendLine("<p>${escapeXml(paragraph.trim())}</p>")
                }
            }
            appendLine("</body>")
            appendLine("</html>")
        }
    }

    /**
     * 从HTML提取纯文本
     */
    private fun extractTextFromHtml(html: String): String {
        var text = html

        // 移除脚本和样式
        text = text.replace(Regex("""<script[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("""<style[^>]*>[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")

        // 移除HTML标签
        text = text.replace(Regex("""<[^>]+>"""), "\n")

        // 清理实体
        text = text.replace("&nbsp;", " ")
        text = text.replace("&lt;", "<")
        text = text.replace("&gt;", ">")
        text = text.replace("&amp;", "&")
        text = text.replace("&quot;", "\"")
        text = text.replace(Regex("""&[a-zA-Z]+;"""), " ")

        // 清理空白
        text = text.replace(Regex("""\n+"""), "\n")
        text = text.replace(Regex("""[ \t]+"""), " ")
        text = text.trim()

        return text
    }

    /**
     * 文本换行
     */
    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine.toString())
                    }
                    currentLine = StringBuilder(word)
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
            lines.add("") // 空行表示段落结束
        }

        return lines
    }

    /**
     * XML转义
     */
    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * 获取导出目录
     */
    fun getExportDir(): File {
        val dir = File(context.getExternalFilesDir("exports"), "pdf")
        dir.mkdirs()
        return dir
    }

    /**
     * 获取所有导出的文件
     */
    fun getExportedFiles(): List<File> {
        val dir = getExportDir()
        return dir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * 删除导出文件
     */
    suspend fun deleteExportedFile(file: File): Boolean {
        return withContext(Dispatchers.IO) {
            file.delete()
        }
    }
}
