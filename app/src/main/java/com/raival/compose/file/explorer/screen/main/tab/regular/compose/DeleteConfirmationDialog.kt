package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.CheckableText
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.plural
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab

@Composable
fun DeleteConfirmationDialog(tab: RegularTab) {
    if (tab.showConfirmDeleteDialog) {
        var moveToRecycleBin by remember {
            mutableStateOf(true)
        }

        val targetFiles by remember(tab.id, tab.activeFolder.getPath()) {
            mutableStateOf(tab.selectedFiles.map { it.value }.toList())
        }

        fun onDismissRequest() {
            tab.showConfirmDeleteDialog = false
        }

        AlertDialog(
            onDismissRequest = { onDismissRequest() },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                        tab.unselectAllFiles()
                        tab.deleteFiles(targetFiles, tab.taskCallback, moveToRecycleBin)
                    }
                ) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) { Text(stringResource(R.string.dismiss)) }
            },
            title = { Text(text = stringResource(R.string.delete_confirmation)) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(
                            id = R.string.delete_confirmation_message,
                            targetFiles.size,
                            plural(targetFiles.size)
                        )
                    )

                    if (!tab.showEmptyRecycleBin) {
                        Space(size = 8.dp)

                        CheckableText(
                            modifier = Modifier.fillMaxWidth(),
                            checked = moveToRecycleBin,
                            onCheckedChange = { moveToRecycleBin = it },
                            text = {
                                Text(
                                    modifier = Modifier.alpha(0.7f),
                                    text = stringResource(R.string.move_to_recycle_bin)
                                )
                            }
                        )
                    }
                }
            }
        )
    }
}