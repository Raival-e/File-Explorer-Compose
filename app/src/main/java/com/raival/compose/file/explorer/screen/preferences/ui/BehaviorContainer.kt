package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString

@Composable
fun BehaviorContainer() {
    val prefs = globalClass.preferencesManager

    Container(title = stringResource(R.string.behavior)) {
        PreferenceItem(
            label = stringResource(R.string.show_files_options_menu_on_long_click),
            supportingText = emptyString,
            icon = Icons.Rounded.TouchApp,
            switchState = prefs.showFileOptionMenuOnLongClick,
            onSwitchChange = { prefs.showFileOptionMenuOnLongClick = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.disable_pull_down_to_refresh),
            supportingText = emptyString,
            icon = Icons.Rounded.Refresh,
            switchState = prefs.disablePullDownToRefresh,
            onSwitchChange = { prefs.disablePullDownToRefresh = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.skip_home_when_tab_closed),
            supportingText = emptyString,
            icon = Icons.Rounded.TouchApp,
            switchState = prefs.skipHomeWhenTabClosed,
            onSwitchChange = { prefs.skipHomeWhenTabClosed = it }
        )
    }
}