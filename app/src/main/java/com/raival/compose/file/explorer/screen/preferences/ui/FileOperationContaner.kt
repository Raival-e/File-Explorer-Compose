package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun FileOperationContainer() {
    val preferences = globalClass.preferencesManager

    Container(title = stringResource(R.string.file_operation)) {
        PreferenceItem(
            label = stringResource(R.string.auto_sign_merged_apk_bundle_files),
            supportingText = stringResource(R.string.auto_sign_merged_apk_bundle_files_description),
            icon = Icons.Rounded.Key,
            switchState = preferences.signMergedApkBundleFiles,
            onSwitchChange = { preferences.signMergedApkBundleFiles = it }
        )
    }
}