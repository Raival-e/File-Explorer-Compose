package com.raival.compose.file.explorer.screen.viewer.audio.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.viewer.audio.AudioPlayerInstance
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import com.raival.compose.file.explorer.screen.viewer.audio.model.PlaylistState

@Composable
fun PlaylistDetailBottomSheet(
    isVisible: Boolean,
    playlist: Playlist,
    onDismiss: () -> Unit,
    onPlaySong: (Int) -> Unit,
    audioPlayerInstance: AudioPlayerInstance
) {
    if (isVisible) {
        val playlistManager = remember { PlaylistManager.getInstance() }
        val playlistState by audioPlayerInstance.playlistState.collectAsState()
        val currentPlaylist = playlistState.currentPlaylist
        val isCurrentPlaylist = currentPlaylist?.id == playlist.id
        BottomSheetDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                PlaylistHeader(playlist, isCurrentPlaylist, audioPlayerInstance, playlistState, onDismiss)
                Spacer(modifier = Modifier.height(16.dp))

                if (playlist.songs.isNotEmpty()) {
                    PlayAllButton(playlist, audioPlayerInstance, onDismiss)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (playlist.songs.isEmpty()) {
                    EmptyPlaylistCard()
                } else {
                    PlaylistSongsList(playlist, playlistManager, onPlaySong, isCurrentPlaylist, playlistState)
                }
            }
        }
    }
}

@Composable
fun PlaylistHeader(
    playlist: Playlist,
    isCurrentPlaylist: Boolean,
    audioPlayerInstance: AudioPlayerInstance,
    playlistState: PlaylistState,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.size()} ${stringResource(R.string.song)}${if (playlist.size() != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row {
            if (playlist.size() > 1) {
                ShuffleButton(playlist, audioPlayerInstance, isCurrentPlaylist, playlistState)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        }
    }
}

@Composable
fun ShuffleButton(playlist: Playlist, audioPlayerInstance: AudioPlayerInstance, isCurrentPlaylist: Boolean, playlistState: PlaylistState) {
    IconButton(
        onClick = {
            audioPlayerInstance.loadPlaylist(playlist, 0)
            audioPlayerInstance.toggleShuffle()
            audioPlayerInstance.playPause()
        }
    ) {
        Icon(
            Icons.Default.Shuffle,
            contentDescription = stringResource(R.string.shuffle_mode),
            tint = if (isCurrentPlaylist && playlistState.isShuffled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
fun PlayAllButton(playlist: Playlist, audioPlayerInstance: AudioPlayerInstance, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                audioPlayerInstance.loadPlaylist(playlist, 0)
                audioPlayerInstance.playPause()
                onDismiss()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.play_all),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun EmptyPlaylistCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_playlist),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlaylistSongsList(
    playlist: Playlist,
    playlistManager: PlaylistManager,
    onPlaySong: (Int) -> Unit,
    isCurrentPlaylist: Boolean,
    playlistState: PlaylistState
) {
    LazyColumn {
        itemsIndexed(playlist.songs) { index, song ->
            PlaylistSongItem(
                song = song,
                index = index,
                isCurrentSong = isCurrentPlaylist && playlistState.currentSongIndex == index,
                onSongClick = {
                    onPlaySong(index)
                },
                onRemoveClick = {
                    playlistManager.removeSongFromPlaylistAt(playlist.id, index)
                }
            )
        }
    }
}

@Composable
fun PlaylistSongItem(
    song: LocalFileHolder,
    index: Int,
    isCurrentSong: Boolean,
    onSongClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onSongClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSong) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongIndicator(isCurrentSong = isCurrentSong, index = index)

            Spacer(modifier = Modifier.width(12.dp))

            SongDetails(song = song, isCurrentSong = isCurrentSong)

            RemoveButton(onRemoveClick)
        }
    }
}

@Composable
fun SongIndicator(isCurrentSong: Boolean, index: Int) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(
                if (isCurrentSong) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCurrentSong) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SongDetails(song: LocalFileHolder, isCurrentSong: Boolean, modifier: Modifier = Modifier ) {
    Column(modifier = modifier) {
        Text(
            text = song.displayName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrentSong) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isCurrentSong) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        Text(
            text = song.file.name ?: stringResource(R.string.unknown),
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrentSong) {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RemoveButton(onRemoveClick: () -> Unit) {
    IconButton(onClick = onRemoveClick) {
        Icon(
            Icons.Default.Delete,
            contentDescription = stringResource(R.string.remove_from_playlist),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
    }
}
