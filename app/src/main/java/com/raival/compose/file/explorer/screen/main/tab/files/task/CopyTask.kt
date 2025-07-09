package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.listFilesAndEmptyDirs
import com.raival.compose.file.explorer.common.extension.orIf
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toRelativeString
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CopyTask(
    val sourceFiles: List<ContentHolder>,
    val deleteSourceFiles: Boolean
) : Task() {
    private var aborted = false
    private var parameters: CopyTaskParameters? = null
    private var pendingFiles: ArrayList<TaskContentItem> = arrayListOf()

    override val metadata = System.currentTimeMillis().toFormattedDate().let { time ->
        TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(
                if (deleteSourceFiles) R.string.move else R.string.copy
            ),
            subtitle = globalClass.resources.getString(R.string.task_subtitle, sourceFiles.size),
            displayDetails = sourceFiles.joinToString(", ") { it.displayName },
            fullDetails = buildString {
                sourceFiles.forEachIndexed { index, source ->
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

    override fun validate() = sourceFiles.find { !it.isValid() || !it.canRead } == null

    override fun abortTask() {
        aborted = true
    }

    override suspend fun run(params: TaskParameters) {
        if (parameters == null) parameters = params as CopyTaskParameters
        progressMonitor.status = TaskStatus.RUNNING
        aborted = false

        if (sourceFiles.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return
        }

        // Determine the source type and target type
        sourceFiles.first().let { sample ->
            val sourcePath = sample.getParent()?.uniquePath ?: emptyString
            try {
                if (sample is LocalFileHolder) {
                    (parameters!!.destHolder as? ZipFileHolder)?.let {
                        copyLocalFilesToZip(
                            sourcePath,
                            it
                        )
                    }
                    (parameters!!.destHolder as? LocalFileHolder)?.let {
                        copyLocalFiles(
                            sourcePath,
                            it
                        )
                    }
                } else if (sample is ZipFileHolder) {
                    (parameters!!.destHolder as? LocalFileHolder)?.let {
                        copyZipFilesToLocal(
                            sourcePath,
                            it
                        )
                    }
                    (parameters!!.destHolder as? ZipFileHolder)?.let {
                        copyZipFilesToZip(
                            sourcePath,
                            it
                        )
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
                return
            }
        }

        if (deleteSourceFiles) performSourceDeletion()

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.status = TaskStatus.SUCCESS
            progressMonitor.summary = buildString {
                pendingFiles.forEach { content ->
                    append(content.content.displayName)
                    append(" -> ")
                    append(content.status.name)
                }
            }
        }
    }

    override suspend fun continueTask() {
        if (parameters == null) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_missing_destination))
            return
        }

        run(parameters!!)
    }

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
        }
    }

    private fun performSourceDeletion() {
        if (!deleteSourceFiles) return

        sourceFiles.first().let { sample ->
            progressMonitor.processName =
                globalClass.resources.getString(R.string.deleting_source_files)
            if (sample is LocalFileHolder) {
                pendingFiles.forEachIndexed { index, it ->
                    if (aborted) {
                        progressMonitor.status = TaskStatus.CANCELLED
                        return
                    }

                    if (it.status == TaskContentStatus.SUCCESS) {
                        progressMonitor.apply {
                            remainingContent = sourceFiles.size - index + 1
                            progress = (index + 1f) / sourceFiles.size
                        }
                        (it.content as LocalFileHolder).file.deleteRecursively()
                    }
                }
                sourceFiles.forEach { content ->
                    if ((content as LocalFileHolder).file.walkTopDown().count { it.isFile } == 0) {
                        content.file.deleteRecursively()
                    }
                }
            } else if (sample is ZipFileHolder) {
                ZipFile(sample.zipTree.source.file).use { zipFile ->
                    zipFile.removeFiles(pendingFiles.filter { it.status == TaskContentStatus.SUCCESS }
                        .map { (it.content as ZipFileHolder).node.path })
                    sourceFiles.forEach { src ->
                        (src as ZipFileHolder).let { zipSrc ->
                            if (zipSrc.node.listFilesAndEmptyDirs()
                                    .count { !it.isDirectory } == 0
                            ) {
                                zipFile.removeFile(zipSrc.node.path)
                            }
                        }
                    }
                }
            }
        }
    }

    /** `sourcePath` is the path of the folder from which the files are copied **/
    private fun copyLocalFiles(sourcePath: String, destinationHolder: LocalFileHolder) {
        // prevent copying a directory into itself
        if ((sourceFiles.first() as LocalFileHolder).file.parentFile?.canonicalPath == destinationHolder.file.canonicalPath) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_invalid_dest))
            return
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)

        if (pendingFiles.isEmpty()) {
            sourceFiles.forEach {
                pendingFiles.addAll(listFilesWithRelativePath(sourcePath, it))
            }
        }

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        pendingFiles.forEachIndexed { index, itemToCopy ->
            if (aborted) {
                progressMonitor.status = TaskStatus.CANCELLED
                return
            }

            (itemToCopy.content as LocalFileHolder).let { fileToCopy ->
                val destinationFile = File(destinationHolder.file, itemToCopy.relativePath)

                if (itemToCopy.status == TaskContentStatus.PENDING) {

                    progressMonitor.apply {
                        contentName = fileToCopy.displayName
                        remainingContent = pendingFiles.size - index + 1
                        progress = (index + 1f) / pendingFiles.size
                    }

                    if (destinationFile.exists()
                        && destinationFile.isFile
                        && (commonConflictResolution == TaskContentStatus.ASK || commonConflictResolution == TaskContentStatus.SKIP)
                    ) {
                        if (commonConflictResolution == TaskContentStatus.ASK) {
                            itemToCopy.status = TaskContentStatus.CONFLICT
                            progressMonitor.status = TaskStatus.CONFLICT
                            globalClass.taskManager.taskInterceptor.interceptTask(
                                taskContentItem = itemToCopy,
                                task = this
                            )
                        } else {
                            itemToCopy.status = TaskContentStatus.SKIP
                        }
                    } else {
                        if (fileToCopy.isFile()) {
                            try {
                                if (deleteSourceFiles) {
                                    fileToCopy.file.copyTo(
                                        destinationFile,
                                        overwrite = commonConflictResolution == TaskContentStatus.REPLACE
                                    )
                                } else {
                                    fileToCopy.file.copyTo(
                                        destinationFile,
                                        overwrite = commonConflictResolution == TaskContentStatus.REPLACE
                                    )
                                }
                                itemToCopy.status = TaskContentStatus.SUCCESS
                            } catch (_: Exception) {
                                itemToCopy.status = TaskContentStatus.FAILED
                            }
                        } else if (!destinationFile.mkdirs() && !destinationFile.exists()) {
                            itemToCopy.status = TaskContentStatus.FAILED
                        }
                        itemToCopy.status = TaskContentStatus.SUCCESS
                    }
                } else if (itemToCopy.status == TaskContentStatus.REPLACE) {
                    // targetFile should always be a file at this point
                    try {
                        if (deleteSourceFiles) {
                            Files.move(
                                fileToCopy.file.toPath(),
                                destinationFile.toPath(),
                                StandardCopyOption.ATOMIC_MOVE,
                                StandardCopyOption.REPLACE_EXISTING
                            )
                        } else {
                            fileToCopy.file.copyTo(
                                destinationFile,
                                overwrite = true
                            )
                        }
                        itemToCopy.status = TaskContentStatus.SUCCESS
                    } catch (_: Exception) {
                        itemToCopy.status = TaskContentStatus.FAILED
                    }
                } else if (itemToCopy.status == TaskContentStatus.SKIP) {
                    itemToCopy.status = TaskContentStatus.SKIP
                }
            }
        }
    }

    private fun addLocalFileToZip(
        zipFile: ZipFile,
        fileToCopy: TaskContentItem,
        targetPath: String,
        override: Boolean
    ) {
        try {
            if (fileToCopy.content.isFile()) {
                zipFile.addFile(
                    (fileToCopy.content as LocalFileHolder).file,
                    ZipParameters().apply {
                        isOverrideExistingFilesInZip = override
                        this.fileNameInZip = targetPath
                    }
                )
            } else {
                zipFile.addFolder(
                    (fileToCopy.content as LocalFileHolder).file,
                    ZipParameters().apply {
                        isOverrideExistingFilesInZip = override
                        this.fileNameInZip = targetPath
                    }
                )
            }
            fileToCopy.status = TaskContentStatus.SUCCESS
        } catch (_: Exception) {
            fileToCopy.status = TaskContentStatus.FAILED
        }
    }

    /** `sourcePath` is the path of the folder from which the files are copied **/
    private fun copyLocalFilesToZip(sourcePath: String, destinationHolder: ZipFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)

        if (pendingFiles.isEmpty()) {
            sourceFiles.forEach {
                pendingFiles.addAll(listFilesWithRelativePath(sourcePath, it))
            }
        }

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        ZipFile(destinationHolder.zipTree.source.file).use { targetZipFile ->
            pendingFiles.forEachIndexed { index, fileToCopy ->
                if (aborted) {
                    progressMonitor.status = TaskStatus.CANCELLED
                    return
                }

                val targetEntryPath =
                    "${destinationHolder.node.path}${if (destinationHolder.node.path.isEmpty()) emptyString else File.separator}${fileToCopy.relativePath}"
                if (fileToCopy.status == TaskContentStatus.PENDING) {

                    progressMonitor.apply {
                        contentName = fileToCopy.content.displayName
                        remainingContent = pendingFiles.size - index + 1
                        progress = (index + 1f) / pendingFiles.size
                    }

                    val existingFile = targetZipFile.getFileHeader(targetEntryPath)
                    if (existingFile != null) {
                        if (existingFile.isDirectory || commonConflictResolution == TaskContentStatus.SKIP) { // Don't notify for existing folders
                            fileToCopy.status = TaskContentStatus.SKIP
                            return@forEachIndexed
                        }
                        if (commonConflictResolution == TaskContentStatus.ASK) {
                            fileToCopy.status = TaskContentStatus.CONFLICT
                            progressMonitor.status = TaskStatus.CONFLICT
                            globalClass.taskManager.taskInterceptor.interceptTask(
                                taskContentItem = fileToCopy,
                                task = this
                            )
                            return@use
                        }
                    }
                    // No conflicts were detected, so continue with the copy (with common resolution in mind)
                    if (commonConflictResolution == TaskContentStatus.REPLACE) {
                        addLocalFileToZip(targetZipFile, fileToCopy, targetEntryPath, true)
                    } else {
                        addLocalFileToZip(targetZipFile, fileToCopy, targetEntryPath, false)
                    }
                } else if (fileToCopy.status == TaskContentStatus.REPLACE) {
                    // Only files (not folders) can be replaced, already existing folders are skipped
                    // in the previous check.
                    if (fileToCopy.content.isFile()) {
                        addLocalFileToZip(targetZipFile, fileToCopy, targetEntryPath, true)
                    }
                } else if (fileToCopy.status == TaskContentStatus.SKIP) {
                    // Return isn't necessary for now, but in case something is added later
                    return@forEachIndexed
                }
            }
        }
    }

    /** Extract files from a zip archive to the local filesystem. `sourcePath` is the zip entry path being copied from. */
    private fun copyZipFilesToLocal(sourcePath: String, destinationHolder: LocalFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)

        if (pendingFiles.isEmpty()) {
            sourceFiles.forEach {
                pendingFiles.addAll(listFilesWithRelativePath(sourcePath, it))
            }
        }

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.extracting)
        }

        val sourceZipArchive = (sourceFiles.first() as ZipFileHolder).zipTree.source.file
        ZipFile(sourceZipArchive).use { sourceZip ->
            pendingFiles.forEachIndexed { index, itemToExtract ->
                if (aborted) {
                    progressMonitor.status = TaskStatus.CANCELLED
                    return@use
                }

                val sourceHolder = itemToExtract.content as ZipFileHolder
                val destinationFile = File(destinationHolder.uniquePath, itemToExtract.relativePath)

                if (itemToExtract.status == TaskContentStatus.PENDING) {
                    progressMonitor.apply {
                        contentName = sourceHolder.displayName
                        remainingContent = pendingFiles.size - index + 1
                        progress = (index + 1f) / pendingFiles.size
                    }

                    if (destinationFile.exists() && destinationFile.isFile) {
                        if (commonConflictResolution == TaskContentStatus.SKIP) {
                            itemToExtract.status = TaskContentStatus.SKIP
                        } else if (commonConflictResolution == TaskContentStatus.ASK) {
                            itemToExtract.status = TaskContentStatus.CONFLICT
                            progressMonitor.status = TaskStatus.CONFLICT
                            globalClass.taskManager.taskInterceptor.interceptTask(
                                taskContentItem = itemToExtract,
                                task = this
                            )
                            return@use // Pause the task
                        } else if (commonConflictResolution == TaskContentStatus.REPLACE) {
                            itemToExtract.status = TaskContentStatus.REPLACE
                        }
                    }
                }
                when (itemToExtract.status) {
                    TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                        try {
                            if (sourceHolder.isFile()) {
                                destinationFile.parentFile?.mkdirs()
                                sourceZip.getInputStream(sourceZip.getFileHeader(sourceHolder.node.path))
                                    .use { input ->
                                        destinationFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                            } else { // It's a directory
                                destinationFile.mkdirs()
                            }
                            itemToExtract.status = TaskContentStatus.SUCCESS
                        } catch (_: Exception) {
                            itemToExtract.status = TaskContentStatus.FAILED
                        }
                    }

                    else -> { /* do nothing */
                    }
                }
            }
        }
    }

    /** Copy files from one zip archive to another. */
    private fun copyZipFilesToZip(sourcePath: String, destinationHolder: ZipFileHolder) {
        val sourceFile = (sourceFiles.first() as ZipFileHolder).zipTree.source.file
        val destFile = destinationHolder.zipTree.source.file

        // Prevent copying a directory into itself inside a zip
        if (sourceFile.canonicalPath == destFile.canonicalPath) {
            val destNodePath = destinationHolder.node.path
            if (sourceFiles.any { !(it as ZipFileHolder).isFile() && destNodePath.startsWith(it.node.path) }) {
                markAsFailed(globalClass.resources.getString(R.string.task_summary_invalid_dest))
                return
            }
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)
        if (pendingFiles.isEmpty()) {
            sourceFiles.forEach {
                pendingFiles.addAll(listFilesWithRelativePath(sourcePath, it))
            }
        }

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        ZipFile(sourceFile).use { sourceZip ->
            ZipFile(destFile).use { destZip ->
                pendingFiles.forEachIndexed { index, itemToCopy ->
                    if (aborted) {
                        progressMonitor.status = TaskStatus.CANCELLED
                        return@use
                    }

                    val sourceHolder = itemToCopy.content as ZipFileHolder
                    val targetEntryPath =
                        "${destinationHolder.node.path}${if (destinationHolder.node.path.isEmpty()) emptyString else File.separator}${itemToCopy.relativePath}"

                    if (itemToCopy.status == TaskContentStatus.PENDING) {
                        progressMonitor.apply {
                            contentName = itemToCopy.content.displayName
                            remainingContent = pendingFiles.size - index + 1
                            progress = (index + 1f) / pendingFiles.size
                        }

                        val existingHeader = destZip.getFileHeader(targetEntryPath)
                        if (existingHeader != null) {
                            if (existingHeader.isDirectory || commonConflictResolution == TaskContentStatus.SKIP) {
                                itemToCopy.status = TaskContentStatus.SKIP
                            } else if (commonConflictResolution == TaskContentStatus.ASK) {
                                itemToCopy.status = TaskContentStatus.CONFLICT
                                progressMonitor.status = TaskStatus.CONFLICT
                                globalClass.taskManager.taskInterceptor.interceptTask(
                                    taskContentItem = itemToCopy,
                                    task = this
                                )
                                return@use // Pause task
                            } else if (commonConflictResolution == TaskContentStatus.REPLACE) {
                                itemToCopy.status = TaskContentStatus.REPLACE
                            }
                        }
                    }

                    // If user chose 'Replace' for a conflict, we must first remove the old file.
                    if (itemToCopy.status == TaskContentStatus.REPLACE) {
                        try {
                            destZip.removeFile(targetEntryPath)
                        } catch (_: Exception) {
                            // Ignore if removal fails (e.g., file didn't exist)
                        }
                    }

                    when (itemToCopy.status) {
                        TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                            try {
                                val params = ZipParameters().apply {
                                    fileNameInZip = targetEntryPath
                                    isOverrideExistingFilesInZip = true
                                }

                                if (sourceHolder.isFile()) {
                                    sourceZip.getInputStream(sourceZip.getFileHeader(sourceHolder.node.path))
                                        .use { input ->
                                            destZip.addStream(input, params)
                                        }
                                } else { // It's a directory, add an entry with a trailing slash
                                    params.fileNameInZip =
                                        if (targetEntryPath.endsWith(File.separator)) targetEntryPath else "$targetEntryPath/"
                                    destZip.addStream(ByteArrayInputStream(ByteArray(0)), params)
                                }
                                itemToCopy.status = TaskContentStatus.SUCCESS
                            } catch (_: Exception) {
                                itemToCopy.status = TaskContentStatus.FAILED
                            }
                        }

                        else -> { /* Do nothing */
                        }
                    }
                }
            }
        }
    }

    private fun listFilesWithRelativePath(
        basePath: String,
        startFile: ContentHolder
    ): ArrayList<out TaskContentItem> {
        if (startFile.isFile()) {
            return arrayListOf(
                TaskContentItem(
                    startFile, startFile.displayName,
                    TaskContentStatus.PENDING
                )
            )
        }

        if (startFile is LocalFileHolder) {
            return startFile.file.listFilesAndEmptyDirs()
                .map { file ->
                    TaskContentItem(
                        content = LocalFileHolder(file),
                        relativePath = file
                            .toRelativeString(File(basePath))
                            .orIf(startFile.displayName) { it.isEmpty() },
                        status = TaskContentStatus.PENDING
                    )
                }.toCollection(arrayListOf())
        }

        if (startFile is ZipFileHolder) {
            return startFile.node.listFilesAndEmptyDirs()
                .map { node ->
                    TaskContentItem(
                        content = ZipFileHolder(startFile.zipTree, node),
                        relativePath = node.path
                            .toRelativeString(basePath)
                            .orIf(startFile.displayName) { it.isEmpty() },
                        status = TaskContentStatus.PENDING
                    )
                }.toCollection(arrayListOf())
        }

        return arrayListOf()
    }
}