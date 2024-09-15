package com.raival.compose.file.explorer.screen.main.tab.regular.task

import kotlinx.coroutines.CoroutineScope

abstract class RegularTabTaskCallback(val processingThread: CoroutineScope) {
    abstract fun onPrepare(details: RegularTabTaskDetails)

    abstract fun onReport(details: RegularTabTaskDetails)

    abstract fun onComplete(details: RegularTabTaskDetails)

    abstract fun onFailed(details: RegularTabTaskDetails)
}