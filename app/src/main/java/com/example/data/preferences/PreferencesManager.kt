package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lyric_studio_prefs")

class PreferencesManager(private val context: Context) {

    init {
        try {
            val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            if (!sp.contains("language")) {
                sp.edit().putString("language", "fa").apply()
            }
        } catch (_: Exception) {}
    }

    companion object {
        val KEY_THEME = stringPreferencesKey("theme_id")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_FONT_SIZE = floatPreferencesKey("font_size")
        val KEY_LINE_HEIGHT = floatPreferencesKey("line_height")
        val KEY_LETTER_SPACING = floatPreferencesKey("letter_spacing")
        val KEY_AUTOSAVE_ENABLED = booleanPreferencesKey("autosave_enabled")
        val KEY_AUTOSAVE_DEBOUNCE_MS = longPreferencesKey("autosave_debounce_ms")
        val KEY_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val KEY_GRID_VIEW = booleanPreferencesKey("grid_view_enabled")
    }

    val themeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_THEME] ?: "midnight"
        }

    val languageFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_LANGUAGE] ?: "fa"
        }

    val fontSizeFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_FONT_SIZE] ?: 16f
        }

    val lineHeightFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_LINE_HEIGHT] ?: 1.5f
        }

    val letterSpacingFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_LETTER_SPACING] ?: 0f
        }

    val autosaveEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_AUTOSAVE_ENABLED] ?: true
        }

    val autosaveDebounceMsFlow: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_AUTOSAVE_DEBOUNCE_MS] ?: 600L
        }

    val pinHashFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_PIN_HASH] ?: ""
        }

    val appLockEnabledFlow: Flow<Boolean> = pinHashFlow.map { it.isNotBlank() }

    val gridViewFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_GRID_VIEW] ?: false
        }

    suspend fun setTheme(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = themeId
        }
    }

    suspend fun setLanguage(lang: String) {
        try {
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("language", lang)
                .apply()
        } catch (_: Exception) {}
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = lang
        }
    }

    suspend fun setFontSize(size: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE] = size
        }
    }

    suspend fun setLineHeight(height: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LINE_HEIGHT] = height
        }
    }

    suspend fun setLetterSpacing(spacing: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LETTER_SPACING] = spacing
        }
    }

    suspend fun setAutosaveEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTOSAVE_ENABLED] = enabled
        }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_HASH] = if (pin.isBlank()) "" else hashPin(pin)
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        if (!enabled) {
            setAppLockPin("")
        }
    }

    suspend fun setPin(pin: String) {
        setAppLockPin(pin)
    }

    suspend fun setGridView(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GRID_VIEW] = enabled
        }
    }

    fun hashPin(pin: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            pin.hashCode().toString()
        }
    }
}
