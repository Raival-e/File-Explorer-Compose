package com.raival.compose.file.explorer.screen.main.tab.files.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.block
import com.raival.compose.file.explorer.common.extension.copyToClipboard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilePropertiesRow(
    title: String,
    value: String,
    canCopy: Boolean = false
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.alpha(0.75f),
            text = title,
            fontSize = 14.sp,
        )
        Text(
            modifier = Modifier
                .block(RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        if (canCopy) {
                            value.copyToClipboard()
                            globalClass.showMsg(R.string.copied_to_clipboard)
                        }
                    }
                )
                .fillMaxWidth()
                .padding(8.dp),
            text = value,
            fontSize = 14.sp
        )
    }
}