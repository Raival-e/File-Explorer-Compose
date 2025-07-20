package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.moveSelectionBy
import com.raival.compose.file.explorer.common.ui.CustomIconButton
import com.raival.compose.file.explorer.screen.textEditor.holder.SymbolHolder
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.SelectionMovement

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarView(
    codeEditor: CodeEditor,
    symbolList: List<SymbolHolder>,
    extraButton: @Composable () -> Unit = {},
) {
    val indentChar = "    "
    var currentCursor by remember { mutableIntStateOf(1) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(color = colorScheme.surfaceContainer)
    ) {
        LazyRow(modifier = Modifier.weight(1f)) {
            item {
                SymbolBox(
                    label = "Tab",
                    onClick = {
                        if (!codeEditor.editable) return@SymbolBox

                        if (codeEditor.cursor.isSelected) {
                            codeEditor.indentSelection()
                        } else {
                            codeEditor.insertText(indentChar, indentChar.length)
                        }
                    },
                    onLongClick = {
                        if (!codeEditor.editable) return@SymbolBox

                        if (codeEditor.cursor.isSelected) {
                            codeEditor.unindentSelection()
                        }
                    }
                )
            }

            items(symbolList) { symbol ->
                SymbolBox(
                    label = symbol.label,
                    onClick = {
                        if (!codeEditor.editable) return@SymbolBox

                        codeEditor.let {
                            if (symbol.onSelectionStart.isNotEmpty()
                                && symbol.onSelectionEnd.isNotEmpty()
                                && it.cursor.isSelected
                            ) {
                                it.pasteText(
                                    buildString {
                                        append(symbol.onSelectionStart)
                                        append(it.text.substring(it.cursor.left, it.cursor.right))
                                        append(symbol.onSelectionEnd)
                                    }
                                )
                            } else {
                                it.insertText(
                                    symbol.onClick,
                                    if (symbol.onClickLength < 0) symbol.onClick.length else symbol.onClickLength
                                )
                            }
                        }

                    },
                    onLongClick = {
                        if (!codeEditor.editable) return@SymbolBox

                        if (symbol.onLongClick.isNotEmpty()) codeEditor.insertText(
                            symbol.onLongClick,
                            if (symbol.onLongClickLength < 0) symbol.onLongClick.length else symbol.onLongClickLength
                        )
                    }
                )
            }
        }

        VerticalDivider()

        Row(
            Modifier.fillMaxHeight()
        ) {
            CustomIconButton(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (!codeEditor.cursor.isSelected) {
                            codeEditor.moveOrExtendSelection(SelectionMovement.LEFT, false)
                        } else {
                            codeEditor.moveSelectionBy(-1, currentCursor)
                        }
                    },
                    onLongClick = {
                        if (codeEditor.cursor.isSelected) {
                            currentCursor = -1
                            globalClass.showMsg(R.string.controlling_left_cursor)
                        } else {
                            codeEditor.moveSelection(SelectionMovement.LINE_START)
                        }
                    }
                ),
                icon = Icons.Rounded.ChevronLeft
            )

            CustomIconButton(
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (!codeEditor.cursor.isSelected) {
                            codeEditor.moveOrExtendSelection(SelectionMovement.RIGHT, false)
                        } else {
                            codeEditor.moveSelectionBy(1, currentCursor)
                        }
                    },
                    onLongClick = {
                        if (codeEditor.cursor.isSelected) {
                            currentCursor = 1
                            globalClass.showMsg(R.string.controlling_right_cursor)
                        } else {
                            codeEditor.moveSelection(SelectionMovement.LINE_END)
                        }
                    }
                ),
                icon = Icons.Rounded.ChevronRight
            )

            extraButton()
        }
    }
}