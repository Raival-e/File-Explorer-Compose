package com.raival.compose.file.explorer.screen.viewer.video

import android.net.Uri
import androidx.activity.compose.setContent
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.video.ui.VideoPlayerScreen
import com.raival.compose.file.explorer.theme.FileExplorerTheme

class VideoPlayerActivity : ViewerActivity() {
    override fun onCreateNewInstance(
        uri: Uri,
        uid: String
    ): ViewerInstance {
        return VideoPlayerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        val videoPlayerInstance = instance as VideoPlayerInstance
        setContent {
            FileExplorerTheme {
                VideoPlayerScreen(
                    videoUri = videoPlayerInstance.uri,
                    videoPlayerInstance = videoPlayerInstance,
                    onBackPressed = { finish() }
                )
            }
        }
    }

}