package com.raival.compose.file.explorer.screen.viewer.text

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.exists
import com.raival.compose.file.explorer.common.extension.getUriInfo
import com.raival.compose.file.explorer.common.extension.isDarkTheme
import com.raival.compose.file.explorer.common.extension.lastModified
import com.raival.compose.file.explorer.common.extension.whiteSpace
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsonFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.textEditor.holder.SymbolHolder
import com.raival.compose.file.explorer.screen.textEditor.model.Searcher
import com.raival.compose.file.explorer.screen.textEditor.model.WarningDialogProperties
import com.raival.compose.file.explorer.screen.textEditor.scheme.DarkScheme
import com.raival.compose.file.explorer.screen.textEditor.scheme.LightScheme
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class TextViewerInstance(
    override val uri: Uri,
    override val id: String
) : ViewerInstance {
    val uriContent = uri.getUriInfo(globalClass)

    val warningDialogProperties = WarningDialogProperties()
    val searcher = Searcher()

    var activityTitle by mutableStateOf(uriContent.name ?: globalClass.getString(R.string.unknown))
    var activitySubtitle by mutableStateOf(emptyString)
    var canUndo by mutableStateOf(false)
    var canRedo by mutableStateOf(false)
    var canFormatFile by mutableStateOf(false)
    var requireSave by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var showSearchPanel by mutableStateOf(false)
    var showJumpToPositionDialog by mutableStateOf(false)
    var content by mutableStateOf(emptyString)
    var lastModified by mutableLongStateOf(uriContent.lastModified ?: 0L)

    private val textEditorDir = LocalFileHolder(
        File(globalClass.appFiles.uniquePath, "textEditor").apply { if (!exists()) mkdirs() }
    )

    private val customSymbolsFile = LocalFileHolder(
        File(textEditorDir.file, "symbols.txt")
    )

    private val defaultSymbolHolders = listOf(
        "_", "=", "{", "}", File.separator, "\\", "<", ">", "|", "?", "+", "-", "*"
    ).map { SymbolHolder(it) }
    private var customSymbolHolders = arrayListOf<SymbolHolder>()

    fun readContent(scope: CoroutineScope, onLoaded: () -> Unit) {
        analyseFile()
        scope.launch {
            content = readSourceFile(this)
            onLoaded()
        }
    }

    private suspend fun readSourceFile(scope: CoroutineScope): String {
        return withContext(scope.coroutineContext) {
            globalClass.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: emptyString
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
        } catch (_: Exception) {
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

    private fun analyseFile() {
        activityTitle = uriContent.name ?: globalClass.getString(R.string.unknown)

        canFormatFile = uriContent.extension.let {
            it == jsonFileType || it == javaFileType || it == kotlinFileType
        }
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
        if (!uri.exists(globalClass)) {
            onFileNotFound()
            return
        }

        if (uri.lastModified(globalClass) != lastModified) {
            CoroutineScope(Dispatchers.IO).launch {
                val newText = readSourceFile(this)
                showSourceFileWarningDialog { onSourceReload(newText) }
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
                lastModified = uri.lastModified(globalClass)
                onConfirm()
                requireSave = false
                showWarningDialog = false
            }
            showWarningDialog = true
        }
    }

    fun updateSymbols(): List<SymbolHolder> {
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

        return customSymbolHolders.ifEmpty { defaultSymbolHolders }
    }

    fun save(onSaved: () -> Unit, onFailed: () -> Unit) {
        isSaving = true
        CoroutineScope(Dispatchers.IO).launch {
            globalClass.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(content)
                }
                withContext(Dispatchers.Main) { onSaved().also { isSaving = false } }
            } ?: withContext(Dispatchers.Main) { onFailed().also { isSaving = false } }
        }
    }

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

            resetColorScheme(this, true)
        }
    }

    fun selectionChangeListener(codeEditor: CodeEditor) {
        activitySubtitle = getFormattedCursorPosition(codeEditor)
    }

    fun changeFontStyle(codeEditor: CodeEditor) {
        codeEditor.typefaceText =
            Typeface.createFromAsset(globalClass.assets, "font/JetBrainsMono-Regular.ttf")
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

    override fun onClose() {

    }
}