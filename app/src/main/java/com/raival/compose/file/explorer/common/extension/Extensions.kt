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
import android.provider.MediaStore.MediaColumns.DISPLAY_NAME
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import java.io.File
import java.io.FileInputStream
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
            "file" -> { File(path ?: "").name }
            else -> null
        }
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
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        bitmap
    }
}