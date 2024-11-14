package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.block
import com.raival.compose.file.explorer.common.compose.detectVerticalSwipe
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab

@Composable
fun BottomOptionsBar(tab: RegularTab) {
    Row(
        Modifier
            .fillMaxWidth()
            .block(
                borderSize = 0.dp,
                shape = RoundedCornerShape(topStartPercent = 24, topEndPercent = 24)
            )
            .detectVerticalSwipe(
                onSwipeUp = {
                    tab.showBookmarkDialog = true
                }
            )
    ) {
        if (tab.showEmptyRecycleBin) {
            BottomOptionsBarButton(Icons.Rounded.DeleteSweep, stringResource(R.string.empty)) {
                tab.unselectAllFiles(false)
                tab.activeFolderContent.forEach {
                    tab.selectedFiles[it.getPath()] = it
                }
                tab.quickReloadFiles()
                tab.showConfirmDeleteDialog = true
            }
        } else {
            BottomOptionsBarButton(Icons.Rounded.AddTask, stringResource(R.string.task)) {
                tab.showTasksPanel = true
            }
        }

        BottomOptionsBarButton(Icons.Rounded.Search, stringResource(R.string.search)) {
            tab.showSearchPenal = true
        }

        if (!tab.showEmptyRecycleBin) {
            BottomOptionsBarButton(Icons.Rounded.Add, stringResource(R.string.create)) {
                tab.showCreateNewFileDialog = true
            }
        }

        if (tab.showMoreOptionsButton &&  tab.selectedFiles.isNotEmpty()) {
            BottomOptionsBarButton(Icons.Rounded.SelectAll, stringResource(R.string.select_all)) {
                if (tab.selectedFiles.size == tab.activeFolderContent.size) {
                    tab.unselectAllFiles()
                } else {
                    tab.unselectAllFiles(false)
                    tab.activeFolderContent.forEach {
                        tab.selectedFiles[it.getPath()] = it
                    }
                    tab.quickReloadFiles()
                }
            }

            BottomOptionsBarButton(Icons.Rounded.MoreVert, stringResource(R.string.options)) {
                tab.fileOptionsDialog.show(tab.selectedFiles[tab.selectedFiles.keys.first()]!!)
                tab.quickReloadFiles()
            }
        } else {
            BottomOptionsBarButton(Icons.AutoMirrored.Rounded.Sort, stringResource(R.string.sort), {
                if (tab.showSortingMenu) {
                    FileSortingMenu(
                        tab = tab,
                        reloadFiles = { tab.reloadFiles() }
                    ) { tab.showSortingMenu = false }
                }
            }) {
                tab.showSortingMenu = true
            }

            BottomOptionsBarButton(Icons.Rounded.Bookmark, stringResource(R.string.bookmarks)) {
                tab.showBookmarkDialog = true
            }
        }
    }
}