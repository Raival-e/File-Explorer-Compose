package com.raival.compose.file.explorer.screen.main.tab.files.task

import java.util.UUID

abstract class Task(
    val id: String = UUID.randomUUID().toString()
) {
    var commonConflictResolution = TaskContentStatus.ASK
    open fun overrideConflicts(resolution: TaskContentStatus) {
        commonConflictResolution = resolution
    }

    abstract val metadata: TaskMetadata
    abstract val progressMonitor: TaskProgressMonitor
    abstract fun getCurrentStatus(): TaskStatus
    abstract fun validate(): Boolean
    abstract fun abortTask()
    abstract suspend fun run(params: TaskParameters)
    abstract suspend fun continueTask()
}