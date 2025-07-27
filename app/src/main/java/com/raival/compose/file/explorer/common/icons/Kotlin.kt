package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Kotlin: ImageVector by lazy {
    ImageVector.Builder(
        name = "Kotlin",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 1024f,
        viewportHeight = 1024f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(863.1f, 914.4f)
            arcToRelative(
                116f,
                116f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                -166.4f,
                8.3f
            )
            lineTo(419.9f, 665.6f)
            quadToRelative(-2.2f, -1.8f, -4.3f, -3.9f)
            lineTo(633.6f, 538f)
            lineToRelative(221.3f, 205.6f)
            arcToRelative(
                123f,
                123f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = true,
                8.2f,
                170.8f
            )
            close()
            moveTo(381.8f, 280.8f)
            verticalLineToRelative(-23.5f)
            arcTo(128f, 128f, 0f, isMoreThanHalf = false, isPositiveArc = false, 255.6f, 128f)
            arcToRelative(
                128f,
                128f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                -126.2f,
                129.3f
            )
            lineTo(129.4f, 424.3f)
            close()
            moveTo(128f, 822f)
            verticalLineToRelative(7.2f)
            arcTo(129.2f, 129.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 255.6f, 960f)
            arcToRelative(
                129.2f,
                129.2f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                127.6f,
                -130.8f
            )
            verticalLineToRelative(-152f)
            close()
            moveTo(881.2f, 196.4f)
            arcToRelative(
                116.5f,
                116.5f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                -160.4f,
                -47.2f
            )
            lineToRelative(-587.5f, 333.4f)
            verticalLineToRelative(276.5f)
            lineToRelative(701.8f, -398.3f)
            arcToRelative(
                122.5f,
                122.5f,
                0f,
                isMoreThanHalf = false,
                isPositiveArc = false,
                46.1f,
                -164.4f
            )
            close()
        }
    }.build()
}
