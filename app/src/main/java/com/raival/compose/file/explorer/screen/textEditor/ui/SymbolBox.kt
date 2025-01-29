package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SymbolBox(label: String, onClick: () -> Unit, onLongClick: () -> Unit = { }) {
    Box(
        Modifier
            .fillMaxHeight()
            .defaultMinSize(minWidth = 52.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = label
        )
    }
}