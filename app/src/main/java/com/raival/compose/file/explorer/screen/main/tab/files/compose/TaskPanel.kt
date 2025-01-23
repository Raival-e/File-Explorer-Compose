package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kevinnzou.compose.swipebox.SwipeBox
import com.kevinnzou.compose.swipebox.SwipeDirection
import com.kevinnzou.compose.swipebox.widget.SwipeIcon
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun TaskPanel(tab: FilesTab) {
    if (tab.showTasksPanel) {
        BottomSheetDialog(
            onDismissRequest = { tab.showTasksPanel = false }
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.tasks),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            val coroutineScope = rememberCoroutineScope()
            LazyColumn(Modifier.animateContentSize()) {
                items(globalClass.filesTabManager.filesTabTasks, key = { it.id }) { task ->
                    SwipeBox(
                        modifier = Modifier.fillMaxWidth(),
                        swipeDirection = SwipeDirection.EndToStart,
                        endContentWidth = 60.dp,
                        endContent = { swipeableState, endSwipeProgress ->
                            SwipeIcon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.White,
                                background = Color(0xFFFA1E32),
                                weight = 1f,
                                iconSize = 20.dp
                            ) {
                                globalClass.filesTabManager.filesTabTasks.removeIf { it.id == task.id }
                                coroutineScope.launch {
                                    swipeableState.animateTo(0)
                                }
                            }
                        }
                    ) { _, _, _ ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (!task.isValidSourceFiles()) {
                                            globalClass.showMsg(R.string.invalid_task)
                                            return@combinedClickable
                                        }

                                        var copyToExistingZipFile = false

                                        if (task is CompressTask) {
                                            if (tab.selectedFiles.size == 1) {
                                                val selectedFile = tab.selectedFiles.values.first()
                                                if (selectedFile.isArchive()) {
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        task.execute(
                                                            selectedFile,
                                                            tab.taskCallback
                                                        )
                                                    }
                                                    copyToExistingZipFile = true
                                                }
                                            }
                                            if (!copyToExistingZipFile) {
                                                tab.compressDialog.show(task)
                                            }
                                        } else {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                task.execute(
                                                    tab.activeFolder,
                                                    tab.taskCallback
                                                )
                                            }
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.padding(12.dp),
                                imageVector = task.getIcon(),
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = task.getTitle(),
                                    fontSize = 14.sp
                                )
                                Text(
                                    modifier = Modifier.alpha(0.7f),
                                    text = task.getSubtitle(),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = globalClass.filesTabManager.filesTabTasks.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 60.dp)
                        .fillMaxWidth()
                        .alpha(0.4f),
                    text = stringResource(R.string.empty),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}