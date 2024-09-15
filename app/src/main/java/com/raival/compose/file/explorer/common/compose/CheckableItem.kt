package com.raival.compose.file.explorer.common.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CheckableItem(
    text: String,
    isChecked: Boolean,
    icon: ImageVector?,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = { onClick() },
        trailingIcon = {
            if (isChecked) {
                Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
            }
        },
        leadingIcon = if (icon != null) {
            { Icon(imageVector = icon, contentDescription = null) }
        } else null
    )
}