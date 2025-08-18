package com.raival.compose.file.explorer.screen.playlist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PlaylistManagerScreen(
    onBackPressed: () -> Unit,
    onPlayPlaylist: (Playlist, Int) -> Unit
) {
    val playlistManager = remember { PlaylistManager.getInstance() }
    val playlistState by playlistManager.playlists.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentPlaylist by playlistManager.currentPlaylist.collectAsStateWithLifecycle(initialValue = null)

    var showCreateDialog by remember { mutableStateOf(false) }
    var showPlaylistDetail by remember { mutableStateOf<Playlist?>(null) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val lazyListState = rememberLazyListState()

    val isScrolled = remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0 }
    }

    val fabExpanded by animateFloatAsState(
        targetValue = if (isScrolled.value) 0f else 1f,
        animationSpec = tween(300),
        label = "fabExpanded"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playlists),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    AnimatedVisibility(
                        visible = fabExpanded > 0.5f,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Text(stringResource(R.string.create_playlist))
                    }
                },
                expanded = fabExpanded > 0.5f,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        AnimatedContent(
            targetState = playlistState.isEmpty(),
            label = "ContentAnimation",
            transitionSpec = {
                fadeIn(tween(400)) togetherWith fadeOut(tween(200))
            }
        ) { isEmpty ->
            if (isEmpty) {
                EmptyPlaylistsState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onCreatePlaylist = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = playlistState,
                        key = { it.id }
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            isCurrentlyPlaying = currentPlaylist?.id == playlist.id,
                            modifier = Modifier
                                .animateItem(tween(400))
                                .fillMaxWidth(),
                            onClick = { showPlaylistDetail = playlist },
                            onPlayClick = { onPlayPlaylist(playlist, 0) },
                            onDeleteClick = { playlistManager.deletePlaylist(playlist.id) }
                        )
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                playlistManager.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    // Playlist Detail Sheet
    showPlaylistDetail?.let { playlist ->
        PlaylistDetailSheet(
            playlist = playlist,
            onDismiss = { showPlaylistDetail = null },
            onPlayClick = { index -> onPlayPlaylist(playlist, index) }
        )
    }
}

@Composable
private fun EmptyPlaylistsState(
    modifier: Modifier = Modifier,
    onCreatePlaylist: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.no_playlists),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.no_playlists_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onCreatePlaylist,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.create_playlist),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isCurrentlyPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val cardColor = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            draggedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isCurrentlyPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
                contentColor = if (isCurrentlyPlaying) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = isCurrentlyPlaying,
                        label = "PlayingIconAnimation",
                        transitionSpec = {
                            fadeIn(tween(400)) + scaleIn(tween(400)) togetherWith
                                    fadeOut(tween(200)) + scaleOut(tween(200))
                        }
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) {
                                Icons.Default.GraphicEq
                            } else {
                                Icons.AutoMirrored.Filled.QueueMusic
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = stringResource(R.string.songs_count, playlist.songs.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play button
                Surface(
                    onClick = onPlayClick,
                    enabled = playlist.songs.isNotEmpty(),
                    shape = CircleShape,
                    color = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    contentColor = if (isCurrentlyPlaying) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isCurrentlyPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (isCurrentlyPlaying) {
                                stringResource(R.string.pause)
                            } else {
                                stringResource(R.string.play)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Delete button
                Surface(
                    onClick = { showDeleteConfirmation = true },
                    shape = CircleShape,
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_playlist),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_playlist_confirmation, playlist.name),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete).uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(
                        text = stringResource(R.string.cancel).uppercase(),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    val density = LocalDensity.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.create_playlist),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.enter_playlist_name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text(stringResource(R.string.playlist_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(playlistName.trim()) },
                enabled = playlistName.trim().isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.create).uppercase(),
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}