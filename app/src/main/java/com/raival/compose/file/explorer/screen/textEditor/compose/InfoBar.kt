package com.raival.compose.file.explorer.screen.textEditor.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.compose.Space

@Composable
fun InfoBar() {
    val textEditorManager = globalClass.textEditorManager
    Row(
        Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(color = colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Space(16.dp)
        Text(
            modifier = Modifier
                .alpha(0.65f)
                .animateContentSize(),
            text = textEditorManager.activitySubtitle,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier
                .alpha(0.65f)
                .animateContentSize(),
            text = "UTF-8",
            fontSize = 10.sp
        )
        Space(16.dp)
    }
}