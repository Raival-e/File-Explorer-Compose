package com.raival.compose.file.explorer.screen.main.tab.apps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.block
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.apps.AppsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTabContentView(tab: AppsTab) {
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(tab.id) {
        if (tab.appsList.isEmpty()) {
            tab.fetchInstalledApps()
        }
    }

    LaunchedEffect(tab.selectedChoice, tab.sortOption) {
        tab.updateAppsList()
    }

    // App Info Dialog
    if (tab.previewAppDialog != null) {
        AppInfoBottomSheet(
            app = tab.previewAppDialog!!,
            onDismiss = { tab.previewAppDialog = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Apps List
            LazyColumn {
                item {
                    Space(8.dp)
                }

                items(tab.appsList, key = { it.packageName }) { app ->
                    AppListItem(
                        app = app,
                        onClick = { tab.previewAppDialog = app }
                    )
                }

                item {
                    Space(size = 150.dp)
                }
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(!tab.isSearchPanelOpen) {
                FloatingActionButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        tab.isSearchPanelOpen = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                }
            }

            // Filter and Sort Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Segments
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    SegmentedButton(
                        selected = tab.selectedChoice == 0,
                        onClick = { tab.selectedChoice = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        label = { Text(stringResource(R.string.user_apps)) }
                    )
                    SegmentedButton(
                        selected = tab.selectedChoice == 1,
                        onClick = { tab.selectedChoice = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        label = { Text(stringResource(R.string.system_apps)) }
                    )
                    SegmentedButton(
                        selected = tab.selectedChoice == 2,
                        onClick = { tab.selectedChoice = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        label = { Text(stringResource(R.string.all)) }
                    )
                }

                Space(size = 8.dp)

                // Sort Button
                Box {
                    FloatingActionButton(
                        modifier = Modifier.padding(16.dp),
                        onClick = {
                            showSortMenu = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = null
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.name)) },
                            onClick = {
                                tab.sortOption = AppsTab.SortOption.NAME
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.size)) },
                            onClick = {
                                tab.sortOption = AppsTab.SortOption.SIZE
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.install_date)) },
                            onClick = {
                                tab.sortOption = AppsTab.SortOption.INSTALL_DATE
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.update_date)) },
                            onClick = {
                                tab.sortOption = AppsTab.SortOption.UPDATE_DATE
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(tab.isSearchPanelOpen) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
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
                        value = tab.searchQuery,
                        onValueChange = {
                            tab.searchQuery = it
                        },
                        placeholder = {
                            Text(
                                modifier = Modifier.alpha(0.75f),
                                text = stringResource(R.string.search_query),
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = { tab.isSearchPanelOpen = false }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = tab.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (tab.isSearching) {
                                            tab.isSearching = false
                                        } else {
                                            tab.searchQuery = emptyString
                                            tab.isSearching = false
                                            tab.performSearch()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (tab.isSearching) Icons.Rounded.Pause
                                        else Icons.Rounded.Cancel, contentDescription = null
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                tab.performSearch()
                            }
                        )
                    )
                }
            }
        }

        // Loading Indicator
        AnimatedVisibility(
            visible = tab.isLoading || tab.isSearching,
            modifier = Modifier.align(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}