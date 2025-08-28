package com.raival.compose.file.explorer.screen.playlist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SongItem(
    song: LocalFileHolder,
    index: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }

    val elevation by animateDpAsState(
        targetValue = if (isPlaying) 6.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardElevation"
    )

    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isPlaying) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        SongContent(
            song = song,
            index = index,
            isPlaying = isPlaying,
            onPlayClick = onPlayClick,
            onShowDropdownMenu = { showDropdownMenu = true },
            showDropdownMenu = showDropdownMenu,
            onDismissMenu = { showDropdownMenu = false },
            onRemoveClick = { showRemoveConfirmation = true }
        )
    }

    if (showRemoveConfirmation) {
        RemoveConfirmationDialog(
            songName = song.displayName,
            onConfirm = {
                onRemoveClick()
                showRemoveConfirmation = false
            },
            onDismiss = { showRemoveConfirmation = false }
        )
    }
}

@Composable
private fun SongContent(
    song: LocalFileHolder,
    index: Int,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onShowDropdownMenu: () -> Unit,
    showDropdownMenu: Boolean,
    onDismissMenu: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (isPlaying) {
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlayClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SongNumberIndicator(index = index, isPlaying = isPlaying)

                Spacer(modifier = Modifier.width(16.dp))

                SongDetails(song, Modifier.weight(1f))
            }

            SongItemActions(
                isPlaying = isPlaying,
                onPlayClick = onPlayClick,
                onMenuClick = onShowDropdownMenu,
                showMenu = showDropdownMenu,
                onDismissMenu = onDismissMenu,
                onRemoveClick = onRemoveClick
            )
        }
    }
}

@Composable
private fun SongDetails(song: LocalFileHolder, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = song.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Text(
                text = song.basePath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SongNumberIndicator(index: Int, isPlaying: Boolean) {
    AnimatedContent(
        targetState = isPlaying,
        label = "PlayingState",
        transitionSpec = {
            (fadeIn(tween(300)) + scaleIn(tween(300))) togetherWith
                    (fadeOut(tween(150)) + scaleOut(tween(150)))
        }
    ) { playing ->
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (playing) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (playing) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SongItemActions(
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Box {
        Row {
            AnimatedContent(
                targetState = isPlaying,
                label = "PlayButtonState",
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(150))
                }
            ) { playing ->
                PlayPauseButton(playing, onPlayClick)
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismissMenu,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 4.dp)
        ) {
            RemoveFromPlaylistMenuItem(onDismissMenu, onRemoveClick)
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onPlayClick: () -> Unit) {
    IconButton(
        onClick = onPlayClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.Transparent
                }
            )
    ) {
        Icon(
            imageVector = if (isPlaying) {
                Icons.Default.Pause
            } else {
                Icons.Default.PlayArrow
            },
            contentDescription = stringResource(
                if (isPlaying) R.string.pause else R.string.play
            ),
            tint = if (isPlaying) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun RemoveFromPlaylistMenuItem(onDismissMenu: () -> Unit, onRemoveClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.remove_from_playlist),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.PlaylistRemove,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = {
            onDismissMenu()
            onRemoveClick()
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun RemoveConfirmationDialog(
    songName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.remove_song),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.remove_song_confirmation, songName),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.remove).uppercase(),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel).uppercase(),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier.fillMaxWidth(0.9f)
    )
}