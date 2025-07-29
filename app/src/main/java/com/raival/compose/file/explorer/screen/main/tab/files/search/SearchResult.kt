package com.raival.compose.file.explorer.screen.main.tab.files.search

import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder

data class SearchResult(
    val file: ContentHolder,
    val matchType: MatchType,
    val lineNumber: Int? = null,
    val matchedLine: String? = null,
    val matchCount: Int = 1
) {
    enum class MatchType {
        FILENAME, EXTENSION, CONTENT
    }
}