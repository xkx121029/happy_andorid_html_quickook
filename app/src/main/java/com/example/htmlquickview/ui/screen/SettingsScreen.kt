package com.example.htmlquickview.ui.screen

import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.R
import com.example.htmlquickview.ui.theme.ThemeManager
import com.example.htmlquickview.ui.theme.ThemeMode
import com.example.htmlquickview.viewmodel.HtmlFileViewModel

@Composable
@androidx.compose.material3.ExperimentalMaterial3Api
fun SettingsScreen(
    viewModel: HtmlFileViewModel
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 深色模式设置
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_theme)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        ThemeModeItem(
                            title = stringResource(R.string.theme_system),
                            selected = ThemeManager.currentTheme == ThemeMode.SYSTEM,
                            onClick = { com.example.htmlquickview.ui.theme.setThemeMode(ThemeMode.SYSTEM) }
                        )
                        ThemeModeItem(
                            title = stringResource(R.string.theme_light),
                            selected = ThemeManager.currentTheme == ThemeMode.LIGHT,
                            onClick = { com.example.htmlquickview.ui.theme.setThemeMode(ThemeMode.LIGHT) }
                        )
                        ThemeModeItem(
                            title = stringResource(R.string.theme_dark),
                            selected = ThemeManager.currentTheme == ThemeMode.DARK,
                            onClick = { com.example.htmlquickview.ui.theme.setThemeMode(ThemeMode.DARK) }
                        )
                    }
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.settings_cache)
                ) {
                    Button(
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(stringResource(R.string.settings_clear_cache))
                    }
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.settings_about)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_version))
                            Text(versionName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            content()
        }
    }
}

@Composable
fun ThemeModeItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}