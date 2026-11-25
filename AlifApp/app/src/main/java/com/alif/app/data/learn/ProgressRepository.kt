package com.alif.app.data.learn

import com.alif.app.data.db.DailyActivity
import com.alif.app.data.db.LessonProgress
import com.alif.app.data.db.ProgressDao
import com.alif.app.data.db.ReviewItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ProgressRepository(private val dao: ProgressDao) {

    val completed: Flow<Map<String, LessonProgress>> = dao.lessons().map { list -> list.associateBy { it.lessonId } }

    fun dueCount(): Flow<Int> = dao.dueCount(System.currentTimeMillis())

    data class Stats(val streak: Int, val today: Int, val activeDays: Set<String>)

    val stats: Flow<Stats> = dao.activity().map { list ->
        val days = list.filter { it.lessonsDone > 0 }.map { it.day }.toSet()
        var day = LocalDate.now()
        if (day.toString() !in days) day = day.minusDays(1) // серия не прерывается, пока сегодня не закончился
        var streak = 0
        while (day.toString() in days) {
            streak++
            day = day.minusDays(1)
        }
        Stats(streak, list.firstOrNull { it.day == LocalDate.now().toString() }?.lessonsDone ?: 0, days)
    }

    /** Урок открыт, если пройдены все предыдущие. */
    fun isUnlocked(lessonId: String, done: Map<String, LessonProgress>): Boolean {
        val all = Curriculum.allLessons
        val idx = all.indexOfFirst { it.id == lessonId }
        return idx <= 0 || all.take(idx).all { it.id in done }
    }

    suspend fun completeLesson(lessonId: String, scorePercent: Int) {
        val old = dao.lesson(lessonId)
        dao.saveLesson(LessonProgress(lessonId, maxOf(scorePercent, old?.bestScore ?: 0)))
        countActivity()
    }

    suspend fun countActivity() {
        val today = LocalDate.now().toString()
        val a = dao.activityOn(today)
        dao.saveActivity(DailyActivity(today, (a?.lessonsDone ?: 0) + 1))
    }

    /** Интервалы повторения для коробок 0..5. */
    private val intervals = longArrayOf(10 * MIN, DAY, 3 * DAY, 7 * DAY, 14 * DAY, 30 * DAY)

    /** Записать ответ: ошибка — карточка попадает в повторение; верно — показывать реже. */
    suspend fun recordAnswer(key: String, correct: Boolean) {
        val now = System.currentTimeMillis()
        val old = dao.review(key)
        if (old == null && correct) return // новые верные ответы не засоряют повторение
        val box = if (correct) ((old?.box ?: 0) + 1).coerceAtMost(intervals.lastIndex) else 0
        dao.saveReview(
            ReviewItem(
                key = key,
                box = box,
                dueAt = now + intervals[box],
                wrongCount = (old?.wrongCount ?: 0) + if (correct) 0 else 1,
            )
        )
    }

    suspend fun dueItems(limit: Int = 12): List<QuizItem> =
        dao.dueReviews(System.currentTimeMillis(), limit).mapNotNull { Curriculum.itemsByKey[it.key] }

    companion object {
        private const val MIN = 60_000L
        private const val DAY = 24 * 60 * MIN
    }
}
