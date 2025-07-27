package com.raival.compose.file.explorer.coil.zip

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.FileSystem
import java.util.zip.ZipFile

class ZipFileFetcher(
    private val data: ZipFileHolder,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        return withContext(Dispatchers.IO) {
            try {
                // First check if it's a supported image type
                val mimeType = getMimeTypeFromExtension(data.uniquePath)

                if (mimeType == null || !mimeType.startsWith("image/")) {
                    throw IllegalArgumentException("Unsupported file type: ${data.uniquePath}")
                }

                val bytes = ZipFile(data.zipTree.source.file).use { zip ->
                    val entry = zip.getEntry(data.uniquePath)
                        ?: throw IllegalArgumentException("Entry '${data.uniquePath}' not found in zip file")

                    if (entry.isDirectory) {
                        throw IllegalArgumentException("Cannot load directory entry: ${data.uniquePath}")
                    }

                    // Check file size to prevent OOM (limit to 10MB for images)
                    val maxSize = 10 * 1024 * 1024L // 10MB
                    if (entry.size > maxSize) {
                        throw IllegalArgumentException("Image file too large: ${entry.size / (1024 * 1024)}MB (max 10MB)")
                    }

                    zip.getInputStream(entry).use { inputStream -> inputStream.readBytes() }
                }

                SourceFetchResult(
                    source = ImageSource(
                        source = Buffer().write(bytes),
                        fileSystem = FileSystem.SYSTEM,
                        metadata = null
                    ),
                    mimeType = mimeType,
                    dataSource = DataSource.DISK
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun getMimeTypeFromExtension(path: String): String? {
        val extension = path.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "tiff" -> "image/tiff"
            "heic" -> "image/heic"
            else -> null
        }
    }

    // Factory for creating ZipFileFetcher instances
    class Factory : Fetcher.Factory<ZipFileHolder> {
        override fun create(
            data: ZipFileHolder,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return ZipFileFetcher(data, options)
        }
    }
}