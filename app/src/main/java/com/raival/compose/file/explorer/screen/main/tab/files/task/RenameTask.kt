package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import net.lingala.zip4j.ZipFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RenameTask(val sourceContent: List<ContentHolder>) : Task() {
    private var aborted = false
    private var parameters: RenameTaskParameters? = null
    private var pendingContent = arrayListOf<RenameContentItem>()

    override val metadata = System.currentTimeMillis().toFormattedDate().let { time ->
        TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(R.string.rename),
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
        parameters = params as RenameTaskParameters
        progressMonitor.status = TaskStatus.RUNNING

        if (sourceContent.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return
        }

        // Validate the regular expression
        try {
            if (parameters!!.toFind.isNotEmpty() && parameters!!.useRegex) parameters!!.toFind.toRegex()
        } catch (e: Exception) {
            markAsFailed(
                globalClass.resources.getString(
                    R.string.task_summary_failed,
                    e.message ?: emptyString
                )
            )
            return
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.preparing)

        if (pendingContent.isEmpty()) {
            sourceContent.forEachIndexed { index, content ->
                pendingContent.add(
                    RenameContentItem(
                        source = content,
                        newPath = getNewPath(
                            content = content,
                            newName = parameters!!.newName,
                            index = index,
                        ),
                        status = TaskContentStatus.PENDING
                    )
                )
            }
        }

        progressMonitor.apply {
            totalContent = pendingContent.size
            processName = globalClass.getString(R.string.renaming)
        }

        pendingContent.forEachIndexed { index, itemToRename ->
            if (aborted) {
                progressMonitor.status = TaskStatus.CANCELLED
                return
            }

            if (itemToRename.status == TaskContentStatus.PENDING) {
                progressMonitor.apply {
                    contentName = itemToRename.source.displayName
                    remainingContent = pendingContent.size - index + 1
                    progress = (index + 1f) / pendingContent.size
                }

                try {
                    if (itemToRename.source is LocalFileHolder) {
                        itemToRename.source.file.renameTo(File(itemToRename.newPath))
                    } else if (itemToRename.source is ZipFileHolder) {
                        ZipFile(itemToRename.source.zipTree.source.file).renameFile(
                            itemToRename.source.uniquePath,
                            itemToRename.newPath
                        )
                    }
                    itemToRename.status = TaskContentStatus.SUCCESS
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

    private fun getNewPath(
        content: ContentHolder,
        newName: String,
        index: Int,
    ): String {
        val file = File(content.uniquePath)

        // 1. Deconstruct the original path and file name
        val parentPath = file.parent?.let { "$it/" } ?: emptyString
        val originalFileName = file.name

        if (sourceContent.size == 1) return parentPath + newName + if (content.uniquePath.endsWith(
                File.separator
            )
        ) File.separator else emptyString

        val newFileName = originalFileName.transformFileName(
            newName = newName,
            index = index,
            textToFind = parameters!!.toFind,
            replaceText = parameters!!.toReplace,
            useRegex = parameters!!.useRegex,
            onLastModified = { content.lastModified }
        )

        // 2. Reconstruct the full path
        // Also, preserve the trailing slash if the original path was a directory
        return parentPath + newFileName + if (content.uniquePath.endsWith(File.separatorChar) && !newFileName.endsWith(
                File.separator
            )
        ) File.separator else emptyString
    }

    companion object {
        /**
         * Pattern Syntax:
         * - {p} -> file name without extension (e.g., "document" from "document.txt")
         * - {s} -> file extension (e.g., "txt" from "document.txt")
         * - {e} -> file extension with a dot (e.g., ".txt" from "document.txt")
         * - {t} -> file's last modified time (formatted as "yyyyMMdd_HHmmss")
         * - {n} -> number increment (e.g., {0} -> 0, 1, 2...; {1} -> 1, 2, 3...)
         * - {zn} -> zero-padded number increment (e.g., {z0} -> 0, 1...; {zz0} -> 00, 01...; {zzz1} -> 001, 002...)
         */
        fun String.transformFileName(
            newName: String,
            index: Int,
            textToFind: String,
            replaceText: String,
            useRegex: Boolean,
            onLastModified: () -> Long
        ): String {
            val nameWithoutExtension = substringBeforeLast('.', missingDelimiterValue = this)
            val extension = substringAfterLast('.', missingDelimiterValue = emptyString)
            val lastModifiedTime by lazy {
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                sdf.format(Date(onLastModified()))
            }
            val placeholderRegex = Regex("""\{([^{}]+)\}""")
            val zeroPaddedRegex = Regex("""^(z+)(\d+)$""")
            var newFileName = newName.replace(placeholderRegex) { matchResult ->
                val token = matchResult.groupValues[1]
                val zMatch = zeroPaddedRegex.matchEntire(token)
                when {
                    token == "p" -> nameWithoutExtension
                    token == "s" -> extension
                    token == "e" -> if (extension.isEmpty()) emptyString else ".${extension}"
                    token == "t" -> lastModifiedTime
                    zMatch != null -> {
                        val zPart = zMatch.groupValues[1]
                        val numberPart = zMatch.groupValues[2]
                        val paddingWidth = zPart.length
                        val startNumber = numberPart.toInt()
                        (startNumber + index).toString().padStart(paddingWidth, '0')
                    }

                    token.toIntOrNull() != null -> {
                        val startNumber = token.toInt()
                        (startNumber + index).toString()
                    }

                    else -> matchResult.value
                }
            }

            if (textToFind.isNotEmpty()) {
                newFileName = when (useRegex) {
                    true -> newFileName.replace(textToFind.toRegex(), replaceText)
                    false -> newFileName.replace(textToFind, replaceText)
                }
            }

            return newFileName
        }
    }

    internal data class RenameContentItem(
        val source: ContentHolder,
        val newPath: String,
        var status: TaskContentStatus
    )
}