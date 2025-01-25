package com.raival.compose.file.explorer.screen.main.tab.files.modal

import com.raival.compose.file.explorer.App.Companion.globalClass

abstract class ContentHolder {
    val uid = globalClass.generateUid()

    abstract fun getName(): String
    abstract fun getContent(): Any
}