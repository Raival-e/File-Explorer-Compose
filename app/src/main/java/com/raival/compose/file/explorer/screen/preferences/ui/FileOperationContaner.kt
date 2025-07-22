package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun FileOperationContainer() {
    val preferences = globalClass.preferencesManager
    val limits = arrayListOf("15", "25", "50", "100", stringResource(R.string.unlimited))

    Container(title = stringResource(R.string.file_operation)) {
        PreferenceItem(
            label = stringResource(R.string.search_in_files_limit),
            supportingText = if (preferences.searchInFilesLimit == -1) limits[4] else preferences.searchInFilesLimit.toString(),
            icon = Icons.AutoMirrored.Rounded.ManageSearch,
            onClick = {
                preferences.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.search_in_files_limit),
                    description = globalClass.getString(R.string.maximum_number_of_files_search_desc),
                    choices = limits,
                    selectedChoice = if (preferences.searchInFilesLimit == -1) 4 else limits.indexOf(
                        preferences.searchInFilesLimit.toString()
                    ),
                    onSelect = {
                        val limit = when (limits[it]) {
                            limits[4] -> -1
                            else -> limits[it].toIntOrNull() ?: -1
                        }
                        preferences.searchInFilesLimit = limit
                    }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.auto_sign_merged_apk_bundle_files),
            supportingText = stringResource(R.string.auto_sign_merged_apk_bundle_files_description),
            icon = Icons.Rounded.Key,
            switchState = preferences.signMergedApkBundleFiles,
            onSwitchChange = { preferences.signMergedApkBundleFiles = it }
        )
    }
}