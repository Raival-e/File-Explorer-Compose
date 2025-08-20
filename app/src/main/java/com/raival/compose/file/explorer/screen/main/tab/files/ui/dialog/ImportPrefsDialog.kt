package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.preferences.misc.importPreferences
import kotlinx.coroutines.launch

@Composable
fun ImportPrefsDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show && tab.targetFile != null && tab.targetFile!! is LocalFileHolder) {
        val scope = tab.scope
        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                importPreferences((tab.targetFile!! as LocalFileHolder).readText())
                                showMsg(globalClass.getString(R.string.preferences_imported_successfully))
                                onDismissRequest()
                            } catch (e: Exception) {
                                logger.logError(e)
                                showMsg(globalClass.getString(R.string.invalid_preferences_file))
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismissRequest() }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.import_preferences))
            },
            text = {
                Text(stringResource(R.string.import_prefs_desc))
            }
        )
    }
}