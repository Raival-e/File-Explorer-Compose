package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.R

@Composable
fun SaveTextEditorFilesDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    if (isSaving) {
        AlertDialog(
            text = { Text(text = stringResource(R.string.saving)) },
            onDismissRequest = { },
            confirmButton = { }
        )
    }

    if (show) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.warning)) },
            text = { Text(text = stringResource(R.string.save_files_before_exit_warning_message)) },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onIgnore()
                    }
                ) { Text(text = stringResource(R.string.ignore)) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onSave()
                    }
                ) { Text(text = stringResource(R.string.save)) }
            },
            onDismissRequest = onDismiss
        )
    }
}