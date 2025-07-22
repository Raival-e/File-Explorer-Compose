package com.raival.compose.file.explorer.screen.main.tab.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeLayout
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeSectionConfig
import com.raival.compose.file.explorer.screen.main.tab.home.data.HomeSectionType
import com.raival.compose.file.explorer.screen.main.tab.home.data.getDefaultHomeLayout
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayoutSettingsScreen(
    onBackClick: (List<HomeSectionConfig>) -> Unit
) {
    val useDarkIcons = !isSystemInDarkTheme()
    val coroutineScope = rememberCoroutineScope()
    val sections = remember { mutableStateListOf<HomeSectionConfig>() }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            sections.add(
                to.index,
                sections.removeAt(from.index)
            )
        }
    )
    Dialog(
        onDismissRequest = { onBackClick(sections) },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val color = MaterialTheme.colorScheme.surfaceContainerHigh
        val systemUiController = rememberSystemUiController()
        DisposableEffect(systemUiController, useDarkIcons) {
            systemUiController.setStatusBarColor(color = color, darkIcons = useDarkIcons)
            onDispose {}
        }

        LaunchedEffect(Unit) {
            val config = try {
                Gson().fromJson(
                    globalClass.preferencesManager.homeTabLayout,
                    HomeLayout::class.java
                )
            } catch (e: Exception) {
                logger.logError(e)
                getDefaultHomeLayout()
            }.sections.sortedBy { it.order }

            sections.addAll(config)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.customize_home_layout),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onBackClick(sections) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    sections.clear()
                                    sections.addAll(getDefaultHomeLayout().sections)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(vertical = 16.dp)
            ) {
                // Sections list
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sections, key = { it.id }) { section ->
                        ReorderableItem(
                            state = reorderableState,
                            key = section.id
                        ) { isDragging ->
                            HomeSectionItem(
                                reorderableScope = this,
                                section = section,
                                isDragging = isDragging,
                                onToggle = { enabled ->
                                    val index = sections.indexOf(section)
                                    sections[index] = section.copy(isEnabled = enabled)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeSectionItem(
    reorderableScope: ReorderableCollectionItemScope,
    section: HomeSectionConfig,
    isDragging: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Section icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.type.getIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Section info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = section.type.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = with(reorderableScope) {
                    Modifier
                        .padding(end = 8.dp)
                        .draggableHandle()
                }
            )

            // Toggle switch
            Switch(
                checked = section.isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

fun HomeSectionType.getIcon(): ImageVector {
    return when (this) {
        HomeSectionType.RECENT_FILES -> Icons.Rounded.History
        HomeSectionType.CATEGORIES -> Icons.Rounded.Category
        HomeSectionType.STORAGE -> Icons.Rounded.Storage
        HomeSectionType.BOOKMARKS -> Icons.Rounded.Bookmarks
        HomeSectionType.RECYCLE_BIN -> Icons.Rounded.DeleteSweep
        HomeSectionType.JUMP_TO_PATH -> Icons.Rounded.ArrowOutward
    }
}

fun HomeSectionType.getDescription(): String {
    return when (this) {
        HomeSectionType.RECENT_FILES -> globalClass.getString(R.string.recently_modified_files)
        HomeSectionType.CATEGORIES -> globalClass.getString(R.string.quick_access_categories)
        HomeSectionType.STORAGE -> globalClass.getString(R.string.storage_devices_and_locations)
        HomeSectionType.BOOKMARKS -> globalClass.getString(R.string.bookmarked_files_and_folders)
        HomeSectionType.RECYCLE_BIN -> globalClass.getString(R.string.deleted_files)
        HomeSectionType.JUMP_TO_PATH -> globalClass.getString(R.string.quick_path_navigation)
    }
}