package com.raival.compose.file.explorer.screen.main

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDevice
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class MainActivityManager {
    var title by mutableStateOf(globalClass.getString(R.string.main_activity_title))
    var subtitle by mutableStateOf(emptyString)

    val storageDevices = arrayListOf<StorageDevice>()

    var showFtpServerDialog by mutableStateOf(false)
    var showNewTabDialog by mutableStateOf(false)
    var showAppInfoDialog by mutableStateOf(false)
    var showJumpToPathDialog by mutableStateOf(false)
    var showSaveTextEditorFilesBeforeCloseDialog by mutableStateOf(false)
    var isSavingTextEditorFiles by mutableStateOf(false)


    var selectedTabIndex by mutableIntStateOf(0)
    val tabs = mutableStateListOf<Tab>()

    val tabLayoutState = LazyListState()

    fun setupTabs() {
        storageDevices.addAll(StorageProvider.getStorageDevices(globalClass))
    }

    fun closeAllTabs() {
        tabs.removeIf {
            (it.id isNot tabs[0].id).also { toClose ->
                if (toClose) it.onTabRemoved()
            }
        }
        selectTabAt(0)
    }

    fun removeOtherTabs(tabIndex: Int) {
        if (tabIndex isNot selectedTabIndex) {
            selectTabAt(tabIndex)
            removeOtherTabs(tabIndex)
            return
        }

        val tabToKeep = tabs[tabIndex].id
        tabs.removeIf { it.id.isNot(tabToKeep).also { toClose -> if (toClose) it.onTabRemoved() } }

        selectTabAt(0)
    }

    fun removeTabAt(index: Int) {
        if (tabs.size <= 1) return

        tabs.removeAt(index).apply {
            if (selectedTabIndex == index) onTabStopped()
            onTabRemoved()
        }

        if (selectedTabIndex == index) selectTabAt(max(0, index - 1))
    }

    fun addTabAndSelect(tab: Tab, index: Int = selectedTabIndex + 1) {
        selectTabAt(
            if (tabs.isEmpty()) {
                tabs.add(tab)
                0
            } else if (index < 0) {
                tabs.add(tab)
                tabs.size - 1
            } else {
                tabs.add(index, tab)
                index
            }
        )
    }

    fun selectTabAt(index: Int) {
        if (tabs.isNotEmpty() && selectedTabIndex isNot index && selectedTabIndex < tabs.size) tabs[selectedTabIndex].onTabStopped()
        selectedTabIndex = index

        tabs[selectedTabIndex].apply {
            if (!isCreated) onTabStarted() else onTabResumed()
        }
    }

    fun replaceCurrentTabWith(tab: Tab) {
        if (tabs.isNotEmpty()) tabs[selectedTabIndex].onTabStopped()
        tabs[selectedTabIndex] = tab
        selectTabAt(selectedTabIndex)
    }

    fun jumpToFile(file: LocalFileHolder, context: Context) {
        openFile(file, context)
    }

    private fun openFile(file: LocalFileHolder, context: Context) {
        if (file.exists()) {
            addTabAndSelect(FilesTab(file, context))
        }
    }

    fun resumeActiveTab() {
        tabs[selectedTabIndex].onTabResumed()
    }

    fun getActiveTab(): Tab {
        return tabs[selectedTabIndex]
    }

    fun canExit(coroutineScope: CoroutineScope): Boolean {
        if (tabs[selectedTabIndex].onBackPressed()) {
            return false
        }

        if (tabs[selectedTabIndex] !is HomeTab && !globalClass.preferencesManager.behaviorPrefs.skipHomeWhenTabClosed) {
            replaceCurrentTabWith(HomeTab())
            return false
        }

        if (tabs.size > 1 && selectedTabIndex isNot 0) {
            removeTabAt(selectedTabIndex)
            return false
        }

        if (tabs.size == 1 && !allTextEditorFileInstancesSaved()) {
            showSaveTextEditorFilesBeforeCloseDialog = true
            return false
        }

        return true
    }

    private fun allTextEditorFileInstancesSaved(): Boolean {
        globalClass.textEditorManager.fileInstanceList.forEach {
            if (it.requireSave) return false
        }
        return true
    }

    fun saveTextEditorFiles(onRequestFinish: () -> Unit) {
        isSavingTextEditorFiles = true

        CoroutineScope(Dispatchers.IO).launch {
            globalClass.textEditorManager.fileInstanceList.forEach {
                if (it.requireSave) {
                    it.file.writeText(it.content.toString())
                }
            }

            isSavingTextEditorFiles = false

            onRequestFinish()
        }
    }
}