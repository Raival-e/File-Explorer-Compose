package com.raival.compose.file.explorer.screen.main.tab.files.coil

import coil3.map.Mapper
import coil3.request.Options
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder

class DocumentFileMapper : Mapper<ContentHolder, Any> {
    override fun map(data: ContentHolder, options: Options): Any? {
        return when {
            data is LocalFileHolder && canUseCoil(data) -> data.file
            data is ZipFileHolder && !data.isFolder -> data
            else -> null
        }
    }
}