package com.raival.compose.file.explorer.screen.main.tab.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.google.gson.Gson
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.MainActivityManager
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.coil.canUseCoil
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDevice
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.BOOKMARKS
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.RECENT
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileContentIcon
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeLayout
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeSectionConfig
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeSectionType
import com.raival.compose.file.explorer.screen.main.tab.home.data.getDefaultHomeLayout
import com.raival.compose.file.explorer.screen.main.ui.SimpleNewTabViewItem
import com.raival.compose.file.explorer.screen.main.ui.StorageDeviceView
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.HomeTabContentView(tab: HomeTab) {
    val mainActivityManager = globalClass.mainActivityManager
    val scope = rememberCoroutineScope()
    val enabledSections = remember { mutableStateListOf<HomeSectionConfig>() }

    LaunchedEffect(tab.id) {
        tab.fetchRecentFiles()
        val config = try {
            Gson().fromJson(
                globalClass.preferencesManager.homeTabLayout,
                HomeLayout::class.java
            )
        } catch (e: Exception) {
            logger.logError(e)
            getDefaultHomeLayout()
        }.sections.filter { it.isEnabled }.sortedBy { it.order }

        enabledSections.addAll(config)
    }

    if (tab.showCustomizeHomeTabDialog) {
        HomeLayoutSettingsScreen { sections ->
            tab.showCustomizeHomeTabDialog = false
            var isAllDisabled = false

            // Prevent disabling all sections
            if (sections.all { !it.isEnabled }) {
                isAllDisabled = true
            }

            sections.forEachIndexed { index, config ->
                config.order = index
            }

            enabledSections.apply {
                clear()
                if (isAllDisabled) {
                    addAll(getDefaultHomeLayout(true).sections.filter { it.isEnabled }
                        .sortedBy { it.order })
                } else {
                    addAll(sections.filter { it.isEnabled }.sortedBy { it.order })
                }

            }

            scope.launch {
                if (isAllDisabled) {
                    globalClass.preferencesManager.homeTabLayout = Gson().toJson(
                        getDefaultHomeLayout(true)
                    )
                } else {
                    globalClass.preferencesManager.homeTabLayout = Gson().toJson(
                        HomeLayout(sections)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        enabledSections.forEach { section ->
            when (section.type) {
                HomeSectionType.RECENT_FILES -> {
                    RecentFilesSection(tab = tab, mainActivityManager = mainActivityManager)
                }

                HomeSectionType.CATEGORIES -> {
                    CategoriesSection(tab = tab)
                }

                HomeSectionType.STORAGE -> {
                    StorageSection(mainActivityManager = mainActivityManager)
                }

                HomeSectionType.BOOKMARKS -> {
                    BookmarksSection(mainActivityManager = mainActivityManager)
                }

                HomeSectionType.RECYCLE_BIN -> {
                    RecycleBinSection(mainActivityManager = mainActivityManager)
                }

                HomeSectionType.JUMP_TO_PATH -> {
                    JumpToPathSection(mainActivityManager = mainActivityManager)
                }
            }
        }
    }
}

@Composable
private fun RecentFilesSection(
    tab: HomeTab,
    mainActivityManager: MainActivityManager
) {
    val context = LocalContext.current

    // Recent files
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        text = stringResource(R.string.recent_files),
        style = MaterialTheme.typography.titleMedium
    )

    if (tab.recentFiles.isEmpty()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .clickable {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(RECENT))
                    )
                },
            text = stringResource(R.string.no_recent_files),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    } else {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            verticalAlignment = Alignment.CenterVertically
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
                                it.file.open(context, false, false, null)
                            },
                            onLongClick = {
                                mainActivityManager.replaceCurrentTabWith(
                                    FilesTab(it.file)
                                )
                            }
                        )
                ) {
                    var useCoil by remember(it.file.uid) {
                        mutableStateOf(canUseCoil(it.file))
                    }

                    Box(modifier = Modifier.weight(2f)) {
                        if (useCoil) {
                            AsyncImage(
                                modifier = Modifier.fillMaxSize(),
                                model = ImageRequest
                                    .Builder(globalClass)
                                    .data(it.file)
                                    .build(),
                                filterQuality = FilterQuality.Low,
                                contentScale = ContentScale.Crop,
                                contentDescription = null,
                                onError = { useCoil = false }
                            )
                        } else {
                            FileContentIcon(it.file)
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
            item {
                TextButton(
                    onClick = {
                        mainActivityManager.replaceCurrentTabWith(
                            FilesTab(VirtualFileHolder(RECENT))
                        )
                    }
                ) {
                    Text(
                        text = stringResource(R.string.more)
                    )
                }
            }

            item { Space(6.dp) }
        }
    }

    Space(12.dp)
}

@Composable
private fun CategoriesSection(
    tab: HomeTab
) {
    // Quick access tiles
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
}

@Composable
private fun StorageSection(
    mainActivityManager: MainActivityManager
) {
    val storageList = remember { mutableStateListOf<StorageDevice>() }

    LaunchedEffect(Unit) {
        storageList.addAll(StorageProvider.getStorageDevices(globalClass))
    }

    // Storage options
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        text = stringResource(R.string.storage),
        style = MaterialTheme.typography.titleMedium
    )

    storageList.forEach {
        StorageDeviceView(storageDevice = it) {
            mainActivityManager.replaceCurrentTabWith(FilesTab(it.contentHolder))
        }
        HorizontalDivider()
    }
}

@Composable
private fun BookmarksSection(
    mainActivityManager: MainActivityManager
) {
    if (globalClass.preferencesManager.bookmarks.isNotEmpty()) {
        SimpleNewTabViewItem(
            title = stringResource(R.string.bookmarks),
            imageVector = Icons.Rounded.Bookmarks
        ) {
            mainActivityManager.replaceCurrentTabWith(
                FilesTab(VirtualFileHolder(BOOKMARKS))
            )
        }
        HorizontalDivider()
    }
}

@Composable
private fun RecycleBinSection(
    mainActivityManager: MainActivityManager
) {
    SimpleNewTabViewItem(
        title = stringResource(R.string.recycle_bin),
        imageVector = Icons.Rounded.DeleteSweep
    ) {
        mainActivityManager.replaceCurrentTabWith(FilesTab(globalClass.recycleBinDir))
    }
    HorizontalDivider()
}

@Composable
private fun JumpToPathSection(
    mainActivityManager: MainActivityManager
) {
    SimpleNewTabViewItem(
        title = stringResource(R.string.jump_to_path),
        imageVector = Icons.Rounded.ArrowOutward
    ) {
        mainActivityManager.toggleJumpToPathDialog(true)
    }
}