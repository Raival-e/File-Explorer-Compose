package com.raival.compose.file.explorer.common.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.MediaColumns.DISPLAY_NAME
import android.provider.OpenableColumns
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.log10
import kotlin.math.pow

fun <T> T.conditions(cond: T.() -> Boolean) = this.cond()

@Composable
fun Int.dp() = (this / globalClass.resources.displayMetrics.density).dp

@Composable
fun Float.dp() = (this / globalClass.resources.displayMetrics.density).dp

val Int.px: Int
    get() = (this * globalClass.resources.displayMetrics.density).toInt()

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun File.listFilesAndEmptyDirs(): List<File> {
    val result = mutableListOf<File>()

    // A queue of directories to visit. Start with the root.
    val directoryQueue = ArrayDeque<File>()
    if (isDirectory) {
        directoryQueue.add(this)
    } else if (isFile) {
        // If the starting path is just a file, add it and return.
        result.add(this)
        return result
    } else {
        // Not a valid file or directory, return an empty list.
        return emptyList()
    }

    while (directoryQueue.isNotEmpty()) {
        val dir = directoryQueue.removeFirst()
        // listFiles() can be null if the directory is not accessible
        val children = dir.listFiles() ?: continue

        if (children.isEmpty()) {
            // This directory is empty, add it to the list.
            result.add(dir)
        } else {
            // Process the contents of the non-empty directory
            for (child in children) {
                if (child.isFile) {
                    result.add(child)
                } else if (child.isDirectory) {
                    directoryQueue.add(child)
                }
            }
        }
    }
    return result
}

fun Uri.getMimeType(context: Context): String {
    // 1. Try to get the MIME type from the ContentResolver
    // This is the most reliable way for content:// URIs
    val mimeType = context.contentResolver.getType(this)
    if (mimeType != null) {
        return mimeType
    }

    // 2. If the ContentResolver fails, fall back to the file extension
    val fileExtension = MimeTypeMap.getFileExtensionFromUrl(this.toString())
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
        ?: anyFileType
}

val Uri.name: String?
    get() {
        return when (scheme) {
            "content" -> {
                val cursor = globalClass.contentResolver.query(this, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(DISPLAY_NAME)
                    if (it.moveToFirst() && nameIndex >= 0) {
                        it.getString(nameIndex)
                    } else {
                        null
                    }
                }
            }

            "file" -> {
                File(path ?: emptyString).name
            }

            else -> null
        }
    }

data class UriInfo(
    val uri: Uri,
    val name: String?,
    val size: Long?,
    val mimeType: String?,
    val lastModified: Long?,
    val extension: String?,
    val path: String?
)

fun Uri.lastModified(context: Context): Long {
    return when (scheme) {
        "content" -> {
            val cursor = context.contentResolver.query(this, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val lastModifiedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                    if (lastModifiedIndex != -1) {
                        it.getLong(lastModifiedIndex) * 1000L
                    } else {
                        0L
                    }
                } else {
                    0L
                }
            } ?: 0L
        }

        "file" -> {
            path?.let { File(it).lastModified() } ?: 0L
        }

        else -> 0L
    }
}

fun Uri.getUriInfo(context: Context): UriInfo {
    val contentResolver = context.contentResolver

    var name: String? = null
    var size: Long? = null
    var lastModified: Long? = null
    var path: String? = null

    if (scheme.equals("content", ignoreCase = true)) {
        // This is a content URI. Use ContentResolver to query metadata.
        val cursor = contentResolver.query(this, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                // Get Display Name
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }

                // Get Size
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }

                // Get Last Modified (might not be available for all providers)
                val lastModifiedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                if (lastModifiedIndex != -1) {
                    // The value is in seconds, convert to milliseconds
                    lastModified = it.getLong(lastModifiedIndex) * 1000L
                }

                // Try to get a real path. WARNING: This is not reliable and often returns null.
                // It's most likely to work for MediaStore URIs.
                try {
                    val pathIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                    if (pathIndex != -1) {
                        path = it.getString(pathIndex)
                    }
                } catch (e: Exception) {
                    // The DATA column might not exist for this URI, which is normal.
                    path = null
                }
            }
        }
    } else if (scheme.equals("file", ignoreCase = true)) {
        // This is a file URI.
        this.path?.let { filePath ->
            val file = File(filePath)
            if (file.exists()) {
                name = file.name
                size = file.length()
                lastModified = file.lastModified()
                path = file.absolutePath
            }
        }
    }

    // Get MIME type from ContentResolver, which is more reliable.
    val mimeType = contentResolver.getType(this)

    // Get extension from the file name.
    val extension = name?.substringAfterLast('.', "")

    return UriInfo(
        uri = this,
        name = name,
        size = size,
        mimeType = mimeType,
        lastModified = lastModified,
        extension = extension,
        path = path
    )
}

fun Uri.exists(context: Context): Boolean {
    // 1. Handle `file://` URIs
    if (scheme == "file") {
        return path?.let { File(it).exists() } ?: false
    }

    // 2. Handle `content://` URIs
    if (scheme == "content") {
        try {
            // Use openFileDescriptor for a lightweight check.
            // "r" specifies read-only access.
            context.contentResolver.openFileDescriptor(this, "r")?.use {
                // If we get here, the file descriptor was opened successfully,
                // which means the URI is valid and accessible.
                return true
            }
        } catch (e: FileNotFoundException) {
            // The content provider reported that the file doesn't exist.
            return false
        } catch (e: SecurityException) {
            // We don't have permission to read the URI.
            return false
        } catch (e: Exception) {
            // Handle other potential exceptions, such as IllegalArgumentException for a malformed URI.
            return false
        }
    }

    // 3. For other schemes or if all checks fail
    return false
}

fun String.asCodeEditorCursorCoordinates(): Pair<Int, Int> {
    val trimmedInput = trim()
    return when {
        trimmedInput.matches(Regex("\\d+")) -> Pair(trimmedInput.toInt(), 0)
        trimmedInput.matches(Regex("\\d+:\\d+")) -> {
            val parts = trimmedInput.split(":").map { it.trim().toInt() }
            Pair(parts[0], parts[1])
        }

        else -> Pair(-1, -1)
    }
}

fun Size.isZero() = width == 0 || height == 0

fun Bitmap.scale(scaleFactor: Float, filter: Boolean = false): Bitmap {
    val newWidth = (width * scaleFactor).toInt()
    val newHeight = (height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, filter)
}

fun Throwable.printFullStackTrace(): String {
    val result: Writer = StringWriter()
    val printWriter = PrintWriter(result)
    printStackTrace(printWriter)
    val stacktraceAsString = result.toString()
    printWriter.close()
    return stacktraceAsString
}

infix fun Any?.isNot(value: Any?) = this != value

fun Int.isMultipleOf100(): Boolean {
    return this % 100 == 0
}

fun showMsg(msg: String) {
    globalClass.showMsg(msg)
}

@SuppressLint("SimpleDateFormat")
fun Long.toFormattedDate(): String = SimpleDateFormat("MMM dd, hh:mm a").format(this)

fun Long.toFormattedSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        this / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}

fun Context.isDarkTheme() = (resources.configuration.uiMode
        and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

@Throws(IOException::class)
fun Uri.read(): ByteArray {
    val contentResolver = globalClass.contentResolver
    val inputStream: InputStream? = when (scheme) {
        "content" -> contentResolver.openInputStream(this)
        "file" -> FileInputStream(this.path?.let { File(it) })
        else -> null
    }

    inputStream?.use { stream -> return stream.readBytes() } ?: return ByteArray(0)
}

fun Drawable.drawableToBitmap(): Bitmap? {
    return if (this is BitmapDrawable) {
        bitmap
    } else {
        val bitmap = createBitmap(intrinsicWidth, intrinsicHeight)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap
    }
}