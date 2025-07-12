package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder

data class TaskContentItem(
    val content: ContentHolder,
    val relativePath: String,
    var status: TaskContentStatus
)