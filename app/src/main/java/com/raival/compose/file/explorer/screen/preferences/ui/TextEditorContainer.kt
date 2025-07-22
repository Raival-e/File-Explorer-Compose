package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardTab
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.TextRotationNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun TextEditorContainer() {
    val preferences = globalClass.preferencesManager

    val recentFilesLimits = arrayListOf(
        "5", "10", "15", "25", "Unlimited"
    )

    Container(title = stringResource(R.string.text_editor)) {
        PreferenceItem(
            label = stringResource(R.string.recent_files_limit),
            supportingText = if (preferences.recentFilesLimit == -1) recentFilesLimits[4] else preferences.recentFilesLimit.toString(),
            icon = Icons.Rounded.History,
            onClick = {
                preferences.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.recent_files_limit),
                    description = globalClass.getString(R.string.maximum_number_of_recent_files_desc),
                    choices = recentFilesLimits,
                    selectedChoice = if (preferences.recentFilesLimit == -1) 4 else recentFilesLimits.indexOf(
                        preferences.recentFilesLimit.toString()
                    ),
                    onSelect = {
                        val limit = when (recentFilesLimits[it]) {
                            recentFilesLimits[4] -> -1
                            else -> recentFilesLimits[it].toIntOrNull() ?: -1
                        }
                        preferences.recentFilesLimit = limit
                    }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.pin_numbers_line),
            icon = Icons.Rounded.Numbers,
            switchState = preferences.pinLineNumber,
            onSwitchChange = { preferences.pinLineNumber = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.auto_symbol_pair),
            icon = Icons.Rounded.Code,
            switchState = preferences.symbolPairAutoCompletion,
            onSwitchChange = { preferences.symbolPairAutoCompletion = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.auto_indentation),
            icon = Icons.AutoMirrored.Rounded.KeyboardTab,
            switchState = preferences.autoIndent,
            onSwitchChange = { preferences.autoIndent = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.magnifier),
            icon = Icons.Rounded.Search,
            switchState = preferences.enableMagnifier,
            onSwitchChange = { preferences.enableMagnifier = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.use_icu_selection),
            icon = Icons.Rounded.TextRotationNone,
            switchState = preferences.useICULibToSelectWords,
            onSwitchChange = { preferences.useICULibToSelectWords = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.delete_empty_lines),
            icon = Icons.Rounded.DeleteSweep,
            switchState = preferences.deleteEmptyLineFast,
            onSwitchChange = { preferences.deleteEmptyLineFast = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(id = R.string.delete_tabs),
            icon = Icons.Rounded.SpaceBar,
            switchState = preferences.deleteMultiSpaces,
            onSwitchChange = { preferences.deleteMultiSpaces = it }
        )
    }
}