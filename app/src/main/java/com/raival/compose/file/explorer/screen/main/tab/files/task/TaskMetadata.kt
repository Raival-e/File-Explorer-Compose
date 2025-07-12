package com.raival.compose.file.explorer.screen.main.tab.files.task

data class TaskMetadata(
    val id: String,
    val creationTime: String,
    val title: String,
    val subtitle: String,
    val displayDetails: String,
    val fullDetails: String,
    val isCancellable: Boolean,
    val canMoveToBackground: Boolean
)