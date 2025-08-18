package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.limitLength
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesRow(tab: FilesTab) {
    var selectedIndex by remember {
        mutableIntStateOf(
            tab.selectedCategory?.let {
                tab.categories.indexOf(it)
            } ?: 0
        )
    }

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        divider = {},
    ) {
        Tab(
            selected = selectedIndex == 0,
            onClick = {
                selectedIndex = 0
                tab.selectedCategory = null
                tab.reloadFiles()
            },
            text = {
                Text(text = stringResource(R.string.all))
            }
        )
        tab.categories.forEach {
            Tab(
                selected = selectedIndex == tab.categories.indexOf(it) + 1,
                onClick = {
                    selectedIndex = tab.categories.indexOf(it) + 1
                    tab.selectedCategory = it
                    tab.reloadFiles()
                },
                text = {
                    Text(text = it.name.limitLength(18))
                }
            )
        }
    }
}