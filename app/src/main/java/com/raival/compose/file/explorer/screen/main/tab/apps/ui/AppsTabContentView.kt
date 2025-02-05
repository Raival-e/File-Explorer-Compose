package com.raival.compose.file.explorer.screen.main.tab.apps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.common.ui.block
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.apps.holder.AppHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRow
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRowIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
                Space(150.dp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(!tab.isSearchPanelOpen) {
                FloatingActionButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        tab.isSearchPanelOpen = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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

            AnimatedVisibility(tab.isSearchPanelOpen) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .height(56.dp)
                        .block(
                            borderSize = 0.dp,
                            shape = CircleShape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AlertDialogDefaults.containerColor,
                            unfocusedContainerColor = AlertDialogDefaults.containerColor,
                            disabledContainerColor = AlertDialogDefaults.containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        value = tab.searchQuery,
                        onValueChange = {
                            tab.searchQuery = it
                        },
                        placeholder = {
                            Text(
                                modifier = Modifier.alpha(0.75f),
                                text = stringResource(R.string.search_query),
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = { tab.isSearchPanelOpen = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = tab.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (tab.isSearching) {
                                            tab.isSearching = false
                                        } else {
                                            tab.searchQuery = emptyString
                                            tab.isSearching = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (tab.isSearching) Icons.Rounded.Pause
                                        else Icons.Rounded.Cancel, contentDescription = null
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (tab.isSearching) {
                                        tab.isSearching = false
                                        delay(200)
                                    }

                                    tab.isSearching = true

                                    val list = arrayListOf<AppHolder>().apply {
                                        when (tab.selectedChoice) {
                                            0 -> addAll(tab.userApps)
                                            1 -> addAll(tab.systemApps)
                                            else -> addAll(tab.userApps + tab.systemApps)
                                        }
                                    }

                                    tab.appsList.clear()
                                    tab.appsList.addAll(list.filter {
                                        it.name.contains(tab.searchQuery, true)
                                    })

                                    tab.isSearching = false
                                }
                            }
                        )
                    )
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = tab.isLoading || tab.isSearching
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}