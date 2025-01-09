package com.raival.compose.file.explorer.screen.main.tab.regular.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.ui.graphics.vector.ImageVector
import com.anggrayudi.storage.extension.launchOnUiThread
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.common.extension.randomString
import com.raival.compose.file.explorer.common.extension.trimToLastTwoSegments
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class RegularTabDecompressTask(
    private val source: List<DocumentHolder>
) : RegularTabTask() {
    override val id: String = String.randomString(8)

    override fun getTitle(): String = globalClass.getString(R.string.decompress)

    override fun getSubtitle(): String = if (source.size == 1)
        source[0].getPath().trimToLastTwoSegments()
    else globalClass.getString(R.string.task_subtitle, source.size)

    override fun execute(destination: DocumentHolder, callback: Any) {
        val taskCallback = callback as RegularTabTaskCallback

        var total = 0
        var completed = 0
        var skipped = 0

        val details = RegularTabTaskDetails(
            this,
            TASK_DECOMPRESS,
            getTitle(),
            globalClass.getString(R.string.preparing),
            emptyString,
            0f
        )

        taskCallback.onPrepare(details)

        globalClass.contentResolver.openInputStream(source[0].getUri())?.use { zipInputStream ->
            ZipInputStream(zipInputStream).use { zis ->
                var zipEntry: ZipEntry?
                while (zis.nextEntry.also { zipEntry = it } != null) {
                    zipEntry?.let { entry ->
                        if (!entry.isDirectory) total++
                    }
                }
            }
        }

        fun updateProgress(info: String = emptyString): RegularTabTaskDetails {
            return details.apply {
                if (info.isNotEmpty()) this.info = info
                if (progress >= 0) this.progress = (completed + skipped.toFloat()) / total
                subtitle = globalClass.getString(R.string.progress, completed + skipped, total)
            }
        }

        taskCallback.onReport(updateProgress())

        fun createFileInTargetDir(targetDir: DocumentHolder, entry: ZipEntry): DocumentHolder? {
            var currentDir = targetDir
            val pathSegments = entry.name.split("/")
            for (i in 0 until pathSegments.size - 1) {
                currentDir = currentDir.findFile(pathSegments[i])
                    ?: currentDir.createSubFolder(pathSegments[i]) ?: return null
            }

            val existingFile = currentDir.findFile(pathSegments.last())

            if (existingFile isNot null) {
                if (existingFile!!.isFolder()) existingFile.deleteRecursively()
                else existingFile.delete()
            }

            return if (entry.isDirectory) {
                currentDir.createSubFolder(pathSegments.last())
            } else {
                currentDir.createSubFile(pathSegments.last())
            }
        }

        fun copyStream(input: InputStream, output: OutputStream) {
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                output.write(buffer, 0, length)
            }
        }

        fun writeToFile(zis: ZipInputStream, outputFile: DocumentHolder?) {
            outputFile?.getUri()?.let { uri ->
                globalClass.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    copyStream(zis, outputStream)
                }
            }
        }

        val inputStream = globalClass.contentResolver.openInputStream(source[0].getUri())

        inputStream?.use { zipInputStream ->
            ZipInputStream(zipInputStream).use { zis ->
                var zipEntry: ZipEntry?
                while (zis.nextEntry.also { zipEntry = it } != null) {
                    zipEntry?.let { entry ->
                        taskCallback.onReport(
                            updateProgress(
                                info = globalClass.getString(R.string.decompressing, entry.name)
                            )
                        )
                        val outputFile = createFileInTargetDir(destination, entry)
                        if (outputFile isNot null && !entry.isDirectory) {
                            writeToFile(zis, outputFile)
                            completed++
                        } else if (outputFile == null) {
                            skipped++
                        }
                        taskCallback.onReport(updateProgress())
                    }
                }
            }
        }

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