package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.showMsg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TaskManager {
    val tasks = mutableListOf<Task>()
    val runningTasks = mutableListOf<Task>()
    val pausedTasks = mutableListOf<Task>()
    val failedTasks = mutableListOf<Task>()
    val invalidTasks = mutableListOf<Task>()

    private var isMonitoring = false

    val runningTaskDialogInfo = RunningTaskDialogInfo()
    val taskInterceptor = TaskConflict()

    fun addTask(task: Task) {
        tasks.add(task)
        globalClass.showMsg(globalClass.resources.getString(R.string.new_task_has_been_added))
    }

    fun addTaskAndRun(task: Task, parameters: TaskParameters) {
        tasks.add(task)
        runTask(task.id, parameters)
    }

    fun removeTask(id: String) {
        tasks.removeIf { it.id == id }
        runningTasks.removeIf { it.id == id }
        pausedTasks.removeIf { it.id == id }
        failedTasks.removeIf { it.id == id }
        invalidTasks.removeIf { it.id == id }
    }

    fun validateTasks() {
        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (!task.validate()) {
                invalidTasks.add(task)
                // Use the iterator's remove method, which is safe
                iterator.remove()
            }
        }
    }

    fun overrideConflicts(taskId: String, resolution: TaskContentStatus) {
        val task = pausedTasks.find { it.id == taskId }
        task?.overrideConflicts(resolution)
    }

    fun runTask(id: String, taskParameters: TaskParameters) {
        val task = tasks.find { it.id == id }

        if (task != null) {
            runningTasks.add(task)
            tasks.remove(task)
            CoroutineScope(Dispatchers.IO).launch {
                task.run(taskParameters)
            }
            if (!isMonitoring) {
                CoroutineScope(Dispatchers.IO).launch {
                    monitorRunningTasks()
                }
            }
        } else {
            showMsg(globalClass.getString(R.string.task_not_found))
        }
    }

    fun continueTask(taskId: String) {
        val task = pausedTasks.find { it.id == taskId } ?: failedTasks.find { it.id == taskId }
        if (task != null) {
            runningTasks.add(task)
            if (pausedTasks.contains(task)) pausedTasks.remove(task)
            if (failedTasks.contains(task)) failedTasks.remove(task)
            CoroutineScope(Dispatchers.IO).launch {
                if (task.getCurrentStatus() == TaskStatus.PENDING) {
                    showMsg(globalClass.getString(R.string.unable_to_continue_task))
                } else {
                    task.continueTask()
                }
            }
            if (!isMonitoring) {
                CoroutineScope(Dispatchers.IO).launch {
                    monitorRunningTasks()
                }
            }
        } else {
            showMsg(globalClass.getString(R.string.task_not_found))
        }
    }

    private suspend fun monitorRunningTasks() {
        isMonitoring = true
        while (runningTasks.isNotEmpty()) {
            runningTasks.removeIf { task ->
                if (task.getCurrentStatus() == TaskStatus.CANCELLED) pausedTasks.add(task).also {
                    showMsg(globalClass.getString(R.string.task_paused))
                }
                if (task.getCurrentStatus() == TaskStatus.FAILED) failedTasks.add(task).also {
                    showMsg(globalClass.getString(R.string.task_failed))
                }
                if (task.getCurrentStatus() == TaskStatus.SUCCESS) {
                    showMsg(globalClass.getString(R.string.task_completed))
                }
                if (task.getCurrentStatus() == TaskStatus.CONFLICT) {
                    pausedTasks.add(task)
                }

                task.getCurrentStatus() == TaskStatus.FAILED
                        || task.getCurrentStatus() == TaskStatus.CANCELLED
                        || task.getCurrentStatus() == TaskStatus.SUCCESS
                        || task.getCurrentStatus() == TaskStatus.CONFLICT
            }

            if (runningTasks.isEmpty()) {
                runningTaskDialogInfo.hide()
            }

            // Simple implementation, will be changed when background tasks are implemented
            if (runningTasks.size == 1) {
                runningTasks[0].let { task ->
                    runningTaskDialogInfo.show(task)
                    runningTaskDialogInfo.updateInfo(task.progressMonitor)
                }
            } else {
                runningTaskDialogInfo.hide()
            }
            delay(100)
        }
        isMonitoring = false
        globalClass.mainActivityManager.resumeActiveTab()
        runningTaskDialogInfo.hide()
    }

    class TaskConflict {
        var hasConflict by mutableStateOf(false)
            private set
        var message by mutableStateOf(emptyString)
            private set
        var taskContentItem by mutableStateOf<TaskContentItem?>(null)
            private set
        var task by mutableStateOf<Task?>(null)
            private set

        fun interceptTask(
            taskContentItem: TaskContentItem,
            task: Task
        ) {
            this.hasConflict = true
            this.message = globalClass.resources.getString(
                R.string.file_already_exists,
                taskContentItem.content.displayName
            )
            this.taskContentItem = taskContentItem
            this.task = task
        }

        private fun reset() {
            hasConflict = false
            message = emptyString
            taskContentItem = null
            task = null
        }

        fun hide() {
            reset()
        }

        fun resolve(resolution: TaskContentStatus, applyToAllConflicts: Boolean = false) {
            if (taskContentItem != null) {
                taskContentItem!!.status = resolution
            }

            globalClass.taskManager.apply {
                if (applyToAllConflicts) {
                    globalClass.taskManager.overrideConflicts(task!!.id, resolution)
                }
                globalClass.taskManager.continueTask(task!!.id)
            }

            reset()
        }
    }

    class RunningTaskDialogInfo {
        var linkedTask by mutableStateOf<Task?>(null)

        var showDialog by mutableStateOf(false)
            private set
        var progressMonitor by mutableStateOf<TaskProgressMonitor?>(null)

        fun show(task: Task) {
            if (showDialog) {
                return
            }

            linkedTask = task
            showDialog = true
        }

        fun hide() {
            if (!showDialog) {
                return
            }

            showDialog = false
            reset()
        }

        private fun reset() {
            linkedTask = null
            progressMonitor = null
        }

        fun updateInfo(progressMonitor: TaskProgressMonitor) {
            this.progressMonitor = TaskProgressMonitor(
                status = progressMonitor.status,
                taskTitle = progressMonitor.taskTitle,
                processName = progressMonitor.processName,
                contentName = progressMonitor.contentName,
                totalContent = progressMonitor.totalContent,
                remainingContent = progressMonitor.remainingContent,
                progress = progressMonitor.progress,
                summary = progressMonitor.summary
            )
        }
    }
}