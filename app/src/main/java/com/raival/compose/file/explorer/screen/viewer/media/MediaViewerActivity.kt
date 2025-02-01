package com.raival.compose.file.explorer.screen.viewer.media

import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.KeepScreenOn
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.instance.MediaViewerInstance
import com.raival.compose.file.explorer.screen.viewer.media.misc.MediaSource
import com.raival.compose.file.explorer.screen.viewer.media.ui.AudioPlayer
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
                        if (instance.mediaSource is MediaSource.AudioSource) {
                            AudioPlayer(instance)
                        } else {
                            Box(
                                Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LaunchedEffect(Unit) {
                                    instance.player.play()
                                }

                                AndroidView(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    factory = { context ->
                                        PlayerView(context).apply {
                                            useController =
                                                instance.mediaSource is MediaSource.VideoSource
                                            player = instance.player.apply {
                                                repeatMode = Player.REPEAT_MODE_ONE
                                            }
                                        }
                                    },
                                    update = { },
                                    onRelease = {
                                        it.player?.release()
                                    }
                                )
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