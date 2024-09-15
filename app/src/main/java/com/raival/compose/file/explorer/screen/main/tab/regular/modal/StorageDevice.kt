package com.raival.compose.file.explorer.screen.main.tab.regular.modal

import com.anggrayudi.storage.file.StorageType

data class StorageDevice(
    val documentHolder: DocumentHolder,
    val title: String,
    val totalSize: Long,
    val usedSize: Long,
    val type: Int
)
