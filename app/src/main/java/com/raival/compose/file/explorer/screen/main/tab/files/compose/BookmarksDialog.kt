package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.trimToLastTwoSegments
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarksDialog(tab: FilesTab) {
    if (tab.showBookmarkDialog) {
        val context = LocalContext.current
        BottomSheetDialog(
            onDismissRequest = { tab.showBookmarkDialog = false }
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.bookmarks),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Space(size = 8.dp)
            LazyColumn(Modifier.animateContentSize()) {
                itemsIndexed(
                    globalClass.filesTabManager.bookmarks
                    .map { DocumentHolder.fromFullPath(it) }
                    .takeWhile { it != null } as ArrayList<DocumentHolder>,
                    key = { index, item -> item.path }
                ) { index, item ->
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
                                globalClass.filesTabManager.bookmarks -= item.path
                            }
                        }
                    ) { _, _, _ ->
                        Modifier
                            .fillMaxWidth()
                        Column(
                            Modifier
                                .animateItem()
                                .combinedClickable(
                                    onClick = {
                                        if (item.isFile) {
                                            tab.openFile(context, item)
                                        } else {
                                            tab.requestNewTab(FilesTab(item))
                                        }
                                        tab.showBookmarkDialog = false
                                    },
                                    onLongClick = { }
                                )
                        ) {
                            Space(size = 4.dp)
                            FileItemRow(
                                item = item,
                                fileDetails = item.path.trimToLastTwoSegments()
                            )
                            Space(size = 4.dp)
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(globalClass.filesTabManager.bookmarks.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 60.dp)
                        .fillMaxWidth()
                        .alpha(0.26f),
                    text = stringResource(R.string.empty),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}