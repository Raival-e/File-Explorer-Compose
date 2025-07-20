package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.preferences.constant.ThemePreference

@Composable
fun AppearanceContainer() {
    val manager = globalClass.preferencesManager
    val appearancePrefs = manager.appearancePrefs

    Container(title = stringResource(R.string.appearance)) {
        PreferenceItem(
            label = stringResource(R.string.theme),
            supportingText = when (appearancePrefs.theme) {
                ThemePreference.LIGHT.ordinal -> stringResource(R.string.light)
                ThemePreference.DARK.ordinal -> stringResource(R.string.dark)
                else -> stringResource(R.string.follow_system)
            },
            icon = Icons.Rounded.Nightlight,
            onClick = {
                manager.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.theme),
                    description = globalClass.getString(R.string.select_theme_preference),
                    choices = listOf(
                        globalClass.getString(R.string.light),
                        globalClass.getString(R.string.dark),
                        globalClass.getString(R.string.follow_system)
                    ),
                    selectedChoice = appearancePrefs.theme,
                    onSelect = { appearancePrefs.theme = it }
                )
            }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        PreferenceItem(
            label = stringResource(R.string.show_bottom_bar_labels),
            supportingText = emptyString,
            icon = Icons.AutoMirrored.Rounded.Label,
            switchState = appearancePrefs.showBottomBarLabels,
            onSwitchChange = { appearancePrefs.showBottomBarLabels = it }
        )
    }
}