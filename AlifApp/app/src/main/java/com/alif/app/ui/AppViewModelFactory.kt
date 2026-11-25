package com.alif.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alif.app.AlifApplication
import com.alif.app.AppContainer

/** Фабрика ViewModel с доступом к контейнеру зависимостей. */
inline fun <reified VM : ViewModel> appViewModelFactory(crossinline create: CreationExtras.(AppContainer) -> VM) =
    viewModelFactory {
        initializer {
            val app = this[APPLICATION_KEY] as AlifApplication
            create(app.container)
        }
    }
