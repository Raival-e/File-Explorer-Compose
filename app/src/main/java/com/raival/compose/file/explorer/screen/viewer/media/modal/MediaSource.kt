package com.raival.compose.file.explorer.screen.viewer.media.modal

sealed class MediaSource {
    data object AudioSource : MediaSource()
    data object VideoSource : MediaSource()
    data object UnknownSource : MediaSource()
}