package com.raival.compose.file.explorer.screen.preferences.misc

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.prefDataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")