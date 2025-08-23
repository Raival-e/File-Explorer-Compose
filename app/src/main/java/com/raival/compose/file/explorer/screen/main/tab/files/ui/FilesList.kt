package com.raival.compose.file.explorer.screen.main.tab.files.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Isolate
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.coil.canUseCoil
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.videoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewConfigs
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewType
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListFontSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListIconSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListSpace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FilesList(tab: FilesTab) {
    val preferencesManager = globalClass.preferencesManager
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    Box(Modifier.weight(1f)) {
        if (tab.activeFolderContent.isEmpty() && !tab.isLoading) {
            EmptyFolderContent(tab)
        }

        if (preferencesManager.disablePullDownToRefresh) {
            FilesListContent(tab)
        } else {
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
                modifier = Modifier.fillMaxSize(),
            ) {
                FilesListContent(tab)
            }
        }

        LoadingOverlay(tab)
    }
}

@Composable
private fun EmptyFolderContent(tab: FilesTab) {
    val preferencesManager = globalClass.preferencesManager

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.4f),
            text = stringResource(
                when {
                    !tab.activeFolder.canRead -> R.string.cant_access_content
                    else -> R.string.empty
                }
            ),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        if (tab.activeFolder.canRead && !preferencesManager.showHiddenFiles) {
            Space(12.dp)
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.4f),
                text = stringResource(R.string.empty_without_hidden_files),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingOverlay(tab: FilesTab) {
    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = tab.isLoading
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(colorScheme.surface.copy(alpha = 0.4f))
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

@Composable
private fun FilesListContent(tab: FilesTab) {
    when (tab.viewConfig.viewType) {
        ViewType.LIST -> FilesListColumns(tab)
        ViewType.GRID -> FilesListGrid(tab)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesListColumns(tab: FilesTab) {
    val context = LocalContext.current
    val selectionHighlightColor = colorScheme.surfaceContainerHigh.copy(alpha = 1f)
    val highlightColor = colorScheme.primary.copy(alpha = 0.05f)

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        state = tab.activeListState,
        columns = GridCells.Fixed(tab.viewConfig.columnCount),
    ) {
        itemsIndexed(
            tab.activeFolderContent,
            key = { _, item -> item.uid }
        ) { index, item ->
            val currentItemPath = item.uniquePath
            val isAlreadySelected = tab.selectedFiles.containsKey(currentItemPath)
            var isSelectedItem by remember(isAlreadySelected) { mutableStateOf(isAlreadySelected) }
            ColumnFileItem(
                item = item,
                index = index,
                tab = tab,
                selectionHighlightColor = selectionHighlightColor,
                highlightColor = highlightColor,
                context = context,
                viewConfigs = tab.viewConfig,
                isSelectedItem = isSelectedItem,
                onSelection = { isSelectedItem = it }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesListGrid(tab: FilesTab) {
    val context = LocalContext.current
    val selectionHighlightColor = colorScheme.surfaceContainerHigh.copy(alpha = 1f)
    val highlightColor = colorScheme.primary.copy(alpha = 0.05f)

    LazyVerticalGrid(
        state = tab.activeListState,
        columns = GridCells.Fixed(tab.viewConfig.columnCount),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            tab.activeFolderContent,
            key = { _, item -> item.uid }
        ) { index, item ->
            val currentItemPath = item.uniquePath
            val isAlreadySelected = tab.selectedFiles.containsKey(currentItemPath)
            var isSelectedItem by remember(isAlreadySelected) { mutableStateOf(isAlreadySelected) }
            GridFileItem(
                itemPath = currentItemPath,
                isSelected = isSelectedItem,
                item = item,
                index = index,
                tab = tab,
                selectionHighlightColor = selectionHighlightColor,
                highlightColor = highlightColor,
                context = context,
                viewConfigs = tab.viewConfig,
                onSelection = { isSelectedItem = it }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColumnFileItem(
    item: ContentHolder,
    index: Int,
    tab: FilesTab,
    selectionHighlightColor: Color,
    highlightColor: Color,
    context: Context,
    currentItemPath: String = item.uniquePath,
    isSelectedItem: Boolean,
    onSelection: (Boolean) -> Unit,
    viewConfigs: ViewConfigs
) {
    fun toggleSelection() {
        if (tab.selectedFiles.containsKey(currentItemPath)) {
            tab.selectedFiles.remove(currentItemPath)
            tab.lastSelectedFileIndex = -1
            onSelection(false)
        } else {
            tab.selectedFiles[currentItemPath] = item
            tab.lastSelectedFileIndex = index
            onSelection(true)
        }
        tab.onSelectionChange()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                color = if (isSelectedItem) {
                    selectionHighlightColor
                } else if (tab.highlightedFiles.contains(currentItemPath)) {
                    highlightColor
                } else {
                    Color.Unspecified
                }
            )
            .combinedClickable(
                onClick = {
                    if (tab.selectedFiles.isNotEmpty()) {
                        toggleSelection()
                    } else {
                        if (item.isFile()) {
                            tab.openFile(context, item)
                        } else {
                            tab.openFolder(item, false)
                        }
                    }
                },
                onLongClick = {
                    handleLongClick(tab, currentItemPath, item, index)
                }
            )
    ) {
        Space(size = getFileListSpace(tab.activeFolder).dp)

        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileIcon(
                item = item,
                size = getFileListIconSize(tab.activeFolder).dp,
                viewConfigs = viewConfigs,
                onClick = { toggleSelection() },
                onLongClick = { handleLongClick(tab, currentItemPath, item, index) }
            )

            Space(size = 8.dp)

            Column(Modifier.weight(1f)) {
                val fontSize = getFileListFontSize(tab.activeFolder)

                Text(
                    text = item.displayName,
                    fontSize = fontSize.sp,
                    maxLines = 1,
                    lineHeight = (fontSize + 2).sp,
                    overflow = TextOverflow.Ellipsis,
                    color = if (tab.highlightedFiles.contains(currentItemPath)) {
                        colorScheme.primary
                    } else {
                        Color.Unspecified
                    }
                )

                FileDetails(
                    item = item,
                    currentItemPath = currentItemPath,
                    fontSize = fontSize,
                    isHighlighted = tab.highlightedFiles.contains(currentItemPath)
                )
            }
        }

        Space(size = getFileListSpace(tab.activeFolder).dp)

        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp),
            thickness = 0.5.dp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridFileItem(
    item: ContentHolder,
    index: Int,
    tab: FilesTab,
    selectionHighlightColor: Color,
    highlightColor: Color,
    context: Context,
    itemPath: String,
    isSelected: Boolean,
    onSelection: (Boolean) -> Unit,
    viewConfigs: ViewConfigs
) {
    fun toggleSelection() {
        if (tab.selectedFiles.containsKey(itemPath)) {
            tab.selectedFiles.remove(itemPath)
            tab.lastSelectedFileIndex = -1
            onSelection(false)
        } else {
            tab.selectedFiles[itemPath] = item
            tab.lastSelectedFileIndex = index
            onSelection(true)
        }
        tab.onSelectionChange()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (viewConfigs.galleryMode) Modifier.aspectRatio(1f) else Modifier)
            .combinedClickable(
                onClick = {
                    if (tab.selectedFiles.isNotEmpty()) {
                        toggleSelection()
                    } else {
                        if (item.isFile()) {
                            tab.openFile(context, item)
                        } else {
                            tab.openFolder(item, false)
                        }
                    }
                },
                onLongClick = {
                    handleLongClick(tab, itemPath, item, index)
                }
            )
            .background(
                color = if (isSelected) {
                    selectionHighlightColor
                } else if (tab.highlightedFiles.contains(itemPath)) {
                    highlightColor
                } else {
                    Color.Unspecified
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (viewConfigs.galleryMode) 1.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = if (viewConfigs.galleryMode) Modifier
                    .fillMaxWidth()
                    .weight(1f)
                else Modifier
            ) {
                FileIcon(
                    item = item,
                    size = (getFileListIconSize(tab.activeFolder) * 1.5).dp,
                    onClick = {
                        if (tab.selectedFiles.isNotEmpty()) {
                            toggleSelection()
                        } else {
                            if (item.isFile()) {
                                tab.openFile(context, item)
                            } else {
                                tab.openFolder(item, false)
                            }
                        }
                    },
                    viewConfigs = viewConfigs,
                    onLongClick = {
                        handleLongClick(tab, itemPath, item, index)
                    }
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            imageVector = Icons.Rounded.CheckCircle,
                            tint = colorScheme.primary,
                            contentDescription = null
                        )
                    }
                }
                if (viewConfigs.galleryMode && (item.isFolder || (item.isFile() && ((!videoFileType.contains(
                        item.extension
                    ) && !imageFileType.contains(item.extension)) || !viewConfigs.hideMediaNames)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                color = colorScheme.surface.copy(alpha = 0.6f)
                            )
                            .padding(4.dp)
                    ) {
                        val fontSize = getFileListFontSize(tab.activeFolder) * 0.8
                        Text(
                            text = item.displayName,
                            fontSize = fontSize.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 2,
                            lineHeight = (fontSize + 2).sp,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = if (tab.highlightedFiles.contains(itemPath)) {
                                colorScheme.primary
                            } else {
                                colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            if (!viewConfigs.galleryMode) {
                Spacer(modifier = Modifier.height(4.dp))
                val fontSize = getFileListFontSize(tab.activeFolder) * 0.8
                Text(
                    text = item.displayName,
                    fontSize = fontSize.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 2,
                    lineHeight = (fontSize + 2).sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = if (tab.highlightedFiles.contains(itemPath)) {
                        colorScheme.primary
                    } else {
                        colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FileIcon(
    item: ContentHolder,
    size: Dp,
    viewConfigs: ViewConfigs,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val sizeModifier = if (viewConfigs.viewType == ViewType.GRID && viewConfigs.galleryMode) {
        Modifier.fillMaxSize()
    } else {
        Modifier.size(size)
    }

    Isolate {
        Box(
            modifier = Modifier
                .then(sizeModifier)
                .clip(RoundedCornerShape(4.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .graphicsLayer { alpha = if (item.isHidden()) 0.4f else 1f },
        ) {
            var useCoil by remember(item.uid) {
                mutableStateOf(canUseCoil(item))
            }

            if (useCoil) {
                AsyncImage(
                    modifier = sizeModifier,
                    model = ImageRequest
                        .Builder(globalClass)
                        .data(item)
                        .build(),
                    filterQuality = FilterQuality.Low,
                    contentScale = if (viewConfigs.cropThumbnails) ContentScale.Crop else ContentScale.Fit,
                    contentDescription = null,
                    onError = { useCoil = false }
                )
            } else {
                FileContentIcon(item)
            }

            if (!item.canRead) {
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .alpha(if (item.isHidden()) 0.4f else 1f),
                    imageVector = Icons.Rounded.Lock,
                    tint = Color.Red,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun FileDetails(
    item: ContentHolder,
    currentItemPath: String,
    fontSize: Int,
    isHighlighted: Boolean
) {
    var details by remember(
        key1 = currentItemPath,
        key2 = item.lastModified
    ) { mutableStateOf(emptyString) }

    LaunchedEffect(
        key1 = currentItemPath,
        key2 = item.lastModified
    ) {
        if (details.isEmpty()) {
            val det = withContext(IO) {
                item.getDetails()
            }
            details = det
        }
    }

    Text(
        modifier = Modifier.alpha(0.7f),
        text = details,
        fontSize = (fontSize - 4).sp,
        maxLines = 1,
        lineHeight = (fontSize + 2).sp,
        overflow = TextOverflow.Ellipsis,
        color = if (isHighlighted) {
            colorScheme.primary
        } else {
            Color.Unspecified
        }
    )
}

@Composable
private fun FileDetailsCompact(
    item: ContentHolder,
    currentItemPath: String,
    isHighlighted: Boolean
) {
    Isolate {
        var details by remember(
            key1 = currentItemPath,
            key2 = item.lastModified
        ) { mutableStateOf(emptyString) }

        LaunchedEffect(
            key1 = currentItemPath,
            key2 = item.lastModified
        ) {
            if (details.isEmpty()) {
                val det = item.getDetails()
                withContext(Dispatchers.Main) { details = det }
            }
        }

        Text(
            modifier = Modifier.alpha(0.6f),
            text = details,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isHighlighted) {
                colorScheme.primary
            } else {
                colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun handleLongClick(
    tab: FilesTab,
    currentItemPath: String,
    item: ContentHolder,
    index: Int
) {
    val preferencesManager = globalClass.preferencesManager
    val isFirstSelection = tab.selectedFiles.isEmpty()
    val isAlreadySelected = tab.selectedFiles.containsKey(currentItemPath)
    val isNewSelection = !isAlreadySelected

    tab.selectedFiles[currentItemPath] = item

    if ((isFirstSelection && preferencesManager.showFileOptionMenuOnLongClick)
        || !isNewSelection
    ) {
        tab.toggleFileOptionsMenu(item)
    }

    if (isNewSelection) {
        if (tab.lastSelectedFileIndex >= 0) {
            if (tab.lastSelectedFileIndex > index) {
                for (i in tab.lastSelectedFileIndex downTo index) {
                    tab.selectedFiles[tab.activeFolderContent[i].uniquePath] =
                        tab.activeFolderContent[i]
                }
            } else {
                for (i in tab.lastSelectedFileIndex..index) {
                    tab.selectedFiles[tab.activeFolderContent[i].uniquePath] =
                        tab.activeFolderContent[i]
                }
            }
        }
    }
    tab.lastSelectedFileIndex = index
    tab.quickReloadFiles()
}