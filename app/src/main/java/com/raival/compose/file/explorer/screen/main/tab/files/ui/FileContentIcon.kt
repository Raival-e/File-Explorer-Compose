package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.raival.compose.file.explorer.common.icons.Code
import com.raival.compose.file.explorer.common.icons.Iso
import com.raival.compose.file.explorer.common.icons.Java
import com.raival.compose.file.explorer.common.icons.Kotlin
import com.raival.compose.file.explorer.common.icons.Markdown
import com.raival.compose.file.explorer.common.icons.Pdf
import com.raival.compose.file.explorer.common.icons.PrismIcons
import com.raival.compose.file.explorer.common.icons.Sql
import com.raival.compose.file.explorer.common.icons.Vector
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.archiveFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.docFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.excelFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.fontFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.isoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.markdownFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pdfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pptFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.prismPrefsFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.sqlFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vectorFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.videoFileType

data class FileContentIcon(
    val icon: Any,
    val backgroundColor: Color? = Color(0xFF1C439B),
    val iconColor: Color? = Color.White,
)

@Composable
private fun getContentIcon(content: ContentHolder): FileContentIcon {
    if (content.isFolder) return FileContentIcon(
        icon = Icons.Rounded.Folder,
        backgroundColor = colorScheme.surfaceVariant,
        iconColor = colorScheme.onSurfaceVariant
    )

    val extension = content.extension

    if (extension == prismPrefsFileType) return FileContentIcon(Icons.Default.BuildCircle)
    if (extension == javaFileType) return FileContentIcon(PrismIcons.Java)
    if (extension == kotlinFileType) return FileContentIcon(PrismIcons.Kotlin)
    if (extension == markdownFileType) return FileContentIcon(PrismIcons.Markdown)
    if (extension == isoFileType) return FileContentIcon(PrismIcons.Iso)
    if (extension == sqlFileType) return FileContentIcon(PrismIcons.Sql)
    if (extension == pdfFileType) return FileContentIcon(PrismIcons.Pdf)
    if (extension == apkFileType) return FileContentIcon(Icons.Default.Android)

    if (videoFileType.contains(extension)) return FileContentIcon(Icons.Default.Videocam)
    if (imageFileType.contains(extension)) return FileContentIcon(Icons.Default.Image)
    if (docFileType.contains(extension)) return FileContentIcon(Icons.Default.Description)
    if (excelFileType.contains(extension)) return FileContentIcon(Icons.Default.TableChart)
    if (pptFileType.contains(extension)) return FileContentIcon(Icons.Default.Slideshow)
    if (fontFileType.contains(extension)) return FileContentIcon(Icons.Default.TextFields)
    if (vectorFileType.contains(extension)) return FileContentIcon(PrismIcons.Vector)
    if (audioFileType.contains(extension)) return FileContentIcon(Icons.Default.Audiotrack)
    if (codeFileType.contains(extension)) return FileContentIcon(PrismIcons.Code)
    if (editableFileType.contains(extension)) return FileContentIcon(Icons.Default.Description)
    if (archiveFileType.contains(extension)) return FileContentIcon(Icons.Default.Archive)

    return FileContentIcon(Icons.Default.QuestionMark)
}

@Composable
fun FileContentIcon(item: ContentHolder) {
    val fileContentIcon = getContentIcon(item)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = fileContentIcon.backgroundColor ?: colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val fileIcon = fileContentIcon.icon
        if (fileIcon is ImageVector) {
            Icon(
                modifier = Modifier.fillMaxSize(0.7f),
                imageVector = fileIcon,
                contentDescription = null,
                tint = fileContentIcon.iconColor ?: Color.Unspecified
            )
        }
    }
}