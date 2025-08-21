package com.raival.compose.file.explorer.screen.main.tab.files.holder

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ContentCount
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileListCategory
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getArchiveFiles
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getAudioFiles
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getBookmarks
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getDocumentFiles
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getImageFiles
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getRecentFiles
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getSearchResult
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider.getVideoFiles
import kotlinx.coroutines.runBlocking
import java.io.File

class VirtualFileHolder(val type: Int) : ContentHolder() {
    private var fileCount = 0
    private val contentList = arrayListOf<ContentHolder>()
    private val categories = arrayListOf<String>()

    var selectedCategory: FileListCategory? = null

    override val displayName = when (type) {
        BOOKMARKS -> globalClass.getString(R.string.bookmarks)
        AUDIO -> globalClass.getString(R.string.audios)
        VIDEO -> globalClass.getString(R.string.videos)
        IMAGE -> globalClass.getString(R.string.images)
        ARCHIVE -> globalClass.getString(R.string.archives)
        DOCUMENT -> globalClass.getString(R.string.documents)
        RECENT -> globalClass.getString(R.string.recent_files)
        SEARCH -> globalClass.getString(R.string.search)
        else -> globalClass.getString(R.string.unknown)
    }

    override val isFolder = true

    override val lastModified = 0L

    override val size = 0L

    override val uniquePath = displayName

    override val extension = emptyString

    override suspend fun isValid() = true

    override suspend fun getParent() = null

    override suspend fun listSortedContent(): ArrayList<out ContentHolder> {
        if (type == SEARCH || type == BOOKMARKS) {
            return super.listSortedContent()
        }

        val sortingPrefs = globalClass.preferencesManager.getSortingPrefsFor(this)

        if (type == RECENT && sortingPrefs == globalClass.preferencesManager.getDefaultSortingPrefs()) {
            return listContent().apply {
                if (!globalClass.preferencesManager.showHiddenFiles) {
                    removeIf { it.isHidden() }
                }
            }
        } else return listContent().apply {
            if (!globalClass.preferencesManager.showHiddenFiles) {
                removeIf { it.isHidden() }
            }
        }
    }

    override suspend fun listContent(): ArrayList<out ContentHolder> {
        val sortingPrefs = globalClass.preferencesManager.getSortingPrefsFor(this)
        when (type) {
            BOOKMARKS -> getBookmarks()
            AUDIO -> getAudioFiles(sortingPrefs)
            VIDEO -> getVideoFiles(sortingPrefs)
            IMAGE -> getImageFiles(sortingPrefs)
            ARCHIVE -> getArchiveFiles(sortingPrefs)
            DOCUMENT -> getDocumentFiles(sortingPrefs)
            RECENT -> getRecentFiles()
            SEARCH -> getSearchResult()
            else -> arrayListOf()
        }.also {
            contentList.apply {
                clear()
                addAll(it)
            }
            if (type != BOOKMARKS) fetchCategories()
        }

        return contentList.filter {
            if (selectedCategory == null) return@filter true
            it.uniquePath == (selectedCategory!!.data as File).path + File.separator + it.displayName
        }.toCollection(arrayListOf()).also { fileCount = it.size }
    }

    private fun fetchCategories() {
        categories.clear()
        contentList.forEach { content ->
            if (content is LocalFileHolder) {
                val parentPath = content.file.parent
                if (parentPath != null && !categories.contains(parentPath)) {
                    categories.add(parentPath)
                }
            }
        }
    }

    fun getCategories() = categories.map { File(it).let { FileListCategory(it.name, it) } }

    override suspend fun findFile(name: String): ContentHolder? {
        if (contentList.isEmpty()) {
            runBlocking {
                listContent()
            }
        }

        return contentList.find { it.displayName == name }
    }

    override val canAddNewContent = false

    override val canRead = true

    override val canWrite = false

    override suspend fun getContentCount() = ContentCount(files = fileCount)

    companion object {
        const val BOOKMARKS = 0
        const val AUDIO = 1
        const val VIDEO = 2
        const val IMAGE = 3
        const val ARCHIVE = 4
        const val DOCUMENT = 5
        const val RECENT = 6
        const val SEARCH = 7
    }
}