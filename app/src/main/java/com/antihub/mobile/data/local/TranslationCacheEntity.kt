package com.antihub.mobile.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translation_cache",
    indices = [Index(value = ["textHash", "targetLang", "provider"], unique = true)],
)
data class TranslationCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val textHash: String,
    val sourceText: String,
    val translatedText: String,
    val sourceLang: String?,
    val targetLang: String,
    val provider: String,
    val updatedAtEpochMillis: Long,
)
