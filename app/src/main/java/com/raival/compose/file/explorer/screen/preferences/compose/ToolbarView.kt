package com.raival.compose.file.explorer.screen.preferences.compose

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarView() {
    val onBackPressDispatcher = LocalOnBackPressedDispatcherOwner.current!!.onBackPressedDispatcher

    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.preferences))
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressDispatcher.onBackPressed() }) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}