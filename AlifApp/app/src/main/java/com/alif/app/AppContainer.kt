package com.alif.app

import android.content.Context
import com.alif.app.data.settings.SettingsRepository

/**
 * Ручное внедрение зависимостей (без Hilt — проще для старта).
 * На следующих этапах сюда добавятся база Room, репозитории уроков, Корана и аудио.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }
}
