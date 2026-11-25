package com.alif.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteAyah::class, FavoriteSurah::class, FavoriteItem::class, AyahNote::class,
        LessonProgress::class, ReviewItem::class, DailyActivity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favorites(): FavoritesDao
    abstract fun notes(): NotesDao
    abstract fun progress(): ProgressDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "alif.db").build()
    }
}
