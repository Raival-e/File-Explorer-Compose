package com.raival.compose.file.explorer.coil.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pdfFileType
import java.io.File

class PdfFileDecoder(val source: File) : Decoder {
    override suspend fun decode(): DecodeResult? {
        ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY)
            ?.use { fileDescriptor ->
                PdfRenderer(fileDescriptor).use { pdfRenderer ->
                    pdfRenderer.openPage(0).use { page ->
                        val targetWidth = 500
                        val aspectRatio = page.height.toFloat() / page.width.toFloat()
                        val targetHeight = (targetWidth * aspectRatio).toInt()

                        val bitmap = createBitmap(targetWidth, targetHeight)
                            .also { Canvas(it).drawColor(Color.WHITE) }

                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        return DecodeResult(bitmap.asImage(), false)
                    }
                }
            }
        return null
    }

    class Factory : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val file = result.source.file().toFile()
            if (file.exists() && file.extension.lowercase() == pdfFileType) {
                return PdfFileDecoder(file)
            }
            return null
        }
    }
}