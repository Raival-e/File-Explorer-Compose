package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Markdown: ImageVector by lazy {
    ImageVector.Builder(
        name = "Markdown",
        defaultWidth = 26.dp,
        defaultHeight = 16.dp,
        viewportWidth = 208f,
        viewportHeight = 128f
    ).apply {
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 10f
        ) {
            moveTo(15f, 5f)
            horizontalLineToRelative(178f)
            curveToRelative(5.52f, 0f, 10f, 4.48f, 10f, 10f)
            verticalLineToRelative(98f)
            curveToRelative(0f, 5.52f, -4.48f, 10f, -10f, 10f)
            horizontalLineTo(15f)
            curveToRelative(-5.52f, 0f, -10f, -4.48f, -10f, -10f)
            verticalLineTo(15f)
            curveTo(5f, 9.48f, 9.48f, 5f, 15f, 5f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(30f, 98f)
            verticalLineTo(30f)
            horizontalLineToRelative(20f)
            lineToRelative(20f, 25f)
            lineToRelative(20f, -25f)
            horizontalLineToRelative(20f)
            verticalLineToRelative(68f)
            horizontalLineTo(90f)
            verticalLineTo(59f)
            lineTo(70f, 84f)
            lineTo(50f, 59f)
            verticalLineToRelative(39f)
            horizontalLineTo(30f)
            close()
            moveTo(155f, 98f)
            lineToRelative(-30f, -33f)
            horizontalLineToRelative(20f)
            verticalLineTo(30f)
            horizontalLineToRelative(20f)
            verticalLineToRelative(35f)
            horizontalLineToRelative(20f)
            lineTo(155f, 98f)
            close()
        }
    }.build()
}
