package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.htmlquickview.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_records ORDER BY accessedAt DESC")
    fun getAllHistory(): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history_records WHERE fileName LIKE :query ORDER BY accessedAt DESC")
    fun searchHistory(query: String): Flow<List<HistoryRecord>>

    @Query("SELECT * FROM history_records ORDER BY accessedAt DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<HistoryRecord>

    @Query("SELECT * FROM history_records WHERE htmlFileId = :fileId ORDER BY accessedAt DESC")
    suspend fun getHistoryByFileId(fileId: Long): List<HistoryRecord>

    @Insert
    suspend fun insertHistory(historyRecord: HistoryRecord)

    @Delete
    suspend fun deleteHistory(historyRecord: HistoryRecord)

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history_records WHERE htmlFileId = :fileId")
    suspend fun deleteHistoryByFileId(fileId: Long)

    @Query("DELETE FROM history_records")
    suspend fun deleteAllHistory()

    @Query("SELECT COUNT(*) FROM history_records")
    suspend fun getHistoryCount(): Int

    @Query("DELETE FROM history_records WHERE accessedAt < :threshold")
    suspend fun deleteOldHistory(threshold: Long)
}