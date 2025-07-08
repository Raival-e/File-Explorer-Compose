package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import net.lingala.zip4j.ZipFile

class DeleteTask(
    val sourceContent: List<ContentHolder>
) : Task() {
    private var aborted = false
    private var parameters: DeleteTaskParameters? = null
    private var pendingContent = arrayListOf<DeleteContentItem>()

    override val metadata = System.currentTimeMillis().toFormattedDate().let { time ->
        TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(R.string.delete),
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
            canMoveToBackground = false
        )
    }

    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status

    override fun validate() = sourceContent.find { !it.isValid() } == null

    override fun abortTask() {
        aborted = true
    }

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
        }
    }

    override suspend fun run(params: TaskParameters) {
        parameters = params as DeleteTaskParameters
        progressMonitor.status = TaskStatus.RUNNING

        if (sourceContent.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.preparing)

        if (pendingContent.isEmpty()) {
            sourceContent.forEachIndexed { index, content ->
                pendingContent.add(
                    DeleteContentItem(source = content, status = TaskContentStatus.PENDING)
                )
            }
        }

        progressMonitor.apply {
            totalContent = pendingContent.size
            processName = globalClass.getString(R.string.deleting)
        }

        pendingContent.forEachIndexed { index, itemToDelete ->
            if (aborted) {
                progressMonitor.status = TaskStatus.CANCELLED
                return
            }

            if (itemToDelete.status == TaskContentStatus.PENDING) {
                progressMonitor.apply {
                    contentName = itemToDelete.source.displayName
                    remainingContent = pendingContent.size - index + 1
                    progress = (index + 1f) / pendingContent.size
                }

                try {
                    if (itemToDelete.source is LocalFileHolder) {
                        itemToDelete.source.file.deleteRecursively()
                    } else if (itemToDelete.source is ZipFileHolder) {
                        ZipFile(itemToDelete.source.zipTree.source.file).use {
                            it.removeFile(itemToDelete.source.uniquePath)
                        }
                    } else {
                        itemToDelete.status = TaskContentStatus.SKIP
                        return@forEachIndexed
                    }
                    itemToDelete.status = TaskContentStatus.SUCCESS
                } catch (e: Exception) {
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

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.status = TaskStatus.SUCCESS
            progressMonitor.summary = buildString {
                pendingContent.forEach { content ->
                    append(content.source.displayName)
                    append(" -> ")
                    append(content.status.name)
                }
            }
        }
    }

    override suspend fun continueTask() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

    internal data class DeleteContentItem(
        val source: ContentHolder,
        var status: TaskContentStatus
    )
}