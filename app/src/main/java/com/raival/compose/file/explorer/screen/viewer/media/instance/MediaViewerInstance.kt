package com.raival.compose.file.explorer.screen.viewer.media.instance

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.modal.MediaSource

class MediaViewerInstance(override val uri: Uri, override val id: String) : ViewerInstance {
    val player = ExoPlayer.Builder(globalClass).build()
    val mediaItem = MediaItem.fromUri(uri)
    val mediaSource = getMediaSource(globalClass, uri)

    init {
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun getMediaSource(context: Context, uri: Uri): MediaSource {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: return MediaSource.UnknownSource

        return when {
            mimeType.startsWith("audio/") -> {
                MediaSource.AudioSource
            }

            mimeType.startsWith("video/") -> {
                MediaSource.VideoSource
            }

            else -> {
                MediaSource.UnknownSource
            }
        }
    }

    override fun onClose() {
        player.release()
    }
}