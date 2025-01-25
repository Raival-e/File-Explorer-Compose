package com.raival.compose.file.explorer.screen.main.tab.files.coil

import coil3.map.Mapper
import coil3.request.Options
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_AI
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_ARCHIVE
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_AUDIO
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_CODE
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_CSS
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_DOC
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_FOLDER
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_FONT
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_ISO
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_JAVA
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_JS
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_KOTLIN
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_PPT
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_PSD
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_SQL
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_TEXT
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_VCF
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_VECTOR
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder.Companion.FILE_TYPE_XLS

class DocumentFileMapper : Mapper<DocumentHolder, Any> {
    override fun map(data: DocumentHolder, options: Options): Any {
        return when {
            data.getFileIconType() == FILE_TYPE_FOLDER -> R.drawable.baseline_folder_24
            data.getFileIconType() == FILE_TYPE_AI -> R.drawable.ai_file_extension
            data.getFileIconType() == FILE_TYPE_CSS -> R.drawable.css_file_extension
            data.getFileIconType() == FILE_TYPE_ISO -> R.drawable.iso_file_extension
            data.getFileIconType() == FILE_TYPE_JS -> R.drawable.js_file_extension
            data.getFileIconType() == FILE_TYPE_PSD -> R.drawable.psd_file_extension
            data.getFileIconType() == FILE_TYPE_SQL -> R.drawable.sql_file_extension
            data.getFileIconType() == FILE_TYPE_VCF -> R.drawable.vcf_file_extension
            data.getFileIconType() == FILE_TYPE_JAVA -> R.drawable.css_file_extension
            data.getFileIconType() == FILE_TYPE_KOTLIN -> R.drawable.css_file_extension
            data.getFileIconType() == FILE_TYPE_DOC -> R.drawable.doc_file_extension
            data.getFileIconType() == FILE_TYPE_XLS -> R.drawable.xls_file_extension
            data.getFileIconType() == FILE_TYPE_PPT -> R.drawable.ppt_file_extension
            data.getFileIconType() == FILE_TYPE_FONT -> R.drawable.font_file_extension
            data.getFileIconType() == FILE_TYPE_VECTOR -> R.drawable.vector_file_extension
            data.getFileIconType() == FILE_TYPE_AUDIO -> R.drawable.music_file_extension
            data.getFileIconType() == FILE_TYPE_CODE -> R.drawable.css_file_extension
            data.getFileIconType() == FILE_TYPE_TEXT -> R.drawable.txt_file_extension
            data.getFileIconType() == FILE_TYPE_ARCHIVE -> R.drawable.zip_file_extension
            else -> data.uri
        }
    }
}