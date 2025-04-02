package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.apps.ui.AppsTabContentView
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FilesTabContentView
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import com.raival.compose.file.explorer.screen.main.tab.home.ui.HomeTabContentView

@Composable
fun ColumnScope.TabContentView(tabIndex: Int) {
    val mainActivityManager = globalClass.mainActivityManager

    Column(modifier = Modifier.weight(1f)) {
        if (mainActivityManager.tabs.isNotEmpty()) {
            val currentTab = mainActivityManager.tabs[tabIndex]

            if (currentTab is FilesTab) {
                FilesTabContentView(currentTab)
            } else if (currentTab is HomeTab) {
                HomeTabContentView(currentTab)
            } else if (currentTab is AppsTab) {
                AppsTabContentView(currentTab)
            }
        }
    }
}