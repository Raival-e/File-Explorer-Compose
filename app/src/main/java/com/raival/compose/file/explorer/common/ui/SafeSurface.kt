package com.raival.compose.file.explorer.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SafeSurface(
    enableStatusBarsPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .windowInsetsPadding(WindowInsets.ime),
        color = colorScheme.surfaceContainerLowest
    ) {
        Column(Modifier.fillMaxSize()) {
            if (enableStatusBarsPadding) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(color = colorScheme.surfaceContainer)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {}
            }
            content()
        }
    }
}