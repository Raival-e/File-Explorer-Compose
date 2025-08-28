package com.raival.compose.file.explorer.screen.main.tab.files.misc

import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import java.util.Locale

val sortFoldersFirst = Comparator { file1: ContentHolder, file2: ContentHolder ->
    if (file1.isFolder && !file2.isFolder) {
        return@Comparator -1
    } else if (!file1.isFolder && file2.isFolder) {
        return@Comparator 1
    } else {
        return@Comparator 0
    }
}

val sortFilesFirst = Comparator { file2: ContentHolder, file1: ContentHolder ->
    if (file1.isFolder && !file2.isFolder) {
        return@Comparator -1
    } else if (!file1.isFolder && file2.isFolder) {
        return@Comparator 1
    } else {
        return@Comparator 0
    }
}

val sortOlderFirst = Comparator.comparingLong { obj: ContentHolder -> obj.lastModified }

val sortNewerFirst = Comparator { file1: ContentHolder, file2: ContentHolder ->
    file2.lastModified.compareTo(file1.lastModified)
}

val sortName = Comparator { file1: ContentHolder, file2: ContentHolder ->
    naturalCompare(file1.displayName, file2.displayName)
}

val sortNameRev = Comparator { file1: ContentHolder, file2: ContentHolder ->
    naturalCompare(file2.displayName, file1.displayName)
}

private fun naturalCompare(str1: String, str2: String): Int {
    val s1 = str1.lowercase(Locale.getDefault())
    val s2 = str2.lowercase(Locale.getDefault())

    var i1 = 0
    var i2 = 0

    while (i1 < s1.length && i2 < s2.length) {
        val c1 = s1[i1]
        val c2 = s2[i2]

        if (c1.isDigit() && c2.isDigit()) {
            // Parse numbers directly without regex
            var num1 = 0L
            var num2 = 0L

            while (i1 < s1.length && s1[i1].isDigit()) {
                num1 = num1 * 10 + (s1[i1] - '0')
                i1++
            }

            while (i2 < s2.length && s2[i2].isDigit()) {
                num2 = num2 * 10 + (s2[i2] - '0')
                i2++
            }

            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        } else {
            // Direct character comparison
            if (c1 != c2) {
                return when {
                    c1.isDigit() && !c2.isDigit() -> -1
                    !c1.isDigit() && c2.isDigit() -> 1
                    else -> c1.compareTo(c2)
                }
            }
            i1++
            i2++
        }
    }

    return s1.length.compareTo(s2.length)
}

val sortSmallerFirst = Comparator.comparingLong { obj: ContentHolder -> obj.size }

val sortLargerFirst = Comparator { file1: ContentHolder, file2: ContentHolder ->
    file2.size.compareTo(file1.size)
}

val sortType = Comparator.comparing { file: ContentHolder ->
    file.extension
}

val sortTypeRev = Comparator { file1: ContentHolder, file2: ContentHolder ->
    file2.extension.compareTo(file1.extension)
}
