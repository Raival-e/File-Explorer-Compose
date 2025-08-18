package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass

@Composable
fun InfoRow() {
    if (globalClass.preferencesManager.hideToolbar) {
        val state by globalClass.mainActivityManager.state.collectAsState()
        HorizontalDivider(modifier = Modifier, thickness = 1.dp)
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)) {
            Text(
                text = state.subtitle,
                modifier = Modifier
                    .weight(1f)
                    .alpha(0.9f),
                maxLines = 1,
                fontSize = 12.sp
            )
        }
    }
}