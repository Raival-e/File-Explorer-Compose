package com.raival.compose.file.explorer.screen.main.tab.main

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.runtime.mutableStateListOf
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.modal.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.main.modal.MainCategory
import com.raival.compose.file.explorer.screen.main.tab.main.modal.RecentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainTab : Tab() {
    override val id = globalClass.generateUid()

    override val title = globalClass.getString(R.string.home_tab_title)

    override val subtitle = emptyString

    override val header = globalClass.getString(R.string.home_tab_header)

    val recentFiles = mutableStateListOf<RecentFile>()

    override fun onTabResumed() {
        super.onTabResumed()
        requestHomeToolbarUpdate()
    }

    fun fetchRecentFiles() {
        recentFiles.clear()
        CoroutineScope(Dispatchers.IO).launch {
            recentFiles.addAll(getRecentFiles())
        }
    }

    fun getMainCategories() = arrayListOf<MainCategory>().apply {
        val mainActivityManager = globalClass.mainActivityManager
        add(
            MainCategory(
                name = globalClass.getString(R.string.images),
                icon = Icons.Rounded.Image,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.images)
                    )
                }
            )
        )

        add(
            MainCategory(
                name = globalClass.getString(R.string.videos),
                icon = Icons.Rounded.VideoFile,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.videos)
                    )
                }
            )
        )

        add(
            MainCategory(
                name = globalClass.getString(R.string.audios),
                icon = Icons.Rounded.AudioFile,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.audios)
                    )
                }
            )
        )

        add(
            MainCategory(
                name = globalClass.getString(R.string.documents),
                icon = Icons.AutoMirrored.Rounded.InsertDriveFile,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.documents)
                    )
                }
            )
        )

        add(
            MainCategory(
                name = globalClass.getString(R.string.archives),
                icon = Icons.Rounded.Archive,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.archives)
                    )
                }
            )
        )

        add(
            MainCategory(
                name = globalClass.getString(R.string.recent_files),
                icon = Icons.Rounded.AccessTimeFilled,
                onClick = {
                    mainActivityManager.addTabAndSelect(
                        FilesTab(StorageProvider.recentFiles)
                    )
                }
            )
        )
    }

    private fun getRecentFiles(): ArrayList<RecentFile> {
        val recentFiles = ArrayList<RecentFile>()
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

        val uriWithLimit = uri.buildUpon()
            .appendQueryParameter("limit", "15")
            .build()

        val cursor: Cursor? = contentResolver.query(
            uriWithLimit,
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

            while (it.moveToNext()) {
                val filePath = it.getString(columnIndexPath)
                val lastModified = it.getLong(columnLastModified)
                val name = it.getString(columnName)
                val file = File(filePath)
                if (file.isFile) {
                    recentFiles.add(
                        RecentFile(
                            name,
                            filePath,
                            lastModified
                        )
                    )
                }
            }
        }

        return recentFiles
    }
}