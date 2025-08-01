package com.raival.compose.file.explorer.screen.viewer.image.misc

import android.net.Uri
import com.anggrayudi.storage.extension.toDocumentFile
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.common.toFormattedSize

// Data class for image information
data class ImageInfo(
    val name: String,
    val size: String,
    val dimensions: String,
    val format: String,
    val lastModified: String,
    val path: String
) {
    companion object {
        // Helper function to extract image information
        fun extractImageInfo(uri: Uri, width: String, height: String): ImageInfo {
            val file = uri.toDocumentFile(globalClass)

            return ImageInfo(
                name = file?.name ?: emptyString,
                size = (file?.length() ?: 0).toFormattedSize(),
                dimensions = if (width.isNotEmpty() && height.isNotEmpty()) "$width × $height" else globalClass.getString(
                    R.string.unknown
                ),
                format = globalClass.contentResolver.getType(uri)
                    ?.substringAfter("image/", globalClass.getString(R.string.not_available))
                    ?.uppercase()
                    ?: globalClass.getString(R.string.not_available),
                lastModified = (file?.lastModified() ?: 0).toFormattedDate(),
                path = uri.path.toString()
            )
        }
    }
}