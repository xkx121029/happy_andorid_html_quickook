package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: String = "#FF6200EE",
    val createdAt: Date = Date()
)
