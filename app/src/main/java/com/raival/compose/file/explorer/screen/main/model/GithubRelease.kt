package com.raival.compose.file.explorer.screen.main.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class GithubReleaseAsset(
    @SerializedName("name")
    val name: String,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)

data class GithubRelease(
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("tag_name")
    val tagName: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("published_at")
    val publishedAt: Date,
    @SerializedName("assets")
    val assets: List<GithubReleaseAsset>
)