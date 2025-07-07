package com.raival.compose.file.explorer.screen.viewer.media.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.raival.compose.file.explorer.common.extension.name
import com.raival.compose.file.explorer.screen.viewer.media.instance.MediaViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.misc.VideoPlayerManager
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun VideoPlayer(
    instance: MediaViewerInstance,
    onBackClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var currentTime by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var manualSeek by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val videoPlayerManager = remember { VideoPlayerManager(context) }

    // Auto-hide controls after 3 seconds
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(3000)
            showControls = false
        }
    }

    // Set up video player manager
    LaunchedEffect(Unit) {
        videoPlayerManager.setListener(object : VideoPlayerManager.VideoPlayerManagerListener {
            override fun onPlaybackStateChanged(playing: Boolean, loading: Boolean) {
                isPlaying = playing
                isLoading = loading
            }

            override fun onProgressUpdated(currentPosition: Long, remainingTime: Long) {
                currentTime = currentPosition
            }

            override fun onMetadataChanged(metadata: VideoPlayerManager.VideoMetadata) {
                title = metadata.title ?: instance.uri.name
                duration = metadata.duration
            }
        })
        
        videoPlayerManager.prepare(instance.uri)
    }

    // Clean up when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            videoPlayerManager.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            }
    ) {
        // Video player view
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    player = videoPlayerManager.getPlayer()
                }
            }
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            VideoControls(
                title = title,
                isPlaying = isPlaying,
                currentTime = currentTime,
                duration = duration,
                isDraggingSlider = isDraggingSlider,
                manualSeek = manualSeek,
                onPlayPause = {
                    if (isPlaying) {
                        videoPlayerManager.pause()
                    } else {
                        videoPlayerManager.play()
                    }
                },
                onBackward = { videoPlayerManager.backward() },
                onForward = { videoPlayerManager.forward() },
                onBackClick = onBackClick,
                onSeekStart = { value ->
                    isDraggingSlider = true
                    manualSeek = value
                },
                onSeekEnd = {
                    videoPlayerManager.seekTo(manualSeek.toLong())
                    currentTime = manualSeek.toLong()
                    isDraggingSlider = false
                }
            )
        }
    }
}

@Composable
private fun VideoControls(
    title: String,
    isPlaying: Boolean,
    currentTime: Long,
    duration: Long,
    isDraggingSlider: Boolean,
    manualSeek: Float,
    onPlayPause: () -> Unit,
    onBackward: () -> Unit,
    onForward: () -> Unit,
    onBackClick: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.3f)
            )
    ) {
        // Top bar with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Spacer(modifier = Modifier.size(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        // Center play controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onBackward,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastRewind,
                    contentDescription = "Rewind 10s",
                    modifier = Modifier.size(28.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }

            FilledIconButton(
                onClick = onForward,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.FastForward,
                    contentDescription = "Forward 10s",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Bottom progress bar and time
        if (duration > 0) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = if (isDraggingSlider) manualSeek else currentTime.toFloat(),
                    onValueChange = onSeekStart,
                    onValueChangeFinished = onSeekEnd,
                    valueRange = 0f..duration.toFloat()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(if (isDraggingSlider) manualSeek.toLong() else currentTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun formatTime(timeMillis: Long): String {
    return timeMillis.toDuration(DurationUnit.MILLISECONDS)
        .toComponents { hours, minutes, seconds, _ ->
            if (hours > 0) {
                "%02d:%02d:%02d".format(hours, minutes, seconds)
            } else {
                "%02d:%02d".format(minutes, seconds)
            }
        }
}