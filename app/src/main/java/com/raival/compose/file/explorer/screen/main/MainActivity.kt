package com.raival.compose.file.explorer.screen.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.home.HomeTab
import com.raival.compose.file.explorer.screen.main.ui.DrawerContent
import com.raival.compose.file.explorer.screen.main.ui.JumpToPathDialog
import com.raival.compose.file.explorer.screen.main.ui.SaveTextEditorFilesDialog
import com.raival.compose.file.explorer.screen.main.ui.TabContentView
import com.raival.compose.file.explorer.screen.main.ui.TabLayout
import com.raival.compose.file.explorer.screen.main.ui.Toolbar
import com.raival.compose.file.explorer.theme.FileExplorerTheme
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

                    BackHandler {
                        if (mainActivityManager.canExit(coroutineScope)) {
                            finish()
                        }
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                        if (mainActivityManager.tabs.isNotEmpty())
                            mainActivityManager.tabs[mainActivityManager.selectedTabIndex].onTabResumed()
                    }

                    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                        if (mainActivityManager.tabs.isNotEmpty())
                            mainActivityManager.tabs[mainActivityManager.selectedTabIndex].onTabStopped()
                    }

                    LaunchedEffect(mainActivityManager.selectedTabIndex) {
                        if (mainActivityManager.tabs.isEmpty()) {
                            mainActivityManager.addTabAndSelect(
                                HomeTab()
                            )
                        }
                        handleIntent()
                    }

                    JumpToPathDialog()

                    SaveTextEditorFilesDialog { finish() }

                    ModalNavigationDrawer(
                        drawerState = mainActivityManager.drawerState,
                        drawerContent = {
                            DrawerContent()
                        }
                    ) {
                        Column(Modifier.fillMaxSize()) {
                            Toolbar()
                            TabLayout()
                            TabContentView()
                        }
                    }
                }
            }
        }
    }

    private fun handleIntent() {
        intent?.let {
            if (it.hasExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)) {
                globalClass.mainActivityManager.jumpToFile(
                    DocumentHolder.fromFile(File(it.getStringExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)!!)),
                    this
                )
                it.removeExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)
            }
        }
    }
}