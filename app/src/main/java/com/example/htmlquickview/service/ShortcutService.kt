package com.example.htmlquickview.service

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import com.example.htmlquickview.HtmlViewerActivity

@androidx.compose.material3.ExperimentalMaterial3Api
class ShortcutService(private val context: Context) {
    private val TAG = ShortcutService::class.java.simpleName

    fun createShortcut(fileId: Long, fileName: String, filePath: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createShortcutOreoAndAbove(fileId, fileName)
            } else {
                createShortcutLegacy(fileId, fileName, filePath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "创建快捷方式失败", e)
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun createShortcutLegacy(fileId: Long, fileName: String, filePath: String): Boolean {
        val intent = Intent(context, HtmlViewerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("fileId", fileId)
            putExtra("filePath", filePath)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
            putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
            putExtra(Intent.EXTRA_SHORTCUT_NAME, fileName)
            putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, android.R.drawable.ic_dialog_info))
            putExtra("duplicate", false)
        }

        context.sendBroadcast(shortcutIntent)
        return true
    }

    private fun createShortcutOreoAndAbove(fileId: Long, fileName: String): Boolean {
        val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

        if (!shortcutManager.isRequestPinShortcutSupported) {
            return false
        }

        val intent = Intent(context, HtmlViewerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("fileId", fileId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val shortcutInfo = ShortcutInfo.Builder(context, "html_$fileId")
            .setIntent(intent)
            .setShortLabel(truncateFileName(fileName))
            .setLongLabel(fileName)
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_dialog_info))
            .build()

        shortcutManager.requestPinShortcut(shortcutInfo, null)
        return true
    }

    fun removeShortcut(fileId: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            shortcutManager.removeDynamicShortcuts(listOf("html_$fileId"))
        }
    }

    fun updateShortcut(fileId: Long, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager

            val intent = Intent(context, HtmlViewerActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra("fileId", fileId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val shortcutInfo = ShortcutInfo.Builder(context, "html_$fileId")
                .setIntent(intent)
                .setShortLabel(truncateFileName(fileName))
                .setLongLabel(fileName)
                .setIcon(Icon.createWithResource(context, android.R.drawable.ic_dialog_info))
                .build()

            shortcutManager.updateShortcuts(listOf(shortcutInfo))
        }
    }

    private fun truncateFileName(fileName: String): String {
        // Android ShortcutInfo shortLabel最大长度为10个字符
        return if (fileName.length > 10) {
            fileName.substring(0, 10) + "..."
        } else {
            fileName
        }
    }
}