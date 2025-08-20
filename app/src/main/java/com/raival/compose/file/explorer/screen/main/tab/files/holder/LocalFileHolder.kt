package com.raival.compose.file.explorer.screen.main.tab.files.holder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.mimeType
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.drawableToBitmap
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.hasParent
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ContentCount
import com.raival.compose.file.explorer.screen.main.tab.files.misc.DefaultOpeningMethods
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.prismPrefsFileType
import com.raival.compose.file.explorer.screen.viewer.audio.AudioPlayerActivity
import com.raival.compose.file.explorer.screen.viewer.image.ImageViewerActivity
import com.raival.compose.file.explorer.screen.viewer.pdf.PdfViewerActivity
import com.raival.compose.file.explorer.screen.viewer.video.VideoPlayerActivity
import kotlinx.coroutines.runBlocking
import java.io.File

class LocalFileHolder(val file: File) : ContentHolder() {
    private var folderCount = 0
    private var fileCount = 0
    private var timestamp = -1L

    override val displayName: String by lazy { file.name }

    var details = emptyString

    override val isFolder: Boolean by lazy { file.isDirectory }

    override val lastModified: Long
        get() = file.lastModified().also {
            if (timestamp == -1L) timestamp = it
        }

    override val size: Long by lazy { file.length() }

    override val uniquePath: String by lazy { file.absolutePath }

    override val extension: String by lazy { file.extension.lowercase() }

    override val canAddNewContent: Boolean = true

    override val canRead: Boolean by lazy { file.canRead() }

    override val canWrite: Boolean by lazy { file.canWrite() }

    val mimeType by lazy { file.mimeType ?: anyFileType }

    val basePath by lazy { file.getBasePath(globalClass) }

    override suspend fun getDetails(): String {
        val separator = " | "

        if (details.isNotEmpty()) return details

        return buildString {
            append(getLastModifiedDate())
            if (file.isDirectory) {
                if (globalClass.preferencesManager.showFolderContentCount && file.canRead()) {
                    append(separator)
                    append(getFormattedFileCount())
                }
            } else {
                append(separator)
                append(file.length().toFormattedSize())
                append(separator)
                append(file.extension)
            }
        }.also {
            details = it
        }
    }

    override suspend fun isValid(): Boolean = file.exists()

    override suspend fun listContent(): ArrayList<LocalFileHolder> {
        folderCount = 0
        fileCount = 0

        return arrayListOf<LocalFileHolder>().apply {
            file.listFiles()?.forEach { add(LocalFileHolder(it)) }
        }
    }

    override suspend fun getParent(): LocalFileHolder? =
        file.parentFile?.let { LocalFileHolder(it) }

    override fun open(
        context: Context,
        anonymous: Boolean,
        skipSupportedExtensions: Boolean,
        customMimeType: String?
    ) {
        val defaultOpeningMethods =
            fromJson<DefaultOpeningMethods>(globalClass.preferencesManager.defaultOpeningMethods)
                ?: DefaultOpeningMethods()
        defaultOpeningMethods.openingMethods.forEach {
            if (it.extension == extension) {
                openFileWithPackage(context, it.packageName, it.className)
                return
            }
        }

        if (handleSupportedFiles(skipSupportedExtensions, context)) {
            return
        }

        Intent(Intent.ACTION_VIEW).let { newIntent ->
            newIntent.setDataAndType(
                createUri(),
                customMimeType ?: if (anonymous) anyFileType else file.mimeType
            )

            newIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            try {
                context.startActivity(newIntent)
            } catch (_: ActivityNotFoundException) {
                if (!anonymous) {
                    open(context, anonymous = true, skipSupportedExtensions = true, null)
                } else {
                    globalClass.showMsg(R.string.no_app_can_open_file)
                }
            } catch (e: Exception) {
                with(globalClass) {
                    logger.logError(e)
                    showMsg(getString(R.string.failed_to_open_this_file))
                }
            }
        }
    }

    override suspend fun getContentCount(): ContentCount {
        if (fileCount == 0 && folderCount == 0) {
            file.listFiles()?.let { list ->
                list.forEach {
                    if (it.isFile) fileCount++ else folderCount++
                }
            }
        }

        return ContentCount(fileCount, folderCount)
    }

    override suspend fun createSubFile(name: String, onCreated: (ContentHolder?) -> Unit) {
        File(file, name).let { newFile ->
            if (newFile.createNewFile()) {
                onCreated(LocalFileHolder(newFile))
                return
            }
        }
        onCreated(null)
    }

    override suspend fun createSubFolder(name: String, onCreated: (ContentHolder?) -> Unit) {
        File(file, name).let { newFolder ->
            if (newFolder.exists() || newFolder.mkdir()) {
                onCreated(LocalFileHolder(newFolder))
                return
            }
        }
        onCreated(null)
    }

    override suspend fun findFile(name: String): LocalFileHolder? {
        File(file, name).let {
            if (it.exists()) {
                return LocalFileHolder(it)
            }
        }
        return null
    }

    fun exists() = runBlocking { isValid() }

    fun hasSourceChanged() = timestamp isNot -1L && lastModified isNot timestamp

    fun resetCachedTimestamp() {
        timestamp = lastModified
    }

    fun getAppsHandlingFile(mimeType: String = emptyString): List<OpenWithActivityHolder> {
        val packageManager: PackageManager = globalClass.packageManager

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(createUri(), mimeType.ifEmpty { this@LocalFileHolder.mimeType })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }

        val appsList = ArrayList<OpenWithActivityHolder>()

        packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        ).onEach {
            globalClass.grantUriPermission(
                it.activityInfo.packageName,
                createUri(),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            appsList.add(
                OpenWithActivityHolder(
                    label = it.activityInfo.loadLabel(packageManager).toString(),
                    name = it.activityInfo.name,
                    packageName = it.activityInfo.packageName,
                    icon = it.activityInfo.loadIcon(packageManager).drawableToBitmap(),
                )
            )
        }

        return appsList
    }

    fun openFileWithPackage(context: Context, packageName: String, className: String) {
        val uri = createUri()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                        or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        or Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            setPackage(packageName)
            setClassName(packageName, className)
        }

        if (intent.resolveActivity(globalClass.packageManager) != null) {
            context.startActivity(intent)
        } else {
            globalClass.showMsg("No app found to open this file.")
        }
    }

    fun writeText(text: String) {
        file.writeText(text)
    }

    fun readText() = file.readText()

    private fun handleSupportedFiles(skipSupportedExtensions: Boolean, context: Context): Boolean {
        if (prismPrefsFileType == extension) {
            val activeTab = globalClass.mainActivityManager.getActiveTab()
            if (activeTab is FilesTab) {
                activeTab.toggleImportPrefsDialog(this)
                return true
            }
        }

        if (FileMimeType.supportedArchiveFileType.contains(extension)) {
            if (isApk() && skipSupportedExtensions) return false
            globalClass.zipManager.openArchive(this)
            return true
        }

        if (skipSupportedExtensions) return false

        if (codeFileType.contains(extension) || editableFileType.contains(extension)) {
            globalClass.textEditorManager.openTextEditor(
                this,
                context
            )
            return true
        }

        if (FileMimeType.videoFileType.contains(extension)) {
            openFileWithPackage(
                context,
                context.packageName,
                VideoPlayerActivity::class.java.name
            )
            return true
        }

        if (FileMimeType.audioFileType.contains(extension)) {
            openFileWithPackage(
                context,
                context.packageName,
                AudioPlayerActivity::class.java.name
            )
            return true
        }

        if (FileMimeType.imageFileType.contains(extension)) {
            openFileWithPackage(
                context,
                context.packageName,
                ImageViewerActivity::class.java.name
            )
            return true
        }

        if (FileMimeType.pdfFileType.contains(extension)) {
            openFileWithPackage(
                context,
                context.packageName,
                PdfViewerActivity::class.java.name
            )
            return true
        }

        return false
    }

    private fun createUri() = FileProvider.getUriForFile(
        globalClass,
        "com.raival.compose.file.explorer.provider",
        file
    )

    fun hasParent(parent: LocalFileHolder): Boolean =
        file.absolutePath.hasParent(parent.file.absolutePath)

    suspend fun getFormattedFileCount(): String {
        val contentCount = getContentCount()

        return getFormattedFileCount(
            contentCount.files,
            contentCount.folders
        )
    }
}