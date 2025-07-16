package com.raival.compose.file.explorer.screen.main.tab.apps.holder

import java.util.Date

data class AppHolder(
    val name: String,
    val packageName: String,
    val path: String,
    val versionName: String,
    val versionCode: Int,
    val size: Long,
    val isSystemApp: Boolean,
    val installDate: Date,
    val lastUpdateDate: Date,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val permissions: List<String>,
    val category: String,
    val dataDir: String,
    val uid: Int,
    val enabled: Boolean,
    val debuggable: Boolean
)