package com.alif.app.data.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Все пользовательские настройки приложения. Хранятся в DataStore. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Базовый размер арабского текста в sp. */
    val arabicFontSize: Float = DEFAULT_ARABIC_SIZE,
    val hapticsEnabled: Boolean = true,
    val soundsEnabled: Boolean = true,
    /** Идентификатор чтеца (папка на everyayah.com), выбирается на этапе 6. */
    val defaultReciter: String = "Alafasy_128kbps",
) {
    companion object {
        const val DEFAULT_ARABIC_SIZE = 30f
        const val MIN_ARABIC_SIZE = 20f
        const val MAX_ARABIC_SIZE = 48f
    }
}
