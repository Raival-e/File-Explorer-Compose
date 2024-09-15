package com.raival.compose.file.explorer.screen.textEditor.scheme

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class LightScheme : EditorColorScheme() {
    override fun applyDefault() {
        super.applyDefault()
        setColor(ANNOTATION, -0x9b9b9c)
        setColor(FUNCTION_NAME, -0x1000000)
        setColor(IDENTIFIER_NAME, -0x1000000)
        setColor(IDENTIFIER_VAR, -0x479cc2)
        setColor(LITERAL, -0xd5ff01)
        setColor(OPERATOR, -0xc60000)
        setColor(COMMENT, -0xc080a1)
        setColor(KEYWORD, -0x80ff8c)
        setColor(WHOLE_BACKGROUND, -0x1)
        setColor(TEXT_NORMAL, -0x1000000)
        setColor(LINE_NUMBER_BACKGROUND, -0x1)
        setColor(LINE_NUMBER, -0x878788)
        setColor(SELECTED_TEXT_BACKGROUND, -0xcc6601)
        setColor(MATCHED_TEXT_BACKGROUND, -0x2b2b2c)
        setColor(CURRENT_LINE, -0x170d02)
        setColor(SELECTION_INSERT, -0xfc1415)
        setColor(SELECTION_HANDLE, -0xfc1415)
        setColor(BLOCK_LINE, -0x272728)
        setColor(BLOCK_LINE_CURRENT, 0)
        setColor(TEXT_SELECTED, -0x1)
    }
}