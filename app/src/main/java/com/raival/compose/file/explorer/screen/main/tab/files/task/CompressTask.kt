package com.raival.compose.file.explorer.screen.main.tab.files.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.ui.graphics.vector.ImageVector
import com.anggrayudi.storage.extension.launchOnUiThread
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.addIfAbsent
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.randomString
import com.raival.compose.file.explorer.common.extension.trimToLastTwoSegments
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class CompressTask(
    private val source: List<DocumentHolder>
) : FilesTabTask() {
    override val id: String = String.randomString(8)

    override fun getTitle(): String = globalClass.getString(R.string.compress)

    override fun getSubtitle(): String = if (source.size == 1)
        source[0].path.trimToLastTwoSegments()
    else globalClass.getString(R.string.task_subtitle, source.size)

    override suspend fun execute(destination: DocumentHolder, callback: Any) {
        val taskCallback = callback as FilesTabTaskCallback

        val total: Int
        var completed = 0
        var skipped = 0

        val entriesToAdded = arrayListOf<String>()

        val details = FilesTabTaskDetails(
            this,
            TASK_COMPRESS,
            getTitle(),
            globalClass.getString(R.string.preparing),
            emptyString,
            0f
        )

        taskCallback.onPrepare(details)

        val filesToCompress = arrayListOf<String>()

        source.forEach {
            filesToCompress.add(it.path)
            if (!it.isFile) {
                filesToCompress.addAll(it.walk(true).map { f -> f.path })
            }
        }

        total = filesToCompress.size

        fun updateProgress(info: String = emptyString): FilesTabTaskDetails {
            return details.apply {
                if (info.isNotEmpty()) this.info = info
                if (progress >= 0) this.progress = (completed + skipped) / total.toFloat()
                subtitle = globalClass.getString(R.string.progress, completed + skipped, total)
            }
        }

        fun addFileToZip(
            documentHolder: DocumentHolder,
            zipOut: ZipOutputStream,
            buffer: ByteArray,
            parentPath: String
        ) {
            if (!filesToCompress.contains(documentHolder.path)) return

            val entryName = if (parentPath.isEmpty()) {
                documentHolder.getName()
            } else {
                "$parentPath/${documentHolder.getName()}"
            }

            if (documentHolder.isFolder) {
                val children = documentHolder.listContent(false)
                if (children.isNotEmpty()) {
                    children.forEach { child ->
                        addFileToZip(child, zipOut, buffer, entryName)
                    }
                } else {
                    val entry = ZipEntry("$entryName/")
                    zipOut.putNextEntry(entry)
                    zipOut.closeEntry()
                    completed++
                    entriesToAdded.addIfAbsent(entryName)
                }
            } else {
                taskCallback.onReport(
                    updateProgress(
                        info = globalClass.getString(
                            R.string.compressing,
                            documentHolder.getName()
                        )
                    )
                )

                val inputStream =
                    globalClass.contentResolver.openInputStream(documentHolder.uri)

                if (inputStream == null) {
                    skipped++
                    taskCallback.onReport(updateProgress())
                }

                inputStream?.use { input ->
                    val entry = ZipEntry(entryName)
                    zipOut.putNextEntry(entry)

                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        zipOut.write(buffer, 0, length)
                    }

                    zipOut.closeEntry()

                    completed++

                    entriesToAdded.addIfAbsent(entryName)

                    taskCallback.onReport(updateProgress())
                }
            }
        }

        val buffer = ByteArray(1024)
        val byteArrayOutputStream = ByteArrayOutputStream()
        destination.openInputStream().use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zipInputStream ->
                ZipOutputStream(BufferedOutputStream(byteArrayOutputStream)).use { zipOut ->
                    source.forEach { documentHolder ->
                        addFileToZip(documentHolder, zipOut, buffer, emptyString)
                    }

                    var entry: ZipEntry? = zipInputStream.nextEntry
                    while (entry != null) {
                        if (entriesToAdded.contains(entry.name)) {
                            zipOut.closeEntry()
                            entry = zipInputStream.nextEntry
                            continue
                        }

                        zipOut.putNextEntry(entry)

                        var len: Int
                        while (zipInputStream.read(buffer).also { len = it } > 0) {
                            zipOut.write(buffer, 0, len)
                        }

                        zipOut.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
            }
        }

        val tempDestination = DocumentHolder.fromFile(
            kotlin.io.path.createTempFile(String.randomString(16)).toFile()
        )
        tempDestination.openOutputStream()?.use { outputStream ->
            byteArrayOutputStream.writeTo(outputStream)
        }

        destination.writeText(emptyString)

        tempDestination.openInputStream()?.use { inputStream ->
            destination.openOutputStream()?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        tempDestination.delete()

        launchOnUiThread {
            taskCallback.onComplete(details.apply {
                subtitle = globalClass.getString(R.string.done)
            })
        }
    }

    override fun getIcon(): ImageVector = Icons.Rounded.Compress

    override fun getSourceFiles(): List<DocumentHolder> {
        return source
    }
}