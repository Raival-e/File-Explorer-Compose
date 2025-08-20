package com.raival.compose.file.explorer.screen.playlist.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.audio.PlaylistManager
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PlaylistDetailSheet(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onPlayClick: (Int) -> Unit
) {
    val playlistManager = remember { PlaylistManager.getInstance() }
    val playlists by playlistManager.playlists.collectAsStateWithLifecycle(initialValue = emptyList())
    
    val currentPlaylist = remember(playlists, playlist.id) {
        playlists.find { it.id == playlist.id } ?: playlist
    }
    val dialogScale = remember { Animatable(0.95f) }
    val dialogAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp }
    val screenHeight = with(density) { configuration.screenHeightDp.dp }
    val dialogWidth = min(screenWidth * 0.92f, 480.dp)
    val dialogHeight = min(screenHeight * 0.85f, 680.dp)

    val lazyListState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0 }
    }

    var currentlyPlayingIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        dialogScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        dialogAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(300)
        )
    }

    LaunchedEffect(currentPlaylist.songs.size) {
        if (currentlyPlayingIndex >= currentPlaylist.songs.size) {
            currentlyPlayingIndex = -1
        }
    }

    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            dialogScale.animateTo(
                targetValue = 0.95f,
                animationSpec = tween(200)
            )
            dialogAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(200)
            )
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = dismissWithAnimation,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width(dialogWidth)
                .height(dialogHeight)
                .graphicsLayer {
                    scaleX = dialogScale.value
                    scaleY = dialogScale.value
                    alpha = dialogAlpha.value
                }
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    clip = false,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )

                PlaylistDetailContent(
                    playlist = currentPlaylist,
                    isScrolled = isScrolled,
                    currentlyPlayingIndex = currentlyPlayingIndex,
                    onPlayClick = { index ->
                        onPlayClick(index)
                        currentlyPlayingIndex = if (index == currentlyPlayingIndex) -1 else index
                    },
                    onDismiss = dismissWithAnimation,
                    lazyListState = lazyListState
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistDetailContent(
    playlist: Playlist,
    isScrolled: Boolean,
    currentlyPlayingIndex: Int,
    onPlayClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    lazyListState: LazyListState
) {
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .systemBarsPadding()
        ) {
            HeaderBar(onDismiss = onDismiss)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                PlaylistHeader(
                    playlist = playlist,
                    isScrolled = isScrolled,
                    onPlayAllClick = {
                        if (playlist.songs.isNotEmpty()) onPlayClick(0)
                    }
                )

                AnimatedContent(
                    targetState = playlist.songs.isEmpty(),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "ContentSwitcher"
                ) { isEmpty ->
                    if (isEmpty) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyPlaylistContent()
                        }
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                top = 16.dp,
                                bottom = 24.dp
                            )
                        ) {
                            itemsIndexed(
                                items = playlist.songs,
                                key = { index, song -> "${song.uid}_$index" }
                            ) { index, song ->
                                val isPlaying = index == currentlyPlayingIndex

                                SongItem(
                                    song = song,
                                    index = index,
                                    isPlaying = isPlaying,
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    onPlayClick = { onPlayClick(index) },
                                    onRemoveClick = {
                                            PlaylistManager.getInstance().removeSongFromPlaylistAt(playlist.id, index)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeaderBar(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, bottom = 4.dp)
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(42.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}