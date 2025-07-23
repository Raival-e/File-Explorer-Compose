package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.block
import com.raival.compose.file.explorer.common.copyToClipboard
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.provider.CalculationProgress
import com.raival.compose.file.explorer.screen.main.tab.files.provider.ContentPropertiesProvider
import com.raival.compose.file.explorer.screen.main.tab.files.provider.PropertiesState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FilePropertiesDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val selection = tab.selectedFiles.map { it.value }.toList()
        val contentPropertiesProvider = remember { ContentPropertiesProvider(selection) }
        val uiState by contentPropertiesProvider.uiState.collectAsState()

        DisposableEffect(contentPropertiesProvider) {
            onDispose {
                contentPropertiesProvider.cleanup()
            }
        }

        val title = when (uiState.details) {
            is PropertiesState.SingleContentProperties -> stringResource(R.string.file_properties)
            is PropertiesState.MultipleContentProperties -> stringResource(R.string.selection_properties)
            else -> stringResource(R.string.loading_properties)
        }

        BottomSheetDialog(
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
            ) {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Space(8.dp)
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                Space(12.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item {
                        AnimatedContent(
                            targetState = uiState.details,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "content_transition"
                        ) { details ->
                            when (details) {
                                is PropertiesState.Loading -> {
                                    LoadingContent()
                                }

                                is PropertiesState.SingleContentProperties -> {
                                    SingleFileContent(details)
                                }

                                is PropertiesState.MultipleContentProperties -> {
                                    MultipleFilesContent(details)
                                }
                            }
                        }
                    }
                }

                Space(16.dp)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(R.string.close))
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        CircularProgressIndicator()
        Space(16.dp)
        Text(
            text = stringResource(R.string.analyzing_files),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SingleFileContent(details: PropertiesState.SingleContentProperties) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Basic properties
        PropertySection(title = stringResource(R.string.general)) {
            PropertyRow(
                icon = Icons.Default.DriveFileRenameOutline,
                label = stringResource(R.string.name),
                value = details.name
            )
            PropertyRow(
                icon = Icons.Default.FolderOpen,
                label = stringResource(R.string.location),
                value = details.path
            )
            PropertyRow(
                icon = Icons.Default.Category,
                label = stringResource(R.string.type),
                value = details.type
            )
            PropertyRow(
                icon = Icons.Default.DataUsage,
                label = stringResource(R.string.size),
                value = details.size
            )
            PropertyRow(
                icon = Icons.Default.Schedule,
                label = stringResource(R.string.modified),
                value = details.lastModified
            )
        }

        // System properties
        PropertySection(title = stringResource(R.string.system)) {
            PropertyRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.owner),
                value = details.owner
            )
            PropertyRow(
                icon = Icons.Default.Security,
                label = stringResource(R.string.permissions),
                value = details.permissions
            )
        }

        // Computed properties
        PropertySection(title = stringResource(R.string.analysis)) {
            AsyncPropertyRow(
                icon = Icons.Default.Inventory,
                label = stringResource(R.string.contents),
                valueFlow = details.contentCount,
                progressFlow = details.contentProgress
            )
            AsyncPropertyRow(
                icon = Icons.Default.Fingerprint,
                label = stringResource(R.string.md5_checksum),
                valueFlow = details.checksum,
                progressFlow = details.checksumProgress
            )
            AsyncPropertyRow(
                icon = Icons.Default.Info,
                label = stringResource(R.string.sha1_checksum),
                valueFlow = details.sha256,
                progressFlow = details.sha256Progress
            )
        }
    }
}

@Composable
private fun MultipleFilesContent(details: PropertiesState.MultipleContentProperties) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PropertySection(title = stringResource(R.string.selection_summary)) {
            PropertyRow(
                icon = Icons.Default.SelectAll,
                label = stringResource(R.string.selected),
                value = stringResource(R.string.items_count, details.selectedFileCount)
            )
            AsyncPropertyRow(
                icon = Icons.Default.DataUsage,
                label = stringResource(R.string.total_size),
                valueFlow = details.totalSize,
                progressFlow = details.sizeProgress
            )
            AsyncPropertyRow(
                icon = Icons.Default.Inventory,
                label = stringResource(R.string.total_contents),
                valueFlow = details.totalFileCount,
                progressFlow = details.countProgress
            )
        }
    }
}

@Composable
private fun PropertySection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .block(
                        shape = RoundedCornerShape(6.dp),
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun PropertyRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Space(12.dp)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(100.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Space(8.dp)
            CopiableText(
                text = value,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AsyncPropertyRow(
    icon: ImageVector,
    label: String,
    valueFlow: StateFlow<String>,
    progressFlow: StateFlow<CalculationProgress>
) {
    val value by valueFlow.collectAsState()
    val progress by progressFlow.collectAsState()

    if (value.isNotBlank()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Space(12.dp)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(100.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Space(8.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (progress.isCalculating) {
                            PulsingDot()
                            Space(8.dp)
                        }
                        CopiableText(
                            text = value,
                            color = if (progress.isCalculating)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
    )
}

@Composable
fun CopiableText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    LocalContext.current
    LocalClipboard.current
    var showCopiedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(showCopiedFeedback) {
        if (showCopiedFeedback) {
            delay(2000)
            showCopiedFeedback = false
        }
    }

    Box(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(text) {
                    detectTapGestures(
                        onLongPress = {
                            text.copyToClipboard()
                            showCopiedFeedback = true
                            showMsg(globalClass.getString(R.string.copied_to_clipboard))
                        }
                    )
                }
        )

        // Subtle visual feedback
        if (showCopiedFeedback) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}