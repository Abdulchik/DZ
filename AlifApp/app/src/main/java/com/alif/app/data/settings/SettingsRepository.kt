package com.alif.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val ARABIC_SIZE = floatPreferencesKey("arabic_font_size")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val SOUNDS = booleanPreferencesKey("sounds_enabled")
        val RECITER = stringPreferencesKey("default_reciter")
        val SHOW_ARABIC = booleanPreferencesKey("show_arabic")
        val SHOW_TRANSLATION = booleanPreferencesKey("show_translation")
        val SHOW_TRANSLIT = booleanPreferencesKey("show_translit")
        val SHOW_TG = booleanPreferencesKey("show_tajik")
        val TRANSLIT_STYLE = stringPreferencesKey("translit_style")
        val SHOW_EN = booleanPreferencesKey("show_english")
        val SPEED = floatPreferencesKey("playback_speed")
        val LAST_SURAH = intPreferencesKey("last_surah")
        val LAST_AYAH = intPreferencesKey("last_ayah")
        val GOAL = intPreferencesKey("daily_goal")
        val REMINDER = booleanPreferencesKey("reminder_enabled")
        val REMINDER_H = intPreferencesKey("reminder_hour")
        val REMINDER_M = intPreferencesKey("reminder_minute")
        val UNLOCK_CODE = stringPreferencesKey("unlock_code")
        val PLACE_NAME = stringPreferencesKey("place_name")
        val PLACE_LAT = stringPreferencesKey("place_lat")
        val PLACE_LNG = stringPreferencesKey("place_lng")
        val PLACE_ZONE = stringPreferencesKey("place_zone")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val HANAFI_ASR = booleanPreferencesKey("hanafi_asr")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        val d = AppSettings()
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: d.themeMode,
            arabicFontSize = (p[Keys.ARABIC_SIZE] ?: d.arabicFontSize)
                .coerceIn(AppSettings.MIN_ARABIC_SIZE, AppSettings.MAX_ARABIC_SIZE),
            hapticsEnabled = p[Keys.HAPTICS] ?: d.hapticsEnabled,
            soundsEnabled = p[Keys.SOUNDS] ?: d.soundsEnabled,
            defaultReciter = p[Keys.RECITER] ?: d.defaultReciter,
            showArabic = p[Keys.SHOW_ARABIC] ?: d.showArabic,
            showTranslation = p[Keys.SHOW_TRANSLATION] ?: d.showTranslation,
            showTransliteration = p[Keys.SHOW_TRANSLIT] ?: d.showTransliteration,
            showTajik = p[Keys.SHOW_TG] ?: d.showTajik,
            translitStyle = p[Keys.TRANSLIT_STYLE] ?: d.translitStyle,
            showEnglish = p[Keys.SHOW_EN] ?: d.showEnglish,
            playbackSpeed = p[Keys.SPEED] ?: d.playbackSpeed,
            lastSurah = p[Keys.LAST_SURAH] ?: d.lastSurah,
            lastAyah = p[Keys.LAST_AYAH] ?: d.lastAyah,
            dailyGoal = p[Keys.GOAL] ?: d.dailyGoal,
            reminderEnabled = p[Keys.REMINDER] ?: d.reminderEnabled,
            reminderHour = p[Keys.REMINDER_H] ?: d.reminderHour,
            reminderMinute = p[Keys.REMINDER_M] ?: d.reminderMinute,
            unlockCode = p[Keys.UNLOCK_CODE] ?: d.unlockCode,
            placeName = p[Keys.PLACE_NAME] ?: d.placeName,
            placeLat = p[Keys.PLACE_LAT]?.toDoubleOrNull() ?: d.placeLat,
            placeLng = p[Keys.PLACE_LNG]?.toDoubleOrNull() ?: d.placeLng,
            placeZone = p[Keys.PLACE_ZONE] ?: d.placeZone,
            calcMethod = p[Keys.CALC_METHOD] ?: d.calcMethod,
            hanafiAsr = p[Keys.HANAFI_ASR] ?: d.hanafiAsr,
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setArabicFontSize(size: Float) = context.dataStore.edit { it[Keys.ARABIC_SIZE] = size }
    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setSounds(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUNDS] = enabled }
    suspend fun setDefaultReciter(id: String) = context.dataStore.edit { it[Keys.RECITER] = id }
    suspend fun setTranslitStyle(v: String) = context.dataStore.edit { it[Keys.TRANSLIT_STYLE] = v }
    suspend fun setShowTajik(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_TG] = v }
    suspend fun setShowEnglish(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_EN] = v }
    suspend fun setShowArabic(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_ARABIC] = v }
    suspend fun setShowTranslation(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_TRANSLATION] = v }
    suspend fun setShowTransliteration(v: Boolean) = context.dataStore.edit { it[Keys.SHOW_TRANSLIT] = v }
    suspend fun setPlaybackSpeed(v: Float) = context.dataStore.edit { it[Keys.SPEED] = v }
    suspend fun setLastRead(surah: Int, ayah: Int) = context.dataStore.edit {
        it[Keys.LAST_SURAH] = surah
        it[Keys.LAST_AYAH] = ayah
    }
    suspend fun setPlace(name: String, lat: Double, lng: Double, zone: String) = context.dataStore.edit {
        it[Keys.PLACE_NAME] = name
        it[Keys.PLACE_LAT] = lat.toString()
        it[Keys.PLACE_LNG] = lng.toString()
        it[Keys.PLACE_ZONE] = zone
    }
    suspend fun setCalcMethod(m: String) = context.dataStore.edit { it[Keys.CALC_METHOD] = m }
    suspend fun setHanafiAsr(v: Boolean) = context.dataStore.edit { it[Keys.HANAFI_ASR] = v }
    suspend fun setUnlockCode(code: String) = context.dataStore.edit { it[Keys.UNLOCK_CODE] = code }
    suspend fun setDailyGoal(v: Int) = context.dataStore.edit { it[Keys.GOAL] = v }
    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) = context.dataStore.edit {
        it[Keys.REMINDER] = enabled
        it[Keys.REMINDER_H] = hour
        it[Keys.REMINDER_M] = minute
    }
}
