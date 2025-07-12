package com.raival.compose.file.explorer.screen.viewer.pdf.misc

data class PdfMetadata(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val pages: Int,
)