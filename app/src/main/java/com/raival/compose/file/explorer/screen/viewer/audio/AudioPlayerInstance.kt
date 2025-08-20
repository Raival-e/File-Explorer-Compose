package com.raival.compose.file.explorer.screen.viewer.audio

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.ui.res.stringResource
import androidx.media3.common.C.TIME_UNSET
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioMetadata
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioPlayerColorScheme
import com.raival.compose.file.explorer.screen.viewer.audio.model.PlayerState
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import com.raival.compose.file.explorer.screen.viewer.audio.model.PlaylistState
import com.raival.compose.file.explorer.screen.viewer.audio.ui.extractColorsFromBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _playlistState = MutableStateFlow(PlaylistState())
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()


    private val playlistManager = PlaylistManager.getInstance()

    private var exoPlayer: ExoPlayer? = null
    private var positionTrackingJob: Job? = null

    @OptIn(UnstableApi::class)
    suspend fun initializePlayer(context: Context, uri: Uri) {
        withContext(Dispatchers.Main) {
            // Release previous player if it exists
            exoPlayer?.let { player ->
                player.stop()
                player.release()
            }
            
            // Reset player state when changing songs
            _playerState.update { 
                it.copy(
                    currentPosition = 0L,
                    duration = 0L,
                    isLoading = true
                )
            }
            
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .build()

                setMediaItem(mediaItem)
                prepare()

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update {
                            it.copy(isPlaying = isPlaying)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerState.update {
                            it.copy(
                                isLoading = playbackState == Player.STATE_BUFFERING
                            )
                        }

                        if (playbackState == Player.STATE_READY) {
                            _playerState.update {
                                it.copy(
                                    duration = duration
                                )
                            }
                        }

                        // Handle automatic progression to next song
                        if (playbackState == Player.STATE_ENDED) {
                            handleSongEnded()
                        }
                    }
                })
            }
        }

        extractMetadata(context, uri)
        startPositionTracking()
    }

    // Overloaded method for better metadata extraction from LocalFileHolder
    suspend fun initializePlayer(context: Context, uri: Uri, fileHolder: LocalFileHolder? = null) {
        withContext(Dispatchers.Main) {
            // Release previous player if it exists
            exoPlayer?.let { player ->
                player.stop()
                player.release()
            }
            
            // Reset player state when changing songs
            _playerState.update { 
                it.copy(
                    currentPosition = 0L,
                    duration = 0L,
                    isLoading = true
                )
            }
            
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .build()

                setMediaItem(mediaItem)
                prepare()

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _playerState.update {
                            it.copy(isPlaying = isPlaying)
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playerState.update {
                            it.copy(
                                isLoading = playbackState == Player.STATE_BUFFERING
                            )
                        }

                        if (playbackState == Player.STATE_READY) {
                            _playerState.update {
                                it.copy(
                                    duration = duration
                                )
                            }
                        }

                        // Handle automatic progression to next song
                        if (playbackState == Player.STATE_ENDED) {
                            handleSongEnded()
                        }
                    }
                })
            }
        }

        extractMetadata(context, uri, fileHolder)
        startPositionTracking()
    }

    fun setDefaultColorScheme(colorScheme: AudioPlayerColorScheme) {
        _colorScheme.value = colorScheme
    }

    private suspend fun extractMetadata(context: Context, uri: Uri, fileHolder: LocalFileHolder? = null) {
        withContext(Dispatchers.IO) {
            try {
                val retriever = MediaMetadataRetriever()

                // Try to use file path first if available, then URI
                if (fileHolder != null) {
                    retriever.setDataSource(fileHolder.file.absolutePath)
                } else {
                    retriever.setDataSource(context, uri)
                }

                val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: (fileHolder?.displayName?.substringBeforeLast('.')
                        ?: uri.lastPathSegment?.substringBeforeLast('.')
                        ?: globalClass.getString(R.string.unknown_title))

                val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: globalClass.getString(R.string.unknown_artist)

                val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                    ?: globalClass.getString(R.string.unknown_album)

                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
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
                // Fallback metadata using file name from LocalFileHolder or URI
                val fileName = fileHolder?.displayName ?: uri.lastPathSegment ?: "Unknown"
                val title = fileName.substringBeforeLast('.').ifEmpty { fileName }

                _metadata.value = AudioMetadata(
                    title = title,
                    artist = globalClass.getString(R.string.unknown_artist),
                    album = globalClass.getString(R.string.unknown_album)
                )
            }
        }
    }

    private fun handleSongEnded() {
        val currentState = _playlistState.value
        when (_playerState.value.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                // Repeat current song
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            Player.REPEAT_MODE_ALL -> {
                if (currentState.hasNextSong()) {
                    skipToNext()
                } else {
                    // Go back to first song if we've reached the end
                    stopCurrentPlayer()
                    _playlistState.update { it.copy(currentSongIndex = 0) }
                    val firstSong = _playlistState.value.getCurrentSong()
                    firstSong?.let { song ->
                        CoroutineScope(Dispatchers.Main).launch {
                            val songUri = Uri.fromFile(song.file)
                            initializePlayer(globalClass, songUri, song)
                        }
                    }
                }
            }
            else -> {
                // Play next song if available
                if (currentState.hasNextSong()) {
                    skipToNext()
                }
            }
        }
    }

    private fun startPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                exoPlayer?.let { player ->
                    _playerState.update {
                        it.copy(
                            currentPosition = player.currentPosition,
                            duration = player.duration.takeIf { it isNot TIME_UNSET } ?: 0L
                        )
                    }
                }
                delay(100)
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
        // If we have a playlist loaded, use playlist navigation
        if (_playlistState.value.currentPlaylist != null) {
            skipToNext()
        } else {
            // Fallback to original ExoPlayer navigation
            exoPlayer?.seekToNext()
        }
    }

    fun skipPrevious() {
        // If we have a playlist loaded, use playlist navigation
        if (_playlistState.value.currentPlaylist != null) {
            skipToPrevious()
        } else {
            // Fallback to original ExoPlayer navigation
            exoPlayer?.seekToPrevious()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _playerState.update { it.copy(playbackSpeed = speed) }
    }

    fun toggleRepeatMode() {
        val newMode = when (_playerState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = newMode
        _playerState.update { it.copy(repeatMode = newMode) }
    }

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume
        _playerState.update { it.copy(volume = volume) }
    }

    fun toggleEqualizer() {
        _isEqualizerVisible.value = !_isEqualizerVisible.value
    }

    fun toggleVolume() {
        _isVolumeVisible.value = !_isVolumeVisible.value
    }

    // Playlist management methods
    fun loadPlaylist(playlist: Playlist, startIndex: Int = 0) {
        if (playlist.isEmpty()) return

        // Stop current playback first
        stopCurrentPlayer()

        val shuffledIndices = if (_playlistState.value.isShuffled) {
            playlist.songs.indices.shuffled()
        } else {
            emptyList()
        }

        _playlistState.update {
            it.copy(
                currentPlaylist = playlist,
                currentSongIndex = startIndex,
                shuffledIndices = shuffledIndices
            )
        }

        playlistManager.setCurrentPlaylist(playlist)

        // Load the first song
        val songToPlay = _playlistState.value.getCurrentSong()
        songToPlay?.let { song ->
            CoroutineScope(Dispatchers.Main).launch {
                val songUri = Uri.fromFile(song.file)
                initializePlayer(globalClass, songUri, song)
            }
        }
    }

    fun skipToNext() {
        val currentState = _playlistState.value
        currentState.currentPlaylist?.let { _ ->
            if (currentState.hasNextSong()) {
                // Stop current playback first
                stopCurrentPlayer()
                
                val nextIndex = currentState.currentSongIndex + 1
                _playlistState.update { it.copy(currentSongIndex = nextIndex) }

                val nextSong = _playlistState.value.getCurrentSong()
                nextSong?.let { song ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val songUri = Uri.fromFile(song.file)
                        initializePlayer(globalClass, songUri, song)
                    }
                }
            }
        }
    }

    fun skipToPrevious() {
        val currentState = _playlistState.value
        currentState.currentPlaylist?.let { _ ->
            if (currentState.hasPreviousSong()) {
                // Stop current playback first
                stopCurrentPlayer()
                
                val previousIndex = currentState.currentSongIndex - 1
                _playlistState.update { it.copy(currentSongIndex = previousIndex) }

                val previousSong = _playlistState.value.getCurrentSong()
                previousSong?.let { song ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val songUri = Uri.fromFile(song.file)
                        initializePlayer(globalClass, songUri, song)
                    }
                }
            }
        }
    }

    fun skipToSong(index: Int) {
        val currentState = _playlistState.value
        currentState.currentPlaylist?.let { playlist ->
            val actualIndex = if (currentState.isShuffled && currentState.shuffledIndices.isNotEmpty()) {
                currentState.shuffledIndices.getOrNull(index) ?: index
            } else {
                index
            }

            if (actualIndex in 0 until playlist.songs.size) {
                // Stop current playback first
                stopCurrentPlayer()
                
                _playlistState.update { it.copy(currentSongIndex = index) }

                val song = playlist.songs[actualIndex]
                CoroutineScope(Dispatchers.Main).launch {
                    val songUri = Uri.fromFile(song.file)
                    initializePlayer(globalClass, songUri, song)
                }
            }
        }
    }

    fun toggleShuffle() {
        val currentState = _playlistState.value
        currentState.currentPlaylist?.let { playlist ->
            val newShuffledState = !currentState.isShuffled
            val shuffledIndices = if (newShuffledState) {
                playlist.songs.indices.shuffled()
            } else {
                emptyList()
            }

            _playlistState.update {
                it.copy(
                    isShuffled = newShuffledState,
                    shuffledIndices = shuffledIndices,
                    currentSongIndex = 0 // Reset to first song when toggling shuffle
                )
            }
        }
    }

    fun clearPlaylist() {
        _playlistState.update { PlaylistState() }
        playlistManager.clearCurrentPlaylist()
    }

    private fun stopCurrentPlayer() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
        }
    }

    override fun onClose() {
        positionTrackingJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
        clearPlaylist()
    }

    fun initializePlaylistMode(playlist: Playlist, startIndex: Int = 0) {
        loadPlaylist(playlist, startIndex)
    }
}