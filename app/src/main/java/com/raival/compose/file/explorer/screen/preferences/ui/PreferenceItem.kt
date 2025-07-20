package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.common.emptyString

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(start = 16.dp, end = 24.dp)
            .heightIn(min = if (supportingText == null) 56.dp else 72.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingContent?.invoke()
        Column(
            Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            label.invoke()
            supportingText?.invoke()
        }
        trailingContent?.invoke()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    label: String,
    supportingText: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null
) {
    PreferenceItem(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 16.sp
            )
        },
        supportingText = if (supportingText.isEmpty()) null else {
            {
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = supportingText,
                    fontSize = 14.sp
                )
            }
        },
        trailingContent = trailingContent
    )
}

@Composable
fun PreferenceItem(
    modifier: Modifier = Modifier,
    label: String,
    supportingText: String? = null,
    icon: ImageVector,
    switchState: Boolean,
    onSwitchChange: (switched: Boolean) -> Unit
) {
    var switch by remember { mutableStateOf(switchState) }

    PreferenceItem(
        label = label,
        supportingText = supportingText ?: emptyString,
        icon = icon,
        modifier = modifier,
        trailingContent = {
            Switch(
                checked = switch,
                onCheckedChange = {
                    switch = it
                    onSwitchChange(it)
                }
            )
        },
        onClick = {
            switch = !switch
            onSwitchChange(switch)
        }
    )
}