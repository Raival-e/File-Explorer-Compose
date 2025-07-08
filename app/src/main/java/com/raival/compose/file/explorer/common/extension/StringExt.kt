package com.raival.compose.file.explorer.common.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.anggrayudi.storage.extension.trimFileSeparator
import com.raival.compose.file.explorer.App
import java.io.File
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