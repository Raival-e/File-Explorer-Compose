package com.raival.compose.file.explorer.screen.viewer.pdf

import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.text.format.Formatter
import android.util.Size
import com.anggrayudi.storage.extension.toDocumentFile
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.name
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.pdf.misc.PdfMetadata
import com.raival.compose.file.explorer.screen.viewer.pdf.misc.PdfPageHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex

class PdfViewerInstance(
    override val uri: Uri,
    override val id: String
) : ViewerInstance {
    private val fileDescriptor = try {
        globalClass.contentResolver.openFileDescriptor(uri, "r")
    } catch (e: Exception) {
        logger.logError(e)
        null
    }

    private val pdfRenderer = fileDescriptor?.let {
        try {
            PdfRenderer(it)
        } catch (e: Exception) {
            logger.logError(e)
            null
        }
    }

    val pages = arrayListOf<PdfPageHolder>()
    var defaultPageSize = Size(0, 0)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()

    val metadata by lazy {
        PdfMetadata(
            name = uri.name ?: globalClass.getString(R.string.unknown),
            path = uri.toString(),
            size = fileDescriptor?.statSize ?: 0L,
            lastModified = uri.toDocumentFile(globalClass)?.lastModified() ?: 0L,
            pages = pdfRenderer?.pageCount ?: 0
        )
    }

    private var isReady = false

    fun prepare(onPrepared: (success: Boolean) -> Unit) {
        try {
            if (!isValid()) {
                onPrepared(false)
                return
            }

            if (!isReady) {
                pages.clear()

                for (i in 0 until pdfRenderer!!.pageCount) {
                    pages.add(PdfPageHolder(index = i))
                }

                val samplePageIndex = if (pages.size == 1) 0 else minOf(1, pages.size - 1)
                pdfRenderer.openPage(samplePageIndex).use { page ->
                    defaultPageSize = Size(page.width, page.height)
                }

                isReady = true
            }
            onPrepared(true)
        } catch (e: Exception) {
            logger.logError(e)
            onPrepared(false)
        }
    }

    fun getInfo(): List<Pair<String, String>> = listOf(
        globalClass.getString(R.string.name) to metadata.name,
        globalClass.getString(R.string.page_count) to metadata.pages.toString(),
        globalClass.getString(R.string.size) to Formatter.formatFileSize(
            globalClass,
            metadata.size
        ),
        globalClass.getString(R.string.path) to metadata.path,
        globalClass.getString(R.string.last_modified) to metadata.lastModified.toFormattedDate()
    )

    fun renderPage(page: PdfPageHolder, scale: Float = 2f, onFinished: (PdfPageHolder) -> Unit) {
        if (pdfRenderer != null) {
            page.render(scale, scope, mutex, pdfRenderer, onFinished = { onFinished(page) })
        }
    }

    fun recycle(page: PdfPageHolder) {
        page.recycle()
    }

    fun isValid(): Boolean = pdfRenderer isNot null

    override fun onClose() {
        runBlocking {
            try {
                scope.cancel()
                pages.forEach { it.recycle() }
                pdfRenderer?.close()
                fileDescriptor?.close()
            } catch (e: Exception) {
                logger.logError(e)
            }
        }
    }
}