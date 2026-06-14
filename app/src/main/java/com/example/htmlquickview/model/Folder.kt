package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val parentId: Long? = null,
    val createdAt: Date = Date(),
    val orderIndex: Int = 0
)
