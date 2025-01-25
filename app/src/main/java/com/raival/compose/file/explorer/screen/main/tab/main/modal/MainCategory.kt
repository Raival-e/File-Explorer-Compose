package com.raival.compose.file.explorer.screen.main.tab.main.modal

import androidx.compose.ui.graphics.vector.ImageVector

data class MainCategory(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)