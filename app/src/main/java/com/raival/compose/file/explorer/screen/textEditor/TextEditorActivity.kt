package com.raival.compose.file.explorer.screen.textEditor

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.compose.SafeSurface
import com.raival.compose.file.explorer.common.extension.setContent
import com.raival.compose.file.explorer.screen.textEditor.compose.BottomBarView
import com.raival.compose.file.explorer.screen.textEditor.compose.CodeEditorView
import com.raival.compose.file.explorer.screen.textEditor.compose.InfoBar
import com.raival.compose.file.explorer.screen.textEditor.compose.JumpToPositionDialog
import com.raival.compose.file.explorer.screen.textEditor.compose.RecentFilesDialog
import com.raival.compose.file.explorer.screen.textEditor.compose.SearchPanel
import com.raival.compose.file.explorer.screen.textEditor.compose.ToolbarView
import com.raival.compose.file.explorer.screen.textEditor.compose.WarningDialog
import com.raival.compose.file.explorer.ui.theme.FileExplorerTheme
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

class TextEditorActivity : BaseActivity() {
    private val textEditorManager = globalClass.textEditorManager
    private lateinit var codeEditor: CodeEditor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()
    }

    override fun onPermissionGranted() {
        codeEditor = textEditorManager.createCodeEditorView(this)

        textEditorManager.readActiveFileContent { content, newText, isSourceChanged ->
            codeEditor.setContent(content, textEditorManager.getFileInstance()!!)

            if (isSourceChanged) {
                textEditorManager.showSourceFileWarningDialog {
                    codeEditor.setContent(Content(newText), textEditorManager.getFileInstance()!!)
                }
            }
        }

        textEditorManager.updateSymbols()

        setContent {
            FileExplorerTheme {
                SafeSurface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BackHandler(enabled = textEditorManager.showSearchPanel) {
                            textEditorManager.hideSearchPanel(codeEditor)
                        }

                        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                            textEditorManager.checkActiveFileValidity(
                                onSourceReload = {
                                    codeEditor.setContent(
                                        Content(it),
                                        textEditorManager.getFileInstance()!!
                                    )
                                },
                                onFileNotFound = {
                                    textEditorManager.getFileInstance()?.let {
                                        textEditorManager.fileInstanceList.remove(it)
                                    }
                                    globalClass.showMsg(getString(R.string.file_no_longer_exists))
                                    finish()
                                }
                            )
                        }

                        WarningDialog()
                        JumpToPositionDialog(codeEditor)
                        RecentFilesDialog(codeEditor)
                        ToolbarView(codeEditor, onBackPressedDispatcher)
                        HorizontalDivider()
                        InfoBar()
                        CodeEditorView(codeEditor)
                        HorizontalDivider()
                        BottomBarView(codeEditor)
                        SearchPanel(codeEditor)
                    }
                }
            }
        }
    }
}