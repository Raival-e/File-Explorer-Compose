package com.raival.compose.file.explorer.screen.main.tab.apps.provider

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.raival.compose.file.explorer.screen.main.tab.apps.holder.AppHolder
import java.io.File

fun getInstalledApps(context: Context): List<AppHolder> {
    val packageManager = context.packageManager
    val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    return apps.map { appInfo ->
        createAppHolder(packageManager, appInfo)
    }
}

private fun createAppHolder(
    packageManager: PackageManager,
    appInfo: ApplicationInfo
): AppHolder {
    val packageInfo =
        packageManager.getPackageInfo(appInfo.packageName, PackageManager.GET_META_DATA)
    val appFile = File(appInfo.sourceDir)

    return AppHolder(
        name = appInfo.loadLabel(packageManager).toString(),
        packageName = appInfo.packageName,
        path = appInfo.sourceDir,
        versionName = packageInfo.versionName ?: "Unknown",
        versionCode = packageInfo.versionCode,
        size = appFile.length(),
        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    )
}