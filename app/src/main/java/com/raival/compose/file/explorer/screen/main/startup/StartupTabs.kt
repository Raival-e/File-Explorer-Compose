package com.raival.compose.file.explorer.screen.main.startup

import com.raival.compose.file.explorer.common.emptyString

data class StartupTabs(
    val tabs: ArrayList<StartupTab>
) {
    companion object {
        fun default() = StartupTabs(arrayListOf(StartupTab(StartupTabType.HOME)))
    }
}

data class StartupTab(
    val type: StartupTabType,
    val extra: String = emptyString
)

enum class StartupTabType {
    HOME,
    APPS,
    FILES
}