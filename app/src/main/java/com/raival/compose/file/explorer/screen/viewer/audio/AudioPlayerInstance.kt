package com.raival.compose.file.explorer.screen.viewer.audio

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioMetadata
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioPlayerColorScheme
import com.raival.compose.file.explorer.screen.viewer.audio.model.PlayerState
import com.raival.compose.file.explorer.screen.viewer.audio.ui.extractColorsFromBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioPlayerInstance(
    override val uri: Uri,
    override val id: String
) : ViewerInstance {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _metadata = MutableStateFlow(AudioMetadata())
    val metadata: StateFlow<AudioMetadata> = _metadata.asStateFlow()

    private val _isEqualizerVisible = MutableStateFlow(false)
    val isEqualizerVisible: StateFlow<Boolean> = _isEqualizerVisible.asStateFlow()

    private val _isVolumeVisible = MutableStateFlow(false)
    val isVolumeVisible: StateFlow<Boolean> = _isVolumeVisible.asStateFlow()

    private val _colorScheme = MutableStateFlow(AudioPlayerColorScheme())
    val audioPlayerColorScheme: StateFlow<AudioPlayerColorScheme> = _colorScheme.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var positionTrackingJob: Job? = null

    @OptIn(UnstableApi::class)
    suspend fun initializePlayer(context: Context, uri: Uri) {
        withContext(Dispatchers.Main) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .build()

                setMediaItem(mediaItem)
                prepare()

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
                                duration = duration
                            )
                        }
                    }
                })
            }
        }

        extractMetadata(context, uri)
        startPositionTracking()
    }

    fun setDefaultColorScheme(colorScheme: AudioPlayerColorScheme) {
        _colorScheme.value = colorScheme
    }

    private suspend fun extractMetadata(context: Context, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)

                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: globalClass.getString(R.string.unknown_title)
                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: globalClass.getString(R.string.unknown_artist)
                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    ?: globalClass.getString(R.string.unknown_album)
                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L

                // Extract album art
                val albumArtData = retriever.embeddedPicture
                val albumArt = albumArtData?.let { data ->
                    BitmapFactory.decodeByteArray(data, 0, data.size)
                }

                val metadata = AudioMetadata(
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    albumArt = albumArt
                )

                _metadata.value = metadata

                // Extract colors from album art if available
                albumArt?.let { bitmap ->
                    val colorScheme = extractColorsFromBitmap(bitmap, _colorScheme.value)
                    _colorScheme.value = colorScheme
                }

                retriever.release()
            } catch (e: Exception) {
                logger.logError(e)
                // Fallback metadata
                _metadata.value = AudioMetadata(
                    title = uri.lastPathSegment ?: globalClass.getString(R.string.unknown_title)
                )
            }
        }
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

    fun skipNext() {
        exoPlayer?.seekToNext()
    }

    fun skipPrevious() {
        exoPlayer?.seekToPrevious()
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
    }

    fun toggleRepeatMode() {
        val newMode = when (_playerState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = newMode
        _playerState.value = _playerState.value.copy(repeatMode = newMode)
    }

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume
        _playerState.value = _playerState.value.copy(volume = volume)
    }

    fun toggleEqualizer() {
        _isEqualizerVisible.value = !_isEqualizerVisible.value
    }

    fun toggleVolume() {
        _isVolumeVisible.value = !_isVolumeVisible.value
    }

    override fun onClose() {
        positionTrackingJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}