package com.raival.compose.file.explorer.screen.main.tab.regular.task

import com.raival.compose.file.explorer.common.extension.emptyString

class RegularTabTaskDetails(
    var task: RegularTabTask,
    var type: Int = RegularTabTask.TASK_NONE,
    var title: String = emptyString,
    var subtitle: String = emptyString,
    var info: String = emptyString,
    var progress: Float = -1f
)