package com.raival.compose.file.explorer.screen.main.tab.files.search

data class SearchOptions(
    val ignoreCase: Boolean = true,
    val useRegex: Boolean = false,
    val searchByExtension: Boolean = false,
    val searchInFileContent: Boolean = false,
    val maxFileSize: Long = 50 * 1024 * 1024,
    val maxResults: Int = 1000
)