package com.raival.compose.file.explorer.screen.main.tab.regular.modal

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
import com.raival.compose.file.explorer.common.extension.plural
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.aiFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.archiveFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.audioFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.codeFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.cssFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.docFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.editableFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.excelFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.fontFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.imageFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.isoFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.jsFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.pdfFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.pptFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.psdFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.sqlFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.svgFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.vcfFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.vectorFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.FileMimeType.videoFileType
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.SortingMethod.SORT_BY_DATE
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.SortingMethod.SORT_BY_NAME
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.SortingMethod.SORT_BY_SIZE
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortFoldersFirst
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortLargerFirst
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortName
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortNameRev
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortNewerFirst
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortOlderFirst
import com.raival.compose.file.explorer.screen.main.tab.regular.misc.sortSmallerFirst
import java.io.File

@Immutable
data class DocumentHolder(
    val documentFile: DocumentFile
) {
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

    var formattedDetailsCache = emptyString

    fun getFileName(): String = documentFile.name ?: UNKNOWN_NAME

    fun exists(): Boolean = documentFile.exists()

    fun getFileExtension(): String = documentFile.extension.lowercase()

    fun getFileSize(): Long = documentFile.length()

    fun getLastModified(): Long = documentFile.lastModified()

    fun isHidden(): Boolean = getFileName().startsWith(".")

    fun getUri(): Uri = documentFile.uri

    fun getProviderUri() = if (documentFile.isRawFile) {
        FileProvider.getUriForFile(
            globalClass,
            "com.raival.compose.file.explorer.provider",
            toFile()!!
        )
    } else {
        getUri()
    }

    fun getPath(): String = documentFile.getAbsolutePath(globalClass)

    fun isFile(): Boolean = documentFile.isFile

    fun isFolder(): Boolean = documentFile.isDirectory

    fun isEmpty(): Boolean = documentFile.isEmpty(globalClass)

    fun isArchive() = isFile() && archiveFileType.contains(getFileExtension().lowercase())

    fun isApk() = isFile() && getFileExtension() == apkFileType

    fun findFile(name: String): DocumentHolder? =
        documentFile.findFile(name)?.let { DocumentHolder(it) }

    fun getBasePath(): String = documentFile.getBasePath(globalClass)

    fun delete(): Boolean = documentFile.delete()

    fun deleteRecursively(): Boolean = documentFile.deleteRecursively(globalClass)

    fun getStorageId(): String = documentFile.getStorageId(globalClass)

    fun openInputStream() = documentFile.openInputStream(globalClass)

    fun openOutputStream() = documentFile.openOutputStream(globalClass)

    fun createSubFile(name: String) =
        documentFile.createFile(anyFileType, name)?.let { DocumentHolder(it) }

    fun createSubFolder(name: String) =
        documentFile.createDirectory(name)?.let { DocumentHolder(it) }

    fun renameTo(newName: String) {
        documentFile.renameTo(newName)
    }

    private fun getMimeType(uri: Uri): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    fun getMimeType(): String {
        return documentFile.mimeType ?: getMimeType(getUri()) ?: anyFileType
    }

    fun canAccessParent(): Boolean {
        return documentFile.parentFile != null || getParentAlt() != null
    }

    fun hasParent(parent: DocumentHolder): Boolean =
        documentFile.hasParent(globalClass, parent.documentFile)

    fun getParent(): DocumentHolder? {
        val parent = documentFile.parentFile
        if (parent != null) return DocumentHolder(parent)

        return getParentAlt()
    }

    private fun getParentAlt(): DocumentHolder? {
        val file = File(getPath())
        if (file.exists() && file.canRead()) {
            val parentFile = file.parentFile
            if (parentFile != null && parentFile.exists() && parentFile.canRead()) {
                return DocumentHolder(DocumentFile.fromFile(parentFile))
            }
        }
        return null
    }


    fun walk(includeNoneEmptyFolders: Boolean = false): List<DocumentHolder> {
        val fileTree = mutableListOf<DocumentHolder>()
        listContent(false) {
            if (it.isFolder()) {
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
            onCountSize(getFileSize())
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
                append(getFileExtension())
            } else if (showFolderContentCount) {
                append(getFormattedFileCount())
            }
        }

        return formattedDetailsCache
    }

    fun toFile(): File? {
        if (documentFile.isRawFile) {
            return getUri().toFile()
        }
        return null
    }

    fun writeText(text: String) {
        if (documentFile.isRawFile) {
            File(getPath()).writeText(text)
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
            File(getPath()).appendText(text)
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
            if (it.isFile()) filesCount++ else foldersCount++
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
                            foldersCount,
                            plural(foldersCount)
                        )
                    )
                    if (filesCount > 0) append(", ")
                }
                if (filesCount > 0) {
                    append(
                        globalClass.getString(
                            R.string.files_count,
                            filesCount,
                            plural(filesCount)
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
                getProviderUri(),
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
        val ext = getFileExtension()

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
            setDataAndType(getProviderUri(), mimeType.ifEmpty { getMimeType() })
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
                getProviderUri(),
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
        val uri = getProviderUri()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, getMimeType())
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
        if (isFolder()) {
            return FILE_TYPE_FOLDER
        } else if (getFileExtension() == aiFileType) {
            return FILE_TYPE_AI
        } else if (getFileExtension() == apkFileType) {
            return FILE_TYPE_APK
        } else if (getFileExtension() == cssFileType) {
            return FILE_TYPE_CSS
        } else if (getFileExtension() == isoFileType) {
            return FILE_TYPE_ISO
        } else if (getFileExtension() == jsFileType) {
            return FILE_TYPE_JS
        } else if (getFileExtension() == psdFileType) {
            return FILE_TYPE_PSD
        } else if (getFileExtension() == sqlFileType) {
            return FILE_TYPE_SQL
        } else if (getFileExtension() == svgFileType) {
            return FILE_TYPE_SVG
        } else if (getFileExtension() == vcfFileType) {
            return FILE_TYPE_VCF
        } else if (getFileExtension() == pdfFileType) {
            return FILE_TYPE_PDF
        } else if (docFileType.contains(getFileExtension())) {
            return FILE_TYPE_DOC
        } else if (excelFileType.contains(getFileExtension())) {
            return FILE_TYPE_XLS
        } else if (pptFileType.contains(getFileExtension())) {
            return FILE_TYPE_PPT
        } else if (fontFileType.contains(getFileExtension())) {
            return FILE_TYPE_FONT
        } else if (vectorFileType.contains(getFileExtension())) {
            return FILE_TYPE_VECTOR
        } else if (archiveFileType.contains(getFileExtension())) {
            return FILE_TYPE_ARCHIVE
        } else if (videoFileType.contains(getFileExtension())) {
            return FILE_TYPE_VIDEO
        } else if (codeFileType.contains(getFileExtension())) {
            return FILE_TYPE_CODE
        } else if (editableFileType.contains(getFileExtension())) {
            return FILE_TYPE_TEXT
        } else if (imageFileType.contains(getFileExtension())) {
            return FILE_TYPE_IMAGE
        } else if (audioFileType.contains(getFileExtension())) {
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
}