package com.raival.compose.file.explorer.screen.main.tab.regular.modal

import com.raival.compose.file.explorer.screen.main.tab.regular.misc.SortingMethod.SORT_BY_NAME

data class FileSortingPrefs(
    val sortMethod: Int = SORT_BY_NAME,
    val showFoldersFirst: Boolean = true,
    val reverseSorting: Boolean = false,
    val applyForThisFileOnly: Boolean = false
)