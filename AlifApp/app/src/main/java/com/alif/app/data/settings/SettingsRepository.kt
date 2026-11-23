package com.alif.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ARABIC_SIZE = floatPreferencesKey("arabic_font_size")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SOUNDS = booleanPreferencesKey("sounds_enabled")
        val RECITER = stringPreferencesKey("default_reciter")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        val defaults = AppSettings()
        AppSettings(
            themeMode = p[Keys.THEME]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: defaults.themeMode,
            arabicFontSize = (p[Keys.ARABIC_SIZE] ?: defaults.arabicFontSize)
                .coerceIn(AppSettings.MIN_ARABIC_SIZE, AppSettings.MAX_ARABIC_SIZE),
            hapticsEnabled = p[Keys.HAPTICS] ?: defaults.hapticsEnabled,
            soundsEnabled = p[Keys.SOUNDS] ?: defaults.soundsEnabled,
            defaultReciter = p[Keys.RECITER] ?: defaults.defaultReciter,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setArabicFontSize(size: Float) = context.dataStore.edit { it[Keys.ARABIC_SIZE] = size }
    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setSounds(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUNDS] = enabled }
    suspend fun setDefaultReciter(id: String) = context.dataStore.edit { it[Keys.RECITER] = id }
}
