package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 批注模型 - 在HTML文件上添加的笔记和标注
 */
@Entity(
    tableName = "annotations",
    foreignKeys = [
        ForeignKey(
            entity = HtmlFile::class,
            parentColumns = ["id"],
            childColumns = ["htmlFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["htmlFileId"])]
)
data class Annotation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val htmlFileId: Long,  // 关联的HTML文件ID
    val type: AnnotationType,  // 批注类型
    val content: String,  // 批注内容
    val selectionStart: Int? = null,  // 文本选择开始位置（如果是文本批注）
    val selectionEnd: Int? = null,  // 文本选择结束位置
    val position: String? = null,  // 位置信息（JSON格式，用于高亮等）
    val color: String = "#FFFF00",  // 批注颜色（默认黄色）
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class AnnotationType {
    HIGHLIGHT,  // 高亮
    NOTE,  // 笔记
    BOOKMARK,  // 书签
    UNDERLINE,  // 下划线
    COMMENT  // 评论
}

/**
 * 批注与标签的关联（复用HtmlFileTag）
 */

/**
 * 批注集合 - 将多个批注组织在一起
 */
@Entity(
    tableName = "annotation_collections",
    foreignKeys = [
        ForeignKey(
            entity = HtmlFile::class,
            parentColumns = ["id"],
            childColumns = ["htmlFileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["htmlFileId"])]
)
data class AnnotationCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val htmlFileId: Long,  // 关联的HTML文件ID
    val name: String,  // 集合名称
    val description: String? = null,  // 描述
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * 搜索索引模型 - 用于全文搜索
 */
@Entity(tableName = "search_index")
data class SearchIndex(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val htmlFileId: Long,  // 关联的HTML文件ID
    val content: String,  // 索引内容（提取的文本）
    val title: String,  // 标题
    val indexedAt: Date = Date()
)
