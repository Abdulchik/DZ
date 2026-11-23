package com.alif.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Языки интерфейса. Чтобы включить таджикский, поставьте available = true
 * после того, как появится res/values-tg/strings.xml.
 */
enum class AppLanguage(val tag: String, val nativeName: String, val available: Boolean) {
    RUSSIAN("ru", "Русский", true),
    TAJIK("tg", "Тоҷикӣ", false);

    companion object {
        fun current(): AppLanguage {
            val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return entries.firstOrNull { tag.startsWith(it.tag) } ?: RUSSIAN
        }

        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
}
