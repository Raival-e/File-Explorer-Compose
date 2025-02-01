package com.raival.compose.file.explorer.screen.viewer.media.misc

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

/**
 * A simple audio manager using Media3 ExoPlayer.
 */
class AudioPlayerManager(context: Context) {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var listener: AudioPlayerManagerListener? = null

    // Handler to update playback progress every second.
    private val updateHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            val currentPosition = exoPlayer.currentPosition
            val duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L
            val remaining = if (duration > currentPosition) duration - currentPosition else 0L
            listener?.onProgressUpdated(currentPosition, remaining)
            updateHandler.postDelayed(this, 1000)
        }
    }

    /**
     * Set a listener to receive callbacks.
     */
    fun setListener(listener: AudioPlayerManagerListener) {
        this.listener = listener
    }

    /**
     * Prepares the player with the given audio URI.
     */
    fun prepare(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()

        // Listen for playback and metadata events.
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                // For simplicity, we consider the player "playing" when playWhenReady is true and state is ready.
                listener?.onPlaybackStateChanged(exoPlayer.playWhenReady && state == Player.STATE_READY)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val title = mediaMetadata.title?.toString()
                val album = mediaMetadata.albumTitle?.toString()
                val artist = mediaMetadata.artist?.toString()
                val artwork = mediaMetadata.artworkData ?: mediaMetadata.artworkUri
                val duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L

                val metadata = AudioMetadata(title, album, artist, artwork, duration)
                listener?.onMetadataChanged(metadata)
            }
        })
    }

    /**
     * Starts playback and begins periodic progress updates.
     */
    fun play() {
        exoPlayer.playWhenReady = true
        updateHandler.post(updateRunnable)
    }

    /**
     * Pauses playback and stops progress updates.
     */
    fun pause() {
        exoPlayer.playWhenReady = false
        updateHandler.removeCallbacks(updateRunnable)
    }

    /**
     * Stops playback completely.
     */
    fun stop() {
        exoPlayer.stop()
        updateHandler.removeCallbacks(updateRunnable)
    }

    /**
     * Fast-forwards playback by 5 seconds.
     */
    fun forward() {
        val newPosition = exoPlayer.currentPosition + 2000L
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Rewinds playback by 5 seconds.
     */
    fun backward() {
        val newPosition = (exoPlayer.currentPosition - 2000L).coerceAtLeast(0L)
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Seek to a new position
     */
    fun seekTo(position: Long) {
        val newPosition = position.coerceIn(0, exoPlayer.duration)
        exoPlayer.seekTo(position)
    }

    /**
     * Releases player resources.
     */
    fun release() {
        updateHandler.removeCallbacks(updateRunnable)
        exoPlayer.release()
    }

    /**
     * Callback interface for playback state, progress, and metadata updates.
     */
    interface AudioPlayerManagerListener {
        /**
         * Called when playback state changes.
         * @param isPlaying true if the audio is playing.
         */
        fun onPlaybackStateChanged(isPlaying: Boolean)

        /**
         * Called periodically with the current position and remaining time (in milliseconds).
         */
        fun onProgressUpdated(currentPosition: Long, remainingTime: Long)

        /**
         * Called when metadata for the current media item is available.
         */
        fun onMetadataChanged(metadata: AudioMetadata)
    }

    /**
     * Data class to hold basic audio metadata.
     */
    data class AudioMetadata(
        val title: String?,
        val album: String?,
        val artist: String?,
        val artwork: Any?,
        val duration: Long
    )
}
