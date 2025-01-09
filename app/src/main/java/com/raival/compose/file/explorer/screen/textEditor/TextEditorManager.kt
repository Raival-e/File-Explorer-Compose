package com.raival.compose.file.explorer.screen.textEditor

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.common.extension.whiteSpace
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.jsonFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_JAVA
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_JSON
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_KOTLIN
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.textEditor.misc.setCodeEditorLanguage
import com.raival.compose.file.explorer.screen.textEditor.model.Symbol
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class TextEditorManager {

    object RecentFileDialog {
        var showRecentFileDialog by mutableStateOf(false)

        fun getRecentFiles(textEditorManager: TextEditorManager): SnapshotStateList<FileInstance> {
            val limit = globalClass.preferencesManager.textEditorPrefs.recentFilesLimit
            var index = 0
            textEditorManager.fileInstanceList.removeIf {
                if (limit > 0 && index > limit - 1) {
                    !it.file.exists() || !it.requireSave
                } else {
                    !it.file.exists()
                }.also { index++ }
            }
            return textEditorManager.fileInstanceList
        }
    }

    object Searcher {
        var query by mutableStateOf(emptyString)
        var replace by mutableStateOf(emptyString)
        var caseSensitive by mutableStateOf(false)
        var useRegex by mutableStateOf(false)
    }

    data class FileInstance(
        val file: DocumentHolder,
        var content: Content,
        var lastModified: Long,
        var requireSave: Boolean = false,
        val searcher: Searcher = Searcher
    )

    object WarningDialogProperties {
        var showWarningDialog by mutableStateOf(false)
        var title by mutableStateOf(emptyString)
        var message by mutableStateOf(emptyString)
        var confirmText by mutableStateOf(emptyString)
        var dismissText by mutableStateOf(emptyString)
        var onConfirm: () -> Unit = {}
        var onDismiss: () -> Unit = {}
    }

    var showSearchPanel by mutableStateOf(false)
    val warningDialogProperties = WarningDialogProperties
    val recentFileDialog = RecentFileDialog

    private val untitledFileName = "untitled.txt"

    private val textEditorDir = DocumentHolder.fromFile(
        File(globalClass.appFiles.getPath(), "textEditor").apply { if (!exists()) mkdirs() }
    )

    private val customSymbolsFile =
        DocumentHolder.fromFile(File(textEditorDir.toFile(), "symbols.txt"))

    private val tempFile = textEditorDir.findFile(untitledFileName)
        ?: textEditorDir.createSubFile(untitledFileName)
        ?: throw RuntimeException(globalClass.getString(R.string.failed_to_create_temporary_file))

    var activeFile = tempFile

    var activityTitle by mutableStateOf(emptyString)
    var activitySubtitle by mutableStateOf(emptyString)
    var canFormatFile by mutableStateOf(false)
    var showJumpToPositionDialog by mutableStateOf(false)
    var canUndo by mutableStateOf(false)
    var canRedo by mutableStateOf(false)
    private var isReading by mutableStateOf(false)
    private var isSaving by mutableStateOf(false)
    var requireSaveCurrentFile by mutableStateOf(false)
    val fileInstanceList = mutableStateListOf<FileInstance>()

    private var defaultSymbols = arrayListOf(
        Symbol("_"),
        Symbol("="),
        Symbol("{"),
        Symbol("}"),
        Symbol("/"),
        Symbol("\\"),
        Symbol("<"),
        Symbol(">"),
        Symbol("|"),
        Symbol("?"),
        Symbol("+"),
        Symbol("-"),
        Symbol("*")
    )

    private var customSymbols = arrayListOf<Symbol>()

    val indentChar = "    "

    fun parseCursorPosition(input: String): Pair<Int, Int> {
        val trimmedInput = input.trim()
        return when {
            trimmedInput.matches(Regex("\\d+")) -> Pair(trimmedInput.toInt(), 0)
            trimmedInput.matches(Regex("\\d+:\\d+")) -> {
                val parts = trimmedInput.split(":").map { it.trim().toInt() }
                Pair(parts[0], parts[1])
            }

            else -> Pair(-1, -1)
        }
    }

    fun updateSymbols() {
        if (!customSymbolsFile.exists()) {
            customSymbolsFile.writeText(
                GsonBuilder().setPrettyPrinting().create().toJson(defaultSymbols)
            )
        }

        try {
            customSymbols = Gson().fromJson(
                customSymbolsFile.readText(),
                object : TypeToken<ArrayList<Symbol>>() {}.type
            )
        } catch (_: Exception) {
            globalClass.showMsg(R.string.failed_to_load_symbols_file)
        }
    }

    fun getSymbols(update: Boolean = false): ArrayList<Symbol> {
        if (customSymbols.isNotEmpty()) return customSymbols

        if (update) updateSymbols()

        return customSymbols.ifEmpty { defaultSymbols }
    }

    fun hideSearchPanel(codeEditor: CodeEditor) {
        if (showSearchPanel) toggleSearchPanel(false, codeEditor)
    }

    fun toggleSearchPanel(value: Boolean = !showSearchPanel, codeEditor: CodeEditor) {
        showSearchPanel = value
        if (!value) codeEditor.searcher.stopSearch()
    }

    fun checkActiveFileValidity(
        onSourceReload: (newText: String) -> Unit,
        onFileNotFound: () -> Unit
    ) {
        if (getFileInstance() isNot null) { // to prevent checking when no file instance has yet been created
            if (!activeFile.exists()) {
                onFileNotFound()
            }

            if (activeFile.getLastModified() != getFileInstance()?.lastModified) {
                CoroutineScope(Dispatchers.IO).launch {
                    val newText = activeFile.readText()
                    showSourceFileWarningDialog { onSourceReload(newText) }
                }
            }
        }
    }

    fun showSourceFileWarningDialog(onConfirm: () -> Unit) {
        warningDialogProperties.apply {
            title = globalClass.getString(R.string.warning)
            message = globalClass.getString(R.string.changed_source_file)
            confirmText = globalClass.getString(R.string.reload)
            dismissText = globalClass.getString(R.string.cancel)
            onDismiss = { showWarningDialog = false }
            WarningDialogProperties.onConfirm = {
                getFileInstance()?.apply {
                    lastModified = activeFile.getLastModified()
                    onConfirm()
                    requireSave = false
                }
                requireSaveCurrentFile = false
                showWarningDialog = false
            }
            showWarningDialog = true
        }
    }

    fun getFileInstance(
        documentHolder: DocumentHolder = activeFile,
        bringToTop: Boolean = false
    ): FileInstance? {
        val index = fileInstanceList.indexOfFirst { it.file.getPath() == documentHolder.getPath() }

        if (index == -1) return null

        val instance = fileInstanceList[index]

        if (bringToTop) {
            fileInstanceList.removeAt(index)
            fileInstanceList.add(0, instance)
        }

        return instance
    }

    fun showSaveFileBeforeClose(fileInstance: FileInstance) {
        warningDialogProperties.apply {
            title = globalClass.getString(R.string.warning)
            message = globalClass.getString(R.string.save_file_msg)
            confirmText = globalClass.getString(R.string.save)
            dismissText = globalClass.getString(R.string.ignore_changes)
            onDismiss = {
                fileInstanceList.remove(fileInstance)
                showWarningDialog = false
            }
            onConfirm = {
                save(
                    onSaved = {
                        save(fileInstance) {
                            globalClass.showMsg(R.string.saved)
                            fileInstanceList.remove(fileInstance)
                        }
                    },
                    onFailed = { globalClass.showMsg(R.string.failed_to_save) }
                )
                showWarningDialog = false
            }
            showWarningDialog = true
        }
    }

    private fun save(fileInstance: FileInstance, onSaved: () -> Unit) {
        isSaving = true
        CoroutineScope(Dispatchers.IO).launch {
            fileInstance.apply {
                file.writeText(fileInstance.content.toString())
                lastModified = fileInstance.file.getLastModified()
                requireSave = false
                requireSaveCurrentFile = false
            }
            withContext(Dispatchers.Main) { onSaved() }
            isSaving = false
        }
    }

    fun save(onSaved: () -> Unit, onFailed: () -> Unit) {
        isSaving = true
        CoroutineScope(Dispatchers.IO).launch {
            getFileInstance()?.let { instance ->
                activeFile.writeText(instance.content.toString())
                instance.lastModified = activeFile.getLastModified()
                instance.requireSave = false
                requireSaveCurrentFile = false
                withContext(Dispatchers.Main) { onSaved().also { isSaving = false } }
            } ?: withContext(Dispatchers.Main) { onFailed().also { isSaving = false } }
        }
    }

    private fun analyseFile() {
        activityTitle = activeFile.getFileName()
        activitySubtitle = activeFile.getBasePath()

        canFormatFile = activeFile.getFileExtension().lowercase(Locale.getDefault()).let {
            it == jsonFileType || it == javaFileType || it == kotlinFileType
        }
    }

    private fun setLanguage(codeEditor: CodeEditor) {
        when (activeFile.getFileExtension().lowercase(Locale.getDefault())) {
            javaFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_JAVA)
            kotlinFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_KOTLIN)
            jsonFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_JSON)
            else -> setCodeEditorLanguage(codeEditor, -1)
        }
    }

    fun switchActiveFileTo(
        fileInstance: FileInstance,
        codeEditor: CodeEditor,
        onContentReady: (content: Content, text: String, isSourceFileChanged: Boolean) -> Unit
    ) {
        if (!fileInstance.file.exists() && !fileInstance.file.isFile()) {
            fileInstanceList.remove(fileInstance)
            globalClass.showMsg(R.string.file_not_found)
        }

        activeFile = fileInstance.file

        analyseFile()

        setLanguage(codeEditor)

        readActiveFileContent { content, text, isSourceFileChanged ->
            onContentReady(
                content,
                text,
                isSourceFileChanged
            )
        }
    }

    fun readActiveFileContent(
        onContentReady: (
            content: Content,
            text: String,
            isSourceFileChanged: Boolean
        ) -> Unit
    ) {
        analyseFile()

        CoroutineScope(Dispatchers.IO).launch {
            isReading = true

            val text = activeFile.readText()
            val fileInstance = getFileInstance(bringToTop = true)

            if (fileInstance isNot null) {
                val isSourceChanged = fileInstance!!.lastModified != activeFile.getLastModified()
                val isUnsaved = fileInstance.content.toString() != text

                fileInstance.requireSave = isSourceChanged || isUnsaved
                requireSaveCurrentFile = fileInstance.requireSave

                withContext(Dispatchers.Main) {
                    onContentReady(fileInstance.content, text, isSourceChanged)
                }
            } else {
                val content = Content(text)
                val newFileInstance =
                    FileInstance(activeFile, content, activeFile.getLastModified())
                fileInstanceList.add(0, newFileInstance)

                withContext(Dispatchers.Main) {
                    onContentReady(content, text, false)
                }
            }

            isReading = false
        }
    }

    /**
     * Opens a file in text editor.
     * return true if the file exists, false otherwise
     */
    fun openTextEditor(documentHolder: DocumentHolder, context: Context): Boolean {
        if (!documentHolder.exists()) {
            globalClass.showMsg(R.string.file_not_found)
            return false
        }

        activeFile = getFileInstance(documentHolder)?.file ?: documentHolder

        context.startActivity(Intent(context, TextEditorActivity::class.java))

        return true
    }

    private fun getFormattedCursorPosition(codeEditor: CodeEditor) = buildString {
        val cursor = codeEditor.cursor

        append(
            globalClass.getString(
                R.string.cursor_position,
                cursor.leftLine + 1,
                cursor.leftColumn + 1
            )
        )

        append(whiteSpace)

        if (cursor.isSelected) {
            val selectionCount = cursor.right - cursor.left
            append("($selectionCount)")
        }

        val searcher = codeEditor.searcher
        if (searcher.hasQuery()) {
            val idx = searcher.currentMatchedPositionIndex

            append(whiteSpace)
            append(globalClass.getString(R.string.text_editor_search_result))

            if (idx == -1) {
                append("${searcher.matchedPositionCount}")
            } else {
                append("${idx + 1}/${searcher.matchedPositionCount}")
            }
        }
    }

    private fun selectionChangeListener(codeEditor: CodeEditor) {
        activitySubtitle = getFormattedCursorPosition(codeEditor)
    }

    /**
     * Creates a new code editor view.
     */
    fun createCodeEditorView(context: Context): CodeEditor {
        return CodeEditor(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            props.apply {
                globalClass.preferencesManager.textEditorPrefs.let {
                    useICULibToSelectWords = it.useICULibToSelectWords
                    symbolPairAutoCompletion = it.symbolPairAutoCompletion
                    deleteEmptyLineFast = it.deleteEmptyLineFast
                    deleteMultiSpaces = if (it.deleteMultiSpaces) -1 else 1
                    autoIndent = it.autoIndent
                    boldMatchingDelimiters = false
                    formatPastedText = true
                    isWordwrap = it.wordWrap
                }
            }

            globalClass.preferencesManager.textEditorPrefs.let {
                editable = !it.readOnly
                setPinLineNumber(it.pinLineNumber)
                getComponent(Magnifier::class.java).isEnabled = it.enableMagnifier
            }

            getComponent(EditorAutoCompletion::class.java).isEnabled = false

            typefaceText =
                Typeface.createFromAsset(context.assets, "font/JetBrainsMono-Regular.ttf")

            setLanguage(this)

            subscribeAlways<SelectionChangeEvent> { selectionChangeListener(this) }
            subscribeAlways<PublishSearchResultEvent> { selectionChangeListener(this) }
            subscribeAlways<ContentChangeEvent> {
                getFileInstance()?.let {
                    if (!it.requireSave && !isReading) {
                        it.requireSave = true
                        requireSaveCurrentFile = true
                    }
                }
                canUndo = canUndo()
                canRedo = canRedo()
            }
        }
    }
}