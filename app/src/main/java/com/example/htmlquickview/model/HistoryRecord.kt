package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "history_records")
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val htmlFileId: Long,
    val fileName: String,
    val filePath: String,
    val accessedAt: Date = Date()
)