package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.CalendarToday
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
    val prefs = globalClass.preferencesManager

    Container(title = stringResource(R.string.appearance)) {
        PreferenceItem(
            label = stringResource(R.string.theme),
            supportingText = when (prefs.theme) {
                ThemePreference.LIGHT.ordinal -> stringResource(R.string.light)
                ThemePreference.DARK.ordinal -> stringResource(R.string.dark)
                else -> stringResource(R.string.follow_system)
            },
            icon = Icons.Rounded.Nightlight,
            onClick = {
                prefs.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.theme),
                    description = globalClass.getString(R.string.select_theme_preference),
                    choices = listOf(
                        globalClass.getString(R.string.light),
                        globalClass.getString(R.string.dark),
                        globalClass.getString(R.string.follow_system)
                    ),
                    selectedChoice = prefs.theme,
                    onSelect = { prefs.theme = it }
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
            switchState = prefs.showBottomBarLabels,
            onSwitchChange = { prefs.showBottomBarLabels = it }
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            thickness = 3.dp
        )

        val commonDateFormat = arrayListOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy",
            "MMM dd, yyyy HH:mm:ss",
            "MMM dd, yyyy",
            "MMMM dd, yyyy",
            "EEE, MMM dd, yyyy",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd"
        )

        PreferenceItem(
            label = stringResource(R.string.date_time_format),
            supportingText = prefs.dateTimeFormat,
            icon = Icons.Rounded.CalendarToday,
            onClick = {
                prefs.singleChoiceDialog.show(
                    title = globalClass.getString(R.string.date_time_format),
                    description = globalClass.getString(R.string.select_date_format),
                    choices = commonDateFormat,
                    selectedChoice = commonDateFormat.indexOf(prefs.dateTimeFormat),
                    onSelect = { prefs.dateTimeFormat = commonDateFormat[it] }
                )
            }
        )
    }
}