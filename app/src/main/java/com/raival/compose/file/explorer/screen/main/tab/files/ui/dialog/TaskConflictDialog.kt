package com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.block
import com.raival.compose.file.explorer.common.ui.CheckableText
import com.raival.compose.file.explorer.screen.main.tab.files.task.TaskContentStatus

@Composable
fun TaskConflictDialog() {
    val interceptor = globalClass.taskManager.taskInterceptor
    var applyToOtherConflicts by remember { mutableStateOf(interceptor.hasConflict) }

    if (interceptor.hasConflict) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .block()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = globalClass.getString(R.string.conflict),
                    fontSize = 18.sp
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = interceptor.message,
                    fontSize = 14.sp
                )
                CheckableText(
                    checked = applyToOtherConflicts,
                    onCheckedChange = { applyToOtherConflicts = it }
                ) {
                    Text(text = globalClass.getString(R.string.apply_to_other_conflicts))
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        interceptor.hide()
                    }) {
                        Text(text = globalClass.getString(R.string.cancel))
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = {
                        interceptor.resolve(TaskContentStatus.REPLACE, applyToOtherConflicts)
                    }) {
                        Text(text = globalClass.getString(R.string.replace))
                    }
                    TextButton(onClick = {
                        interceptor.resolve(TaskContentStatus.SKIP, applyToOtherConflicts)
                    }) {
                        Text(text = globalClass.getString(R.string.skip))
                    }
                }
            }
        }
    }
}