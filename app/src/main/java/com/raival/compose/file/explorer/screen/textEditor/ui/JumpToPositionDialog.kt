package com.raival.compose.file.explorer.screen.textEditor.ui

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
import androidx.compose.material3.OutlinedButton
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
import com.raival.compose.file.explorer.common.asCodeEditorCursorCoordinates
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.ui.Space
import io.github.rosemoe.sora.widget.CodeEditor

@Composable
fun JumpToPositionDialog(
    codeEditor: CodeEditor,
    onDismiss: () -> Unit
) {
    var posInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(posInput) {
        val pos = posInput.asCodeEditorCursorCoordinates()
        error = if (posInput.isBlank()) {
            emptyString
        } else if (pos.first < 0 || pos.second < 0) {
            globalClass.getString(R.string.invalid_position)
        } else {
            emptyString
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                        text = stringResource(R.string.jump_to_position),
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
                    value = posInput,
                    onValueChange = {
                        posInput = it
                    },
                    label = { Text(text = stringResource(R.string.jump_to_position_label)) },
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
                            val position = posInput.asCodeEditorCursorCoordinates()
                            if (position.first > -1) {
                                runCatching {
                                    codeEditor.setSelection(position.first - 1, position.second - 1)
                                }.exceptionOrNull()?.let {
                                    globalClass.showMsg(R.string.invalid_position)
                                }
                            } else {
                                globalClass.showMsg(R.string.invalid_position)
                            }
                            onDismiss()
                        },
                        enabled = error.isEmpty() && posInput.isNotBlank(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.go),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}