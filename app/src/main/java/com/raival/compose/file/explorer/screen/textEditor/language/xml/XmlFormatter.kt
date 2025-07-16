package com.raival.compose.file.explorer.screen.textEditor.language.xml

import io.github.rosemoe.sora.lang.format.Formatter
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.TextRange
import org.xml.sax.InputSource
import java.io.StringReader
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class XmlFormatter : Formatter {
    private var receiver: Formatter.FormatResultReceiver? = null
    private var isRunning = false

    override fun format(text: Content, cursorRange: TextRange) {
        isRunning = true
        receiver?.onFormatSucceed(format(text.toString()), cursorRange)
        isRunning = false
    }

    override fun formatRegion(text: Content, rangeToFormat: TextRange, cursorRange: TextRange) {}

    private fun format(txt: String): String {
        if (txt.trim().isEmpty()) return txt

        return try {
            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val inputSource = InputSource(StringReader(txt))
            val document = documentBuilder.parse(inputSource)

            val transformerFactory = TransformerFactory.newInstance()

            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

            val stringWriter = StringWriter()
            transformer.transform(DOMSource(document), StreamResult(stringWriter))
            stringWriter.toString()
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