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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class RenameTask(val sourceContent: List<ContentHolder>) : Task() {
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
        parameters = params as RenameTaskParameters
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

        // Validate the regular expression
        try {
            if (parameters!!.toFind.isNotEmpty() && parameters!!.useRegex) {
                parameters!!.toFind.toRegex()
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

        progressMonitor.apply {
            processName = globalClass.resources.getString(R.string.preparing)
            progress = 0.05f
        }

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
            progress = 0.1f
        }

        // Check source type only once using the first item
        val sampleContent = sourceContent.first()
        when (sampleContent) {
            is ZipFileHolder -> handleZipFileRenaming()
            is LocalFileHolder -> handleLocalFileRenaming()
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
                    logger.logError(e)
                    // Don't fail the entire task for alignment issues
                    logger.logWarning("APK alignment failed: ${e.message}")
                }
            }
        }

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.apply {
                status = TaskStatus.SUCCESS
                progress = 1.0f
                processName = globalClass.getString(R.string.completed)
                summary = globalClass.getString(R.string.task_completed)
            }
        }
    }

    private suspend fun handleLocalFileRenaming() {
        pendingContent.forEachIndexed { index, itemToRename ->
            if (aborted) {
                markAsAborted()
                return
            }

            if (itemToRename.status == TaskContentStatus.PENDING) {
                val progressPercent = 0.1f + (0.8f * (index.toFloat() / pendingContent.size))

                progressMonitor.apply {
                    contentName = itemToRename.source.displayName
                    remainingContent = pendingContent.size - (index + 1)
                    progress = progressPercent
                }

                try {
                    val localFile = itemToRename.source as LocalFileHolder
                    val newFile = File(itemToRename.newPath)

                    if (localFile.file.renameTo(newFile)) {
                        itemToRename.status = TaskContentStatus.SUCCESS
                    } else {
                        throw Exception(globalClass.getString(R.string.failed_to_rename_file))
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

    private suspend fun handleZipFileRenaming() {
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
                progress = 0.15f
            }

            // Create temp file for safe operations
            tempFile = File(sourceZipFile.parent, "${sourceZipFile.nameWithoutExtension}_temp.zip")

            // Create a map of old paths to new paths for all items to rename
            val renameMap = mutableMapOf<String, String>()
            val foldersToRename = mutableSetOf<String>()

            pendingContent.filter { it.status == TaskContentStatus.PENDING }.forEach { item ->
                val zipHolder = item.source as ZipFileHolder
                val oldPath = zipHolder.uniquePath
                val newPath = item.newPath

                if (zipHolder.isFolder) {
                    foldersToRename.add(oldPath)
                }

                renameMap[oldPath] = newPath
            }

            progressMonitor.apply {
                progress = 0.2f
            }

            // Open the source zip file for reading
            ZipFile(sourceZipFile).use { sourceZip ->
                // Create a new zip file
                ZipOutputStream(FileOutputStream(tempFile)).use { zipOut ->
                    val entries = sourceZip.entries().toList()
                    var processedCount = 0

                    entries.forEach { entry ->
                        if (aborted) {
                            markAsAborted()
                            return
                        }

                        val entryName = entry.name.removeSuffix("/")
                        var newEntryName = entryName
                        var wasRenamed = false

                        // Check direct rename
                        if (renameMap.containsKey(entryName)) {
                            newEntryName = renameMap[entryName]!!
                            wasRenamed = true
                        } else {
                            // Check if this entry is inside a folder being renamed
                            for (folderPath in foldersToRename) {
                                if (entryName.startsWith("$folderPath/")) {
                                    val relativePath = entryName.substring(folderPath.length + 1)
                                    newEntryName = "${renameMap[folderPath]}/$relativePath"
                                    wasRenamed = true
                                    break
                                }
                            }
                        }

                        // Restore trailing slash for directories
                        if (entry.isDirectory) {
                            newEntryName += "/"
                        }

                        // Create new entry with the new name
                        val newEntry = ZipEntry(newEntryName).apply {
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

                        // Copy entry data if it's not a directory
                        if (!entry.isDirectory) {
                            sourceZip.getInputStream(entry).use { inputStream ->
                                inputStream.copyTo(zipOut)
                            }
                        }

                        zipOut.closeEntry()

                        // Mark corresponding pending items as successful
                        if (wasRenamed) {
                            pendingContent.find {
                                (it.source as ZipFileHolder).uniquePath == entryName
                            }?.status = TaskContentStatus.SUCCESS
                        }

                        processedCount++
                        val progressPercent =
                            0.2f + (0.7f * (processedCount.toFloat() / entries.size))

                        progressMonitor.apply {
                            contentName = if (wasRenamed) newEntryName else entry.name
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

            // Replace the original file with the new one
            if (!sourceZipFile.delete()) {
                throw Exception(globalClass.getString(R.string.failed_to_delete_original_zip_file))
            }

            if (!tempFile.renameTo(sourceZipFile)) {
                throw Exception(globalClass.getString(R.string.failed_to_replace_original_zip_file))
            }

            tempFile = null // Successfully renamed, don't delete in finally

            // Mark any remaining pending items as successful (for folders without zip entries)
            pendingContent.filter { it.status == TaskContentStatus.PENDING }.forEach { item ->
                val zipHolder = item.source as ZipFileHolder
                if (zipHolder.isFolder) {
                    // Check if this folder has any children that were successfully renamed
                    val hasRenamedChildren = pendingContent.any { other ->
                        other.status == TaskContentStatus.SUCCESS &&
                                (other.source as ZipFileHolder).uniquePath.startsWith("${zipHolder.uniquePath}/")
                    }

                    if (hasRenamedChildren) {
                        item.status = TaskContentStatus.SUCCESS
                    }
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
        parameters = params as RenameTaskParameters
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