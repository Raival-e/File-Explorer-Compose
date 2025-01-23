package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.ui.graphics.vector.ImageVector
import com.anggrayudi.storage.extension.launchOnUiThread
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.randomString
import com.raival.compose.file.explorer.common.extension.trimToLastTwoSegments
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import java.io.File

class DeleteTask(
    private val source: List<DocumentHolder>,
    private val moveToRecycleBin: Boolean = false
) : FilesTabTask() {
    override val id: String = String.randomString(8)

    override fun getTitle(): String = globalClass.getString(R.string.delete)

    override fun getSubtitle(): String = if (source.size == 1)
        source[0].getPath().trimToLastTwoSegments()
    else globalClass.getString(R.string.task_subtitle, source.size)

    override fun execute(destination: DocumentHolder, callback: Any) {
        if (moveToRecycleBin && !destination.hasParent(globalClass.recycleBinDir)) {
            MoveTask(source).execute(
                DocumentHolder.fromFile(
                    File(
                        globalClass.recycleBinDir.toFile()!!,
                        "${System.currentTimeMillis()}"
                    ).apply {
                        mkdirs()
                    }
                ),
                callback
            )
            return
        }

        val taskCallback = callback as FilesTabTaskCallback

        var deleted = 0
        var skipped = 0

        val details = FilesTabTaskDetails(
            this,
            TASK_DELETE,
            getTitle(),
            globalClass.getString(R.string.preparing),
            emptyString,
            -1f
        )
        taskCallback.onPrepare(details)

        fun updateProgress(info: String = emptyString): FilesTabTaskDetails {
            return details.apply {
                subtitle = globalClass.getString(R.string.progress_short, deleted)
                if (info.isNotEmpty()) this.info = info
            }
        }

        fun deleteFolder(toDelete: DocumentHolder) {
            updateProgress(globalClass.getString(R.string.deleting, toDelete.getName()))

            if (toDelete.isEmpty()) {
                if (toDelete.delete()) {
                    deleted++
                } else {
                    skipped++
                }
            } else {
                toDelete.listContent(false).forEach {
                    if (it.isFile()) {
                        updateProgress(globalClass.getString(R.string.deleting, it.getName()))
                        if (it.delete()) {
                            deleted++
                        } else {
                            skipped++
                        }
                    } else {
                        deleteFolder(it)
                    }
                }
                if (toDelete.delete()) {
                    deleted++
                } else {
                    skipped++
                }
            }
        }

        source.forEach { currentFile ->
            if (currentFile.isFile()) {
                updateProgress(globalClass.getString(R.string.deleting, currentFile.getName()))
                if (currentFile.delete()) {
                    deleted++
                } else {
                    skipped++
                }
            } else {
                deleteFolder(currentFile)
            }
        }

        launchOnUiThread {
            taskCallback.onComplete(details.apply {
                subtitle = globalClass.getString(R.string.done)
            })
        }
    }

    override fun getIcon(): ImageVector = Icons.Rounded.ContentCut

    override fun getSourceFiles(): List<DocumentHolder> {
        return source
    }
}