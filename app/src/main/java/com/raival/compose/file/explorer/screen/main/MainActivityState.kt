package com.raival.compose.file.explorer.screen.main

import androidx.compose.foundation.lazy.LazyListState
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDevice

data class MainActivityState(
    val title: String = globalClass.getString(R.string.main_activity_title),
    val subtitle: String = emptyString,
    val showAppInfoDialog: Boolean = false,
    val showJumpToPathDialog: Boolean = false,
    val showSaveEditorFilesDialog: Boolean = false,
    val isSavingFiles: Boolean = false,
    val selectedTabIndex: Int = 0,
    val storageDevices: List<StorageDevice> = emptyList(),
    val tabs: List<Tab> = emptyList(),
    val tabLayoutState: LazyListState = LazyListState()
)