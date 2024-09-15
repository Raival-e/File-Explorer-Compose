package com.raival.compose.file.explorer.screen.main.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTabContentView

@Composable
fun ColumnScope.TabContentView() {
    val mainActivityManager = globalClass.mainActivityManager

    Column(modifier = Modifier.weight(1f)) {
        if (mainActivityManager.tabs.isNotEmpty()) {
            val currentTabIndex = mainActivityManager.selectedTabIndex
            val currentTab = mainActivityManager.tabs[currentTabIndex]

            LaunchedEffect(key1 = currentTab.id) {
                currentTab.onTabStarted()
            }

            if (currentTab is RegularTab) {
                RegularTabContentView(currentTab)
            }
        }
    }
}