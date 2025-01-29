package com.raival.compose.file.explorer.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.detectVerticalSwipe(
    onSwipeUp: () -> Unit = { },
    onSwipeDown: () -> Unit = { },
    threshold: Int = 50
) = this.pointerInput(onSwipeUp, onSwipeDown, threshold) {
    var handled = false
    detectVerticalDragGestures(
        onDragEnd = { handled = false },
        onDragCancel = { handled = false },
        onVerticalDrag = { change, dragAmount ->
            if (!handled) {
                if (dragAmount < -threshold) {
                    onSwipeUp()
                    change.consume()
                    handled = true
                } else if (dragAmount > threshold) {
                    onSwipeDown()
                    change.consume()
                    handled = true
                }
            }
        }
    )
}

fun Modifier.block(
    shape: Shape = RoundedCornerShape(12.dp),
    color: Color = Color.Unspecified,
    applyResultPadding: Boolean = false,
    resultPadding: Dp = 4.dp,
    borderColor: Color = Color.Unspecified,
    borderSize: Dp = 1.dp
) = composed {
    val color1 = if (color.isUnspecified) {
        MaterialTheme.colorScheme.surfaceContainer
    } else color

    this
        .clip(shape)
        .background(color = color1, shape = shape)
        .then(
            if (borderSize == 0.dp) Modifier else Modifier.border(
                width = borderSize,
                color = if (borderColor.isUnspecified)
                    MaterialTheme.colorScheme.outlineVariant(0.1f, color1) else borderColor,
                shape = shape
            )
        )
        .then(if (applyResultPadding) Modifier.padding(resultPadding) else Modifier)
}

fun ColorScheme.outlineVariant(
    luminance: Float = 0.3f,
    onTopOf: Color = surfaceColorAtElevation(3.dp)
) = onSecondaryContainer
    .copy(alpha = luminance)
    .compositeOver(onTopOf)
