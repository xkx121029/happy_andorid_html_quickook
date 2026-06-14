package com.example.htmlquickview.repository

import android.content.Context
import com.example.htmlquickview.database.AppDatabase
import com.example.htmlquickview.database.FolderDao
import com.example.htmlquickview.database.HistoryDao
import com.example.htmlquickview.database.HtmlFileDao
import com.example.htmlquickview.database.TagDao
import com.example.htmlquickview.model.Folder
import com.example.htmlquickview.model.HistoryRecord
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.HtmlFileTag
import com.example.htmlquickview.model.Tag
import kotlinx.coroutines.flow.Flow
import java.util.Date

class HtmlFileRepository(context: Context) {
    private val htmlFileDao: HtmlFileDao
    private val historyDao: HistoryDao
    private val folderDao: FolderDao
    private val tagDao: TagDao

    init {
        val db = AppDatabase.getDatabase(context)
        htmlFileDao = db.htmlFileDao()
        historyDao = db.historyDao()
        folderDao = db.folderDao()
        tagDao = db.tagDao()
    }

    // ===== HtmlFile Operations =====
    fun getAllFiles(): Flow<List<HtmlFile>> = htmlFileDao.getAllFiles()

    fun getFilesByFolder(folderId: Long?): Flow<List<HtmlFile>> = htmlFileDao.getFilesByFolder(folderId)

    fun getFilesByTag(tagId: Long): Flow<List<HtmlFile>> = htmlFileDao.getFilesByTag(tagId)

    fun searchFiles(query: String): Flow<List<HtmlFile>> = htmlFileDao.searchFiles("%$query%")

    suspend fun getFileById(id: Long): HtmlFile? = htmlFileDao.getFileById(id)

    suspend fun getRecentFiles(limit: Int = 20): List<HtmlFile> = htmlFileDao.getRecentFiles(limit)

    suspend fun getFrequentlyUsedFiles(limit: Int = 20): List<HtmlFile> = htmlFileDao.getFrequentlyUsedFiles(limit)

    fun getFavoriteFiles(): Flow<List<HtmlFile>> = htmlFileDao.getFavoriteFiles()

    suspend fun insertFile(htmlFile: HtmlFile): Long {
        val id = htmlFileDao.insertFile(htmlFile)
        addHistory(id, htmlFile.fileName, htmlFile.filePath)
        return id
    }

    suspend fun updateFile(htmlFile: HtmlFile) {
        htmlFileDao.updateFile(htmlFile)
    }

    suspend fun deleteFile(htmlFile: HtmlFile) {
        htmlFileDao.deleteFile(htmlFile)
        historyDao.deleteHistoryByFileId(htmlFile.id)
    }

    suspend fun deleteFileById(id: Long) {
        htmlFileDao.deleteFileById(id)
        historyDao.deleteHistoryByFileId(id)
    }

    suspend fun deleteAllFiles() {
        htmlFileDao.deleteAllFiles()
        historyDao.deleteAllHistory()
    }

    suspend fun getFileCount(): Int = htmlFileDao.getFileCount()

    suspend fun incrementAccessCount(id: Long) {
        htmlFileDao.incrementAccessCount(id, Date().time)
    }

    suspend fun moveFileToFolder(fileId: Long, folderId: Long?) {
        htmlFileDao.moveFileToFolder(fileId, folderId)
    }

    suspend fun toggleFavorite(fileId: Long) {
        val file = htmlFileDao.getFileById(fileId)
        file?.let {
            htmlFileDao.updateFile(it.copy(isFavorite = !it.isFavorite))
        }
    }

    // ===== Tag Operations for Files =====
    suspend fun getTagsForFile(htmlFileId: Long): List<Tag> = htmlFileDao.getTagsForFile(htmlFileId)

    fun getTagsForFileFlow(htmlFileId: Long): Flow<List<Tag>> = htmlFileDao.getTagsForFileFlow(htmlFileId)

    suspend fun addTagToFile(htmlFileId: Long, tagId: Long) {
        htmlFileDao.addTagToFile(htmlFileId, tagId)
    }

    suspend fun removeTagFromFile(htmlFileId: Long, tagId: Long) {
        htmlFileDao.removeTagFromFile(htmlFileId, tagId)
    }

    suspend fun clearTagsForFile(htmlFileId: Long) {
        htmlFileDao.clearTagsForFile(htmlFileId)
    }

    // ===== History Operations =====
    fun getAllHistory(): Flow<List<HistoryRecord>> = historyDao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryRecord>> = historyDao.searchHistory("%$query%")

    suspend fun getRecentHistory(limit: Int = 50): List<HistoryRecord> = historyDao.getRecentHistory(limit)

    suspend fun addHistory(fileId: Long, fileName: String, filePath: String) {
        val record = HistoryRecord(
            htmlFileId = fileId,
            fileName = fileName,
            filePath = filePath,
            accessedAt = Date()
        )
        historyDao.insertHistory(record)
    }

    suspend fun deleteHistory(historyRecord: HistoryRecord) {
        historyDao.deleteHistory(historyRecord)
    }

    suspend fun deleteAllHistory() {
        historyDao.deleteAllHistory()
    }

    suspend fun getHistoryCount(): Int = historyDao.getHistoryCount()

    suspend fun cleanOldHistory(daysToKeep: Int = 30) {
        val threshold = Date().time - (daysToKeep * 24 * 60 * 60 * 1000L)
        historyDao.deleteOldHistory(threshold)
    }

    // ===== Folder Operations =====
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()

    fun getFoldersByParentId(parentId: Long?): Flow<List<Folder>> = folderDao.getFoldersByParentId(parentId)

    suspend fun getFolderById(id: Long): Folder? = folderDao.getFolderById(id)

    suspend fun insertFolder(folder: Folder): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: Folder) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folder: Folder) = folderDao.deleteFolder(folder)

    suspend fun deleteFolderById(id: Long) = folderDao.deleteFolderById(id)

    suspend fun getFolderCount(): Int = folderDao.getFolderCount()

    // ===== Tag Operations =====
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun getTagById(id: Long): Tag? = tagDao.getTagById(id)

    suspend fun getTagByName(name: String): Tag? = tagDao.getTagByName(name)

    suspend fun insertTag(tag: Tag): Long = tagDao.insertTag(tag)

    suspend fun updateTag(tag: Tag) = tagDao.updateTag(tag)

    suspend fun deleteTag(tag: Tag) = tagDao.deleteTag(tag)

    suspend fun deleteTagById(id: Long) = tagDao.deleteTagById(id)

    suspend fun getTagCount(): Int = tagDao.getTagCount()

    suspend fun getFileCountByTag(tagId: Long): Int = tagDao.getFileCountByTag(tagId)
}
