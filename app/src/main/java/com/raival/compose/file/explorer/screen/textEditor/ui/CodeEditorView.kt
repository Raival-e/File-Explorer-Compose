package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun ColumnScope.CodeEditorView(codeEditor: CodeEditor) {
    AndroidView(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        factory = { codeEditor },
        update = { },
        onRelease = { codeEditor.release() }
    )
}