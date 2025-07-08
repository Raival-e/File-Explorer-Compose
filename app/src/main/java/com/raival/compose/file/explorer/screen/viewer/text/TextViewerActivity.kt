package com.raival.compose.file.explorer.screen.viewer.text

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsonFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.textEditor.holder.SymbolHolder
import com.raival.compose.file.explorer.screen.textEditor.language.json.JsonCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.language.kotlin.KotlinCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.ui.BottomBarView
import com.raival.compose.file.explorer.screen.textEditor.ui.InfoBar
import com.raival.compose.file.explorer.screen.textEditor.ui.JumpToPositionDialog
import com.raival.compose.file.explorer.screen.textEditor.ui.SearchPanel
import com.raival.compose.file.explorer.screen.textEditor.ui.WarningDialog
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.text.ui.ToolbarView
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.subscribeAlways

class TextViewerActivity : ViewerActivity() {
    private lateinit var codeEditor: CodeEditor

    override fun onCreateNewInstance(
        uri: Uri,
        uid: String
    ): ViewerInstance {
        return TextViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        val textViewerInstance = instance as TextViewerInstance
        codeEditor = textViewerInstance.createCodeEditorView(this)
        setContent {
            FileExplorerTheme {
                SafeSurface {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val symbols = remember { mutableStateListOf<SymbolHolder>() }
                        val activityScope = rememberCoroutineScope()

                        LaunchedEffect(Unit) {
                            textViewerInstance.readContent(scope = activityScope) {
                                codeEditor.apply {
                                    setText(textViewerInstance.content, false, null)
                                    subscribeAlways<SelectionChangeEvent> {
                                        textViewerInstance.selectionChangeListener(
                                            this
                                        )
                                    }
                                    subscribeAlways<PublishSearchResultEvent> {
                                        textViewerInstance.selectionChangeListener(
                                            this
                                        )
                                    }
                                    subscribeAlways<ContentChangeEvent> {
                                        textViewerInstance.content = it.editor.text.toString()
                                        textViewerInstance.requireSave = true
                                        textViewerInstance.canUndo = canUndo()
                                        textViewerInstance.canRedo = canRedo()
                                    }
                                }
                                val name = textViewerInstance.uriContent.name
                                if (name != null) {
                                    if (name.endsWith(javaFileType)) {
                                        codeEditor.setEditorLanguage(JavaLanguage())
                                        textViewerInstance.changeFontStyle(codeEditor)
                                    } else if (name.endsWith(kotlinFileType)) {
                                        codeEditor.setEditorLanguage(KotlinCodeLanguage())
                                        textViewerInstance.changeFontStyle(codeEditor)
                                    } else if (name.endsWith(jsonFileType)) {
                                        codeEditor.setEditorLanguage(JsonCodeLanguage())
                                        textViewerInstance.changeFontStyle(codeEditor)
                                    } else {
                                        codeEditor.setEditorLanguage(EmptyLanguage())
                                    }
                                }
                            }

                            symbols.addAll(textViewerInstance.updateSymbols())
                        }

                        BackHandler(enabled = textViewerInstance.showSearchPanel) {
                            textViewerInstance.hideSearchPanel(codeEditor)
                        }

                        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                            textViewerInstance.checkActiveFileValidity(
                                onSourceReload = {
                                    codeEditor.setText(it, true, null)
                                },
                                onFileNotFound = {
                                    globalClass.showMsg(getString(R.string.file_no_longer_exists))
                                    finish()
                                }
                            )
                        }

                        if (textViewerInstance.warningDialogProperties.showWarningDialog) {
                            WarningDialog(textViewerInstance.warningDialogProperties)
                        }

                        if (textViewerInstance.showJumpToPositionDialog) {
                            JumpToPositionDialog(codeEditor) {
                                textViewerInstance.showJumpToPositionDialog = false
                            }
                        }

                        ToolbarView(textViewerInstance, codeEditor, onBackPressedDispatcher)
                        HorizontalDivider()
                        InfoBar(textViewerInstance.activitySubtitle)
                        AndroidView(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            factory = { codeEditor },
                            update = {},
                            onRelease = { codeEditor.release() }
                        )
                        HorizontalDivider()
                        BottomBarView(codeEditor, symbols)
                        SearchPanel(
                            codeEditor,
                            textViewerInstance.searcher,
                            textViewerInstance.showSearchPanel
                        )
                    }
                }
            }
        }
    }
}