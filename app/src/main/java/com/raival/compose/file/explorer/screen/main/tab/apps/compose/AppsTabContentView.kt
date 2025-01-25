package com.raival.compose.file.explorer.screen.main.tab.apps.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.BottomSheetDialog
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.compose.ItemRow
import com.raival.compose.file.explorer.screen.main.tab.files.compose.ItemRowIcon
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import java.io.File

@Composable
fun ColumnScope.AppsTabContentView(tab: AppsTab) {
    LaunchedEffect(tab.id) {
        if (tab.appsList.isEmpty()) {
            tab.fetchInstalledApps()
        }
    }

    LaunchedEffect(tab.selectedChoice) {
        tab.appsList.clear()

        when (tab.selectedChoice) {
            0 -> tab.appsList.addAll(tab.userApps)
            1 -> tab.appsList.addAll(tab.systemApps)
            2 -> {
                tab.appsList.addAll(tab.userApps)
                tab.appsList.addAll(tab.systemApps)
            }
        }
    }

    if (tab.previewAppDialog != null) {
        val selectedApp = tab.previewAppDialog!!
        val details = arrayListOf<Pair<String, String>>().apply {
            add(Pair(globalClass.getString(R.string.package_name), selectedApp.packageName))
            add(Pair(globalClass.getString(R.string.version_name), selectedApp.versionName))
            add(
                Pair(
                    globalClass.getString(R.string.version_code),
                    selectedApp.versionCode.toString()
                )
            )
            add(Pair(globalClass.getString(R.string.size), selectedApp.size.toFormattedSize()))
        }

        BottomSheetDialog(
            onDismissRequest = { tab.previewAppDialog = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        model = selectedApp.path,
                        filterQuality = FilterQuality.Low,
                        error = painterResource(id = R.drawable.apk_file_extension),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                }
                Space(size = 8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = selectedApp.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Space(size = 8.dp)

                LazyColumn {
                    items(details, key = { it.first }) {
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(text = "${it.first}: ")
                            Text(
                                modifier = Modifier.alpha(0.5f),
                                text = it.second,
                                maxLines = 1
                            )
                        }
                    }
                }

                Space(size = 8.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            globalClass.filesTabManager.filesTabTasks.add(
                                CopyTask(arrayListOf(DocumentHolder.fromFile(File(selectedApp.path))))
                            )
                            tab.previewAppDialog = null
                            globalClass.showMsg(R.string.new_task_has_been_added)
                        }
                    ) {
                        Text(text = stringResource(R.string.copy))
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn {
            items(tab.appsList, key = { it.packageName }) { app ->
                ItemRow(
                    title = app.name,
                    subtitle = app.packageName,
                    icon = {
                        ItemRowIcon(
                            icon = app.path,
                            placeholder = R.drawable.apk_file_extension
                        )
                    },
                    onItemClick = {
                        tab.previewAppDialog = app
                    }
                )
            }

            item {
                Space(70.dp)
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
        ) {
            SegmentedButton(
                selected = tab.selectedChoice == 0,
                onClick = {
                    tab.selectedChoice = 0
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                label = {
                    Text(text = stringResource(R.string.user_apps))
                }
            )

            SegmentedButton(
                selected = tab.selectedChoice == 1,
                onClick = {
                    tab.selectedChoice = 1
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                label = {
                    Text(text = stringResource(R.string.system_apps))
                }
            )

            SegmentedButton(
                selected = tab.selectedChoice == 2,
                onClick = {
                    tab.selectedChoice = 2
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                label = {
                    Text(text = stringResource(R.string.all))
                }
            )
        }

        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = tab.isLoading
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}