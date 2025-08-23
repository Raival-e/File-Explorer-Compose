package com.raival.compose.file.explorer.screen.main.tab.home

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.VideoFile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.ARCHIVE
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.AUDIO
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.DOCUMENT
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.IMAGE
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder.Companion.VIDEO
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.home.holder.HomeCategory
import com.raival.compose.file.explorer.screen.main.tab.home.holder.RecentFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeTab : Tab() {
    override val id = globalClass.generateUid()
    val scope = CoroutineScope(Dispatchers.IO)
    override val header = globalClass.getString(R.string.home_tab_header)
    val recentFiles = mutableStateListOf<RecentFile>()
    val pinnedFiles = arrayListOf<LocalFileHolder>()

    var showCustomizeHomeTabDialog by mutableStateOf(false)

    override fun onTabStarted() {
        super.onTabStarted()
        requestHomeToolbarUpdate()
    }

    override fun onTabResumed() {
        super.onTabResumed()
        requestHomeToolbarUpdate()
    }

    override suspend fun getSubtitle() = emptyString

    override suspend fun getTitle() = globalClass.getString(R.string.home_tab_title)

    fun getPinnedFiles() {
        pinnedFiles.clear()
        pinnedFiles.addAll(
            globalClass.preferencesManager.pinnedFiles.map { LocalFileHolder(File(it)) }
        )
    }

    fun fetchRecentFiles() {
        if (recentFiles.isNotEmpty()) return

        scope.launch {
            recentFiles.addAll(getRecentFiles())
        }
    }

    fun getMainCategories() = arrayListOf<HomeCategory>().apply {
        val mainActivityManager = globalClass.mainActivityManager
        add(
            HomeCategory(
                name = globalClass.getString(R.string.images),
                icon = Icons.Rounded.Image,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(IMAGE))
                    )
                }
            )
        )

        add(
            HomeCategory(
                name = globalClass.getString(R.string.videos),
                icon = Icons.Rounded.VideoFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(VIDEO))
                    )
                }
            )
        )

        add(
            HomeCategory(
                name = globalClass.getString(R.string.audios),
                icon = Icons.Rounded.AudioFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(AUDIO))
                    )
                }
            )
        )

        add(
            HomeCategory(
                name = globalClass.getString(R.string.playlists),
                icon = Icons.Rounded.QueueMusic,
                onClick = {
                    try {
                        val context = globalClass.applicationContext
                        val intent = Intent(context, com.raival.compose.file.explorer.screen.playlist.PlaylistActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Log the error or handle it gracefully
                        e.printStackTrace()
                    }
                }
            )
        )

        add(
            HomeCategory(
                name = globalClass.getString(R.string.documents),
                icon = Icons.AutoMirrored.Rounded.InsertDriveFile,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(DOCUMENT))
                    )
                }
            )
        )

        add(
            HomeCategory(
                name = globalClass.getString(R.string.archives),
                icon = Icons.Rounded.Archive,
                onClick = {
                    mainActivityManager.replaceCurrentTabWith(
                        FilesTab(VirtualFileHolder(ARCHIVE))
                    )
                }
            )
        )

        add(
            HomeCategory(
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

    private fun getRecentFiles(): ArrayList<RecentFile> {
        return arrayListOf<RecentFile>().apply {
            addAll(
                StorageProvider.getRawRecentFiles(
                    recentHours = 24,
                    limit = 25
                )
            )
        }
    }

    fun removePinnedFile(file: LocalFileHolder) {
        pinnedFiles.remove(file)
        globalClass.preferencesManager.pinnedFiles = pinnedFiles.map { it.uniquePath }.toSet()
    }
}