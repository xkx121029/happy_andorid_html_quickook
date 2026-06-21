package com.example.htmlquickview.service

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.net.BindException

enum class ServerErrorType {
    NONE,
    PORT_IN_USE,
    NETWORK_ERROR,
    FILE_LOAD_ERROR,
    UNKNOWN_ERROR
}

class LocalHttpServerService(private val context: Context) {
    private var server: LocalHttpServer? = null
    private var currentPort: Int = 8080
    private var htmlContent: String = ""
    private var lastError: String = ""
    private var lastErrorType: ServerErrorType = ServerErrorType.NONE

    @Synchronized
    fun startServer(htmlContent: String, startPort: Int = 8080): Boolean {
        stopServer()
        this.htmlContent = htmlContent
        this.currentPort = startPort
        lastError = ""
        lastErrorType = ServerErrorType.NONE

        return try {
            server = LocalHttpServer(startPort, htmlContent)
            server?.start()
            // 等待服务器真正启动，增加等待时间
            Thread.sleep(500)
            if (server?.isAlive == true) {
                true
            } else {
                lastErrorType = ServerErrorType.PORT_IN_USE
                lastError = "端口 $startPort 被占用"
                false
            }
        } catch (e: BindException) {
            lastErrorType = ServerErrorType.PORT_IN_USE
            lastError = "端口 $startPort 被其他应用占用"
            e.printStackTrace()
            false
        } catch (e: IOException) {
            lastErrorType = ServerErrorType.NETWORK_ERROR
            lastError = "网络错误: ${e.message ?: "无法连接"}"
            e.printStackTrace()
            false
        } catch (e: Exception) {
            lastErrorType = ServerErrorType.UNKNOWN_ERROR
            lastError = "未知错误: ${e.message ?: "未知原因"}"
            e.printStackTrace()
            false
        }
    }

    @Synchronized
    fun stopServer() {
        try {
            server?.stop()
            Thread.sleep(50) // 等待服务器真正停止
        } catch (e: Exception) {
            e.printStackTrace()
        }
        server = null
    }

    fun isRunning(): Boolean {
        return try {
            server?.isAlive == true
        } catch (e: Exception) {
            false
        }
    }

    fun getServerUrl(): String = "http://localhost:$currentPort/"

    fun getPort(): Int = currentPort

    fun getLastError(): String = lastError

    fun getLastErrorType(): ServerErrorType = lastErrorType

    private class LocalHttpServer(port: Int, private val htmlContent: String) : NanoHTTPD("0.0.0.0", port) {
        override fun serve(session: IHTTPSession): Response {
            val bytes = htmlContent.toByteArray(Charsets.UTF_8)
            
            return newFixedLengthResponse(
                Response.Status.OK,
                "text/html; charset=UTF-8",
                java.io.ByteArrayInputStream(bytes),
                bytes.size.toLong()
            ).apply {
                addHeader("Access-Control-Allow-Origin", "*")
                addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                addHeader("Access-Control-Allow-Headers", "Content-Type")
                addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                addHeader("Pragma", "no-cache")
                addHeader("Expires", "0")
            }
        }
    }
}
