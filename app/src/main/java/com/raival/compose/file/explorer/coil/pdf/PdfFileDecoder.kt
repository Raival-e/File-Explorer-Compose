package com.raival.compose.file.explorer.coil.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
                        val resolutionMultiplier = 0.75f
                        val bitmap = Bitmap.createBitmap(
                            (page.width * resolutionMultiplier).toInt(),
                            (page.height * resolutionMultiplier).toInt(),
                            Bitmap.Config.ARGB_8888
                        ).also { Canvas(it).drawColor(Color.WHITE) }
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