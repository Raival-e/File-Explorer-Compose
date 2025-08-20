package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.reandroid.archive.ZipAlign
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class DeleteTask(
    val sourceContent: List<ContentHolder>
) : Task() {
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
            canMoveToBackground = true
        )
    }

    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status

    override suspend fun validate() = sourceContent.find { !it.isValid() } == null

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
            progress = 0f
        }
    }

    private fun markAsAborted() {
        progressMonitor.apply {
            status = TaskStatus.PAUSED
            summary = globalClass.getString(R.string.task_aborted)
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
        parameters = params as DeleteTaskParameters
        progressMonitor.status = TaskStatus.RUNNING
        protect = false

        // Check abortion early
        if (aborted) {
            markAsAborted()
            return
        }

        if (sourceContent.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return
        }

        progressMonitor.apply {
            processName = globalClass.resources.getString(R.string.preparing)
            progress = 0.05f
        }

        if (pendingContent.isEmpty()) {
            sourceContent.forEach { content ->
                pendingContent.add(
                    DeleteContentItem(source = content, status = TaskContentStatus.PENDING)
                )
            }
        }

        progressMonitor.apply {
            totalContent = pendingContent.size
            processName = globalClass.getString(R.string.deleting)
            progress = 0.1f
        }

        // Check source type only once using the first item
        val sampleContent = sourceContent.first()
        when (sampleContent) {
            is LocalFileHolder -> handleLocalFileDeletion()
            is ZipFileHolder -> handleZipFileDeletion()
            else -> {
                markAsFailed(globalClass.getString(R.string.unsupported_source_type))
                return
            }
        }

        // Handle APK alignment if needed
        if (progressMonitor.status == TaskStatus.RUNNING && sampleContent is ZipFileHolder) {
            if (sampleContent.zipTree.source.extension == apkFileType) {
                // Check abortion before alignment
                if (aborted) {
                    markAsAborted()
                    return
                }

                progressMonitor.apply {
                    processName = globalClass.resources.getString(R.string.aligning_apk)
                    progress = 0.95f
                    contentName = emptyString
                }

                try {
                    ZipAlign.alignApk(sampleContent.zipTree.source.file)
                } catch (e: Exception) {
                    // Don't fail the entire task for alignment issues
                    logger.logError(e)
                }
            }
        }

        if (progressMonitor.status == TaskStatus.RUNNING) {
            val successCount = pendingContent.count { it.status == TaskContentStatus.SUCCESS }
            val skipCount = pendingContent.count { it.status == TaskContentStatus.SKIP }

            progressMonitor.apply {
                status = TaskStatus.SUCCESS
                progress = 1.0f
                processName = globalClass.getString(R.string.completed)
                summary = globalClass.getString(
                    R.string.task_completed
                )
            }
        }
    }

    private suspend fun handleLocalFileDeletion() {
        pendingContent.forEachIndexed { index, itemToDelete ->
            if (aborted) {
                markAsAborted()
                return
            }

            if (itemToDelete.status == TaskContentStatus.PENDING) {
                val progressPercent = 0.1f + (0.8f * (index.toFloat() / pendingContent.size))

                progressMonitor.apply {
                    contentName = itemToDelete.source.displayName
                    remainingContent = pendingContent.size - (index + 1)
                    progress = progressPercent
                }

                try {
                    val localFile = itemToDelete.source as LocalFileHolder
                    if (localFile.file.deleteRecursively()) {
                        itemToDelete.status = TaskContentStatus.SUCCESS
                    } else {
                        throw Exception(globalClass.getString(R.string.failed_to_delete_file))
                    }
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

    private suspend fun handleZipFileDeletion() {
        val zipFileHolder = sourceContent.first() as ZipFileHolder
        val sourceZipFile = zipFileHolder.zipTree.source.file
        var tempFile: File? = null

        try {
            // Check abortion before processing
            if (aborted) {
                markAsAborted()
                return
            }

            progressMonitor.apply {
                processName = globalClass.getString(R.string.updating)
                progress = 0.15f
            }

            // Create temp file for safe operations
            tempFile = File(sourceZipFile.parent, "${sourceZipFile.nameWithoutExtension}_temp.zip")

            // Create a set of paths to delete
            val pathsToDelete = mutableSetOf<String>()
            val foldersToDelete = mutableSetOf<String>()

            pendingContent.filter { it.status == TaskContentStatus.PENDING }.forEach { item ->
                val zipHolder = item.source as ZipFileHolder
                val pathToDelete = zipHolder.uniquePath
                pathsToDelete.add(pathToDelete)

                if (zipHolder.isFolder) {
                    foldersToDelete.add(pathToDelete)
                }
            }

            progressMonitor.apply {
                progress = 0.2f
            }

            // Process the zip file
            ZipFile(sourceZipFile).use { sourceZip ->
                ZipOutputStream(FileOutputStream(tempFile)).use { zipOut ->
                    val entries = sourceZip.entries().toList()
                    var processedCount = 0

                    entries.forEach { entry ->
                        if (aborted) {
                            markAsAborted()
                            return
                        }

                        val entryPath = entry.name.removeSuffix("/")
                        var shouldDelete = false

                        // Check if this entry should be deleted
                        if (pathsToDelete.contains(entryPath)) {
                            shouldDelete = true
                        } else {
                            // Check if this entry is inside a folder being deleted
                            for (folderPath in foldersToDelete) {
                                if (entryPath.startsWith("$folderPath/")) {
                                    shouldDelete = true
                                    break
                                }
                            }
                        }

                        if (!shouldDelete) {
                            // Copy the entry to the new zip
                            val newEntry = ZipEntry(entry.name).apply {
                                time = entry.time
                                if (!entry.isDirectory) {
                                    method = entry.method
                                    if (entry.method == ZipEntry.STORED) {
                                        size = entry.size
                                        crc = entry.crc
                                    }
                                }
                            }

                            zipOut.putNextEntry(newEntry)

                            if (!entry.isDirectory) {
                                sourceZip.getInputStream(entry).use { inputStream ->
                                    inputStream.copyTo(zipOut)
                                }
                            }

                            zipOut.closeEntry()
                        } else {
                            // Mark corresponding pending items as successful
                            pendingContent.find {
                                (it.source as ZipFileHolder).uniquePath == entryPath
                            }?.status = TaskContentStatus.SUCCESS
                        }

                        processedCount++
                        val progressPercent =
                            0.2f + (0.7f * (processedCount.toFloat() / entries.size))

                        progressMonitor.apply {
                            contentName = entry.name
                            progress = progressPercent
                        }
                    }
                }
            }

            // Check abortion before finalizing
            if (aborted) {
                markAsAborted()
                return
            }

            progressMonitor.apply {
                progress = 0.9f
                processName = globalClass.getString(R.string.finalizing)
            }

            // Replace the original file with the modified one
            if (!sourceZipFile.delete()) {
                throw Exception(globalClass.getString(R.string.failed_to_delete_original_zip_file))
            }

            if (!tempFile.renameTo(sourceZipFile)) {
                throw Exception(globalClass.getString(R.string.failed_to_replace_original_zip_file))
            }

            tempFile = null // Successfully renamed, don't delete in finally

            // Mark any remaining pending items as successful (for virtual folders)
            pendingContent.filter { it.status == TaskContentStatus.PENDING }.forEach { item ->
                val zipHolder = item.source as ZipFileHolder
                if (zipHolder.isFolder) {
                    item.status = TaskContentStatus.SUCCESS
                }
            }

        } catch (e: Exception) {
            logger.logError(e)
            markAsFailed(
                globalClass.resources.getString(
                    R.string.task_summary_failed,
                    e.message ?: emptyString
                )
            )
        } finally {
            // Clean up temp file if it still exists
            tempFile?.delete()
        }
    }

    override suspend fun continueTask() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

    override fun setParameters(params: TaskParameters) {
        parameters = params as DeleteTaskParameters
    }

    internal data class DeleteContentItem(
        val source: ContentHolder,
        var status: TaskContentStatus
    )
}