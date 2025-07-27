package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kevinnzou.compose.swipebox.SwipeBox
import com.kevinnzou.compose.swipebox.SwipeDirection
import com.kevinnzou.compose.swipebox.widget.SwipeIcon
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.trimToLastTwoSegments
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileItemRow
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun BookmarksDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current
        val bookmarks = remember {
            mutableStateListOf<LocalFileHolder>()
        }

        LaunchedEffect(Unit) {
            val originalList = globalClass.preferencesManager.bookmarks
            bookmarks.addAll(
                originalList.map { LocalFileHolder(File(it)) }.filter { it.isValid() }
            )

            if (bookmarks.size isNot originalList.size) {
                globalClass.preferencesManager.bookmarks = bookmarks.map { it.uniquePath }.toSet()
            }
        }

        BottomSheetDialog(
            onDismissRequest = onDismissRequest
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Space(8.dp)
                Text(
                    text = stringResource(R.string.bookmarks),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Space(size = 8.dp)

            AnimatedVisibility(globalClass.preferencesManager.bookmarks.isNotEmpty()) {
                LazyColumn(Modifier.animateContentSize()) {
                    itemsIndexed(
                        items = bookmarks,
                        key = { index, item -> item.uniquePath }
                    ) { index, item ->
                        SwipeBox(
                            modifier = Modifier.fillMaxWidth(),
                            swipeDirection = SwipeDirection.EndToStart,
                            endContentWidth = 60.dp,
                            endContent = { swipeableState, endSwipeProgress ->
                                SwipeIcon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color.White,
                                    background = Color(0xFFFA1E32),
                                    weight = 1f,
                                    iconSize = 20.dp
                                ) {
                                    globalClass.preferencesManager.bookmarks -= item.uniquePath
                                }
                            }
                        ) { _, _, _ ->
                            Modifier
                                .fillMaxWidth()
                            Column(
                                Modifier
                                    .animateItem()
                                    .combinedClickable(
                                        onClick = {
                                            if (item.isFile()) {
                                                tab.openFile(context, item)
                                            } else {
                                                tab.requestNewTab(FilesTab(item))
                                            }
                                            onDismissRequest()
                                        },
                                        onLongClick = { }
                                    )
                            ) {
                                Space(size = 4.dp)
                                FileItemRow(
                                    item = item,
                                    fileDetails = item.uniquePath.trimToLastTwoSegments(),
                                    ignoreSizePreferences = true
                                )
                                Space(size = 4.dp)
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(globalClass.preferencesManager.bookmarks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BookmarkBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Space(16.dp)
                    Text(
                        text = stringResource(R.string.empty),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Space(8.dp)
                    Text(
                        text = stringResource(R.string.no_bookmarks_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}