package com.raival.compose.file.explorer.screen.main.tab

import com.raival.compose.file.explorer.App.Companion.globalClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class Tab {
    var isCreated = false

    abstract val id: Int
    abstract val title: String
    abstract val subtitle: String
    abstract val header: String

    open fun onTabStopped() {}
    open fun onTabResumed() {}
    open fun onTabClicked() {}
    open fun onTabStarted() {
        isCreated = true
    }

    fun requestHomeToolbarUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            globalClass.mainActivityManager.title = title
            globalClass.mainActivityManager.subtitle = subtitle
        }
    }
}