package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileCopy
import androidx.compose.material.icons.rounded.FormatColorText
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.OpenInNewOff
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.DefaultOpeningMethods
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkBundleFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.task.ApksMergeTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.ApksMergeTaskParameters
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.CopyTask
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileIcon
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRow
import com.raival.compose.file.explorer.screen.viewer.audio.ui.PlaylistBottomSheet

@Composable
fun FileOptionsMenuDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current

        val targetFiles by remember {
            mutableStateOf(tab.selectedFiles.map { it.value }.toList())
        }

        val targetContentHolder = tab.targetFile!!

        val selectedFilesCount = targetFiles.size
        val isMultipleSelection = selectedFilesCount > 1
        val isSingleFile = !isMultipleSelection && targetContentHolder.isFile()
        val isSingleFolder = !isMultipleSelection && targetContentHolder.isFolder
        val isAudioFile = isSingleFile && targetContentHolder is LocalFileHolder && 
                         audioFileType.contains(targetContentHolder.file.extension)
        
        // Check for multiple audio files
        val audioFiles = targetFiles.filter { file ->
            file is LocalFileHolder && file.isFile() && audioFileType.contains(file.file.extension)
        }.map { it as LocalFileHolder }
        val hasMultipleAudioFiles = audioFiles.size > 1
        val hasAnyAudioFiles = audioFiles.isNotEmpty()

        var showPlaylistDialog by remember { mutableStateOf(false) }

        var hasFolders = false
        tab.selectedFiles.forEach {
            if (it.component2().isFolder) {
                hasFolders = true
                return@forEach
            }
        }

        BottomSheetDialog(onDismissRequest = { tab.toggleFileOptionsMenu(null) }) {
            var details by remember {
                mutableStateOf(
                    if (selectedFilesCount > 1) {
                        "and %d more".format(selectedFilesCount - 1)
                    } else {
                        emptyString
                    }
                )
            }

            LaunchedEffect(Unit) {
                if (details.isEmpty()) details = targetContentHolder.getDetails()
            }

            ItemRow(
                title = targetContentHolder.displayName,
                subtitle = details,
                ignoreSizePreferences = true,
                icon = {
                    FileIcon(
                        contentHolder = targetContentHolder,
                        ignoreSizePreferences = true
                    )
                }
            )

            Space(size = 6.dp)
            HorizontalDivider()
            Space(size = 6.dp)

            Row {
                // Delete
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleDeleteConfirmationDialog(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // Cut
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.unselectAllFiles()
                        globalClass.taskManager.addTask(
                            CopyTask(
                                targetFiles,
                                deleteSourceFiles = true
                            )
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.ContentCut, contentDescription = null)
                }

                // Copy
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.unselectAllFiles()
                        globalClass.taskManager.addTask(
                            CopyTask(
                                targetFiles,
                                deleteSourceFiles = false
                            )
                        )
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.FileCopy, contentDescription = null)
                }

                // Rename
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleRenameDialog(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatColorText,
                        contentDescription = null
                    )
                }

                // Share
                if (!hasFolders && targetContentHolder is LocalFileHolder) {
                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onDismissRequest()
                            tab.shareSelectedFiles(context)
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null)
                    }
                }

                // Properties
                IconButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onDismissRequest()
                        tab.toggleFilePropertiesDialog(true)
                    }
                ) {
                    Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                }
            }

            Space(size = 6.dp)
            HorizontalDivider()

            if (isSingleFolder) {
                FileOption(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.open_in_new_tab)
                ) {
                    onDismissRequest()
                    tab.requestNewTab(FilesTab(targetContentHolder))
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile && targetContentHolder is LocalFileHolder) {
                FileOption(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    stringResource(R.string.open_with)
                ) {
                    onDismissRequest()
                    tab.toggleOpenWithDialog(true)
                }
            }

            if (tab.activeFolder is LocalFileHolder ||
                (tab.activeFolder is VirtualFileHolder && (tab.activeFolder as VirtualFileHolder).type isNot VirtualFileHolder.BOOKMARKS)
            ) {
                FileOption(Icons.Rounded.BookmarkAdd, stringResource(R.string.add_to_bookmarks)) {
                    onDismissRequest()
                    globalClass.preferencesManager.bookmarks += targetFiles.map { it.uniquePath }
                        .distinct()
                    globalClass.showMsg(R.string.added_to_bookmarks)
                    tab.unselectAllFiles()
                }
                FileOption(Icons.Rounded.PushPin, stringResource(R.string.pin_to_home_tab)) {
                    onDismissRequest()
                    val oldSet = globalClass.preferencesManager.pinnedFiles
                    globalClass.preferencesManager.pinnedFiles =
                        oldSet + targetFiles.map { it.uniquePath }
                    globalClass.showMsg(R.string.done)
                    tab.unselectAllFiles()
                }
            }

            if (isRequestPinShortcutSupported(context) && tab.activeFolder is LocalFileHolder && (isSingleFile || isSingleFolder)) {
                FileOption(Icons.Rounded.Home, stringResource(R.string.add_to_home_screen)) {
                    onDismissRequest()
                    tab.addToHomeScreen(context, targetContentHolder as LocalFileHolder)
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile && targetContentHolder is LocalFileHolder) {
                FileOption(Icons.Rounded.EditNote, stringResource(R.string.edit_with_text_editor)) {
                    onDismissRequest()
                    globalClass.textEditorManager.openTextEditor(targetContentHolder, context)
                    tab.unselectAllFiles()
                }

                if (isAudioFile) {
                    FileOption(Icons.AutoMirrored.Rounded.PlaylistAdd, stringResource(R.string.add_to_playlist)) {
                        showPlaylistDialog = true
                    }
                }

                if (apkBundleFileType.contains(targetContentHolder.file.extension)) {
                    FileOption(Icons.Rounded.Merge, stringResource(R.string.convert_to_apk)) {
                        onDismissRequest()
                        globalClass.taskManager.addTaskAndRun(
                            ApksMergeTask(targetContentHolder),
                            ApksMergeTaskParameters(
                                globalClass.preferencesManager.signMergedApkBundleFiles
                            )
                        )
                        tab.unselectAllFiles()
                    }
                }
            }

            if (hasMultipleAudioFiles) {
                FileOption(Icons.AutoMirrored.Rounded.PlaylistAdd, stringResource(R.string.add_multiple_to_playlist)) {
                    showPlaylistDialog = true
                }
            }

            if (tab.activeFolder !is ZipFileHolder) {
                FileOption(Icons.Rounded.Compress, stringResource(R.string.compress)) {
                    globalClass.taskManager.addTask(
                        CompressTask(targetFiles)
                    )
                    onDismissRequest()
                    tab.unselectAllFiles()
                }
            }

            if (isSingleFile && targetContentHolder is LocalFileHolder) {
                FileOption(
                    Icons.Rounded.OpenInNewOff,
                    stringResource(R.string.remove_default_opening_method)
                ) {
                    fromJson<DefaultOpeningMethods>(globalClass.preferencesManager.defaultOpeningMethods)?.let {
                        globalClass.preferencesManager.defaultOpeningMethods =
                            DefaultOpeningMethods(
                                it.openingMethods.filter { it.extension != targetContentHolder.file.extension }
                            ).toJson()
                    }
                    onDismissRequest()
                }
            }
        }

        // Playlist dialog for audio files
        if (isAudioFile || hasAnyAudioFiles) {
            PlaylistBottomSheet(
                isVisible = showPlaylistDialog,
                onDismiss = { 
                    showPlaylistDialog = false 
                    onDismissRequest()
                },
                onPlaylistSelected = { playlist ->
                    showPlaylistDialog = false
                    onDismissRequest()
                    tab.unselectAllFiles()
                },
                selectedSong = if (isAudioFile && targetContentHolder is LocalFileHolder) targetContentHolder else null,
                selectedSongs = if (hasMultipleAudioFiles) audioFiles else emptyList()
            )
        }
    }
}

@Composable
fun FileOption(
    icon: ImageVector,
    text: String,
    highlight: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(21.dp),
            imageVector = icon,
            tint = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight,
            contentDescription = null
        )
        Space(size = 12.dp)
        Text(
            text = text,
            color = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight
        )
    }
}