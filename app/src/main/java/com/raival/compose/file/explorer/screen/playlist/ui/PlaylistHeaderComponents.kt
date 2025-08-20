package com.raival.compose.file.explorer.screen.playlist.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.audio.model.Playlist
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaylistHeader(
    playlist: Playlist,
    isScrolled: Boolean,
    onPlayAllClick: () -> Unit
) {
    val bottomPadding by animateDpAsState(
        targetValue = if (isScrolled) 8.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bottomPadding"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
    ) {
        HeaderRow(playlist, isScrolled, onPlayAllClick)

        val dividerThickness by animateDpAsState(
            targetValue = if (isScrolled) 1.dp else 0.5.dp,
            animationSpec = tween(500),
            label = "dividerThickness"
        )

        val dividerAlpha by animateFloatAsState(
            targetValue = if (isScrolled) 1f else 0.6f,
            animationSpec = tween(500),
            label = "dividerAlpha"
        )

        HorizontalDivider(
            thickness = dividerThickness,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = dividerAlpha),
            modifier = Modifier
                .padding(horizontal = if (isScrolled) 0.dp else 8.dp)
                .animateContentSize()
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
            .padding(top = 8.dp, bottom = if (isScrolled) 8.dp else 16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistInfo(
            playlist = playlist,
            isScrolled = isScrolled,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        if (playlist.songs.isNotEmpty()) {
            PlayAllButton(onPlayAllClick)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
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
                fadeIn(tween(300)) + slideInVertically(
                    initialOffsetY = { if (targetState) 20 else -20 },
                    animationSpec = tween(300, easing = EaseOutQuint)
                ) togetherWith fadeOut(tween(150))
            }
        ) { scrolled ->
            Column(
                modifier = Modifier.fillMaxWidth()
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

                Spacer(modifier = Modifier.height(2.dp))

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
    val size by animateDpAsState(
        targetValue = if (isScrolled) 40.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconSize"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isScrolled) 10.dp else 14.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cornerRadius"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnimation"
    )

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.secondary,
                    )
                )
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f * scale),
                            Color.Transparent
                        )
                    )
                )
        )

        val iconSize by animateDpAsState(
            targetValue = if (isScrolled) 24.dp else 32.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "iconInnerSize"
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                }
        )
    }
}

@Composable
fun PlayAllButton(onPlayAllClick: () -> Unit) {
    val buttonScale = remember { Animatable(0.9f) }

    LaunchedEffect(Unit) {
        buttonScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    ElevatedButton(
        onClick = {
            onPlayAllClick()
        },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 2.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier
            .height(40.dp)
            .padding(end = 2.dp)
            .graphicsLayer {
                scaleX = buttonScale.value
                scaleY = buttonScale.value
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.play_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}