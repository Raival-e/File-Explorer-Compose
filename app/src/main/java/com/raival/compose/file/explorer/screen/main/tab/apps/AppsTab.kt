package com.raival.compose.file.explorer.screen.main.tab.apps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.apps.holder.AppHolder
import com.raival.compose.file.explorer.screen.main.tab.apps.provider.getInstalledApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppsTab : Tab() {
    override val id = globalClass.generateUid()
    override val title = globalClass.getString(R.string.apps_tab_title)
    override val subtitle = emptyString
    override val header = globalClass.getString(R.string.apps_tab_header)

    val appsList = mutableStateListOf<AppHolder>()
    val systemApps = ArrayList<AppHolder>()
    val userApps = ArrayList<AppHolder>()

    var selectedChoice by mutableIntStateOf(0)
    var previewAppDialog by mutableStateOf<AppHolder?>(null)
    var isSearchPanelOpen by mutableStateOf(false)
    var searchQuery by mutableStateOf(emptyString)
    var isSearching by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var sortOption by mutableStateOf(SortOption.NAME)

    enum class SortOption {
        NAME, SIZE, INSTALL_DATE, UPDATE_DATE
    }

    override fun onTabResumed() {
        super.onTabResumed()
        requestHomeToolbarUpdate()
    }

    override fun onTabStarted() {
        super.onTabStarted()
        requestHomeToolbarUpdate()
    }

    override fun onBackPressed(): Boolean {
        if (isSearchPanelOpen) {
            isSearchPanelOpen = false
            searchQuery = emptyString
            updateAppsList()
            return true
        }
        return false
    }

    fun fetchInstalledApps() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apps = getInstalledApps(globalClass)
                apps.forEach { app ->
                    if (app.isSystemApp) systemApps.add(app)
                    else userApps.add(app)
                }
                updateAppsList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateAppsList() {
        appsList.clear()
        val filteredApps = when (selectedChoice) {
            0 -> userApps
            1 -> systemApps
            2 -> userApps + systemApps
            else -> emptyList()
        }

        val sortedApps = when (sortOption) {
            SortOption.NAME -> filteredApps.sortedBy { it.name.lowercase() }
            SortOption.SIZE -> filteredApps.sortedByDescending { it.size }
            SortOption.INSTALL_DATE -> filteredApps.sortedByDescending { it.installDate }
            SortOption.UPDATE_DATE -> filteredApps.sortedByDescending { it.lastUpdateDate }
        }

        appsList.addAll(sortedApps)
    }

    fun performSearch() {
        if (searchQuery.isBlank()) {
            updateAppsList()
            return
        }

        isSearching = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val baseList = when (selectedChoice) {
                    0 -> userApps
                    1 -> systemApps
                    2 -> userApps + systemApps
                    else -> emptyList()
                }

                val filteredApps = baseList.filter { app ->
                    app.name.contains(searchQuery, ignoreCase = true) ||
                            app.packageName.contains(searchQuery, ignoreCase = true)
                }

                val sortedApps = when (sortOption) {
                    SortOption.NAME -> filteredApps.sortedBy { it.name.lowercase() }
                    SortOption.SIZE -> filteredApps.sortedByDescending { it.size }
                    SortOption.INSTALL_DATE -> filteredApps.sortedByDescending { it.installDate }
                    SortOption.UPDATE_DATE -> filteredApps.sortedByDescending { it.lastUpdateDate }
                }

                appsList.clear()
                appsList.addAll(sortedApps)
            } finally {
                isSearching = false
            }
        }
    }
}