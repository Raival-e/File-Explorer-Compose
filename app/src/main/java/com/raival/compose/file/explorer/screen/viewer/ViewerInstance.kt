package com.raival.compose.file.explorer.screen.viewer

import android.net.Uri

interface ViewerInstance {
    val uri: Uri
    val id: String

    fun onClose()
}