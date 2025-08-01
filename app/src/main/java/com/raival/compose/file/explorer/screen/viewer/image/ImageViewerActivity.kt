package com.raival.compose.file.explorer.screen.viewer.image

import android.net.Uri
import androidx.activity.compose.setContent
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.image.ui.ImageViewerScreen
import com.raival.compose.file.explorer.theme.FileExplorerTheme

class ImageViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return ImageViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        setContent {
            FileExplorerTheme {
                SafeSurface(enableStatusBarsPadding = false) {
                    ImageViewerScreen(instance)
                }
            }
        }
    }
}