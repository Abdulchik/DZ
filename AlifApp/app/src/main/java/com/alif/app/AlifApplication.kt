package com.alif.app

import android.app.Application

class AlifApplication : Application() {
    /** Простейший «контейнер зависимостей»: все репозитории создаются здесь один раз. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
