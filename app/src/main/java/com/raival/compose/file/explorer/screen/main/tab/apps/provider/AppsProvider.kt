package com.raival.compose.file.explorer.screen.main.tab.apps.provider

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.main.tab.apps.holder.AppHolder
import java.io.File
import java.util.Date

fun getInstalledApps(context: Context): List<AppHolder> {
    val packageManager = context.packageManager
    val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    return apps.mapNotNull { appInfo ->
        try {
            createAppHolder(packageManager, appInfo)
        } catch (_: Exception) {
            null // Skip apps that can't be processed
        }
    }
}

private fun createAppHolder(
    packageManager: PackageManager,
    appInfo: ApplicationInfo
): AppHolder {
    val packageInfo = packageManager.getPackageInfo(
        appInfo.packageName,
        PackageManager.GET_PERMISSIONS or PackageManager.GET_META_DATA
    )
    val appFile = File(appInfo.sourceDir)

    val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()
    val category = getCategoryName(appInfo.category)

    return AppHolder(
        name = appInfo.loadLabel(packageManager).toString(),
        packageName = appInfo.packageName,
        path = appInfo.sourceDir,
        versionName = packageInfo.versionName ?: globalClass.getString(R.string.unknown),
        versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        },
        size = appFile.length(),
        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
        installDate = Date(packageInfo.firstInstallTime),
        lastUpdateDate = Date(packageInfo.lastUpdateTime),
        targetSdkVersion = appInfo.targetSdkVersion,
        minSdkVersion =
            appInfo.minSdkVersion,
        permissions = permissions,
        category = category,
        dataDir = appInfo.dataDir,
        uid = appInfo.uid,
        enabled = appInfo.enabled,
        debuggable = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    )
}

private fun getCategoryName(category: Int): String {
    return when (category) {
        ApplicationInfo.CATEGORY_AUDIO -> globalClass.getString(R.string.audio)
        ApplicationInfo.CATEGORY_GAME -> globalClass.getString(R.string.game)
        ApplicationInfo.CATEGORY_IMAGE -> globalClass.getString(R.string.image)
        ApplicationInfo.CATEGORY_MAPS -> globalClass.getString(R.string.maps)
        ApplicationInfo.CATEGORY_NEWS -> globalClass.getString(R.string.news)
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> globalClass.getString(R.string.productivity)
        ApplicationInfo.CATEGORY_SOCIAL -> globalClass.getString(R.string.social)
        ApplicationInfo.CATEGORY_VIDEO -> globalClass.getString(R.string.video)
        else -> emptyString
    }
}