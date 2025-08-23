package com.raival.compose.file.explorer.screen.viewer.audio

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.ui.MusicPlayerScreen
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import kotlinx.coroutines.launch

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
        val audioInstance = instance as AudioPlayerInstance
        
        if (fromPlaylist) {
            val playlistManager = PlaylistManager.getInstance()
            playlistManager.currentPlaylist.value?.let { playlist ->
                if (playlist.songs.isNotEmpty() && startIndex < playlist.songs.size) {
                    audioInstance.loadPlaylist(playlist, startIndex)
                }
            }
        } else {
            // Individual song - initialize with autoplay if enabled
            lifecycleScope.launch {
                audioInstance.initializePlayer(this@AudioPlayerActivity, audioInstance.uri)
            }
        }
        
        setContent {
            FileExplorerTheme {
                MusicPlayerScreen(
                    audioPlayerInstance = audioInstance,
                    onClosed = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}