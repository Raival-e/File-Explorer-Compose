package com.raival.compose.file.explorer.screen.main.tab.files.task

import kotlinx.coroutines.CoroutineScope

abstract class FilesTabTaskCallback(val processingThread: CoroutineScope) {
    abstract fun onPrepare(details: FilesTabTaskDetails)

    abstract fun onReport(details: FilesTabTaskDetails)

    abstract fun onComplete(details: FilesTabTaskDetails)

    abstract fun onFailed(details: FilesTabTaskDetails)
}