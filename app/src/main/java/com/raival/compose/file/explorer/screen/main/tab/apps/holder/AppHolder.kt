package com.raival.compose.file.explorer.screen.main.tab.apps.holder

data class AppHolder(
    val name: String,
    val packageName: String,
    val path: String,
    val versionName: String,
    val versionCode: Int,
    val size: Long,
    val isSystemApp: Boolean
)