package com.raival.compose.file.explorer.screen.viewer.pdf

import android.net.Uri
import androidx.activity.compose.setContent
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.screen.viewer.pdf.ui.PdfViewerContent
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import net.engawapg.lib.zoomable.ExperimentalZoomableApi

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
                        PdfViewerContent(
                            instance = instance,
                            onBackPress = { onBackPressedDispatcher.onBackPressed() }
                        )
                    }
                }
            }
        } else {
            globalClass.showMsg(getString(R.string.invalid_pdf))
            finish()
        }
    }
}