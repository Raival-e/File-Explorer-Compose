package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Code: ImageVector by lazy {
    ImageVector.Builder(
        name = "CodeFile",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(14f, 2f)
            lineTo(6f, 2f)
            curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
            lineTo(4f, 20f)
            curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
            lineTo(18f, 22f)
            curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
            lineTo(20f, 8f)
            lineTo(14f, 2f)
            close()
            moveTo(11.034f, 13.652f)
            lineTo(8.441f, 15.215f)
            lineTo(11.034f, 16.714f)
            lineTo(11.034f, 18.451f)
            lineTo(6.734f, 15.651f)
            lineTo(6.734f, 14.751f)
            lineTo(11.034f, 11.851f)
            lineTo(11.034f, 13.652f)
            close()
            moveTo(17.207f, 15.562f)
            lineTo(13.007f, 18.449f)
            lineTo(12.995f, 16.712f)
            lineTo(15.464f, 15.164f)
            lineTo(13.007f, 13.65f)
            lineTo(13.007f, 11.862f)
            lineTo(17.207f, 14.762f)
            lineTo(17.207f, 15.562f)
            lineTo(17.207f, 15.562f)
            close()
            moveTo(13f, 9f)
            lineTo(13f, 3.5f)
            lineTo(18.5f, 9f)
            lineTo(13f, 9f)
            close()
        }
    }.build()
}
