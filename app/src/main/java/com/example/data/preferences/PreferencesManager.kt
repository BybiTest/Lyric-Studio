package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
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
        val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val KEY_PIN_HASH = stringPreferencesKey("app_lock_pin_hash")
        val KEY_GRID_VIEW = booleanPreferencesKey("grid_view_enabled")
        private const val SALT = "LyricStudio_Secure_Salt_2026"
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

    val appLockPinHashFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val hash = preferences[KEY_PIN_HASH]
            if (hash.isNullOrBlank()) null else hash
        }

    val pinHashFlow: Flow<String> = appLockPinHashFlow.map { it ?: "" }

    val appLockEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val isEnabled = preferences[KEY_APP_LOCK_ENABLED] ?: false
            val hash = preferences[KEY_PIN_HASH] ?: ""
            isEnabled && hash.isNotBlank()
        }

    val gridViewFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_GRID_VIEW] ?: false
        }

    fun isAppLockEnabledSync(): Boolean {
        return try {
            val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isEnabled = sp.getBoolean("app_lock_enabled", false)
            val hash = sp.getString("app_lock_pin_hash", null)
            isEnabled && !hash.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    fun getPinHashSync(): String? {
        return try {
            val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val hash = sp.getString("app_lock_pin_hash", null)
            if (hash.isNullOrBlank()) null else hash
        } catch (_: Exception) {
            null
        }
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
        val hash = if (pin.isBlank()) "" else hashPin(pin)
        val isEnabled = pin.isNotBlank()
        try {
            val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sp.edit()
                .putString("app_lock_pin_hash", hash)
                .putBoolean("app_lock_enabled", isEnabled)
                .apply()
        } catch (_: Exception) {}
        context.dataStore.edit { preferences ->
            preferences[KEY_PIN_HASH] = hash
            preferences[KEY_APP_LOCK_ENABLED] = isEnabled
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        try {
            val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val editor = sp.edit().putBoolean("app_lock_enabled", enabled)
            if (!enabled) {
                editor.remove("app_lock_pin_hash")
            }
            editor.apply()
        } catch (_: Exception) {}
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
            if (!enabled) {
                preferences[KEY_PIN_HASH] = ""
            }
        }
    }

    suspend fun setPin(pin: String) {
        setAppLockPin(pin)
    }

    fun verifyPin(pin: String): Boolean {
        val savedHash = getPinHashSync() ?: return false
        val hashedWithSalt = hashPin(pin)
        val hashedLegacy = hashPinLegacy(pin)
        return hashedWithSalt == savedHash || hashedLegacy == savedHash
    }

    suspend fun verifyPinAsync(pin: String): Boolean {
        var savedHash = getPinHashSync()
        if (savedHash == null) {
            context.dataStore.data.firstOrNull()?.let { prefs ->
                savedHash = prefs[KEY_PIN_HASH]
            }
        }
        if (savedHash.isNullOrBlank()) return false
        return hashPin(pin) == savedHash || hashPinLegacy(pin) == savedHash
    }

    suspend fun setGridView(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GRID_VIEW] = enabled
        }
    }

    fun hashPin(pin: String, salt: String = SALT): String {
        return try {
            val message = "$salt:$pin"
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(message.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            (pin.hashCode() xor salt.hashCode()).toString()
        }
    }

    private fun hashPinLegacy(pin: String): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            pin.hashCode().toString()
        }
    }
}
