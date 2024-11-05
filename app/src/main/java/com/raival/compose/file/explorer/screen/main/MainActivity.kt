package com.raival.compose.file.explorer.screen.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationSource
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.compose.SafeSurface
import com.raival.compose.file.explorer.screen.main.compose.DrawerContent
import com.raival.compose.file.explorer.screen.main.compose.JumpToPathDialog
import com.raival.compose.file.explorer.screen.main.compose.NewTabDialog
import com.raival.compose.file.explorer.screen.main.compose.SaveTextEditorFilesDialog
import com.raival.compose.file.explorer.screen.main.compose.TabContentView
import com.raival.compose.file.explorer.screen.main.compose.TabLayout
import com.raival.compose.file.explorer.screen.main.compose.Toolbar
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.regular.provider.FileProvider
import com.raival.compose.file.explorer.ui.theme.FileExplorerTheme
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
                SafeSurface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
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
                                RegularTab(
                                    FileProvider.getPrimaryInternalStorage(globalClass).documentHolder,
                                    context
                                )
                            )
                        }
                        handleIntent()
                    }

                    JumpToPathDialog()

                    NewTabDialog()

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