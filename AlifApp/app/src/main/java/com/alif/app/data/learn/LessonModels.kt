package com.alif.app.data.learn

import androidx.annotation.StringRes

/** Пример: арабский текст + подпись + (необязательно) звук или ссылка на аят. */
data class Example(
    val arabic: String,
    val label: String,
    val audioKey: String? = null,
    /** Сура и аят — звук берётся из аудио Корана (обучающее чтение). */
    val surah: Int = 0,
    val ayah: Int = 0,
)

/** Страница объяснения в начале урока. */
sealed interface LessonPage {
    data class Info(
        val title: String,
        val body: String,
        val arabic: String? = null,
        val examples: List<Example> = emptyList(),
    ) : LessonPage

    data class LetterIntro(val letterId: String) : LessonPage
    data class FormsTable(val letterIds: List<String>) : LessonPage
    data class SimilarLetters(val groups: List<SimilarGroup>) : LessonPage
    data object JoinBuilder : LessonPage
    data class Grid(val title: String, val body: String, val items: List<QuizItem>) : LessonPage
}

/**
 * Единица для упражнений: что показать по-арабски и какой правильный ответ.
 * key — постоянный идентификатор (для повторения ошибок и избранного).
 */
data class QuizItem(
    val key: String,
    val arabic: String,
    /** Правильный ответ: название, транскрипция или правило. */
    val answer: String,
    val audioKey: String? = null,
    val meaning: String? = null,
    /** Слово без огласовок — для упражнения «Собери слово». */
    val build: String? = null,
    val surah: Int = 0,
    val ayah: Int = 0,
)

enum class ExerciseKind {
    /** Увидь арабское → выбери ответ (звук/название/правило). */
    SEE_CHOOSE,
    /** Прочитай ответ → найди арабское. */
    CHOOSE_ARABIC,
    /** Услышь звук → выбери арабское (только если есть аудиофайл). */
    HEAR_CHOOSE,
    /** Какой формой буква стоит в слове. */
    FIND_FORM,
    /** Собери слово из букв. */
    BUILD_WORD,
    /** Прочитай сам, затем сверь с ответом и аудио. */
    READ_CHECK,
}

data class Lesson(
    val id: String,
    val moduleId: String,
    val title: String,
    val pages: List<LessonPage>,
    val items: List<QuizItem>,
    val kinds: List<ExerciseKind>,
    /** Слова без огласовок для упражнения на формы букв. */
    val formWords: List<String> = emptyList(),
    /** Буквы, которые ищем в словах (id). Пусто — любые. */
    val formLetters: List<String> = emptyList(),
)

data class LearningModule(
    val id: String,
    val number: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    /** Арабский символ-обложка. */
    val symbol: String,
    val lessons: List<Lesson>,
)
