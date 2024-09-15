package com.raival.compose.file.explorer.screen.textEditor.language.java

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

open class JavaCodeLanguage : TextMateLanguage(
    GrammarRegistry.getInstance().findGrammar("source.java"),
    GrammarRegistry.getInstance().findLanguageConfiguration("source.java"),
    GrammarRegistry.getInstance(),
    ThemeRegistry.getInstance(),
    true
) {

    private val javaFormatter = JavaFormatter()
    private val newlineHandlers = arrayOf<NewlineHandler>(BraceHandler())

    override fun getFormatter(): Formatter {
        return javaFormatter
    }

    override fun getIndentAdvance(text: ContentReference, line: Int, column: Int): Int {
        val content = text.getLine(line).substring(0, column)
        return getIndentAdvance(content)
    }

    override fun useTab(): Boolean {
        return false
    }


    private fun getIndentAdvance(content: String): Int {
        val t = JavaTextTokenizer(content)
        var token: Tokens
        var advance = 0

        while (t.nextToken().also { token = it } !== Tokens.EOF) {
            if (token === Tokens.LBRACE) {
                advance++
            }
            if (token === Tokens.LPAREN) {
                advance++
            }

            if (advance > 0) {
                if (token === Tokens.RBRACE) {
                    advance--
                }
                if (token === Tokens.RPAREN) {
                    advance--
                }
            }
        }

        advance = 0.coerceAtLeast(advance)

        if (advance > 0) return 4
        return 0
    }

    override fun getNewlineHandlers(): Array<NewlineHandler> {
        return newlineHandlers
    }

    inner class BraceHandler : NewlineHandler {
        override fun matchesRequirement(
            text: Content,
            position: CharPosition,
            style: Styles?
        ): Boolean {
            val line = text.getLine(position.line)
            return !StylesUtils.checkNoCompletion(style, position) &&
                    ((getNonEmptyTextBefore(line, position.column, 1) == "{" &&
                            getNonEmptyTextAfter(line, position.column, 1) == "}") ||
                            (getNonEmptyTextBefore(line, position.column, 1) == "(" &&
                                    getNonEmptyTextAfter(line, position.column, 1) == ")"))
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