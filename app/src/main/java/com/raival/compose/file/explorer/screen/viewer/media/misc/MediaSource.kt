package com.raival.compose.file.explorer.screen.viewer.media.misc

sealed class MediaSource {
    data object AudioSource : MediaSource()
    data object VideoSource : MediaSource()
    data object UnknownSource : MediaSource()
}