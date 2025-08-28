package com.raival.compose.file.explorer.screen.viewer.audio.model

data class PlaylistData(
    val id: String,
    val name: String,
    val songPaths: List<String>,
    val createdAt: Long
)