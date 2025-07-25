package com.raival.compose.file.explorer.screen.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab
import com.raival.compose.file.explorer.screen.main.tab.apps.ui.AppsTabContentView
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FilesTabContentView
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import com.raival.compose.file.explorer.screen.main.tab.home.ui.HomeTabContentView
import com.raival.compose.file.explorer.screen.main.ui.AppInfoDialog
import com.raival.compose.file.explorer.screen.main.ui.JumpToPathDialog
import com.raival.compose.file.explorer.screen.main.ui.SaveTextEditorFilesDialog
import com.raival.compose.file.explorer.screen.main.ui.StartupTabsSettingsScreen
import com.raival.compose.file.explorer.screen.main.ui.TabLayout
import com.raival.compose.file.explorer.screen.main.ui.Toolbar
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : BaseActivity() {
    private val HOME_SCREEN_SHORTCUT_EXTRA_KEY = "filePath"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()
    }

    override fun onPermissionGranted() {
        setContent {
            FileExplorerTheme {
                SafeSurface {
                    val coroutineScope = rememberCoroutineScope()
                    val mainActivityManager = globalClass.mainActivityManager
                    val mainActivityState by mainActivityManager.state.collectAsState()

                    BackHandler {
                        coroutineScope.launch {
                            if (mainActivityManager.canExit()) {
                                finish()
                            }
                        }
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        mainActivityManager.onResume()
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                        mainActivityManager.onStop()
                    }

                    LaunchedEffect(Unit) {
                        if (hasIntent()) {
                            handleIntent()
                        } else {
                            if (mainActivityState.tabs.isEmpty()) {
                                mainActivityManager.loadStartupTabs()
                            }
                        }
                    }

                    JumpToPathDialog(
                        show = mainActivityState.showJumpToPathDialog,
                        onDismiss = { mainActivityManager.toggleJumpToPathDialog(false) }
                    )

                    AppInfoDialog(
                        show = mainActivityState.showAppInfoDialog,
                        onDismiss = { mainActivityManager.toggleAppInfoDialog(false) }
                    )

                    SaveTextEditorFilesDialog(
                        show = mainActivityState.showSaveEditorFilesDialog,
                        isSaving = mainActivityState.isSavingFiles,
                        onDismiss = { mainActivityManager.toggleSaveEditorFilesDialog(false) },
                        onRequestFinish = { finish() },
                        onSave = { mainActivityManager.saveTextEditorFiles { finish() } }
                    )

                    StartupTabsSettingsScreen(mainActivityState.showStartupTabsDialog) {
                        mainActivityManager.toggleStartupTabsDialog(false)
                        globalClass.preferencesManager.startupTabs = it.toJson()
                    }

                    Column(Modifier.fillMaxSize()) {
                        Toolbar(
                            title = mainActivityState.title,
                            subtitle = mainActivityState.subtitle,
                            onToggleAppInfoDialog = { mainActivityManager.toggleAppInfoDialog(it) }
                        )
                        TabLayout(
                            tabLayoutState = mainActivityState.tabLayoutState,
                            tabs = mainActivityState.tabs,
                            selectedTabIndex = mainActivityState.selectedTabIndex,
                            onReorder = { from, to -> mainActivityManager.reorderTabs(from, to) },
                            onAddNewTab = { mainActivityManager.addTabAndSelect(HomeTab()) },
                        )
                        TabsPager(mainActivityState)
                    }
                }
            }
        }
    }

    @Composable
    fun ColumnScope.TabsPager(state: MainActivityState) {
        val manager = globalClass.mainActivityManager

        if (state.tabs.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            val pagerState = rememberPagerState(initialPage = state.selectedTabIndex) {
                state.tabs.size
            }

            LaunchedEffect(state.selectedTabIndex) {
                if (pagerState.currentPage != state.selectedTabIndex) {
                    pagerState.scrollToPage(state.selectedTabIndex)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    if (page != state.selectedTabIndex) {
                        manager.selectTabAt(page, true)
                    }
                    state.tabLayoutState.animateScrollToItem(
                        state.selectedTabIndex
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                key = { state.tabs[it].id }
            ) { index ->
                key(index) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (state.tabs.isNotEmpty()) {
                            val currentTab = state.tabs[index]
                            when (currentTab) {
                                is FilesTab -> {
                                    FilesTabContentView(currentTab)
                                }

                                is HomeTab -> {
                                    HomeTabContentView(currentTab)
                                }

                                is AppsTab -> {
                                    AppsTabContentView(currentTab)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        globalClass.cleanOnExitDir()
    }

    private fun hasIntent(): Boolean {
        return intent != null && intent!!.hasExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)
    }

    private fun handleIntent() {
        intent?.let {
            if (it.hasExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)) {
                globalClass.mainActivityManager.jumpToFile(
                    file = LocalFileHolder(File(it.getStringExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)!!)),
                    context = this
                )
                intent = null
            }
        }
    }
}