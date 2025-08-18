package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.startup.StartupTab
import com.raival.compose.file.explorer.screen.main.startup.StartupTabType
import com.raival.compose.file.explorer.screen.main.startup.StartupTabs
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun TabLayout(
    tabLayoutState: LazyListState,
    tabs: List<Tab>,
    selectedTabIndex: Int,
    onReorder: (Int, Int) -> Unit,
    onAddNewTab: () -> Unit
) {
    val selectedTabBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val unselectedTabBackgroundColor =
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.5f)
    val draggedTabBackgroundColor = MaterialTheme.colorScheme.primary
    val draggedTabTextColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val hideToolbar = globalClass.preferencesManager.hideToolbar

    val mainActivityManager = globalClass.mainActivityManager

    var from by remember { mutableIntStateOf(-1) }
    var to by remember { mutableIntStateOf(-1) }
    var draggedItem by remember { mutableIntStateOf(-1) }
    val list = remember { mutableStateListOf<Int>() }

    val reorderableLazyListState = rememberReorderableLazyListState(tabLayoutState) { old, new ->
        if (draggedItem > -1) {
            if (from < 0) from = old.index
            to = new.index

            list.add(new.index, list.removeAt(old.index))
        }
    }

    LaunchedEffect(draggedItem) {
        if (draggedItem < 0 && from > -1 && to > -1) {
            onReorder(from, to)
            from = -1
            to = -1
        }
    }

    LaunchedEffect(tabs) {
        list.clear()
        list.addAll(tabs.map { it.id })
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(if (hideToolbar) 50.dp else 42.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!hideToolbar) {
            IconButton(
                onClick = onAddNewTab
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
        }
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 42.dp),
            state = tabLayoutState,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hideToolbar) {
                item {
                    Space(8.dp)
                }
            }
            itemsIndexed(list, key = { _, item -> item }) { index, id ->
                tabs.find { it.id == id }?.let { tab ->
                    ReorderableItem(reorderableLazyListState, key = tab.id) { isDragged ->
                        if (isDragged && draggedItem isNot id) {
                            draggedItem = id
                        } else if (!isDragged && draggedItem == id) {
                            draggedItem = -1
                        }

                        val isSelected = selectedTabIndex == index

                        val backgroundColorAnim = animateColorAsState(
                            targetValue =
                                if (isDragged) {
                                    draggedTabBackgroundColor
                                } else if (isSelected) {
                                    selectedTabBackgroundColor
                                } else {
                                    unselectedTabBackgroundColor
                                }
                        )

                        val textColorAnim = animateColorAsState(
                            targetValue =
                                if (isDragged) {
                                    draggedTabTextColor
                                } else {
                                    Color.Unspecified
                                }
                        )

                        val alphaAnim = animateFloatAsState(
                            targetValue = if (isSelected && draggedItem == -1) 1f else 0.5f
                        )

                        var showTabHeaderMenu by remember(tab.id) {
                            mutableStateOf(false)
                        }

                        Row(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 100.dp)
                                .fillMaxHeight()
                                .padding(end = 4.dp)
                                .background(
                                    color = backgroundColorAnim.value,
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                )
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {
                                        if (!isSelected) {
                                            mainActivityManager.selectTabAt(index)
                                        } else {
                                            showTabHeaderMenu = true
                                        }
                                    }
                                )
                                .padding(horizontal = 20.dp)
                                .longPressDraggableHandle(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.alpha(alphaAnim.value),
                                text = tab.header,
                                fontSize = 14.sp,
                                color = textColorAnim.value
                            )
                            if (showTabHeaderMenu) {
                                OptionsMenu(
                                    tab = tab,
                                    index = index,
                                    onDismiss = { showTabHeaderMenu = false }
                                )
                            }
                        }
                    }
                }
            }
        }
        if (globalClass.preferencesManager.hideToolbar) {
            IconButton(
                onClick = onAddNewTab
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
            }
            MoreOptionsButton()
        }
    }
}

@Composable
fun OptionsMenu(
    tab: Tab,
    index: Int,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        // get startup tabs
        val startupTabs = remember {
            arrayListOf<StartupTab>().apply {
                addAll(
                    (fromJson(globalClass.preferencesManager.startupTabs)
                        ?: StartupTabs.default()).tabs
                )
            }
        }

        @Composable
        fun addStartupTabMenuItems(
            tabType: StartupTabType,
            extra: String = emptyString,
            canAdd: Boolean,
            canRemove: Boolean
        ) {
            if (canAdd) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.add_to_startup)) },
                    onClick = {
                        startupTabs.add(StartupTab(tabType, extra))
                        globalClass.preferencesManager.startupTabs =
                            StartupTabs(startupTabs).toJson()
                        onDismiss()
                        showMsg(globalClass.getString(R.string.added_as_startup_tab))
                    }
                )
            }

            if (canRemove && startupTabs.size > 1) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.remove_from_startup)) },
                    onClick = {
                        startupTabs.removeIf {
                            it.type == tabType && (extra == emptyString || it.extra == extra)
                        }
                        globalClass.preferencesManager.startupTabs =
                            StartupTabs(startupTabs).toJson()
                        onDismiss()
                        showMsg(globalClass.getString(R.string.removed_from_startup_tabs))
                    }
                )
            }
        }

        when (tab) {
            is HomeTab -> {
                val homeTabs = startupTabs.filter { it.type == StartupTabType.HOME }
                addStartupTabMenuItems(
                    tabType = StartupTabType.HOME,
                    canAdd = homeTabs.isEmpty(),
                    canRemove = homeTabs.isNotEmpty()
                )
            }

            is AppsTab -> {
                val appsTabs = startupTabs.filter { it.type == StartupTabType.APPS }
                addStartupTabMenuItems(
                    tabType = StartupTabType.APPS,
                    canAdd = appsTabs.isEmpty(),
                    canRemove = appsTabs.isNotEmpty()
                )
            }

            is FilesTab -> {
                if (tab.activeFolder is LocalFileHolder) {
                    val uniquePath = (tab.activeFolder as LocalFileHolder).uniquePath
                    val filesTabsPaths = startupTabs
                        .filter { it.type == StartupTabType.FILES }
                        .map { it.extra }

                    addStartupTabMenuItems(
                        tabType = StartupTabType.FILES,
                        extra = uniquePath,
                        canAdd = true, // Files tabs can always be added
                        canRemove = filesTabsPaths.contains(uniquePath)
                    )
                }
            }
        }

        if (index > 0) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.close)) },
                onClick = {
                    globalClass.mainActivityManager.removeTabAt(index)
                    onDismiss()
                }
            )
        }

        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.close_others)) },
            onClick = {
                globalClass.mainActivityManager.removeOtherTabs(index)
                onDismiss()
            }
        )

        if (tab !is HomeTab) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.home_tab_title)) },
                onClick = {
                    globalClass.mainActivityManager.replaceCurrentTabWith(HomeTab())
                    onDismiss()
                }
            )
        }
    }
}