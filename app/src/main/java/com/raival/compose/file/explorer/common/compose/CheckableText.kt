package com.raival.compose.file.explorer.common.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CheckableText(
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = { },
    textStartPadding: Dp = 8.dp,
    boxShape: Shape = RoundedCornerShape(4.dp),
    checkedBoxBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedBoxBackgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.5.dp),
    boxSize: Dp = 21.dp,
    strokeWidth: Dp = 1.dp,
    checkIcon: ImageVector = Icons.Rounded.Done,
    checkIconTint: Color = MaterialTheme.colorScheme.surface,
    text: @Composable () -> Unit
) {
    val colorAnimation = animateColorAsState(
        targetValue = if (checked) checkedBoxBackgroundColor
        else uncheckedBoxBackgroundColor, label = "color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = { onCheckedChange(!checked) },
                onLongClick = { onCheckedChange(!checked) },
                interactionSource = interactionSource,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .size(boxSize)
                .background(
                    color = colorAnimation.value,
                    shape = boxShape
                )
                .clip(boxShape)
                .clickable { onCheckedChange(!checked) }
                .then(
                    if (strokeWidth > 0.dp) Modifier.border(
                        width = strokeWidth,
                        color = MaterialTheme.colorScheme.outline,
                        shape = boxShape
                    ) else Modifier
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = checked) {
                Icon(
                    modifier = Modifier.size(boxSize - 1.dp),
                    imageVector = checkIcon,
                    contentDescription = null,
                    tint = checkIconTint
                )
            }
        }

        Space(textStartPadding)

        text()
    }
}