package com.raival.compose.file.explorer.screen.textEditor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.common.emptyString

class Searcher {
    var query by mutableStateOf(emptyString)
    var replace by mutableStateOf(emptyString)
    var caseSensitive by mutableStateOf(false)
    var useRegex by mutableStateOf(false)
}