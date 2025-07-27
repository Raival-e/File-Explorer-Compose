package com.raival.compose.file.explorer.screen.main.tab.files.misc

object FileMimeType {
    const val apkFileType = "apk"
    const val isoFileType = "iso"
    const val pdfFileType = "pdf"
    const val sqlFileType = "sql"
    const val svgFileType = "svg"
    const val javaFileType = "java"
    const val kotlinFileType = "kt"
    const val jsonFileType = "json"
    const val markdownFileType = "md"
    const val xmlFileType = "xml"
    const val anyFileType = "*/*"

    @JvmField
    val docFileType = arrayOf("doc", "docx")

    @JvmField
    val excelFileType = arrayOf("xls", "xlsx")

    @JvmField
    val pptFileType = arrayOf("ppt", "pptx")

    @JvmField
    val fontFileType = arrayOf("ttf", "otf")

    @JvmField
    val vectorFileType = arrayOf(
        "svg", "ai", "eps", "pdf", "dxf",
        "wmf", "emf", "cdr", "odg", "swf"
    )

    @JvmField
    val archiveFileType = arrayOf(
        "zip", "7z", "tar", "jar", "gz", "xz", "obb", "rar",
        "iso", "bz2", "tgz", "tbz2", "lz", "lzma"
    )

    @JvmField
    val supportedArchiveFileType = arrayOf("zip", "jar", "apk", "apks")

    @JvmField
    val videoFileType = arrayOf(
        "mp4", "mov", "avi", "mkv", "wmv", "m4v", "3gp",
        "webm", "flv", "mpeg", "mpg", "ogv", "mxf", "vob", "ts"
    )

    @JvmField
    val codeFileType = arrayOf(
        javaFileType, "xml", "py", "css", kotlinFileType, "cs", "xml", jsonFileType, "html",
        "js", "ts", "php", "rb", "pl", "sh", "cpp", "c", "h", "swift", "go", "rs",
        "scala", "sql", "r", "md", "ini", "yaml", "yml"
    )

    @JvmField
    val editableFileType = arrayOf(
        "txt", "text", "log", "dsc", "apt", "rtf", "rtx", "md",
        "csv", "tsv", "ini", "conf", "cfg", "nfo", "json", "xml"
    )

    @JvmField
    val imageFileType = arrayOf(
        "png", "jpeg", "jpg", "heic", "tiff", "gif", "webp", svgFileType, "bmp", "raw"
    )

    @JvmField
    val audioFileType = arrayOf(
        "mp3", "4mp", "aup", "ogg", "3ga", "m4b", "wav", "acc",
        "m4a", "flac", "aac", "wma", "aiff", "amr", "midi", "mid", "opus"
    )

    @JvmField
    val apkBundleFileType = arrayOf("apks", "xapk", "apkm")

}