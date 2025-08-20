package com.raival.compose.file.explorer.screen.viewer.audio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist

@Composable
fun PlaylistBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    selectedSong: LocalFileHolder? = null,
    selectedSongs: List<LocalFileHolder> = emptyList()
) {
    if (isVisible) {
        val playlistManager = remember { PlaylistManager.getInstance() }
        val playlists by playlistManager.playlists.collectAsState()
        var showCreateDialog by remember { mutableStateOf(false) }
        
        val songsToAdd = when {
            selectedSongs.isNotEmpty() -> selectedSongs
            selectedSong != null -> listOf(selectedSong)
            else -> emptyList()
        }
        val isMultipleSongs = songsToAdd.size > 1

        BottomSheetDialog(onDismissRequest = onDismiss) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMultipleSongs) {
                            "${stringResource(R.string.add_multiple_to_playlist)} (${songsToAdd.size})"
                        } else {
                            stringResource(R.string.playlists)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_playlist))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (playlists.isEmpty()) {
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
                                text = stringResource(R.string.no_playlists_created),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text =stringResource(R.string.tap_to_create_first_playlist),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn {
                        items(playlists) { playlist ->
                            PlaylistItem(
                                playlist = playlist,
                                onPlaylistClick = {
                                    if (songsToAdd.isNotEmpty()) {
                                        if (isMultipleSongs) {
                                            val addedCount = playlistManager.addMultipleSongsToPlaylist(playlist.id, songsToAdd)
                                            val duplicateCount = songsToAdd.size - addedCount
                                            
                                            if (duplicateCount > 0) {
                                                globalClass.showMsg(
                                                    globalClass.getString(R.string.songs_added_with_duplicates, addedCount, duplicateCount)
                                                )
                                            } else {
                                                globalClass.showMsg(
                                                    globalClass.getString(R.string.songs_added_to_playlist, addedCount)
                                                )
                                            }
                                        } else {
                                            val wasAdded = playlistManager.addSongToPlaylist(playlist.id, songsToAdd.first())
                                            if (wasAdded) {
                                                globalClass.showMsg(R.string.song_added_to_playlist)
                                            } else {
                                                globalClass.showMsg(R.string.song_already_in_playlist)
                                            }
                                        }
                                    }
                                    onPlaylistSelected(playlist)
                                },
                                onDeleteClick = {
                                    playlistManager.deletePlaylist(playlist.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreateDialog = false },
                onPlaylistCreated = { name ->
                    val newPlaylist = if (songsToAdd.isNotEmpty()) {
                        if (isMultipleSongs) {
                            val playlist = playlistManager.createPlaylist(name)
                            val addedCount = playlistManager.addMultipleSongsToPlaylist(playlist.id, songsToAdd)
                            val duplicateCount = songsToAdd.size - addedCount
                            
                            if (duplicateCount > 0) {
                                globalClass.showMsg(
                                    globalClass.getString(R.string.songs_added_with_duplicates, addedCount, duplicateCount)
                                )
                            } else {
                                globalClass.showMsg(
                                    globalClass.getString(R.string.songs_added_to_playlist, addedCount)
                                )
                            }
                            playlist
                        } else {
                            val playlist = playlistManager.createPlaylistWithSong(name, songsToAdd.first())
                            globalClass.showMsg(R.string.song_added_to_playlist)
                            playlist
                        }
                    } else {
                        playlistManager.createPlaylist(name)
                    }
                    onPlaylistSelected(newPlaylist)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun PlaylistItem(
    playlist: Playlist,
    onPlaylistClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPlaylistClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.size()} ${stringResource(R.string.song)}${if (playlist.size() != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_playlist),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onPlaylistCreated: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_playlist)) },
        text = {
            Column {
                Text(stringResource(R.string.playlist_name))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text(stringResource(R.string.enter_playlist_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onPlaylistCreated(playlistName.trim())
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
