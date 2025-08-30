package com.raival.compose.file.explorer.screen.main.tab

import com.raival.compose.file.explorer.App.Companion.globalClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class Tab {
    var isCreated = false

    abstract val id: Int
    abstract val header: String

    open fun onTabRemoved() {}
    open fun onTabStopped() {}
    open fun onTabResumed() {}
    open fun onTabStarted() {
        isCreated = true
    }

    abstract suspend fun getTitle(): String
    abstract suspend fun getSubtitle(): String

    open fun onBackPressed(): Boolean = false

    fun requestHomeToolbarUpdate() {
        if (globalClass.mainActivityManager.getActiveTab() == this) {
            CoroutineScope(Dispatchers.IO).launch {
                globalClass.mainActivityManager.updateHomeToolbar(
                    title = getTitle(),
                    subtitle = getSubtitle()
                )
            }
        }
    }
}