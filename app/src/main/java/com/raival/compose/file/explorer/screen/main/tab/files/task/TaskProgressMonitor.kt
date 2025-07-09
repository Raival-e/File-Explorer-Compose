package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.common.extension.emptyString

data class TaskProgressMonitor(
    var status: TaskStatus = TaskStatus.PENDING, // Pending, Running, Success, Failed, Cancelled
    var taskTitle: String = emptyString, // e.g, "Copy", "Move"
    var processName: String = emptyString, // e.g, "Copying", "Moving"
    var contentName: String = emptyString, // e.g, "File 1", "File 2". can be empty when not processing files
    var totalContent: Int = 0, // Total number of content to be processed
    var remainingContent: Int = 0, // Remaining number of content to be processed
    var progress: Float = -1f, // Progress of the task from 0 to 1
    var summary: String = emptyString // Summary of the task after it stops
)