package com.raival.compose.file.explorer.screen.viewer.video.model

import androidx.media3.common.Player

data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isMuted: Boolean = true,
    val showControls: Boolean = true,
    val title: String = ""
)
