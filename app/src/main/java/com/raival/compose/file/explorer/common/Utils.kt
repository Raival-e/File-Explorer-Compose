package com.raival.compose.file.explorer.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.anggrayudi.storage.extension.trimFileSeparator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.textEditor.TextEditorManager
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor
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
import java.util.UUID
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

// =============================================================================
// CONSTANTS
// =============================================================================

const val emptyString = ""

const val whiteSpace = " "

// =============================================================================
// GENERIC UTILITY EXTENSIONS
// =============================================================================

fun <T> T.conditions(cond: T.() -> Boolean) = this.cond()

infix fun Any?.isNot(value: Any?) = this != value

// =============================================================================
// COMPOSE & UI EXTENSIONS
// =============================================================================

@Composable
fun Int.dp() = (this / globalClass.resources.displayMetrics.density).dp

@Composable
fun Float.dp() = (this / globalClass.resources.displayMetrics.density).dp

fun Modifier.detectVerticalSwipe(
    onSwipeUp: () -> Unit = { },
    onSwipeDown: () -> Unit = { },
    threshold: Int = 50
) = this.pointerInput(onSwipeUp, onSwipeDown, threshold) {
    var handled = false
    detectVerticalDragGestures(
        onDragEnd = { handled = false },
        onDragCancel = { handled = false },
        onVerticalDrag = { change, dragAmount ->
            if (!handled) {
                if (dragAmount < -threshold) {
                    onSwipeUp()
                    change.consume()
                    handled = true
                } else if (dragAmount > threshold) {
                    onSwipeDown()
                    change.consume()
                    handled = true
                }
            }
        }
    )
}

fun Modifier.block(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    padding: Dp = 0.dp,
    borderColor: Color = Color.Unspecified,
    borderSize: Dp = 0.dp
) = composed {
    val color1 = if (color.isUnspecified) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else color

    this
        .clip(shape)
        .background(color = color1, shape = shape)
        .then(
            if (borderSize == 0.dp) Modifier else Modifier.border(
                width = borderSize,
                color = if (borderColor.isUnspecified)
                    MaterialTheme.colorScheme.outlineVariant(0.1f, color1) else borderColor,
                shape = shape
            )
        )
        .padding(padding)
}

private fun ColorScheme.outlineVariant(
    luminance: Float = 0.3f,
    onTopOf: Color = surfaceColorAtElevation(3.dp)
) = onSecondaryContainer
    .copy(alpha = luminance)
    .compositeOver(onTopOf)

// =============================================================================
// STRING EXTENSIONS
// =============================================================================

fun String.Companion.randomString(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString(emptyString)
}

fun String.hasParent(parentPath: String): Boolean {
    val parentTree = parentPath.getFolderTree()
    val subFolderTree = getFolderTree()
    return parentTree.size <= subFolderTree.size && subFolderTree.take(parentTree.size) == parentTree
}

private fun String.getFolderTree() =
    split('/').map { it.trimFileSeparator() }.filter { it.isNotEmpty() }

fun String.orIf(value: String, condition: (String) -> Boolean) =
    if (condition(this)) value else this

fun String.isValidAsFileName() = !conditions {
    contains(":") || contains(File.separator) || contains("*") || contains("?")
            || contains("\"") || contains("<") || contains(">")
            || contains("|")
} && isNotBlank()

fun String.copyToClipboard() {
    (globalClass.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
        ClipData.newPlainText("clipboard", this)
    )
}

fun String.trimToLastTwoSegments(): String {
    val segments = this.split(File.separator)
    return if (segments.size >= 2) {
        segments.takeLast(2).joinToString(File.separator)
    } else {
        this
    }
}

/**
 * Calculates the relative path of this string path with respect to a given [base] path string.
 * This is a string-based reimplementation of [File.toRelativeString].
 *
 * @param base The base path string to which the path should be made relative.
 * @return The relative path string, or an empty string if the paths are the same.
 * @throws IllegalArgumentException if this path does not start with the [base] path.
 */
fun String.toRelativeString(base: String): String {
    // 1. Normalize both paths for consistent comparison
    //    - Replace backslashes with forward slashes
    //    - Remove any trailing slashes
    val thisPath = this.replace('\\', '/').trimEnd('/')
    val basePath = base.replace('\\', '/').trimEnd('/')

    // 2. Handle the case where the paths are identical
    if (thisPath == basePath) {
        return emptyString
    }

    // 3. The base path must be a true prefix.
    //    We append a slash to the base path to ensure we're matching a full directory segment.
    //    This prevents "/a/b_c" from being considered relative to "/a/b".
    //    If the base path is empty, we don't add a leading slash.
    val basePrefix = if (basePath.isEmpty()) emptyString else "$basePath/"

    if (!thisPath.startsWith(basePrefix)) {
        return thisPath
    }

    // 4. The relative path is the part of this path that comes after the base prefix.
    return thisPath.substring(basePrefix.length)
}

fun String.limitLength(maxLength: Int): String {
    if (this.length <= maxLength) {
        return this
    }
    return this.substring(0, maxLength - 3) + "..."
}

fun String.toUuid(): UUID {
    return UUID.nameUUIDFromBytes(this.toByteArray())
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

// =============================================================================
// COLLECTION EXTENSIONS
// =============================================================================

fun <T> List<T>.getIndexIf(condition: T.() -> Boolean): Int {
    forEachIndexed { index, item ->
        if (condition(item)) {
            return index
        }
    }
    return -1
}

fun <A, B> HashMap<A, B>.removeIf(condition: (A, B) -> Boolean) {
    keys.forEach {
        if (condition(it, this[it]!!)) {
            remove(it)
        }
    }
}

// =============================================================================
// JSON EXTENSIONS
// =============================================================================

inline fun <reified T> fromJson(json: String?): T? {
    if (json == null) return null

    runCatching {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    return null
}

fun <T> T.toJson(prettyPrint: Boolean = true): String {
    return GsonBuilder().apply {
        if (prettyPrint) setPrettyPrinting()
    }.create().toJson(this)
}

// =============================================================================
// ANDROID CONTEXT EXTENSIONS
// =============================================================================

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun Context.isDarkTheme() = (resources.configuration.uiMode
        and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

// =============================================================================
// CODE EDITOR EXTENSIONS
// =============================================================================

fun CodeEditor.setContent(content: Content, fileInstance: TextEditorManager.FileInstance) {
    setText(content.toString())
    post {
        try {
            setSelectionRegion(
                content.cursor.leftLine,
                content.cursor.leftColumn,
                content.cursor.rightLine,
                content.cursor.rightColumn
            )
        } catch (_: Exception) {
            setSelection(0, 0)
        } finally {
            ensureSelectionVisible()
            fileInstance.content = text
        }
    }
}

data class CursorPosition(val line: Int, val column: Int)

fun CodeEditor.calculateNewCursorPosition(
    line: Int,
    column: Int,
    step: Int
): CursorPosition {
    if (step > 0) { // Moving forward
        val maxColumn = text.getColumnCount(line)
        return if (column < maxColumn) {
            CursorPosition(line, column + step)
        } else if (line < lineCount - 1) {
            CursorPosition(line + 1, 0)
        } else {
            CursorPosition(line, column)
        }
    } else { // Moving backward
        return if (column > 0) {
            CursorPosition(line, column + step)
        } else if (line > 0) {
            val prevLine = line - 1
            CursorPosition(prevLine, text.getColumnCount(prevLine))
        } else {
            CursorPosition(line, column)
        }
    }
}

fun CodeEditor.moveSelectionBy(step: Int, controlledCursor: Int) {
    var rightPos = CursorPosition(cursor.rightLine, cursor.rightColumn)
    var leftPos = CursorPosition(cursor.leftLine, cursor.leftColumn)

    when {
        controlledCursor > 0 -> {
            rightPos = calculateNewCursorPosition(rightPos.line, rightPos.column, step)
        }

        controlledCursor < 0 -> {
            leftPos = calculateNewCursorPosition(leftPos.line, leftPos.column, step)
        }
    }

    setSelectionRegion(
        rightPos.line,
        rightPos.column,
        leftPos.line,
        leftPos.column
    )
}

// =============================================================================
// FILE EXTENSIONS
// =============================================================================

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

// =============================================================================
// URI EXTENSIONS
// =============================================================================

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
                    if (lastModifiedIndex isNot -1) {
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
                if (nameIndex isNot -1) {
                    name = it.getString(nameIndex)
                }

                // Get Size
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex isNot -1) {
                    size = it.getLong(sizeIndex)
                }

                // Get Last Modified (might not be available for all providers)
                val lastModifiedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                if (lastModifiedIndex isNot -1) {
                    // The value is in seconds, convert to milliseconds
                    lastModified = it.getLong(lastModifiedIndex) * 1000L
                }

                // Try to get a real path. WARNING: This is not reliable and often returns null.
                // It's most likely to work for MediaStore URIs.
                try {
                    val pathIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                    if (pathIndex isNot -1) {
                        path = it.getString(pathIndex)
                    }
                } catch (_: Exception) {
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
        } catch (_: FileNotFoundException) {
            // The content provider reported that the file doesn't exist.
            return false
        } catch (_: SecurityException) {
            // We don't have permission to read the URI.
            return false
        } catch (_: Exception) {
            // Handle other potential exceptions, such as IllegalArgumentException for a malformed URI.
            return false
        }
    }

    // 3. For other schemes or if all checks fail
    return false
}

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

// =============================================================================
// NUMERIC EXTENSIONS
// =============================================================================

@SuppressLint("SimpleDateFormat")
fun Long.toFormattedDate(
    hideSeconds: Boolean = false,
    customFormat: String? = null
): String {
    val format = when {
        customFormat != null -> customFormat
        hideSeconds -> globalClass.preferencesManager.dateTimeFormat.replace(":ss", "")
        else -> globalClass.preferencesManager.dateTimeFormat
    }
    return SimpleDateFormat(format).format(this)
}

fun Long.toFormattedSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(
        this / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}

fun Long.toFormattedTime(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

// =============================================================================
// GRAPHICS & BITMAP EXTENSIONS
// =============================================================================

fun Size.isZero() = width == 0 || height == 0

fun Bitmap.scale(scaleFactor: Float, filter: Boolean = false): Bitmap {
    val newWidth = (width * scaleFactor).toInt()
    val newHeight = (height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, filter)
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

// =============================================================================
// ERROR HANDLING EXTENSIONS
// =============================================================================

fun Throwable.printFullStackTrace(): String {
    val result: Writer = StringWriter()
    val printWriter = PrintWriter(result)
    printStackTrace(printWriter)
    val stacktraceAsString = result.toString()
    printWriter.close()
    return stacktraceAsString
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

fun showMsg(msg: String) {
    globalClass.showMsg(msg)
}

fun showMsg(msgId: Int) {
    globalClass.showMsg(msgId)
}