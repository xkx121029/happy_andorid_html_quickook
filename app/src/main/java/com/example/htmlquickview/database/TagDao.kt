package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.htmlquickview.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY createdAt DESC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): Tag?

    @Insert
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("SELECT COUNT(*) FROM tags")
    suspend fun getTagCount(): Int

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN html_file_tags hft ON t.id = hft.tagId
        WHERE hft.htmlFileId = :htmlFileId
    """)
    suspend fun getTagsForHtmlFile(htmlFileId: Long): List<Tag>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN html_file_tags hft ON t.id = hft.tagId
        WHERE hft.htmlFileId = :htmlFileId
    """)
    fun getTagsForHtmlFileFlow(htmlFileId: Long): Flow<List<Tag>>

    @Query("SELECT COUNT(*) FROM html_file_tags WHERE tagId = :tagId")
    suspend fun getFileCountByTag(tagId: Long): Int
}
