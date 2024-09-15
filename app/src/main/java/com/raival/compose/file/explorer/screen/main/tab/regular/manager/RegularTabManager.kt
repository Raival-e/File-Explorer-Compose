package com.raival.compose.file.explorer.screen.main.tab.regular.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.raival.compose.file.explorer.screen.main.tab.regular.task.RegularTabTask
import com.raival.compose.file.explorer.screen.preferences.modal.prefMutableState

class RegularTabManager {
    val regularTabTasks = mutableStateListOf<RegularTabTask>()

    var bookmarks by prefMutableState(
        keyName = "bookmarks",
        defaultValue = emptySet(),
        getPreferencesKey = { stringSetPreferencesKey(it) }
    )
}