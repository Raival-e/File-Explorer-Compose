package com.raival.compose.file.explorer.screen.viewer.media

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.KeepScreenOn
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.instance.MediaViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.misc.MediaSource
import com.raival.compose.file.explorer.screen.viewer.media.ui.AudioPlayer
import com.raival.compose.file.explorer.screen.viewer.media.ui.VideoPlayer
import com.raival.compose.file.explorer.theme.FileExplorerTheme

class MediaViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return MediaViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        if (instance is MediaViewerInstance) {
            setContent {
                KeepScreenOn()
                FileExplorerTheme {
                    SafeSurface {
                        when (instance.mediaSource) {
                            is MediaSource.AudioSource -> {
                                AudioPlayer(instance)
                            }
                            is MediaSource.VideoSource -> {
                                VideoPlayer(
                                    instance = instance,
                                    onBackClick = { finish() }
                                )
                            }
                            else -> {
                                // Handle unknown media type - show error and finish
                                LaunchedEffect(Unit) {
                                    globalClass.showMsg(getString(R.string.invalid_media_file))
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            globalClass.showMsg(getString(R.string.invalid_media_file))
            finish()
        }
    }
}