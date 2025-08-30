package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Warning
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
            icon = Icons.Rounded.Home,
            switchState = prefs.skipHomeWhenTabClosed,
            onSwitchChange = { prefs.skipHomeWhenTabClosed = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.close_tab_on_back_nav),
            supportingText = emptyString,
            icon = Icons.Rounded.FlipToBack,
            switchState = prefs.closeTabOnBackPress,
            onSwitchChange = { prefs.closeTabOnBackPress = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.remember_last_session),
            supportingText = stringResource(R.string.remember_last_session_desc),
            icon = Icons.Rounded.Restore,
            switchState = prefs.rememberLastSession,
            onSwitchChange = { prefs.rememberLastSession = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.confirm_before_exit),
            supportingText = emptyString,
            icon = Icons.Rounded.Warning,
            switchState = prefs.confirmBeforeAppClose,
            onSwitchChange = { prefs.confirmBeforeAppClose = it }
        )

        PreferenceItem(
            label = stringResource(R.string.use_builtin_viewers),
            supportingText = stringResource(R.string.use_builtin_viewers_desc),
            icon = Icons.Rounded.OpenInBrowser,
            switchState = prefs.useBuiltInViewer,
            onSwitchChange = { prefs.useBuiltInViewer = it }
        )
    }
}