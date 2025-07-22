package com.raival.compose.file.explorer.screen.main.tab.files.holder

import android.content.Context
import androidx.annotation.DrawableRes
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ContentCount
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.aiFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.archiveFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.cssFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.docFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.excelFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.fontFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.isoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pptFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.psdFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.sqlFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vcfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vectorFileType
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
    abstract val details: String
    abstract val icon: Any

    @get:DrawableRes
    abstract val iconPlaceholder: Int

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
    abstract fun getParent(): ContentHolder?

    open fun createSubFile(name: String, onCreated: (ContentHolder?) -> Unit) {
        onCreated(null)
    }

    open fun createSubFolder(name: String, onCreated: (ContentHolder?) -> Unit) {
        onCreated(null)
    }

    abstract fun getContentCount(): ContentCount
    abstract fun findFile(name: String): ContentHolder?

    /**
     * Checks if the content holder is a valid content.
     * For example, it might check if the underlying file or folder still exists on the file system.
     *
     * @return `true` if the content holder is valid, `false` otherwise.
     */
    abstract fun isValid(): Boolean

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

    open fun hasParent(onlyReadable: Boolean = true): Boolean {
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

    fun isApk(): Boolean = extension.endsWith(apkFileType)

    fun isHidden(): Boolean = displayName.startsWith(".")

    fun getFormattedFileCount(filesCount: Int, foldersCount: Int): String {
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

    @DrawableRes
    fun getContentIconPlaceholderResource(): Int {
        return when {
            isFolder -> R.drawable.baseline_folder_24
            extension == aiFileType -> R.drawable.ai_file_extension
            extension == cssFileType -> R.drawable.css_file_extension
            extension == isoFileType -> R.drawable.iso_file_extension
            extension == jsFileType -> R.drawable.js_file_extension
            extension == psdFileType -> R.drawable.psd_file_extension
            extension == sqlFileType -> R.drawable.sql_file_extension
            extension == vcfFileType -> R.drawable.vcf_file_extension
            extension == javaFileType -> R.drawable.css_file_extension
            extension == kotlinFileType -> R.drawable.css_file_extension
            imageFileType.contains(extension) -> R.drawable.jpg_file_extension
            docFileType.contains(extension) -> R.drawable.doc_file_extension
            excelFileType.contains(extension) -> R.drawable.xls_file_extension
            pptFileType.contains(extension) -> R.drawable.ppt_file_extension
            fontFileType.contains(extension) -> R.drawable.font_file_extension
            vectorFileType.contains(extension) -> R.drawable.vector_file_extension
            audioFileType.contains(extension) -> R.drawable.music_file_extension
            codeFileType.contains(extension) -> R.drawable.css_file_extension
            editableFileType.contains(extension) -> R.drawable.txt_file_extension
            archiveFileType.contains(extension) -> R.drawable.zip_file_extension
            apkFileType == extension -> R.drawable.apk_file_extension
            else -> R.drawable.unknown_file_extension
        }
    }
}