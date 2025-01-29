package com.raival.compose.file.explorer.common.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.raival.compose.file.explorer.common.extension.emptyString

data class InputDialogInput(
    val label: String,
    var content: String = emptyString,
    val onValidate: ((content: String) -> String?)? = null
)

data class InputDialogButton(
    val label: String,
    val onClick: (inputs: ArrayList<InputDialogInput>) -> Unit
)

@Composable
fun InputDialog(
    title: String,
    inputs: ArrayList<InputDialogInput>,
    buttons: ArrayList<InputDialogButton>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .block()
                .padding(24.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                fontSize = 18.sp
            )

            inputs.forEach { input ->
                Space(size = 16.dp)

                var text by remember { mutableStateOf(input.content) }
                var error by remember { mutableStateOf<String?>(null) }

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = {
                        text = it
                        input.content = it
                        error = input.onValidate?.invoke(it)
                    },
                    label = { Text(text = input.label) },
                    isError = error != null && error?.isNotEmpty() ?: false,
                    supportingText = { error?.let { Text(text = it) } }
                )
            }

            buttons.forEach { button ->
                Space(size = 8.dp)

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { button.onClick(inputs) }
                ) {
                    Text(text = button.label)
                }
            }
        }
    }
}