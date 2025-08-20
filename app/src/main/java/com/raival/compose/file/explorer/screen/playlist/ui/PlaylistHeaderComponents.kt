package com.raival.compose.file.explorer.screen.playlist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist

@Composable
fun PlaylistHeaderBackground(isScrolled: Boolean) {
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
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaylistHeader(
    playlist: Playlist,
    isScrolled: Boolean,
    onPlayAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isScrolled) 0.dp else 16.dp)
    ) {
        HeaderRow(playlist, isScrolled, onPlayAllClick)
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
fun HeaderRow(
    playlist: Playlist,
    isScrolled: Boolean,
    onPlayAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistInfo(playlist, isScrolled)

        if (playlist.songs.isNotEmpty()) {
            PlayAllButton(onPlayAllClick)
        }
    }
}

@Composable
fun PlaylistInfo(playlist: Playlist, isScrolled: Boolean, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        PlaylistIcon(isScrolled)

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
                    text = stringResource(R.string.songs_count, playlist.songs.size),
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
}

@Composable
fun PlaylistIcon(isScrolled: Boolean) {
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
}

@Composable
fun PlayAllButton(onPlayAllClick: () -> Unit) {
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