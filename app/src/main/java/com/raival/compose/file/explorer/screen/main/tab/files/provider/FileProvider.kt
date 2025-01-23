package com.raival.compose.file.explorer.screen.main.tab.files.provider

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.modal.EXTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.modal.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.modal.ROOT
import com.raival.compose.file.explorer.screen.main.tab.files.modal.StorageDevice
import java.io.File

object FileProvider {
    fun getStorageDevices(context: Context): List<StorageDevice> {
        val storageDevices = mutableListOf<StorageDevice>()

        storageDevices.add(getPrimaryInternalStorage(context))

        val externalDirs = getExternalStorageDirectories(context)

        for (externalDir in externalDirs) {
            if (externalDir.path == Environment.getExternalStorageDirectory().path){
                continue
            }

            val statFs = StatFs(externalDir.absolutePath)
            val totalSize = statFs.totalBytes
            val availableSize = statFs.availableBytes
            val usedSize = totalSize - availableSize

            storageDevices.add(
                StorageDevice(
                    DocumentHolder(DocumentFile.fromFile(externalDir)),
                    "External Storage (${externalDir.name})",
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
            DocumentHolder(DocumentFileCompat.fromFile(context, externalStorageDir)!!),
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
}