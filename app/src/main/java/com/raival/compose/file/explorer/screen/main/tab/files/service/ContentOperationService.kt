package com.raival.compose.file.explorer.screen.main.tab.files.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.MainActivity
import com.raival.compose.file.explorer.screen.main.tab.files.task.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ContentOperationService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Tasks ids and whether they are running
    private val runningTaskIds = ConcurrentHashMap<String, Boolean>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.task_running_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activeTasksCount = runningTaskIds.size
        val userActionRequired = runningTaskIds.values.any { !it }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(globalClass.getString(R.string.tasks))
            .setContentText(
                if (userActionRequired) getString(R.string.action_required)
                else globalClass.getString(R.string.task_running_notification_text)
            )
            .setSmallIcon(R.mipmap.app_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setNumber(activeTasksCount)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { intent ->
            when (intent.action) {
                ACTION_START_TASK -> {
                    val taskId = intent.getStringExtra(TASK_ID)
                    if (taskId != null) {
                        startTask(taskId)
                    }
                }

                ACTION_REMOVE_TASK -> {
                    val taskId = intent.getStringExtra(TASK_ID)
                    if (taskId != null) {
                        removeTask(taskId)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun removeTask(taskId: String) {
        if (runningTaskIds.containsKey(taskId)) {
            if (runningTaskIds[taskId] == true) {
                globalClass.showMsg(R.string.task_is_running)
            } else {
                runningTaskIds.remove(taskId)
                updateNotification()

                // Stop service if no more tasks
                if (runningTaskIds.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    private fun startTask(taskId: String) {
        if (runningTaskIds.containsKey(taskId) && runningTaskIds[taskId] == true) {
            return
        }

        val task = globalClass.taskManager.getTask(taskId)

        if (task == null) {
            return
        }

        runningTaskIds[taskId] = true
        updateNotification()

        serviceScope.launch {
            try {
                task.run()
                globalClass.taskManager.handleTaskStatusChange(task, task.getCurrentStatus())
            } catch (e: Exception) {
                logger.logError(e)
                globalClass.taskManager.handleTaskStatusChange(task, TaskStatus.FAILED)
            } finally {
                globalClass.mainActivityManager.resumeActiveTab()
                if (task.getCurrentStatus() == TaskStatus.SUCCESS) {
                    runningTaskIds.remove(taskId)
                } else {
                    // require action from user
                    runningTaskIds[taskId] = false
                }

                updateNotification()

                // Stop service if no more tasks
                if (runningTaskIds.isEmpty()) {
                    stopSelf()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)

        // Cancel all running tasks
        runningTaskIds.keys.forEach { taskId ->
            globalClass.taskManager.getTask(taskId)?.abortTask()
        }

        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TASK_ID = "taskId"
        private const val NOTIFICATION_ID = 1297
        private const val CHANNEL_ID = "task_background_service"
        const val ACTION_START_TASK = "ACTION_START_TASK"
        const val ACTION_REMOVE_TASK = "ACTION_REMOVE_TASK"

        fun startNewBackgroundTask(context: Context, taskId: String) {
            val intent = Intent(context, ContentOperationService::class.java).apply {
                action = ACTION_START_TASK
                putExtra(TASK_ID, taskId)
            }
            context.startService(intent)
        }

        fun removeBackgroundTask(context: Context, taskId: String) {
            val intent = Intent(context, ContentOperationService::class.java).apply {
                action = ACTION_REMOVE_TASK
                putExtra(TASK_ID, taskId)
            }
            context.startService(intent)
        }
    }
}