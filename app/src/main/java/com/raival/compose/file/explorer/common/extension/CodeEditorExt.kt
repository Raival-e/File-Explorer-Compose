package com.raival.compose.file.explorer.common.extension

import com.raival.compose.file.explorer.screen.textEditor.TextEditorManager
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

fun CodeEditor.setContent(content: Content, fileInstance: TextEditorManager.FileInstance) {
    setText(content.toString())
    post {
        try {
            setSelectionRegion(
                content.cursor.leftLine,
                content.cursor.leftColumn,
                content.cursor.rightLine,
                content.cursor.rightColumn
            )
        } catch (_: Exception) {
            setSelection(0, 0)
        } finally {
            ensureSelectionVisible()
            fileInstance.content = text
        }
    }
}

fun CodeEditor.moveSelectionBy(step: Int, controlledCursor: Int) {
    val maxLines = lineCount

    val currentRightLine = cursor.rightLine
    val currentRightColumn = cursor.rightColumn
    val maxColumnInCurrentRightLine = text.getColumnCount(currentRightLine)
    val canMoveRightCursorToRight = currentRightColumn < maxColumnInCurrentRightLine
    val canMoveRightCursorToLeft = currentRightColumn > 0
    val canMoveRightCursorToNextLine = currentRightLine < maxLines - 1
    val canMoveRightCursorToPreviousLine = currentRightLine > 0

    val currentLeftLine = cursor.leftLine
    val currentLeftColumn = cursor.leftColumn
    val maxColumnInCurrentLeftLine = text.getColumnCount(currentLeftLine)
    val canMoveLeftCursorToLeft = currentLeftColumn > 0
    val canMoveLeftCursorToPreviousLine = currentLeftLine > 0
    val canMoveLeftCursorToNextLine = currentLeftLine < maxLines - 1
    val canMoveLeftCursorToRight = currentLeftColumn < maxColumnInCurrentLeftLine

    val rightCursorColumn = if (controlledCursor > 0) {
        if (step > 0) {
            if (canMoveRightCursorToRight) {
                currentRightColumn + step
            } else {
                if (canMoveRightCursorToNextLine) {
                    0
                } else {
                    currentRightColumn
                }
            }
        } else {
            if (canMoveRightCursorToLeft) {
                currentRightColumn + step
            } else {
                if (canMoveRightCursorToPreviousLine) {
                    text.getColumnCount(currentRightLine + step)
                } else {
                    0
                }
            }
        }
    } else {
        currentRightColumn
    }

    val leftCursorColumn = if (controlledCursor < 0) {
        if (step > 0) {
            if (canMoveLeftCursorToRight) {
                currentLeftColumn + step
            } else {
                if (canMoveLeftCursorToNextLine) {
                    0
                } else {
                    currentLeftColumn
                }
            }
        } else {
            if (canMoveLeftCursorToLeft) {
                currentLeftColumn + step
            } else {
                if (canMoveLeftCursorToPreviousLine) {
                    text.getColumnCount(currentLeftLine + step)
                } else {
                    0
                }
            }
        }
    } else {
        currentLeftColumn
    }

    val rightCursorLine = if (controlledCursor > 0) {
        if (step > 0) {
            if (canMoveRightCursorToRight) {
                currentRightLine
            } else {
                if (canMoveRightCursorToNextLine) {
                    currentRightLine + 1
                } else {
                    currentRightLine
                }
            }
        } else {
            if (canMoveRightCursorToLeft) {
                currentRightLine
            } else {
                if (canMoveRightCursorToPreviousLine) {
                    currentRightLine - 1
                } else {
                    currentRightLine
                }
            }
        }
    } else {
        currentRightLine
    }

    val leftCursorLine = if (controlledCursor < 0) {
        if (step > 0) {
            if (canMoveLeftCursorToRight) {
                currentLeftLine
            } else {
                if (canMoveLeftCursorToNextLine) {
                    currentLeftLine + 1
                } else {
                    currentLeftLine
                }
            }
        } else {
            if (canMoveLeftCursorToLeft) {
                currentLeftLine
            } else {
                if (canMoveLeftCursorToPreviousLine) {
                    currentLeftLine - 1
                } else {
                    currentLeftLine
                }
            }
        }
    } else {
        currentLeftLine
    }

    setSelectionRegion(
        rightCursorLine,
        rightCursorColumn,
        leftCursorLine,
        leftCursorColumn
    )
}