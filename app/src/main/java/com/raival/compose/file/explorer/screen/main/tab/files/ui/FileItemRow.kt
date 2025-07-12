package com.raival.compose.file.explorer.screen.main.tab.files.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.preferences.constant.FilesTabFileListSize
import com.raival.compose.file.explorer.screen.preferences.constant.FilesTabFileListSizeMap.getFileListFontSize
import com.raival.compose.file.explorer.screen.preferences.constant.FilesTabFileListSizeMap.getFileListIconSize

@Composable
fun FileItemRow(
    item: ContentHolder,
    fileDetails: String,
    namePrefix: String = emptyString,
    onFileIconClick: (() -> Unit)? = null
) {
    ItemRow(
        title = namePrefix + item.displayName,
        subtitle = fileDetails,
        icon = {
            FileIcon(
                contentHolder = item,
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

    Column(
        Modifier
            .fillMaxWidth()
            .then(if (onItemClick != null) Modifier.clickable { onItemClick() } else Modifier)) {
        Space(
            size = when (preferencesManager.fileListPrefs.itemSize) {
                FilesTabFileListSize.LARGE.ordinal, FilesTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                else -> 4.dp
            }
        )

        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Space(size = 8.dp)

            Column(
                Modifier.weight(1f)
            ) {
                val fontSize = getFileListFontSize()

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
            size = when (preferencesManager.fileListPrefs.itemSize) {
                FilesTabFileListSize.LARGE.ordinal, FilesTabFileListSize.EXTRA_LARGE.ordinal -> 8.dp
                else -> 4.dp
            }
        )
    }
}


@Composable
fun FileIcon(
    contentHolder: ContentHolder,
    onClickListener: (() -> Unit)? = null
) {
    val iconSize = getFileListIconSize()

    if (contentHolder.isFile()) {
        ItemRowIcon(
            icon = contentHolder,
            alpha = if (contentHolder.isHidden()) 0.4f else 1f,
            onClickListener = onClickListener,
            placeholder = contentHolder.iconPlaceholder
        )
    } else {
        Icon(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    onClickListener?.invoke()
                }
                .alpha(if (contentHolder.isHidden()) 0.4f else 1f),
            imageVector = Icons.Rounded.Folder,
            contentDescription = null
        )
    }
}

@SuppressLint("CheckResult", "UseCompatLoadingForDrawables")
@Composable
fun ItemRowIcon(
    icon: Any?,
    alpha: Float = 1f,
    onClickListener: (() -> Unit)? = null,
    placeholder: Int,
) {
    val iconSize = getFileListIconSize()

    val modifier = Modifier
        .size(iconSize.dp)
        .clip(RoundedCornerShape(4.dp))
        .then(if (onClickListener != null) Modifier.clickable { onClickListener() } else Modifier)

    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(globalClass).data(icon).build(),
        filterQuality = FilterQuality.Low,
        error = painterResource(id = placeholder),
        contentScale = ContentScale.Fit,
        alpha = alpha,
        contentDescription = null
    )
}