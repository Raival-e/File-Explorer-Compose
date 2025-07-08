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

val sortName = Comparator.comparing { file: ContentHolder ->
    file.displayName.lowercase(Locale.getDefault())
}

val sortNameRev = Comparator { file1: ContentHolder, file2: ContentHolder ->
    file2.displayName.lowercase(Locale.getDefault()).compareTo(
        file1.displayName.lowercase(
            Locale.getDefault()
        )
    )
}

val sortSmallerFirst = Comparator.comparingLong { obj: ContentHolder -> obj.size }

val sortLargerFirst = Comparator { file1: ContentHolder, file2: ContentHolder ->
    file2.size.compareTo(file1.size)
}
