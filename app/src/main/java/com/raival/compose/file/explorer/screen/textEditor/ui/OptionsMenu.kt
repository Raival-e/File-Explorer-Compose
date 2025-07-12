package com.raival.compose.file.explorer.screen.textEditor.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.rounded.WrapText
import androidx.compose.material.icons.rounded.AirlineStops
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SpaceBar
import androidx.compose.material.icons.rounded.TextRotationNone
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.component.Magnifier
import me.saket.cascade.CascadeDropdownMenu

@Composable
fun OptionsMenu(expanded: Boolean, codeEditor: CodeEditor, onDismissRequest: () -> Unit) {
    val textEditorManager = globalClass.textEditorManager
    val preferences = globalClass.preferencesManager.textEditorPrefs

    CascadeDropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() },
        fixedWidth = 250.dp
    ) {
        androidx.compose.material3.DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.search)) },
            onClick = {
                textEditorManager.toggleSearchPanel(codeEditor = codeEditor)
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Rounded.Search, null) }
        )

        if (textEditorManager.canFormatFile) {
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(text = stringResource(R.string.format)) },
                onClick = {
                    codeEditor.formatCodeAsync()
                    onDismissRequest()
                },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.FormatAlignLeft, null) }
            )
        }

        androidx.compose.material3.DropdownMenuItem(
            text = { Text(text = stringResource(R.string.word_wrap)) },
            onClick = {
                preferences.wordWrap = (!preferences.wordWrap).also { newValue ->
                    codeEditor.isWordwrap = newValue
                    onDismissRequest()
                }
            },
            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.WrapText, null) },
            trailingIcon = {
                Checkbox(preferences.wordWrap, {
                    preferences.wordWrap = (!preferences.wordWrap).also { newValue ->
                        codeEditor.isWordwrap = newValue
                        onDismissRequest()
                    }
                })
            }
        )

        androidx.compose.material3.DropdownMenuItem(
            text = { Text(text = stringResource(R.string.read_only)) },
            onClick = {
                preferences.readOnly = (!preferences.readOnly).also { newValue ->
                    codeEditor.editable = !newValue
                    onDismissRequest()
                }
            },
            leadingIcon = { Icon(Icons.Rounded.RemoveRedEye, null) },
            trailingIcon = {
                Checkbox(preferences.readOnly, {
                    preferences.readOnly = (!preferences.readOnly).also { newValue ->
                        codeEditor.editable = !newValue
                        onDismissRequest()
                    }
                })
            }
        )

        androidx.compose.material3.DropdownMenuItem(
            text = { Text(text = stringResource(R.string.jump_to_line)) },
            onClick = {
                textEditorManager.showJumpToPositionDialog = true
                onDismissRequest()
            },
            leadingIcon = { Icon(Icons.Rounded.AirlineStops, null) }
        )

        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.preferences)) },
            leadingIcon = { Icon(Icons.Rounded.Settings, null) },
            children = {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.use_icu_selection)) },
                    onClick = {
                        preferences.useICULibToSelectWords =
                            (!preferences.useICULibToSelectWords).also { newValue ->
                                codeEditor.props.useICULibToSelectWords = newValue
                            }
                    },
                    leadingIcon = { Icon(Icons.Rounded.TextRotationNone, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.useICULibToSelectWords,
                            onCheckedChange = {
                                preferences.useICULibToSelectWords =
                                    !preferences.useICULibToSelectWords.also { newValue ->
                                        codeEditor.props.useICULibToSelectWords = newValue
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.pin_numbers_line)) },
                    onClick = {
                        preferences.pinLineNumber =
                            (!preferences.pinLineNumber).also { newValue ->
                                codeEditor.setPinLineNumber(newValue)
                            }
                    },
                    leadingIcon = { Icon(Icons.Rounded.Numbers, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.pinLineNumber,
                            onCheckedChange = {
                                preferences.pinLineNumber =
                                    (!preferences.pinLineNumber).also { newValue ->
                                        codeEditor.setPinLineNumber(newValue)
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.auto_symbol_pair)) },
                    onClick = {
                        preferences.symbolPairAutoCompletion =
                            (!preferences.symbolPairAutoCompletion).also { newValue ->
                                codeEditor.props.symbolPairAutoCompletion = newValue
                            }
                    },
                    leadingIcon = { Icon(Icons.Rounded.Code, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.symbolPairAutoCompletion,
                            onCheckedChange = {
                                preferences.symbolPairAutoCompletion =
                                    (!preferences.symbolPairAutoCompletion).also { newValue ->
                                        codeEditor.props.symbolPairAutoCompletion = newValue
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.delete_empty_lines)) },
                    onClick = {
                        preferences.deleteEmptyLineFast =
                            (!preferences.deleteEmptyLineFast).also { newValue ->
                                codeEditor.props.deleteEmptyLineFast = newValue
                            }
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Backspace, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.deleteEmptyLineFast,
                            onCheckedChange = {
                                preferences.deleteEmptyLineFast =
                                    (!preferences.deleteEmptyLineFast).also { newValue ->
                                        codeEditor.props.deleteEmptyLineFast = newValue
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.delete_tabs)) },
                    onClick = {
                        preferences.deleteMultiSpaces =
                            (!preferences.deleteMultiSpaces).also { newValue ->
                                codeEditor.props.deleteMultiSpaces = if (newValue) -1 else 1
                            }
                    },
                    leadingIcon = { Icon(Icons.Rounded.SpaceBar, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.deleteMultiSpaces,
                            onCheckedChange = {
                                preferences.deleteMultiSpaces =
                                    (!preferences.deleteMultiSpaces).also { newValue ->
                                        codeEditor.props.deleteMultiSpaces = if (newValue) -1 else 1
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.auto_indentation)) },
                    onClick = {
                        preferences.autoIndent = (!preferences.autoIndent).also { newValue ->
                            codeEditor.props.autoIndent = newValue
                        }
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Rounded.FormatIndentIncrease, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.autoIndent,
                            onCheckedChange = {
                                preferences.autoIndent =
                                    (!preferences.autoIndent).also { newValue ->
                                        codeEditor.props.autoIndent = newValue
                                    }
                            }
                        )
                    }
                )

                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.magnifier)) },
                    onClick = {
                        preferences.enableMagnifier =
                            (!preferences.enableMagnifier).also { newValue ->
                                codeEditor.getComponent(Magnifier::class.java).isEnabled =
                                    newValue
                            }
                    },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = {
                        Checkbox(
                            checked = preferences.enableMagnifier,
                            onCheckedChange = {
                                preferences.enableMagnifier =
                                    (!preferences.enableMagnifier).also { newValue ->
                                        codeEditor.getComponent(Magnifier::class.java).isEnabled =
                                            newValue
                                    }
                            }
                        )
                    }
                )
            }
        )
    }
}