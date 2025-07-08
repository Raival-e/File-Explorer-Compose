package com.raival.compose.file.explorer.screen.viewer.audio.model

import androidx.media3.common.Player

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val volume: Float = 1.0f
)