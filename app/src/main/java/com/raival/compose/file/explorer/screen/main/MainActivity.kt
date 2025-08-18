package com.raival.compose.file.explorer.screen.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.isNot
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
import kotlin.math.abs
import kotlin.math.exp

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
                        mainActivityManager.checkForUpdate()
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
                        hasNewUpdate = mainActivityState.hasNewUpdate,
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
                        if (!globalClass.preferencesManager.hideToolbar) {
                            Toolbar(
                                title = mainActivityState.title,
                                subtitle = mainActivityState.subtitle,
                                hasNewUpdate = mainActivityState.hasNewUpdate,
                                onToggleAppInfoDialog = { mainActivityManager.toggleAppInfoDialog(it) }
                            )
                        }
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
                if (pagerState.currentPage isNot state.selectedTabIndex) {
                    pagerState.scrollToPage(state.selectedTabIndex)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    if (page isNot state.selectedTabIndex) {
                        manager.selectTabAt(page, true)
                    }
                    state.tabLayoutState.animateScrollToItem(page)
                }
            }

            var overscrollAmount by remember { mutableFloatStateOf(0f) }
            val threshold = 100f
            val animationScope = rememberCoroutineScope()
            var isAnimatingBack by remember { mutableStateOf(false) }

            fun applyExponentialTension(current: Float, addition: Float, threshold: Float): Float {
                return if (current < threshold) {
                    current + addition
                } else {
                    val excess = current - threshold
                    val decayFactor =
                        exp(-excess / threshold * 2f) // Adjust multiplier for steepness
                    current + (addition * decayFactor)
                }
            }

            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        // Smoothly animate overscroll back to zero when scrolling in opposite direction
                        if (available.x > 0 && overscrollAmount > 0 && !isAnimatingBack) {
                            isAnimatingBack = true
                            animationScope.launch {
                                animate(
                                    initialValue = overscrollAmount,
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = 150,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { value, _ ->
                                    overscrollAmount = value
                                }
                                isAnimatingBack = false
                            }
                        }
                        return Offset.Zero
                    }

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        // Check if we're on the last page and there's leftover scroll
                        val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
                        if (isLastPage && available.x < 0 && source == NestedScrollSource.UserInput) {
                            val availableAmount = abs(available.x)

                            overscrollAmount = applyExponentialTension(
                                overscrollAmount,
                                availableAmount,
                                threshold
                            )
                            return Offset(available.x, 0f) // Consume the scroll
                        }
                        return Offset.Zero
                    }

                    override suspend fun onPreFling(available: Velocity): Velocity {
                        val isLastPage = pagerState.currentPage == pagerState.pageCount - 1

                        if (isLastPage && overscrollAmount > 0 && !isAnimatingBack) {
                            // Trigger action when releasing overscroll
                            if (overscrollAmount > threshold) {
                                manager.addTabAndSelect(HomeTab())
                            }

                            // Animate overscroll back to 0 to prevent page jumping
                            // This helps avoid the pager shooting to previous page
                            // Animate overscroll back to 0 to prevent page jumping
                            isAnimatingBack = true
                            animationScope.launch {
                                animate(
                                    initialValue = overscrollAmount,
                                    targetValue = 0f,
                                    animationSpec = tween(
                                        durationMillis = 200,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { value, _ ->
                                    overscrollAmount = value
                                }
                                isAnimatingBack = false
                            }

                            // Don't consume velocity if we're not overscrolling significantly
                            // This prevents interfering with normal pager fling behavior
                            return if (overscrollAmount > 10f) {
                                Velocity(
                                    available.x * 0.3f,
                                    available.y
                                ) // Reduce horizontal velocity
                            } else {
                                Velocity.Zero
                            }
                        }

                        overscrollAmount = 0f
                        return Velocity.Zero
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection),
                key = { state.tabs[it].id }
            ) { index ->
                key(index) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                translationX = -overscrollAmount
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
        return intent isNot null && intent!!.hasExtra(HOME_SCREEN_SHORTCUT_EXTRA_KEY)
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