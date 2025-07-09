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
import com.raival.compose.file.explorer.common.extension.conditions
import com.raival.compose.file.explorer.common.extension.drawableToBitmap
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.hasParent
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ContentCount
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import java.io.File

class LocalFileHolder(val file: File) : ContentHolder() {
    private var folderCount = 0
    private var fileCount = 0
    private var cachedLastModified = -1L

    override val displayName: String by lazy { file.name }

    override val details by lazy {
        val separator = " | "
        buildString {
            append(lastModified.toFormattedDate())
            if (file.isDirectory) {
                if (globalClass.preferencesManager.fileListPrefs.showFolderContentCount && file.canRead()) {
                    append(separator)
                    append(getFormattedFileCount())
                }
            } else {
                append(separator)
                append(file.length().toFormattedSize())
                append(separator)
                append(file.extension)
            }
        }
    }

    override val isFolder: Boolean by lazy { file.isDirectory }

    override val lastModified: Long
        get() = file.lastModified().also {
            if (cachedLastModified == -1L) cachedLastModified = it
        }

    override val size: Long by lazy { file.length() }

    override val icon: Any = file

    override val iconPlaceholder: Int by lazy { getContentIconPlaceholderResource() }

    override val uniquePath: String by lazy { file.absolutePath }

    override val extension: String by lazy { file.extension.lowercase() }

    override val canAddNewContent: Boolean = true

    override val canRead: Boolean by lazy { file.canRead() }

    override val canWrite: Boolean by lazy { file.canWrite() }

    val mimeType by lazy { file.mimeType ?: anyFileType }

    val basePath by lazy { file.getBasePath(globalClass) }

    override fun isValid(): Boolean = file.exists()

    override suspend fun listContent(): ArrayList<LocalFileHolder> {
        folderCount = 0
        fileCount = 0

        return arrayListOf<LocalFileHolder>().apply {
            file.listFiles()?.forEach { add(LocalFileHolder(it)) }
        }
    }

    override fun getParent(): LocalFileHolder? = file.parentFile?.let { LocalFileHolder(it) }

    override fun open(
        context: Context,
        anonymous: Boolean,
        skipSupportedExtensions: Boolean,
        customMimeType: String?
    ) {
        if (!skipSupportedExtensions && handleSupportedFiles(context)) {
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

    override fun getContentCount(): ContentCount {
        if (fileCount == 0 && folderCount == 0) {
            file.listFiles()?.let { list ->
                list.forEach {
                    if (it.isFile) fileCount++ else folderCount++
                }
            }
        }

        return ContentCount(fileCount, folderCount)
    }

    override fun createSubFile(name: String, onCreated: (ContentHolder?) -> Unit) {
        File(file, name).let { newFile ->
            if (newFile.createNewFile()) {
                onCreated(LocalFileHolder(newFile))
                return
            }
        }
        onCreated(null)
    }

    override fun createSubFolder(name: String, onCreated: (ContentHolder?) -> Unit) {
        File(file, name).let { newFolder ->
            if (newFolder.exists() || newFolder.mkdir()) {
                onCreated(LocalFileHolder(newFolder))
                return
            }
        }
        onCreated(null)
    }

    override fun findFile(name: String): LocalFileHolder? {
        File(file, name).let {
            if (it.exists()) {
                return LocalFileHolder(it)
            }
        }
        return null
    }

    fun exists() = isValid()

    fun hasSourceChanged() = cachedLastModified != -1L && lastModified != cachedLastModified

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

    fun appendText(text: String) {
        file.appendText(text)
    }

    fun readText() = file.readText()

    private fun handleSupportedFiles(context: Context): Boolean {
        if (FileMimeType.conditions {
                codeFileType.contains(extension) || editableFileType.contains(
                    extension
                )
            }) {
            globalClass.textEditorManager.openTextEditor(
                this,
                context
            )
            return true
        }

        if (FileMimeType.supportedArchiveFileType.contains(extension)) {
            globalClass.zipManager.openArchive(this)
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

    fun getFormattedFileCount(): String {
        val contentCount = getContentCount()

        return getFormattedFileCount(
            contentCount.files,
            contentCount.folders
        )
    }
}