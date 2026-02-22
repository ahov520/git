package com.antihub.mobile.data.repository

import com.antihub.mobile.core.config.AppConfig
import com.antihub.mobile.data.local.TranslationCacheDao
import com.antihub.mobile.data.local.TranslationCacheEntity
import com.antihub.mobile.data.remote.GoogleTranslateRequest
import com.antihub.mobile.data.remote.TranslationApiService
import com.antihub.mobile.domain.model.TranslationResult
import com.antihub.mobile.domain.repository.TranslationRepository
import java.security.MessageDigest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val translationApiService: TranslationApiService,
    private val translationCacheDao: TranslationCacheDao,
) : TranslationRepository {

    private val provider = "google-translate"

    override suspend fun translate(
        text: String,
        sourceLang: String?,
        targetLang: String,
    ): Result<TranslationResult> {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            return Result.success(
                TranslationResult(
                    translatedText = "",
                    detectedSourceLang = sourceLang,
                    provider = provider,
                ),
            )
        }

        val hash = sha256(normalized)
        val cached = translationCacheDao.getCached(
            textHash = hash,
            targetLang = targetLang,
            provider = provider,
        )
        if (cached != null) {
            return Result.success(
                TranslationResult(
                    translatedText = cached.translatedText,
                    detectedSourceLang = cached.sourceLang,
                    provider = cached.provider,
                ),
            )
        }

        return runCatching {
            val apiKey = AppConfig.translationApiKey
            require(apiKey.isNotBlank()) {
                "Google Translate API key is missing"
            }

            val response = translationApiService.translate(
                apiKey = apiKey,
                request = GoogleTranslateRequest(
                    q = listOf(normalized),
                    source = sourceLang,
                    target = targetLang,
                ),
            )

            val first = response.data.translations.firstOrNull()
                ?: error("Empty translation response")

            val result = TranslationResult(
                translatedText = first.translatedText,
                detectedSourceLang = first.detectedSourceLanguage,
                provider = provider,
            )

            translationCacheDao.upsert(
                TranslationCacheEntity(
                    textHash = hash,
                    sourceText = normalized,
                    translatedText = result.translatedText,
                    sourceLang = result.detectedSourceLang,
                    targetLang = targetLang,
                    provider = provider,
                    updatedAtEpochMillis = Instant.now().toEpochMilli(),
                ),
            )

            result
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString(separator = "") { b -> "%02x".format(b) }
    }
}
