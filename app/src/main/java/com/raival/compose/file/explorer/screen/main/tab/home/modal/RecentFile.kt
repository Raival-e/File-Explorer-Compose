package com.raival.compose.file.explorer.screen.main.tab.home.modal

import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import java.io.File

data class RecentFile(
    val name: String,
    val path: String,
    val lastModified: Long,
    val documentHolder: DocumentHolder = DocumentHolder.fromFile(File(path))
)