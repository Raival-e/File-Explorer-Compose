package com.raival.compose.file.explorer.screen.preferences.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.Note
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun GeneralContainer() {
    val manager = globalClass.preferencesManager
    val preferences = manager.generalPrefs

    val limits = arrayListOf(
        "15", "25", "50", "100", "Unlimited"
    )

    Container(title = stringResource(R.string.general)) {
        PreferenceItem(
            label = stringResource(R.string.search_in_files_limit),
            supportingText = if (preferences.searchInFilesLimit == -1) limits[4] else preferences.searchInFilesLimit.toString(),
            icon = Icons.AutoMirrored.Rounded.ManageSearch,
            onClick = {
                manager.singleChoiceDialog.show(
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
        PreferenceItem(
            label = stringResource(R.string.sign_apk),
            // Couldn't find a suitable ImageVector for it, please change
            icon = Icons.AutoMirrored.Rounded.Note,
            supportingText = if (preferences.signApk) "True" else "False",
            onClick = {
                manager.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.sign_apk),
                    description = globalClass.getString(R.string.sign_apk_desc),
                    choices = listOf("True", "False"),
                    selectedChoice = if (preferences.signApk) 0 else 1,
                    onSelect = {
                        preferences.signApk = it == 0
                    }
                )
            }
        )
    }
}