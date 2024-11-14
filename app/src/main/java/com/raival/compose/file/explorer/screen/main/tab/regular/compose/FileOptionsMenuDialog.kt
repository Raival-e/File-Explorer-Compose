package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.BottomSheetDialog
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab
import com.raival.compose.file.explorer.screen.main.tab.regular.task.RegularTabCompressTask
import com.raival.compose.file.explorer.screen.main.tab.regular.task.RegularTabCopyTask
import com.raival.compose.file.explorer.screen.main.tab.regular.task.RegularTabDecompressTask
import com.raival.compose.file.explorer.screen.main.tab.regular.task.RegularTabMoveTask

@Composable
fun FileOptionsMenuDialog(tab: RegularTab) {
    if (tab.fileOptionsDialog.showFileOptionsDialog && tab.fileOptionsDialog.targetFile != null) {
        val context = LocalContext.current

        val targetFiles by remember { mutableStateOf(tab.selectedFiles.map { it.value }.toList()) }
        val targetDocumentHolder = tab.fileOptionsDialog.targetFile!!

        val selectedFilesCount = targetFiles.size
        val isMultipleSelection = selectedFilesCount > 1
        val isSingleFile = !isMultipleSelection && targetDocumentHolder.isFile()
        val isSingleFolder = !isMultipleSelection && targetDocumentHolder.isFolder()

        var hasFolders = false
        tab.selectedFiles.forEach {
            if (it.component2().isFolder()) {
                hasFolders = true
                return@forEach
            }
        }

        BottomSheetDialog(onDismissRequest = { tab.fileOptionsDialog.hide() }) {
            val details by remember {
                mutableStateOf(
                    if (selectedFilesCount > 1) {
                        "and %d more".format(selectedFilesCount - 1)
                    } else {
                        targetDocumentHolder.getFormattedDetails(
                            showFolderContentCount = globalClass.preferencesManager.displayPrefs.showFolderContentCount
                        )
                    }
                )
            }

            ItemRow(
                title = targetDocumentHolder.getFileName(),
                subtitle = details,
                icon = {
                    FileIcon(documentHolder = targetDocumentHolder)
                }
            )

            Space(size = 6.dp)
            HorizontalDivider()
            Space(size = 6.dp)

            Row {
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        tab.hideDocumentOptionsMenu()
                        tab.showConfirmDeleteDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        tab.hideDocumentOptionsMenu()
                        tab.addNewTask(RegularTabMoveTask(targetFiles))
                        tab.unselectAllFiles()
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null)
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        tab.hideDocumentOptionsMenu()
                        tab.addNewTask(RegularTabCopyTask(targetFiles))
                        tab.unselectAllFiles()
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.FileCopy, contentDescription = null)
                }

                if (isSingleFile || isSingleFolder) {
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            tab.hideDocumentOptionsMenu()
                            tab.renameDialog.show(targetDocumentHolder)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatColorText,
                            contentDescription = null
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        tab.hideDocumentOptionsMenu()
                        globalClass.regularTabManager.bookmarks += targetFiles.map { it.getPath() }
                            .distinct()
                        globalClass.showMsg(R.string.added_to_bookmarks)
                        tab.unselectAllFiles()
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.BookmarkAdd, contentDescription = null)
                }

                if (!hasFolders) {
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            tab.hideDocumentOptionsMenu()
                            tab.share(context, targetDocumentHolder)
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                    }
                }
            }

            Space(size = 6.dp)
            HorizontalDivider()

            if (isSingleFolder) {
                FileOption(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.open_in_new_tab)
                ) {
                    tab.hideDocumentOptionsMenu()
                    tab.requestNewTab(RegularTab(targetDocumentHolder))
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile) {
                FileOption(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.open_with)
                ) {
                    tab.hideDocumentOptionsMenu()
                    tab.openWithDialog.show(targetDocumentHolder)
                }
            }

            if (isRequestPinShortcutSupported(context) && (isSingleFile || isSingleFolder)) {
                FileOption(Icons.Rounded.Home, stringResource(R.string.add_to_home_screen)) {
                    tab.hideDocumentOptionsMenu()
                    tab.addToHomeScreen(context, targetFiles[0])
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile) {
                FileOption(Icons.Rounded.EditNote, stringResource(R.string.edit_with_text_editor)) {
                    tab.hideDocumentOptionsMenu()
                    globalClass.textEditorManager.openTextEditor(targetFiles[0], context)
                    tab.unselectAllFiles()
                }
            }

            FileOption(Icons.Rounded.Compress, stringResource(R.string.compress)) {
                tab.hideDocumentOptionsMenu()
                tab.addNewTask(RegularTabCompressTask(targetFiles))
                tab.unselectAllFiles()
            }

            if (isSingleFile && !hasFolders && targetFiles[0].isArchive()) {
                FileOption(
                    Icons.AutoMirrored.Rounded.ExitToApp,
                    stringResource(R.string.decompress)
                ) {
                    tab.hideDocumentOptionsMenu()
                    tab.addNewTask(RegularTabDecompressTask(targetFiles))
                    tab.unselectAllFiles()
                }
            }

            FileOption(Icons.Rounded.Info, stringResource(R.string.details)) {
                tab.hideDocumentOptionsMenu()
                tab.showFileProperties = true
            }
        }
    }
}