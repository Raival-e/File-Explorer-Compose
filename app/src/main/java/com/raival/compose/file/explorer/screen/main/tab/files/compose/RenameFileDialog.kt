package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.InputDialog
import com.raival.compose.file.explorer.common.compose.InputDialogButton
import com.raival.compose.file.explorer.common.compose.InputDialogInput
import com.raival.compose.file.explorer.common.extension.isValidAsFileName
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab

@Composable
fun RenameFileDialog(tab: FilesTab) {
    if (tab.renameDialog.showRenameFileDialog && tab.selectedFiles.isNotEmpty() && tab.renameDialog.targetFile != null) {
        val targetFile = tab.renameDialog.targetFile!!

        InputDialog(
            title = stringResource(R.string.rename),
            inputs = arrayListOf(
                InputDialogInput(
                    label = stringResource(R.string.name),
                    content = targetFile.getName()
                ) {
                    if (!it.isValidAsFileName()) {
                        globalClass.getString(R.string.invalid_file_name)
                    } else {
                        null
                    }
                }
            ),
            buttons = arrayListOf(
                InputDialogButton(globalClass.getString(R.string.rename)) { inputs ->
                    val input = inputs[0]
                    if (input.content.isValidAsFileName()) {
                        val similarFile = tab.activeFolder.findFile(input.content)
                        if (similarFile == null) {
                            targetFile.renameTo(input.content)
                            with(tab) {
                                highlightedFiles.apply {
                                    clear()
                                    add(targetFile.path)
                                }
                                unselectAllFiles(false)
                                reloadFiles()
                                renameDialog.hide()
                            }
                        } else {
                            globalClass.showMsg(R.string.similar_file_exists)
                        }
                    } else {
                        globalClass.showMsg(R.string.invalid_file_name)
                    }
                },
                InputDialogButton(stringResource(R.string.cancel)) {
                    tab.renameDialog.hide()
                }
            )
        ) {
            tab.renameDialog.hide()
        }
    }
}