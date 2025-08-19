package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.screen.main.ui.AppInfoDialog
import com.raival.compose.file.explorer.screen.preferences.misc.exportPreferences
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AppInfoContainer() {
    var showAppInfoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AppInfoDialog(
        show = showAppInfoDialog,
        onDismiss = { showAppInfoDialog = false },
        hasNewUpdate = globalClass.mainActivityManager.newUpdate != null
    )

    Container(title = stringResource(R.string.other)) {
        PreferenceItem(
            label = stringResource(R.string.export_preferences),
            supportingText = stringResource(R.string.export_preferences_desc),
            icon = Icons.Rounded.Upload,
            onClick = {
                scope.launch {
                    val data = exportPreferences()
                    File(globalClass.appFiles.file, "preferences.prismPrefs").writeText(data)
                    showMsg(globalClass.getString(R.string.exported_prism_preferences))
                }
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.about),
            supportingText = emptyString,
            icon = Icons.Rounded.Info,
            onClick = {
                showAppInfoDialog = true
            }
        )
    }
}