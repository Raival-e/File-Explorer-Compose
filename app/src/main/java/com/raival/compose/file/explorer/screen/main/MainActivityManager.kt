package com.raival.compose.file.explorer.screen.main

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.printFullStackTrace
import com.raival.compose.file.explorer.common.showMsg
import com.raival.compose.file.explorer.screen.main.model.GithubRelease
import com.raival.compose.file.explorer.screen.main.startup.StartupTabType
import com.raival.compose.file.explorer.screen.main.startup.StartupTabs
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import kotlin.math.max
import kotlin.math.min

class MainActivityManager {
    val managerScope = CoroutineScope(Dispatchers.IO)
    private val _state = MutableStateFlow(MainActivityState())
    val state = _state.asStateFlow()

    var newUpdate: GithubRelease? = null

    /**\
     * Loads available storage devices (Internal Storage, SD cards, etc)
     */
    fun setup() {
        managerScope.launch {
            _state.update {
                it.copy(
                    storageDevices = StorageProvider.getStorageDevices(globalClass)
                )
            }
        }
    }

    /**
     * Removes all tabs except the one at the given index.
     *
     * If the tab at the given index is not already selected, it will be selected first.
     * Then, all other tabs will be removed.
     *
     * @param tabIndex The index of the tab to keep.
     */
    fun removeOtherTabs(tabIndex: Int) {
        // For now, you can only remove tabs other than the selected tab
        if (tabIndex isNot _state.value.selectedTabIndex) return

        val tabToKeep = _state.value.tabs[tabIndex]
        _state.update {
            it.copy(
                tabs = _state.value.tabs.filter { item ->
                    val toKeep = item == tabToKeep
                    if (!toKeep) item.onTabRemoved()
                    toKeep
                },
                selectedTabIndex = 0
            )
        }
    }

    fun removeTabAt(index: Int) {
        // There must be at least one tab
        if (_state.value.tabs.size <= 1) return

        // Call the proper callbacks on the tab to be removed
        _state.value.tabs[index].apply {
            if (_state.value.selectedTabIndex == index) onTabStopped()
            onTabRemoved()
        }

        // If the tab to be removed is the selected tab, select the previous tab
        val newSelectedTabIndex = if (index <= _state.value.selectedTabIndex) {
            max(0, index - 1)
        } else {
            index
        }.also {
            // Call the proper callbacks on the new selected tab (if it's not the already selected one)
            if (index isNot it) {
                val newSelectedTab = _state.value.tabs[it]
                if (newSelectedTab.isCreated) newSelectedTab.onTabResumed() else newSelectedTab.onTabStarted()
            }
        }

        // Update the state
        _state.update {
            it.copy(
                tabs = _state.value.tabs.filterIndexed { i, _ ->
                    i isNot index
                },
                selectedTabIndex = newSelectedTabIndex
            )
        }
    }

    fun addTabAndSelect(tab: Tab, index: Int = -1) {
        // Stop the active tab
        getActiveTab()?.onTabStopped()

        // Start the new tab
        if (tab.isCreated) tab.onTabResumed() else tab.onTabStarted()

        // Validate the index
        val validatedIndex = if (index isNot -1) {
            max(0, min(index, _state.value.tabs.lastIndex + 1))
        } else {
            _state.value.tabs.lastIndex + 1
        }

        // Update the state
        _state.update {
            it.copy(
                tabs = _state.value.tabs + tab,
                selectedTabIndex = validatedIndex
            )
        }
    }

    fun selectTabAt(index: Int, skipTabRefresh: Boolean = false) {
        // Validate the index
        val validatedIndex = if (index isNot -1) {
            max(0, min(index, _state.value.tabs.lastIndex))
        } else {
            _state.value.tabs.lastIndex
        }

        // If the tab is already selected, resume that tab (kind of refreshing the tab)
        if (validatedIndex == _state.value.selectedTabIndex) {
            if (!skipTabRefresh) getActiveTab()?.onTabResumed()
        } else {
            // Stop the active tab
            getActiveTab()?.onTabStopped()

            // Start the new tab
            val newSelectedTab = _state.value.tabs[validatedIndex]
            if (newSelectedTab.isCreated) newSelectedTab.onTabResumed() else newSelectedTab.onTabStarted()

            // Update the state
            _state.update {
                it.copy(selectedTabIndex = validatedIndex)
            }
        }
    }

    fun replaceCurrentTabWith(tab: Tab) {
        // Stop the active tab
        getActiveTab()?.apply {
            onTabStopped()
            onTabRemoved()
        }

        // Start the new tab
        if (tab.isCreated) tab.onTabResumed() else tab.onTabStarted()

        // update the state
        _state.update {
            it.copy(
                tabs = _state.value.tabs.mapIndexed { index, oldTab ->
                    if (index == _state.value.selectedTabIndex) tab else oldTab
                }
            )
        }
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
        getActiveTab()?.onTabResumed()
    }

    fun onResume() {
        resumeActiveTab()
    }

    fun onStop() {
        getActiveTab()?.onTabStopped()
    }

    fun getActiveTab(): Tab? {
        return if (_state.value.tabs.isNotEmpty()) {
            _state.value.tabs[_state.value.selectedTabIndex]
        } else {
            null
        }
    }

    /**
     * Checks if the app can exit.
     *
     * This function checks the following conditions in order:
     * 1. If the active tab handles the back press, the app cannot exit.
     * 2. If the active tab is not the home tab and the "skip home when tab closed" setting is disabled,
     *    the active tab is replaced with the home tab and the app cannot exit.
     * 3. If there is more than one tab and the selected tab is not the first tab,
     *    the active tab is removed and the app cannot exit.
     * 4. If there is only one tab and there are unsaved files in the text editor,
     *    a dialog is shown to save the files and the app cannot exit.
     *
     * If none of the above conditions are met, the app can exit.
     *
     * @return True if the app can exit, false otherwise.
     */
    fun canExit(): Boolean {
        val tabs = _state.value.tabs
        val selectedTabIndex = _state.value.selectedTabIndex

        if (tabs.isEmpty()) {
            return true
        }

        // Handle tabs's onBackPress
        if (getActiveTab()!!.onBackPressed()) {
            return false
        }

        // Replace the active tab with the home tab (if turned on in settings)
        if (getActiveTab() !is HomeTab && !globalClass.preferencesManager.skipHomeWhenTabClosed) {
            replaceCurrentTabWith(HomeTab())
            return false
        }

        // Remove the active tab
        if (tabs.size > 1 && selectedTabIndex isNot 0) {
            removeTabAt(selectedTabIndex)
            return false
        }

        // Check TextEditor files
        if (tabs.size == 1 && !allTextEditorFileInstancesSaved()) {
            _state.update {
                it.copy(
                    showSaveEditorFilesDialog = true
                )
            }
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

    fun saveTextEditorFiles(onFinish: () -> Unit) {
        _state.update {
            it.copy(
                isSavingFiles = true
            )
        }

        managerScope.launch {
            globalClass.textEditorManager.fileInstanceList.forEach {
                if (it.requireSave) {
                    it.file.writeText(it.content.toString())
                }
            }

            _state.update {
                it.copy(
                    isSavingFiles = false
                )
            }

            onFinish()
        }
    }

    fun loadStartupTabs() {
        managerScope.launch {
            val startupTabs: StartupTabs =
                fromJson(globalClass.preferencesManager.startupTabs)
                    ?: StartupTabs.default()

            val tabs = arrayListOf<Tab>()
            val index = 0

            startupTabs.tabs.forEachIndexed { index, tab ->
                val newTab = when (tab.type) {
                    StartupTabType.FILES -> FilesTab(LocalFileHolder(File(tab.extra)))
                    StartupTabType.APPS -> AppsTab()
                    else -> HomeTab()
                }.apply {
                    if (isCreated) onTabResumed() else onTabStarted()
                }

                tabs.add(newTab)
            }

            _state.update {
                it.copy(
                    tabs = tabs,
                    selectedTabIndex = index
                )
            }
        }
    }

    fun checkForUpdate() {
        fetchGithubReleases { releases ->
            val latestRelease = releases.firstOrNull()
            if (latestRelease != null && latestRelease.tagName != "v${
                    globalClass.packageManager.getPackageInfo(
                        globalClass.packageName,
                        0
                    ).versionName
                }"
            ) {
                newUpdate = latestRelease
                _state.update { it.copy(hasNewUpdate = true) }
                showMsg(globalClass.getString(R.string.new_update_available))
            }
        }
    }

    fun fetchGithubReleases(
        onResult: (List<GithubRelease>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "https://api.github.com/repos/Raival-e/Prism-File-Explorer/releases"
            var releases = emptyList<GithubRelease>()

            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 5000
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "Prism-File-Explorer")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { inputStream ->
                        releases = Gson().fromJson(
                            InputStreamReader(
                                inputStream
                            ),
                            object : TypeToken<List<GithubRelease>>() {}.type
                        )
                    }
                }
            } catch (_: UnknownHostException) {
                logger.logInfo(globalClass.getString(R.string.check_for_updates_failed_no_internet_connection))
            } catch (_: ConnectException) {
                logger.logInfo(globalClass.getString(R.string.check_for_updates_failed_failed_to_connect_to_server))
            } catch (e: Exception) {
                logger.logError(e.printFullStackTrace())
            }

            onResult(releases)
        }
    }

    fun toggleJumpToPathDialog(show: Boolean) {
        _state.update {
            it.copy(
                showJumpToPathDialog = show
            )
        }
    }

    fun toggleAppInfoDialog(show: Boolean) {
        _state.update {
            it.copy(
                showAppInfoDialog = show
            )
        }
    }

    fun toggleSaveEditorFilesDialog(show: Boolean) {
        _state.update {
            it.copy(
                showSaveEditorFilesDialog = show
            )
        }
    }

    fun toggleStartupTabsDialog(show: Boolean) {
        _state.update {
            it.copy(
                showStartupTabsDialog = show
            )
        }
    }

    fun reorderTabs(from: Int, to: Int) {
        _state.update {
            it.copy(
                tabs = _state.value.tabs.toMutableList().apply {
                    add(to, removeAt(from))
                },
                selectedTabIndex = _state.value.tabs.indexOf(getActiveTab())
            )
        }
    }

    fun updateHomeToolbar(title: String, subtitle: String) {
        _state.update {
            it.copy(title = title, subtitle = subtitle)
        }
    }
}