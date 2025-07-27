package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Sql: ImageVector by lazy {
    ImageVector.Builder(
        name = "SQL",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 72f,
        viewportHeight = 72f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(36f, 12.07f)
            curveToRelative(-11.85f, 0f, -21.46f, 3.21f, -21.46f, 7.19f)
            verticalLineToRelative(5.89f)
            curveToRelative(0f, 4f, 9.61f, 7.19f, 21.46f, 7.19f)
            reflectiveCurveToRelative(21.45f, -3.21f, 21.45f, -7.19f)
            lineTo(57.45f, 19.26f)
            curveTo(57.46f, 15.28f, 47.85f, 12.07f, 36f, 12.07f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(36f, 35.78f)
            curveToRelative(-11.32f, 0f, -20.64f, -2.93f, -21.46f, -6.66f)
            curveToRelative(0f, 0.18f, 0f, 9.75f, 0f, 9.75f)
            curveToRelative(0f, 4f, 9.61f, 7.18f, 21.46f, 7.18f)
            reflectiveCurveToRelative(21.45f, -3.21f, 21.45f, -7.18f)
            curveToRelative(0f, 0f, 0f, -9.57f, 0f, -9.75f)
            curveTo(56.63f, 32.85f, 47.32f, 35.78f, 36f, 35.78f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(57.44f, 43f)
            curveToRelative(-0.82f, 3.72f, -10.12f, 6.66f, -21.43f, 6.66f)
            reflectiveCurveTo(15.37f, 46.72f, 14.55f, 43f)
            verticalLineToRelative(9.75f)
            curveToRelative(0f, 4f, 9.61f, 7.18f, 21.46f, 7.18f)
            reflectiveCurveToRelative(21.45f, -3.21f, 21.45f, -7.18f)
            close()
        }
    }.build()
}