package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.ui.Space

@Composable
fun RowScope.BottomOptionsBarButton(
    imageVector: ImageVector,
    text: String,
    view: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    val preferencesManager = globalClass.preferencesManager

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!preferencesManager.displayPrefs.showBottomBarLabels) {
            Space(size = 4.dp)
        }

        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = null
        )

        Space(size = 4.dp)

        if (preferencesManager.displayPrefs.showBottomBarLabels) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        view()
    }
}
