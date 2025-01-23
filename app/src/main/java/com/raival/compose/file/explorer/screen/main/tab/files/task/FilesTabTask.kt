package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.ui.graphics.vector.ImageVector
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder

abstract class FilesTabTask {
    abstract val id: String
    abstract fun getTitle(): String
    abstract fun getSubtitle(): String
    abstract fun execute(destination: DocumentHolder, callback: Any)
    abstract fun getIcon(): ImageVector
    abstract fun getSourceFiles(): List<DocumentHolder>

    fun isValidSourceFiles(): Boolean {
        getSourceFiles().forEach {
            if (!it.exists()) return false
        }
        return true
    }

    companion object {
        const val TASK_NONE = -1
        const val TASK_COPY = 0
        const val TASK_MOVE = 1
        const val TASK_DELETE = 2
        const val TASK_COMPRESS = 3
        const val TASK_DECOMPRESS = 4
    }
}