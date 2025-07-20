package com.raival.compose.file.explorer.screen.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import java.io.File

@Composable
fun JumpToPathDialog(
    show: Boolean,
    onDismiss: () -> Unit = {}
) {
    val mainActivityManager = globalClass.mainActivityManager
    if (show) {
        val context = LocalContext.current
        var jumpToPathText by remember { mutableStateOf(emptyString) }
        var isJumpToPathValid by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
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
                            text = stringResource(R.string.jump_to_path),
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
                        value = jumpToPathText,
                        onValueChange = {
                            jumpToPathText = it
                            isJumpToPathValid = FilesTab.isValidLocalPath(it)
                        },
                        label = { Text(text = stringResource(R.string.destination_path)) },
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        colors = TextFieldDefaults.colors(
                            errorIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss,
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
                                if (isJumpToPathValid) {
                                    mainActivityManager.jumpToFile(
                                        LocalFileHolder(
                                            File(jumpToPathText)
                                        ),
                                        context
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = jumpToPathText.isNotEmpty() && isJumpToPathValid,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isJumpToPathValid) stringResource(R.string.open) else stringResource(
                                    R.string.invalid_path
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}