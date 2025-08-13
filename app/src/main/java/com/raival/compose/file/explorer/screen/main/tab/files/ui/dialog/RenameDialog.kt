package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.isValidAsFileName
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.task.RenameTask
import com.raival.compose.file.explorer.screen.main.tab.files.task.RenameTask.Companion.transformFileName
import com.raival.compose.file.explorer.screen.main.tab.files.task.RenameTaskParameters
import kotlinx.coroutines.launch

@Composable
fun RenameDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        if (tab.selectedFiles.size == 1) {
            val target by remember { mutableStateOf(tab.selectedFiles.values.first()) }
            val listContent by remember {
                mutableStateOf(tab.activeFolderContent.map { it.displayName }.toTypedArray())
            }

            var newNameInput by remember { mutableStateOf(target.displayName) }
            var error by remember { mutableStateOf("") }

            LaunchedEffect(newNameInput) {
                error = if (newNameInput.isBlank() || newNameInput == target.displayName) {
                    emptyString
                } else if (!newNameInput.isValidAsFileName()) {
                    globalClass.getString(R.string.invalid_file_name)
                } else if (listContent.contains(newNameInput)) {
                    globalClass.getString(R.string.similar_file_exists)
                } else {
                    emptyString
                }
            }

            Dialog(
                onDismissRequest = onDismissRequest,
            ) {
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.rename),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Space(8.dp)
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = newNameInput,
                            onValueChange = {
                                newNameInput = it
                            },
                            label = { Text(text = stringResource(R.string.new_name)) },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = TextFieldDefaults.colors(
                                errorIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            ),
                            isError = error.isNotEmpty(),
                            supportingText = if (error.isNotEmpty()) {
                                { Text(error) }
                            } else null
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onDismissRequest()
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    tab.scope.launch {
                                        if (newNameInput.isValidAsFileName()) {
                                            val similarFile =
                                                tab.activeFolder.findFile(newNameInput)
                                            if (similarFile == null) {
                                                onDismissRequest()
                                                globalClass.taskManager.addTaskAndRun(
                                                    task = RenameTask(sourceContent = tab.selectedFiles.values.toList()),
                                                    parameters = RenameTaskParameters(
                                                        newName = newNameInput,
                                                        toFind = emptyString,
                                                        toReplace = emptyString,
                                                        useRegex = false
                                                    )
                                                )
                                            } else {
                                                globalClass.showMsg(R.string.similar_file_exists)
                                            }
                                        } else {
                                            globalClass.showMsg(R.string.invalid_file_name)
                                        }
                                    }
                                },
                                enabled = error.isEmpty() && newNameInput.isNotBlank() && newNameInput isNot target.displayName,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.rename),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        } else {
            AdvanceRenameDialog(
                tab = tab,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
fun AdvanceRenameDialog(
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    val useDarkIcons = !isSystemInDarkTheme()
    val originalList =
        remember { tab.selectedFiles.values.map { it.displayName to it }.toTypedArray() }
    val remainingFiles = tab.activeFolderContent.map { it.displayName }
        .filter { !originalList.map { it.first }.contains(it) }.toList()
    val currentList = remember { mutableStateListOf<Pair<String, ContentHolder>>() }
    val conflicts = remember { mutableStateListOf<String>() }
    val suggestions = remember {
        arrayListOf<Pair<String, String>>().apply {
            add("{p}" to globalClass.getString(R.string.name))
            add("{s}" to globalClass.getString(R.string.extension))
            add("{e}" to globalClass.getString(R.string.extension_with_dot))
            add("{t}" to globalClass.getString(R.string.last_modified))
            add("{n}" to globalClass.getString(R.string.number_increment))
            add("{zn}" to globalClass.getString(R.string.zero_number_increment))
        }
    }

    var isReady by remember { mutableStateOf(false) }
    var useRegex by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(TextFieldValue("{p}{e}")) }
    var findInput by remember { mutableStateOf(emptyString) }
    var replaceInput by remember { mutableStateOf(emptyString) }

    LaunchedEffect(Unit) {
        currentList.clear()
        conflicts.clear()
        currentList.addAll(originalList)
    }

    fun preview() {
        conflicts.clear()
        currentList.clear()
        val cumulativeNames = Array(originalList.size) { emptyString }
        originalList.forEachIndexed { index, reference ->
            val lastModified by lazy { reference.second.lastModified }
            val newFileName = reference.first.transformFileName(
                newName = newNameInput.text,
                index = index,
                textToFind = findInput,
                replaceText = replaceInput,
                useRegex = useRegex,
                onLastModified = { lastModified }
            )

            currentList.add(newFileName to reference.second)

            if (cumulativeNames.contains(newFileName) || remainingFiles.contains(newFileName)) {
                conflicts.add(newFileName)
            } else {
                cumulativeNames[index] = newFileName
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val color = MaterialTheme.colorScheme.surfaceContainerHigh
        val systemUiController = rememberSystemUiController()
        DisposableEffect(systemUiController, useDarkIcons) {
            systemUiController.setStatusBarColor(color = color, darkIcons = useDarkIcons)
            onDispose {}
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .statusBarsPadding(),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.batch_rename),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Space(8.dp)
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = newNameInput,
                    onValueChange = { newNameInput = it.also { isReady = false } },
                    label = { Text(text = stringResource(R.string.new_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { showInfo = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        errorIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = suggestions, key = { it.first }) { suggestion ->
                        FilterChip(
                            onClick = {
                                val selection = newNameInput.selection
                                val newText: String
                                val newSelection: TextRange

                                if (selection.collapsed) {
                                    val cursorPosition = selection.start
                                    val text = newNameInput.text
                                    val textBeforeCursor = text.substring(0, cursorPosition)
                                    val textAfterCursor =
                                        text.substring(cursorPosition, text.length)
                                    newText = textBeforeCursor + suggestion.first + textAfterCursor
                                    val newCursorPosition = cursorPosition + suggestion.first.length
                                    newSelection = TextRange(newCursorPosition)
                                } else {
                                    val text = newNameInput.text
                                    val textBeforeSelection = text.substring(0, selection.start)
                                    val textAfterSelection =
                                        text.substring(selection.end, text.length)
                                    newText =
                                        textBeforeSelection + suggestion.first + textAfterSelection
                                    val newCursorPosition =
                                        selection.start + suggestion.first.length
                                    newSelection = TextRange(newCursorPosition)
                                }

                                newNameInput = TextFieldValue(
                                    text = newText,
                                    selection = newSelection
                                )
                                isReady = false
                            },
                            label = {
                                Text(
                                    text = suggestion.second,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = false,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Find & Replace Section
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.find_n_replace),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FilterChip(
                            onClick = { useRegex = !useRegex.also { isReady = false } },
                            label = {
                                Text(
                                    text = stringResource(R.string.regex),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = useRegex,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = findInput,
                            onValueChange = { findInput = it.also { isReady = false } },
                            label = { Text(text = stringResource(R.string.text_to_find)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                errorIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = replaceInput,
                            onValueChange = { replaceInput = it.also { isReady = false } },
                            label = { Text(text = stringResource(R.string.replace_with)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                errorIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // Preview Section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.preview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                items = currentList,
                                key = { index, item -> item.first + item.second.displayName }) { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.second.isFolder)
                                            Icons.Rounded.Folder
                                        else Icons.AutoMirrored.Rounded.InsertDriveFile,
                                        contentDescription = null,
                                        tint = if (conflicts.contains(item.first))
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Space(12.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.second.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (conflicts.contains(item.first))
                                                MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (conflicts.contains(item.first))
                                                    MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.primary
                                            )
                                            Space(8.dp)
                                            Text(
                                                text = item.first,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = if (conflicts.contains(item.first))
                                                    MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                if (index isNot currentList.lastIndex) {
                                    Space(8.dp)
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isReady && conflicts.isEmpty()) {
                                onDismissRequest()
                                globalClass.taskManager.addTaskAndRun(
                                    task = RenameTask(tab.selectedFiles.values.toList()),
                                    parameters = RenameTaskParameters(
                                        newName = newNameInput.text,
                                        toFind = findInput,
                                        toReplace = replaceInput,
                                        useRegex = useRegex
                                    )
                                )
                            } else {
                                preview()
                                isReady = true
                            }
                        },
                        enabled = !isReady || conflicts.isEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isReady) null else ButtonDefaults.outlinedButtonBorder(),
                        colors = if (isReady) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            text = stringResource(if (isReady) R.string.rename else R.string.preview),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = {
                Text(
                    text = stringResource(R.string.syntax),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.batch_rename_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfo = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}