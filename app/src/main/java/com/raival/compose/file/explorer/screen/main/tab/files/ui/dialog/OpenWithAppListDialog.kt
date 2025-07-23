package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.common.ui.DynamicSelectTextField
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.OpenWithActivityHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRow
import com.raival.compose.file.explorer.screen.main.tab.files.ui.ItemRowIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OpenWithAppListDialog(
    show: Boolean,
    tab: FilesTab,
    onDismissRequest: () -> Unit
) {
    if (show) {
        val contentHolder = tab.targetFile!! as LocalFileHolder
        val context = LocalContext.current

        val appsList = remember {
            mutableStateListOf<OpenWithActivityHolder>().apply {
                addAll(contentHolder.getAppsHandlingFile())
            }
        }

        val loading = remember {
            mutableStateOf(true)
        }

        val scope = rememberCoroutineScope()

        fun loadActivities(mimeType: String) {
            loading.value = true
            scope.launch(Dispatchers.IO) {
                val list = contentHolder.getAppsHandlingFile(mimeType)
                appsList.clear()
                appsList.addAll(list)
                loading.value = false
            }
        }

        LaunchedEffect(Unit) {
            loadActivities(emptyString)
        }

        BottomSheetDialog(
            onDismissRequest = onDismissRequest
        ) {
            Column(Modifier.animateContentSize()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.open_with),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Space(size = 8.dp)

                DynamicSelectTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    initValue = 0,
                    options = arrayListOf(
                        Pair(contentHolder.mimeType, contentHolder.mimeType),
                        Pair("image", "image/*"),
                        Pair("Video", "video/*"),
                        Pair("Audio", "audio/*"),
                        Pair("Text", "text/plain"),
                        Pair("Any", anyFileType)
                    ),
                    label = stringResource(R.string.mime_type),
                    onValueChangedEvent = {
                        loadActivities(it.second)
                    }
                )

                AnimatedVisibility(
                    visible = loading.value
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                    )
                }

                LazyColumn {
                    itemsIndexed(appsList, key = { index, item -> item.id }) { index, item ->
                        Modifier
                            .fillMaxWidth()
                        Column(
                            Modifier
                                .animateItem()
                                .combinedClickable(
                                    onClick = {
                                        contentHolder.openFileWithPackage(
                                            context,
                                            item.packageName,
                                            item.name
                                        )
                                        onDismissRequest()
                                    }
                                )
                        ) {
                            Space(size = 4.dp)
                            ItemRow(
                                title = item.label,
                                subtitle = item.name,
                                icon = {
                                    ItemRowIcon(
                                        icon = item.icon,
                                        placeholder = R.drawable.unknown_file_extension
                                    )
                                }
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
    }
}