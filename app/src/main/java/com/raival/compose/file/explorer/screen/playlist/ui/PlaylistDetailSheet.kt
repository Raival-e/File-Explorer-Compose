package com.raival.compose.file.explorer.screen.playlist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PlaylistDetailSheet(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onPlayClick: (Int) -> Unit
) {
    // Calculate optimal dialog size based on screen size
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Use 80% of screen width and height, but limit to reasonable values
    val screenWidth = with(density) { configuration.screenWidthDp.dp }
    val screenHeight = with(density) { configuration.screenHeightDp.dp }

    val dialogWidth = min(screenWidth * 0.9f, 480.dp)
    val dialogHeight = min(screenHeight * 0.8f, 650.dp)

    val lazyListState = rememberLazyListState()

    val isScrolled by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0 }
    }

    var currentlyPlayingIndex by remember { mutableIntStateOf(-1) }

    // Simulating current playing track for UI purposes
    LaunchedEffect(playlist) {
        if (playlist.songs.isNotEmpty()) {
            currentlyPlayingIndex = -1
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width(dialogWidth)
                .height(dialogHeight),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Close button in the top right
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Header background that shows when scrolled
                AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {}
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    // Header with playlist info
                    PlaylistHeader(
                        playlist = playlist,
                        isScrolled = isScrolled,
                        onPlayAllClick = { onPlayClick(0) }
                    )

                    // Songs List
                    if (playlist.songs.isEmpty()) {
                        EmptyPlaylistContent()
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            itemsIndexed(
                                items = playlist.songs,
                                key = { _, song -> song.uid }
                            ) { index, song ->
                                val isPlaying = index == currentlyPlayingIndex

                                SongItem(
                                    song = song,
                                    index = index,
                                    isPlaying = isPlaying,
                                    modifier = Modifier
                                        .animateItem(
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            )
                                        )
                                        .fillMaxWidth(),
                                    onPlayClick = {
                                        onPlayClick(index)
                                        currentlyPlayingIndex = if (isPlaying) -1 else index
                                    },
                                    onRemoveClick = {
                                        PlaylistManager.getInstance().removeSongFromPlaylistAt(playlist.id, index)
                                        if (index == currentlyPlayingIndex) {
                                            currentlyPlayingIndex = -1
                                        } else if (index < currentlyPlayingIndex) {
                                            currentlyPlayingIndex--
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom spacing for better visual appearance
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlaylistHeader(
    playlist: Playlist,
    isScrolled: Boolean,
    onPlayAllClick: () -> Unit
) {
    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 0.dp,
        animationSpec = tween(300),
        label = "headerElevation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isScrolled) 0.dp else 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playlist info with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Playlist icon with animation
                Box(
                    modifier = Modifier
                        .size(if (isScrolled) 40.dp else 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(if (isScrolled) 24.dp else 32.dp)
                    )
                }

                // Playlist name and count
                AnimatedContent(
                    targetState = isScrolled,
                    label = "HeaderTextAnimation",
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(150))
                    }
                ) { scrolled ->
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = playlist.name,
                            style = if (scrolled) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.headlineSmall
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = stringResource(
                                R.string.songs_count,
                                playlist.songs.size
                            ),
                            style = if (scrolled) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Play all button
            if (playlist.songs.isNotEmpty()) {
                FilledTonalButton(
                    onClick = onPlayAllClick,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.play_all),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Visual divider that animates based on scroll
        HorizontalDivider(
            thickness = if (isScrolled) 1.dp else 0.5.dp,
            color = if (isScrolled) {
                MaterialTheme.colorScheme.outlineVariant
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun EmptyPlaylistContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // Empty state icon with animation
            var showAnimation by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(3000)
                    showAnimation = !showAnimation
                    delay(200)
                    showAnimation = !showAnimation
                }
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = showAnimation,
                    label = "EmptyAnimation",
                    transitionSpec = {
                        (fadeIn(tween(500)) + scaleIn(tween(500))) togetherWith
                                (fadeOut(tween(200)) + scaleOut(tween(200)))
                    }
                ) { state ->
                    Icon(
                        imageVector = if (state) Icons.Default.MusicNote else Icons.Default.MusicOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                text = stringResource(R.string.empty_playlist),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.empty_playlist_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .alpha(0.8f)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SongItem(
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
        Box(modifier = Modifier.fillMaxWidth()) {
            // Song progress indicator for currently playing song
            if (isPlaying) {
                LinearProgressIndicator(
                    progress = { 0.7f }, // Simulate progress for UI demonstration
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlayClick() }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track number or play icon
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

                Spacer(modifier = Modifier.width(16.dp))

                // Song info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = song.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isPlaying) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
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

                // Action buttons
                Box {
                    Row {
                        // Play/Pause button
                        AnimatedContent(
                            targetState = isPlaying,
                            label = "PlayButtonState",
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(150))
                            }
                        ) { playing ->
                            IconButton(
                                onClick = onPlayClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (playing) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                            ) {
                                Icon(
                                    imageVector = if (playing) {
                                        Icons.Default.Pause
                                    } else {
                                        Icons.Default.PlayArrow
                                    },
                                    contentDescription = stringResource(
                                        if (playing) R.string.pause else R.string.play
                                    ),
                                    tint = if (playing) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }

                        // More options button
                        IconButton(
                            onClick = { showDropdownMenu = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.more),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(stringResource(R.string.remove_from_playlist))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlaylistRemove,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showDropdownMenu = false
                                showRemoveConfirmation = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Remove confirmation dialog with fixed size and center positioning
    if (showRemoveConfirmation) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.remove_song),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.remove_song_confirmation, song.displayName),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveClick()
                        showRemoveConfirmation = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.remove).uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveConfirmation = false }) {
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
}