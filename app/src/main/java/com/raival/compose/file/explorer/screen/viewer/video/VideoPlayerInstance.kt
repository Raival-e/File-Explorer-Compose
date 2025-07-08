package com.raival.compose.file.explorer.screen.viewer.video

import android.content.Context
import android.net.Uri
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.name
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.video.model.VideoPlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoPlayerInstance(
    override val uri: Uri,
    override val id: String
) : ViewerInstance {
    private val _playerState = MutableStateFlow(VideoPlayerState())
    val playerState: StateFlow<VideoPlayerState> = _playerState.asStateFlow()
    private var exoPlayer: ExoPlayer? = null
    private var positionTrackingJob: Job? = null

    suspend fun initializePlayer(context: Context, uri: Uri) {
        _playerState.value = _playerState.value.copy(isLoading = true)

        withContext(Dispatchers.Main) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .build()

                setMediaItem(mediaItem)
                prepare()

                volume = 0f

                _playerState.value = _playerState.value.copy(
                    title = uri.name ?: globalClass.getString(R.string.unknown)
                )

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerState.value = _playerState.value.copy(
                            isLoading = playbackState == Player.STATE_BUFFERING
                        )

                        if (playbackState == Player.STATE_READY) {
                            _playerState.value = _playerState.value.copy(
                                duration = duration,
                                isLoading = false
                            )
                        }
                    }
                })
            }
        }
        startPositionTracking()
    }

    private fun startPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                exoPlayer?.let { player ->
                    _playerState.value = _playerState.value.copy(
                        currentPosition = player.currentPosition,
                        duration = player.duration.takeIf { it != TIME_UNSET } ?: 0L
                    )
                }
                delay(1000)
            }
        }
    }

    fun playPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun toggleMute() {
        exoPlayer?.let { player ->
            val currentVolume = player.volume
            val newVolume = if (currentVolume > 0f) 0f else 1f
            player.volume = newVolume
            _playerState.value = _playerState.value.copy(isMuted = newVolume == 0f)
        }
    }

    fun toggleControls() {
        _playerState.value =
            _playerState.value.copy(showControls = !_playerState.value.showControls)
    }

    fun toggleRepeatMode() {
        val newMode = when (_playerState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = newMode
        _playerState.value = _playerState.value.copy(repeatMode = newMode)
    }

    fun getPlayer() = exoPlayer

    override fun onClose() {
        positionTrackingJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}