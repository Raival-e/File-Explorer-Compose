package com.raival.compose.file.explorer.screen.preferences.constant

import com.raival.compose.file.explorer.App.Companion.globalClass

object FilesTabFileListSizeMap {
    object IconSize {
        const val EXTRA_SMALL = 32
        const val SMALL = 42
        const val MEDIUM = 46
        const val LARGE = 48
        const val EXTRA_LARGE = 52
    }

    object FontSize {
        const val EXTRA_SMALL = 12
        const val SMALL = 15
        const val MEDIUM = 18
        const val LARGE = 20
        const val EXTRA_LARGE = 22
    }

    fun getFileListSpace() = when (globalClass.preferencesManager.fileListPrefs.itemSize) {
        FilesTabFileListSize.LARGE.ordinal, FilesTabFileListSize.EXTRA_LARGE.ordinal -> 8
        else -> 4
    }

    fun getFileListIconSize() = when (globalClass.preferencesManager.fileListPrefs.itemSize) {
        FilesTabFileListSize.EXTRA_SMALL.ordinal -> IconSize.EXTRA_SMALL
        FilesTabFileListSize.SMALL.ordinal -> IconSize.SMALL
        FilesTabFileListSize.MEDIUM.ordinal -> IconSize.MEDIUM
        FilesTabFileListSize.LARGE.ordinal -> IconSize.LARGE
        else -> IconSize.EXTRA_LARGE
    }

    fun getFileListFontSize() = when (globalClass.preferencesManager.fileListPrefs.itemSize) {
        FilesTabFileListSize.EXTRA_SMALL.ordinal -> FontSize.EXTRA_SMALL
        FilesTabFileListSize.SMALL.ordinal -> FontSize.SMALL
        FilesTabFileListSize.MEDIUM.ordinal -> FontSize.MEDIUM
        FilesTabFileListSize.LARGE.ordinal -> FontSize.LARGE
        else -> FontSize.EXTRA_LARGE
    }
}