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

    open fun onTabRemoved() {}
    open fun onTabStopped() {}
    open fun onTabResumed() {}
    open fun onTabStarted() {
        isCreated = true
    }

    open fun onBackPressed(): Boolean = false

    fun requestHomeToolbarUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            globalClass.mainActivityManager.updateHomeToolbar(
                title = title,
                subtitle = subtitle
            )
        }
    }
}