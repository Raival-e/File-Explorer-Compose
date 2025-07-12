package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.isValidAsFileName
import com.raival.compose.file.explorer.common.ui.CheckableText
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CreateNewFileDialog(tab: FilesTab) {
    if (tab.showCreateNewFileDialog) {
        var isOpenFileDirectly by remember { mutableStateOf(false) }
        val listContent by remember(tab.activeFolderContent) {
            mutableStateOf(tab.activeFolderContent.map { it.displayName }.toTypedArray())
        }
        var newNameInput by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        LaunchedEffect(newNameInput) {
            error = if (newNameInput.isBlank()) {
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
            onDismissRequest = { tab.showCreateNewFileDialog = false },
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
                            text = stringResource(R.string.create_new),
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
                        label = { Text(text = stringResource(R.string.name)) },
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

                    CheckableText(
                        checked = isOpenFileDirectly,
                        onCheckedChange = { isOpenFileDirectly = it },
                    ) {
                        Text(stringResource(R.string.open_created_folder))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (newNameInput.isValidAsFileName()) {
                                    val similarFile = tab.activeFolder.findFile(newNameInput)
                                    if (similarFile == null) {
                                        tab.showCreateNewFileDialog = false
                                        tab.isLoading = true
                                        CoroutineScope(Dispatchers.IO).launch {
                                            tab.activeFolder.createSubFile(newNameInput) { newFile ->
                                                tab.isLoading = false
                                                if (newFile == null) {
                                                    globalClass.showMsg(R.string.failed_to_create_file)
                                                } else {
                                                    tab.onNewFileCreated(newFile)
                                                }
                                            }
                                        }
                                    } else {
                                        globalClass.showMsg(R.string.similar_file_exists)
                                    }
                                } else {
                                    globalClass.showMsg(R.string.invalid_file_name)
                                }
                            },
                            enabled = error.isEmpty() && newNameInput.isNotBlank(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.file),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (newNameInput.isValidAsFileName()) {
                                    val similarFile = tab.activeFolder.findFile(newNameInput)
                                    if (similarFile == null) {
                                        tab.showCreateNewFileDialog = false
                                        tab.isLoading = true
                                        CoroutineScope(Dispatchers.IO).launch {
                                            tab.activeFolder.createSubFolder(newNameInput) { newFile ->
                                                tab.isLoading = false
                                                if (newFile == null) {
                                                    globalClass.showMsg(R.string.failed_to_create_folder)
                                                } else {
                                                    tab.onNewFileCreated(
                                                        newFile,
                                                        isOpenFileDirectly
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        globalClass.showMsg(R.string.similar_file_exists)
                                    }
                                } else {
                                    globalClass.showMsg(R.string.invalid_folder_name)
                                }
                            },
                            enabled = error.isEmpty() && newNameInput.isNotBlank(),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.folder),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}