package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 网页存档模型 - 保存完整网页的元数据
 */
@Entity(tableName = "web_archives")
data class WebArchive(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val htmlFileId: Long,  // 关联的HTML文件ID
    val sourceUrl: String,  // 原始URL
    val title: String?,  // 网页标题
    val savedAt: Date = Date(),  // 保存时间
    val resourceCount: Int = 0,  // 资源数量
    val totalSize: Long = 0,  // 总大小
    val isComplete: Boolean = false  // 是否完整保存
)

/**
 * 网页资源模型 - 保存下载的CSS、图片等资源
 */
@Entity(
    tableName = "web_resources",
    foreignKeys = [
        ForeignKey(
            entity = WebArchive::class,
            parentColumns = ["id"],
            childColumns = ["archiveId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["archiveId"])]
)
data class WebResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val archiveId: Long,  // 所属存档ID
    val originalUrl: String,  // 原始URL
    val localPath: String,  // 本地保存路径
    val resourceType: ResourceType,  // 资源类型
    val mimeType: String?,  // MIME类型
    val size: Long,  // 文件大小
    val savedAt: Date = Date()
)

enum class ResourceType {
    HTML, CSS, IMAGE, JAVASCRIPT, FONT, OTHER
}
