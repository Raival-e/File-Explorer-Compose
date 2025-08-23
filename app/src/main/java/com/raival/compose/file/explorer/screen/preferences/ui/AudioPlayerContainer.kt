package com.raival.compose.file.explorer.screen.preferences.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R

@Composable
fun AudioPlayerContainer() {
    val prefs = globalClass.preferencesManager

    Container(title = stringResource(R.string.title_activity_audio_player)) {
        PreferenceItem(
            label = stringResource(R.string.auto_play_music),
            supportingText = stringResource(R.string.auto_play_music_desc),
            icon = Icons.Rounded.PlayArrow,
            switchState = prefs.autoPlayMusic,
            onSwitchChange = { prefs.autoPlayMusic = it }
        )
    }
}
