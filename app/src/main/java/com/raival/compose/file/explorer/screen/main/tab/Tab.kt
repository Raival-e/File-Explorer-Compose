package com.raival.compose.file.explorer.screen.main.tab

abstract class Tab {
    abstract val id: Int
    abstract val title: String
    abstract val subtitle: String
    abstract var onTabClicked: () -> Unit
    abstract var onTabResumed: () -> Unit
    abstract var onTabStopped: () -> Unit
    abstract var onTabStarted: () -> Unit
}