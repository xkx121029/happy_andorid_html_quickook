package com.example.htmlquickview.service

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.htmlquickview.model.HtmlFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class FileStorageService(private val context: Context) {
    private val htmlFilesDir: File by lazy {
        File(context.filesDir, "html_files").apply { mkdirs() }
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, "html_cache").apply { mkdirs() }
    }

    private val masterKey: MasterKey by lazy {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        MasterKey.Builder(context)
            .setKeyGenParameterSpec(keyGenParameterSpec)
            .build()
    }

    fun generateFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "html_${timestamp}.html"
    }

    fun saveHtmlContent(content: String, fileName: String): String {
        val file = File(htmlFilesDir, fileName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(content.toByteArray(StandardCharsets.UTF_8))
            }
        } else {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray(StandardCharsets.UTF_8))
            }
        }
        return file.absolutePath
    }

    fun loadHtmlContent(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw FileNotFoundException("File not found: $filePath")
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()

            encryptedFile.openFileInput().use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.toString(StandardCharsets.UTF_8.name())
                }
            }
        } else {
            FileInputStream(file).use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.toString(StandardCharsets.UTF_8.name())
                }
            }
        }
    }

    fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return file.delete()
    }

    fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    fun getFileSize(filePath: String): Long {
        return File(filePath).length()
    }

    fun listAllFiles(): List<File> {
        return htmlFilesDir.listFiles()?.toList() ?: emptyList()
    }

    fun saveResourceFile(resourceData: ByteArray, fileName: String): String {
        val resourceDir = File(htmlFilesDir, "resources").apply { mkdirs() }
        val file = File(resourceDir, fileName)
        FileOutputStream(file).use { it.write(resourceData) }
        return file.absolutePath
    }

    fun getResourceFilePath(resourceName: String): String? {
        val resourceDir = File(htmlFilesDir, "resources")
        val file = File(resourceDir, resourceName)
        return if (file.exists()) file.absolutePath else null
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun saveToCache(content: String, cacheKey: String): String {
        val cacheFile = File(cacheDir, "${cacheKey}.html")
        FileOutputStream(cacheFile).use { it.write(content.toByteArray()) }
        return cacheFile.absolutePath
    }

    fun loadFromCache(cacheKey: String): String? {
        val cacheFile = File(cacheDir, "${cacheKey}.html")
        return if (cacheFile.exists()) {
            FileInputStream(cacheFile).use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } else {
            null
        }
    }

    fun generateContentHash(content: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun createHtmlFile(content: String, fileName: String? = null): HtmlFile {
        val actualFileName = fileName ?: generateFileName()
        val filePath = saveHtmlContent(content, actualFileName)
        val fileSize = getFileSize(filePath)
        val contentHash = generateContentHash(content)

        return HtmlFile(
            fileName = actualFileName,
            filePath = filePath,
            fileSize = fileSize,
            contentHash = contentHash
        )
    }
}