package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.common.compose.Space

@Composable
fun FileOption(
    icon: ImageVector,
    text: String,
    highlight: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(21.dp),
            imageVector = icon,
            tint = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight,
            contentDescription = null
        )
        Space(size = 12.dp)
        Text(
            text = text,
            color = if (highlight == Color.Unspecified) MaterialTheme.colorScheme.onSurface else highlight
        )
    }
}