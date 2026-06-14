package com.example.htmlquickview.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class HtmlCacheService(private val context: Context) {
    private val TAG = HtmlCacheService::class.java.simpleName
    private val cacheDir: File by lazy {
        File(context.cacheDir, "html_web_cache").apply { mkdirs() }
    }

    private val memoryCache = ConcurrentHashMap<String, String>()
    private val cacheTimeouts = ConcurrentHashMap<String, Long>()
    private val CACHE_TIMEOUT_MS = 24 * 60 * 60 * 1000L

    @WorkerThread
    fun cacheResource(url: String): String? {
        return try {
            val cacheKey = generateCacheKey(url)
            val cachedPath = getCachedPath(cacheKey)

            if (isCacheValid(cacheKey)) {
                return cachedPath
            }

            val connection = URL(url).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            connection.getInputStream().use { inputStream ->
                FileOutputStream(File(cachedPath)).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            memoryCache[cacheKey] = cachedPath
            cacheTimeouts[cacheKey] = System.currentTimeMillis()

            cachedPath
        } catch (e: Exception) {
            Log.e(TAG, "缓存资源失败: $url", e)
            null
        }
    }

    fun getCachedResource(url: String): String? {
        val cacheKey = generateCacheKey(url)
        if (!isCacheValid(cacheKey)) {
            return null
        }
        return memoryCache[cacheKey] ?: getCachedPath(cacheKey).takeIf { File(it).exists() }
    }

    fun isCached(url: String): Boolean {
        val cacheKey = generateCacheKey(url)
        return isCacheValid(cacheKey) && File(getCachedPath(cacheKey)).exists()
    }

    fun clearOldCache() {
        val now = System.currentTimeMillis()
        cacheTimeouts.entries.removeIf { (key, timestamp) ->
            if (now - timestamp > CACHE_TIMEOUT_MS) {
                File(getCachedPath(key)).delete()
                true
            } else {
                false
            }
        }
    }

    fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
        memoryCache.clear()
        cacheTimeouts.clear()
    }

    fun getCacheSize(): Long {
        return cacheDir.walk().sumOf { it.length() }
    }

    fun getRelativeCachePath(url: String): String? {
        val cachedPath = getCachedResource(url) ?: return null
        return Uri.fromFile(File(cachedPath)).toString()
    }

    private fun generateCacheKey(url: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun getCachedPath(cacheKey: String): String {
        return File(cacheDir, cacheKey).absolutePath
    }

    private fun isCacheValid(cacheKey: String): Boolean {
        val timestamp = cacheTimeouts[cacheKey] ?: return false
        return System.currentTimeMillis() - timestamp < CACHE_TIMEOUT_MS
    }
}