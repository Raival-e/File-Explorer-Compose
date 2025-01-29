package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SaveAs
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun ToolbarView(codeEditor: CodeEditor, onBackPressedDispatcher: OnBackPressedDispatcher) {
    val textEditorManager = globalClass.textEditorManager
    var showOptionsMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = colorScheme.surfaceContainer)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
        }

        Column(
            Modifier
                .weight(1f)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = textEditorManager.activityTitle,
                fontSize = 21.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(enabled = textEditorManager.canUndo, onClick = { codeEditor.undo() }) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Undo, contentDescription = null)
        }

        IconButton(enabled = textEditorManager.canRedo, onClick = { codeEditor.redo() }) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.Redo, contentDescription = null)
        }

        IconButton(
            onClick = {
                textEditorManager.save(
                    onSaved = { globalClass.showMsg(R.string.saved) },
                    onFailed = { globalClass.showMsg(R.string.failed_to_save) }
                )
            }
        ) {
            Icon(
                imageVector = if (textEditorManager.requireSaveCurrentFile) {
                    Icons.Rounded.SaveAs
                } else {
                    Icons.Rounded.Save
                }, contentDescription = null
            )
        }

        IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
            Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
            OptionsMenu(showOptionsMenu, codeEditor) { showOptionsMenu = false }
        }
    }
}
