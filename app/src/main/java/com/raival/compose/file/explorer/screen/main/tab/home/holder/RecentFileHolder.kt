package com.raival.compose.file.explorer.screen.main.tab.home.holder

import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import java.io.File

data class RecentFileHolder(
    val name: String,
    val path: String,
    val lastModified: Long,
    val documentHolder: DocumentHolder = DocumentHolder.fromFile(File(path))
)