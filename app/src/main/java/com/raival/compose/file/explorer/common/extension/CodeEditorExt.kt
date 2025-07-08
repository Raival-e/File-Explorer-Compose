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

private data class Position(val line: Int, val column: Int)

private fun CodeEditor.calculateNewCursorPosition(
    line: Int,
    column: Int,
    step: Int
): Position {
    if (step > 0) { // Moving forward
        val maxColumn = text.getColumnCount(line)
        return if (column < maxColumn) {
            Position(line, column + step)
        } else if (line < lineCount - 1) {
            Position(line + 1, 0)
        } else {
            Position(line, column)
        }
    } else { // Moving backward
        return if (column > 0) {
            Position(line, column + step)
        } else if (line > 0) {
            val prevLine = line - 1
            Position(prevLine, text.getColumnCount(prevLine))
        } else {
            Position(line, column)
        }
    }
}

fun CodeEditor.moveSelectionBy(step: Int, controlledCursor: Int) {
    var rightPos = Position(cursor.rightLine, cursor.rightColumn)
    var leftPos = Position(cursor.leftLine, cursor.leftColumn)

    when {
        controlledCursor > 0 -> {
            rightPos = calculateNewCursorPosition(rightPos.line, rightPos.column, step)
        }

        controlledCursor < 0 -> {
            leftPos = calculateNewCursorPosition(leftPos.line, leftPos.column, step)
        }
    }

    setSelectionRegion(
        rightPos.line,
        rightPos.column,
        leftPos.line,
        leftPos.column
    )
}