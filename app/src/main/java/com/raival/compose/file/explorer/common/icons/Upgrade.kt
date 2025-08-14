package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Upgrade: ImageVector by lazy {
    ImageVector.Builder(
        name = "Upgrade",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 1f)
            curveTo(5.92f, 1f, 1f, 5.92f, 1f, 12f)
            reflectiveCurveToRelative(4.92f, 11f, 11f, 11f)
            reflectiveCurveToRelative(11f, -4.92f, 11f, -11f)
            reflectiveCurveTo(18.08f, 1f, 12f, 1f)
            close()
            moveTo(16.29f, 12.71f)
            lineTo(13f, 9.41f)
            verticalLineTo(18f)
            horizontalLineToRelative(-2f)
            verticalLineTo(9.41f)
            lineToRelative(-3.29f, 3.29f)
            lineToRelative(-1.41f, -1.41f)
            lineTo(12f, 5.59f)
            lineToRelative(5.71f, 5.71f)
            lineTo(16.29f, 12.71f)
            close()
        }
    }.build()
}
