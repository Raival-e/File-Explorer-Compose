package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.block
import com.raival.compose.file.explorer.common.copyToClipboard
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.search.SearchManager
import com.raival.compose.file.explorer.screen.main.tab.files.search.SearchOptions
import com.raival.compose.file.explorer.screen.main.tab.files.search.SearchResult
import com.raival.compose.file.explorer.screen.main.tab.files.ui.FileItemRow

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    val searchManager = globalClass.searchManager

    if (show) {
        val context = LocalContext.current
        val useDarkIcons = !isSystemInDarkTheme()
        var showAdvancedOptions by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false,
                usePlatformDefaultWidth = false
            )
        ) {
            // Status bar color fix
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
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(0.dp)
                    )
            ) {
                // Search Header
                SearchHeader(
                    searchManager = searchManager,
                    onBackClick = onDismissRequest,
                    onSearchClick = { searchManager.startSearch(tab) },
                    onAdvancedToggle = { showAdvancedOptions = !showAdvancedOptions }
                )

                // Advanced Options
                AnimatedVisibility(
                    visible = showAdvancedOptions,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    AdvancedOptionsPanel(
                        options = searchManager.searchOptions,
                        onOptionsChange = { searchManager.searchOptions = it }
                    )
                }

                // Search Progress
                AnimatedVisibility(
                    visible = searchManager.isSearching,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SearchProgressPanel(searchManager = searchManager)
                }

                // Results Section
                SearchResultsSection(
                    searchManager = searchManager,
                    tab = tab,
                    context = context,
                    onExpandClick = {
                        searchManager.onExpand()
                        onDismissRequest()
                    },
                    onDismissRequest = onDismissRequest
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(
    searchManager: SearchManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAdvancedToggle: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .block(
                    borderSize = 0.dp,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier
                    .weight(1f),
                value = searchManager.searchQuery,
                onValueChange = { searchManager.searchQuery = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_query),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        if (searchManager.isSearching) {
                            IconButton(
                                onClick = {
                                    if (searchManager.isSearching) {
                                        searchManager.stopSearch()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = null,
                                )
                            }
                        } else {
                            IconButton(onClick = onSearchClick) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() }),
                singleLine = true,
            )

            IconButton(onClick = onAdvancedToggle) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun AdvancedOptionsPanel(
    options: SearchOptions,
    onOptionsChange: (SearchOptions) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.advanced_options),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Space(size = 12.dp)

        // Search Options Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionSwitch(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.ignore_case),
                checked = options.ignoreCase,
                onCheckedChange = { onOptionsChange(options.copy(ignoreCase = it)) },
                icon = Icons.Rounded.TextFields
            )

            OptionSwitch(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.use_regex),
                checked = options.useRegex,
                onCheckedChange = { onOptionsChange(options.copy(useRegex = it)) },
                icon = Icons.Rounded.Code
            )
        }

        Space(size = 8.dp)

        // Search Options Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OptionSwitch(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.by_extension),
                checked = options.searchByExtension,
                onCheckedChange = { onOptionsChange(options.copy(searchByExtension = it)) },
                icon = Icons.Rounded.Extension
            )

            OptionSwitch(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.in_content),
                checked = options.searchInFileContent,
                onCheckedChange = { onOptionsChange(options.copy(searchInFileContent = it)) },
                icon = Icons.Rounded.Description
            )
        }

        Space(size = 12.dp)

        // Max Results Options
        Column {
            Text(
                text = stringResource(
                    R.string.max_results,
                    if (options.maxResults == -1) globalClass.getString(R.string.unlimited) else options.maxResults.toString()
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Space(size = 8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val maxResultsOptions = listOf(10, 100, 1000, 5000, 10000)
                val labels = listOf("10", "100", "1K", "5k", "10K")

                maxResultsOptions.forEachIndexed { index, value ->
                    TextButton(
                        onClick = { onOptionsChange(options.copy(maxResults = value)) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            containerColor = if (options.maxResults == value)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (options.maxResults == value)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (options.maxResults == value) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionSwitch(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.graphicsLayer { scaleX = 0.8f; scaleY = 0.8f },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun SearchProgressPanel(searchManager: SearchManager) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.searching),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            if (searchManager.searchProgress > 0) {
                Text(
                    text = "${(searchManager.searchProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Space(size = 8.dp)

        if (searchManager.searchProgress > -1) {
            LinearProgressIndicator(
                progress = { searchManager.searchProgress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        }


        if (searchManager.currentSearchingFile.isNotEmpty()) {
            Space(size = 8.dp)
            Text(
                text = searchManager.currentSearchingFile,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchManager: SearchManager,
    tab: FilesTab,
    context: android.content.Context,
    onExpandClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        // Results Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(bottom = if (searchManager.searchResults.isEmpty()) 12.dp else 8.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_results, searchManager.searchResults.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            if (searchManager.searchResults.isNotEmpty()) {
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = { searchManager.clearResults() }
                ) {
                    Text(stringResource(R.string.clear))
                }
                if (!searchManager.isSearching) {
                    Space(8.dp)
                    TextButton(onClick = onExpandClick) {
                        Text(stringResource(R.string.expand))
                    }
                }
            }
        }

        // Results List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(
                searchManager.searchResults,
                key = { index, item -> "${item.file.uniquePath}_${item.matchType}_$index" }
            ) { index, searchResult ->
                SearchResultItem(
                    searchResult = searchResult,
                    onItemClick = {
                        if (searchResult.file.isFile()) {
                            tab.openFile(context, searchResult.file)
                        } else {
                            tab.openFolder(searchResult.file, rememberListState = false)
                        }
                    },
                    onLocateClick = {
                        onDismissRequest()
                        globalClass.mainActivityManager.replaceCurrentTabWith(
                            tab = FilesTab(source = searchResult.file)
                        )
                    },
                    onCopyPathClick = {
                        searchResult.file.uniquePath.copyToClipboard()
                        globalClass.showMsg(globalClass.getString(R.string.copied_to_clipboard))
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    searchResult: SearchResult,
    onItemClick: () -> Unit,
    onLocateClick: () -> Unit,
    onCopyPathClick: () -> Unit
) {
    var showMoreOptionsMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = { showMoreOptionsMenu = true }
            ),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(6.dp)
        ) {
            // File item row
            FileItemRow(
                item = searchResult.file,
                fileDetails = if (searchResult.file is LocalFileHolder)
                    searchResult.file.basePath else searchResult.file.uniquePath,
                ignoreSizePreferences = true
            )

            // Content preview for content matches
            if (searchResult.matchType == SearchResult.MatchType.CONTENT &&
                !searchResult.matchedLine.isNullOrEmpty()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.line, searchResult.lineNumber!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = MaterialTheme.colorScheme.surfaceContainer),
                    ) {
                        Text(
                            text = searchResult.matchedLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }
            }
        }

        // Context menu
        DropdownMenu(
            expanded = showMoreOptionsMenu,
            onDismissRequest = { showMoreOptionsMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.locate)) },
                onClick = {
                    showMoreOptionsMenu = false
                    onLocateClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = null
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.copy_path)) },
                onClick = {
                    showMoreOptionsMenu = false
                    onCopyPathClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null
                    )
                }
            )
        }
    }
}