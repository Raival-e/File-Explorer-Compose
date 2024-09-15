package com.raival.compose.file.explorer.screen.textEditor.misc

import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.raival.compose.file.explorer.App
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.extension.isDarkTheme
import com.raival.compose.file.explorer.screen.textEditor.language.java.JavaCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.language.json.JsonCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.language.kotlin.KotlinCodeLanguage
import com.raival.compose.file.explorer.screen.textEditor.scheme.DarkScheme
import com.raival.compose.file.explorer.screen.textEditor.scheme.LightScheme
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

val javaLanguage: Language
    get() = JavaCodeLanguage()

val kotlinLanguage: Language
    get() = KotlinCodeLanguage()

val jsonLanguage: Language
    get() = JsonCodeLanguage()

fun getLightScheme() = LightScheme()

fun getDarkScheme() = DarkScheme()

fun resetColorScheme(codeEditor: CodeEditor, isTextmate: Boolean) {
    codeEditor.apply {
        if (isTextmate) {
            ensureTextmateTheme(codeEditor)
            if (App.globalClass.isDarkTheme()) {
                ThemeRegistry.getInstance().setTheme("dark")
            } else {
                ThemeRegistry.getInstance().setTheme("light")
            }
            val cs = colorScheme
            adaptCodeEditorScheme(cs)

            colorScheme = cs
            adaptCodeEditorScheme(colorScheme)
        } else {
            colorScheme = if (App.globalClass.isDarkTheme()) getDarkScheme() else getLightScheme()
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

fun setCodeEditorLanguage(codeEditor: CodeEditor, language: Int) {
    when (language) {
        com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_JAVA -> {
            codeEditor.apply {
                setEditorLanguage(javaLanguage)
            }
        }

        com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_KOTLIN -> {
            codeEditor.apply {
                setEditorLanguage(kotlinLanguage)
            }
        }

        com.raival.compose.file.explorer.screen.main.tab.regular.misc.Language.LANGUAGE_JSON -> {
            codeEditor.apply {
                setEditorLanguage(jsonLanguage)
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