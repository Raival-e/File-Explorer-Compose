package com.raival.compose.file.explorer.screen.viewer.media.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun RotatingDisk(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rotationEasing: Easing = LinearEasing,
    diskColor: Color = colorScheme.surfaceContainerHighest,
    dotColor: Color = colorScheme.surfaceContainer
) {
    val rotation = remember { Animatable(0f) }
    val sizeAnimation = remember { Animatable(1f) }
    val dotRadiusFraction = remember { 0.056f }
    val dotMargin = remember { 15f }

    LaunchedEffect(enabled) {
        launch {
            while (enabled) {
                rotation.animateTo(
                    targetValue = rotation.value + 1,
                    animationSpec = tween(durationMillis = 50, easing = rotationEasing)
                )
                if (rotation.value == 360f) {
                    rotation.snapTo(0f)
                }
            }
        }
    }

    LaunchedEffect(enabled) {
        launch {
            while (enabled) {
                sizeAnimation.animateTo(
                    targetValue = Random.nextInt(95, 105) / 100f,
                    animationSpec = tween(
                        durationMillis = Random.nextInt(50, 250),
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = sizeAnimation.value
            scaleY = sizeAnimation.value
        },
        onDraw = {
            val radius = size.minDimension / 2
            val dotRadius = radius * dotRadiusFraction
            val dotAngle = 45f

            drawRotatingDisk(diskColor, radius, rotation.value)
            drawRotatingDot(dotColor, radius, dotRadius, dotAngle, dotMargin, rotation.value)
        }
    )
}

fun DrawScope.drawRotatingDisk(color: Color, radius: Float, rotationValue: Float) {
    rotate(rotationValue, pivot = center) {
        drawCircle(
            color = color,
            radius = radius,
            center = center
        )
    }
}

fun DrawScope.drawRotatingDot(
    color: Color,
    radius: Float,
    dotRadius: Float,
    angle: Float,
    margin: Float,
    rotationValue: Float
) {
    val dotX = center.x + (radius - dotRadius - margin) * cos(Math.toRadians(angle.toDouble())).toFloat()
    val dotY = center.y - (radius - dotRadius - margin) * sin(Math.toRadians(angle.toDouble())).toFloat()

    rotate(rotationValue, pivot = center) {
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(dotX, dotY)
        )
    }
}