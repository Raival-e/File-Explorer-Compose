package com.raival.compose.file.explorer.screen.preferences.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.compose.BottomSheetDialog
import com.raival.compose.file.explorer.common.compose.CheckableItem
import com.raival.compose.file.explorer.common.compose.Space

@Composable
fun SingleChoiceDialog() {
    val dialog = globalClass.preferencesManager.singleChoiceDialog
    if (dialog.show) {
        var currentSelectedChoice by remember { mutableIntStateOf(dialog.selectedChoice) }

        BottomSheetDialog(onDismissRequest = { dialog.dismiss() }) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = dialog.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = dialog.description,
                    fontSize = 14.sp
                )

                Space(size = 8.dp)

                HorizontalDivider()

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    dialog.choices.forEachIndexed { index, choice ->
                        CheckableItem(
                            text = choice,
                            isChecked = index == currentSelectedChoice,
                            icon = null,
                        ) {
                            currentSelectedChoice = index
                            dialog.onSelect(index)
                            dialog.dismiss()
                        }
                    }
                }
            }
        }
    }
}