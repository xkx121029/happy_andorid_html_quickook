package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface HtmlFileDao {
    @Query("SELECT * FROM html_files ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files WHERE folderId = :folderId OR (folderId IS NULL AND :folderId IS NULL) ORDER BY createdAt DESC")
    fun getFilesByFolder(folderId: Long?): Flow<List<HtmlFile>>

    @Query("""
        SELECT hf.* FROM html_files hf
        INNER JOIN html_file_tags hft ON hf.id = hft.htmlFileId
        WHERE hft.tagId = :tagId
        ORDER BY hf.createdAt DESC
    """)
    fun getFilesByTag(tagId: Long): Flow<List<HtmlFile>>

    @Query("""
        SELECT hf.* FROM html_files hf
        INNER JOIN html_file_tags hft ON hf.id = hft.htmlFileId
        WHERE hft.tagId IN (:tagIds)
        GROUP BY hf.id
        ORDER BY hf.createdAt DESC
    """)
    fun getFilesByAnyTag(tagIds: List<Long>): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files WHERE fileName LIKE :query OR description LIKE :query ORDER BY createdAt DESC")
    fun searchFiles(query: String): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files WHERE id = :id")
    suspend fun getFileById(id: Long): HtmlFile?

    @Query("SELECT * FROM html_files ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFiles(limit: Int): List<HtmlFile>

    @Query("SELECT * FROM html_files ORDER BY accessCount DESC LIMIT :limit")
    suspend fun getFrequentlyUsedFiles(limit: Int): List<HtmlFile>

    @Query("SELECT * FROM html_files ORDER BY fileName ASC")
    fun getAllFilesByNameAsc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files ORDER BY fileName DESC")
    fun getAllFilesByNameDesc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files ORDER BY createdAt ASC")
    fun getAllFilesByDateAsc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files ORDER BY createdAt DESC")
    fun getAllFilesByDateDesc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files ORDER BY fileSize ASC")
    fun getAllFilesBySizeAsc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files ORDER BY fileSize DESC")
    fun getAllFilesBySizeDesc(): Flow<List<HtmlFile>>

    @Query("SELECT * FROM html_files WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteFiles(): Flow<List<HtmlFile>>

    @Insert
    suspend fun insertFile(htmlFile: HtmlFile): Long

    @Update
    suspend fun updateFile(htmlFile: HtmlFile)

    @Delete
    suspend fun deleteFile(htmlFile: HtmlFile)

    @Query("DELETE FROM html_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM html_files")
    suspend fun deleteAllFiles()

    @Query("SELECT COUNT(*) FROM html_files")
    suspend fun getFileCount(): Int

    @Query("UPDATE html_files SET accessCount = accessCount + 1, lastModified = :timestamp WHERE id = :id")
    suspend fun incrementAccessCount(id: Long, timestamp: Long)

    @Query("UPDATE html_files SET folderId = :folderId WHERE id = :fileId")
    suspend fun moveFileToFolder(fileId: Long, folderId: Long?)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN html_file_tags hft ON t.id = hft.tagId
        WHERE hft.htmlFileId = :htmlFileId
    """)
    suspend fun getTagsForFile(htmlFileId: Long): List<Tag>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN html_file_tags hft ON t.id = hft.tagId
        WHERE hft.htmlFileId = :htmlFileId
    """)
    fun getTagsForFileFlow(htmlFileId: Long): Flow<List<Tag>>

    @Query("INSERT OR IGNORE INTO html_file_tags (htmlFileId, tagId) VALUES (:htmlFileId, :tagId)")
    suspend fun addTagToFile(htmlFileId: Long, tagId: Long)

    @Query("DELETE FROM html_file_tags WHERE htmlFileId = :htmlFileId AND tagId = :tagId")
    suspend fun removeTagFromFile(htmlFileId: Long, tagId: Long)

    @Query("DELETE FROM html_file_tags WHERE htmlFileId = :htmlFileId")
    suspend fun clearTagsForFile(htmlFileId: Long)
}
