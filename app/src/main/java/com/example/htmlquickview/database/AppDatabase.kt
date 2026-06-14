package com.example.htmlquickview.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.htmlquickview.model.Annotation
import com.example.htmlquickview.model.AnnotationCollection
import com.example.htmlquickview.model.Folder
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.model.HtmlFileTag
import com.example.htmlquickview.model.HistoryRecord
import com.example.htmlquickview.model.SearchIndex
import com.example.htmlquickview.model.Tag
import com.example.htmlquickview.model.WebArchive
import com.example.htmlquickview.model.WebResource

@Database(
    entities = [
        HtmlFile::class,
        HistoryRecord::class,
        Folder::class,
        Tag::class,
        HtmlFileTag::class,
        WebArchive::class,
        WebResource::class,
        Annotation::class,
        AnnotationCollection::class,
        SearchIndex::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun htmlFileDao(): HtmlFileDao
    abstract fun historyDao(): HistoryDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun webArchiveDao(): WebArchiveDao
    abstract fun webResourceDao(): WebResourceDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun annotationCollectionDao(): AnnotationCollectionDao
    abstract fun searchIndexDao(): SearchIndexDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "html_files.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
