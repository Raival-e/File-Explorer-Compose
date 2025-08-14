package com.raival.compose.file.explorer.screen.main.ui

import android.content.Intent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.icons.Github
import com.raival.compose.file.explorer.common.icons.PrismIcons
import com.raival.compose.file.explorer.common.icons.Upgrade
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.logs.LogsActivity
import kotlinx.coroutines.delay

@Composable
fun AppInfoDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    hasNewUpdate: Boolean
) {
    val appIcon = AppCompatResources.getDrawable(globalClass, R.drawable.app_icon)
    val versionName =
        globalClass.packageManager.getPackageInfo(globalClass.packageName, 0).versionName
    val context = LocalContext.current
    val newUpdate = globalClass.mainActivityManager.newUpdate

    var animateContent by remember { mutableStateOf(false) }

    LaunchedEffect(show) {
        if (show) {
            delay(100)
            animateContent = true
        } else {
            animateContent = false
        }
    }

    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Box(
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // App Icon with animation
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = scaleIn(spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                                exit = scaleOut() + fadeOut()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .shadow(
                                            elevation = 24.dp,
                                            shape = RoundedCornerShape(24.dp),
                                            clip = false
                                        )
                                ) {
                                    AsyncImage(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(24.dp)),
                                        model = appIcon,
                                        contentDescription = null
                                    )

                                    // Update indicator badge
                                    if (hasNewUpdate) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(28.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.error,
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.NewReleases,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Space(16.dp)

                            // App Name with animation
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = fadeIn(animationSpec = tween(delayMillis = 100))
                            ) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Version badge with update animation
                            AnimatedVisibility(
                                visible = animateContent,
                                enter = fadeIn(animationSpec = tween(delayMillis = 200))
                            ) {
                                VersionBadge(
                                    currentVersion = versionName
                                        ?: stringResource(R.string.unknown),
                                    newVersion = if (hasNewUpdate) newUpdate?.tagName else null,
                                    hasUpdate = hasNewUpdate
                                )
                            }

                            Space(32.dp)

                            // Divider
                            HorizontalDivider(
                                thickness = DividerDefaults.Thickness,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            Space(20.dp)

                            // Action cards
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (hasNewUpdate && newUpdate != null) {
                                    val downloadUrl =
                                        newUpdate.assets.firstOrNull()?.browserDownloadUrl
                                    if (downloadUrl != null) {
                                        AnimatedVisibility(
                                            visible = animateContent,
                                            enter = fadeIn(animationSpec = tween(delayMillis = 300))
                                        ) {
                                            UpdateCard(
                                                onClick = {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            downloadUrl.toUri()
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = animateContent,
                                    enter = fadeIn(animationSpec = tween(delayMillis = 400))
                                ) {
                                    ActionCard(
                                        icon = PrismIcons.Github,
                                        title = stringResource(R.string.github),
                                        description = stringResource(R.string.view_source_code),
                                        onClick = {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://github.com/Raival-e/Prism-File-Explorer".toUri()
                                                )
                                            )
                                        }
                                    )
                                }

                                AnimatedVisibility(
                                    visible = animateContent,
                                    enter = fadeIn(animationSpec = tween(delayMillis = 500))
                                ) {
                                    ActionCard(
                                        icon = Icons.Outlined.BugReport,
                                        title = stringResource(R.string.title_activity_logs),
                                        description = stringResource(R.string.view_application_logs),
                                        onClick = {
                                            context.startActivity(
                                                Intent(context, LogsActivity::class.java)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionBadge(
    currentVersion: String,
    newVersion: String?,
    hasUpdate: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (hasUpdate) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(500),
        label = "version_bg_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (hasUpdate) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "version_scale"
    )

    Row(
        modifier = Modifier
            .padding(top = 12.dp)
            .scale(scale)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "v$currentVersion",
            style = MaterialTheme.typography.labelLarge,
            color = if (hasUpdate) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
            fontWeight = FontWeight.Medium
        )

        if (hasUpdate && newVersion != null) {
            Icon(
                imageVector = PrismIcons.Upgrade,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = newVersion,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UpdateCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PrismIcons.Upgrade,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.title_upgrade),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Space(4.dp)

                Text(
                    text = stringResource(R.string.download_new_update),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Space(4.dp)

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}