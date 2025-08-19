@file:OptIn(ExperimentalMaterial3Api::class)

package com.raival.compose.file.explorer.screen.viewer.audio.ui

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.toFormattedTime
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.viewer.audio.AudioPlayerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioMetadata
import com.raival.compose.file.explorer.screen.viewer.audio.model.AudioPlayerColorScheme
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import kotlin.math.abs

@Composable
fun MusicPlayerScreen(
    audioPlayerInstance: AudioPlayerInstance,
    onClosed: () -> Unit
) {
    val context = LocalContext.current
    val playerState by audioPlayerInstance.playerState.collectAsState()
    val metadata by audioPlayerInstance.metadata.collectAsState()
    val isEqualizerVisible by audioPlayerInstance.isEqualizerVisible.collectAsState()
    val isVolumeVisible by audioPlayerInstance.isVolumeVisible.collectAsState()
    val customColorScheme by audioPlayerInstance.audioPlayerColorScheme.collectAsState()
    val playlistState by audioPlayerInstance.playlistState.collectAsState()
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var showPlaylistDetailDialog by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    
    val defaultScheme = AudioPlayerColorScheme(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.secondary,
        background = MaterialTheme.colorScheme.background,
        surface = MaterialTheme.colorScheme.surface
    )

    // Initialize player
    LaunchedEffect(audioPlayerInstance.uri) {
        audioPlayerInstance.setDefaultColorScheme(defaultScheme)
        audioPlayerInstance.initializePlayer(context, audioPlayerInstance.uri)
    }

    // Dispose player when leaving
    DisposableEffect(Unit) {
        onDispose { audioPlayerInstance.onClose() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = customColorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            customColorScheme.background,
                            customColorScheme.surface,
                            customColorScheme.background.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status bar spacing
                Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))

                // Top controls
                TopControls(
                    onEqualizerClick = { audioPlayerInstance.toggleEqualizer() },
                    onVolumeClick = { audioPlayerInstance.toggleVolume() },
                    onPlaylistClick = { showPlaylistDialog = true },
                    onCloseClick = onClosed,
                    audioPlayerColorScheme = customColorScheme
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Album art with rotation animation
                AlbumArt(
                    isPlaying = playerState.isPlaying,
                    metadata = metadata,
                    audioPlayerColorScheme = customColorScheme
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Song info
                SongInfo(metadata = metadata, colorScheme = customColorScheme)

                Spacer(modifier = Modifier.height(24.dp))

                // Progress bar
                ProgressBar(
                    currentPosition = playerState.currentPosition,
                    duration = playerState.duration,
                    onSeek = { audioPlayerInstance.seekTo(it) },
                    colorScheme = customColorScheme
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Main controls
                MainControls(
                    isPlaying = playerState.isPlaying,
                    isLoading = playerState.isLoading,
                    onPlayPause = { audioPlayerInstance.playPause() },
                    onSkipNext = { audioPlayerInstance.skipNext() },
                    onSkipPrevious = { audioPlayerInstance.skipPrevious() },
                    colorScheme = customColorScheme
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Additional controls
                AdditionalControls(
                    playbackSpeed = playerState.playbackSpeed,
                    repeatMode = playerState.repeatMode,
                    onSpeedChange = { audioPlayerInstance.setPlaybackSpeed(it) },
                    onRepeatToggle = { audioPlayerInstance.toggleRepeatMode() },
                    colorScheme = customColorScheme
                )

                // Current playlist info
                playlistState.currentPlaylist?.let { playlist ->
                    Spacer(modifier = Modifier.height(16.dp))
                    CurrentPlaylistInfo(
                        playlist = playlist,
                        currentSongIndex = playlistState.currentSongIndex,
                        isShuffled = playlistState.isShuffled,
                        onShuffleToggle = { audioPlayerInstance.toggleShuffle() },
                        onPlaylistClick = {
                            selectedPlaylist = playlist
                            showPlaylistDetailDialog = true
                        },
                        colorScheme = customColorScheme
                    )
                }
            }

            // Volume overlay
            AnimatedVisibility(
                visible = isVolumeVisible,
                enter = slideInVertically { -it } + fadeIn() + scaleIn(initialScale = 0.6f),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                VolumeView(
                    volume = playerState.volume,
                    onVolumeChange = { audioPlayerInstance.setVolume(it) },
                    onDismiss = { audioPlayerInstance.toggleVolume() },
                    colorScheme = customColorScheme
                )
            }

            // Equalizer overlay
            AnimatedVisibility(
                visible = isEqualizerVisible,
                enter = slideInVertically { -it } + fadeIn() + scaleIn(initialScale = 0.6f),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                EqualizerView(
                    onDismiss = { audioPlayerInstance.toggleEqualizer() },
                    colorScheme = customColorScheme
                )
            }
        }

        // Playlist dialogs
        PlaylistBottomSheet(
            isVisible = showPlaylistDialog,
            onDismiss = { showPlaylistDialog = false },
            onPlaylistSelected = { playlist ->
                selectedPlaylist = playlist
                showPlaylistDialog = false
                showPlaylistDetailDialog = true
            }
        )

        selectedPlaylist?.let { playlist ->
            PlaylistDetailBottomSheet(
                isVisible = showPlaylistDetailDialog,
                playlist = playlist,
                onDismiss = { showPlaylistDetailDialog = false },
                onPlaySong = { index -> },
                audioPlayerInstance = audioPlayerInstance
            )
        }
    }
}

@Composable
fun TopControls(
    onEqualizerClick: () -> Unit,
    onVolumeClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onCloseClick: () -> Unit,
    audioPlayerColorScheme: AudioPlayerColorScheme,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = audioPlayerColorScheme.tintColor
            )
        }

        Spacer(Modifier.weight(1f))

        Row {
            IconButton(onClick = onPlaylistClick) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Playlists",
                    tint = audioPlayerColorScheme.tintColor
                )
            }

            IconButton(onClick = onVolumeClick) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = audioPlayerColorScheme.tintColor
                )
            }

            IconButton(onClick = onEqualizerClick) {
                Icon(
                    Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = audioPlayerColorScheme.tintColor
                )
            }
        }
    }
}

@Composable
fun AlbumArt(
    isPlaying: Boolean,
    metadata: AudioMetadata,
    audioPlayerColorScheme: AudioPlayerColorScheme
) {
    RotatingContainer(isRotating = isPlaying) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            audioPlayerColorScheme.primary.copy(alpha = 0.6f),
                            audioPlayerColorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (metadata.albumArt != null) {
                Image(
                    bitmap = metadata.albumArt.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun RotatingContainer(
    modifier: Modifier = Modifier,
    isRotating: Boolean,
    rotationDuration: Int = 10000,
    content: @Composable () -> Unit
) {
    var currentRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation = remember { Animatable(0f) }

    LaunchedEffect(isRotating) {
        if (isRotating) {
            animatedRotation.snapTo(currentRotation)

            while (isRotating) {
                animatedRotation.animateTo(
                    targetValue = currentRotation + 360f,
                    animationSpec = tween(
                        durationMillis = rotationDuration,
                        easing = LinearEasing
                    )
                )
                currentRotation = animatedRotation.value % 360f
                animatedRotation.snapTo(currentRotation)
            }
        } else {
            currentRotation = animatedRotation.value % 360f
            animatedRotation.stop()
        }
    }

    LaunchedEffect(isRotating, animatedRotation.value) {
        if (isRotating) {
            currentRotation = animatedRotation.value % 360f
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = if (isRotating) animatedRotation.value else currentRotation
            }
    ) {
        content()
    }
}

@Composable
fun SongInfo(metadata: AudioMetadata, colorScheme: AudioPlayerColorScheme) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = metadata.title,
            color = colorScheme.tintColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = metadata.artist,
            color = colorScheme.tintColor.copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = metadata.album,
            color = colorScheme.tintColor.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ProgressBar(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    var manualPosition by remember { mutableLongStateOf(0L) }
    var manualSeek by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val progress = if (duration > 0) {
        if (abs(currentPosition - manualPosition) < 1000) {
            (currentPosition.toFloat() / duration.toFloat()).also {
                manualPosition = currentPosition
            }
        } else manualSeek
    } else 0f

    Column {
        Slider(
            value = if (isDragging) manualSeek else progress,
            onValueChange = {
                isDragging = true
                manualSeek = it
            },
            onValueChangeFinished = {
                (manualSeek * duration).toLong().let { newPosition ->
                    manualPosition = newPosition
                    onSeek(newPosition)
                }
                isDragging = false
            },
            colors = SliderDefaults.colors(
                thumbColor = colorScheme.tintColor,
                activeTrackColor = colorScheme.primary,
                inactiveTrackColor = colorScheme.tintColor.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = (if (isDragging) (manualSeek * duration).toLong() else manualPosition).toFormattedTime(),
                color = colorScheme.tintColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = duration.toFormattedTime(),
                color = colorScheme.tintColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MainControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        IconButton(
            onClick = onSkipPrevious,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = null,
                tint = colorScheme.tintColor,
                modifier = Modifier.size(32.dp)
            )
        }

        // Play/Pause button
        Card(
            modifier = Modifier
                .size(72.dp)
                .clickable { onPlayPause() },
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.primary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = colorScheme.tintColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Next button
        IconButton(
            onClick = onSkipNext,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = null,
                tint = colorScheme.tintColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun AdditionalControls(
    playbackSpeed: Float,
    repeatMode: Int,
    onSpeedChange: (Float) -> Unit,
    onRepeatToggle: () -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Speed control
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = {
                    val newSpeed = when (playbackSpeed) {
                        0.5f -> 1.0f
                        1.0f -> 1.25f
                        1.25f -> 1.5f
                        1.5f -> 2.0f
                        else -> 0.5f
                    }
                    onSpeedChange(newSpeed)
                }
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = colorScheme.tintColor
                )
            }
            Text(
                text = "${playbackSpeed}x",
                color = colorScheme.tintColor.copy(alpha = 0.8f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Repeat control (only two modes: off and all)
        IconButton(onClick = onRepeatToggle) {
            Icon(
                Icons.Default.Repeat,
                contentDescription = null,
                tint = if (repeatMode == Player.REPEAT_MODE_OFF)
                    colorScheme.tintColor.copy(alpha = 0.5f)
                else
                    colorScheme.tintColor,
            )
        }
    }
}

@Composable
fun VolumeView(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.volume),
                    color = colorScheme.tintColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = colorScheme.tintColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = null,
                    tint = colorScheme.tintColor.copy(alpha = 0.7f)
                )

                Space(8.dp)

                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.tintColor,
                        activeTrackColor = colorScheme.primary,
                        inactiveTrackColor = colorScheme.tintColor.copy(alpha = 0.3f)
                    )
                )

                Space(8.dp)

                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = colorScheme.tintColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EqualizerView(
    onDismiss: () -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    val frequencies = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
    val gains = remember { mutableStateListOf(0f, 0f, 0f, 0f, 0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.equalizer),
                    color = colorScheme.tintColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = colorScheme.tintColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                frequencies.forEachIndexed { index, frequency ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+15",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelSmall
                        )

                        Space(8.dp)

                        Slider(
                            value = gains[index],
                            onValueChange = { gains[index] = it },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = colorScheme.tintColor,
                                activeTrackColor = colorScheme.primary,
                                inactiveTrackColor = colorScheme.tintColor.copy(alpha = 0.3f)
                            )
                        )

                        Space(8.dp)

                        Text(
                            text = "-15",
                            color = colorScheme.tintColor.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            style = MaterialTheme.typography.labelSmall
                        )

                        Space(8.dp)

                        Text(
                            text = frequency,
                            color = colorScheme.tintColor,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        for (i in gains.indices) {
                            gains[i] = 0f
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        color = colorScheme.tintColor
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        color = colorScheme.tintColor
                    )
                }
            }
        }
    }
}

// Extract colors from bitmap
fun extractColorsFromBitmap(
    bitmap: Bitmap,
    defaultScheme: AudioPlayerColorScheme
): AudioPlayerColorScheme {
    return try {
        val palette = Palette.from(bitmap).generate()
        val primaryColor = palette.getDominantColor(0xFF6750A4.toInt())
        val vibrantColor = palette.getVibrantColor(primaryColor)
        val mutedColor = palette.getMutedColor(0xFF625B71.toInt())

        // Create darker variants for background
        val primaryHsl = FloatArray(3)
        ColorUtils.colorToHSL(primaryColor, primaryHsl)
        primaryHsl[2] = 0.1f // Very dark
        val backgroundColor = ColorUtils.HSLToColor(primaryHsl)

        primaryHsl[2] = 0.2f // Slightly lighter
        val surfaceColor = ColorUtils.HSLToColor(primaryHsl)

        AudioPlayerColorScheme(
            primary = Color(vibrantColor),
            secondary = Color(mutedColor),
            background = Color(backgroundColor),
            surface = Color(surfaceColor)
        )
    } catch (_: Exception) {
        defaultScheme // Fallback to default colors
    }
}

@Composable
fun CurrentPlaylistInfo(
    playlist: Playlist,
    currentSongIndex: Int,
    isShuffled: Boolean,
    onShuffleToggle: () -> Unit,
    onPlaylistClick: () -> Unit,
    colorScheme: AudioPlayerColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlaylistClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.tintColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${currentSongIndex + 1} de ${playlist.size()}${if (isShuffled) " • Aleatório" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.tintColor.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onShuffleToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    if (isShuffled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                    contentDescription = "Toggle Shuffle",
                    tint = if (isShuffled) colorScheme.primary else colorScheme.tintColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}