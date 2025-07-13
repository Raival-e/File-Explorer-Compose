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

    fun render(
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

                            // Determine page orientation
                            val isLandscape = page.width > page.height

                            // Set different limits based on orientation
                            val maxWidth = if (isLandscape) 4096 else 2048
                            val maxHeight = if (isLandscape) 2048 else 6144

                            // Calculate initial scaled dimensions
                            val scaledWidth = (size.width * scale).toInt()
                            val scaledHeight = (size.height * scale).toInt()

                            // Check if dimensions exceed orientation-specific limits
                            val finalWidth: Int
                            val finalHeight: Int

                            if (scaledWidth > maxWidth || scaledHeight > maxHeight) {
                                // Calculate safe scale factors for both dimensions
                                val maxScaleForWidth = maxWidth.toFloat() / size.width
                                val maxScaleForHeight = maxHeight.toFloat() / size.height

                                // Use the most restrictive scale to ensure both dimensions are within limits
                                val safeScale = minOf(maxScaleForWidth, maxScaleForHeight)

                                finalWidth = (size.width * safeScale).toInt()
                                finalHeight = (size.height * safeScale).toInt()
                            } else {
                                finalWidth = scaledWidth
                                finalHeight = scaledHeight
                            }

                            val newBitmap = createBitmap(finalWidth, finalHeight)
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