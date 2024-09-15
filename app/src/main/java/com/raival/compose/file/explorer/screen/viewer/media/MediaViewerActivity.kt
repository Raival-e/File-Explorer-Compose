package com.raival.compose.file.explorer.screen.viewer.media

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_GET_METADATA
import androidx.media3.common.Player.Listener
import androidx.media3.ui.PlayerView
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.name
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.compose.RotatingDisk
import com.raival.compose.file.explorer.screen.viewer.media.instance.MediaViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.modal.MediaSource
import com.raival.compose.file.explorer.ui.theme.FileExplorerTheme
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class MediaViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return MediaViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        if (instance is MediaViewerInstance) {
            setContent {
                FileExplorerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colorScheme.background
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LaunchedEffect(Unit) {
                                instance.player.play()
                            }

                            AndroidView(
                                modifier = Modifier
                                    .fillMaxSize(),
                                factory = { context ->
                                    PlayerView(context).apply {
                                        useController =
                                            instance.mediaSource is MediaSource.VideoSource
                                        player = instance.player.apply {
                                            repeatMode = Player.REPEAT_MODE_ONE
                                        }
                                    }
                                },
                                update = { },
                                onRelease = {
                                    it.player?.release()
                                }
                            )

                            if (instance.mediaSource is MediaSource.AudioSource) {
                                var isPlaying by remember { mutableStateOf(false) }
                                var title by remember { mutableStateOf("") }
                                var artist by remember { mutableStateOf("") }
                                var album by remember { mutableStateOf("") }
                                var currentTime by remember { mutableLongStateOf(0L) }
                                var duration by remember { mutableLongStateOf(0L) }
                                var isDraggingSlider by remember { mutableStateOf(false) }
                                var manualSeek by remember { mutableFloatStateOf(0f) }

                                fun updateAudioDetails() {
                                    title = instance.player.mediaMetadata.title?.toString() ?: ""
                                    artist = instance.player.mediaMetadata.artist?.toString() ?: ""
                                    album =
                                        instance.player.mediaMetadata.albumTitle?.toString() ?: ""
                                }

                                LaunchedEffect(isPlaying) {
                                    instance.player.addListener(object : Listener {
                                        override fun onPlaybackStateChanged(playbackState: Int) {
                                            super.onPlaybackStateChanged(playbackState)
                                            if (playbackState == Player.STATE_READY) {
                                                if (!isPlaying) {
                                                    isPlaying = true
                                                }
                                            }
                                        }

                                        override fun onIsPlayingChanged(playing: Boolean) {
                                            isPlaying = playing
                                        }

                                        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
                                            if (availableCommands.containsAny(COMMAND_GET_METADATA)) {
                                                updateAudioDetails()
                                            }

                                            if (availableCommands.contains(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
                                                duration = instance.player.duration
                                            }
                                        }
                                    })

                                    while (isPlaying) {
                                        currentTime = instance.player.currentPosition

                                        if (duration == 0L) {
                                            duration = instance.player.duration
                                            updateAudioDetails()
                                        }

                                        if (currentTime >= duration) {
                                            currentTime = duration
                                            isPlaying = false
                                        }
                                        delay(1000)
                                    }
                                }

                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .background(colorScheme.surfaceContainer)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box (
                                        Modifier.weight(1f).padding(52.dp),
                                    ) {
                                        RotatingDisk(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .fillMaxSize(),
                                            enabled = isPlaying
                                        )
                                    }

                                    Text(
                                        text = title.ifEmpty {
                                            instance.uri.name ?: getString(R.string.unknown)
                                        },
                                        color = colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Space(12.dp)

                                    Text(
                                        text = artist.ifEmpty { album.ifEmpty { getString(R.string.unknown_artist) } },
                                        color = colorScheme.onSurface,
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
                                            instance.player.seekTo(manualSeek.toLong())
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
                                            color = colorScheme.onSurface,
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
                                            color = colorScheme.onSurface,
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
                                                instance.player.seekTo(max(currentTime - 2000, 0))
                                            }
                                        ) {
                                            Text("-2s")
                                        }

                                        Space(16.dp)

                                        FloatingActionButton(
                                            modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize + 42.dp),
                                            shape = FloatingActionButtonDefaults.largeShape,
                                            onClick = {
                                                isPlaying = !isPlaying

                                                if (isPlaying) {
                                                    instance.player.play()
                                                } else {
                                                    instance.player.pause()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(
                                                    FloatingActionButtonDefaults.LargeIconSize
                                                ),
                                                imageVector = if (!isPlaying) Icons.Rounded.PlayArrow
                                                else Icons.Rounded.Pause,
                                                contentDescription = null
                                            )
                                        }

                                        Space(16.dp)

                                        FloatingActionButton(
                                            onClick = {
                                                instance.player.seekTo(
                                                    min(
                                                        currentTime + 2000,
                                                        duration
                                                    )
                                                )
                                            }
                                        ) {
                                            Text("+2s")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            globalClass.showMsg(getString(R.string.invalid_media_file))
            finish()
        }
    }
}