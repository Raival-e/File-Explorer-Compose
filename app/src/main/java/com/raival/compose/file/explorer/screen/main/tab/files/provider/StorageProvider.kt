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
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileSortingPrefs
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_DATE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_NAME
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_SIZE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_TYPE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.EXTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.ROOT
import com.raival.compose.file.explorer.screen.main.tab.home.holder.RecentFile
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

    /**
     * Generic helper function to fetch files based on a list of MIME types.
     */
    private fun getFilesByMimeTypes(
        mimeTypes: Array<String>,
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        if (mimeTypes.isEmpty()) {
            return ArrayList()
        }

        val files = ArrayList<LocalFileHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME, // Required for SORT_BY_NAME
            MediaStore.Files.FileColumns.DATE_MODIFIED, // Required for SORT_BY_DATE
            MediaStore.Files.FileColumns.SIZE         // Required for SORT_BY_SIZE
        )

        // Build the selection clause to match any of the provided MIME types.
        // Creates a clause like: "MIME_TYPE = ? OR MIME_TYPE = ? OR ..."
        val selection =
            mimeTypes.joinToString(" OR ") { "${MediaStore.Files.FileColumns.MIME_TYPE} = ?" }
        val selectionArgs = mimeTypes

        // EFFICIENT SORTING: Build the SQL ORDER BY clause to handle sorting and reversal.
        val sortOrder = when (sortingPrefs?.sortMethod) {
            SORT_BY_NAME -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} ${if (sortingPrefs.reverseSorting) "DESC" else "ASC"}"
            SORT_BY_DATE -> "${MediaStore.Files.FileColumns.DATE_MODIFIED} ${if (sortingPrefs.reverseSorting) "ASC" else "DESC"}"
            SORT_BY_SIZE -> "${MediaStore.Files.FileColumns.SIZE} ${if (sortingPrefs.reverseSorting) "ASC" else "DESC"}"
            else -> "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        }

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                if (!path.isNullOrEmpty()) {
                    files.add(LocalFileHolder(File(path)))
                }
            }
        }

        return files
    }

    /**
     * Gets all document files.
     */
    fun getDocumentFiles(
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        val documentMimeTypes = arrayOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // DOCX
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // XLSX
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation" // PPTX
        )
        return getFilesByMimeTypes(documentMimeTypes, sortingPrefs)
    }

    /**
     * Gets all archive files.
     */
    fun getArchiveFiles(
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        val archiveMimeTypes = arrayOf(
            "application/zip",
            "application/x-rar-compressed",
            "application/x-tar",
            "application/gzip",
            "application/x-7z-compressed"
        )
        return getFilesByMimeTypes(archiveMimeTypes, sortingPrefs)
    }

    /**
     * Generic helper function to fetch media files of a specific type.
     */
    private fun getMediaFiles(
        mediaType: Int,
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        val files = ArrayList<LocalFileHolder>()
        val contentResolver: ContentResolver = globalClass.contentResolver

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME, // Required for SORT_BY_NAME
            MediaStore.Files.FileColumns.DATE_MODIFIED, // Required for SORT_BY_DATE
            MediaStore.Files.FileColumns.SIZE         // Required for SORT_BY_SIZE
        )

        // Filter the results to only the media type we want (images, video, etc.).
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        val selectionArgs = arrayOf(mediaType.toString())

        val sortOrder = when (sortingPrefs?.sortMethod) {
            SORT_BY_NAME -> "${MediaStore.Files.FileColumns.DISPLAY_NAME} ${if (sortingPrefs.reverseSorting) "DESC" else "ASC"}"
            SORT_BY_DATE -> "${MediaStore.Files.FileColumns.DATE_MODIFIED} ${if (sortingPrefs.reverseSorting) "ASC" else "DESC"}"
            SORT_BY_SIZE -> "${MediaStore.Files.FileColumns.SIZE} ${if (sortingPrefs.reverseSorting) "ASC" else "DESC"}"
            SORT_BY_TYPE -> {
                val direction = if (sortingPrefs.reverseSorting) "DESC" else "ASC"
                // This SQL expression extracts the file extension and uses it for sorting.
                // It sorts files without an extension first, then sorts by extension alphabetically.
                """
            CASE
                WHEN INSTR(${MediaStore.Files.FileColumns.DISPLAY_NAME}, '.') = 0 THEN 1
                ELSE 0
            END,
            SUBSTR(${MediaStore.Files.FileColumns.DISPLAY_NAME}, INSTR(${MediaStore.Files.FileColumns.DISPLAY_NAME}, '.') + 1) $direction
            """.trimIndent()
            }

            else -> "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        }

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)

            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                if (!path.isNullOrEmpty()) {
                    files.add(LocalFileHolder(File(path)))
                }
            }
        }

        return files
    }

    /**
     * Gets all image files.
     */
    fun getImageFiles(
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        return getMediaFiles(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, sortingPrefs)
    }

    /**
     * Gets all video files.
     */
    fun getVideoFiles(
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        return getMediaFiles(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, sortingPrefs)
    }

    /**
     * Gets all audio files.
     */
    fun getAudioFiles(
        sortingPrefs: FileSortingPrefs?
    ): ArrayList<LocalFileHolder> {
        return getMediaFiles(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO, sortingPrefs)
    }

    fun getBookmarks() = globalClass.preferencesManager.bookmarks
        .map { LocalFileHolder(File(it)) } as ArrayList<LocalFileHolder>

    fun getRawRecentFiles(
        recentHours: Int = 24 * 5,
        limit: Int = 100
    ): ArrayList<RecentFile> {
        val recentFiles = ArrayList<RecentFile>(limit)
        val contentResolver: ContentResolver = globalClass.contentResolver
        val showHiddenFiles = globalClass.preferencesManager.showHiddenFiles

        val uri: Uri = MediaStore.Files.getContentUri("external")

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        )

        // Build the selection clause and arguments dynamically.
        val selectionClauses = mutableListOf<String>()
        val selectionArgsList = mutableListOf<String>()

        // Filter by modification time.
        val time = (System.currentTimeMillis() / 1000) - (recentHours * 60 * 60)
        selectionClauses.add("${MediaStore.Files.FileColumns.DATE_MODIFIED} >= ?")
        selectionArgsList.add(time.toString())

        // Exclude directories directly in the query.
        // This avoids the expensive file.isFile check inside the loop.
        selectionClauses.add("${MediaStore.MediaColumns.MIME_TYPE} IS NOT NULL")

        // Handle hidden files
        if (!showHiddenFiles) {
            selectionClauses.add("${MediaStore.Files.FileColumns.DISPLAY_NAME} NOT LIKE '.%'")
        }

        // Exclude specified paths at the database level.
        val excludedPaths = globalClass.preferencesManager.excludedPathsFromRecentFiles
        excludedPaths.forEach { excludedPath ->
            selectionClauses.add("${MediaStore.Files.FileColumns.DATA} NOT LIKE ?")
            selectionArgsList.add("$excludedPath%")
        }

        // Exclude files within hidden directories (e.g., /storage/emulated/0/SomeApp/.cache/file.txt)
        val excludeHiddenPaths = globalClass.preferencesManager.removeHiddenPathsFromRecentFiles
        if (excludeHiddenPaths) {
            selectionClauses.add("${MediaStore.Files.FileColumns.DATA} NOT LIKE '%/.%'")
        }

        // Combine all selection clauses.
        val selection = selectionClauses.joinToString(" AND ")
        val selectionArgs = selectionArgsList.toTypedArray()

        // Apply the limit directly to the query URI for Android Q (API 29) and above.
        // This is the most efficient way to limit results.
        val queryUri = uri.buildUpon().apply {
            appendQueryParameter("limit", limit.toString())
        }.build()

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val cursor: Cursor? = contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val columnIndexPath = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val columnLastModified =
                it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val columnName = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

            while (it.moveToNext() && recentFiles.size < limit) {
                val filePath = it.getString(columnIndexPath)
                val name = it.getString(columnName)
                val lastModified = it.getLong(columnLastModified)

                if (filePath != null && name != null) {
                    recentFiles.add(
                        RecentFile(
                            name,
                            filePath,
                            lastModified
                        )
                    )
                }
            }
        }

        return recentFiles
    }

    fun getRecentFiles(
        recentHours: Int = 24 * 5,
        limit: Int = 100
    ): ArrayList<LocalFileHolder> {
        return arrayListOf<LocalFileHolder>().apply {
            addAll(
                getRawRecentFiles(recentHours, limit).map { LocalFileHolder(it.file.file) }
            )
        }
    }

    fun getSearchResult(): ArrayList<ContentHolder> {
        return globalClass.searchManager.searchResults.map { it.file } as ArrayList<ContentHolder>
    }
}