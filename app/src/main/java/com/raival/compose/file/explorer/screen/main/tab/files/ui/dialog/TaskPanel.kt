package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kevinnzou.compose.swipebox.SwipeBox
import com.kevinnzou.compose.swipebox.SwipeDirection
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.service.ContentOperationService.Companion.removeBackgroundTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTaskParameters
import com.raival.compose.file.explorer.screen.main.tab.files.task.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun TaskPanel(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current

        val runningTasks = remember { mutableStateListOf<Task>() }
        val pausedTasks = remember { mutableStateListOf<Task>() }
        val failedTasks = remember { mutableStateListOf<Task>() }
        val pendingTasks = remember { mutableStateListOf<Task>() }
        val invalidTasks = remember { mutableStateListOf<Task>() }

        LaunchedEffect(Unit) {
            globalClass.taskManager.validateTasks()

            while (tab.dialogsState.value.showTasksPanel) {
                if (globalClass.taskManager.runningTasks != runningTasks) {
                    runningTasks.clear()
                    runningTasks.addAll(globalClass.taskManager.runningTasks)
                }
                if (globalClass.taskManager.pausedTasks != pausedTasks) {
                    pausedTasks.clear()
                    pausedTasks.addAll(globalClass.taskManager.pausedTasks)
                }
                if (globalClass.taskManager.failedTasks != failedTasks) {
                    failedTasks.clear()
                    failedTasks.addAll(globalClass.taskManager.failedTasks)
                }
                if (globalClass.taskManager.pendingTasks != pendingTasks) {
                    pendingTasks.clear()
                    pendingTasks.addAll(globalClass.taskManager.pendingTasks)
                }
                if (globalClass.taskManager.invalidTasks != invalidTasks) {
                    invalidTasks.clear()
                    invalidTasks.addAll(globalClass.taskManager.invalidTasks)
                }
                delay(250)
            }
        }

        BottomSheetDialog(
            onDismissRequest = onDismissRequest
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Space(8.dp)
                Text(
                    text = stringResource(R.string.tasks),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(
                !runningTasks.isEmpty() || !pausedTasks.isEmpty() ||
                        !failedTasks.isEmpty() || !pendingTasks.isEmpty() || !invalidTasks.isEmpty()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(300)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Running Tasks
                    if (runningTasks.isNotEmpty()) {
                        item {
                            TaskCategoryHeader(
                                title = stringResource(R.string.tasks_running),
                                count = runningTasks.size,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(runningTasks, key = { "running-${it.id}" }) { task ->
                            RunningTaskItem(
                                task = task,
                                onClick = {
                                    tab.toggleTasksPanel(false)
                                    globalClass.taskManager.bringToForeground(task)
                                }
                            )
                        }
                        item { Space(8.dp) }
                    }

                    // Paused Tasks
                    if (pausedTasks.isNotEmpty()) {
                        item {
                            TaskCategoryHeader(
                                title = stringResource(R.string.tasks_paused),
                                count = pausedTasks.size,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        items(pausedTasks, key = { "paused-${it.id}" }) { task ->
                            SwipeableTaskItem(
                                task = task,
                                icon = Icons.Default.Pause,
                                iconTint = MaterialTheme.colorScheme.tertiary,
                                onSwipeToDelete = {
                                    pausedTasks.remove(task)
                                    globalClass.taskManager.removeTask(task.id)
                                    removeBackgroundTask(context, task.id)
                                },
                                onClick = {
                                    globalClass.taskManager.continueTask(task.id)
                                    tab.toggleTasksPanel(false)
                                }
                            )
                        }
                        item { Space(8.dp) }
                    }

                    // Failed Tasks
                    if (failedTasks.isNotEmpty()) {
                        item {
                            TaskCategoryHeader(
                                title = stringResource(R.string.tasks_failed),
                                count = failedTasks.size,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        items(failedTasks, key = { "failed-${it.id}" }) { task ->
                            SwipeableTaskItem(
                                task = task,
                                icon = Icons.Default.ErrorOutline,
                                iconTint = MaterialTheme.colorScheme.error,
                                onSwipeToDelete = {
                                    failedTasks.remove(task)
                                    globalClass.taskManager.removeTask(task.id)
                                    removeBackgroundTask(context, task.id)
                                },
                                onClick = {
                                    globalClass.taskManager.continueTask(task.id)
                                    tab.toggleTasksPanel(false)
                                }
                            )
                        }
                        item { Space(8.dp) }
                    }

                    // Pending Tasks
                    if (pendingTasks.isNotEmpty()) {
                        item {
                            TaskCategoryHeader(
                                title = stringResource(R.string.tasks_pending),
                                count = pendingTasks.size,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        items(pendingTasks, key = { "pending-${it.id}" }) { task ->
                            SwipeableTaskItem(
                                task = task,
                                icon = Icons.Default.HourglassEmpty,
                                iconTint = MaterialTheme.colorScheme.secondary,
                                onSwipeToDelete = {
                                    pendingTasks.remove(task)
                                    globalClass.taskManager.removeTask(task.id)
                                },
                                onClick = {
                                    if (tab.activeFolder is VirtualFileHolder || tab !is FilesTab) {
                                        globalClass.showMsg(globalClass.getString(R.string.can_not_run_tasks))
                                        return@SwipeableTaskItem
                                    }
                                    tab.toggleTasksPanel(false)
                                    when (task) {
                                        is CopyTask -> {
                                            globalClass.taskManager.runTask(
                                                task.id,
                                                CopyTaskParameters(tab.activeFolder)
                                            )
                                        }

                                        is CompressTask -> tab.toggleCompressTaskDialog(task)
                                    }
                                }
                            )
                        }
                        item { Space(8.dp) }
                    }

                    // Invalid Tasks
                    if (invalidTasks.isNotEmpty()) {
                        item {
                            TaskCategoryHeader(
                                title = stringResource(R.string.tasks_invalid),
                                count = invalidTasks.size,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        items(invalidTasks, key = { "invalid-${it.id}" }) { task ->
                            SwipeableTaskItem(
                                task = task,
                                icon = Icons.Default.Warning,
                                iconTint = MaterialTheme.colorScheme.outline,
                                onSwipeToDelete = {
                                    invalidTasks.remove(task)
                                    globalClass.taskManager.removeTask(task.id)
                                },
                                onClick = { }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = runningTasks.isEmpty() && pausedTasks.isEmpty() &&
                        failedTasks.isEmpty() && pendingTasks.isEmpty() && invalidTasks.isEmpty()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Task,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Space(16.dp)
                    Text(
                        text = stringResource(R.string.empty),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Space(8.dp)
                    Text(
                        text = stringResource(R.string.no_tasks_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCategoryHeader(
    title: String,
    count: Int,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = RoundedCornerShape(50))
            )
            Space(12.dp)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RunningTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    val job = remember { SupervisorJob() }
    val scope = remember { CoroutineScope(Dispatchers.IO + job) }

    var progress by remember(task.id) { mutableFloatStateOf(task.progressMonitor.progress) }
    var processName by remember(task.id) { mutableStateOf(task.progressMonitor.processName) }
    var contentName by remember(task.id) { mutableStateOf(task.progressMonitor.contentName) }
    var totalContent by remember(task.id) { mutableIntStateOf(task.progressMonitor.totalContent) }
    var remainingContent by remember(task.id) { mutableIntStateOf(task.progressMonitor.remainingContent) }

    DisposableEffect(task.id) {
        scope.launch {
            while (true) {
                progress = task.progressMonitor.progress
                processName = task.progressMonitor.processName
                contentName = task.progressMonitor.contentName
                totalContent = task.progressMonitor.totalContent
                remainingContent = task.progressMonitor.remainingContent
                delay(100)
            }
        }

        onDispose {
            job.cancel()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Space(16.dp)
            Column(modifier = Modifier.weight(1f)) {
                if (processName.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = processName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (contentName.isNotEmpty()) {
                            Text(
                                text = " - $contentName",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                if (progress > 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )

                        // Progress Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Content Counter
                            if (totalContent > 0) {
                                Text(
                                    text = "${
                                        max(
                                            totalContent - remainingContent,
                                            0
                                        )
                                    }/${totalContent}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Percentage
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    // Indeterminate progress
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )

                        if (totalContent > 0) {
                            Text(
                                text = "${totalContent - remainingContent}/${totalContent}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun SwipeableTaskItem(
    task: Task,
    icon: ImageVector,
    iconTint: Color,
    onSwipeToDelete: () -> Unit,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    SwipeBox(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        swipeDirection = SwipeDirection.EndToStart,
        endContentWidth = 72.dp,
        endContent = { swipeableState, _ ->
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .clickable {
                        onSwipeToDelete()
                        coroutineScope.launch { swipeableState.animateTo(0) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { _, _, _ ->
        TaskItem(
            task = task,
            icon = icon,
            iconTint = iconTint,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskItem(
    task: Task,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    expanded = !expanded
                }
            )
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconTint.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Space(16.dp)

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.metadata.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                )
                if (task.metadata.subtitle.isNotBlank()) {
                    Space(4.dp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = task.metadata.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = task.metadata.creationTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                AnimatedVisibility(
                    visible = expanded,
                    enter = scaleIn() + slideInVertically { -it },
                    exit = scaleOut(targetScale = 0.5f) + slideOutVertically { -it / 4 } + fadeOut()
                ) {
                    Text(
                        text = task.metadata.displayDetails,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 3
                    )
                }
            }
        }
    }
}