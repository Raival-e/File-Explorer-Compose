package com.raival.compose.file.explorer.screen.main.tab.files.coil

import coil3.map.Mapper
import coil3.request.Options
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.aiFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.archiveFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.cssFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.docFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.excelFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.fontFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.isoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.javaFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.kotlinFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pptFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.psdFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.sqlFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vcfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vectorFileType

class DocumentFileMapper : Mapper<ContentHolder, Any> {
    override fun map(data: ContentHolder, options: Options): Any {
        return when {
            data.isFolder -> R.drawable.baseline_folder_24
            data is LocalFileHolder && canUseCoil(data) -> data.icon
            data is ZipFileHolder && !data.isFolder -> data
            data.extension == aiFileType -> R.drawable.ai_file_extension
            data.extension == cssFileType -> R.drawable.css_file_extension
            data.extension == isoFileType -> R.drawable.iso_file_extension
            data.extension == jsFileType -> R.drawable.js_file_extension
            data.extension == psdFileType -> R.drawable.psd_file_extension
            data.extension == sqlFileType -> R.drawable.sql_file_extension
            data.extension == vcfFileType -> R.drawable.vcf_file_extension
            data.extension == javaFileType -> R.drawable.css_file_extension
            data.extension == kotlinFileType -> R.drawable.css_file_extension
            imageFileType.contains(data.extension) -> R.drawable.jpg_file_extension
            docFileType.contains(data.extension) -> R.drawable.doc_file_extension
            excelFileType.contains(data.extension) -> R.drawable.xls_file_extension
            pptFileType.contains(data.extension) -> R.drawable.ppt_file_extension
            fontFileType.contains(data.extension) -> R.drawable.font_file_extension
            vectorFileType.contains(data.extension) -> R.drawable.vector_file_extension
            audioFileType.contains(data.extension) -> R.drawable.music_file_extension
            codeFileType.contains(data.extension) -> R.drawable.css_file_extension
            editableFileType.contains(data.extension) -> R.drawable.txt_file_extension
            archiveFileType.contains(data.extension) -> R.drawable.zip_file_extension
            apkFileType == data.extension -> R.drawable.apk_file_extension
            else -> R.drawable.unknown_file_extension
        }
    }
}