package com.raival.compose.file.explorer.screen.viewer.audio.model

import android.net.Uri
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import java.util.UUID

data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val songs: MutableList<LocalFileHolder> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val currentSongIndex: Int = 0
) {
    fun addSong(song: LocalFileHolder) {
        if (!songs.contains(song)) {
            songs.add(song)
        }
    }

    fun removeSong(song: LocalFileHolder) {
        songs.remove(song)
    }

    fun removeSongAt(index: Int) {
        if (index in 0 until songs.size) {
            songs.removeAt(index)
        }
    }

    fun getSongUris(): List<Uri> {
        return songs.map { Uri.fromFile(it.file) }
    }

    fun isEmpty() = songs.isEmpty()

    fun size() = songs.size
}
