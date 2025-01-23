package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.compose.block
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab

@Composable
fun TaskDialog(tab: FilesTab) {
    if (tab.taskDialog.showTaskDialog) {
        val taskDialog = tab.taskDialog
        Dialog(
            onDismissRequest = { tab.taskDialog.showTaskDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .block()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = taskDialog.taskDialogTitle,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Space(size = 12.dp)

                Text(
                    modifier = Modifier
                        .alpha(0.75f)
                        .fillMaxWidth(),
                    text = taskDialog.taskDialogSubtitle,
                    fontSize = 16.sp
                )

                if (taskDialog.showTaskDialogProgressbar) {
                    Space(size = 8.dp)
                    if (taskDialog.taskDialogProgress < 0) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = {
                                taskDialog.taskDialogProgress
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Space(size = 4.dp)

                Text(
                    modifier = Modifier
                        .alpha(0.5f)
                        .fillMaxWidth(),
                    text = taskDialog.taskDialogInfo,
                    fontSize = 10.sp
                )
            }
        }
    }
}