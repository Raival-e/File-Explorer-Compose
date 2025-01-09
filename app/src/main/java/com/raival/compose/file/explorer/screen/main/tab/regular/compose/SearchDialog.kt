package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.AlertDialogDefaults
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
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.compose.block
import com.raival.compose.file.explorer.common.extension.copyToClipboard
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.getIndexIf
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchDialog(tab: RegularTab) {
    if (tab.showSearchPenal) {
        val context = LocalContext.current
        var isSearching by remember { mutableStateOf(false) }

        fun search(query: String) {
            if (query.isNotEmpty()) {
                isSearching = true
                tab.search.searchResults.clear()

                suspend fun searchIn(doc: DocumentHolder) {
                    if (!isSearching) return

                    val searchLimit = globalClass
                        .preferencesManager
                        .generalPrefs
                        .searchInFilesLimit

                    val isExceedingTheSearchLimit =
                        searchLimit > 0 && tab.search.searchResults.size >= searchLimit

                    if (isExceedingTheSearchLimit) return

                    if (doc.isFile()) {
                        if (doc.getFileName().contains(query, true)) {
                            if (!isSearching) return
                            tab.search.searchResults += doc
                            delay(150)
                        }
                    }

                    if (doc.isFolder()) {
                        if (!isSearching) return
                        doc.documentFile.listFiles().forEach {
                            if (!isSearching) return
                            searchIn(DocumentHolder(it))
                        }
                    }
                }

                CoroutineScope(Dispatchers.IO).launch {
                    searchIn(tab.activeFolder)
                    isSearching = false
                }
            } else {
                isSearching = false
                tab.search.searchResults.clear()
            }
        }

        Dialog(
            onDismissRequest = { tab.showSearchPenal = false },
            properties = DialogProperties(
                decorFitsSystemWindows = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            focusedContainerColor = AlertDialogDefaults.containerColor,
                            unfocusedContainerColor = AlertDialogDefaults.containerColor,
                            disabledContainerColor = AlertDialogDefaults.containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        value = tab.search.searchQuery,
                        onValueChange = {
                            tab.search.searchQuery = it
                        },
                        placeholder = {
                            Text(
                                modifier = Modifier.alpha(0.75f),
                                text = stringResource(R.string.search_query),
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = { tab.showSearchPenal = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = tab.search.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (isSearching) {
                                            isSearching = false
                                        } else {
                                            tab.search.searchQuery = emptyString
                                            tab.search.searchResults.clear()
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
                                CoroutineScope(Dispatchers.Default).launch {
                                    if (isSearching) {
                                        isSearching = false
                                        delay(200)
                                    }
                                    search(tab.search.searchQuery)
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
                            tab.search.searchResults,
                            key = { index, item -> item.getPath() }) { index, item ->
                            var showMoreOptionsMenu by remember(item.getPath()) {
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
                                FileItemRow(item = item, fileDetails = item.getBasePath())
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
                                            tab.showSearchPenal = false
                                            if (item.canAccessParent()) {
                                                tab.highlightedFiles.apply {
                                                    clear()
                                                    add(item.getPath())
                                                }
                                                tab.openFolder(item.getParent()!!) {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        tab.getFileListState().scrollToItem(
                                                            tab.activeFolderContent.getIndexIf { getPath() == item.getPath() },
                                                            0
                                                        )
                                                    }
                                                }
                                            }
                                            showMoreOptionsMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = stringResource(R.string.copy_path))
                                        },
                                        onClick = {
                                            showMoreOptionsMenu = false
                                            item.getPath().copyToClipboard()
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