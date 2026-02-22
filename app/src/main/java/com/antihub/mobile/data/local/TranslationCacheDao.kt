package com.antihub.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TranslationCacheDao {
    @Query(
        """
        SELECT * FROM translation_cache
        WHERE textHash = :textHash AND targetLang = :targetLang AND provider = :provider
        LIMIT 1
        """,
    )
    suspend fun getCached(
        textHash: String,
        targetLang: String,
        provider: String,
    ): TranslationCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TranslationCacheEntity)
}
