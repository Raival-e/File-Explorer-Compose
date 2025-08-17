package com.raival.compose.file.explorer.screen.main.tab.files.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.coil.canUseCoil
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.FontSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.IconSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListFontSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListIconSize
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSizeMap.getFileListSpace

@Composable
fun FileItemRow(
    item: ContentHolder,
    fileDetails: String,
    namePrefix: String = emptyString,
    ignoreSizePreferences: Boolean = false,
    onFileIconClick: (() -> Unit)? = null
) {
    ItemRow(
        title = namePrefix + item.displayName,
        subtitle = fileDetails,
        icon = {
            FileIcon(
                contentHolder = item,
                ignoreSizePreferences = ignoreSizePreferences,
                onClickListener = onFileIconClick
            )
        },
        ignoreSizePreferences = ignoreSizePreferences
    )
}

@Composable
fun ItemRow(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit = { },
    ignoreSizePreferences: Boolean = false,
    onItemClick: (() -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .then(if (onItemClick != null) Modifier.clickable { onItemClick() } else Modifier)) {

        Space(size = if (ignoreSizePreferences) 4.dp else getFileListSpace().dp)

        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()

            Space(size = 8.dp)

            Column(
                Modifier.weight(1f)
            ) {
                val fontSize = if (ignoreSizePreferences) FontSize.MEDIUM else getFileListFontSize()

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

        Space(size = if (ignoreSizePreferences) 4.dp else getFileListSpace().dp)
    }
}


@Composable
fun FileIcon(
    contentHolder: ContentHolder,
    ignoreSizePreferences: Boolean = false,
    onClickListener: (() -> Unit)? = null
) {
    val iconSize = if (ignoreSizePreferences) IconSize.MEDIUM else getFileListIconSize()
    Box(
        modifier = Modifier
            .size(iconSize.dp)
            .clip(RoundedCornerShape(4.dp))
            .then(if (onClickListener != null) Modifier.clickable { onClickListener() } else Modifier)
            .graphicsLayer { alpha = if (contentHolder.isHidden()) 0.4f else 1f },
    ) {
        var useCoil by remember(contentHolder.uid) {
            mutableStateOf(canUseCoil(contentHolder))
        }

        if (useCoil) {
            AsyncImage(
                modifier = Modifier.size(iconSize.dp),
                model = ImageRequest
                    .Builder(globalClass)
                    .data(contentHolder)
                    .build(),
                filterQuality = FilterQuality.Low,
                contentScale = ContentScale.Fit,
                contentDescription = null,
                onError = { useCoil = false }
            )
        } else {
            FileContentIcon(contentHolder)
        }
    }
}

@SuppressLint("CheckResult", "UseCompatLoadingForDrawables")
@Composable
fun ItemRowIcon(
    icon: Any?,
    alpha: Float = 1f,
    onClickListener: (() -> Unit)? = null,
    ignoreSizePreferences: Boolean = false,
    placeholder: Int,
) {
    val iconSize = if (ignoreSizePreferences) IconSize.MEDIUM else getFileListIconSize()

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