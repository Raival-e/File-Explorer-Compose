package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.isValidAsFileName
import com.raival.compose.file.explorer.common.ui.InputDialog
import com.raival.compose.file.explorer.common.ui.InputDialogButton
import com.raival.compose.file.explorer.common.ui.InputDialogInput
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FileCompressionDialog(tab: FilesTab) {
    if (tab.compressDialog.showCompressDialog) {
        InputDialog(
            title = stringResource(R.string.create_archive),
            inputs = arrayListOf(
                InputDialogInput(
                    stringResource(R.string.name),
                    "${tab.activeFolder.getName()}.zip"
                ) {
                    if (!it.isValidAsFileName()) {
                        globalClass.getString(R.string.invalid_file_name)
                    } else {
                        null
                    }
                }
            ),
            buttons = arrayListOf(
                InputDialogButton(stringResource(R.string.create)) { inputs ->
                    val input = inputs[0]
                    if (input.content.isValidAsFileName()) {
                        val zip = tab.activeFolder.createSubFile(input.content)
                            ?: tab.activeFolder.findFile(input.content)

                        if (zip != null) {
                            tab.showTasksPanel = false
                            tab.compressDialog.hide()
                            CoroutineScope(Dispatchers.IO).launch {
                                (tab.compressDialog.task)?.execute(zip, tab.taskCallback)
                            }
                        } else {
                            globalClass.showMsg(R.string.unable_to_create_file)
                        }
                    } else {
                        globalClass.showMsg(R.string.invalid_folder_name)
                    }
                },
                InputDialogButton(stringResource(R.string.cancel)) {
                    tab.compressDialog.hide()
                }
            )
        ) {
            tab.compressDialog.hide()
        }
    }
}