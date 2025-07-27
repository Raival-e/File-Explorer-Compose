package com.raival.compose.file.explorer.screen.main.tab.files.coil

import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pdfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.videoFileType

fun canUseCoil(contentHolder: ContentHolder): Boolean {
    return (contentHolder.isFile()
            && imageFileType.contains(contentHolder.extension)
            || videoFileType.contains(contentHolder.extension)
            || audioFileType.contains(contentHolder.extension)
            || contentHolder.extension == apkFileType
            || contentHolder.extension == pdfFileType)
}