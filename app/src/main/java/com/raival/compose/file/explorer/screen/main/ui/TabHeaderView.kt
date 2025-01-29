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
import com.raival.compose.file.explorer.App
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.screen.main.tab.Tab
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

    val mainActivityManager = App.globalClass.mainActivityManager

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
                    if (mainActivityManager.selectedTabIndex isNot index) {
                        mainActivityManager.tabs[mainActivityManager.selectedTabIndex].onTabStopped()
                        mainActivityManager.selectedTabIndex = index
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
                onDismissRequest = { showTabHeaderMenu = false }
            ) {
                if (index > 0) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.close)) },
                        onClick = {
                            App.globalClass.mainActivityManager.removeTabAt(index)
                            showTabHeaderMenu = false
                            tab.requestHomeToolbarUpdate()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.close_others)) },
                    onClick = {
                        App.globalClass.mainActivityManager.removeOtherTabs(index)
                        showTabHeaderMenu = false
                        tab.requestHomeToolbarUpdate()
                    }
                )
                if (index > 0) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.close_all)) },
                        onClick = {
                            App.globalClass.mainActivityManager.closeAllTabs()
                            showTabHeaderMenu = false
                            tab.requestHomeToolbarUpdate()
                        }
                    )
                }
            }
        }
    }
}