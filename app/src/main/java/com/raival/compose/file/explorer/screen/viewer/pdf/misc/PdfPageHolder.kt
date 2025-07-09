package com.raival.compose.file.explorer.screen.viewer.pdf.misc

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.util.Size
import androidx.core.graphics.createBitmap
import com.raival.compose.file.explorer.App.Companion.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class PdfPageHolder(
    val index: Int,
    var size: Size = Size(0, 0),
    var bitmap: Bitmap? = null,
    var isLoading: Boolean = false
) {
    private var job: Job? = null

    fun fetch(
        scale: Float,
        scope: CoroutineScope,
        mutex: Mutex,
        pdfRenderer: PdfRenderer,
        debounceMs: Long = 150L,
        onFinished: () -> Unit
    ) {
        if (isLoading || bitmap != null) return

        job?.cancel()
        job = scope.launch {
            // Small debounce to avoid loading during fast scroll
            delay(debounceMs)

            isLoading = true
            try {
                mutex.withLock {
                    if (job?.isCancelled == false && bitmap == null) {
                        pdfRenderer.openPage(index).use { page ->
                            size = Size(page.width, page.height)
                            val newBitmap = createBitmap(
                                (size.width * scale).toInt(),
                                (size.height * scale).toInt()
                            )
                            newBitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(newBitmap, null, null, RENDER_MODE_FOR_DISPLAY)

                            if (job?.isCancelled == false) {
                                bitmap = newBitmap
                                onFinished()
                            } else {
                                recycle()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.logError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun recycle() {
        job?.cancel()
        bitmap?.recycle()
        bitmap = null
        isLoading = false
    }
}