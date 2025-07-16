package com.raival.compose.file.explorer.screen.main.tab.files.misc

import android.graphics.drawable.Drawable

data class ApkInfo(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: String,
    val icon: Drawable?,
    val size: String,
    val isInstalled: Boolean,
    val installedVersion: String?,
    val minSdkVersion: Int?,
    val targetSdkVersion: Int?,
    val permissions: List<String>,
    val installTime: String?,
    val lastUpdateTime: String?
)