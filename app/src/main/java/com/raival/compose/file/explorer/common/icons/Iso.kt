package com.raival.compose.`file`.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Iso: ImageVector by lazy {
    ImageVector.Builder(
        name = "ISO",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 489f,
        viewportHeight = 489f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(244.5f, 489f)
            curveTo(109.3f, 489f, 0f, 379.7f, 0f, 244.5f)
            reflectiveCurveTo(109.3f, 0f, 244.5f, 0f)
            reflectiveCurveTo(489f, 109.3f, 489f, 244.5f)
            reflectiveCurveTo(379.7f, 489f, 244.5f, 489f)
            close()
            moveTo(244.5f, 40.6f)
            curveToRelative(-112.4f, 0f, -203.9f, 91.5f, -203.9f, 203.9f)
            reflectiveCurveToRelative(91.5f, 203.9f, 203.9f, 203.9f)
            reflectiveCurveToRelative(203.9f, -91.5f, 203.9f, -203.9f)
            reflectiveCurveTo(356.9f, 40.6f, 244.5f, 40.6f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(244.5f, 278.8f)
            curveToRelative(-18.7f, 0f, -34.3f, -15.6f, -34.3f, -34.3f)
            reflectiveCurveToRelative(15.6f, -34.3f, 34.3f, -34.3f)
            reflectiveCurveToRelative(34.3f, 15.6f, 34.3f, 34.3f)
            reflectiveCurveTo(263.2f, 278.8f, 244.5f, 278.8f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(335f, 351.7f)
            curveToRelative(-8.3f, -7.3f, -9.4f, -20.8f, -2.1f, -29.1f)
            curveToRelative(19.8f, -21.8f, 30.2f, -49.9f, 30.2f, -79.1f)
            reflectiveCurveToRelative(-10.4f, -57.2f, -30.2f, -79.1f)
            curveToRelative(-7.3f, -8.3f, -6.2f, -20.8f, 2.1f, -29.1f)
            curveToRelative(8.3f, -7.3f, 20.8f, -6.2f, 29.1f, 2.1f)
            curveToRelative(26f, 29.1f, 40.6f, 66.6f, 40.6f, 106.1f)
            reflectiveCurveToRelative(-14.6f, 77f, -40.6f, 106.1f)
            curveTo(354f, 362.6f, 337.7f, 354.9f, 335f, 351.7f)
            close()
        }
    }.build()
}