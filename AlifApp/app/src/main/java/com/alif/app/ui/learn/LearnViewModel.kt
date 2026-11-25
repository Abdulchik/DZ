package com.alif.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.db.LessonProgress
import com.alif.app.data.learn.Curriculum
import com.alif.app.data.learn.Lesson
import com.alif.app.data.learn.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class LearnUiState(
    val done: Map<String, LessonProgress> = emptyMap(),
    val dueReviews: Int = 0,
    val streak: Int = 0,
    val todayDone: Int = 0,
    val dailyGoal: Int = 2,
    val loaded: Boolean = false,
) {
    val nextLesson: Lesson? get() = Curriculum.allLessons.firstOrNull { it.id !in done }
    val total: Int get() = Curriculum.allLessons.size
}

class LearnViewModel(c: AppContainer) : ViewModel() {
    private val progress: ProgressRepository = c.progressRepository

    val state: StateFlow<LearnUiState> = combine(
        progress.completed, progress.dueCount(), progress.stats, c.settingsRepository.settings,
    ) { done, due, stats, settings ->
        LearnUiState(done, due, stats.streak, stats.today, settings.dailyGoal, loaded = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnUiState())

    fun isUnlocked(lessonId: String, done: Map<String, LessonProgress>) = progress.isUnlocked(lessonId, done)
}
