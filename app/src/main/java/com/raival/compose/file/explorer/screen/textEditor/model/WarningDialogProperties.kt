package com.raival.compose.file.explorer.screen.textEditor.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.raival.compose.file.explorer.common.emptyString

class WarningDialogProperties {
    var showWarningDialog by mutableStateOf(false)
    var title by mutableStateOf(emptyString)
    var message by mutableStateOf(emptyString)
    var confirmText by mutableStateOf(emptyString)
    var dismissText by mutableStateOf(emptyString)
    var onConfirm: () -> Unit = {}
    var onDismiss: () -> Unit = {}
}