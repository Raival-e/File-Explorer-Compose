package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.common.toFormattedSize
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ApkInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkPreviewDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current
        val apkFile = tab.targetFile!!
        val packageManager = globalClass.packageManager

        var apkInfo by remember { mutableStateOf<ApkInfo?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var showExtraDetails by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            try {
                val archiveInfo = packageManager.getPackageArchiveInfo(
                    apkFile.uniquePath,
                    PackageManager.GET_PERMISSIONS
                )

                if (archiveInfo != null) {
                    archiveInfo.applicationInfo?.sourceDir = apkFile.uniquePath
                    archiveInfo.applicationInfo?.publicSourceDir = apkFile.uniquePath

                    val icon = archiveInfo.applicationInfo?.loadIcon(packageManager)
                    val appName = archiveInfo.applicationInfo?.loadLabel(packageManager).toString()

                    // Check if app is installed
                    var isInstalled = false
                    var installedVersion: String? = null
                    var installTime: String? = null
                    var lastUpdateTime: String? = null

                    try {
                        val installedInfo =
                            packageManager.getPackageInfo(archiveInfo.packageName, 0)
                        isInstalled = true
                        installedVersion = installedInfo.versionName
                        installTime = installedInfo.firstInstallTime.toFormattedDate()
                        lastUpdateTime = installedInfo.lastUpdateTime.toFormattedDate()
                    } catch (_: PackageManager.NameNotFoundException) {
                        // App not installed
                    }

                    val permissions = archiveInfo.requestedPermissions?.toList() ?: emptyList()

                    apkInfo = ApkInfo(
                        appName = appName,
                        packageName = archiveInfo.packageName,
                        versionName = archiveInfo.versionName
                            ?: globalClass.getString(R.string.unknown),
                        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            archiveInfo.longVersionCode.toString()
                        } else {
                            archiveInfo.versionCode.toString()
                        },
                        icon = icon,
                        size = apkFile.size.toFormattedSize(),
                        isInstalled = isInstalled,
                        installedVersion = installedVersion,
                        minSdkVersion = archiveInfo.applicationInfo?.minSdkVersion,
                        targetSdkVersion = archiveInfo.applicationInfo?.targetSdkVersion,
                        permissions = permissions,
                        installTime = installTime,
                        lastUpdateTime = lastUpdateTime
                    )
                }
            } catch (e: Exception) {
                logger.logError(e)
            } finally {
                isLoading = false
            }
        }

        BottomSheetDialog(onDismissRequest = onDismissRequest) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (apkInfo == null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Space(12.dp)
                        Text(
                            text = stringResource(R.string.invalid_apk_file),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Space(12.dp)
                        OutlinedButton(
                            onClick = {
                                apkFile.open(
                                    context = context,
                                    anonymous = false,
                                    skipSupportedExtensions = false,
                                    customMimeType = "application/zip"
                                )
                                onDismissRequest()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Space(size = 8.dp)
                            Text(stringResource(R.string.explore))
                        }
                    }
                } else {
                    apkInfo?.let { info ->
                        // App Header
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                model = info.icon,
                                filterQuality = FilterQuality.Low,
                                error = painterResource(id = R.drawable.apk_file_placeholder),
                                contentScale = ContentScale.Fit,
                                contentDescription = null
                            )

                            Space(size = 12.dp)

                            Text(
                                text = info.appName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Space(size = 4.dp)

                            Text(
                                text = info.packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                    label = { Text("v${info.versionName}") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )

                                if (info.isInstalled) {
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(stringResource(R.string.installed)) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CheckCircle,
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
                            }
                        }

                        Space(size = 16.dp)

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    apkFile.open(
                                        context = context,
                                        anonymous = false,
                                        skipSupportedExtensions = false,
                                        customMimeType = "application/zip"
                                    )
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Space(size = 8.dp)
                                Text(stringResource(R.string.explore))
                            }

                            Button(
                                onClick = {
                                    apkFile.open(
                                        context,
                                        anonymous = false,
                                        skipSupportedExtensions = true,
                                        customMimeType = "application/vnd.android.package-archive"
                                    )
                                    onDismissRequest()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (info.isInstalled) Icons.Default.Update else Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Space(size = 8.dp)
                                Text(
                                    if (info.isInstalled) stringResource(R.string.update) else stringResource(
                                        R.string.install
                                    )
                                )
                            }
                        }

                        if (!showExtraDetails) {
                            OutlinedButton(
                                onClick = { showExtraDetails = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.properties))
                            }
                        }

                        if (showExtraDetails) {
                            Space(size = 12.dp)
                            // Tab Row
                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                indicator = { tabPositions ->
                                    if (selectedTab < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            ) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text(stringResource(R.string.details)) }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text(stringResource(R.string.permissions)) }
                                )
                            }

                            Space(size = 16.dp)

                            // Tab Content
                            when (selectedTab) {
                                0 -> DetailsTab(info)
                                1 -> PermissionsTab(info.permissions)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DetailsTab(info: ApkInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            DetailItem(
                icon = Icons.Default.Badge,
                label = stringResource(R.string.version_code),
                value = info.versionCode
            )
        }

        item {
            DetailItem(
                icon = Icons.Default.Storage,
                label = stringResource(R.string.size),
                value = info.size
            )
        }

        if (info.minSdkVersion != null) {
            item {
                DetailItem(
                    icon = Icons.Default.Android,
                    label = stringResource(R.string.minimum_sdk),
                    value = "API ${info.minSdkVersion}"
                )
            }
        }

        if (info.targetSdkVersion != null) {
            item {
                DetailItem(
                    icon = Icons.Default.GpsFixed,
                    label = stringResource(R.string.target_sdk),
                    value = "API ${info.targetSdkVersion}"
                )
            }
        }

        if (info.isInstalled) {
            item {
                DetailItem(
                    icon = Icons.Default.Update,
                    label = stringResource(R.string.installed_version),
                    value = info.installedVersion ?: globalClass.getString(R.string.unknown)
                )
            }

            info.installTime?.let { time ->
                item {
                    DetailItem(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.install_date),
                        value = time
                    )
                }
            }

            info.lastUpdateTime?.let { time ->
                item {
                    DetailItem(
                        icon = Icons.Default.History,
                        label = stringResource(R.string.last_update),
                        value = time
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.PermissionsTab(permissions: List<String>) {
    if (permissions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_permissions_requested),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(permissions) { permission ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Space(size = 12.dp)
                        Text(
                            text = permission.removePrefix("android.permission."),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Space(size = 16.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}