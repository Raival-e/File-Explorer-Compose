package com.raival.compose.file.explorer.common.compose

import androidx.compose.runtime.Composable

@Composable
fun Isolate(content: @Composable () -> Unit) { content() }