package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FilePropertiesDialog(tab: FilesTab) {
    if (tab.showFileProperties) {
        val targetFiles by remember {
            mutableStateOf(tab.selectedFiles.map { it.value }.toList())
        }

        val totalFiles by remember {
            mutableIntStateOf(targetFiles.size)
        }

        val selectedFilesCount by remember {
            mutableIntStateOf(targetFiles.count { it.isFile })
        }

        val selectedFoldersCount by remember {
            mutableIntStateOf(targetFiles.count { it.isFolder })
        }

        var filesCount by remember {
            mutableIntStateOf(0)
        }

        var foldersCount by remember {
            mutableIntStateOf(0)
        }

        var emptyFoldersCount by remember {
            mutableIntStateOf(0)
        }

        var totalSize by remember {
            mutableLongStateOf(0L)
        }

        val countingThread = rememberCoroutineScope { Dispatchers.IO }

        val scrollState = rememberScrollState()

        LaunchedEffect(targetFiles) {
            countingThread.launch {
                targetFiles.forEach {
                    it.analyze(
                        onCountFile = {
                            filesCount++
                        },
                        onCountFolder = { isEmpty ->
                            foldersCount++
                            if (isEmpty) emptyFoldersCount++
                        },
                        onCountSize = { size ->
                            totalSize += size
                        }
                    )
                }
            }
        }

        BottomSheetDialog(
            onDismissRequest = { tab.showFileProperties = false }
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.properties),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                if (totalFiles == 1) {
                    FilePropertiesRow(
                        title = stringResource(R.string.name).lowercase(),
                        value = targetFiles[0].getName(),
                        canCopy = true
                    )

                    FilePropertiesRow(
                        title = stringResource(R.string.path),
                        value = targetFiles[0].path,
                        canCopy = true
                    )

                    FilePropertiesRow(
                        title = stringResource(R.string.uri),
                        value = targetFiles[0].uri.toString(),
                        canCopy = true
                    )

                    FilePropertiesRow(
                        title = stringResource(R.string.modification_date),
                        value = targetFiles[0].lastModified.toFormattedDate(),
                        canCopy = true
                    )
                }

                FilePropertiesRow(
                    title = stringResource(R.string.parent),
                    value = tab.activeFolder.path,
                    canCopy = true
                )

                FilePropertiesRow(
                    title = stringResource(R.string.size),
                    value = totalSize.toFormattedSize(),
                    canCopy = true
                )

                FilePropertiesRow(
                    title = stringResource(R.string.selected),
                    value = stringResource(
                        id = R.string.selected_files_count,
                        totalFiles,
                        selectedFilesCount,
                        selectedFoldersCount
                    )
                )

                FilePropertiesRow(
                    title = stringResource(R.string.content),
                    value = stringResource(
                        id = R.string.content_count,
                        filesCount + foldersCount,
                        filesCount,
                        foldersCount
                    )
                )
            }
        }
    }
}