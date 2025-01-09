package com.raival.compose.file.explorer.common.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.raival.compose.file.explorer.App
import kotlin.random.Random

const val emptyString = ""
const val whiteSpace = " "

fun String.Companion.randomString(length: Int): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString(emptyString)
}

fun String.orIf(value: String, condition: (String) -> Boolean) =
    if (condition(this)) value else this

fun String.isValidAsFileName() = !conditions {
    contains(":") || contains("/") || contains("*") || contains("?")
            || contains("\"") || contains("<") || contains(">")
            || contains("|")
} && isNotBlank()

fun String.equals(vararg condition: String): Boolean {
    condition.forEach { if (this == it) return true }
    return false
}

fun String.endsWithOneOf(vararg condition: String): Boolean {
    condition.forEach { if (this.endsWith(it)) return true }
    return false
}

fun String.copyToClipboard() {
    (App.globalClass.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
        ClipData.newPlainText("clipboard", this)
    )
}

fun String.trimToLastTwoSegments(): String {
    val segments = this.split("/")
    return if (segments.size >= 2) {
        segments.takeLast(2).joinToString("/")
    } else {
        this
    }
}

fun String.limitLength(maxLength: Int): String {
    if (this.length <= maxLength) { return this }
    return this.substring(0, maxLength - 3) + "..."
}