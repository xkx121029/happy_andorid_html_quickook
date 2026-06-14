package com.example.htmlquickview.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.AnnotationCollection
import com.example.htmlquickview.model.SearchIndex
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE htmlFileId = :htmlFileId ORDER BY createdAt DESC")
    fun getAnnotationsByHtmlFileId(htmlFileId: Long): Flow<List<Annotation>>

    @Query("SELECT * FROM annotations WHERE htmlFileId = :htmlFileId ORDER BY createdAt DESC")
    suspend fun getAnnotationsByHtmlFileIdList(htmlFileId: Long): List<Annotation>

    @Query("SELECT * FROM annotations WHERE id = :id")
    suspend fun getAnnotationById(id: Long): Annotation?

    @Query("SELECT * FROM annotations WHERE type = :type AND htmlFileId = :htmlFileId")
    fun getAnnotationsByType(type: String, htmlFileId: Long): Flow<List<Annotation>>

    @Insert
    suspend fun insertAnnotation(annotation: Annotation): Long

    @Update
    suspend fun updateAnnotation(annotation: Annotation)

    @Delete
    suspend fun deleteAnnotation(annotation: Annotation)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Long)

    @Query("DELETE FROM annotations WHERE htmlFileId = :htmlFileId")
    suspend fun deleteAnnotationsByHtmlFileId(htmlFileId: Long)

    @Query("SELECT COUNT(*) FROM annotations WHERE htmlFileId = :htmlFileId")
    suspend fun getAnnotationCountByHtmlFileId(htmlFileId: Long): Int
}

@Dao
interface AnnotationCollectionDao {
    @Query("SELECT * FROM annotation_collections WHERE htmlFileId = :htmlFileId ORDER BY updatedAt DESC")
    fun getCollectionsByHtmlFileId(htmlFileId: Long): Flow<List<AnnotationCollection>>

    @Query("SELECT * FROM annotation_collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): AnnotationCollection?

    @Insert
    suspend fun insertCollection(collection: AnnotationCollection): Long

    @Update
    suspend fun updateCollection(collection: AnnotationCollection)

    @Delete
    suspend fun deleteCollection(collection: AnnotationCollection)

    @Query("DELETE FROM annotation_collections WHERE htmlFileId = :htmlFileId")
    suspend fun deleteCollectionsByHtmlFileId(htmlFileId: Long)
}

@Dao
interface SearchIndexDao {
    @Query("SELECT * FROM search_index WHERE htmlFileId = :htmlFileId")
    suspend fun getSearchIndexByHtmlFileId(htmlFileId: Long): SearchIndex?

    @Query("""
        SELECT * FROM search_index
        WHERE content LIKE :query OR title LIKE :query
        ORDER BY indexedAt DESC
    """)
    fun searchIndex(query: String): Flow<List<SearchIndex>>

    @Query("""
        SELECT si.* FROM search_index si
        INNER JOIN html_files hf ON si.htmlFileId = hf.id
        WHERE (si.content LIKE :query OR si.title LIKE :query)
        ORDER BY hf.lastModified DESC
    """)
    fun searchIndexWithFiles(query: String): Flow<List<SearchIndex>>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertSearchIndex(searchIndex: SearchIndex): Long

    @Update
    suspend fun updateSearchIndex(searchIndex: SearchIndex)

    @Query("DELETE FROM search_index WHERE htmlFileId = :htmlFileId")
    suspend fun deleteSearchIndexByHtmlFileId(htmlFileId: Long)

    @Query("DELETE FROM search_index")
    suspend fun deleteAllSearchIndex()
}
