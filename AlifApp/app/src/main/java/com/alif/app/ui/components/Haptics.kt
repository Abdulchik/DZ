package com.alif.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.alif.app.ui.theme.LocalAppSettings

/** Тактильный отклик с учётом настройки «Тактильный отклик». */
class Haptics internal constructor(
    private val perform: (HapticFeedbackType) -> Unit,
    private val enabled: Boolean,
) {
    /** Лёгкое касание — обычные кнопки и вкладки. */
    fun tap() { if (enabled) perform(HapticFeedbackType.TextHandleMove) }

    /** Сильный отклик — долгое нажатие, неверный ответ. */
    fun strong() { if (enabled) perform(HapticFeedbackType.LongPress) }
}

@Composable
fun rememberHaptics(): Haptics {
    val feedback = LocalHapticFeedback.current
    val enabled = LocalAppSettings.current.hapticsEnabled
    return remember(feedback, enabled) { Haptics(feedback::performHapticFeedback, enabled) }
}
