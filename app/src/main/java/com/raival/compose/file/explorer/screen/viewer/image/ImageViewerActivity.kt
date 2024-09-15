package com.raival.compose.file.explorer.screen.viewer.image

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.read
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.image.instance.ImageViewerInstance
import com.raival.compose.file.explorer.ui.theme.FileExplorerTheme
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

class ImageViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return ImageViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        setContent {
            FileExplorerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.surfaceContainerLowest
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val image = remember { mutableStateOf(ByteArray(0)) }
                        var isLoaded by remember { mutableStateOf(false) }
                        var isError by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            image.value = instance.uri.read()
                        }

                        if (!isLoaded) {
                            CircularProgressIndicator()
                        }

                        if (isError) {
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(R.drawable.unknown_file_extension),
                                contentDescription = null
                            )
                        } else {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .zoomable(rememberZoomState()),
                                model = image.value,
                                contentDescription = null,
                                onState = { state ->
                                    when (state) {
                                        is AsyncImagePainter.State.Success -> {
                                            isLoaded = true
                                        }

                                        is AsyncImagePainter.State.Error -> {
                                            isError = true
                                        }

                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}