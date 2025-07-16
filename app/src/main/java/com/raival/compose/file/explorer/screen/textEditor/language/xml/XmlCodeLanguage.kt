package com.raival.compose.file.explorer.screen.textEditor.language.xml

import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.lang.styling.StylesUtils
import io.github.rosemoe.sora.langs.java.JavaTextTokenizer
import io.github.rosemoe.sora.langs.java.Tokens
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.text.TextUtils
import java.lang.Character.isWhitespace
import kotlin.math.max

class XmlCodeLanguage() : TextMateLanguage(
    GrammarRegistry.getInstance().findGrammar("text.xml"),
    GrammarRegistry.getInstance().findLanguageConfiguration("text.xml"),
    GrammarRegistry.getInstance(),
    ThemeRegistry.getInstance(),
    true
) {
    private val xmlFormatter = XmlFormatter()
    private val newlineHandlers = arrayOf<NewlineHandler>(TagHandler())

    override fun getFormatter(): Formatter {
        return xmlFormatter
    }

    override fun getIndentAdvance(text: ContentReference, line: Int, column: Int): Int {
        val content = text.getLine(line).substring(0, column)
        return getIndentAdvance(content)
    }

    override fun useTab(): Boolean {
        return false
    }

    private fun getIndentAdvance(content: String): Int {
        var advance = 0
        var i = 0

        while (i < content.length) {
            if (content[i] == '<') {
                if (i + 1 < content.length && content[i + 1] == '/') {
                    // Closing tag
                    if (advance > 0) {
                        advance--
                    }
                } else if (i + 1 < content.length && content[i + 1] != '!') {
                    // Opening tag (not comment or declaration)
                    val tagEnd = content.indexOf('>', i)
                    if (tagEnd != -1) {
                        val tagContent = content.substring(i, tagEnd + 1)
                        if (!tagContent.endsWith("/>")) {
                            // Not self-closing tag
                            advance++
                        }
                    }
                }
            }
            i++
        }

        advance = 0.coerceAtLeast(advance)
        return if (advance > 0) 4 else 0
    }

    override fun getNewlineHandlers(): Array<NewlineHandler> {
        return newlineHandlers
    }

    inner class TagHandler : NewlineHandler {
        override fun matchesRequirement(
            text: Content,
            position: CharPosition,
            style: Styles?
        ): Boolean {
            val line = text.getLine(position.line)
            return !StylesUtils.checkNoCompletion(style, position) &&
                    (isWithinTagPair(line, position.column))
        }

        override fun handleNewline(
            text: Content,
            position: CharPosition,
            style: Styles?,
            tabSize: Int
        ): NewlineHandleResult {
            val line = text.getLine(position.line)
            val index = position.column
            val beforeText = line.subSequence(0, index).toString()
            val afterText = line.subSequence(index, line.length).toString()
            return handleNewline(beforeText, afterText, tabSize)
        }

        private fun handleNewline(
            beforeText: String,
            afterText: String,
            tabSize: Int
        ): NewlineHandleResult {
            val count = TextUtils.countLeadingSpaceCount(beforeText, tabSize)
            val advanceBefore: Int = getIndentAdvance(beforeText)
            val advanceAfter: Int = getIndentAdvance(afterText)
            var text: String
            val sb = StringBuilder("\n")
                .append(TextUtils.createIndent(count + advanceBefore, tabSize, useTab()))
                .append('\n')
                .append(TextUtils.createIndent(count + advanceAfter, tabSize, useTab()).also {
                    text = it
                })
            val shiftLeft = text.length + 1
            return NewlineHandleResult(sb, shiftLeft)
        }

        private fun isWithinTagPair(text: CharSequence, index: Int): Boolean {
            val beforeNonEmpty = getNonEmptyTextBefore(text, index, 1)
            val afterNonEmpty = getNonEmptyTextAfter(text, index, 2)
            return beforeNonEmpty == ">" && afterNonEmpty.startsWith("</")
        }

        private fun getNonEmptyTextBefore(text: CharSequence, index: Int, length: Int): String {
            var indexVar = index
            while (indexVar > 0 && isWhitespace(text[indexVar - 1])) {
                indexVar--
            }
            return text.subSequence(max(0, indexVar - length), indexVar).toString()
        }

        private fun getNonEmptyTextAfter(text: CharSequence, index: Int, length: Int): String {
            var indexVar = index
            while (indexVar < text.length && isWhitespace(text[indexVar])) {
                indexVar++
            }
            return text.subSequence(indexVar, Math.min(indexVar + length, text.length)).toString()
        }
    }
}