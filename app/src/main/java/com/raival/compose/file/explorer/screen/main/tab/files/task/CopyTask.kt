package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.ui.graphics.vector.ImageVector
import com.anggrayudi.storage.extension.launchOnUiThread
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.common.extension.randomString
import com.raival.compose.file.explorer.common.extension.trimToLastTwoSegments
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder

class CopyTask(
    private val source: List<DocumentHolder>
) : FilesTabTask() {
    override val id: String = String.randomString(8)

    override fun getTitle(): String = globalClass.getString(R.string.copy)

    override fun getSubtitle(): String = if (source.size == 1)
        source[0].path.trimToLastTwoSegments()
    else globalClass.getString(R.string.task_subtitle, source.size)

    override fun execute(destination: DocumentHolder, callback: Any) {
        val taskCallback = callback as FilesTabTaskCallback

        val total: Int
        var completed = 0
        var replaced = 0
        var skipped = 0

        val details = FilesTabTaskDetails(
            this,
            TASK_COPY,
            getTitle(),
            globalClass.getString(R.string.preparing),
            emptyString,
            0f
        )

        taskCallback.onPrepare(details)

        val filesToCopy = arrayListOf<String>()

        source.forEach {
            filesToCopy.add(it.path)
            if (!it.isFile) {
                filesToCopy.addAll(it.walk(true).map { f -> f.path })
            }
        }

        total = filesToCopy.size

        fun updateProgress(info: String): FilesTabTaskDetails {
            return details.apply {
                globalClass.getString(R.string.progress, completed + skipped + replaced, total)
                if (progress >= 0) this.progress =
                    (completed + replaced + skipped) / total.toFloat()
                if (info.isNotEmpty()) this.info = info
            }
        }

        fun copyFile(from: DocumentHolder, to: DocumentHolder) {
            if (!filesToCopy.contains(from.path)) return

            val existingFile = from.getName().let { to.findFile(it) }

            taskCallback.onReport(
                updateProgress(
                    info = globalClass.getString(
                        R.string.copying_destination,
                        from.getName(),
                        destination.getName()
                    )
                )
            )

            if (existingFile isNot null) {
                if (from.path == existingFile?.path || !existingFile!!.delete()) {
                    skipped++
                    return
                }
            }

            from.getName().let { fileName ->
                to.createSubFile(fileName)?.let { target ->
                    val inStream = from.openInputStream()
                    val outStream = target.openOutputStream()

                    inStream.use { input ->
                        outStream.use { output ->
                            input?.copyTo(output ?: return)
                        }
                    }

                    if (existingFile isNot null) replaced++ else completed++

                    taskCallback.onReport(updateProgress(emptyString))

                    return
                }
            }

            skipped++
        }

        fun copyFolder(from: DocumentHolder, to: DocumentHolder) {
            if (!filesToCopy.contains(from.path)) return

            val newFolder = from.getName().let { to.createSubFolder(it) }

            if (newFolder isNot null) {
                completed++
                from.listContent(false).forEach { currentFile ->
                    if (currentFile.isFile) {
                        copyFile(currentFile, newFolder!!)
                    } else {
                        copyFolder(currentFile, newFolder!!)
                    }
                }
            } else {
                skipped++
            }
        }

        source.forEach { currentFile ->
            if (currentFile.isFile) {
                copyFile(currentFile, destination)
            } else {
                copyFolder(currentFile, destination)
            }
        }

        launchOnUiThread {
            taskCallback.onComplete(details.apply {
                subtitle = globalClass.getString(R.string.done)
            })
        }
    }

    override fun getIcon(): ImageVector = Icons.Rounded.ContentCopy

    override fun getSourceFiles(): List<DocumentHolder> {
        return source
    }
}