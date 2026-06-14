package com.example.htmlquickview.util

/**
 * 文件工具类
 */
object FileUtils {
    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串，如 "1.5MB"
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            size < 1024 * 1024 * 1024 -> String.format("%.1fMB", size / (1024.0 * 1024.0))
            else -> String.format("%.2fGB", size / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
