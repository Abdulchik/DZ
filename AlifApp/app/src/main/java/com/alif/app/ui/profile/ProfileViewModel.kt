package com.alif.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.learn.Curriculum
import com.alif.app.data.learn.ProgressRepository
import com.alif.app.data.settings.AppSettings
import com.alif.app.data.settings.ThemeMode
import com.alif.app.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileStats(
    val streak: Int = 0,
    val lessonsDone: Int = 0,
    val totalLessons: Int = Curriculum.allLessons.size,
    val today: Int = 0,
    val activeDays: Set<String> = emptySet(),
) {
    val percent: Int get() = if (totalLessons == 0) 0 else lessonsDone * 100 / totalLessons
}

class ProfileViewModel(private val c: AppContainer) : ViewModel() {
    private val repo = c.settingsRepository

    val settings: StateFlow<AppSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val stats: StateFlow<ProfileStats> = combine(c.progressRepository.completed, c.progressRepository.stats) { done, s: ProgressRepository.Stats ->
        ProfileStats(s.streak, done.size, today = s.today, activeDays = s.activeDays)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileStats())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setArabicSize(size: Float) = viewModelScope.launch { repo.setArabicFontSize(size) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { repo.setHaptics(enabled) }
    fun setSounds(enabled: Boolean) = viewModelScope.launch { repo.setSounds(enabled) }
    fun setReciter(folder: String) = viewModelScope.launch { repo.setDefaultReciter(folder) }
    fun setGoal(n: Int) = viewModelScope.launch { repo.setDailyGoal(n) }

    fun setReminder(enabled: Boolean, hour: Int, minute: Int) = viewModelScope.launch {
        repo.setReminder(enabled, hour, minute)
        if (enabled) ReminderScheduler.schedule(c.appContext, hour, minute) else ReminderScheduler.cancel(c.appContext)
    }
}
