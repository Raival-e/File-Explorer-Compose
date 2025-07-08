package com.raival.compose.file.explorer.screen.viewer.pdf

import android.net.Uri
import android.util.Size
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.dp
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.pdf.misc.PdfPageHolder
import com.raival.compose.file.explorer.screen.viewer.pdf.ui.InfoDialog
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import my.nanihadesuka.compose.InternalLazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarLayoutSide
import my.nanihadesuka.compose.ScrollbarSettings
import net.engawapg.lib.zoomable.ExperimentalZoomableApi
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomableWithScroll

class PdfViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return PdfViewerInstance(uri, uid)
    }

    @OptIn(ExperimentalZoomableApi::class)
    override fun onReady(instance: ViewerInstance) {
        if (instance is PdfViewerInstance) {
            setContent {
                FileExplorerTheme {
                    SafeSurface(false) {
                        PdfViewerContent(instance = instance)
                    }
                }
            }
        } else {
            globalClass.showMsg(getString(R.string.invalid_pdf))
            finish()
        }
    }

    @OptIn(ExperimentalZoomableApi::class)
    @Composable
    private fun PdfViewerContent(instance: PdfViewerInstance) {
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
                        errorMessage = getString(R.string.failed_to_load_pdf)
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
                errorMessage != null -> ErrorScreen(
                    message = errorMessage!!,
                    onRetry = { finish() }
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
                                instance = instance
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
                        onBackClick = { onBackPressedDispatcher.onBackPressed() },
                        onInfoClick = {
                            showInfoDialog = true
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun PdfPageItem(
        page: PdfPageHolder,
        pageSize: Size,
        instance: PdfViewerInstance
    ) {
        var bitmap by remember(page.index) { mutableStateOf(page.bitmap) }
        var isLoading by remember(page.index) { mutableStateOf(false) }

        DisposableEffect(page.index) {
            if (bitmap == null && !isLoading) {
                isLoading = true
                instance.fetchPage(page) { readyPage ->
                    bitmap = readyPage.bitmap
                    isLoading = false
                }
            }
            onDispose {
                instance.recycle(page)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLowest
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pageSize.height.dp())
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmap != null -> {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = bitmap!!,
                            contentDescription = stringResource(R.string.page, page.index + 1),
                            contentScale = ContentScale.Fit
                        )
                    }

                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }

                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Description,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.page, page.index + 1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Page number overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = colorScheme.primary.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "${page.index + 1}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }
    }

    @Composable
    private fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.loading_pdf),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface
                )
            }
        }
    }

    @Composable
    private fun ErrorScreen(
        message: String,
        onRetry: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.error),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.go_back))
                }
            }
        }
    }

    @Composable
    private fun PageIndicator(
        pageNumber: String,
        isPressed: Boolean,
        totalPages: Int,
    ) {
        Card(
            modifier = Modifier
                .padding(start = 4.dp)
                .alpha(0.8f),
            colors = CardDefaults.cardColors(
                containerColor = if (isPressed)
                    colorScheme.primaryContainer
                else
                    colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 8.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pageNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPressed)
                        colorScheme.onPrimaryContainer
                    else
                        colorScheme.onSurface
                )
                Text(
                    text = "of $totalPages",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPressed)
                        colorScheme.onPrimaryContainer
                    else
                        colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun TopToolbar(
        visible: Boolean,
        title: String,
        onBackClick: () -> Unit,
        onInfoClick: () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = colorScheme.surfaceContainer)
            ) {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onInfoClick) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}