package com.raival.compose.file.explorer.screen.main.tab.files.ui

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
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.CheckableText
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab

@Composable
fun DeleteConfirmationDialog(tab: FilesTab) {
    if (tab.showConfirmDeleteDialog) {
        val preferencesManager = globalClass.preferencesManager

        var moveToRecycleBin by remember {
            mutableStateOf(preferencesManager.generalPrefs.moveToRecycleBin)
        }
        var showRememberChoice by remember {
            mutableStateOf(false)
        }
        var rememberChoice by remember {
            mutableStateOf(false)
        }

        val targetFiles by remember(tab.id, tab.activeFolder.path) {
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
                        if (showRememberChoice && rememberChoice) {
                            preferencesManager.generalPrefs.moveToRecycleBin = moveToRecycleBin
                        }
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
                        text = stringResource(id = R.string.delete_confirmation_message)
                    )

                    if (!tab.showEmptyRecycleBin) {
                        Space(size = 8.dp)

                        CheckableText(
                            modifier = Modifier.fillMaxWidth(),
                            checked = moveToRecycleBin,
                            onCheckedChange = {
                                moveToRecycleBin = it
                                showRememberChoice = true
                            },
                            text = {
                                Text(
                                    modifier = Modifier.alpha(0.7f),
                                    text = stringResource(R.string.move_to_recycle_bin)
                                )
                            }
                        )

                        if (showRememberChoice) {
                            Space(size = 4.dp)
                            CheckableText(
                                modifier = Modifier.fillMaxWidth(),
                                checked = rememberChoice,
                                onCheckedChange = { rememberChoice = it },
                                text = {
                                    Text(
                                        modifier = Modifier.alpha(0.6f),
                                        text = stringResource(R.string.remember_this_choice)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}