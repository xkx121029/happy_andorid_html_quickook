package com.example.htmlquickview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.example.htmlquickview.R
import com.example.htmlquickview.model.HtmlFile
import com.example.htmlquickview.service.FileStorageService
import com.example.htmlquickview.ui.screen.DetailScreen
import com.example.htmlquickview.ui.screen.HistoryScreen
import com.example.htmlquickview.ui.screen.HomeScreen
import com.example.htmlquickview.ui.screen.SettingsScreen
import com.example.htmlquickview.ui.theme.HtmlQuickViewTheme
import com.example.htmlquickview.viewmodel.HtmlFileViewModel

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: HtmlFileViewModel
    private lateinit var fileStorageService: FileStorageService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HtmlFileViewModel::class.java]
        fileStorageService = FileStorageService(this)

        setContent {
            HtmlQuickViewTheme {
                MainApp(viewModel, fileStorageService)
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainApp(
    viewModel: HtmlFileViewModel,
    fileStorageService: FileStorageService
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFile by remember { mutableStateOf<HtmlFile?>(null) }

    // 如果选择了文件，显示详情页面
    if (selectedFile != null) {
        DetailScreen(
            htmlFile = selectedFile!!,
            fileStorageService = fileStorageService,
            viewModel = viewModel,
            onBack = { selectedFile = null }
        )
    } else {
        // 否则显示主界面
        Scaffold(
            bottomBar = {
                BottomAppBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, stringResource(R.string.title_home)) },
                        label = { Text(stringResource(R.string.title_home)) },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, stringResource(R.string.title_history)) },
                        label = { Text(stringResource(R.string.title_history)) },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, stringResource(R.string.title_settings)) },
                        label = { Text(stringResource(R.string.title_settings)) },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier.padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(viewModel) { file ->
                        viewModel.incrementAccessCount(file.id)
                        selectedFile = file
                    }
                    1 -> HistoryScreen(viewModel) { file ->
                        viewModel.incrementAccessCount(file.id)
                        selectedFile = file
                    }
                    2 -> SettingsScreen(viewModel)
                }
            }
        }
    }
}