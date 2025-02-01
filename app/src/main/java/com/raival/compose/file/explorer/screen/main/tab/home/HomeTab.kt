package com.raival.compose.file.explorer.screen.main.tab.home

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.runtime.mutableStateListOf
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.home.holder.HomeCategoryHolder
import com.raival.compose.file.explorer.screen.main.tab.home.holder.RecentFileHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeTab : Tab() {
    override val id = globalClass.generateUid()

    override val title = globalClass.getString(R.string.home_tab_title)

    override val subtitle = emptyString

    override val header = globalClass.getString(R.string.home_tab_header)

    val recentFileHolders = mutableStateListOf<RecentFileHolder>()

    override fun onTabStarted() {
        super.onTabStarted()
        requestHomeToolbarUpdate()
    }

    override fun onTabResumed() {
        super.onTabResumed()
        requestHomeToolbarUpdate()
    }

    fun fetchRecentFiles() {
        recentFileHolders.clear()
        CoroutineScope(Dispatchers.IO).launch {
            recentFileHolders.addAll(getRecentFiles())
        }
    }

    fun getMainCategories() = arrayListOf<HomeCategoryHolder>().apply {
        val mainActivityManager = globalClass.mainActivityManager
        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.images),
                icon = Icons.Rounded.Image,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(StorageProvider.images)
                    )
                }
            )
        )

        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.videos),
                icon = Icons.Rounded.VideoFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(StorageProvider.videos)
                    )
                }
            )
        )

        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.audios),
                icon = Icons.Rounded.AudioFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(StorageProvider.audios)
                    )
                }
            )
        )

        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.documents),
                icon = Icons.AutoMirrored.Rounded.InsertDriveFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(StorageProvider.documents)
                    )
                }
            )
        )

        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.archives),
                icon = Icons.Rounded.Archive,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(StorageProvider.archives)
                    )
                }
            )
        )

        add(
            HomeCategoryHolder(
                name = globalClass.getString(R.string.apps),
                icon = Icons.Rounded.Android,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        AppsTab()
                    )
                }
            )
        )
    }

    private fun getRecentFiles(): ArrayList<RecentFileHolder> {
        val recentFileHolders = ArrayList<RecentFileHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        val time = (System.currentTimeMillis() / 1000) - (24 * 60 * 60)

        val selection = "${MediaStore.Files.FileColumns.DATE_MODIFIED} >= ?"
        val selectionArgs = arrayOf(time.toString())

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndexPath = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val columnLastModified =
                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val columnName = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (it.moveToNext() && recentFileHolders.size < 15) {
                val filePath = it.getString(columnIndexPath)
                val lastModified = it.getLong(columnLastModified)
                val name = it.getString(columnName)
                val file = File(filePath)
                if (file.isFile) {
                    recentFileHolders.add(
                        RecentFileHolder(
                            name,
                            filePath,
                            lastModified
                        )
                    )
                }
            }
        }

        return recentFileHolders
    }
}