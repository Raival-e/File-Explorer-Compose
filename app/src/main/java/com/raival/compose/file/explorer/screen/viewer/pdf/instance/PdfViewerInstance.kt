package com.raival.compose.file.explorer.screen.viewer.pdf.instance

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.name
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PdfViewerInstance(override val uri: Uri, override val id: String) : ViewerInstance {
    sealed interface PageContent {
        data class BlankPage(val width: Int, val height: Int) : PageContent
        data class BitmapPage(val bitmap: Bitmap) : PageContent
    }

    class PdfPage(
        val index: Int,
        val pdfRenderer: PdfRenderer,
        val mutex: Mutex,
        val scope: CoroutineScope,
        val initDimension: Size
    ) {
        var isLoaded = false
        var job: Job? = null
        var dimension = Size(initDimension.width, initDimension.height)
        var isTemporaryPageSize = true

        var pageContent by mutableStateOf<PageContent>(
            PageContent.BlankPage(
                width = dimension.width,
                height = dimension.height
            )
        )

        private fun createBitmap() = Bitmap.createBitmap(
            dimension.width,
            dimension.height,
            Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(android.graphics.Color.WHITE)
        }

        fun load(forceLoad: Boolean = false) {
            if (forceLoad || !isLoaded) {
                job = scope.launch {
                    mutex.withLock {
                        pdfRenderer.let { renderer ->
                            renderer.openPage(index).use { page ->
                                if (isTemporaryPageSize) dimension = Size(
                                    dimension.width,
                                    (page.height * (initDimension.width.toFloat() / page.width)).toInt()
                                )

                                isTemporaryPageSize = false

                                val bitmap = createBitmap()

                                page.render(
                                    bitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )

                                isLoaded = true

                                pageContent = PageContent.BitmapPage(bitmap)
                            }
                        }
                    }
                }
            }
        }

        fun recycle(terminal: Boolean) {
            val image = (pageContent as? PageContent.BitmapPage)?.bitmap

            if (!terminal && pageContent is PageContent.BitmapPage) {
                pageContent = PageContent.BlankPage(
                    width = dimension.width,
                    height = dimension.height
                )
            }

            image?.recycle()

            isLoaded = false
        }
    }

    private val fileDescriptor = globalClass.contentResolver.openFileDescriptor(uri, "r")
    private val pdfRenderer = fileDescriptor?.let { PdfRenderer(it) }
    val isValidPdf = pdfRenderer != null
    var isLoaded = false
    var pdfFileName = uri.name ?: globalClass.getString(R.string.unknown)
    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()
    val pageCount = pdfRenderer?.pageCount ?: 0
    val pages = arrayListOf<PdfPage>()

    fun load(dimension: Size, reload: Boolean = false) {
        if (isValidPdf && (!isLoaded || reload)) {
            if (pages.isNotEmpty()) {
                pages.forEach {
                    it.recycle(true)
                }
                pages.clear()
            }

            repeat(pageCount) { index ->
                pages.add(
                    PdfPage(
                        index = index,
                        pdfRenderer = pdfRenderer!!,
                        mutex = mutex,
                        scope = scope,
                        initDimension = dimension
                    )
                )
            }

            isLoaded = true
        }
    }

    override fun onClose() {
        runBlocking {
            pages.forEach {
                runCatching {
                    it.recycle(true)
                    pdfRenderer?.close()
                    fileDescriptor?.close()
                }
            }
        }
    }
}