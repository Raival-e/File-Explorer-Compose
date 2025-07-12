package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.BottomSheetDialog
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider

@Composable
fun NewTabDialog() {
    val mainActivityManager = globalClass.mainActivityManager

    if (mainActivityManager.showNewTabDialog) {
        BottomSheetDialog(onDismissRequest = { mainActivityManager.showNewTabDialog = false }) {
            Column {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(R.string.new_tab),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                StorageProvider.getStorageDevices(globalClass).forEach {
                    StorageDeviceView(storageDevice = it) {
                        globalClass.mainActivityManager.addTabAndSelect(FilesTab(it.contentHolder))
                        mainActivityManager.showNewTabDialog = false
                    }
                    HorizontalDivider()
                }

                HorizontalDivider()

                SimpleNewTabViewItem(
                    title = stringResource(R.string.recycle_bin),
                    imageVector = Icons.Rounded.DeleteSweep
                ) {
                    globalClass.mainActivityManager.addTabAndSelect(FilesTab(globalClass.recycleBinDir))
                    mainActivityManager.showNewTabDialog = false
                }

                HorizontalDivider()

                SimpleNewTabViewItem(
                    title = stringResource(R.string.jump_to_path),
                    imageVector = Icons.Rounded.ArrowOutward
                ) {
                    mainActivityManager.showJumpToPathDialog = true
                    mainActivityManager.showNewTabDialog = false
                }
            }
        }
    }
}