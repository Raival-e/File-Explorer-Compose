package com.raival.compose.file.explorer.screen.playlist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.raival.compose.file.explorer.screen.playlist.ui.PlaylistManagerScreen
import com.raival.compose.file.explorer.screen.viewer.audio.AudioPlayerActivity
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.theme.FileExplorerTheme

class PlaylistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FileExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlaylistManagerScreen(
                        onBackPressed = { onBackPressedDispatcher.onBackPressed() },
                        onPlayPlaylist = { playlist, startIndex ->
                            PlaylistManager.getInstance().loadPlaylist(playlist.id)
                            if (playlist.songs.isNotEmpty() && startIndex < playlist.songs.size) {
                                val firstSong = playlist.songs[startIndex]
                                val intent = Intent(this@PlaylistActivity, AudioPlayerActivity::class.java).apply {
                                    data = Uri.fromFile(firstSong.file)
                                    putExtra("startIndex", startIndex)
                                    putExtra("fromPlaylist", true)
                                    putExtra("uid", firstSong.uid)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
