package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.InputDialog
import com.raival.compose.file.explorer.common.compose.InputDialogButton
import com.raival.compose.file.explorer.common.compose.InputDialogInput
import com.raival.compose.file.explorer.common.extension.isValidAsFileName
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab

@Composable
fun CreateNewFileDialog(tab: RegularTab) {
    if (tab.showCreateNewFileDialog) {
        InputDialog(
            title = stringResource(R.string.create_new),
            inputs = arrayListOf(
                InputDialogInput(stringResource(R.string.name)) {
                    if (!it.isValidAsFileName()) {
                        globalClass.getString(R.string.invalid_file_name)
                    } else {
                        null
                    }
                }
            ),
            buttons = arrayListOf(
                InputDialogButton(stringResource(R.string.file)) { inputs ->
                    val input = inputs[0]
                    if (input.content.isValidAsFileName()) {
                        val similarFile = tab.activeFolder.findFile(input.content)
                        if (similarFile == null) {
                            tab.activeFolder.createSubFile(input.content)
                            tab.showCreateNewFileDialog = false

                            if (tab.activeFolder.findFile(input.content) == null) {
                                globalClass.showMsg(R.string.failed_to_create_file)
                            } else {
                                tab.onNewFileCreated(input.content)
                            }
                        } else {
                            globalClass.showMsg(R.string.similar_file_exists)
                        }
                    } else {
                        globalClass.showMsg(R.string.invalid_file_name)
                    }
                },
                InputDialogButton(stringResource(R.string.folder)) { inputs ->
                    val input = inputs[0]
                    if (input.content.isValidAsFileName()) {
                        val similarFile = tab.activeFolder.findFile(input.content)
                        if (similarFile == null) {
                            tab.activeFolder.createSubFolder(input.content)
                            tab.showCreateNewFileDialog = false

                            if (tab.activeFolder.findFile(input.content) == null) {
                                globalClass.showMsg(R.string.failed_to_create_folder)
                            } else {
                                tab.onNewFileCreated(input.content)
                            }
                        } else {
                            globalClass.showMsg(R.string.similar_file_exists)
                        }
                    } else {
                        globalClass.showMsg(R.string.invalid_folder_name)
                    }
                }
            )
        ) {
            tab.showCreateNewFileDialog = false
        }
    }
}