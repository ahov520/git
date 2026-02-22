package com.antihub.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslationApiService {
    @POST("language/translate/v2")
    suspend fun translate(
        @Query("key") apiKey: String,
        @Body request: GoogleTranslateRequest,
    ): GoogleTranslateResponse
}

@Serializable
data class GoogleTranslateRequest(
    @SerialName("q") val q: List<String>,
    @SerialName("target") val target: String,
    @SerialName("source") val source: String? = null,
    @SerialName("format") val format: String = "text",
)

@Serializable
data class GoogleTranslateResponse(
    @SerialName("data") val data: GoogleTranslateData,
)

@Serializable
data class GoogleTranslateData(
    @SerialName("translations") val translations: List<GoogleTranslateItem>,
)

@Serializable
data class GoogleTranslateItem(
    @SerialName("translatedText") val translatedText: String,
    @SerialName("detectedSourceLanguage") val detectedSourceLanguage: String? = null,
)
