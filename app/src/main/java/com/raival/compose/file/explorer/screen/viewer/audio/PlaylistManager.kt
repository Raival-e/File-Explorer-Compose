package com.raival.compose.file.explorer.screen.viewer.audio

import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaylistManager {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(name = name)
        _playlists.value = _playlists.value + playlist
        return playlist
    }

    fun createPlaylistWithSong(name: String, song: LocalFileHolder): Playlist {
        val playlist = Playlist(name = name)
        playlist.addSong(song)
        _playlists.value = _playlists.value + playlist
        return playlist
    }

    fun addSongToPlaylist(playlistId: String, song: LocalFileHolder) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy().apply { addSong(song) }
            } else {
                playlist
            }
        }
        
        // Update current playlist if it's the one being modified
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
    }

    fun removeSongFromPlaylistAt(playlistId: String, index: Int) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy().apply { removeSongAt(index) }
            } else {
                playlist
            }
        }
        
        // Update current playlist if it's the one being modified
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        
        // Clear current playlist if it was deleted
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = null
        }
    }

    fun setCurrentPlaylist(playlist: Playlist) {
        _currentPlaylist.value = playlist
    }

    fun clearCurrentPlaylist() {
        _currentPlaylist.value = null
    }

    fun loadPlaylist(playlistId: String) {
        val playlist = _playlists.value.find { it.id == playlistId }
        _currentPlaylist.value = playlist
    }

    fun getPlaylistById(id: String): Playlist? {
        return _playlists.value.find { it.id == id }
    }

    fun updatePlaylistName(playlistId: String, newName: String) {
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(name = newName)
            } else {
                playlist
            }
        }
        
        // Update current playlist if it's the one being modified
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PlaylistManager? = null

        fun getInstance(): PlaylistManager {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: PlaylistManager()
                INSTANCE = instance
                instance
            }
        }
    }
}
