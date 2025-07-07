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
 * A video player manager using Media3 ExoPlayer.
 * Provides video playback controls and state management.
 */
class VideoPlayerManager(context: Context) {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var listener: VideoPlayerManagerListener? = null

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
    fun setListener(listener: VideoPlayerManagerListener) {
        this.listener = listener
    }

    /**
     * Prepares the player with the given video URI.
     */
    fun prepare(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()

        // Listen for playback and metadata events.
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val isPlaying = exoPlayer.playWhenReady && state == Player.STATE_READY
                val isLoading = state == Player.STATE_BUFFERING
                listener?.onPlaybackStateChanged(isPlaying, isLoading)
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                val title = mediaMetadata.title?.toString()
                val duration = exoPlayer.duration.takeIf { it > 0 } ?: 0L

                val metadata = VideoMetadata(title, duration)
                listener?.onMetadataChanged(metadata)
            }
        })
    }

    /**
     * Gets the ExoPlayer instance for video rendering.
     */
    fun getPlayer(): ExoPlayer = exoPlayer

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
     * Fast-forwards playback by 10 seconds.
     */
    fun forward() {
        val newPosition = exoPlayer.currentPosition + 10000L
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Rewinds playback by 10 seconds.
     */
    fun backward() {
        val newPosition = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
        exoPlayer.seekTo(newPosition)
    }

    /**
     * Seek to a new position
     */
    fun seekTo(position: Long) {
        val newPosition = position.coerceIn(0, exoPlayer.duration)
        exoPlayer.seekTo(newPosition)
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
    interface VideoPlayerManagerListener {
        /**
         * Called when playback state changes.
         * @param isPlaying true if the video is playing.
         * @param isLoading true if the video is buffering/loading.
         */
        fun onPlaybackStateChanged(isPlaying: Boolean, isLoading: Boolean)

        /**
         * Called periodically with the current position and remaining time (in milliseconds).
         */
        fun onProgressUpdated(currentPosition: Long, remainingTime: Long)

        /**
         * Called when metadata for the current media item is available.
         */
        fun onMetadataChanged(metadata: VideoMetadata)
    }

    /**
     * Data class to hold basic video metadata.
     */
    data class VideoMetadata(
        val title: String?,
        val duration: Long
    )
}