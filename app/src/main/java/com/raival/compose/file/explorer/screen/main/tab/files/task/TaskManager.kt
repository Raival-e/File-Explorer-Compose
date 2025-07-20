package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.screen.main.tab.files.service.ContentOperationService.Companion.startNewBackgroundTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class TaskManager {
    private val taskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val taskMutex = Mutex()

    // Use thread-safe collections
    private val allTasks = ConcurrentHashMap<String, Task>()

    val pendingTasks = mutableListOf<Task>()
    val runningTasks = mutableListOf<Task>()
    val pausedTasks = mutableListOf<Task>()
    val failedTasks = mutableListOf<Task>()
    val invalidTasks = mutableListOf<Task>()
    val completedTasks = mutableListOf<Task>()

    private var isMonitoring = false

    @Volatile
    var runningTaskDialogInfo = RunningTaskDialogInfo()
    val taskInterceptor = TaskConflict()

    suspend fun addTask(task: Task, notifyUser: Boolean = true) = taskMutex.withLock {
        if (task.validate()) {
            allTasks[task.id] = task
            pendingTasks.add(task)
            if (notifyUser)
                globalClass.showMsg(globalClass.resources.getString(R.string.new_task_has_been_added))
        } else {
            invalidTasks.add(task)
            globalClass.showMsg(globalClass.getString(R.string.task_validation_failed))
        }
    }

    suspend fun addTaskAndRun(task: Task, parameters: TaskParameters) {
        addTask(task, false)
        if (allTasks.containsKey(task.id)) {
            runTask(task.id, parameters)
        }
    }

    suspend fun removeTask(id: String) = taskMutex.withLock {
        allTasks[id]?.abortTask()
        allTasks.remove(id)

        pendingTasks.removeIf { it.id == id }
        runningTasks.removeIf { it.id == id }
        pausedTasks.removeIf { it.id == id }
        failedTasks.removeIf { it.id == id }
        invalidTasks.removeIf { it.id == id }
        completedTasks.removeIf { it.id == id }
    }

    suspend fun validateTasks() = taskMutex.withLock {
        val iterator = pendingTasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            if (!task.validate()) {
                invalidTasks.add(task)
                // Use the iterator's remove method, which is safe
                iterator.remove()
            }
        }
    }

    suspend fun runTask(id: String, taskParameters: TaskParameters) = taskMutex.withLock {
        val task = allTasks[id]
        if (task == null) {
            showMsg(globalClass.getString(R.string.task_not_found))
            return@withLock
        }

        if (task.getCurrentStatus() != TaskStatus.PENDING) {
            showMsg(globalClass.getString(R.string.task_is_not_in_pending_state))
            return@withLock
        }

        moveTaskToRunning(task)
        task.setParameters(taskParameters)
        bringToForeground(task)
        startNewBackgroundTask(globalClass, task.id)
    }

    suspend fun continueTask(taskId: String) = taskMutex.withLock {
        val task = allTasks[taskId]
        if (task == null) {
            showMsg(globalClass.getString(R.string.task_not_found))
            return@withLock
        }

        val currentStatus = task.getCurrentStatus()

        if (currentStatus != TaskStatus.CONFLICT
            && currentStatus != TaskStatus.FAILED
            && currentStatus != TaskStatus.PAUSED
        ) {
            showMsg(
                globalClass.getString(
                    R.string.task_cannot_be_continued_from_current_state,
                    currentStatus
                )
            )
            return@withLock
        }

        // Move task back to running
        pausedTasks.removeIf { it.id == taskId }
        failedTasks.removeIf { it.id == taskId }

        if (!runningTasks.any { it.id == taskId }) {
            runningTasks.add(task)
        }

        bringToForeground(task)
        startNewBackgroundTask(globalClass, task.id)
    }

    private fun moveTaskToRunning(task: Task) {
        pendingTasks.removeIf { it.id == task.id }
        pausedTasks.removeIf { it.id == task.id }
        failedTasks.removeIf { it.id == task.id }

        if (!runningTasks.any { it.id == task.id }) {
            runningTasks.add(task)
        }
    }

    suspend fun handleTaskStatusChange(task: Task, newStatus: TaskStatus) = taskMutex.withLock {
        when (newStatus) {
            TaskStatus.SUCCESS -> {
                runningTasks.removeIf { it.id == task.id }
                completedTasks.add(task)
                showMsg(globalClass.getString(R.string.task_completed))
            }

            TaskStatus.FAILED -> {
                runningTasks.removeIf { it.id == task.id }
                failedTasks.add(task)
                showMsg(globalClass.getString(R.string.task_failed))
            }

            TaskStatus.PAUSED -> {
                runningTasks.removeIf { it.id == task.id }
                pausedTasks.add(task)
                showMsg(globalClass.getString(R.string.task_paused))
            }

            TaskStatus.CONFLICT -> {
                runningTasks.removeIf { it.id == task.id }
                pausedTasks.add(task)
                // Conflict will be handled by the UI
            }

            else -> {
                // No action needed for other states
            }
        }
    }

    fun getTask(id: String): Task? = allTasks[id]

    fun overrideConflicts(taskId: String, resolution: TaskContentStatus) {
        allTasks[taskId]?.overrideConflicts(resolution)
    }

    fun bringToForeground(task: Task) {
        if (!isMonitoring) {
            taskScope.launch {
                monitorRunningTask(task)
            }
        }
    }

    private suspend fun monitorRunningTask(task: Task) {
        isMonitoring = true
        runningTaskDialogInfo.show(task)
        try {
            while (runningTaskDialogInfo.showDialog) {
                if (!runningTasks.contains(task)) {
                    break
                }

                runningTaskDialogInfo.updateInfo(task.progressMonitor)
                delay(100)
            }
        } finally {
            isMonitoring = false
            runningTaskDialogInfo.hide()
        }
    }

    fun hideRunningTaskDialog() {
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

        fun interceptTask(taskContentItem: TaskContentItem, task: Task) {
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
            task = null
            message = emptyString
            taskContentItem = null
        }

        fun hide() {
            reset()
        }

        fun resolve(resolution: TaskContentStatus, applyToAllConflicts: Boolean = false) {
            val currentTask = task
            val currentItem = taskContentItem

            if (currentTask == null || currentItem == null) return

            currentItem.status = resolution

            if (applyToAllConflicts) {
                globalClass.taskManager.overrideConflicts(currentTask.id, resolution)
            }

            // Reset before continuing to avoid race conditions
            hide()

            // Continue the task
            CoroutineScope(Dispatchers.IO).launch {
                globalClass.taskManager.continueTask(currentTask.id)
            }
        }
    }

    class RunningTaskDialogInfo {
        var linkedTask by mutableStateOf<Task?>(null)
        var showDialog by mutableStateOf(false)
            private set
        var progressMonitor by mutableStateOf<TaskProgressMonitor?>(null)

        fun show(task: Task) {
            if (showDialog) return

            linkedTask = task
            showDialog = true
        }

        fun hide() {
            if (!showDialog) return

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