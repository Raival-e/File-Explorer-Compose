package com.raival.compose.file.explorer.screen.viewer.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.name
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.viewer.media.instance.MediaViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.misc.AudioPlayerManager
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun AudioPlayer(instance: MediaViewerInstance) {
    var playing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var currentTime by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var manualSeek by remember { mutableFloatStateOf(0f) }
    var artwork by remember { mutableStateOf<Any?>(null) }

    val playerManager = instance.audioManager.apply {
        setListener(object : AudioPlayerManager.AudioPlayerManagerListener {
            override fun onPlaybackStateChanged(isPlaying: Boolean) {
                playing = isPlaying
            }

            override fun onProgressUpdated(currentPosition: Long, remainingTime: Long) {
                currentTime = currentPosition
            }

            override fun onMetadataChanged(metadata: AudioPlayerManager.AudioMetadata) {
                title = metadata.title ?: ""
                artist = metadata.artist ?: ""
                album = metadata.album ?: ""
                duration = metadata.duration
                artwork = metadata.artwork
            }
        })
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .weight(1f)
                .padding(52.dp),
        ) {
            if (artwork != null) {
                AsyncImage(
                    model = artwork,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                )
            } else {
                RotatingDisk(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(),
                    enabled = playing
                )
            }
        }

        Text(
            text = title.ifEmpty {
                instance.uri.name ?: globalClass.getString(R.string.unknown)
            },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Space(12.dp)

        Text(
            text = artist.ifEmpty { album.ifEmpty { globalClass.getString(R.string.unknown_artist) } },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium
        )

        Space(16.dp)

        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = if (isDraggingSlider) manualSeek else currentTime.toFloat(),
            onValueChange = {
                isDraggingSlider = true
                manualSeek = it
            },
            onValueChangeFinished = {
                playerManager.seekTo(manualSeek.toLong())
                currentTime = manualSeek.toLong()
                isDraggingSlider = false
            },
            valueRange = 0f..duration.toFloat()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = buildString {
                    val value =
                        if (isDraggingSlider) manualSeek else currentTime.toFloat()
                    value.toLong().toDuration(DurationUnit.MILLISECONDS)
                        .toComponents { hours, minutes, seconds, nanoseconds ->
                            if (hours > 0) {
                                append("%02d:".format(hours))
                            }
                            append("%02d:".format(minutes))
                            append("%02d".format(seconds))
                        }
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = buildString {
                    duration.toDuration(DurationUnit.MILLISECONDS)
                        .toComponents { hours, minutes, seconds, nanoseconds ->
                            if (hours > 0) {
                                append("%02d:".format(hours))
                            }
                            append("%02d:".format(minutes))
                            append("%02d".format(seconds))
                        }
                },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Space(8.dp)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    playerManager.backward()
                    currentTime - 2
                }
            ) {
                Text("-2s")
            }

            Space(16.dp)

            FloatingActionButton(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize + 42.dp),
                shape = FloatingActionButtonDefaults.largeShape,
                onClick = {
                    playing = !playing

                    if (playing) {
                        playerManager.play()
                    } else {
                        playerManager.pause()
                    }

                    duration = instance.player.duration
                }
            ) {
                Icon(
                    modifier = Modifier.size(
                        FloatingActionButtonDefaults.LargeIconSize
                    ),
                    imageVector = if (!playing) Icons.Rounded.PlayArrow
                    else Icons.Rounded.Pause,
                    contentDescription = null
                )
            }

            Space(16.dp)

            FloatingActionButton(
                onClick = {
                    playerManager.forward()
                    currentTime + 2
                }
            ) {
                Text("+2s")
            }
        }
    }
}