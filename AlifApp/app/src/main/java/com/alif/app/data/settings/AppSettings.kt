package com.alif.app.data.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Все пользовательские настройки приложения. Хранятся в DataStore. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Базовый размер арабского текста в sp. */
    val arabicFontSize: Float = DEFAULT_ARABIC_SIZE,
    val hapticsEnabled: Boolean = true,
    val soundsEnabled: Boolean = true,
    /** Папка чтеца на everyayah.com */
    val defaultReciter: String = "Alafasy_128kbps",
    // Экран чтения
    val showArabic: Boolean = true,
    val showTranslation: Boolean = true,
    val showTransliteration: Boolean = true,
    /** Стиль транслитерации: "tj" — как в таджикских изданиях (ā ӣ ҳ қ), "ru" — простая русская, "en" — латиница. */
    val translitStyle: String = "tj",
    val showTajik: Boolean = false,
    val showEnglish: Boolean = false,
    val playbackSpeed: Float = 1f,
    // Где остановился в Коране (0 — ещё не читал)
    val lastSurah: Int = 0,
    val lastAyah: Int = 0,
    // Цель и напоминание
    val dailyGoal: Int = 2,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 19,
    val reminderMinute: Int = 0,
    /** Код, который нужно набрать в калькуляторе и зажать «=», чтобы открыть приложение. */
    val unlockCode: String = DEFAULT_CODE,
    // Время намаза: место, метод расчёта
    val placeName: String = "Душанбе",
    val placeLat: Double = 38.5598,
    val placeLng: Double = 68.7870,
    /** Часовой пояс места; пусто — часовой пояс телефона (для GPS). */
    val placeZone: String = "Asia/Dushanbe",
    val calcMethod: String = "KARACHI",
    val hanafiAsr: Boolean = true,
) {
    companion object {
        const val DEFAULT_ARABIC_SIZE = 26f
        const val DEFAULT_CODE = "786"
        const val MIN_ARABIC_SIZE = 20f
        const val MAX_ARABIC_SIZE = 48f
    }
}
