package com.raival.compose.file.explorer.common.extension

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

inline fun <reified T> fromJson(json: String?): T? {
    if (json == null) return null

    runCatching {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    return null
}

fun <T> T.toJson(prettyPrint: Boolean = true): String {
    return GsonBuilder().apply {
        if (prettyPrint) setPrettyPrinting()
    }.create().toJson(this)
}