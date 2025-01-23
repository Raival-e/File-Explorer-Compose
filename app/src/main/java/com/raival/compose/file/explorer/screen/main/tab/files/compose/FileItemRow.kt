package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.preferences.constant.RegularTabFileListSize
import com.raival.compose.file.explorer.screen.preferences.constant.RegularTabFileListSizeMap

@Composable
fun FileItemRow(
    item: DocumentHolder,
    fileDetails: String,
    namePrefix: String = emptyString,
    onFileIconClick: (() -> Unit)? = null
) {
    ItemRow(
        title = namePrefix + item.getName(),
        subtitle = fileDetails,
        icon = {
            FileIcon(
                documentHolder = item,
                onClickListener = onFileIconClick
            )
        }
    )
}

@Composable
fun ItemRow(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit = { },
    onItemClick: (() -> Unit)? = null,
) {
    val preferencesManager = globalClass.preferencesManager

    Column (Modifier.fillMaxWidth()) {
        Space(
            size = when (preferencesManager.displayPrefs.fileListSize) {
                RegularTabFileListSize.LARGE.ordinal, RegularTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                else -> 4.dp
            }
        )

        Row(
            Modifier
                .padding(horizontal = 12.dp)
                .padding(vertical = 6.dp)
                .then(if (onItemClick != null) Modifier.clickable { onItemClick() } else Modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Space(size = 8.dp)

            Column(
                Modifier.weight(1f)
            ) {
                val fontSize = when (preferencesManager.displayPrefs.fileListSize) {
                    RegularTabFileListSize.SMALL.ordinal -> RegularTabFileListSizeMap.FontSize.SMALL
                    RegularTabFileListSize.MEDIUM.ordinal -> RegularTabFileListSizeMap.FontSize.MEDIUM
                    RegularTabFileListSize.LARGE.ordinal -> RegularTabFileListSizeMap.FontSize.LARGE
                    else -> RegularTabFileListSizeMap.FontSize.EXTRA_LARGE
                }

                Text(
                    text = title,
                    fontSize = fontSize.sp,
                    maxLines = 1,
                    lineHeight = (fontSize + 2).sp,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = subtitle,
                    fontSize = (fontSize - 4).sp,
                    maxLines = 1,
                    lineHeight = (fontSize + 2).sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Space(
            size = when (preferencesManager.displayPrefs.fileListSize) {
                RegularTabFileListSize.LARGE.ordinal, RegularTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                else -> 4.dp
            }
        )
    }


}