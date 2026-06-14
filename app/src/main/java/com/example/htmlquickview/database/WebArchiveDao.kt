package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.htmlquickview.model.WebArchive
import com.example.htmlquickview.model.WebResource
import kotlinx.coroutines.flow.Flow

@Dao
interface WebArchiveDao {
    @Query("SELECT * FROM web_archives WHERE htmlFileId = :htmlFileId")
    suspend fun getArchiveByHtmlFileId(htmlFileId: Long): WebArchive?

    @Query("SELECT * FROM web_archives WHERE htmlFileId = :htmlFileId")
    fun getArchiveByHtmlFileIdFlow(htmlFileId: Long): Flow<WebArchive?>

    @Query("SELECT * FROM web_archives ORDER BY savedAt DESC")
    fun getAllArchives(): Flow<List<WebArchive>>

    @Insert
    suspend fun insertArchive(archive: WebArchive): Long

    @Update
    suspend fun updateArchive(archive: WebArchive)

    @Delete
    suspend fun deleteArchive(archive: WebArchive)

    @Query("DELETE FROM web_archives WHERE htmlFileId = :htmlFileId")
    suspend fun deleteArchiveByHtmlFileId(htmlFileId: Long)

    @Query("UPDATE web_archives SET isComplete = :isComplete, resourceCount = :resourceCount, totalSize = :totalSize WHERE id = :archiveId")
    suspend fun updateArchiveStatus(archiveId: Long, isComplete: Boolean, resourceCount: Int, totalSize: Long)
}

@Dao
interface WebResourceDao {
    @Query("SELECT * FROM web_resources WHERE archiveId = :archiveId")
    suspend fun getResourcesByArchiveId(archiveId: Long): List<WebResource>

    @Query("SELECT * FROM web_resources WHERE archiveId = :archiveId")
    fun getResourcesByArchiveIdFlow(archiveId: Long): Flow<List<WebResource>>

    @Query("SELECT * FROM web_resources WHERE originalUrl LIKE :url")
    suspend fun getResourceByOriginalUrl(url: String): WebResource?

    @Insert
    suspend fun insertResource(resource: WebResource): Long

    @Insert
    suspend fun insertResources(resources: List<WebResource>)

    @Delete
    suspend fun deleteResource(resource: WebResource)

    @Query("DELETE FROM web_resources WHERE archiveId = :archiveId")
    suspend fun deleteResourcesByArchiveId(archiveId: Long)

    @Query("SELECT SUM(size) FROM web_resources WHERE archiveId = :archiveId")
    suspend fun getTotalSizeByArchiveId(archiveId: Long): Long?
}
