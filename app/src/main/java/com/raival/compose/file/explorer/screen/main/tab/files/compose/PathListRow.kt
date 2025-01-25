package com.raival.compose.file.explorer.screen.main.tab.files.compose

import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.orIf
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PathListRow(tab: FilesTab) {
    val highlightedPathListItemColor = MaterialTheme.colorScheme.primary

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                tab.openFolder(tab.homeDir, false)
            }
        ) {
            Icon(imageVector = Icons.Rounded.Home, contentDescription = null)
        }

        val animationScope = rememberCoroutineScope()
        LaunchedEffect(key1 = tab.currentPathSegments.size) {
            animationScope.launch { tab.currentPathSegmentsListState.scrollToItem(tab.currentPathSegments.size) }
        }

        LazyRow(
            Modifier.weight(1f),
            tab.currentPathSegmentsListState,
        ) {
            itemsIndexed(tab.currentPathSegments, key = { _, it -> it.path }) { index, item ->
                val isHighlighted = index == tab.currentPathSegments.size - 1

                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier
                            .clip(CircleShape)
                            .combinedClickable(
                                onClick = {
                                    tab.openFolder(
                                        item = item,
                                        rememberSelectedFiles = true,
                                    )
                                }
                            )
                            .padding(8.dp)
                            .alpha(0.8f),
                        text = item.getName()
                            .orIf(stringResource(id = R.string.internal_storage)) {
                                item.path == Environment.getExternalStorageDirectory().absolutePath
                            },
                        fontSize = 14.sp,
                        fontWeight = if (isHighlighted) FontWeight.Medium else FontWeight.Normal,
                        color = if (isHighlighted) highlightedPathListItemColor else Color.Unspecified
                    )
                }
            }
        }
    }
}
