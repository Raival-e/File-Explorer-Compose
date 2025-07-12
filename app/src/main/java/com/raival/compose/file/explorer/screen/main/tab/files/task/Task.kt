package com.raival.compose.file.explorer.screen.main.tab.files.task

import java.util.UUID

abstract class Task(
    val id: String = UUID.randomUUID().toString()
) {
    var commonConflictResolution = TaskContentStatus.ASK

    @Volatile
    var aborted = false

    /**
     * Used to protect task from the time it is added to the running list and gets actually run
     */
    var protect = false

    open fun overrideConflicts(resolution: TaskContentStatus) {
        commonConflictResolution = resolution
    }

    open fun abortTask() {
        aborted = true
    }

    open fun getConflictedFile(): TaskContentItem? {
        return null
    }

    abstract val metadata: TaskMetadata
    abstract val progressMonitor: TaskProgressMonitor
    abstract fun getCurrentStatus(): TaskStatus
    abstract fun validate(): Boolean
    abstract suspend fun run(params: TaskParameters)
    abstract suspend fun run()
    abstract suspend fun continueTask()
    abstract fun setParameters(params: TaskParameters)
}