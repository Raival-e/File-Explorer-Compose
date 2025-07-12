package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.holder.StorageDevice
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.misc.StorageDeviceType.ROOT

@Composable
fun StorageDeviceView(
    storageDevice: StorageDevice,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(12.dp)
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(8.dp),
            imageVector = when (storageDevice.type) {
                INTERNAL_STORAGE -> Icons.Rounded.FolderOpen
                ROOT -> Icons.Rounded.Numbers
                else -> Icons.Rounded.SdStorage
            },
            contentDescription = null
        )
        Space(size = 8.dp)
        Column {
            Text(text = storageDevice.title)
            Space(size = 8.dp)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    progress = { storageDevice.usedSize.toFloat() / storageDevice.totalSize },
                    strokeCap = StrokeCap.Round
                )
                Space(size = 8.dp)
                Text(
                    modifier = Modifier
                        .alpha(0.6f)
                        .weight(1f),
                    text = "${storageDevice.usedSize.toFormattedSize()}/${storageDevice.totalSize.toFormattedSize()}",
                    fontSize = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}