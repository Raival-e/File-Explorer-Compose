package com.raival.compose.file.explorer.screen.viewer.pdf.ui

import android.util.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.dp
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.ui.Isolate
import com.raival.compose.file.explorer.screen.viewer.pdf.PdfViewerInstance
import com.raival.compose.file.explorer.screen.viewer.pdf.misc.PdfPageHolder
import net.engawapg.lib.zoomable.ZoomState

@Composable
fun PdfPageItem(
    page: PdfPageHolder,
    pageSize: Size,
    instance: PdfViewerInstance,
    zoomState: ZoomState
) {
    var bitmap by remember(page.index) { mutableStateOf(page.bitmap) }
    var isLoading by remember(page.index) { mutableStateOf(false) }

    DisposableEffect(page.index) {
        if (bitmap == null && !isLoading) {
            isLoading = true
            instance.renderPage(page) { readyPage ->
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
                bitmap isNot null -> {
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
            Isolate {
                val showPageNumber by remember(page.index) {
                    derivedStateOf {
                        zoomState.scale < 1.2f
                    }
                }
                if (showPageNumber) {
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
    }
}