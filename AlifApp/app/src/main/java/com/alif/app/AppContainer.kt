package com.alif.app

import android.content.Context
import com.alif.app.data.audio.AssetAudio
import com.alif.app.data.audio.QuranPlayer
import com.alif.app.data.audio.UiSounds
import com.alif.app.data.db.AppDatabase
import com.alif.app.data.learn.ProgressRepository
import com.alif.app.data.quran.QuranRepository
import com.alif.app.data.settings.SettingsRepository

/** Ручное внедрение зависимостей: всё создаётся один раз на всё приложение. */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    val settingsRepository by lazy { SettingsRepository(appContext) }
    val database by lazy { AppDatabase.create(appContext) }
    val quranRepository by lazy { QuranRepository(appContext) }
    val progressRepository by lazy { ProgressRepository(database.progress()) }
    val assetAudio by lazy { AssetAudio(appContext) }
    val uiSounds by lazy { UiSounds(appContext) }
    val quranPlayer by lazy { QuranPlayer(appContext) }
}
