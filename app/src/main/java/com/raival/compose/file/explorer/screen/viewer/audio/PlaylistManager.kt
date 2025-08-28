package com.raival.compose.file.explorer.screen.viewer.audio

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import com.raival.compose.file.explorer.screen.viewer.audio.model.PlaylistData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import androidx.core.content.edit

class PlaylistManager private constructor() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()
    private val gson = Gson()
    private val preferences: SharedPreferences
        get() = globalClass.getSharedPreferences("app_preferences", 0)

    companion object {
        @Volatile
        private var INSTANCE: PlaylistManager? = null
        private const val PREFS_KEY = "saved_playlists"

        fun getInstance(): PlaylistManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlaylistManager()
                instance.loadPlaylists()
                INSTANCE = instance
                instance
            }
        }
    }

    private fun savePlaylists() {
        try {
            val playlistsData = _playlists.value.map { playlist ->
                PlaylistData(
                    id = playlist.id,
                    name = playlist.name,
                    songPaths = playlist.songs.map { it.file.absolutePath },
                    createdAt = playlist.createdAt
                )
            }
            val json = gson.toJson(playlistsData)

            preferences.edit { putString(PREFS_KEY, json) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPlaylists() {
        try {
            val json = preferences.getString(PREFS_KEY, "[]") ?: "[]"

            val type = object : TypeToken<List<PlaylistData>>() {}.type
            val playlistsData: List<PlaylistData> = gson.fromJson(json, type)

            val loadedPlaylists = playlistsData.mapNotNull { data ->
                try {
                    val validSongs = data.songPaths.mapNotNull { path ->
                        val file = File(path)
                        if (file.exists() && file.isFile) {
                            LocalFileHolder(file)
                        } else null
                    }.toMutableList()

                    Playlist(
                        id = data.id,
                        name = data.name,
                        songs = validSongs,
                        createdAt = data.createdAt
                    )
                } catch (e: Exception) {
                    null
                }
            }
            _playlists.value = loadedPlaylists
        } catch (e: Exception) {
            e.printStackTrace()
            _playlists.value = emptyList()
        }
    }

    fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(name = name)
        _playlists.value = _playlists.value + playlist
        savePlaylists()
        return playlist
    }

    fun createPlaylistWithSong(name: String, song: LocalFileHolder): Playlist {
        val playlist = Playlist(name = name)
        playlist.addSong(song)
        _playlists.value = _playlists.value + playlist
        savePlaylists()
        return playlist
    }

    fun addSongToPlaylist(playlistId: String, song: LocalFileHolder): Boolean {
        var wasAdded = false
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy().apply { wasAdded = addSong(song) }
            } else {
                playlist
            }
        }
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
        savePlaylists()
        return wasAdded
    }

    fun addMultipleSongsToPlaylist(playlistId: String, songs: List<LocalFileHolder>): Int {
        if (songs.isEmpty()) return 0
        
        var addedCount = 0
        _playlists.value = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy().apply { 
                    songs.forEach { song -> 
                        if (addSong(song)) {
                            addedCount++
                        }
                    }
                }
            } else {
                playlist
            }
        }
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
        savePlaylists()
        return addedCount
    }

    fun removeSongFromPlaylistAt(playlistId: String, index: Int) {
        val updatedPlaylists = _playlists.value.map { playlist ->
            if (playlist.id == playlistId && index >= 0 && index < playlist.songs.size) {
                val newSongs = playlist.songs.toMutableList()
                newSongs.removeAt(index)
                playlist.copy(songs = newSongs)
            } else {
                playlist
            }
        }
        _playlists.value = updatedPlaylists
        
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = updatedPlaylists.find { it.id == playlistId }
        }
        savePlaylists()
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filter { it.id != playlistId }
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = null
        }
        savePlaylists()
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
        if (_currentPlaylist.value?.id == playlistId) {
            _currentPlaylist.value = _playlists.value.find { it.id == playlistId }
        }
        savePlaylists()
    }
}