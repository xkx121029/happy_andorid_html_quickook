package com.example.htmlquickview.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.htmlquickview.viewmodel.HtmlFileViewModel

@Composable
fun CategoryTabs(
    selectedCategory: HtmlFileViewModel.Category,
    onCategoryChange: (HtmlFileViewModel.Category) -> Unit
) {
    val categories = listOf(
        HtmlFileViewModel.Category.ALL to "全部",
        HtmlFileViewModel.Category.RECENT to "最近",
        HtmlFileViewModel.Category.FREQUENTLY to "常用",
        HtmlFileViewModel.Category.FAVORITE to "收藏"
    )

    TabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }
            .coerceAtLeast(0),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        categories.forEach { (category, label) ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategoryChange(category) },
                text = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.wrapContentWidth()
                    )
                },
                modifier = Modifier.padding(horizontal = 8.dp),
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.outline
            )
        }
    }
}
