package com.raival.compose.file.explorer.screen.main.tab.files

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.raival.compose.file.explorer.screen.preferences.misc.prefMutableState

class FilesTabManager {
    var bookmarks by prefMutableState(
        keyName = "bookmarks",
        defaultValue = emptySet(),
        getPreferencesKey = { stringSetPreferencesKey(it) }
    )
}