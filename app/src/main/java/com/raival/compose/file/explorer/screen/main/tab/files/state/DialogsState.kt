package com.raival.compose.file.explorer.screen.main.tab.files.state

data class DialogsState(
    val showRenameDialog: Boolean = false,
    val showNewZipFileDialog: Boolean = false,
    val showOpenWithDialog: Boolean = false,
    val showFileOptionsDialog: Boolean = false,
    val showApkDialog: Boolean = false,
    val showSortingMenu: Boolean = false,
    val showViewConfigDialog: Boolean = false,
    val showCreateNewFileDialog: Boolean = false,
    val showConfirmDeleteDialog: Boolean = false,
    val showFileProperties: Boolean = false,
    val showTasksPanel: Boolean = false,
    val showSearchPenal: Boolean = false,
    val showBookmarkDialog: Boolean = false,
    val showImportPrefsDialog: Boolean = false,
)