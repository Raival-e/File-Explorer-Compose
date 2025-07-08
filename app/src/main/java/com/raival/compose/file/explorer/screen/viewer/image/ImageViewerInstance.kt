package com.raival.compose.file.explorer.screen.viewer.image

import android.net.Uri
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance

class ImageViewerInstance(
    override val uri: Uri,
    override val id: String
) : ViewerInstance {
    override fun onClose() {

    }
}