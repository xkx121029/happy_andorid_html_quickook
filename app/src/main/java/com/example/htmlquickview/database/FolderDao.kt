package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.htmlquickview.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY orderIndex ASC, createdAt DESC")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId OR (parentId IS NULL AND :parentId IS NULL) ORDER BY orderIndex ASC")
    fun getFoldersByParentId(parentId: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): Folder?

    @Insert
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun getFolderCount(): Int
}
