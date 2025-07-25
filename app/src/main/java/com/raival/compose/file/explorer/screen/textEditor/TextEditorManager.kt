package com.raival.compose.file.explorer.screen.textEditor

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.view.ViewGroup
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.isDarkTheme
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.whiteSpace
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsonFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.xmlFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.Language.LANGUAGE_JAVA
import com.raival.compose.file.explorer.screen.main.tab.files.misc.Language.LANGUAGE_JSON
import com.raival.compose.file.explorer.screen.main.tab.files.misc.Language.LANGUAGE_KOTLIN
import com.raival.compose.file.explorer.screen.main.tab.files.misc.Language.LANGUAGE_XML
import com.raival.compose.file.explorer.screen.textEditor.holder.SymbolHolder
import com.raival.compose.file.explorer.screen.textEditor.language.json.JsonCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.language.kotlin.KotlinCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.language.xml.XmlCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.model.Searcher
import com.raival.compose.file.explorer.screen.textEditor.model.WarningDialogProperties
import com.raival.compose.file.explorer.screen.textEditor.scheme.DarkScheme
import com.raival.compose.file.explorer.screen.textEditor.scheme.LightScheme
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.PublishSearchResultEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.subscribeAlways
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

class TextEditorManager {
    val scope = CoroutineScope(Dispatchers.IO)
    var showSearchPanel by mutableStateOf(false)
    val warningDialogProperties = WarningDialogProperties()
    val recentFileDialog = RecentFileDialog()

    private val untitledFileName = "untitled.txt"

    private val textEditorDir = LocalFileHolder(
        File(globalClass.appFiles.uniquePath, "textEditor").apply { if (!exists()) mkdirs() }
    )

    private val customSymbolsFile = LocalFileHolder(
        File(textEditorDir.file, "symbols.txt")
    )

    private val tempFile = LocalFileHolder(File(textEditorDir.file, untitledFileName))

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

    private var defaultSymbolHolders = arrayListOf(
        SymbolHolder("_"),
        SymbolHolder("="),
        SymbolHolder("{"),
        SymbolHolder("}"),
        SymbolHolder(File.separator),
        SymbolHolder("\\"),
        SymbolHolder("<"),
        SymbolHolder(">"),
        SymbolHolder("|"),
        SymbolHolder("?"),
        SymbolHolder("+"),
        SymbolHolder("-"),
        SymbolHolder("*")
    )

    private var customSymbolHolders = arrayListOf<SymbolHolder>()

    fun updateSymbols() {
        if (!customSymbolsFile.exists()) {
            customSymbolsFile.writeText(
                GsonBuilder().setPrettyPrinting().create().toJson(defaultSymbolHolders)
            )
        }

        try {
            customSymbolHolders = Gson().fromJson(
                customSymbolsFile.readText(),
                object : TypeToken<ArrayList<SymbolHolder>>() {}.type
            )
        } catch (e: Exception) {
            logger.logError(e)
            globalClass.showMsg(R.string.failed_to_load_symbols_file)
        }
    }

    fun getSymbols(update: Boolean = false): ArrayList<SymbolHolder> {
        if (customSymbolHolders.isNotEmpty()) return customSymbolHolders

        if (update) updateSymbols()

        return customSymbolHolders.ifEmpty { defaultSymbolHolders }
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

            if (activeFile.lastModified != getFileInstance()?.lastModified) {
                scope.launch {
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
            warningDialogProperties.onConfirm = {
                getFileInstance()?.apply {
                    lastModified = activeFile.lastModified
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
        contentHolder: LocalFileHolder = activeFile,
        bringToTop: Boolean = false
    ): FileInstance? {
        val index = fileInstanceList.indexOfFirst { it.file.uniquePath == contentHolder.uniquePath }

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
        scope.launch {
            fileInstance.apply {
                file.writeText(fileInstance.content.toString())
                lastModified = fileInstance.file.lastModified
                requireSave = false
                requireSaveCurrentFile = false
            }
            withContext(Dispatchers.Main) { onSaved() }
            isSaving = false
        }
    }

    fun save(onSaved: () -> Unit, onFailed: () -> Unit) {
        isSaving = true
        scope.launch {
            getFileInstance()?.let { instance ->
                activeFile.writeText(instance.content.toString())
                instance.lastModified = activeFile.lastModified
                instance.requireSave = false
                requireSaveCurrentFile = false
                withContext(Dispatchers.Main) { onSaved().also { isSaving = false } }
            } ?: withContext(Dispatchers.Main) { onFailed().also { isSaving = false } }
        }
    }

    fun getLightScheme() = LightScheme()

    fun getDarkScheme() = DarkScheme()

    fun resetColorScheme(codeEditor: CodeEditor, isTextmate: Boolean) {
        codeEditor.apply {
            if (isTextmate) {
                ensureTextmateTheme(codeEditor)
                if (globalClass.isDarkTheme()) {
                    ThemeRegistry.getInstance().setTheme("dark")
                } else {
                    ThemeRegistry.getInstance().setTheme("light")
                }
                adaptCodeEditorScheme(colorScheme)
            } else {
                colorScheme = if (globalClass.isDarkTheme()) getDarkScheme() else getLightScheme()
                adaptCodeEditorScheme(colorScheme)
            }
        }
    }

    fun ensureTextmateTheme(codeEditor: CodeEditor) {
        try {
            var editorColorScheme = codeEditor.colorScheme
            if (editorColorScheme !is TextMateColorScheme) {
                editorColorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
                codeEditor.colorScheme = editorColorScheme
            }
        } catch (e: Exception) {
            logger.logError(e)
        }
    }

    fun adaptCodeEditorScheme(scheme: EditorColorScheme) {
        val colorScheme = if (globalClass.isDarkTheme()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(globalClass)
            } else {
                darkColorScheme()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(globalClass)
            } else {
                lightColorScheme()
            }
        }
        scheme.apply {
            setColor(
                EditorColorScheme.LINE_NUMBER_CURRENT,
                colorScheme.onSurface.toArgb()
            )
            setColor(
                EditorColorScheme.SELECTION_HANDLE,
                colorScheme.primary.toArgb()
            )
            setColor(
                EditorColorScheme.SELECTION_INSERT,
                colorScheme.primary.toArgb()
            )
            setColor(
                EditorColorScheme.SELECTED_TEXT_BACKGROUND,
                colorScheme.primary.copy(alpha = 0.3f).toArgb()
            )
            setColor(
                EditorColorScheme.CURRENT_LINE,
                colorScheme.surfaceContainerHigh.toArgb()
            )
            setColor(
                EditorColorScheme.WHOLE_BACKGROUND,
                colorScheme.surfaceContainerLowest.toArgb()
            )
            setColor(
                EditorColorScheme.LINE_NUMBER_BACKGROUND,
                colorScheme.surfaceContainer.toArgb()
            )
            setColor(
                EditorColorScheme.LINE_NUMBER,
                colorScheme.onSurface.copy(alpha = 0.5f).toArgb()
            )
            setColor(
                EditorColorScheme.MATCHED_TEXT_BACKGROUND,
                colorScheme.surfaceVariant.toArgb()
            )
            setColor(EditorColorScheme.HIGHLIGHTED_DELIMITERS_FOREGROUND, Color.Red.toArgb())
        }
    }

    fun setCodeEditorLanguage(codeEditor: CodeEditor, language: Int) {
        when (language) {
            LANGUAGE_JAVA -> {
                codeEditor.apply {
                    setEditorLanguage(JavaLanguage())
                }
            }

            LANGUAGE_KOTLIN -> {
                codeEditor.apply {
                    setEditorLanguage(KotlinCodeLanguage())
                }
            }

            LANGUAGE_JSON -> {
                codeEditor.apply {
                    setEditorLanguage(JsonCodeLanguage())
                }
            }

            LANGUAGE_XML -> {
                codeEditor.apply {
                    setEditorLanguage(XmlCodeLanguage())
                }
            }

            else -> {
                codeEditor.apply {
                    setEditorLanguage(EmptyLanguage())
                }
            }
        }
        resetColorScheme(codeEditor, true)
    }

    private fun analyseFile() {
        activityTitle = activeFile.displayName
        activitySubtitle = activeFile.basePath

        canFormatFile = activeFile.extension.let {
            it == jsonFileType || it == javaFileType || it == kotlinFileType || it == xmlFileType
        }
    }

    private fun setLanguage(codeEditor: CodeEditor) {
        when (activeFile.extension) {
            javaFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_JAVA)
            kotlinFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_KOTLIN)
            jsonFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_JSON)
            xmlFileType -> setCodeEditorLanguage(codeEditor, LANGUAGE_XML)
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

        scope.launch {
            isReading = true

            val text = activeFile.readText()
            val fileInstance = getFileInstance(bringToTop = true)

            if (fileInstance isNot null) {
                val isSourceChanged = fileInstance!!.lastModified != activeFile.lastModified
                val isUnsaved = fileInstance.content.toString() != text

                fileInstance.requireSave = isSourceChanged || isUnsaved
                requireSaveCurrentFile = fileInstance.requireSave

                withContext(Dispatchers.Main) {
                    onContentReady(fileInstance.content, text, isSourceChanged)
                }
            } else {
                val content = Content(text)
                val newFileInstance =
                    FileInstance(activeFile, content, activeFile.lastModified)
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
    fun openTextEditor(localFileHolder: LocalFileHolder, context: Context): Boolean {
        if (runBlocking { !localFileHolder.isValid() }) {
            globalClass.showMsg(R.string.file_not_found)
            return false
        }

        activeFile = getFileInstance(localFileHolder)?.file ?: localFileHolder

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
                globalClass.preferencesManager.let {
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

            globalClass.preferencesManager.let {
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

    class RecentFileDialog {
        var showRecentFileDialog by mutableStateOf(false)

        fun getRecentFiles(textEditorManager: TextEditorManager): SnapshotStateList<FileInstance> {
            val limit = globalClass.preferencesManager.recentFilesLimit
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

    data class FileInstance(
        val file: LocalFileHolder,
        var content: Content,
        var lastModified: Long,
        var requireSave: Boolean = false,
        val searcher: Searcher = Searcher()
    )
}