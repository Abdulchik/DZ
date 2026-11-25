package com.alif.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_ayah ORDER BY addedAt DESC")
    fun ayahs(): Flow<List<FavoriteAyah>>

    @Query("SELECT * FROM favorite_ayah WHERE surah = :surah")
    fun ayahsOfSurah(surah: Int): Flow<List<FavoriteAyah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAyah(f: FavoriteAyah)

    @Query("DELETE FROM favorite_ayah WHERE surah = :surah AND ayah = :ayah")
    suspend fun removeAyah(surah: Int, ayah: Int)

    @Query("SELECT * FROM favorite_surah ORDER BY addedAt DESC")
    fun surahs(): Flow<List<FavoriteSurah>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSurah(f: FavoriteSurah)

    @Query("DELETE FROM favorite_surah WHERE surah = :surah")
    suspend fun removeSurah(surah: Int)

    @Query("SELECT * FROM favorite_item ORDER BY addedAt DESC")
    fun items(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(f: FavoriteItem)

    @Query("DELETE FROM favorite_item WHERE `key` = :key")
    suspend fun removeItem(key: String)
}

@Dao
interface NotesDao {
    @Query("SELECT * FROM ayah_note ORDER BY updatedAt DESC")
    fun all(): Flow<List<AyahNote>>

    @Query("SELECT * FROM ayah_note WHERE surah = :surah")
    fun ofSurah(surah: Int): Flow<List<AyahNote>>

    @Upsert
    suspend fun save(note: AyahNote)

    @Query("DELETE FROM ayah_note WHERE surah = :surah AND ayah = :ayah")
    suspend fun delete(surah: Int, ayah: Int)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun lessons(): Flow<List<LessonProgress>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :id")
    suspend fun lesson(id: String): LessonProgress?

    @Upsert
    suspend fun saveLesson(p: LessonProgress)

    @Query("SELECT * FROM review_item WHERE `key` = :key")
    suspend fun review(key: String): ReviewItem?

    @Query("SELECT * FROM review_item WHERE dueAt <= :now ORDER BY dueAt LIMIT :limit")
    suspend fun dueReviews(now: Long, limit: Int): List<ReviewItem>

    @Query("SELECT COUNT(*) FROM review_item WHERE dueAt <= :now")
    fun dueCount(now: Long): Flow<Int>

    @Upsert
    suspend fun saveReview(r: ReviewItem)

    @Query("SELECT * FROM daily_activity ORDER BY day DESC LIMIT 400")
    fun activity(): Flow<List<DailyActivity>>

    @Query("SELECT * FROM daily_activity WHERE day = :day")
    suspend fun activityOn(day: String): DailyActivity?

    @Upsert
    suspend fun saveActivity(a: DailyActivity)
}
