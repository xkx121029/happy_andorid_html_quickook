package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "html_files")
data class HtmlFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val createdAt: Date = Date(),
    val lastModified: Date = Date(),
    val accessCount: Int = 0,
    val isFavorite: Boolean = false,
    val thumbnailPath: String? = null,
    val contentHash: String? = null,
    val folderId: Long? = null,
    val sourceUrl: String? = null,
    val description: String? = null
)
