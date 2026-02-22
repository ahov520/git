package com.antihub.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TranslationCacheEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationCacheDao(): TranslationCacheDao
}
