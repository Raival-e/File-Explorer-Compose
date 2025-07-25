package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.block
import com.raival.compose.file.explorer.common.copyToClipboard
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileItemRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val context = LocalContext.current
        var isSearching by remember { mutableStateOf(false) }
        val useDarkIcons = !isSystemInDarkTheme()

        fun search(query: String) {
            if (query.isNotEmpty()) {
                isSearching = true
                tab.searcher.searchResults.clear()

                suspend fun searchInList() {
                    if (!isSearching) return

                    val searchLimit = globalClass
                        .preferencesManager
                        .searchInFilesLimit

                    val isExceedingTheSearchLimit =
                        searchLimit > 0 && tab.searcher.searchResults.size >= searchLimit

                    if (isExceedingTheSearchLimit) return

                    tab.activeFolderContent.forEach {
                        if (!isSearching) return
                        if (it.displayName.contains(query, true)) {
                            tab.searcher.searchResults += it
                            delay(150)
                        }
                    }
                }

                suspend fun searchIn(contentHolder: ContentHolder) {
                    if (!isSearching) return

                    val searchLimit = globalClass
                        .preferencesManager
                        .searchInFilesLimit

                    val isExceedingTheSearchLimit =
                        searchLimit > 0 && tab.searcher.searchResults.size >= searchLimit

                    if (isExceedingTheSearchLimit) return

                    if (contentHolder.isFile()) {
                        if (contentHolder.displayName.contains(query, true)) {
                            if (!isSearching) return
                            tab.searcher.searchResults += contentHolder
                            delay(150)
                        }
                    }

                    if (contentHolder.isFolder) {
                        if (!isSearching) return
                        contentHolder.listContent().forEach {
                            if (!isSearching) return
                            searchIn(it)
                        }
                    }
                }

                tab.scope.launch {
                    if (tab.activeFolder is VirtualFileHolder) {
                        searchInList()
                    } else {
                        searchIn(tab.activeFolder)
                    }

                    isSearching = false
                }
            } else {
                isSearching = false
                tab.searcher.searchResults.clear()
            }
        }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
                usePlatformDefaultWidth = false
            )
        ) {
            // There is a strange artifact that overlaps with the status bar, not sure why it occurs.
            // This code is a workaround to fix it.
            val color = MaterialTheme.colorScheme.surfaceContainerHigh
            val systemUiController = rememberSystemUiController()
            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setStatusBarColor(color = color, darkIcons = useDarkIcons)
                onDispose {}
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .statusBarsPadding()
                    .block(
                        shape = RectangleShape,
                        borderSize = 0.dp
                    )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(56.dp)
                        .block(
                            borderSize = 0.dp,
                            shape = CircleShape
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        value = tab.searcher.searchQuery,
                        onValueChange = {
                            tab.searcher.searchQuery = it
                        },
                        placeholder = {
                            Text(
                                modifier = Modifier.alpha(0.75f),
                                text = stringResource(R.string.search_query),
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = tab.searcher.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (isSearching) {
                                            isSearching = false
                                        } else {
                                            tab.searcher.searchQuery = emptyString
                                            tab.searcher.searchResults.clear()
                                            isSearching = false
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSearching) Icons.Rounded.Pause
                                        else Icons.Rounded.Cancel, contentDescription = null
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                tab.scope.launch {
                                    if (isSearching) {
                                        isSearching = false
                                        delay(200)
                                    }
                                    search(tab.searcher.searchQuery)
                                }
                            }
                        )
                    )
                }

                Text(
                    modifier = Modifier
                        .alpha(0.75f)
                        .padding(horizontal = 16.dp),
                    text = stringResource(R.string.results),
                    fontSize = 14.sp
                )

                Space(size = 12.dp)

                AnimatedVisibility(isSearching) {
                    LinearProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Space(size = 8.dp)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)
                        )
                        .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                ) {
                    LazyColumn {
                        itemsIndexed(
                            tab.searcher.searchResults,
                            key = { index, item -> item.uniquePath }) { index, item ->
                            var showMoreOptionsMenu by remember(item.uniquePath) {
                                mutableStateOf(false)
                            }

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (item.isFile()) {
                                                tab.openFile(context, item)
                                            } else {
                                                tab.openFolder(item, rememberListState = false)
                                            }
                                        },
                                        onLongClick = {
                                            showMoreOptionsMenu = true
                                        }
                                    )
                            ) {
                                Space(size = 4.dp)
                                FileItemRow(
                                    item = item, fileDetails = if (item is LocalFileHolder)
                                        item.basePath else item.uniquePath
                                )
                                Space(size = 4.dp)
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp
                                )
                                DropdownMenu(
                                    expanded = showMoreOptionsMenu,
                                    onDismissRequest = { showMoreOptionsMenu = false }) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = stringResource(R.string.locate))
                                        },
                                        onClick = {
                                            onDismissRequest()
                                            globalClass.mainActivityManager.replaceCurrentTabWith(
                                                tab = FilesTab(
                                                    source = item
                                                )
                                            )
                                            showMoreOptionsMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = stringResource(R.string.copy_path))
                                        },
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            item.uniquePath.copyToClipboard()
                                            globalClass.showMsg(globalClass.getString(R.string.copied_to_clipboard))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}