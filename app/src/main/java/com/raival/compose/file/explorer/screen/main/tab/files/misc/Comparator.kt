package com.raival.compose.file.explorer.screen.main.tab.files.misc

import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import java.util.Locale

val sortFoldersFirst = Comparator { file1: DocumentHolder, file2: DocumentHolder ->
    if (file1.isFolder && !file2.isFolder) {
        return@Comparator -1
    } else if (!file1.isFolder && file2.isFolder) {
        return@Comparator 1
    } else {
        return@Comparator 0
    }
}

val sortFilesFirst = Comparator { file2: DocumentHolder, file1: DocumentHolder ->
    if (file1.isFolder && !file2.isFolder) {
        return@Comparator -1
    } else if (!file1.isFolder && file2.isFolder) {
        return@Comparator 1
    } else {
        return@Comparator 0
    }
}

val sortOlderFirst = Comparator.comparingLong { obj: DocumentHolder -> obj.lastModified }

val sortNewerFirst = Comparator { file1: DocumentHolder, file2: DocumentHolder ->
    file2.lastModified.compareTo(file1.lastModified)
}

val sortName = Comparator.comparing { file: DocumentHolder ->
    file.getName().lowercase(Locale.getDefault())
}

val sortNameRev = Comparator { file1: DocumentHolder, file2: DocumentHolder ->
    file2.getName().lowercase(Locale.getDefault()).compareTo(
        file1.getName().lowercase(
            Locale.getDefault()
        )
    )
}

val sortSmallerFirst = Comparator.comparingLong { obj: DocumentHolder -> obj.fileSize }

val sortLargerFirst = Comparator { file1: DocumentHolder, file2: DocumentHolder ->
    file2.fileSize.compareTo(file1.fileSize)
}
