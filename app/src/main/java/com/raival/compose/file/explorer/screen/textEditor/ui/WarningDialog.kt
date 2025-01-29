package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.raival.compose.file.explorer.App.Companion.globalClass

@Composable
fun WarningDialog() {
    globalClass.textEditorManager.warningDialogProperties.run {
        if (showWarningDialog) {
            AlertDialog(
                title = { Text(text = title) },
                text = { Text(text = message) },
                dismissButton = {
                    TextButton(onClick = { onDismiss() }) { Text(text = dismissText) }
                },
                confirmButton = {
                    TextButton(onClick = { onConfirm() }) { Text(text = confirmText) }
                },
                onDismissRequest = { onDismiss() }
            )
        }
    }
}