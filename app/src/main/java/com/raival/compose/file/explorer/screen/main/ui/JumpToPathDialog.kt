package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.common.ui.block
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.DocumentHolder

@Composable
fun JumpToPathDialog() {
    val mainActivityManager = globalClass.mainActivityManager
    if (mainActivityManager.showJumpToPathDialog) {
        val context = LocalContext.current

        var jumpToPathText by remember { mutableStateOf(emptyString) }

        var isJumpToPathValid by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { mainActivityManager.showJumpToPathDialog = false }) {
            Column(
                modifier = Modifier
                    .block()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(R.string.jump_to_path),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Space(size = 12.dp)

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = jumpToPathText,
                    onValueChange = {
                        jumpToPathText = it
                        isJumpToPathValid = FilesTab.isValidPath(it)
                    },
                    label = {
                        Text(text = stringResource(R.string.destination_path))
                    },
                    trailingIcon = {
                        AnimatedVisibility(visible = jumpToPathText.isNotEmpty()) {
                            IconButton(
                                onClick = { jumpToPathText = emptyString }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )

                Space(size = 8.dp)

                Button(
                    onClick = {
                        if (isJumpToPathValid) {
                            mainActivityManager.jumpToFile(
                                DocumentHolder.fromFullPath(jumpToPathText)!!,
                                context
                            )
                            mainActivityManager.showJumpToPathDialog = false
                        }
                    },
                    enabled = isJumpToPathValid
                ) {
                    Text(
                        text = if (isJumpToPathValid) stringResource(R.string.open) else stringResource(
                            R.string.invalid_path
                        )
                    )
                }
            }
        }
    }
}