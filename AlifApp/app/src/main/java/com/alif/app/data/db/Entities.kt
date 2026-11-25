package com.alif.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_ayah", primaryKeys = ["surah", "ayah"])
data class FavoriteAyah(val surah: Int, val ayah: Int, val addedAt: Long = System.currentTimeMillis())

@Entity(tableName = "favorite_surah")
data class FavoriteSurah(@PrimaryKey val surah: Int, val addedAt: Long = System.currentTimeMillis())

/** Буква или слово из уроков. key — например "letter:ba" или "word:kitab". */
@Entity(tableName = "favorite_item")
data class FavoriteItem(
    @PrimaryKey val key: String,
    val arabic: String,
    val title: String,
    val subtitle: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "ayah_note", primaryKeys = ["surah", "ayah"])
data class AyahNote(
    val surah: Int,
    val ayah: Int,
    val text: String,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey val lessonId: String,
    val bestScore: Int,
    val completedAt: Long = System.currentTimeMillis(),
)

/**
 * Карточка интервального повторения (система Лейтнера).
 * box 0..5: чем выше, тем реже показываем. Ошибка — назад в box 0.
 */
@Entity(tableName = "review_item")
data class ReviewItem(
    @PrimaryKey val key: String,
    val box: Int,
    val dueAt: Long,
    val wrongCount: Int,
)

/** Активность за день — для серии дней и ежедневной цели. day = "2026-11-25". */
@Entity(tableName = "daily_activity")
data class DailyActivity(
    @PrimaryKey val day: String,
    val lessonsDone: Int,
)
