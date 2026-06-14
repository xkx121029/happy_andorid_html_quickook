package com.example.htmlquickview.viewmodel

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.AnnotationType
import com.example.htmlquickview.model.Folder
import com.example.htmlquickview.model.HistoryRecord
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.SearchIndex
import com.example.htmlquickview.model.Tag
import com.example.htmlquickview.model.WebArchive
import com.example.htmlquickview.repository.HtmlFileRepository
import com.example.htmlquickview.service.AnnotationService
import com.example.htmlquickview.service.ExportService
import com.example.htmlquickview.service.FileStorageService
import com.example.htmlquickview.service.SearchIndexService
import com.example.htmlquickview.service.WebArchiveService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File

class HtmlFileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HtmlFileRepository(application)
    private val fileStorageService = FileStorageService(application)
    private val webArchiveService = WebArchiveService(application)
    private val annotationService = AnnotationService(application)
    private val searchIndexService = SearchIndexService(application)
    private val exportService = ExportService(application)

    private val _searchQuery = MutableLiveData("")
    private val _sortType = MutableLiveData<SortType>(SortType.DATE_DESC)
    private val _selectedCategory = MutableLiveData<Category>(Category.ALL)
    private val _selectedFolderId = MutableLiveData<Long?>(null)
    private val _selectedTagId = MutableLiveData<Long?>(null)

    // 搜索状态
    private val _fullTextQuery = MutableLiveData("")
    val fullTextQuery: LiveData<String> = _fullTextQuery

    // 保存进度
    private val _saveProgress = MutableLiveData<SaveProgressState>()
    val saveProgress: LiveData<SaveProgressState> = _saveProgress

    val allFiles = repository.getAllFiles().asLiveData()
    val allHistory = repository.getAllHistory().asLiveData()
    val allFolders = repository.getAllFolders().asLiveData()
    val allTags = repository.getAllTags().asLiveData()
    val favoriteFiles = repository.getFavoriteFiles().asLiveData()

    val searchQuery: LiveData<String> = _searchQuery
    val sortType: LiveData<SortType> = _sortType
    val selectedCategory: LiveData<Category> = _selectedCategory
    val selectedFolderId: LiveData<Long?> = _selectedFolderId
    val selectedTagId: LiveData<Long?> = _selectedTagId

    private val _currentFiles = MutableLiveData<List<HtmlFile>>()
    val currentFiles: LiveData<List<HtmlFile>> = _currentFiles

    private var updateJob: Job? = null

    init {
        updateCurrentFiles()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateCurrentFiles()
    }

    fun setSortType(type: SortType) {
        _sortType.value = type
        updateCurrentFiles()
    }

    fun setCategory(category: Category) {
        _selectedCategory.value = category
        // �л�����ʱ����ļ��кͱ�ǩɸѡ
        if (category != Category.FOLDER) _selectedFolderId.value = null
        if (category != Category.TAG) _selectedTagId.value = null
        updateCurrentFiles()
    }

    fun setFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        _selectedCategory.value = Category.FOLDER
        _selectedTagId.value = null
        updateCurrentFiles()
    }

    fun setTag(tagId: Long?) {
        _selectedTagId.value = tagId
        _selectedCategory.value = Category.TAG
        _selectedFolderId.value = null
        updateCurrentFiles()
    }

    fun clearFilters() {
        _selectedFolderId.value = null
        _selectedTagId.value = null
        _selectedCategory.value = Category.ALL
        _searchQuery.value = ""
        updateCurrentFiles()
    }

    fun insertFile(content: String, fileName: String? = null, folderId: Long? = null, sourceUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val htmlFile = fileStorageService.createHtmlFile(content, fileName).copy(
                folderId = folderId,
                sourceUrl = sourceUrl
            )
            repository.insertFile(htmlFile)
        }
    }

    fun deleteFile(htmlFile: HtmlFile) {
        viewModelScope.launch(Dispatchers.IO) {
            fileStorageService.deleteFile(htmlFile.filePath)
            repository.deleteFile(htmlFile)
        }
    }

    fun deleteFileById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = repository.getFileById(id)
            file?.let {
                fileStorageService.deleteFile(it.filePath)
            }
            repository.deleteFileById(id)
        }
    }

    suspend fun getFileById(id: Long): HtmlFile? {
        return withContext(Dispatchers.IO) {
            repository.getFileById(id)
        }
    }

    fun deleteAllFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllFiles().first().forEach { file ->
                fileStorageService.deleteFile(file.filePath)
            }
            repository.deleteAllFiles()
        }
    }

    fun loadFileContent(filePath: String): Result<String> {
        return try {
            Result.success(fileStorageService.loadHtmlContent(filePath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun incrementAccessCount(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementAccessCount(fileId)
        }
    }

    fun toggleFavorite(fileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(fileId)
        }
    }

    fun moveFileToFolder(fileId: Long, folderId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.moveFileToFolder(fileId, folderId)
        }
    }

    fun updateFileDescription(fileId: Long, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = repository.getFileById(fileId)
            file?.let {
                repository.updateFile(it.copy(description = description))
            }
        }
    }

    // ===== Tag Operations =====
    fun addTagToFile(htmlFileId: Long, tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTagToFile(htmlFileId, tagId)
        }
    }

    fun removeTagFromFile(htmlFileId: Long, tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeTagFromFile(htmlFileId, tagId)
        }
    }

    suspend fun getTagsForFile(htmlFileId: Long): List<Tag> {
        return withContext(Dispatchers.IO) {
            repository.getTagsForFile(htmlFileId)
        }
    }

    fun createTag(name: String, color: String = "#FF6200EE") {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getTagByName(name)
            if (existing == null) {
                repository.insertTag(Tag(name = name, color = color))
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTag(tag)
        }
    }

    // ===== Folder Operations =====
    fun createFolder(name: String, parentId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertFolder(Folder(name = name, parentId = parentId))
        }
    }

    fun updateFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFolder(folder)
        }
    }

    // ===== History Operations =====
    fun deleteHistory(record: HistoryRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistory(record)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory()
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            fileStorageService.clearCache()
        }
    }

    private fun updateCurrentFiles() {
        updateJob?.cancel()

        updateJob = viewModelScope.launch(Dispatchers.IO) {
            val query = _searchQuery.value ?: ""
            val sort = _sortType.value ?: SortType.DATE_DESC
            val category = _selectedCategory.value ?: Category.ALL
            val folderId = _selectedFolderId.value
            val tagId = _selectedTagId.value

            val files = when {
                query.isNotEmpty() -> {
                    repository.searchFiles(query)
                }
                category == Category.FOLDER && folderId != null -> {
                    repository.getFilesByFolder(folderId)
                }
                category == Category.TAG && tagId != null -> {
                    repository.getFilesByTag(tagId)
                }
                category == Category.RECENT -> {
                    repository.getRecentFiles(20).let { MutableLiveData(it).asFlow() }
                }
                category == Category.FREQUENTLY -> {
                    repository.getFrequentlyUsedFiles(20).let { MutableLiveData(it).asFlow() }
                }
                category == Category.FAVORITE -> {
                    repository.getFavoriteFiles()
                }
                else -> {
                    repository.getAllFiles()
                }
            }

            files.collect { list ->
                val sorted = when (sort) {
                    SortType.NAME_ASC -> list.sortedBy { it.fileName }
                    SortType.NAME_DESC -> list.sortedByDescending { it.fileName }
                    SortType.DATE_ASC -> list.sortedBy { it.createdAt }
                    SortType.DATE_DESC -> list.sortedByDescending { it.createdAt }
                    SortType.SIZE_ASC -> list.sortedBy { it.fileSize }
                    SortType.SIZE_DESC -> list.sortedByDescending { it.fileSize }
                }
                _currentFiles.postValue(sorted)
            }
        }
    }

    enum class SortType {
        NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC, SIZE_ASC, SIZE_DESC
    }

    enum class Category {
        ALL, RECENT, FREQUENTLY, FAVORITE, FOLDER, TAG
    }

    data class SaveProgressState(
        val progress: Int,
        val status: String,
        val isComplete: Boolean = false
    )

    // ===== 离线网页保存 =====
    fun saveWebPage(
        htmlFile: HtmlFile,
        sourceUrl: String?,
        onProgress: (Int, String) -> Unit,
        onComplete: (Result<WebArchive>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _saveProgress.postValue(SaveProgressState(0, "准备保存..."))

            val content = try {
                fileStorageService.loadHtmlContent(htmlFile.filePath)
            } catch (e: Exception) {
                onComplete(Result.failure(e))
                return@launch
            }

            val result = webArchiveService.saveWebPage(
                htmlFileId = htmlFile.id,
                sourceUrl = sourceUrl ?: htmlFile.sourceUrl ?: "",
                htmlContent = content,
                title = htmlFile.fileName,
                onProgress = { current, total, status ->
                    val progress = if (total > 0) (current * 100 / total) else 0
                    _saveProgress.postValue(SaveProgressState(progress, status))
                    onProgress(progress, status)
                }
            )

            _saveProgress.postValue(SaveProgressState(100, "保存完成", true))
            onComplete(result)
        }
    }

    suspend fun getWebArchive(htmlFileId: Long): WebArchive? {
        return withContext(Dispatchers.IO) {
            webArchiveService.getArchive(htmlFileId)
        }
    }

    // ===== 批注功能 =====
    fun getAnnotations(htmlFileId: Long): Flow<List<Annotation>> {
        return annotationService.getAnnotations(htmlFileId)
    }

    suspend fun getAnnotationsList(htmlFileId: Long): List<Annotation> {
        return withContext(Dispatchers.IO) {
            annotationService.getAnnotationsList(htmlFileId)
        }
    }

    fun addHighlight(htmlFileId: Long, content: String, position: String, color: String) {
        viewModelScope.launch(Dispatchers.IO) {
            annotationService.addHighlight(htmlFileId, content, position, color)
        }
    }

    fun addNote(htmlFileId: Long, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            annotationService.addNote(htmlFileId, content)
        }
    }

    fun addBookmark(htmlFileId: Long, title: String, position: String) {
        viewModelScope.launch(Dispatchers.IO) {
            annotationService.addBookmark(htmlFileId, title, position)
        }
    }

    fun deleteAnnotation(annotation: Annotation) {
        viewModelScope.launch(Dispatchers.IO) {
            annotationService.deleteAnnotation(annotation)
        }
    }

    suspend fun exportAnnotationsToHtml(htmlFileId: Long): String {
        return withContext(Dispatchers.IO) {
            annotationService.exportAnnotationsToHtml(htmlFileId)
        }
    }

    // ===== 全文搜索 =====
    fun searchFilesFullText(query: String): Flow<List<HtmlFile>> {
        _fullTextQuery.value = query
        return searchIndexService.searchHtmlFiles(query)
    }

    suspend fun indexHtmlFile(htmlFile: HtmlFile): Result<Unit> {
        return withContext(Dispatchers.IO) {
            searchIndexService.indexHtmlFile(htmlFile)
        }
    }

    suspend fun rebuildAllSearchIndex(onProgress: (Int, Int) -> Unit = { _, _ -> }) {
        withContext(Dispatchers.IO) {
            val files = repository.getAllFiles().first()
            searchIndexService.rebuildAllIndex(files, onProgress)
        }
    }

    fun highlightSearchResults(text: String, query: String): String {
        return searchIndexService.highlightSearchResults(text, query)
    }

    fun getSearchSnippet(text: String, query: String): String {
        return searchIndexService.getSearchSnippet(text, query)
    }

    // ===== 导出功能 =====
    suspend fun exportToPdf(htmlFile: HtmlFile): Result<File> {
        return withContext(Dispatchers.IO) {
            exportService.exportToPdf(htmlFile)
        }
    }

    suspend fun exportToEpub(htmlFile: HtmlFile): Result<File> {
        return withContext(Dispatchers.IO) {
            exportService.exportToEpub(htmlFile)
        }
    }

    suspend fun exportToHtml(htmlFile: HtmlFile): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val content = fileStorageService.loadHtmlContent(htmlFile.filePath)
                val outputFile = File(exportService.getExportDir(), "${htmlFile.fileName}.html")
                outputFile.writeText(content)
                Result.success(outputFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getExportedFiles(): List<File> {
        return exportService.getExportedFiles()
    }

    suspend fun deleteExportedFile(file: File): Boolean {
        return exportService.deleteExportedFile(file)
    }
}
