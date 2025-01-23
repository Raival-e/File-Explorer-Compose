package com.raival.compose.file.explorer.screen.main.tab

import com.raival.compose.file.explorer.App.Companion.globalClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class Tab {
    abstract val id: Int
    abstract val title: String
    abstract val subtitle: String
    abstract val header: String
    abstract var onTabClicked: () -> Unit
    abstract var onTabResumed: () -> Unit
    abstract var onTabStopped: () -> Unit
    abstract var onTabStarted: () -> Unit

    fun requestHomeToolbarUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            globalClass.mainActivityManager.title = title
            globalClass.mainActivityManager.subtitle = subtitle
        }
    }
}