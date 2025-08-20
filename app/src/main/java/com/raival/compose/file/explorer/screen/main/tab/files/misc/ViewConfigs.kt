package com.raival.compose.file.explorer.screen.main.tab.files.misc

import com.raival.compose.file.explorer.App.Companion.globalClass

data class ViewConfigs(
    val viewType: ViewType = ViewType.LIST,
    val columnCount: Int = 1,
    val cropThumbnails: Boolean = false,
    val galleryMode: Boolean = false,
    val hideMediaNames: Boolean = false,
    val itemSize: Int = globalClass.preferencesManager.itemSize
)