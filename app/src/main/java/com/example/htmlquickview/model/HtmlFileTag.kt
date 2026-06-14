package com.example.htmlquickview.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "html_file_tags",
    primaryKeys = ["htmlFileId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = HtmlFile::class,
            parentColumns = ["id"],
            childColumns = ["htmlFileId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tagId"]),
        Index(value = ["htmlFileId"])
    ]
)
data class HtmlFileTag(
    val htmlFileId: Long,
    val tagId: Long
)
