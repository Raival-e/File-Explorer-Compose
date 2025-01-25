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
import com.raival.compose.file.explorer.screen.main.tab.apps.modal.AppHolder
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

    var isLoading by mutableStateOf(false)

    override fun onTabResumed() {
        super.onTabResumed()
        requestHomeToolbarUpdate()
    }

    fun fetchInstalledApps() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            getInstalledApps(globalClass).forEach {
                if (it.isSystemApp) systemApps.add(it)
                else userApps.add(it)
            }

            appsList.clear()
            appsList.addAll(userApps)

            isLoading = false
        }
    }
}