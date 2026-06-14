package com.example.htmlquickview

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htmlquickview.repository.HtmlFileRepository
import com.example.htmlquickview.service.ShareReceiverService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
class ShareReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareReceiverService = ShareReceiverService(this)
        val repository = HtmlFileRepository(this)

        when (val result = shareReceiverService.processShareIntent(intent)) {
            is ShareReceiverService.ShareResult.Success -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.insertFile(result.htmlFile)
                }
                // 先显示Toast，再启动MainActivity，最后finish
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
            is ShareReceiverService.ShareResult.Error -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
            }
        }

        // 使用Handler延迟finish，确保Toast有时间显示
        android.os.Handler(mainLooper).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, 500)
    }
}