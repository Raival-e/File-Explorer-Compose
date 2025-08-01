package com.raival.compose.file.explorer.screen.main.tab.files.provider

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.RootFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDevice
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.EXTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.ROOT
import java.io.File

object StorageProvider {
    suspend fun getStorageDevices(context: Context): List<StorageDevice> {
        val storageDevices = mutableListOf<StorageDevice>()

        storageDevices.add(getPrimaryInternalStorage(context))

        val externalStoragePairs = getExternalStorageDirectories(context)

        for ((label, externalDir) in externalStoragePairs) {
            if (externalDir.absolutePath == Environment.getExternalStorageDirectory().absolutePath) {
                continue
            }

            val statFs = StatFs(externalDir.absolutePath)
            val totalSize = statFs.totalBytes
            val availableSize = statFs.availableBytes
            val usedSize = totalSize - availableSize
            val storageLabel = label.ifEmpty { "${externalDir.name}" }

            storageDevices.add(
                StorageDevice(
                    LocalFileHolder(externalDir),
                    storageLabel,
                    totalSize,
                    usedSize,
                    EXTERNAL_STORAGE
                )
            )
        }

        storageDevices.add(getRoot(context))

        return storageDevices
    }

    fun getRoot(context: Context): StorageDevice {
        val externalStorageDir = Environment.getRootDirectory()

        val externalStatFs = StatFs(externalStorageDir.absolutePath)

        val externalTotalSize = externalStatFs.totalBytes
        val externalAvailableSize = externalStatFs.availableBytes
        val externalUsedSize = externalTotalSize - externalAvailableSize

        return StorageDevice(
            RootFileHolder(),
            context.getString(R.string.root_dir),
            externalTotalSize,
            externalUsedSize,
            ROOT
        )
    }

    fun getPrimaryInternalStorage(context: Context): StorageDevice {
        val externalStorageDir = Environment.getExternalStorageDirectory()

        val externalStatFs = StatFs(externalStorageDir.absolutePath)

        val externalTotalSize = externalStatFs.totalBytes
        val externalAvailableSize = externalStatFs.availableBytes
        val externalUsedSize = externalTotalSize - externalAvailableSize

        return StorageDevice(
            LocalFileHolder(externalStorageDir),
            context.getString(R.string.internal_storage),
            externalTotalSize,
            externalUsedSize,
            INTERNAL_STORAGE
        )
    }

    private fun getExternalStorageDirectories(context: Context): List<Pair<String, File>> {
        val storageList = mutableListOf<Pair<String, File>>()
        val addedPaths = mutableSetOf<String>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Method 1: Using ContextCompat.getExternalFilesDirs ()
        ContextCompat.getExternalFilesDirs(context, null).forEach { directory ->
            val volume = directory?.parentFile?.parentFile?.parentFile?.parentFile
            if (volume != null && volume.exists() && addedPaths.add(volume.absolutePath)) {
                var label = ""
                try {
                    val storageVolume = storageManager.getStorageVolume(volume)
                    if (storageVolume != null) {
                        label =
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                storageVolume.getDescription(context) ?: volume.name
                            } else {
                                @Suppress("DEPRECATION")
                                storageVolume.getDescription(context) ?: volume.name
                            }
                    }
                } catch (_: Exception) { /* Ignore */
                }
                storageList.add(Pair(label.ifEmpty { volume.name }, volume))
            }
        }

        // Method 2: Parse /proc/mounts for additional storage devices
        try {
            val mountsFile = File("/proc/mounts")
            if (mountsFile.exists()) {
                mountsFile.readLines().forEach { line ->
                    val parts = line.split(" ")
                    if (parts.size >= 2) {
                        val mountPoint = parts[1]
                        val fsType = if (parts.size >= 3) parts[2] else ""

                        // Look for common removable storage mount points and file systems
                        if ((mountPoint.contains("/storage/") || mountPoint.contains("/mnt/")) &&
                            !mountPoint.contains("emulated") &&
                            (fsType in listOf("vfat", "exfat", "ntfs", "ext4", "ext3", "ext2")) &&
                            mountPoint != "/storage/self"
                        ) {

                            val storageDir = File(mountPoint)
                            if (storageDir.exists() && storageDir.canRead() &&
                                addedPaths.add(storageDir.absolutePath)
                            ) {
                                storageList.add(Pair(storageDir.name, storageDir))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore errors reading /proc/mounts
        }

        // Method 3: Check common OTG/USB mount points
        val commonOtgPaths = listOf(
            "/storage/usb",
            "/storage/usbotg",
            "/storage/usb1",
            "/storage/usb2",
            "/mnt/usb",
            "/mnt/usbdisk",
            "/mnt/usb_storage"
        )

        commonOtgPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.canRead() && addedPaths.add(dir.absolutePath)) {
                storageList.add(Pair(dir.name, dir))
            }

            // Also check subdirectories
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { subDir ->
                    if (subDir.isDirectory && subDir.canRead() &&
                        addedPaths.add(subDir.absolutePath)
                    ) {
                        storageList.add(Pair(subDir.name, subDir))
                    }
                }
            }
        }

        return storageList
    }

    fun getDocumentFiles(): ArrayList<LocalFileHolder> {
        val documentFiles = ArrayList<LocalFileHolder>()
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
                documentFiles.add(LocalFileHolder(File(path)))
            }
        }

        return documentFiles
    }

    fun getArchiveFiles(): ArrayList<LocalFileHolder> {
        val archiveFiles = ArrayList<LocalFileHolder>()
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
                archiveFiles.add(LocalFileHolder(File(path)))
            }
        }

        return archiveFiles
    }

    fun getImageFiles(): ArrayList<LocalFileHolder> {
        val imageFiles = ArrayList<LocalFileHolder>()
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
                imageFiles.add(LocalFileHolder(File(path)))
            }
        }

        return imageFiles
    }

    fun getVideoFiles(): ArrayList<LocalFileHolder> {
        val videoFiles = ArrayList<LocalFileHolder>()
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
                videoFiles.add(LocalFileHolder(File(path)))
            }
        }

        return videoFiles
    }

    fun getBookmarks() = globalClass.preferencesManager.bookmarks
        .map { LocalFileHolder(File(it)) } as ArrayList<LocalFileHolder>

    fun getAudioFiles(): ArrayList<LocalFileHolder> {
        val audioFiles = ArrayList<LocalFileHolder>()
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
                audioFiles.add(LocalFileHolder(File(path)))
            }
        }

        return audioFiles
    }

    fun getRecentFiles(
        recentHours: Int = 48,
        limit: Int = 25
    ): ArrayList<LocalFileHolder> {
        val recentFiles = ArrayList<LocalFileHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver
        val showHiddenFiles = globalClass.preferencesManager.showHiddenFiles

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
                if (file.isFile && (showHiddenFiles || !file.name.startsWith("."))) {
                    recentFiles.add(LocalFileHolder(file))
                }
            }
        }

        return recentFiles
    }

    fun getSearchResult(): ArrayList<ContentHolder> {
        return globalClass.searchManager.searchResults.map { it.file } as ArrayList<ContentHolder>
    }
}