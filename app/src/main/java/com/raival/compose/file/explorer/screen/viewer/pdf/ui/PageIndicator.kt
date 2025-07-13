package com.raival.compose.file.explorer.screen.viewer.pdf.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicator(
    pageNumber: String,
    isPressed: Boolean,
    totalPages: Int,
) {
    Card(
        modifier = Modifier
            .padding(start = 4.dp)
            .alpha(0.8f),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed)
                colorScheme.primaryContainer
            else
                colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pageNumber,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPressed)
                    colorScheme.onPrimaryContainer
                else
                    colorScheme.onSurface
            )
            Text(
                text = "of $totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPressed)
                    colorScheme.onPrimaryContainer
                else
                    colorScheme.onSurfaceVariant
            )
        }
    }
}