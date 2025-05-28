package com.raival.compose.file.explorer.screen.main.tab.files.ui

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
import androidx.compose.material.icons.rounded.Task
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
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
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
                items(emptyList<Int>(), key = { it }) { task ->
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
                                        if (!tab.canRunTasks()) {
                                            globalClass.showMsg(globalClass.getString(R.string.can_not_run_tasks))
                                            return@combinedClickable
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.padding(12.dp),
                                imageVector = Icons.Rounded.Task,
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "",
                                    fontSize = 14.sp
                                )
                                Text(
                                    modifier = Modifier.alpha(0.7f),
                                    text = "",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = true) {
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