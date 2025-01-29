package com.raival.compose.file.explorer.screen.main.tab.files.holder

data class StorageDeviceHolder(
    val documentHolder: DocumentHolder,
    val title: String,
    val totalSize: Long,
    val usedSize: Long,
    val type: Int
)
