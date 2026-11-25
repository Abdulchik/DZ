package com.alif.app.ui.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alif.app.AppContainer
import com.alif.app.data.db.FavoriteItem
import com.alif.app.data.learn.Curriculum
import com.alif.app.data.learn.Example
import com.alif.app.data.learn.Exercise
import com.alif.app.data.learn.ExerciseGenerator
import com.alif.app.data.learn.LessonPage
import com.alif.app.data.learn.LetterForm
import com.alif.app.data.learn.QuizItem
import com.alif.app.data.settings.AppSettings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Phase { PAGES, LISTEN, PRACTICE, TEST, RESULT }

/** Результат ответа на текущее упражнение. */
data class Answer(val correct: Boolean, val chosen: String? = null)

data class LessonUiState(
    val title: String = "",
    val isReview: Boolean = false,
    val phase: Phase = Phase.PAGES,
    val pages: List<LessonPage> = emptyList(),
    val pageIndex: Int = 0,
    val listenItems: List<QuizItem> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val index: Int = 0,
    val answer: Answer? = null,
    /** Для «Прочитай и сверь»: ответ показан. */
    val revealed: Boolean = false,
    val testCorrect: Int = 0,
    val testTotal: Int = 0,
    val mistakes: Int = 0,
    val passed: Boolean = false,
) {
    val current: Exercise? get() = exercises.getOrNull(index)

    /** Общий прогресс урока 0..1 для полоски сверху. */
    val progress: Float
        get() = when (phase) {
            Phase.PAGES -> 0.25f * (pageIndex + 1) / pages.size.coerceAtLeast(1)
            Phase.LISTEN -> 0.3f
            Phase.PRACTICE -> 0.3f + 0.4f * index / exercises.size.coerceAtLeast(1)
            Phase.TEST -> 0.7f + 0.3f * index / exercises.size.coerceAtLeast(1)
            Phase.RESULT -> 1f
        }
}

class LessonViewModel(private val c: AppContainer, lessonId: String) : ViewModel() {

    private val lesson = Curriculum.lesson(lessonId)
    private val isReview = lessonId == REVIEW_ID

    private val _state = MutableStateFlow(LessonUiState())
    val state: StateFlow<LessonUiState> = _state.asStateFlow()

    /** Сообщения для «тоста» внизу экрана. */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    val favoriteKeys: StateFlow<Set<String>> = c.database.favorites().items()
        .map { list -> list.map { it.key }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val settings = c.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var practiceRequeued = mutableSetOf<String>()

    init {
        if (isReview) {
            viewModelScope.launch {
                val items = c.progressRepository.dueItems()
                val ex = ExerciseGenerator.review(items, ::hasAudio)
                _state.value = LessonUiState(
                    title = "Повторение ошибок", isReview = true,
                    phase = if (ex.isEmpty()) Phase.RESULT else Phase.PRACTICE,
                    exercises = ex, passed = true,
                )
            }
        } else if (lesson != null) {
            _state.value = LessonUiState(
                title = lesson.title,
                pages = lesson.pages,
                listenItems = lesson.items.filter { it.audioKey != null || it.surah > 0 }.distinctBy { it.key }.take(30),
            )
        }
    }

    // ---------- Звук ----------
    fun hasAudio(item: QuizItem): Boolean = item.audioKey?.let(c.assetAudio::exists) ?: false

    fun play(item: QuizItem) = play(item.audioKey, item.surah, item.ayah, item.answer)

    fun play(example: Example) = play(example.audioKey, example.surah, example.ayah, example.arabic)

    fun playLetter(letterId: String) = play("letters/$letterId", 0, 0, letterId)

    private fun play(audioKey: String?, surah: Int, ayah: Int, label: String) {
        c.quranPlayer.stop()
        when {
            surah > 0 -> c.quranPlayer.play(TEACHING_RECITER, surah, ayah, ayah)
            audioKey != null && c.assetAudio.play(audioKey) -> Unit
            else -> _messages.trySend("Аудио ещё не добавлено: ${audioKey ?: label}")
        }
    }

    fun toggleFavorite(key: String, arabic: String, title: String, subtitle: String) = viewModelScope.launch {
        val dao = c.database.favorites()
        if (key in favoriteKeys.value) dao.removeItem(key) else dao.addItem(FavoriteItem(key, arabic, title, subtitle))
    }

    // ---------- Переходы ----------
    fun nextPage() = _state.update { s ->
        if (s.pageIndex < s.pages.lastIndex) s.copy(pageIndex = s.pageIndex + 1)
        else if (s.listenItems.isNotEmpty()) s.copy(phase = Phase.LISTEN)
        else startPractice(s)
    }

    fun prevPage() = _state.update { s -> if (s.pageIndex > 0) s.copy(pageIndex = s.pageIndex - 1) else s }

    fun finishListening() = _state.update(::startPractice)

    private fun startPractice(s: LessonUiState): LessonUiState {
        val l = lesson ?: return s
        practiceRequeued.clear()
        val ex = ExerciseGenerator.generate(l, PRACTICE_COUNT, ::hasAudio)
        return s.copy(phase = Phase.PRACTICE, exercises = ex, index = 0, answer = null, revealed = false, mistakes = 0)
    }

    private fun startTest(s: LessonUiState): LessonUiState {
        val l = lesson ?: return s
        val ex = ExerciseGenerator.generate(l, TEST_COUNT, ::hasAudio)
        return s.copy(phase = Phase.TEST, exercises = ex, index = 0, answer = null, revealed = false, testCorrect = 0, testTotal = ex.size)
    }

    fun retryTest() = _state.update(::startTest)

    // ---------- Ответы ----------
    fun answerChoice(option: QuizItem) {
        val ex = _state.value.current as? Exercise.Choice ?: return
        submit(option.key == ex.item.key || option.answer == ex.item.answer && option.arabic == ex.item.arabic, option.key)
    }

    fun answerForm(form: LetterForm) {
        val ex = _state.value.current as? Exercise.FindForm ?: return
        submit(form == ex.answer, form.name)
    }

    fun answerBuild(letters: List<String>) {
        val ex = _state.value.current as? Exercise.Build ?: return
        submit(letters == ex.target, letters.joinToString(""))
    }

    fun reveal() {
        val ex = _state.value.current as? Exercise.Read ?: return
        _state.update { it.copy(revealed = true) }
        play(ex.item)
    }

    fun selfAssess(correct: Boolean) = submit(correct, null)

    private fun submit(correct: Boolean, chosen: String?) {
        val s = _state.value
        if (s.answer != null) return
        val ex = s.current ?: return
        if (settings.value.soundsEnabled) if (correct) c.uiSounds.correct() else c.uiSounds.wrong()

        viewModelScope.launch { c.progressRepository.recordAnswer(ex.item.key, correct) }

        var exercises = s.exercises
        // В практике ошибочное упражнение повторяется в конце (один раз)
        if (!correct && s.phase == Phase.PRACTICE && practiceRequeued.add(ex.item.key)) {
            exercises = exercises + ex
        }
        _state.value = s.copy(
            answer = Answer(correct, chosen),
            exercises = exercises,
            mistakes = s.mistakes + if (correct) 0 else 1,
            testCorrect = s.testCorrect + if (correct && s.phase == Phase.TEST) 1 else 0,
        )
    }

    fun next() {
        val s = _state.value
        if (s.index < s.exercises.lastIndex) {
            _state.value = s.copy(index = s.index + 1, answer = null, revealed = false)
            return
        }
        when (s.phase) {
            Phase.PRACTICE -> if (s.isReview) finish(s, passed = true) else _state.value = startTest(s)
            Phase.TEST -> finish(s, passed = s.testCorrect * 100 >= PASS_PERCENT * s.testTotal)
            else -> Unit
        }
    }

    private fun finish(s: LessonUiState, passed: Boolean) {
        _state.value = s.copy(phase = Phase.RESULT, passed = passed, answer = null)
        viewModelScope.launch {
            if (passed) {
                if (settings.value.soundsEnabled) c.uiSounds.complete()
                if (lesson != null) {
                    val score = if (s.testTotal == 0) 100 else s.testCorrect * 100 / s.testTotal
                    c.progressRepository.completeLesson(lesson.id, score)
                } else {
                    c.progressRepository.countActivity()
                }
            }
        }
    }

    override fun onCleared() {
        c.assetAudio.stop()
        super.onCleared()
    }

    companion object {
        const val REVIEW_ID = "review"
        const val PRACTICE_COUNT = 8
        const val TEST_COUNT = 5
        const val PASS_PERCENT = 80
        /** Обучающее чтение Аль-Хусари — медленное, удобно для повторения. */
        const val TEACHING_RECITER = "Husary_Muallim_128kbps"
    }
}
