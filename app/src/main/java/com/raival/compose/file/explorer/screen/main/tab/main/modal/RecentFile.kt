package com.raival.compose.file.explorer.screen.main.tab.main.modal

data class RecentFile(
    val name: String,
    val path: String,
    val lastModified: Long,
)