package com.raival.compose.file.explorer.screen.main.tab.apps.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.common.toFormattedSize
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.apps.holder.AppHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInfoBottomSheet(
    app: AppHolder,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    BottomSheetDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // App Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    model = app.path,
                    filterQuality = FilterQuality.Low,
                    error = painterResource(id = R.drawable.apk_file_placeholder),
                    contentScale = ContentScale.Fit,
                    contentDescription = null
                )
                Space(size = 12.dp)
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Space(size = 4.dp)
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Space(size = 8.dp)
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("v${app.versionName}") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    if (app.debuggable) {
                        AssistChip(
                            onClick = { },
                            label = { Text(stringResource(R.string.debug)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    VerticalDivider(Modifier.height(20.dp))
                    AssistChip(
                        onClick = {
                            globalClass.taskManager.addTask(
                                task = CopyTask(
                                    sourceFiles = arrayListOf(
                                        LocalFileHolder(
                                            file = File(app.path)
                                        )
                                    ),
                                    false
                                )
                            )
                        },
                        label = { Text(stringResource(R.string.extract)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Space(size = 16.dp)

            // Tab Buttons
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.details)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.advance)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.permissions)) }
                )
            }

            Space(size = 16.dp)

            // Tab Content
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InfoCard(
                                title = stringResource(R.string.version),
                                value = "${app.versionName} (${app.versionCode})",
                                icon = Icons.Rounded.Info
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.size),
                                value = app.size.toFormattedSize(),
                                icon = Icons.Rounded.Memory
                            )
                        }
                        if (app.category.isNotEmpty()) {
                            item {
                                InfoCard(
                                    title = stringResource(R.string.category),
                                    value = app.category,
                                    icon = Icons.Rounded.Category
                                )
                            }
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.install_date),
                                value = app.installDate.time.toFormattedDate(),
                                icon = Icons.Rounded.CalendarMonth
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.last_update),
                                value = app.lastUpdateDate.time.toFormattedDate(),
                                icon = Icons.Rounded.Update
                            )
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InfoCard(
                                title = stringResource(R.string.target_sdk),
                                value = app.targetSdkVersion.toString(),
                                icon = Icons.Rounded.Android
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.minimum_sdk),
                                value = if (app.minSdkVersion > 0) app.minSdkVersion.toString() else stringResource(
                                    R.string.unknown
                                ),
                                icon = Icons.Rounded.Code
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.uid),
                                value = app.uid.toString(),
                                icon = Icons.Rounded.Security
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.data_directory),
                                value = app.dataDir,
                                icon = Icons.Rounded.Folder
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.apk_path),
                                value = app.path,
                                icon = Icons.Rounded.DataUsage
                            )
                        }
                        item {
                            InfoCard(
                                title = stringResource(R.string.debug_mode),
                                value = if (app.debuggable) stringResource(R.string.enabled) else stringResource(
                                    R.string.disabled
                                ),
                                icon = Icons.Rounded.BugReport
                            )
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.permissions),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(app.permissions) { permission ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                                )
                            ) {
                                Text(
                                    text = permission.removePrefix("android.permission."),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Space(size = 16.dp)

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Space(size = 12.dp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}