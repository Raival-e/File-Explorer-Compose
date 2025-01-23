package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.common.extension.emptyString

class FilesTabTaskDetails(
    var task: FilesTabTask,
    var type: Int = FilesTabTask.TASK_NONE,
    var title: String = emptyString,
    var subtitle: String = emptyString,
    var info: String = emptyString,
    var progress: Float = -1f
)