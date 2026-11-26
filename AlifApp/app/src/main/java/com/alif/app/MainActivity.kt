package com.alif.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alif.app.data.settings.AppSettings
import com.alif.app.ui.AlifRoot
import com.alif.app.ui.theme.AlifTheme
import com.alif.app.ui.theme.isAppInDarkTheme

// AppCompatActivity (а не ComponentActivity) нужна для смены языка интерфейса на Android 8–12.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Приложение открывается только из калькулятора (после ввода кода)
        if (savedInstanceState == null && !intent.getBooleanExtra(EXTRA_UNLOCKED, false)) {
            finish()
            return
        }
        // В списке недавних приложений не показываем содержимое экрана
        if (android.os.Build.VERSION.SDK_INT >= 33) setRecentsScreenshotEnabled(false)
        enableEdgeToEdge()

        val settingsRepository = (application as AlifApplication).container.settingsRepository

        setContent {
            val settings by settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val dark = isAppInDarkTheme(settings)

            // Цвет значков в строке состояния должен совпадать с темой приложения, а не системы
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }

            AlifTheme(settings = settings) {
                AlifRoot()
            }
        }
    }

    /** Ушли из приложения — закрываем его; при следующем открытии снова будет калькулятор. */
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) finish()
    }

    companion object {
        const val EXTRA_UNLOCKED = "unlocked"
    }
}
