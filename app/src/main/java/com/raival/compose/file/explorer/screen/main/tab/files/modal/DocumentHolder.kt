package com.raival.compose.file.explorer.screen.main.tab.files.modal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Immutable
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.deleteRecursively
import com.anggrayudi.storage.file.extension
import com.anggrayudi.storage.file.getAbsolutePath
import com.anggrayudi.storage.file.getBasePath
import com.anggrayudi.storage.file.getStorageId
import com.anggrayudi.storage.file.hasParent
import com.anggrayudi.storage.file.isEmpty
import com.anggrayudi.storage.file.isRawFile
import com.anggrayudi.storage.file.mimeType
import com.anggrayudi.storage.file.openInputStream
import com.anggrayudi.storage.file.openOutputStream
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.conditions
import com.raival.compose.file.explorer.common.extension.drawableToBitmap
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isNot
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.aiFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkBundleFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.archiveFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.cssFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.docFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.excelFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.fontFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.isoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.jsFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pdfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.pptFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.psdFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.sqlFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.svgFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vcfFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.vectorFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.videoFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_DATE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_NAME
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_SIZE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortFoldersFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortLargerFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortName
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortNameRev
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortNewerFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortOlderFirst
import com.raival.compose.file.explorer.screen.main.tab.files.misc.sortSmallerFirst
import java.io.File

@Immutable
data class DocumentHolder(val documentFile: DocumentFile) : ContentHolder() {
    var formattedDetailsCache = emptyString

    override fun getName() = documentFile.name ?: UNKNOWN_NAME
    override fun getContent() = documentFile

    val fileExtension by lazy { documentFile.extension.lowercase() }

    val fileSize by lazy { documentFile.length() }

    val lastModified by lazy { documentFile.lastModified() }

    val isHidden by lazy { getName().startsWith(".") }

    val uri by lazy { documentFile.uri }

    val path by lazy { documentFile.getAbsolutePath(globalClass) }

    val isFile by lazy { documentFile.isFile }

    val isFolder by lazy { documentFile.isDirectory }

    val isArchive by lazy { isFile && archiveFileType.contains(fileExtension) }

    val isApk by lazy { isFile && fileExtension == apkFileType }

    val isApks by lazy { isFile && apkBundleFileType.contains(fileExtension) }

    val isImage by lazy { isFile && imageFileType.contains(fileExtension) }

    val isVideo by lazy { isFile && videoFileType.contains(fileExtension) }

    val basePath by lazy { documentFile.getBasePath(globalClass) }

    val storageId by lazy { documentFile.getStorageId(globalClass) }

    val mimeType by lazy { documentFile.mimeType ?: getMimeType(uri) ?: anyFileType }

    val canAccessParent by lazy { documentFile.parentFile != null || parentAlt != null }

    val parent: DocumentHolder? by lazy {
        documentFile.parentFile?.let { DocumentHolder(it) } ?: parentAlt
    }

    private val parentAlt: DocumentHolder? by lazy {
        val file = File(path)
        if (file.exists() && file.canRead()) {
            file.parentFile?.takeIf { it.exists() && it.canRead() }?.let {
                DocumentHolder(DocumentFile.fromFile(it))
            }
        } else null
    }

    fun exists() = documentFile.exists()

    fun isEmpty() = documentFile.isEmpty(globalClass)

    fun hasParent(parent: DocumentHolder): Boolean =
        documentFile.hasParent(globalClass, parent.documentFile)

    private fun getMimeType(uri: Uri): String? = MimeTypeMap
        .getSingleton()
        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()))

    fun findFile(name: String): DocumentHolder? =
        documentFile.findFile(name)?.let { DocumentHolder(it) }

    fun delete() = documentFile.delete()

    fun deleteRecursively() = documentFile.deleteRecursively(globalClass)

    fun openInputStream() = documentFile.openInputStream(globalClass)

    fun openOutputStream() = documentFile.openOutputStream(globalClass)

    fun createSubFile(name: String) =
        documentFile.createFile(anyFileType, name)?.let { DocumentHolder(it) }

    fun createSubFolder(name: String) =
        documentFile.createDirectory(name)?.let { DocumentHolder(it) }

    fun renameTo(newName: String) {
        documentFile.renameTo(newName)
    }

    private fun createUri() = if (documentFile.isRawFile) {
        FileProvider.getUriForFile(
            globalClass,
            "com.raival.compose.file.explorer.provider",
            toFile()!!
        )
    } else {
        uri
    }

    fun walk(includeNoneEmptyFolders: Boolean = false): List<DocumentHolder> {
        val fileTree = mutableListOf<DocumentHolder>()
        listContent(false) {
            if (it.isFolder) {
                if (it.isEmpty()) {
                    fileTree.add(it)
                } else {
                    if (includeNoneEmptyFolders) fileTree.add(it)
                    fileTree.addAll(it.walk(includeNoneEmptyFolders))
                }
            } else {
                fileTree.add(it)
            }
        }
        return fileTree
    }

    fun analyze(
        onCountFile: () -> Unit,
        onCountFolder: (isEmpty: Boolean) -> Unit,
        onCountSize: (size: Long) -> Unit
    ) {
        if (documentFile.isFile) {
            onCountFile()
            onCountSize(fileSize)
        } else if (documentFile.isDirectory) {
            onCountFolder(documentFile.isEmpty(globalClass))
            listContent(false) { file ->
                file.analyze(onCountFile, onCountFolder, onCountSize)
            }
        }
    }

    fun listContent(
        sort: Boolean = true,
        sortingPrefs: FileSortingPrefs = FileSortingPrefs(),
        onFile: (DocumentHolder) -> Unit = { }
    ): ArrayList<DocumentHolder> {
        return arrayListOf<DocumentHolder>().apply {
            documentFile.listFiles().forEach {
                DocumentHolder(it).let { file ->
                    add(file)
                    onFile(file)
                }
            }

            if (sort) {
                when (sortingPrefs.sortMethod) {
                    SORT_BY_NAME -> {
                        sortWith(if (sortingPrefs.reverseSorting) sortNameRev else sortName)
                    }

                    SORT_BY_DATE -> {
                        sortWith(if (sortingPrefs.reverseSorting) sortOlderFirst else sortNewerFirst)
                    }

                    SORT_BY_SIZE -> {
                        sortWith(if (sortingPrefs.reverseSorting) sortLargerFirst else sortSmallerFirst)
                    }
                }

                if (sortingPrefs.showFoldersFirst) sortWith(sortFoldersFirst)
            }
        }
    }

    fun getFormattedDetails(
        useCache: Boolean = false,
        showFolderContentCount: Boolean
    ): String {
        if (useCache && formattedDetailsCache.isNotEmpty()) {
            return formattedDetailsCache
        }

        val separator = " | "

        formattedDetailsCache = buildString {
            append(documentFile.lastModified().toFormattedDate())
            if (showFolderContentCount) append(separator)
            if (documentFile.isFile) {
                if (!showFolderContentCount) append(separator)
                append(documentFile.length().toFormattedSize())
                append(separator)
                append(fileExtension)
            } else if (showFolderContentCount) {
                append(getFormattedFileCount())
            }
        }

        return formattedDetailsCache
    }

    fun toFile(): File? {
        if (documentFile.isRawFile) {
            return uri.toFile()
        }
        return null
    }

    fun writeText(text: String) {
        if (documentFile.isRawFile) {
            File(path).writeText(text)
        } else {
            documentFile.openOutputStream(globalClass, false)?.use {
                it.write(text.toByteArray())
            }
        }
    }

    fun readText() = documentFile.openInputStream(globalClass)?.bufferedReader()?.use { reader ->
        val text = reader.readText()
        reader.close()
        text
    } ?: emptyString

    fun readLines(
        maxLines: Int = -1,
        onReadLine: (index: Int, line: String) -> Unit
    ) = documentFile.openInputStream(globalClass)?.bufferedReader()?.use { reader ->
        var lineCount = 0
        reader.forEachLine { line ->
            if (maxLines in 1..lineCount) return@forEachLine
            onReadLine(lineCount, line)
            lineCount++
        }
    } ?: emptyString

    fun appendText(text: String) {
        if (documentFile.isRawFile) {
            File(path).appendText(text)
        } else {
            documentFile.openOutputStream(globalClass, true)?.use {
                it.write(text.toByteArray())
            }
        }
    }

    private fun getFormattedFileCount(): String {
        var filesCount = 0
        var foldersCount = 0

        listContent(false) {
            if (it.isFile) filesCount++ else foldersCount++
        }

        return getFormattedFileCount(filesCount, foldersCount)
    }

    fun getFormattedFileCount(filesCount: Int, foldersCount: Int): String {
        return buildString {
            if (foldersCount == 0 && filesCount == 0) {
                append(globalClass.getString(R.string.empty_folder))
            } else {
                if (foldersCount > 0) {
                    append(
                        globalClass.getString(
                            R.string.folders_count,
                            foldersCount
                        )
                    )
                    if (filesCount > 0) append(", ")
                }
                if (filesCount > 0) {
                    append(
                        globalClass.getString(
                            R.string.files_count,
                            filesCount
                        )
                    )
                }
            }
        }
    }

    fun openFile(
        context: Context,
        anonymous: Boolean,
        skipSupportedExtensions: Boolean,
        customMimeType: String? = null
    ) {
        if (!skipSupportedExtensions && handleSupportedFiles(context)) {
            return
        }

        Intent(Intent.ACTION_VIEW).let { newIntent ->
            newIntent.setDataAndType(
                createUri(),
                customMimeType ?: if (anonymous) anyFileType else documentFile.mimeType
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
            } catch (e: ActivityNotFoundException) {
                if (!anonymous) {
                    openFile(context, anonymous = true, skipSupportedExtensions = true)
                } else {
                    globalClass.showMsg(R.string.no_app_can_open_file)
                }
            } catch (e: Exception) {
                with(globalClass) {
                    showMsg(getString(R.string.failed_to_open_this_file))
                    log(e)
                }
            }
        }
    }

    private fun handleSupportedFiles(context: Context): Boolean {
        val ext = fileExtension

        if (FileMimeType.conditions { codeFileType.contains(ext) || editableFileType.contains(ext) }) {
            globalClass.textEditorManager.openTextEditor(
                this,
                context
            )
            return true
        }

        return false
    }

    fun getAppsHandlingFile(mimeType: String = emptyString): List<OpenWithActivityHolder> {
        val packageManager: PackageManager = globalClass.packageManager

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(createUri(), mimeType.ifEmpty { this@DocumentHolder.mimeType })
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

    fun getFileIconType(): Int {
        if (isFolder) {
            return FILE_TYPE_FOLDER
        } else if (fileExtension == aiFileType) {
            return FILE_TYPE_AI
        } else if (fileExtension == apkFileType) {
            return FILE_TYPE_APK
        } else if (fileExtension == cssFileType) {
            return FILE_TYPE_CSS
        } else if (fileExtension == isoFileType) {
            return FILE_TYPE_ISO
        } else if (fileExtension == jsFileType) {
            return FILE_TYPE_JS
        } else if (fileExtension == psdFileType) {
            return FILE_TYPE_PSD
        } else if (fileExtension == sqlFileType) {
            return FILE_TYPE_SQL
        } else if (fileExtension == svgFileType) {
            return FILE_TYPE_SVG
        } else if (fileExtension == vcfFileType) {
            return FILE_TYPE_VCF
        } else if (fileExtension == pdfFileType) {
            return FILE_TYPE_PDF
        } else if (docFileType.contains(fileExtension)) {
            return FILE_TYPE_DOC
        } else if (excelFileType.contains(fileExtension)) {
            return FILE_TYPE_XLS
        } else if (pptFileType.contains(fileExtension)) {
            return FILE_TYPE_PPT
        } else if (fontFileType.contains(fileExtension)) {
            return FILE_TYPE_FONT
        } else if (vectorFileType.contains(fileExtension)) {
            return FILE_TYPE_VECTOR
        } else if (archiveFileType.contains(fileExtension) || apkBundleFileType.contains(fileExtension)) {
            return FILE_TYPE_ARCHIVE
        } else if (videoFileType.contains(fileExtension)) {
            return FILE_TYPE_VIDEO
        } else if (codeFileType.contains(fileExtension)) {
            return FILE_TYPE_CODE
        } else if (editableFileType.contains(fileExtension)) {
            return FILE_TYPE_TEXT
        } else if (imageFileType.contains(fileExtension)) {
            return FILE_TYPE_IMAGE
        } else if (audioFileType.contains(fileExtension)) {
            return FILE_TYPE_AUDIO
        } else {
            return FILE_TYPE_UNKNOWN
        }
    }

    fun getFileIconResource() = when (getFileIconType()) {
        FILE_TYPE_FOLDER -> R.drawable.baseline_folder_24
        FILE_TYPE_AI -> R.drawable.ai_file_extension
        FILE_TYPE_APK -> R.drawable.apk_file_extension
        FILE_TYPE_CSS -> R.drawable.css_file_extension
        FILE_TYPE_ISO -> R.drawable.iso_file_extension
        FILE_TYPE_JS -> R.drawable.js_file_extension
        FILE_TYPE_PDF -> R.drawable.pdf_file_extension
        FILE_TYPE_PSD -> R.drawable.psd_file_extension
        FILE_TYPE_SQL -> R.drawable.sql_file_extension
        FILE_TYPE_SVG -> R.drawable.svg_file_extension
        FILE_TYPE_VCF -> R.drawable.vcf_file_extension
        FILE_TYPE_JAVA -> R.drawable.javascript_file_extension
        FILE_TYPE_KOTLIN -> R.drawable.css_file_extension
        FILE_TYPE_DOC -> R.drawable.doc_file_extension
        FILE_TYPE_XLS -> R.drawable.xls_file_extension
        FILE_TYPE_PPT -> R.drawable.ppt_file_extension
        FILE_TYPE_FONT -> R.drawable.font_file_extension
        FILE_TYPE_VECTOR -> R.drawable.vector_file_extension
        FILE_TYPE_VIDEO -> R.drawable.video_file_extension
        FILE_TYPE_AUDIO -> R.drawable.music_file_extension
        FILE_TYPE_IMAGE -> R.drawable.jpg_file_extension
        FILE_TYPE_CODE -> R.drawable.css_file_extension
        FILE_TYPE_TEXT -> R.drawable.txt_file_extension
        FILE_TYPE_ARCHIVE -> R.drawable.zip_file_extension
        else -> R.drawable.unknown_file_extension
    }

    companion object {
        const val UNKNOWN_NAME = "UNKNOWN"

        const val FILE_TYPE_FOLDER = -1
        const val FILE_TYPE_UNKNOWN = 0
        const val FILE_TYPE_AI = 1
        const val FILE_TYPE_APK = 2
        const val FILE_TYPE_CSS = 3
        const val FILE_TYPE_ISO = 4
        const val FILE_TYPE_JS = 5
        const val FILE_TYPE_PDF = 6
        const val FILE_TYPE_PSD = 7
        const val FILE_TYPE_SQL = 8
        const val FILE_TYPE_SVG = 9
        const val FILE_TYPE_VCF = 10
        const val FILE_TYPE_JAVA = 11
        const val FILE_TYPE_KOTLIN = 12
        const val FILE_TYPE_DOC = 13
        const val FILE_TYPE_XLS = 14
        const val FILE_TYPE_PPT = 15
        const val FILE_TYPE_FONT = 16
        const val FILE_TYPE_VECTOR = 17
        const val FILE_TYPE_VIDEO = 18
        const val FILE_TYPE_AUDIO = 19
        const val FILE_TYPE_IMAGE = 20
        const val FILE_TYPE_CODE = 21
        const val FILE_TYPE_TEXT = 22
        const val FILE_TYPE_ARCHIVE = 23


        fun fromFullPath(path: String): DocumentHolder? {
            val doc = DocumentFileCompat.fromFullPath(globalClass, path)
            return if (doc isNot null) DocumentHolder(doc!!) else null
        }

        fun fromFile(file: File): DocumentHolder {
            return DocumentHolder(DocumentFile.fromFile(file))
        }

        fun fromUri(uri: Uri): DocumentHolder? {
            val doc = DocumentFileCompat.fromUri(globalClass, uri)
            return if (doc isNot null) DocumentHolder(doc!!) else null
        }
    }
}