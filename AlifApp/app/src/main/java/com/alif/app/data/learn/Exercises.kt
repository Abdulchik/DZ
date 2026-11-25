package com.alif.app.data.learn

import kotlin.random.Random

sealed interface Exercise {
    val item: QuizItem

    /** Выбор из вариантов. kind: SEE_CHOOSE, CHOOSE_ARABIC или HEAR_CHOOSE. */
    data class Choice(val kind: ExerciseKind, override val item: QuizItem, val options: List<QuizItem>) : Exercise

    /** Какой формой стоит буква [index] в слове [word]. */
    data class FindForm(override val item: QuizItem, val word: String, val index: Int, val answer: LetterForm) : Exercise

    /** Собрать слово item.build из плиток-букв. */
    data class Build(override val item: QuizItem, val target: List<String>, val tiles: List<String>) : Exercise

    /** Прочитай сам → проверь. */
    data class Read(override val item: QuizItem) : Exercise
}

object ExerciseGenerator {

    fun formAt(letters: List<Letter>, i: Int): LetterForm {
        val joinPrev = i > 0 && letters[i - 1].joinsLeft
        val joinNext = i < letters.lastIndex && letters[i].joinsLeft
        return when {
            joinPrev && joinNext -> LetterForm.MEDIAL
            joinPrev -> LetterForm.FINAL
            joinNext -> LetterForm.INITIAL
            else -> LetterForm.ISOLATED
        }
    }

    /**
     * @param hasAudio проверка, есть ли аудиофайл (упражнение «услышь звук» только при наличии записи)
     */
    fun generate(
        lesson: Lesson,
        count: Int,
        hasAudio: (QuizItem) -> Boolean,
        random: Random = Random.Default,
        focus: List<QuizItem> = emptyList(),
    ): List<Exercise> {
        val result = ArrayList<Exercise>()
        // Сначала каждый элемент урока хотя бы раз (в случайном порядке), потом — случайные
        val queue = ArrayDeque((focus + lesson.items.shuffled(random)).distinctBy { it.key })
        var guard = 0
        while (result.size < count && guard++ < count * 10) {
            val item = queue.removeFirstOrNull() ?: lesson.items.random(random)
            make(lesson, item, hasAudio, random, avoidKind = (result.lastOrNull() as? Exercise.Choice)?.kind)
                ?.let(result::add)
        }
        return result
    }

    private fun make(
        lesson: Lesson,
        item: QuizItem,
        hasAudio: (QuizItem) -> Boolean,
        random: Random,
        avoidKind: ExerciseKind?,
    ): Exercise? {
        val kinds = lesson.kinds.filter { k ->
            when (k) {
                ExerciseKind.HEAR_CHOOSE -> hasAudio(item)
                ExerciseKind.BUILD_WORD -> (item.build?.length ?: 0) >= 2
                ExerciseKind.FIND_FORM -> lesson.formWords.isNotEmpty()
                else -> true
            }
        }.ifEmpty { listOf(ExerciseKind.SEE_CHOOSE) }
        val kind = (kinds.filter { it != avoidKind }.ifEmpty { kinds }).random(random)
        return when (kind) {
            ExerciseKind.SEE_CHOOSE, ExerciseKind.CHOOSE_ARABIC, ExerciseKind.HEAR_CHOOSE -> choice(lesson, item, kind, random)
            ExerciseKind.READ_CHECK -> Exercise.Read(item)
            ExerciseKind.BUILD_WORD -> build(item, random)
            ExerciseKind.FIND_FORM -> findForm(lesson, random) ?: choice(lesson, item, ExerciseKind.SEE_CHOOSE, random)
        }
    }

    fun choice(lesson: Lesson?, item: QuizItem, kind: ExerciseKind, random: Random): Exercise.Choice? {
        val prefix = item.key.substringBefore(':')
        val local = lesson?.items.orEmpty().filter { it.key.substringBefore(':') == prefix }
        val pool = (local.shuffled(random) + Curriculum.sameKind(item).shuffled(random))
        val options = ArrayList<QuizItem>().apply { add(item) }
        for (o in pool) {
            if (options.size >= 4) break
            // Варианты должны отличаться и ответом, и написанием
            if (options.none { it.answer == o.answer || it.arabic == o.arabic }) options.add(o)
        }
        if (options.size < 2) return null
        return Exercise.Choice(kind, item, options.shuffled(random))
    }

    private fun build(item: QuizItem, random: Random): Exercise.Build {
        val target = item.build!!.map { it.toString() }
        val extra = Alphabet.letters.map { it.char }.filter { it !in target }.shuffled(random).take(2)
        return Exercise.Build(item, target, (target + extra).shuffled(random))
    }

    private fun findForm(lesson: Lesson, random: Random): Exercise.FindForm? {
        val wanted = lesson.formLetters.toSet()
        val candidates = lesson.formWords.flatMap { word ->
            val ls = Alphabet.lettersOf(word)
            if (ls.size != word.length) return@flatMap emptyList()
            ls.indices.filter { wanted.isEmpty() || ls[it].id in wanted }.map { Triple(word, it, ls) }
        }
        val (word, index, ls) = candidates.randomOrNull(random) ?: return null
        return Exercise.FindForm(Curriculum.letterItem(ls[index]), word, index, formAt(ls, index))
    }

    /** Упражнения для повторения ошибок. */
    fun review(items: List<QuizItem>, hasAudio: (QuizItem) -> Boolean, random: Random = Random.Default): List<Exercise> =
        items.mapNotNull { item ->
            val isText = item.key.startsWith("word:") || item.key.startsWith("ayah:") || item.key.startsWith("read:")
            val kinds = buildList {
                add(ExerciseKind.SEE_CHOOSE)
                add(ExerciseKind.CHOOSE_ARABIC)
                if (hasAudio(item)) add(ExerciseKind.HEAR_CHOOSE)
            }
            if (isText && random.nextBoolean()) Exercise.Read(item)
            else choice(null, item, kinds.random(random), random) ?: Exercise.Read(item)
        }
}
