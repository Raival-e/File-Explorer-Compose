package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.screen.main.startup.StartupTab
import com.raival.compose.file.explorer.screen.main.startup.StartupTabType
import com.raival.compose.file.explorer.screen.main.startup.StartupTabs
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import sh.calvin.reorderable.ReorderableCollectionItemScope

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabHeaderView(
    tab: Tab,
    isSelected: Boolean,
    index: Int,
    reorderableScope: ReorderableCollectionItemScope
) {
    val selectedBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val unselectedBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow

    val backgroundColorAnim = animateColorAsState(
        targetValue = if (!isSelected) unselectedBackgroundColor else selectedBackgroundColor
    )

    val alphaAnim = animateFloatAsState(targetValue = if (isSelected) 1f else 0.5f)

    var showTabHeaderMenu by remember(tab.id) {
        mutableStateOf(false)
    }

    val mainActivityManager = globalClass.mainActivityManager

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
            .then(with(reorderableScope) { Modifier.longPressDraggableHandle() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.alpha(alphaAnim.value),
            text = tab.header,
            fontSize = 14.sp
        )

        if (showTabHeaderMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showTabHeaderMenu = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                // get startup tabs
                val startupTabs = remember {
                    arrayListOf<StartupTab>().apply {
                        addAll(
                            (fromJson(globalClass.preferencesManager.behaviorPrefs.startupTabs)
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
                                globalClass.preferencesManager.behaviorPrefs.startupTabs =
                                    StartupTabs(startupTabs).toJson()
                                showTabHeaderMenu = false
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
                                globalClass.preferencesManager.behaviorPrefs.startupTabs =
                                    StartupTabs(startupTabs).toJson()
                                showTabHeaderMenu = false
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
                            showTabHeaderMenu = false
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.close_others)) },
                    onClick = {
                        globalClass.mainActivityManager.removeOtherTabs(index)
                        showTabHeaderMenu = false
                    }
                )
            }
        }
    }
}