package com.raival.compose.file.explorer.common.compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kevinnzou.compose.swipebox.SwipeBox
import com.kevinnzou.compose.swipebox.SwipeDirection
import com.kevinnzou.compose.swipebox.widget.SwipeIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteBox(
    modifier: Modifier = Modifier,
    endContentWidth: Dp = 60.dp,
    swipeDirection: SwipeDirection = SwipeDirection.EndToStart,
    endIcon: ImageVector = Icons.Outlined.Delete,
    onDeleteConfirm: () -> Unit = { },
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    SwipeBox(
        modifier = modifier,
        swipeDirection = swipeDirection,
        endContentWidth = endContentWidth,
        endContent = { swipeableState, endSwipeProgress ->
            SwipeIcon(
                imageVector = endIcon,
                contentDescription = null,
                tint = Color.White,
                background = Color(0xFFFA1E32),
                weight = 1f,
                iconSize = 20.dp
            ) {
                onDeleteConfirm()
                coroutineScope.launch { swipeableState.animateTo(0) }
            }
        }
    ) { _, _, _ ->
        content()
    }
}