package com.raival.compose.file.explorer.screen.main.tab.files.holder

import android.content.Context
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ContentCount
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_DATE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_NAME
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_SIZE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortFoldersFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortLargerFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortName
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortNameRev
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortNewerFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortOlderFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortSmallerFirst

/**
 * Abstract class representing a content holder.
 * This class provides common properties and methods for managing content.
 */
abstract class ContentHolder {
    val uid = globalClass.generateUid()
    abstract val uniquePath: String

    abstract val displayName: String

    abstract val isFolder: Boolean
    abstract val lastModified: Long
    abstract val size: Long
    abstract val extension: String
    abstract val canRead: Boolean
    abstract val canWrite: Boolean

    abstract val canAddNewContent: Boolean

    /**
     * Returns unsorted content list, use listSortedContent() to get sorted list.
     */
    abstract suspend fun listContent(): ArrayList<out ContentHolder>

    abstract suspend fun getParent(): ContentHolder?

    open suspend fun createSubFile(name: String, onCreated: (ContentHolder?) -> Unit) {
        onCreated(null)
    }

    open suspend fun createSubFolder(name: String, onCreated: (ContentHolder?) -> Unit) {
        onCreated(null)
    }

    abstract suspend fun getContentCount(): ContentCount
    abstract suspend fun findFile(name: String): ContentHolder?

    open suspend fun getDetails(): String {
        return emptyString
    }

    /**
     * Checks if the content holder is a valid content.
     * For example, it might check if the underlying file or folder still exists on the file system.
     *
     * @return `true` if the content holder is valid, `false` otherwise.
     */
    abstract suspend fun isValid(): Boolean

    open fun open(
        context: Context,
        anonymous: Boolean,
        skipSupportedExtensions: Boolean,
        customMimeType: String?
    ) {
    }

    open fun isFile(): Boolean {
        return !isFolder
    }

    open suspend fun hasParent(onlyReadable: Boolean = true): Boolean {
        val parent = getParent()
        return parent != null && parent.isValid() && (!onlyReadable || parent.canRead)
    }

    open suspend fun listSortedContent(): ArrayList<out ContentHolder> {
        val sortingPrefs = globalClass.preferencesManager.getSortingPrefsFor(this)

        return listContent().apply {
            when (sortingPrefs.sortMethod) {
                SORT_BY_NAME -> {
                    sortWith(if (sortingPrefs.reverseSorting) sortNameRev else sortName)
                }

                SORT_BY_DATE -> {
                    sortWith(if (sortingPrefs.reverseSorting) sortOlderFirst else sortNewerFirst)
                }

                SORT_BY_SIZE -> {
                    sortWith(if (sortingPrefs.reverseSorting) sortLargerFirst else sortSmallerFirst)
                }
            }

            if (sortingPrefs.showFoldersFirst) sortWith(sortFoldersFirst)

            if (!globalClass.preferencesManager.showHiddenFiles) {
                removeIf { it.isHidden() }
            }
        }
    }

    fun isApk(): Boolean = extension == apkFileType

    fun isHidden(): Boolean = displayName.startsWith(".")

    suspend fun getFormattedFileCount(filesCount: Int, foldersCount: Int): String {
        return buildString {
            if (foldersCount == 0 && filesCount == 0) {
                append(globalClass.getString(R.string.empty_folder))
            } else {
                if (foldersCount > 0) {
                    append(
                        globalClass.getString(
                            R.string.folders_count,
                            foldersCount
                        )
                    )
                    if (filesCount > 0) append(", ")
                }
                if (filesCount > 0) {
                    append(
                        globalClass.getString(
                            R.string.files_count,
                            filesCount
                        )
                    )
                }
            }
        }
    }

    fun getLastModifiedDate(): String {
        return lastModified.toFormattedDate(hideSeconds = true)
    }
}