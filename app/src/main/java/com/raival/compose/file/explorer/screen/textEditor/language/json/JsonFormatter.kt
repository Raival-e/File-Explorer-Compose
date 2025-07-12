package com.raival.compose.file.explorer.screen.textEditor.language.json

import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange
import org.json.JSONArray
import org.json.JSONObject

class JsonFormatter : Formatter {
    private var receiver: Formatter.FormatResultReceiver? = null
    private var isRunning = false

    override fun format(text: Content, cursorRange: TextRange) {
        isRunning = true
        receiver?.onFormatSucceed(format(text.toString()), cursorRange)
        isRunning = false
    }

    override fun formatRegion(text: Content, rangeToFormat: TextRange, cursorRange: TextRange) {
    }

    private fun format(txt: String): String {
        if (txt.isEmpty()) return txt

        val isObject = txt.trim()[0] == '{'

        return try {
            if (isObject) JSONObject(txt).toString(2)
            else JSONArray(txt).toString(2)
        } catch (_: Exception) {
            txt
        }
    }

    override fun setReceiver(receiver: Formatter.FormatResultReceiver?) {
        this.receiver = receiver
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    override fun destroy() {
        receiver = null
    }
}