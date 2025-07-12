package com.raival.compose.file.explorer.screen.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.copyToClipboard
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.logger.LogHolder
import com.raival.compose.file.explorer.common.ui.Space
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

enum class LogType(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    ALL("All", Icons.Filled.FilterList, Color.Gray),
    ERROR("Errors", Icons.Filled.Error, Color(0xFFD32F2F)),
    WARNING("Warnings", Icons.Filled.Warning, Color(0xFFF57C00)),
    INFO("Info", Icons.Filled.Info, Color(0xFF1976D2))
}

data class CombinedLog(
    val logHolder: LogHolder,
    val type: LogType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Collect logs from logger
    val errors by logger.getErrors().collectAsStateWithLifecycle(initialValue = emptyList())
    val warnings by logger.getWarnings().collectAsStateWithLifecycle(initialValue = emptyList())
    val infos by logger.getInfos().collectAsStateWithLifecycle(initialValue = emptyList())

    // UI State
    var selectedLogType by remember { mutableStateOf(LogType.ALL) }
    var showClearDialog by remember { mutableStateOf(false) }
    var isClearing by remember { mutableStateOf(false) }

    // Combine all logs with their types
    val combinedLogs = remember(errors, warnings, infos) {
        val allLogs = mutableListOf<CombinedLog>()
        errors.forEach { allLogs.add(CombinedLog(it, LogType.ERROR)) }
        warnings.forEach { allLogs.add(CombinedLog(it, LogType.WARNING)) }
        infos.forEach { allLogs.add(CombinedLog(it, LogType.INFO)) }

        // Sort by timestamp (newest first)
        allLogs.sortedByDescending { it.logHolder.timestamp }
    }

    // Filter logs based on selected type
    val filteredLogs = remember(combinedLogs, selectedLogType) {
        when (selectedLogType) {
            LogType.ALL -> combinedLogs
            LogType.ERROR -> combinedLogs.filter { it.type == LogType.ERROR }
            LogType.WARNING -> combinedLogs.filter { it.type == LogType.WARNING }
            LogType.INFO -> combinedLogs.filter { it.type == LogType.INFO }
        }
    }

    // Function to handle clearing logs
    suspend fun clearSelectedLogs() {
        isClearing = true
        try {
            runBlocking {
                when (selectedLogType) {
                    LogType.ALL -> logger.clearAllLogs()
                    LogType.ERROR -> logger.clearErrors()
                    LogType.WARNING -> logger.clearWarnings()
                    LogType.INFO -> logger.clearInfos()
                }
            }
        } finally {
            isClearing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_activity_logs),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showClearDialog = true },
                        enabled = combinedLogs.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.clear_logs)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LogType.entries.toTypedArray()) { logType ->
                    val count = when (logType) {
                        LogType.ALL -> combinedLogs.size
                        LogType.ERROR -> errors.size
                        LogType.WARNING -> warnings.size
                        LogType.INFO -> infos.size
                    }

                    FilterChip(
                        onClick = { selectedLogType = logType },
                        label = {
                            Text("${logType.displayName} ($count)")
                        },
                        selected = selectedLogType == logType,
                        leadingIcon = {
                            Icon(
                                imageVector = logType.icon,
                                contentDescription = null,
                                tint = if (selectedLogType == logType) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    logType.color
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = logType.color.copy(alpha = 0.12f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Logs list
                if (filteredLogs.isEmpty()) {
                    EmptyLogsState(selectedLogType = selectedLogType)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredLogs,
                            key = { it.logHolder.id }
                        ) { combinedLog ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = {
                                            combinedLog.logHolder.message.copyToClipboard()
                                            globalClass.showMsg(R.string.copied_to_clipboard)
                                        }
                                    )
                            ) {
                                LogItem(
                                    combinedLog = combinedLog,
                                    modifier = Modifier
                                        .animateItem()
                                )
                                Space(8.dp)
                                HorizontalDivider()
                            }
                        }
                    }
                }

                // Loading overlay
                if (isClearing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.padding(32.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = stringResource(R.string.clearing_logs),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_logs)) },
            text = {
                Text(stringResource(R.string.clear_all_logs_warning))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { clearSelectedLogs() }
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun LogItem(
    combinedLog: CombinedLog,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Log type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(combinedLog.type.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = combinedLog.type.icon,
                    contentDescription = combinedLog.type.displayName,
                    tint = combinedLog.type.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Log content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = combinedLog.type.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = combinedLog.type.color,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = combinedLog.logHolder.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = combinedLog.logHolder.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyLogsState(
    selectedLogType: LogType,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = selectedLogType.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (selectedLogType) {
                LogType.ALL -> stringResource(R.string.no_logs_available)
                LogType.ERROR -> stringResource(R.string.no_errors_logged)
                LogType.WARNING -> stringResource(R.string.no_warnings_logged)
                LogType.INFO -> stringResource(R.string.no_info_logs_available)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = when (selectedLogType) {
                LogType.ALL -> emptyString
                LogType.ERROR -> stringResource(R.string.great_no_errors_have_been_encountered)
                LogType.WARNING -> stringResource(R.string.no_warnings_have_been_logged)
                LogType.INFO -> stringResource(R.string.no_information_logs_have_been_recorded)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}