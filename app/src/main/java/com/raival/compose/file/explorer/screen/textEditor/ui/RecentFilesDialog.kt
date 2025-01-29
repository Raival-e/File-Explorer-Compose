package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.setContent
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.common.ui.SwipeToDeleteBox
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileItemRow
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentFilesDialog(codeEditor: CodeEditor) {
    val textEditorManager = globalClass.textEditorManager

    if (textEditorManager.recentFileDialog.showRecentFileDialog) {
        BottomSheetDialog(onDismissRequest = {
            textEditorManager.recentFileDialog.showRecentFileDialog = false
        }) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.recent_files),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Space(size = 8.dp)

            LazyColumn {
                items(textEditorManager.recentFileDialog.getRecentFiles(textEditorManager)) {
                    val isActiveFile = it.file == textEditorManager.getFileInstance()?.file

                    SwipeToDeleteBox(
                        onDeleteConfirm = {
                            if (!isActiveFile) {
                                val fileInstance = textEditorManager.getFileInstance(it.file)!!
                                if (fileInstance.requireSave) {
                                    textEditorManager.showSaveFileBeforeClose(fileInstance)
                                } else {
                                    textEditorManager.fileInstanceList.remove(it)
                                }
                            } else {
                                globalClass.showMsg(R.string.this_file_is_currently_open)
                            }
                        },
                        content = {
                            Modifier
                                .fillMaxWidth()
                            Column(
                                Modifier
                                    .animateItem()
                                    .combinedClickable(
                                        onClick = {
                                            textEditorManager.switchActiveFileTo(
                                                it,
                                                codeEditor
                                            ) { content, text, isSourceChanged ->
                                                codeEditor.setContent(
                                                    content,
                                                    textEditorManager.getFileInstance()!!
                                                )
                                                if (isSourceChanged) {
                                                    textEditorManager.showSourceFileWarningDialog {
                                                        codeEditor.setContent(
                                                            Content(text),
                                                            textEditorManager.getFileInstance()!!
                                                        )
                                                    }
                                                }
                                                textEditorManager.recentFileDialog.showRecentFileDialog =
                                                    false
                                            }
                                        }
                                    )
                            ) {
                                Space(size = 4.dp)
                                Row(Modifier.fillMaxWidth()) {
                                    if (isActiveFile) Spacer(
                                        Modifier
                                            .fillMaxHeight()
                                            .padding(vertical = 8.dp)
                                            .width(6.dp)
                                            .background(
                                                color = colorScheme.primary,
                                                shape = RoundedCornerShape(0, 50, 50, 0)
                                            ),
                                    )
                                    FileItemRow(
                                        item = it.file,
                                        fileDetails = it.file.basePath,
                                        namePrefix = if (it.requireSave) "* " else emptyString
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
