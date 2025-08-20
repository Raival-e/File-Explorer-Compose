package com.raival.compose.file.explorer.screen.viewer.audio

import android.net.Uri
import androidx.activity.compose.setContent
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.ui.MusicPlayerScreen
import com.raival.compose.file.explorer.theme.FileExplorerTheme

class AudioPlayerActivity : ViewerActivity() {
    override fun onCreateNewInstance(
        uri: Uri,
        uid: String
    ): ViewerInstance {
        return AudioPlayerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        val fromPlaylist = intent.getBooleanExtra("fromPlaylist", false)
        val startIndex = intent.getIntExtra("startIndex", 0)
        
        if (fromPlaylist) {
            val playlistManager = PlaylistManager.getInstance()
            playlistManager.currentPlaylist.value?.let { playlist ->
                if (playlist.songs.isNotEmpty() && startIndex < playlist.songs.size) {
                    val audioInstance = instance as AudioPlayerInstance
                    audioInstance.loadPlaylist(playlist, startIndex)
                }
            }
        }
        
        setContent {
            FileExplorerTheme {
                MusicPlayerScreen(
                    audioPlayerInstance = instance as AudioPlayerInstance,
                    onClosed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}