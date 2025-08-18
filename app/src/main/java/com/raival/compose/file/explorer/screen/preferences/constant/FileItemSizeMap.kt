package com.raival.compose.file.explorer.screen.preferences.constant

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder

object FileItemSizeMap {
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

    fun getFileListSpace(contentHolder: ContentHolder? = null) = when (
        if (contentHolder == null) globalClass.preferencesManager.itemSize
        else globalClass.preferencesManager.getViewConfigPrefsFor(contentHolder).itemSize
    ) {
        FileItemSize.LARGE.ordinal, FileItemSize.EXTRA_LARGE.ordinal -> 8
        else -> 4
    }

    fun getFileListIconSize(contentHolder: ContentHolder? = null) = when (
        if (contentHolder == null) globalClass.preferencesManager.itemSize
        else globalClass.preferencesManager.getViewConfigPrefsFor(contentHolder).itemSize
    ) {
        FileItemSize.EXTRA_SMALL.ordinal -> IconSize.EXTRA_SMALL
        FileItemSize.SMALL.ordinal -> IconSize.SMALL
        FileItemSize.MEDIUM.ordinal -> IconSize.MEDIUM
        FileItemSize.LARGE.ordinal -> IconSize.LARGE
        else -> IconSize.EXTRA_LARGE
    }

    fun getFileListFontSize(contentHolder: ContentHolder? = null) = when (
        if (contentHolder == null) globalClass.preferencesManager.itemSize
        else globalClass.preferencesManager.getViewConfigPrefsFor(contentHolder).itemSize
    ) {
        FileItemSize.EXTRA_SMALL.ordinal -> FontSize.EXTRA_SMALL
        FileItemSize.SMALL.ordinal -> FontSize.SMALL
        FileItemSize.MEDIUM.ordinal -> FontSize.MEDIUM
        FileItemSize.LARGE.ordinal -> FontSize.LARGE
        else -> FontSize.EXTRA_LARGE
    }
}