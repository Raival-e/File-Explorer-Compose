package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import net.lingala.zip4j.ZipFile
import java.io.File

class CompressTask(
    val sourceContent: List<ContentHolder>
) : Task() {
    private var parameters: CompressTaskParameters? = null
    private var pendingContent = arrayListOf<TaskContentItem>()

    override val metadata = System.currentTimeMillis().toFormattedDate().let { time ->
        TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(R.string.compress),
            subtitle = globalClass.resources.getString(R.string.task_subtitle, sourceContent.size),
            displayDetails = sourceContent.joinToString(", ") { it.displayName },
            fullDetails = buildString {
                sourceContent.forEachIndexed { index, source ->
                    append(source.displayName)
                    append("\n")
                }
                append("\n")
                append(time)
            },
            isCancellable = true,
            canMoveToBackground = true
        )
    }
    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status

    override fun validate() = sourceContent.find { !it.isValid() } == null

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
        }
    }

    override suspend fun run() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

    override suspend fun run(params: TaskParameters) {
        parameters = params as CompressTaskParameters
        progressMonitor.status = TaskStatus.RUNNING
        protect = false

        if (sourceContent.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.preparing)

        val basePath = sourceContent[0].getParent()?.uniquePath ?: emptyString

        if (pendingContent.isEmpty()) {
            sourceContent.forEachIndexed { index, content ->
                pendingContent.add(
                    TaskContentItem(
                        content = content,
                        relativePath = content.uniquePath.removePrefix("/$basePath"),
                        status = TaskContentStatus.PENDING
                    )
                )
            }
        }

        progressMonitor.apply {
            totalContent = pendingContent.size
            processName = globalClass.getString(R.string.compressing)
        }

        ZipFile(parameters!!.destPath).use { zipFile ->
            pendingContent.forEachIndexed { index, itemToCompress ->
                if (aborted) {
                    progressMonitor.status = TaskStatus.PAUSED
                    return
                }

                if (itemToCompress.status == TaskContentStatus.PENDING) {
                    progressMonitor.apply {
                        contentName = itemToCompress.content.displayName
                        remainingContent = pendingContent.size - (index + 1)
                        progress = -1f
                    }

                    try {
                        if (itemToCompress.content.isFolder) {
                            zipFile.addFolder(File(itemToCompress.content.uniquePath))
                        } else {
                            zipFile.addFile(itemToCompress.content.uniquePath)
                        }
                        itemToCompress.status = TaskContentStatus.SUCCESS
                    } catch (e: Exception) {
                        logger.logError(e)
                        markAsFailed(
                            globalClass.resources.getString(
                                R.string.task_summary_failed,
                                e.message ?: emptyString
                            )
                        )
                        return
                    }
                }
            }
        }

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.status = TaskStatus.SUCCESS
            progressMonitor.summary = buildString {
                pendingContent.forEach { content ->
                    append(content.content.displayName)
                    append(" -> ")
                    append(content.status.name)
                }
            }
        }
    }

    override fun setParameters(params: TaskParameters) {
        parameters = params as CompressTaskParameters
    }

    override suspend fun continueTask() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

}