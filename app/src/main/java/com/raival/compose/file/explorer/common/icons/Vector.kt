package com.raival.compose.file.explorer.common.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PrismIcons.Vector: ImageVector by lazy {
    ImageVector.Builder(
        name = "Vector",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = SolidColor(Color.White),
            strokeLineWidth = 1f,
            pathFillType = PathFillType.EvenOdd
        ) {
            moveTo(7f, 2f)
            curveTo(8.105f, 2f, 9f, 2.895f, 9f, 4f)
            lineTo(14.268f, 4f)
            curveTo(14.613f, 3.402f, 15.26f, 3f, 16f, 3f)
            curveTo(17.105f, 3f, 18f, 3.895f, 18f, 5f)
            curveTo(18f, 6.105f, 17.105f, 7f, 16f, 7f)
            curveTo(15.26f, 7f, 14.613f, 6.598f, 14.268f, 6f)
            lineTo(10.899f, 6f)
            curveTo(12.196f, 7.271f, 13f, 9.041f, 13f, 11f)
            lineTo(13f, 13f)
            curveTo(13f, 14.722f, 13.87f, 16.24f, 15.194f, 17.139f)
            curveTo(15.516f, 16.465f, 16.204f, 16f, 17f, 16f)
            lineTo(19f, 16f)
            curveTo(20.105f, 16f, 21f, 16.895f, 21f, 18f)
            lineTo(21f, 20f)
            curveTo(21f, 21.105f, 20.105f, 22f, 19f, 22f)
            lineTo(17f, 22f)
            curveTo(15.895f, 22f, 15f, 21.105f, 15f, 20f)
            lineTo(9.732f, 20f)
            curveTo(9.387f, 20.598f, 8.74f, 21f, 8f, 21f)
            curveTo(6.895f, 21f, 6f, 20.105f, 6f, 19f)
            curveTo(6f, 17.895f, 6.895f, 17f, 8f, 17f)
            curveTo(8.74f, 17f, 9.387f, 17.402f, 9.732f, 18f)
            lineTo(13.101f, 18f)
            curveTo(11.804f, 16.729f, 11f, 14.959f, 11f, 13f)
            lineTo(11f, 11f)
            curveTo(11f, 9.279f, 10.13f, 7.76f, 8.806f, 6.861f)
            curveTo(8.484f, 7.535f, 7.796f, 8f, 7f, 8f)
            lineTo(5f, 8f)
            curveTo(3.895f, 8f, 3f, 7.105f, 3f, 6f)
            lineTo(3f, 4f)
            curveTo(3f, 2.895f, 3.895f, 2f, 5f, 2f)
            lineTo(7f, 2f)
            close()
            moveTo(19f, 18f)
            lineTo(17f, 18f)
            lineTo(17f, 20f)
            lineTo(19f, 20f)
            lineTo(19f, 18f)
            close()
            moveTo(7f, 4f)
            lineTo(5f, 4f)
            lineTo(5f, 6f)
            lineTo(7f, 6f)
            lineTo(7f, 4f)
            close()
        }
    }.build()
}