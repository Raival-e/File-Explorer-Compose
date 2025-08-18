package com.raival.compose.file.explorer.screen.viewer.audio.model

data class PlaylistState(
    val currentPlaylist: Playlist? = null,
    val currentSongIndex: Int = 0,
    val isShuffled: Boolean = false,
    val shuffledIndices: List<Int> = emptyList()
) {
    fun getCurrentSong() = currentPlaylist?.songs?.getOrNull(
        if (isShuffled && shuffledIndices.isNotEmpty()) {
            shuffledIndices.getOrNull(currentSongIndex) ?: currentSongIndex
        } else {
            currentSongIndex
        }
    )

    fun hasNextSong() = if (isShuffled && shuffledIndices.isNotEmpty()) {
        currentSongIndex < shuffledIndices.size - 1
    } else {
        currentPlaylist?.let { currentSongIndex < it.songs.size - 1 } ?: false
    }

    fun hasPreviousSong() = currentSongIndex > 0

    fun getTotalSongs() = currentPlaylist?.songs?.size ?: 0
}
