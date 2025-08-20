package com.raival.compose.file.explorer.screen.preferences.misc

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raival.compose.file.explorer.App.Companion.globalClass
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

val Context.prefDataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

data class PreferenceItem(
    val key: String,
    val value: String,
    val type: String
)

suspend fun exportPreferences(): String {
    val preferences = globalClass.prefDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.first()

    val exportData = preferences.asMap().map { (key, value) ->
        PreferenceItem(
            key = key.name,
            value = when (value) {
                is Set<*> -> Gson().toJson(value.toList())
                else -> value.toString()
            },
            type = when (value) {
                is String -> "string"
                is Int -> "int"
                is Boolean -> "boolean"
                is Float -> "float"
                is Long -> "long"
                is Set<*> -> "stringSet"
                else -> "string"
            }
        )
    }

    return Gson().toJson(exportData)
}

suspend fun importPreferences(jsonData: String) {
    val gson = Gson()
    val type = object : TypeToken<List<PreferenceItem>>() {}.type
    val importData: List<PreferenceItem> = gson.fromJson(jsonData, type)

    globalClass.prefDataStore.edit { preferences ->
        preferences.clear()
        importData.forEach { item ->
            when (item.type) {
                "string" -> preferences[stringPreferencesKey(item.key)] = item.value
                "int" -> preferences[intPreferencesKey(item.key)] = item.value.toInt()
                "boolean" -> preferences[booleanPreferencesKey(item.key)] = item.value.toBoolean()
                "float" -> preferences[floatPreferencesKey(item.key)] = item.value.toFloat()
                "long" -> preferences[longPreferencesKey(item.key)] = item.value.toLong()
                "stringSet" -> {
                    val stringListType = object : TypeToken<List<String>>() {}.type
                    val stringList: List<String> = gson.fromJson(item.value, stringListType)
                    preferences[stringSetPreferencesKey(item.key)] = stringList.toSet()
                }
            }
        }
    }
}