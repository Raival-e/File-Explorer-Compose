package com.raival.compose.file.explorer.screen.main.tab.regular.modal

data class StorageDevice(
    val documentHolder: DocumentHolder,
    val title: String,
    val totalSize: Long,
    val usedSize: Long,
    val type: Int
)
