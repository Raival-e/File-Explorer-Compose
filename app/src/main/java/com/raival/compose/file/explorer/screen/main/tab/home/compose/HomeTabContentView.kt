package com.raival.compose.file.explorer.screen.main.tab.home.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.screen.main.compose.SimpleNewTabViewItem
import com.raival.compose.file.explorer.screen.main.compose.StorageDeviceView
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.coil.canUseCoil
import com.raival.compose.file.explorer.screen.main.tab.files.modal.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.MainTabContentView(tab: HomeTab) {
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())) {
        val mainActivityManager = globalClass.mainActivityManager
        val context = LocalContext.current

        LaunchedEffect(tab.id) {
            tab.fetchRecentFiles()
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            text = stringResource(R.string.recent_files),
            style = MaterialTheme.typography.titleMedium
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            item { Space(6.dp) }

            items(tab.recentFiles, key = { it.path }) {
                Column(
                    modifier = Modifier
                        .size(110.dp, 140.dp)
                        .padding(horizontal = 6.dp)
                        .background(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .combinedClickable(
                            onClick = {
                                it.documentHolder.openFile(context, false, false)
                            },
                            onLongClick = {
                                mainActivityManager.addTabAndSelect(
                                    FilesTab(it.documentHolder)
                                )
                            }
                        )
                ) {
                    if (canUseCoil(it.documentHolder)) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(2f),
                            model = it.documentHolder,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.Low
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.size(48.dp),
                                painter = painterResource(id = it.documentHolder.getFileIconResource()),
                                contentDescription = null
                            )
                        }
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        text = it.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2
                    )
                }
            }

            item { Space(6.dp) }
        }

        Space(12.dp)

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            text = stringResource(R.string.categories),
            style = MaterialTheme.typography.titleMedium
        )

        VerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            columns = SimpleGridCells.Fixed(3)
        ) {
            tab.getMainCategories().forEach {
                Column(
                    Modifier
                        .padding(4.dp)
                        .background(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { it.onClick() }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        modifier = Modifier.padding(8.dp),
                        imageVector = it.icon,
                        contentDescription = null
                    )
                    Text(text = it.name)
                }
            }
        }

        Space(12.dp)

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            text = stringResource(R.string.storage),
            style = MaterialTheme.typography.titleMedium
        )

        StorageProvider.getStorageDevices(globalClass).forEach {
            StorageDeviceView(storageDevice = it) {
                globalClass.mainActivityManager.addTabAndSelect(FilesTab(it.documentHolder))
                mainActivityManager.showNewTabDialog = false
            }
            HorizontalDivider()
        }

        HorizontalDivider()

        SimpleNewTabViewItem(
            title = stringResource(R.string.recycle_bin),
            imageVector = Icons.Rounded.DeleteSweep
        ) {
            globalClass.mainActivityManager.addTabAndSelect(FilesTab(globalClass.recycleBinDir))
            mainActivityManager.showNewTabDialog = false
        }

        HorizontalDivider()

        SimpleNewTabViewItem(
            title = stringResource(R.string.jump_to_path),
            imageVector = Icons.Rounded.ArrowOutward
        ) {
            mainActivityManager.showJumpToPathDialog = true
            mainActivityManager.showNewTabDialog = false
        }
    }
}