package com.example.htmlquickview.service

import android.content.Context
import com.example.htmlquickview.database.AppDatabase
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.AnnotationCollection
import com.example.htmlquickview.model.AnnotationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * 批注服务 - 管理HTML文件的笔记和标注
 */
class AnnotationService(context: Context) {
    private val db = AppDatabase.getDatabase(context)

    /**
     * 获取文件的所有批注
     */
    fun getAnnotations(htmlFileId: Long): Flow<List<Annotation>> {
        return db.annotationDao().getAnnotationsByHtmlFileId(htmlFileId)
    }

    /**
     * 获取文件的所有批注（列表）
     */
    suspend fun getAnnotationsList(htmlFileId: Long): List<Annotation> {
        return db.annotationDao().getAnnotationsByHtmlFileIdList(htmlFileId)
    }

    /**
     * 获取指定类型的批注
     */
    fun getAnnotationsByType(htmlFileId: Long, type: AnnotationType): Flow<List<Annotation>> {
        return db.annotationDao().getAnnotationsByType(type.name, htmlFileId)
    }

    /**
     * 添加批注
     */
    suspend fun addAnnotation(
        htmlFileId: Long,
        type: AnnotationType,
        content: String,
        selectionStart: Int? = null,
        selectionEnd: Int? = null,
        position: String? = null,
        color: String = "#FFFF00"
    ): Long {
        val annotation = Annotation(
            htmlFileId = htmlFileId,
            type = type,
            content = content,
            selectionStart = selectionStart,
            selectionEnd = selectionEnd,
            position = position,
            color = color
        )
        return db.annotationDao().insertAnnotation(annotation)
    }

    /**
     * 添加高亮批注
     */
    suspend fun addHighlight(
        htmlFileId: Long,
        content: String,
        position: String,
        color: String = "#FFFF00"
    ): Long {
        return addAnnotation(htmlFileId, AnnotationType.HIGHLIGHT, content, position = position, color = color)
    }

    /**
     * 添加笔记批注
     */
    suspend fun addNote(
        htmlFileId: Long,
        content: String,
        selectionStart: Int? = null,
        selectionEnd: Int? = null
    ): Long {
        return addAnnotation(
            htmlFileId,
            AnnotationType.NOTE,
            content,
            selectionStart,
            selectionEnd
        )
    }

    /**
     * 添加书签
     */
    suspend fun addBookmark(
        htmlFileId: Long,
        title: String,
        position: String
    ): Long {
        return addAnnotation(htmlFileId, AnnotationType.BOOKMARK, title, position = position)
    }

    /**
     * 更新批注
     */
    suspend fun updateAnnotation(annotation: Annotation) {
        db.annotationDao().updateAnnotation(annotation.copy(updatedAt = Date()))
    }

    /**
     * 删除批注
     */
    suspend fun deleteAnnotation(annotation: Annotation) {
        db.annotationDao().deleteAnnotation(annotation)
    }

    /**
     * 删除批注
     */
    suspend fun deleteAnnotationById(id: Long) {
        db.annotationDao().deleteAnnotationById(id)
    }

    /**
     * 获取批注数量
     */
    suspend fun getAnnotationCount(htmlFileId: Long): Int {
        return db.annotationDao().getAnnotationCountByHtmlFileId(htmlFileId)
    }

    /**
     * 导出批注为HTML
     */
    suspend fun exportAnnotationsToHtml(htmlFileId: Long): String = withContext(Dispatchers.IO) {
        val annotations = getAnnotationsList(htmlFileId)

        buildString {
            appendLine("<!DOCTYPE html>")
            appendLine("<html><head><meta charset='UTF-8'><title>Annotations</title>")
            appendLine("<style>")
            appendLine("body { font-family: Arial, sans-serif; padding: 20px; }")
            appendLine(".annotation { margin: 10px 0; padding: 10px; border-left: 3px solid #ddd; }")
            appendLine(".highlight { background-color: #ffff00; }")
            appendLine(".note { background-color: #e3f2fd; border-color: #2196f3; }")
            appendLine(".bookmark { background-color: #fff3e0; border-color: #ff9800; }")
            appendLine(".meta { color: #666; font-size: 12px; }")
            appendLine("</style></head><body>")
            appendLine("<h1>Annotations</h1>")

            annotations.forEach { annotation ->
                val className = when (annotation.type) {
                    AnnotationType.HIGHLIGHT -> "highlight"
                    AnnotationType.NOTE -> "note"
                    AnnotationType.BOOKMARK -> "bookmark"
                    AnnotationType.UNDERLINE -> "underline"
                    AnnotationType.COMMENT -> "comment"
                }
                val icon = when (annotation.type) {
                    AnnotationType.HIGHLIGHT -> "★"
                    AnnotationType.NOTE -> "📝"
                    AnnotationType.BOOKMARK -> "🔖"
                    AnnotationType.UNDERLINE -> "―"
                    AnnotationType.COMMENT -> "💬"
                }

                appendLine("<div class='annotation $className'>")
                appendLine("<div class='meta'>$icon ${annotation.type.name} - ${annotation.createdAt}</div>")
                appendLine("<div class='content'>${annotation.content}</div>")
                appendLine("</div>")
            }

            appendLine("</body></html>")
        }
    }

    // ===== 批注集合操作 =====

    /**
     * 获取批注集合
     */
    fun getCollections(htmlFileId: Long): Flow<List<AnnotationCollection>> {
        return db.annotationCollectionDao().getCollectionsByHtmlFileId(htmlFileId)
    }

    /**
     * 创建批注集合
     */
    suspend fun createCollection(htmlFileId: Long, name: String, description: String? = null): Long {
        val collection = AnnotationCollection(
            htmlFileId = htmlFileId,
            name = name,
            description = description
        )
        return db.annotationCollectionDao().insertCollection(collection)
    }

    /**
     * 更新批注集合
     */
    suspend fun updateCollection(collection: AnnotationCollection) {
        db.annotationCollectionDao().updateCollection(collection.copy(updatedAt = Date()))
    }

    /**
     * 删除批注集合
     */
    suspend fun deleteCollection(collection: AnnotationCollection) {
        db.annotationCollectionDao().deleteCollection(collection)
    }
}
