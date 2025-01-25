package com.raival.compose.file.explorer.screen.main.tab.home.modal

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeCategory(
    val name: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)