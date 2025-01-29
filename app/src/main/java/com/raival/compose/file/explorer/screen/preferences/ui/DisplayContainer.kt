package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.preferences.constant.FilesTabFileListSize
import com.raival.compose.file.explorer.screen.preferences.constant.ThemePreference

@Composable
fun DisplayContainer() {
    val manager = globalClass.preferencesManager
    val preferences = manager.displayPrefs

    Container(title = stringResource(R.string.display)) {
        PreferenceItem(
            label = stringResource(R.string.theme),
            supportingText = when (preferences.theme) {
                ThemePreference.LIGHT.ordinal -> stringResource(R.string.light)
                ThemePreference.DARK.ordinal -> stringResource(R.string.dark)
                else -> stringResource(R.string.follow_system)
            },
            icon = Icons.Rounded.Nightlight,
            onClick = {
                manager.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.theme),
                    description = globalClass.getString(R.string.select_theme_preference),
                    choices = listOf(
                        globalClass.getString(R.string.light),
                        globalClass.getString(R.string.dark),
                        globalClass.getString(R.string.follow_system)
                    ),
                    selectedChoice = preferences.theme,
                    onSelect = { preferences.theme = it }
                )
            }
        )

        PreferenceItem(
            label = stringResource(R.string.file_list_size),
            supportingText = when (preferences.fileListSize) {
                FilesTabFileListSize.SMALL.ordinal -> stringResource(R.string.small)
                FilesTabFileListSize.MEDIUM.ordinal -> stringResource(R.string.medium)
                FilesTabFileListSize.LARGE.ordinal -> stringResource(R.string.large)
                else -> stringResource(R.string.extra_large)
            },
            icon = Icons.Rounded.Height,
            onClick = {
                manager.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.file_list_size),
                    description = globalClass.getString(R.string.file_list_size_desc),
                    choices = listOf(
                        globalClass.getString(R.string.small),
                        globalClass.getString(R.string.medium),
                        globalClass.getString(R.string.large),
                        globalClass.getString(R.string.extra_large)
                    ),
                    selectedChoice = preferences.fileListSize,
                    onSelect = { preferences.fileListSize = it }
                )
            }
        )

        val columnCount = arrayListOf(
            "1", "2", "3", "4", "Auto"
        )

        PreferenceItem(
            label = stringResource(R.string.files_list_column_count),
            supportingText = if (preferences.fileListColumnCount == -1) columnCount[4] else preferences.fileListColumnCount.toString(),
            icon = Icons.AutoMirrored.Rounded.ManageSearch,
            onClick = {
                manager.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.files_list_column_count),
                    description = globalClass.getString(R.string.choose_number_of_columns),
                    choices = columnCount,
                    selectedChoice = if (preferences.fileListColumnCount == -1) 4 else columnCount.indexOf(
                        preferences.fileListColumnCount.toString()
                    ),
                    onSelect = {
                        val limit = when (columnCount[it]) {
                            columnCount[4] -> -1
                            else -> columnCount[it].toIntOrNull() ?: -1
                        }
                        preferences.fileListColumnCount = limit
                    }
                )
            }
        )

        PreferenceItem(
            label = stringResource(R.string.show_hidden_files),
            supportingText = emptyString,
            icon = Icons.Rounded.HideSource,
            switchState = preferences.showHiddenFiles,
            onSwitchChange = { preferences.showHiddenFiles = it }
        )

        PreferenceItem(
            label = stringResource(R.string.show_folder_s_content_count),
            supportingText = emptyString,
            icon = Icons.Rounded.Numbers,
            switchState = preferences.showFolderContentCount,
            onSwitchChange = { preferences.showFolderContentCount = it }
        )

        PreferenceItem(
            label = stringResource(R.string.show_bottom_bar_labels),
            supportingText = emptyString,
            icon = Icons.AutoMirrored.Rounded.Label,
            switchState = preferences.showBottomBarLabels,
            onSwitchChange = { preferences.showBottomBarLabels = it }
        )
    }
}