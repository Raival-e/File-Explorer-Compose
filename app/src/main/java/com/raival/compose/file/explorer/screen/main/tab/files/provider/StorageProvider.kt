package com.raival.compose.file.explorer.screen.main.tab.files.provider

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDeviceHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.EXTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ROOT
import java.io.File

object StorageProvider {
    val recentFiles = DocumentHolder.fromFile(File("/:Recent"))
    val images = DocumentHolder.fromFile(File("/:Images"))
    val videos = DocumentHolder.fromFile(File("/:Videos"))
    val audios = DocumentHolder.fromFile(File("/:Audios"))
    val documents = DocumentHolder.fromFile(File("/:Documents"))
    val archives = DocumentHolder.fromFile(File("/:Archives"))

    fun getStorageDevices(context: Context): List<StorageDeviceHolder> {
        val storageDeviceHolders = mutableListOf<StorageDeviceHolder>()

        storageDeviceHolders.add(getPrimaryInternalStorage(context))

        val externalDirs = getExternalStorageDirectories(context)

        for (externalDir in externalDirs) {
            if (externalDir.path == Environment.getExternalStorageDirectory().path){
                continue
            }

            val statFs = StatFs(externalDir.absolutePath)
            val totalSize = statFs.totalBytes
            val availableSize = statFs.availableBytes
            val usedSize = totalSize - availableSize

            storageDeviceHolders.add(
                StorageDeviceHolder(
                    DocumentHolder(DocumentFile.fromFile(externalDir)),
                    "External Storage (${externalDir.name})",
                    totalSize,
                    usedSize,
                    EXTERNAL_STORAGE
                )
            )
        }

        storageDeviceHolders.add(getRoot(context))

        return storageDeviceHolders
    }

    fun getRoot(context: Context): StorageDeviceHolder {
        val externalStorageDir = Environment.getRootDirectory()

        val externalStatFs = StatFs(externalStorageDir.absolutePath)

        val externalTotalSize = externalStatFs.totalBytes
        val externalAvailableSize = externalStatFs.availableBytes
        val externalUsedSize = externalTotalSize - externalAvailableSize

        return StorageDeviceHolder(
            DocumentHolder(DocumentFileCompat.fromFile(context, externalStorageDir)!!),
            context.getString(R.string.root_dir),
            externalTotalSize,
            externalUsedSize,
            ROOT
        )
    }

    fun getPrimaryInternalStorage(context: Context): StorageDeviceHolder {
        val externalStorageDir = Environment.getExternalStorageDirectory()

        val externalStatFs = StatFs(externalStorageDir.absolutePath)

        val externalTotalSize = externalStatFs.totalBytes
        val externalAvailableSize = externalStatFs.availableBytes
        val externalUsedSize = externalTotalSize - externalAvailableSize

        return StorageDeviceHolder(
            DocumentHolder(DocumentFile.fromFile(externalStorageDir)),
            context.getString(R.string.internal_storage),
            externalTotalSize,
            externalUsedSize,
            INTERNAL_STORAGE
        )
    }

    private fun getExternalStorageDirectories(context: Context): List<File> {
        val directories = mutableListOf<File>()

        // I'm sure that there are better ways to do this :)
        ContextCompat.getExternalFilesDirs(context, null).forEach { directory ->
            val volume = directory?.parentFile?.parentFile?.parentFile?.parentFile
            if (volume != null && volume.exists()) {
                directories.add(volume)
            }
        }

        return directories
    }

    fun getDocumentFiles(): ArrayList<DocumentHolder> {
        val documentFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA
        )

        val documentMimeTypes = arrayOf(
            "application/pdf", // PDF
            "application/msword", // DOC
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
            "application/vnd.ms-excel", // XLS
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
            "application/vnd.ms-powerpoint", // PPT
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" // PPTX
        )

        val selection =
            documentMimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
        val selectionArgs = documentMimeTypes.toCollection(ArrayList()).toTypedArray()

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                documentFiles.add(DocumentHolder.fromFile(File(path)))
            }
        }

        return documentFiles
    }

    fun getArchiveFiles(): ArrayList<DocumentHolder> {
        val archiveFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA
        )

        val archiveMimeTypes = arrayOf(
            "application/zip", // ZIP
            "application/x-rar-compressed", // RAR
            "application/x-tar", // TAR
            "application/gzip", // GZIP
            "application/x-7z-compressed" // 7Z
        )

        val selection =
            archiveMimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
        val selectionArgs = archiveMimeTypes.toList().toTypedArray()

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                archiveFiles.add(DocumentHolder.fromFile(File(path)))
            }
        }

        return archiveFiles
    }

    fun getImageFiles(): ArrayList<DocumentHolder> {
        val imageFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media.DATA,
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                imageFiles.add(DocumentHolder.fromFile(File(path)))
            }
        }

        return imageFiles
    }

    fun getVideoFiles(): ArrayList<DocumentHolder> {
        val videoFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                videoFiles.add(DocumentHolder.fromFile(File(path)))
            }
        }

        return videoFiles
    }

    fun getAudioFiles(): ArrayList<DocumentHolder> {
        val audioFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                audioFiles.add(DocumentHolder.fromFile(File(path)))
            }
        }

        return audioFiles
    }

    fun getRecentFiles(
        recentHours: Int = 48,
        limit: Int = 25
    ): ArrayList<DocumentHolder> {
        val recentFiles = ArrayList<DocumentHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val time = (System.currentTimeMillis() / 1000) - (recentHours * 60 * 60)

        val selection = "${MediaStore.Files.FileColumns.DATE_MODIFIED} >= ?"
        val selectionArgs = arrayOf(time.toString())

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndexPath = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext() && recentFiles.size < limit) {
                val filePath = it.getString(columnIndexPath)
                val file = File(filePath)
                if (file.isFile) {
                    recentFiles.add(DocumentHolder.fromFile(file))
                }
            }
        }

        return recentFiles
    }
}