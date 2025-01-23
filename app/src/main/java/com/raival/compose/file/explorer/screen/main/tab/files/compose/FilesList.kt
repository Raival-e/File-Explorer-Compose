package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.Isolate
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.preferences.constant.RegularTabFileListSize
import com.raival.compose.file.explorer.screen.preferences.constant.RegularTabFileListSizeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FilesList(tab: FilesTab) {
    val context = LocalContext.current
    val documentHolderSelectionHighlightColor = colorScheme.surfaceContainerHigh.copy(alpha = 1f)
    val documentHolderHighlightColor = colorScheme.primary.copy(alpha = 0.05f)
    val preferencesManager = globalClass.preferencesManager
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    if (tab.activeFolderContent.isEmpty()) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    } else {
        val columnCount = preferencesManager.displayPrefs.fileListColumnCount
        Box(Modifier.weight(1f)) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    tab.openFolder(tab.activeFolder, true, true)
                    coroutineScope.launch {
                        delay(100)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalGrid(
                    columns = if (columnCount > 0)
                        GridCells.Fixed(columnCount) else GridCells.Adaptive(300.dp),
                    modifier = Modifier
                        .fillMaxSize(),
                    state = tab.activeListState
                ) {
                    itemsIndexed(
                        tab.activeFolderContent,
                        key = { index, item -> tab.getUID() }
                    ) { index, item ->
                        val currentItemPath = item.getPath()
                        val itemDetailsCoroutine = rememberCoroutineScope()
                        val isAlreadySelected = tab.selectedFiles.containsKey(currentItemPath)

                        fun toggleSelection() {
                            if (tab.selectedFiles.containsKey(currentItemPath)) {
                                tab.selectedFiles.remove(currentItemPath)
                                tab.lastSelectedFileIndex = -1
                            } else {
                                tab.selectedFiles[currentItemPath] = item
                                tab.lastSelectedFileIndex = index
                            }
                        }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isAlreadySelected) {
                                        documentHolderSelectionHighlightColor
                                    } else if (tab.highlightedFiles.contains(currentItemPath)) {
                                        documentHolderHighlightColor
                                    } else {
                                        Color.Unspecified
                                    }
                                )
                                .combinedClickable(
                                    onClick = {
                                        if (tab.selectedFiles.isNotEmpty()) {
                                            toggleSelection()
                                            tab.quickReloadFiles()
                                        } else {
                                            if (item.isFile()) {
                                                tab.openFile(context, item)
                                            } else {
                                                tab.openFolder(item, false)
                                            }
                                        }
                                    },
                                    onLongClick = {
                                        val isFirstSelection = tab.selectedFiles.isEmpty()
                                        val isNewSelection = !isAlreadySelected

                                        tab.selectedFiles[currentItemPath] = item

                                        if ((isFirstSelection && preferencesManager.generalPrefs.showFileOptionMenuOnLongClick) || !isNewSelection)
                                            tab.fileOptionsDialog.show(item)

                                        if (isNewSelection) {
                                            if (tab.lastSelectedFileIndex >= 0) {
                                                if (tab.lastSelectedFileIndex > index) {
                                                    for (i in tab.lastSelectedFileIndex downTo index) {
                                                        tab.selectedFiles[tab.activeFolderContent[i].getPath()] =
                                                            tab.activeFolderContent[i]
                                                    }
                                                } else {
                                                    for (i in tab.lastSelectedFileIndex..index) {
                                                        tab.selectedFiles[tab.activeFolderContent[i].getPath()] =
                                                            tab.activeFolderContent[i]
                                                    }
                                                }
                                            }
                                        }
                                        tab.lastSelectedFileIndex = index

                                        tab.quickReloadFiles()
                                    }
                                )
                        ) {
                            Space(
                                size = when (preferencesManager.displayPrefs.fileListSize) {
                                    RegularTabFileListSize.LARGE.ordinal, RegularTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                                    else -> 4.dp
                                }
                            )

                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Isolate {
                                    val iconSize =
                                        when (preferencesManager.displayPrefs.fileListSize) {
                                            RegularTabFileListSize.SMALL.ordinal -> RegularTabFileListSizeMap.IconSize.SMALL.dp
                                            RegularTabFileListSize.MEDIUM.ordinal -> RegularTabFileListSizeMap.IconSize.MEDIUM.dp
                                            RegularTabFileListSize.LARGE.ordinal -> RegularTabFileListSizeMap.IconSize.LARGE.dp
                                            else -> RegularTabFileListSizeMap.IconSize.EXTRA_LARGE.dp
                                        }

                                    if (item.isFile()) {
                                        AsyncImage(
                                            modifier = Modifier
                                                .size(iconSize)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    toggleSelection()
                                                    tab.quickReloadFiles()
                                                },
                                            model = ImageRequest.Builder(globalClass).data(item)
                                                .build(),
                                            filterQuality = FilterQuality.Low,
                                            error = painterResource(id = item.getFileIconResource()),
                                            contentScale = ContentScale.Fit,
                                            alpha = if (item.isHidden()) 0.4f else 1f,
                                            contentDescription = null
                                        )
                                    } else {
                                        Icon(
                                            modifier = Modifier
                                                .size(iconSize)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    toggleSelection()
                                                    tab.quickReloadFiles()
                                                }
                                                .alpha(if (item.isHidden()) 0.4f else 1f),
                                            imageVector = Icons.Rounded.Folder,
                                            contentDescription = null
                                        )
                                    }
                                }

                                Space(size = 8.dp)

                                Column(Modifier.weight(1f)) {
                                    val fontSize =
                                        when (preferencesManager.displayPrefs.fileListSize) {
                                            RegularTabFileListSize.SMALL.ordinal -> RegularTabFileListSizeMap.FontSize.SMALL
                                            RegularTabFileListSize.MEDIUM.ordinal -> RegularTabFileListSizeMap.FontSize.MEDIUM
                                            RegularTabFileListSize.LARGE.ordinal -> RegularTabFileListSizeMap.FontSize.LARGE
                                            else -> RegularTabFileListSizeMap.FontSize.EXTRA_LARGE
                                        }

                                    Text(
                                        text = item.getName(),
                                        fontSize = fontSize.sp,
                                        maxLines = 1,
                                        lineHeight = (fontSize + 2).sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Isolate {
                                        var details by remember(
                                            key1 = currentItemPath,
                                            key2 = item.getLastModified()
                                        ) { mutableStateOf(item.formattedDetailsCache) }

                                        LaunchedEffect(
                                            key1 = currentItemPath,
                                            key2 = item.getLastModified()
                                        ) {
                                            if (details.isEmpty()) {
                                                itemDetailsCoroutine.launch(Dispatchers.IO) {
                                                    val det = item.getFormattedDetails(
                                                        true,
                                                        preferencesManager.displayPrefs.showFolderContentCount
                                                    )
                                                    withContext(Dispatchers.Main) { details = det }
                                                }
                                            }
                                        }

                                        Text(
                                            modifier = Modifier.alpha(0.7f),
                                            text = details,
                                            fontSize = (fontSize - 4).sp,
                                            maxLines = 1,
                                            lineHeight = (fontSize + 2).sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Space(
                                size = when (preferencesManager.displayPrefs.fileListSize) {
                                    RegularTabFileListSize.LARGE.ordinal, RegularTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                                    else -> 4.dp
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = tab.isLoading
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}