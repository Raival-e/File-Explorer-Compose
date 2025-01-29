package com.raival.compose.file.explorer.screen.viewer.pdf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.viewer.pdf.instance.PdfViewerInstance

@Composable
fun BottomBarView(instance: PdfViewerInstance) {
    Row(
        modifier = Modifier
            .padding(12.dp)
            .background(
                color = colorScheme.surfaceContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.page_count, instance.pageCount),
            maxLines = 2,
            style = typography.titleMedium,
            overflow = TextOverflow.Ellipsis
        )
    }
}