package com.raival.compose.file.explorer.screen.main

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.conditions
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.modal.StorageDevice
import com.raival.compose.file.explorer.screen.main.tab.files.modal.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivityManager {
    var title by mutableStateOf(globalClass.getString(R.string.main_activity_title))
    var subtitle by mutableStateOf(emptyString)

    val storageDevices = arrayListOf<StorageDevice>()

    var showNewTabDialog by mutableStateOf(false)
    var showJumpToPathDialog by mutableStateOf(false)
    var showSaveTextEditorFilesBeforeCloseDialog by mutableStateOf(false)
    var isSavingTextEditorFiles by mutableStateOf(false)

    var selectedTabIndex by mutableIntStateOf(0)
    val tabs = mutableStateListOf<Tab>()

    val drawerState = DrawerState(initialValue = DrawerValue.Closed)

    fun setupTabs() {
        storageDevices.addAll(StorageProvider.getStorageDevices(globalClass))
    }

    fun closeAllTabs() {
        tabs.removeIf { it.id isNot tabs[0].id }
        selectedTabIndex = 0
    }

    fun removeOtherTabs(currentTabIndex: Int) {
        if (currentTabIndex isNot selectedTabIndex) {
            selectedTabIndex = currentTabIndex
        }

        tabs.removeIf { it.id.conditions { isNot(tabs[0].id) && isNot(tabs[currentTabIndex].id) } }

        selectedTabIndex = if (currentTabIndex > 0) 1 else 0
    }

    fun removeTabAt(index: Int) {
        tabs.removeAt(index)
        if (selectedTabIndex == index && tabs.size <= selectedTabIndex) selectedTabIndex--
    }

    fun addTabAndSelect(tab: Tab, index: Int = selectedTabIndex + 1) {
        if (tabs.isNotEmpty()) tabs[selectedTabIndex].onTabStopped()

        selectedTabIndex = if (tabs.isEmpty()) {
            tabs.add(tab)
            0
        } else if (index < 0) {
            tabs.add(tab)
            tabs.size - 1
        } else {
            tabs.add(index, tab)
            index
        }
    }

    fun jumpToFile(file: DocumentHolder, context: Context) {
        openFile(file, context)
    }

    private fun openFile(file: DocumentHolder, context: Context) {
        if (file.exists()) {
            addTabAndSelect(FilesTab(file, context))
        }
    }

    fun canExit(coroutineScope: CoroutineScope): Boolean {
        if (drawerState.isOpen) {
            coroutineScope.launch {
                drawerState.close()
            }
            return false
        }

        if (tabs.size > 1 && selectedTabIndex isNot 0) {
            tabs.removeAt(selectedTabIndex)
            selectedTabIndex--
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