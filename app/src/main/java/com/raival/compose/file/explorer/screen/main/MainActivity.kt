package com.raival.compose.file.explorer.screen.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.ui.AppInfoDialog
import com.raival.compose.file.explorer.screen.main.ui.JumpToPathDialog
import com.raival.compose.file.explorer.screen.main.ui.SaveTextEditorFilesDialog
import com.raival.compose.file.explorer.screen.main.ui.TabContentView
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
                    val pagerState =
                        rememberPagerState(initialPage = mainActivityManager.selectedTabIndex) {
                            mainActivityManager.tabs.size
                        }

                    BackHandler {
                        coroutineScope.launch {
                            if (mainActivityManager.canExit()) {
                                finish()
                            }
                        }
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        if (mainActivityManager.tabs.isNotEmpty())
                            mainActivityManager.getActiveTab().onTabResumed()
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                        if (mainActivityManager.tabs.isNotEmpty())
                            mainActivityManager.getActiveTab().onTabStopped()
                    }

                    LaunchedEffect(Unit) {
                        handleIntent()
                    }

                    LaunchedEffect(mainActivityManager.selectedTabIndex) {
                        if (mainActivityManager.tabs.isEmpty()) {
                            mainActivityManager.loadStartupTabs()
                        }

                        if (pagerState.currentPage != mainActivityManager.selectedTabIndex) {
                            pagerState.scrollToPage(mainActivityManager.selectedTabIndex)
                        }
                    }

                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collect { page ->
                            if (page != mainActivityManager.selectedTabIndex) {
                                mainActivityManager.selectTabAt(page)
                            }
                            mainActivityManager.tabLayoutState.animateScrollToItem(
                                mainActivityManager.selectedTabIndex
                            )
                        }
                    }

                    JumpToPathDialog()

                    AppInfoDialog()

                    SaveTextEditorFilesDialog { finish() }

                    Column(Modifier.fillMaxSize()) {
                        Toolbar()
                        TabLayout()
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.weight(1f),
                            key = { mainActivityManager.tabs[it].id }
                        ) { index ->
                            key(index) {
                                TabContentView(index)
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