package com.raival.compose.file.explorer.screen.viewer.audio.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

data class AudioPlayerColorScheme(
    val primary: Color = Color(0xFF6750A4),
    val secondary: Color = Color(0xFF625B71),
    val background: Color = Color(0xFF1C1B1F),
    val surface: Color = Color(0xFF2B2930)
) {
    val tintColor: Color by lazy {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(primary.toArgb(), hsl)
        hsl[2] = (hsl[2] + 0.5f).coerceIn(0f, 1f)
        Color(ColorUtils.HSLToColor(hsl))
    }
}