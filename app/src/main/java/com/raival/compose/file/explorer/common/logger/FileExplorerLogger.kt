package com.raival.compose.file.explorer.common.logger

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.raival.compose.file.explorer.common.extension.printFullStackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "logs")

data class LogHolder(
    val message: String,
    val timestamp: String,
    val id: String = UUID.randomUUID().toString()
)

class FileExplorerLogger(private val context: Context, private val scope: CoroutineScope) {

    companion object {
        private val ERRORS_KEY = stringPreferencesKey("error_logs")
        private val WARNINGS_KEY = stringPreferencesKey("warning_logs")
        private val INFOS_KEY = stringPreferencesKey("info_logs")
        private const val MAX_ERRORS = 5
        private const val MAX_WARNINGS = 10
        private const val MAX_INFOS = 30
    }

    private val gson = Gson()
    private val logListType = object : com.google.gson.reflect.TypeToken<List<LogHolder>>() {}.type
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // ERROR LOGGING FUNCTIONS
    /**
     * Log a string error message
     */
    fun logError(message: String) {
        val errorLog = LogHolder(
            message = message,
            timestamp = dateFormat.format(Date())
        )
        scope.launch {
            saveError(errorLog)
        }
    }

    /**
     * Log an exception
     */
    fun logError(exception: Throwable) {
        val errorLog = LogHolder(
            message = exception.stackTraceToString(),
            timestamp = dateFormat.format(Date())
        )
        scope.launch {
            saveError(errorLog)
        }
    }

    /**
     * Save error to DataStore, maintaining only the 5 most recent
     */
    private suspend fun saveError(newError: LogHolder) {
        context.dataStore.edit { preferences ->
            val currentErrorsJson = preferences[ERRORS_KEY] ?: "[]"
            val currentErrors = try {
                gson.fromJson<List<LogHolder>>(currentErrorsJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            // Add new error at the beginning and keep only the most recent 5
            val updatedErrors = (listOf(newError) + currentErrors).reversed().takeLast(MAX_ERRORS)

            preferences[ERRORS_KEY] = gson.toJson(updatedErrors)
        }
    }

    /**
     * Get all stored errors as a Flow
     */
    fun getErrors(): Flow<List<LogHolder>> {
        return context.dataStore.data.map { preferences ->
            val errorsJson = preferences[ERRORS_KEY] ?: "[]"
            try {
                gson.fromJson<List<LogHolder>>(errorsJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Clear all stored errors
     */
    suspend fun clearErrors() {
        context.dataStore.edit { preferences ->
            preferences.remove(ERRORS_KEY)
        }
    }

    /**
     * Get the most recent error
     */
    fun getLatestError(): Flow<LogHolder?> {
        return getErrors().map { errors ->
            errors.firstOrNull()
        }
    }

    // WARNING LOGGING FUNCTIONS
    /**
     * Log a string warning message
     */
    fun logWarning(message: String) {
        val warningLog = LogHolder(
            message = message,
            timestamp = dateFormat.format(Date())
        )
        scope.launch {
            saveWarning(warningLog)
        }
    }

    /**
     * Log an exception as warning
     */
    fun logWarning(exception: Throwable) {
        val warningLog = LogHolder(
            message = exception.printFullStackTrace(),
            timestamp = dateFormat.format(Date())
        )
        scope.launch {
            saveWarning(warningLog)
        }
    }

    /**
     * Save warning to DataStore, maintaining only the 10 most recent
     */
    private suspend fun saveWarning(newWarning: LogHolder) {
        context.dataStore.edit { preferences ->
            val currentWarningsJson = preferences[WARNINGS_KEY] ?: "[]"
            val currentWarnings = try {
                gson.fromJson<List<LogHolder>>(currentWarningsJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            // Add new warning at the beginning and keep only the most recent 10
            val updatedWarnings =
                (listOf(newWarning) + currentWarnings).reversed().takeLast(MAX_WARNINGS)

            preferences[WARNINGS_KEY] = gson.toJson(updatedWarnings)
        }
    }

    /**
     * Get all stored warnings as a Flow
     */
    fun getWarnings(): Flow<List<LogHolder>> {
        return context.dataStore.data.map { preferences ->
            val warningsJson = preferences[WARNINGS_KEY] ?: "[]"
            try {
                gson.fromJson<List<LogHolder>>(warningsJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Clear all stored warnings
     */
    suspend fun clearWarnings() {
        context.dataStore.edit { preferences ->
            preferences.remove(WARNINGS_KEY)
        }
    }

    /**
     * Get the most recent warning
     */
    fun getLatestWarning(): Flow<LogHolder?> {
        return getWarnings().map { warnings ->
            warnings.firstOrNull()
        }
    }

    // INFO LOGGING FUNCTIONS
    /**
     * Log a string info message
     */
    fun logInfo(message: String) {
        val infoLog = LogHolder(
            message = message,
            timestamp = dateFormat.format(Date())
        )
        scope.launch {
            saveInfo(infoLog)
        }
    }

    /**
     * Save info to DataStore, maintaining only the 30 most recent
     */
    private suspend fun saveInfo(newInfo: LogHolder) {
        context.dataStore.edit { preferences ->
            val currentInfosJson = preferences[INFOS_KEY] ?: "[]"
            val currentInfos = try {
                gson.fromJson<List<LogHolder>>(currentInfosJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            // Add new info at the beginning and keep only the most recent 30
            val updatedInfos = (listOf(newInfo) + currentInfos).reversed().takeLast(MAX_INFOS)

            preferences[INFOS_KEY] = gson.toJson(updatedInfos)
        }
    }

    /**
     * Get all stored infos as a Flow
     */
    fun getInfos(): Flow<List<LogHolder>> {
        return context.dataStore.data.map { preferences ->
            val infosJson = preferences[INFOS_KEY] ?: "[]"
            try {
                gson.fromJson<List<LogHolder>>(infosJson, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Clear all stored infos
     */
    suspend fun clearInfos() {
        context.dataStore.edit { preferences ->
            preferences.remove(INFOS_KEY)
        }
    }

    /**
     * Get the most recent info
     */
    fun getLatestInfo(): Flow<LogHolder?> {
        return getInfos().map { infos ->
            infos.firstOrNull()
        }
    }

    // UTILITY FUNCTIONS
    /**
     * Clear all logs (errors, warnings, and infos)
     */
    suspend fun clearAllLogs() {
        context.dataStore.edit { preferences ->
            preferences.remove(ERRORS_KEY)
            preferences.remove(WARNINGS_KEY)
            preferences.remove(INFOS_KEY)
        }
    }

    /**
     * Get total count of all logs
     */
    fun getTotalLogCount(): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            val errorCount = try {
                val errorsJson = preferences[ERRORS_KEY] ?: "[]"
                gson.fromJson<List<LogHolder>>(errorsJson, logListType)?.size ?: 0
            } catch (_: Exception) {
                0
            }

            val warningCount = try {
                val warningsJson = preferences[WARNINGS_KEY] ?: "[]"
                gson.fromJson<List<LogHolder>>(warningsJson, logListType)?.size ?: 0
            } catch (_: Exception) {
                0
            }

            val infoCount = try {
                val infosJson = preferences[INFOS_KEY] ?: "[]"
                gson.fromJson<List<LogHolder>>(infosJson, logListType)?.size ?: 0
            } catch (_: Exception) {
                0
            }

            errorCount + warningCount + infoCount
        }
    }
}