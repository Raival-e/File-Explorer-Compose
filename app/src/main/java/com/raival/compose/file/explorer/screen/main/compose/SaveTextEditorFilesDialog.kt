package com.raival.compose.file.explorer.screen.main.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun SaveTextEditorFilesDialog(onRequestFinish: () -> Unit) {
    val mainActivityManager = globalClass.mainActivityManager

    if (mainActivityManager.isSavingTextEditorFiles) {
        AlertDialog(
            text = { Text(text = stringResource(R.string.saving)) },
            onDismissRequest = { },
            confirmButton = { }
        )
    }

    if (mainActivityManager.showSaveTextEditorFilesBeforeCloseDialog) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.warning)) },
            text = { Text(text = stringResource(R.string.save_files_before_exit_warning_message)) },
            dismissButton = {
                TextButton(
                    onClick = {
                        mainActivityManager.showSaveTextEditorFilesBeforeCloseDialog = false
                        onRequestFinish()
                    }
                ) { Text(text = stringResource(R.string.ignore)) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainActivityManager.showSaveTextEditorFilesBeforeCloseDialog = false
                        mainActivityManager.saveTextEditorFiles(onRequestFinish)
                    }
                ) { Text(text = stringResource(R.string.save)) }
            },
            onDismissRequest = {
                mainActivityManager.showSaveTextEditorFilesBeforeCloseDialog = false
            }
        )
    }
}