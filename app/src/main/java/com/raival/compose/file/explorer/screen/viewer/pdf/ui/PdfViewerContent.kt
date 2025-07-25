package com.raival.compose.file.explorer.screen.viewer.pdf.ui

import android.util.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.screen.viewer.pdf.PdfViewerInstance
import com.raival.compose.file.explorer.screen.viewer.pdf.misc.PdfPageHolder
import my.nanihadesuka.compose.InternalLazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSettings
import net.engawapg.lib.zoomable.ExperimentalZoomableApi
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomableWithScroll

@OptIn(ExperimentalZoomableApi::class)
@Composable
fun PdfViewerContent(instance: PdfViewerInstance, onBackPress: () -> Unit) {
    BoxWithConstraints(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val constraints = this.constraints
        var showToolbars by remember { mutableStateOf(true) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val pdfPages = remember { mutableStateListOf<PdfPageHolder>() }
        var showInfoDialog by remember { mutableStateOf(false) }

        val listState = rememberLazyListState()
        val zoomState = rememberZoomState()
        var defaultPageSize by remember { mutableStateOf(Size(0, 0)) }
        val isFirstItemVisible by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex == 0
            }
        }

        val visiblePageNumbers by remember {
            derivedStateOf {
                val visiblePages = listState.layoutInfo.visibleItemsInfo.map { visibleItem ->
                    visibleItem.index
                }.filter { it > 0 }
                buildString {
                    append(visiblePages.firstOrNull() ?: 1)
                    if (visiblePages.size > 1) {
                        append("-")
                        append(visiblePages.lastOrNull() ?: 1)
                    }
                }
            }
        }

        LaunchedEffect(isFirstItemVisible) {
            showToolbars = isFirstItemVisible
        }

        LaunchedEffect(Unit) {
            instance.prepare { success ->
                if (success) {
                    defaultPageSize = Size(
                        constraints.maxWidth,
                        instance.defaultPageSize.height * constraints.maxWidth / instance.defaultPageSize.width
                    )
                    pdfPages.addAll(instance.pages)
                    isLoading = false
                } else {
                    errorMessage = globalClass.getString(R.string.failed_to_load_pdf)
                    isLoading = false
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                instance.onClose()
            }
        }

        if (showInfoDialog) {
            InfoDialog(
                title = globalClass.getString(R.string.pdf_info),
                properties = instance.getInfo(),
                onDismiss = { showInfoDialog = false }
            )
        }

        when {
            isLoading -> LoadingScreen()
            errorMessage isNot null -> ErrorScreen(
                message = errorMessage!!,
                onClose = onBackPress
            )

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomableWithScroll(
                            zoomState = zoomState,
                            onTap = { if (!isFirstItemVisible) showToolbars = !showToolbars }
                        ),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(100.dp)) }

                    items(
                        items = pdfPages,
                        key = { it.index }
                    ) { page ->
                        PdfPageItem(
                            page = page,
                            pageSize = defaultPageSize,
                            instance = instance,
                            zoomState = zoomState
                        )
                    }
                }
                InternalLazyColumnScrollbar(
                    modifier = Modifier.padding(top = 110.dp),
                    state = listState,
                    settings = ScrollbarSettings.Default.copy(
                        thumbUnselectedColor = colorScheme.surfaceContainerHigh,
                        thumbSelectedColor = colorScheme.primary,
                        side = ScrollbarLayoutSide.Start,
                        thumbThickness = 6.dp,
                        scrollbarPadding = 12.dp
                    ),
                    indicatorContent = { index, isPressed ->
                        PageIndicator(
                            pageNumber = visiblePageNumbers,
                            isPressed = isPressed,
                            totalPages = pdfPages.size
                        )
                    }
                )

                TopToolbar(
                    visible = showToolbars,
                    title = instance.metadata.name,
                    onBackClick = onBackPress,
                    onInfoClick = {
                        showInfoDialog = true
                    }
                )
            }
        }
    }
}