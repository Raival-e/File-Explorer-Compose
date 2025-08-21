package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewConfigs
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewConfigDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit,
) {
    if (show) {
        val prefs = globalClass.preferencesManager

        var applyToThisPathOnly by remember {
            mutableStateOf(
                prefs.getDefaultViewConfigPrefs() != prefs.getViewConfigPrefsFor(tab.activeFolder)
            )
        }

        var viewType by remember {
            mutableIntStateOf(tab.viewConfig.viewType.ordinal)
        }

        var columnCount by remember {
            mutableFloatStateOf(
                value = when {
                    viewType == ViewType.GRID.ordinal -> tab.viewConfig.columnCount.coerceIn(3, 8)
                        .toFloat()

                    else -> tab.viewConfig.columnCount.coerceIn(1, 5).toFloat()
                }
            )
        }

        var itemSize by remember {
            mutableIntStateOf(tab.viewConfig.itemSize)
        }

        var cropThumbnails by remember {
            mutableStateOf(tab.viewConfig.cropThumbnails)
        }

        var galleryMode by remember {
            mutableStateOf(tab.viewConfig.galleryMode)
        }

        var hideMediaNames by remember {
            mutableStateOf(tab.viewConfig.hideMediaNames)
        }

        fun saveConfigs() {
            val configs = ViewConfigs(
                viewType = if (viewType == ViewType.GRID.ordinal) ViewType.GRID else ViewType.LIST,
                columnCount = columnCount.roundToInt(),
                cropThumbnails = cropThumbnails,
                galleryMode = galleryMode,
                hideMediaNames = hideMediaNames,
                itemSize = itemSize
            )
            if (applyToThisPathOnly) {
                prefs.setViewConfigPrefsFor(tab.activeFolder, configs)
            } else {
                prefs.setDefaultViewConfigPrefs(configs)
            }
        }

        BottomSheetDialog(
            onDismissRequest = {
                saveConfigs()
                onDismissRequest()
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ViewModule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Space(8.dp)
                    Text(
                        text = stringResource(R.string.view_configuration),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Scope Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SwitchSettingItem(
                        icon = Icons.Rounded.Folder,
                        title = stringResource(R.string.apply_to_this_folder_only),
                        checked = applyToThisPathOnly,
                        onCheckedChange = { applyToThisPathOnly = it }
                    )
                }

                Space(size = 24.dp)

                // View Type Section
                Text(
                    text = stringResource(R.string.view_type),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup()
                ) {
                    RadioButtonItem(
                        icon = Icons.Rounded.GridView,
                        text = stringResource(R.string.grid_view),
                        selected = viewType == ViewType.GRID.ordinal,
                        onClick = {
                            viewType = ViewType.GRID.ordinal
                            // Reset column count for grid view
                            columnCount = 4f
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    )

                    RadioButtonItem(
                        icon = Icons.AutoMirrored.Rounded.ViewList,
                        text = stringResource(R.string.list_view),
                        selected = viewType == ViewType.LIST.ordinal,
                        onClick = {
                            viewType = ViewType.LIST.ordinal
                            // Reset column count for column view
                            columnCount = 1f
                        }
                    )
                }

                Space(size = 24.dp)

                // Column Count Section
                Text(
                    text = stringResource(R.string.properties),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (viewType == ViewType.GRID.ordinal) stringResource(
                                R.string.cell_count,
                                columnCount.roundToInt()
                            ) else stringResource(R.string.column_count, columnCount.roundToInt()),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (viewType == ViewType.GRID.ordinal) "3" else "1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Space(8.dp)

                            Slider(
                                value = columnCount,
                                onValueChange = { columnCount = it },
                                valueRange = if (viewType == ViewType.GRID.ordinal) 3f..8f else 1f..5f,
                                steps = if (viewType == ViewType.GRID.ordinal) 4 else 3, // 5 steps for grid (3-8), 4 steps for columns (1-5)
                                modifier = Modifier.weight(1f),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .padding(0.dp)
                                            .padding(start = if (it.value == 0f) 8.dp else 0.dp)
                                            .size(16.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(SliderDefaults.TickSize)
                                                .align(Alignment.Center)
                                                .background(
                                                    color = MaterialTheme.colorScheme.surface,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                },
                                track = {
                                    SliderDefaults.Track(
                                        sliderState = it,
                                        thumbTrackGapSize = 0.dp
                                    )
                                }
                            )

                            Space(8.dp)

                            Text(
                                text = if (viewType == ViewType.GRID.ordinal) "8" else "5",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Space(12.dp)

                        Text(
                            text = if (viewType == ViewType.GRID.ordinal && cropThumbnails) stringResource(
                                R.string.font_size
                            )
                            else stringResource(
                                R.string.item_size
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Slider(
                            value = itemSize.toFloat(),
                            onValueChange = { itemSize = it.roundToInt() },
                            valueRange = 0f..4f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth(),
                            thumb = {
                                Box(
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .size(16.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(SliderDefaults.TickSize)
                                            .align(Alignment.Center)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            },
                            track = {
                                SliderDefaults.Track(
                                    sliderState = it,
                                    thumbTrackGapSize = 0.dp
                                )
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.small),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.large),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Space(size = 24.dp)

                // Display Options Section
                Text(
                    text = stringResource(R.string.display_options),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    SwitchSettingItem(
                        icon = Icons.Rounded.CropFree,
                        title = stringResource(
                            R.string.crop_in_thumbnails
                        ),
                        checked = cropThumbnails,
                        onCheckedChange = { cropThumbnails = it }
                    )
                }

                if (viewType == ViewType.GRID.ordinal) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        SwitchSettingItem(
                            icon = Icons.Rounded.Image,
                            title = stringResource(R.string.gallery_mode),
                            checked = galleryMode,
                            onCheckedChange = { galleryMode = it }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        SwitchSettingItem(
                            icon = Icons.Rounded.TextFields,
                            title = stringResource(R.string.hide_media_names),
                            checked = hideMediaNames,
                            onCheckedChange = { hideMediaNames = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioButtonItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = selected,
                onClick = null
            )
        }
    }
}

@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { onCheckedChange(!checked) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}