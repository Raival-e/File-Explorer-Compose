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
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class CopyTask(
    val sourceFiles: List<ContentHolder>,
    val deleteSourceFiles: Boolean
) : Task() {
    private var parameters: CopyTaskParameters? = null
    private val pendingFiles: ArrayList<TaskContentItem> = arrayListOf()
    private var canPerformAtomicFileMove = true

    override val metadata = createTaskMetadata()
    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status
    override fun validate() = sourceFiles.all { it.isValid() && it.canRead }

    override suspend fun run() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

    override suspend fun run(params: TaskParameters) {
        parameters = params as? CopyTaskParameters

        if (!initializeTask()) return

        try {
            executeTaskBasedOnSourceType()
            if (deleteSourceFiles && progressMonitor.status == TaskStatus.RUNNING) {
                performSourceDeletion()
            }
            finalizeTask()
        } catch (e: Exception) {
            handleTaskError(e)
        }
    }

    override suspend fun continueTask() {
        parameters?.let { run(it) } ?: run {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_missing_destination))
        }
    }

    override fun setParameters(params: TaskParameters) {
        parameters = params as? CopyTaskParameters
    }

    // Private helper methods

    private fun createTaskMetadata(): TaskMetadata {
        val time = System.currentTimeMillis().toFormattedDate()
        return TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(
                if (deleteSourceFiles) R.string.move else R.string.copy
            ),
            subtitle = globalClass.resources.getString(R.string.task_subtitle, sourceFiles.size),
            displayDetails = sourceFiles.joinToString(", ") { it.displayName },
            fullDetails = buildString {
                sourceFiles.forEach { source ->
                    appendLine(source.displayName)
                }
                appendLine()
                append(time)
            },
            isCancellable = true,
            canMoveToBackground = true
        )
    }

    private fun initializeTask(): Boolean {
        progressMonitor.status = TaskStatus.RUNNING
        aborted = false
        protect = false

        if (sourceFiles.isEmpty()) {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_no_src))
            return false
        }

        parameters?.let { params ->
            if (!validateDestination(params.destHolder)) {
                return false
            }
        } ?: run {
            markAsFailed(globalClass.resources.getString(R.string.task_summary_missing_destination))
            return false
        }

        return true
    }

    private fun validateDestination(destHolder: ContentHolder): Boolean {
        val firstSource = sourceFiles.first()

        // Prevent copying into self for local files
        if (firstSource is LocalFileHolder && destHolder is LocalFileHolder) {
            val sourceParent = firstSource.file.parentFile?.canonicalPath
            val destPath = destHolder.file.canonicalPath
            if (sourceParent == destPath) {
                markAsFailed(globalClass.resources.getString(R.string.task_summary_invalid_dest))
                return false
            }
        }

        // Prevent copying zip directory into itself
        if (firstSource is ZipFileHolder && destHolder is ZipFileHolder) {
            val sourceFile = firstSource.zipTree.source.file.canonicalPath
            val destFile = destHolder.zipTree.source.file.canonicalPath

            if (sourceFile == destFile) {
                val destPath = destHolder.node.path
                val hasInvalidNesting = sourceFiles
                    .filterIsInstance<ZipFileHolder>()
                    .filter { !it.isFile() }
                    .any { destPath.startsWith(it.node.path) }

                if (hasInvalidNesting) {
                    markAsFailed(globalClass.resources.getString(R.string.task_summary_invalid_dest))
                    return false
                }
            }
        }

        return true
    }

    private fun executeTaskBasedOnSourceType() {
        val sample = sourceFiles.first()
        val destHolder = parameters!!.destHolder
        val sourcePath = sample.getParent()?.uniquePath ?: emptyString

        when {
            sample is LocalFileHolder && destHolder is LocalFileHolder ->
                copyLocalFiles(sourcePath, destHolder)

            sample is LocalFileHolder && destHolder is ZipFileHolder ->
                copyLocalFilesToZip(sourcePath, destHolder)

            sample is ZipFileHolder && destHolder is LocalFileHolder ->
                copyZipFilesToLocal(sourcePath, destHolder)

            sample is ZipFileHolder && destHolder is ZipFileHolder ->
                copyZipFilesToZip(sourcePath, destHolder)

            else ->
                throw IllegalStateException(globalClass.getString(R.string.unsupported_source_destination_combination))
        }
    }

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
        }
    }

    private fun handleTaskError(e: Exception) {
        logger.logError(e)
        markAsFailed(
            globalClass.resources.getString(
                R.string.task_summary_failed,
                e.message ?: globalClass.getString(R.string.unknown_error)
            )
        )
    }

    private fun finalizeTask() {
        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.status = TaskStatus.SUCCESS
            progressMonitor.summary = buildString {
                pendingFiles.forEach { content ->
                    append(content.content.displayName)
                    append(" -> ")
                    appendLine(content.status.name)
                }
            }
        }
    }

    private fun preparePendingFiles(sourcePath: String) {
        if (pendingFiles.isEmpty()) {
            sourceFiles.forEach { source ->
                pendingFiles.addAll(listFilesWithRelativePath(sourcePath, source))
            }
        }
    }

    private fun updateProgress(index: Int, itemName: String) {
        progressMonitor.apply {
            contentName = itemName
            remainingContent = pendingFiles.size - (index + 1)
            progress = (index + 1f) / pendingFiles.size
        }
    }

    private fun handleConflict(item: TaskContentItem): Boolean {
        return when (commonConflictResolution) {
            TaskContentStatus.SKIP -> {
                item.status = TaskContentStatus.SKIP
                true
            }

            TaskContentStatus.ASK -> {
                item.status = TaskContentStatus.CONFLICT
                progressMonitor.status = TaskStatus.CONFLICT
                globalClass.taskManager.taskInterceptor.interceptTask(item, this)
                false // Pause execution
            }

            TaskContentStatus.REPLACE -> {
                item.status = TaskContentStatus.REPLACE
                true
            }

            else -> true
        }
    }

    private fun performSourceDeletion() {
        if (!deleteSourceFiles) return

        progressMonitor.processName =
            globalClass.resources.getString(R.string.deleting_source_files)

        when (val sample = sourceFiles.first()) {
            is LocalFileHolder -> deleteLocalSources()
            is ZipFileHolder -> deleteZipSources(sample)
        }
    }

    private fun deleteLocalSources() {
        val successfulItems = pendingFiles.filter { it.status == TaskContentStatus.SUCCESS }

        successfulItems.forEachIndexed { index, item ->
            if (aborted) {
                progressMonitor.status = TaskStatus.PAUSED
                return
            }

            progressMonitor.apply {
                remainingContent = successfulItems.size - (index + 1)
                progress = (index + 1f) / successfulItems.size
            }

            (item.content as LocalFileHolder).file.deleteRecursively()
        }

        // Clean up empty directories
        sourceFiles.forEach { content ->
            val file = (content as LocalFileHolder).file
            if (file.exists() && file.walkTopDown().none { it.isFile }) {
                file.deleteRecursively()
            }
        }
    }

    private fun deleteZipSources(sample: ZipFileHolder) {
        try {
            ZipFile(sample.zipTree.source.file).use { zipFile ->
                val successfulPaths = pendingFiles
                    .filter { it.status == TaskContentStatus.SUCCESS }
                    .map { (it.content as ZipFileHolder).node.path }

                if (successfulPaths.isNotEmpty()) {
                    zipFile.removeFiles(successfulPaths)
                }

                // Remove empty directories
                sourceFiles.forEach { src ->
                    val zipSrc = src as ZipFileHolder
                    val hasFiles = zipSrc.node.listFilesAndEmptyDirs().any { !it.isDirectory }
                    if (!hasFiles) {
                        zipFile.removeFile(zipSrc.node.path)
                    }
                }
            }
        } catch (e: Exception) {
            logger.logError(e)
            // Don't fail the entire task if deletion fails
        }
    }

    // Specialized copy methods

    private fun copyLocalFiles(sourcePath: String, destinationHolder: LocalFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)
        preparePendingFiles(sourcePath)

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        pendingFiles.forEachIndexed { index, item ->
            if (aborted) {
                progressMonitor.status = TaskStatus.PAUSED
                return
            }

            if (item.status != TaskContentStatus.PENDING
                && item.status != TaskContentStatus.REPLACE
                && item.status != TaskContentStatus.CONFLICT
            ) {
                return@forEachIndexed
            }

            val sourceFile = (item.content as LocalFileHolder).file
            val destinationFile = File(destinationHolder.file, item.relativePath)

            updateProgress(index, sourceFile.name)

            if (item.status == TaskContentStatus.CONFLICT && !handleConflict(item)) {
                return
            }

            if (item.status == TaskContentStatus.PENDING) {
                val conflictExists = destinationFile.exists() && destinationFile.isFile
                if (conflictExists && !handleConflict(item)) {
                    return
                }
            }

            when (item.status) {
                TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                    item.status = if (copyLocalFile(
                            sourceFile,
                            destinationFile,
                            item.status == TaskContentStatus.REPLACE
                        )
                    ) {
                        TaskContentStatus.SUCCESS
                    } else {
                        TaskContentStatus.FAILED
                    }
                }

                else -> { /* Already handled */
                }
            }
        }
    }

    private fun copyLocalFile(source: File, destination: File, overwrite: Boolean): Boolean {
        return try {
            if (source.isFile) {
                destination.parentFile?.mkdirs()
                if (deleteSourceFiles) {
                    if (!canPerformAtomicFileMove) {
                        // Fallback to copy only - deletion will be handled by performSourceDeletion()
                        Files.copy(
                            source.toPath(),
                            destination.toPath(),
                            *buildList {
                                add(StandardCopyOption.COPY_ATTRIBUTES)
                                if (overwrite) add(StandardCopyOption.REPLACE_EXISTING)
                            }.toTypedArray()
                        )
                    } else {
                        try {
                            // Try atomic move first (faster when supported)
                            Files.move(
                                source.toPath(),
                                destination.toPath(),
                                *buildList {
                                    add(StandardCopyOption.ATOMIC_MOVE)
                                    if (overwrite) add(StandardCopyOption.REPLACE_EXISTING)
                                }.toTypedArray()
                            )
                        } catch (_: AtomicMoveNotSupportedException) {
                            // Fallback to copy only - deletion will be handled by performSourceDeletion()
                            Files.copy(
                                source.toPath(),
                                destination.toPath(),
                                *buildList {
                                    add(StandardCopyOption.COPY_ATTRIBUTES)
                                    if (overwrite) add(StandardCopyOption.REPLACE_EXISTING)
                                }.toTypedArray()
                            )
                            canPerformAtomicFileMove = false
                        }
                    }
                } else {
                    source.copyTo(destination, overwrite)
                }
            } else {
                destination.mkdirs() || destination.exists()
            }
            true
        } catch (e: Exception) {
            logger.logError(e)
            false
        }
    }

    private fun copyLocalFilesToZip(sourcePath: String, destinationHolder: ZipFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)
        preparePendingFiles(sourcePath)

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        try {
            ZipFile(destinationHolder.zipTree.source.file).use { targetZipFile ->
                pendingFiles.forEachIndexed { index, item ->
                    if (aborted) {
                        progressMonitor.status = TaskStatus.PAUSED
                        return
                    }

                    if (item.status != TaskContentStatus.PENDING
                        && item.status != TaskContentStatus.REPLACE
                        && item.status != TaskContentStatus.CONFLICT
                    ) {
                        return@forEachIndexed
                    }

                    updateProgress(index, item.content.displayName)

                    if (item.status == TaskContentStatus.CONFLICT && !handleConflict(item)) {
                        return
                    }

                    val targetPath =
                        createZipEntryPath(destinationHolder.node.path, item.relativePath)

                    if (item.status == TaskContentStatus.PENDING) {
                        val existingHeader = targetZipFile.getFileHeader(targetPath)
                        val conflictExists = existingHeader != null && !existingHeader.isDirectory
                        if (conflictExists && !handleConflict(item)) {
                            return
                        }
                    }

                    when (item.status) {
                        TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                            item.status = if (addLocalFileToZip(
                                    targetZipFile,
                                    item,
                                    targetPath,
                                    item.status == TaskContentStatus.REPLACE
                                )
                            ) {
                                TaskContentStatus.SUCCESS
                            } else {
                                TaskContentStatus.FAILED
                            }
                        }

                        else -> { /* Already handled */
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(globalClass.getString(R.string.failed_to_copy_files_to_zip), e)
        }
    }

    private fun addLocalFileToZip(
        zipFile: ZipFile,
        item: TaskContentItem,
        targetPath: String,
        overwrite: Boolean
    ): Boolean {
        return try {
            val sourceFile = (item.content as LocalFileHolder).file
            val params = ZipParameters().apply {
                isOverrideExistingFilesInZip = overwrite
                fileNameInZip = targetPath
            }

            if (sourceFile.isFile) {
                zipFile.addFile(sourceFile, params)
            } else {
                zipFile.addFolder(sourceFile, params)
            }
            true
        } catch (e: Exception) {
            logger.logError(e)
            false
        }
    }

    private fun copyZipFilesToLocal(sourcePath: String, destinationHolder: LocalFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)
        preparePendingFiles(sourcePath)

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.extracting)
        }

        val sourceZipFile = (sourceFiles.first() as ZipFileHolder).zipTree.source.file
        try {
            ZipFile(sourceZipFile).use { sourceZip ->
                pendingFiles.forEachIndexed { index, item ->
                    if (aborted) {
                        progressMonitor.status = TaskStatus.PAUSED
                        return
                    }

                    if (item.status != TaskContentStatus.PENDING
                        && item.status != TaskContentStatus.REPLACE
                        && item.status != TaskContentStatus.CONFLICT
                    ) {
                        return@forEachIndexed
                    }

                    val sourceHolder = item.content as ZipFileHolder
                    val destinationFile = File(destinationHolder.uniquePath, item.relativePath)

                    updateProgress(index, sourceHolder.displayName)

                    if (item.status == TaskContentStatus.CONFLICT && !handleConflict(item)) {
                        return
                    }

                    if (item.status == TaskContentStatus.PENDING) {
                        val conflictExists = destinationFile.exists() && destinationFile.isFile
                        if (conflictExists && !handleConflict(item)) {
                            return
                        }
                    }

                    when (item.status) {
                        TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                            item.status =
                                if (extractZipEntry(sourceZip, sourceHolder, destinationFile)) {
                                    TaskContentStatus.SUCCESS
                                } else {
                                    TaskContentStatus.FAILED
                                }
                        }

                        else -> { /* Already handled */
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(
                globalClass.getString(R.string.failed_to_extract_files_from_zip),
                e
            )
        }
    }

    private fun extractZipEntry(
        sourceZip: ZipFile,
        sourceHolder: ZipFileHolder,
        destinationFile: File
    ): Boolean {
        return try {
            if (sourceHolder.isFile()) {
                destinationFile.parentFile?.mkdirs()
                sourceZip.getInputStream(sourceZip.getFileHeader(sourceHolder.node.path))
                    .use { input ->
                        destinationFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
            } else {
                destinationFile.mkdirs()
            }
            true
        } catch (e: Exception) {
            logger.logError(e)
            false
        }
    }

    private fun copyZipFilesToZip(sourcePath: String, destinationHolder: ZipFileHolder) {
        progressMonitor.processName = globalClass.resources.getString(R.string.counting_files)
        preparePendingFiles(sourcePath)

        progressMonitor.apply {
            totalContent = pendingFiles.size
            processName = globalClass.resources.getString(R.string.copying)
        }

        val sourceFile = (sourceFiles.first() as ZipFileHolder).zipTree.source.file
        val destFile = destinationHolder.zipTree.source.file

        try {
            ZipFile(sourceFile).use { sourceZip ->
                ZipFile(destFile).use { destZip ->
                    pendingFiles.forEachIndexed { index, item ->
                        if (aborted) {
                            progressMonitor.status = TaskStatus.PAUSED
                            return
                        }

                        if (item.status != TaskContentStatus.PENDING
                            && item.status != TaskContentStatus.REPLACE
                            && item.status != TaskContentStatus.CONFLICT
                        ) {
                            return@forEachIndexed
                        }

                        updateProgress(index, item.content.displayName)

                        if (item.status == TaskContentStatus.CONFLICT && !handleConflict(item)
                        ) {
                            return
                        }

                        val targetPath =
                            createZipEntryPath(destinationHolder.node.path, item.relativePath)

                        if (item.status == TaskContentStatus.PENDING) {
                            val existingHeader = destZip.getFileHeader(targetPath)
                            val conflictExists =
                                existingHeader != null && !existingHeader.isDirectory
                            if (conflictExists && !handleConflict(item)) {
                                return
                            }
                        }

                        // Remove existing file if replacing
                        if (item.status == TaskContentStatus.REPLACE) {
                            try {
                                destZip.removeFile(targetPath)
                            } catch (e: Exception) {
                                logger.logError(e) // Log but continue
                            }
                        }

                        when (item.status) {
                            TaskContentStatus.PENDING, TaskContentStatus.REPLACE -> {
                                item.status =
                                    if (copyZipEntry(sourceZip, destZip, item, targetPath)) {
                                        TaskContentStatus.SUCCESS
                                    } else {
                                        TaskContentStatus.FAILED
                                    }
                            }

                            else -> { /* Already handled */
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(globalClass.getString(R.string.failed_to_copy_zip_entries), e)
        }
    }

    private fun copyZipEntry(
        sourceZip: ZipFile,
        destZip: ZipFile,
        item: TaskContentItem,
        targetPath: String
    ): Boolean {
        return try {
            val sourceHolder = item.content as ZipFileHolder
            val params = ZipParameters().apply {
                fileNameInZip = if (sourceHolder.isFile()) targetPath else "$targetPath/"
                isOverrideExistingFilesInZip = true
            }

            if (sourceHolder.isFile()) {
                sourceZip.getInputStream(sourceZip.getFileHeader(sourceHolder.node.path))
                    .use { input ->
                        destZip.addStream(input, params)
                    }
            } else {
                destZip.addStream(ByteArrayInputStream(ByteArray(0)), params)
            }
            true
        } catch (e: Exception) {
            logger.logError(e)
            false
        }
    }

    private fun createZipEntryPath(basePath: String, relativePath: String): String {
        return if (basePath.isEmpty()) {
            relativePath
        } else {
            "$basePath${File.separator}$relativePath"
        }
    }

    private fun listFilesWithRelativePath(
        basePath: String,
        startFile: ContentHolder
    ): List<TaskContentItem> {
        if (startFile.isFile()) {
            return listOf(
                TaskContentItem(
                    content = startFile,
                    relativePath = startFile.displayName,
                    status = TaskContentStatus.PENDING
                )
            )
        }

        return when (startFile) {
            is LocalFileHolder -> {
                startFile.file.listFilesAndEmptyDirs().map { file ->
                    TaskContentItem(
                        content = LocalFileHolder(file),
                        relativePath = file.toRelativeString(File(basePath))
                            .orIf(startFile.displayName) { it.isEmpty() },
                        status = TaskContentStatus.PENDING
                    )
                }
            }

            is ZipFileHolder -> {
                startFile.node.listFilesAndEmptyDirs().map { node ->
                    TaskContentItem(
                        content = ZipFileHolder(startFile.zipTree, node),
                        relativePath = node.path.toRelativeString(basePath)
                            .orIf(startFile.displayName) { it.isEmpty() },
                        status = TaskContentStatus.PENDING
                    )
                }
            }

            else -> emptyList()
        }
    }
}