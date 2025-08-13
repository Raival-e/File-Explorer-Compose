package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewType
import com.raival.compose.file.explorer.screen.preferences.constant.FilesTabFileListSize

@Composable
fun FileListContainer() {
    val prefs = globalClass.preferencesManager

    Container(title = stringResource(R.string.file_list)) {
        PreferenceItem(
            label = stringResource(R.string.file_list_size),
            supportingText = when (prefs.itemSize) {
                FilesTabFileListSize.EXTRA_SMALL.ordinal -> stringResource(R.string.extra_small)
                FilesTabFileListSize.SMALL.ordinal -> stringResource(R.string.small)
                FilesTabFileListSize.MEDIUM.ordinal -> stringResource(R.string.medium)
                FilesTabFileListSize.LARGE.ordinal -> stringResource(R.string.large)
                else -> stringResource(R.string.extra_large)
            },
            icon = Icons.Rounded.Height,
            onClick = {
                prefs.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.file_list_size),
                    description = globalClass.getString(R.string.file_list_size_desc),
                    choices = listOf(
                        globalClass.getString(R.string.extra_small),
                        globalClass.getString(R.string.small),
                        globalClass.getString(R.string.medium),
                        globalClass.getString(R.string.large),
                        globalClass.getString(R.string.extra_large)
                    ),
                    selectedChoice = prefs.itemSize,
                    onSelect = { prefs.itemSize = it }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.file_list_display_mode),
            supportingText = if (prefs.viewType == ViewType.COLUMNS.ordinal) stringResource(R.string.columns) else stringResource(
                R.string.grid
            ),
            icon = Icons.AutoMirrored.Rounded.ListAlt,
            onClick = {
                prefs.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.file_list_display_mode),
                    description = globalClass.getString(R.string.choose_display_mode),
                    choices = listOf(
                        globalClass.getString(R.string.columns),
                        globalClass.getString(R.string.grid)
                    ),
                    selectedChoice = prefs.viewType,
                    onSelect = {
                        prefs.viewType = it
                        if (it == ViewType.COLUMNS.ordinal) {
                            prefs.columnCount = 1
                        } else {
                            prefs.columnCount = 4
                        }
                    }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.files_list_column_count),
            supportingText = prefs.columnCount.toString(),
            icon = Icons.AutoMirrored.Rounded.ManageSearch,
            onClick = {
                prefs.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.files_list_column_count),
                    description = globalClass.getString(R.string.choose_number_of_columns),
                    choices = (if (prefs.viewType == ViewType.COLUMNS.ordinal) (1..3) else (3..6)).toList()
                        .map { it.toString() },
                    selectedChoice = prefs.columnCount - if (prefs.viewType == ViewType.COLUMNS.ordinal) 1 else 3,
                    onSelect = {
                        prefs.columnCount =
                            it + if (prefs.viewType == ViewType.COLUMNS.ordinal) 1 else 3
                    }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.show_hidden_files),
            supportingText = emptyString,
            icon = Icons.Rounded.HideSource,
            switchState = prefs.showHiddenFiles,
            onSwitchChange = { prefs.showHiddenFiles = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.show_folder_s_content_count),
            supportingText = emptyString,
            icon = Icons.Rounded.Numbers,
            switchState = prefs.showFolderContentCount,
            onSwitchChange = { prefs.showFolderContentCount = it }
        )
    }
}