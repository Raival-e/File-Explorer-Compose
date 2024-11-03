package com.raival.compose.file.explorer.screen.main.tab.regular.provider

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageType
import com.anggrayudi.storage.file.getRelativePath
import com.raival.compose.file.explorer.App
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.EXTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.ROOT
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.StorageDevice
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