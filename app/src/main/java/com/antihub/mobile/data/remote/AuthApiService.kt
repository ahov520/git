package com.antihub.mobile.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("oauth/github/exchange")
    suspend fun exchangeCode(@Body request: ExchangeCodeRequest): AuthTokenResponse

    @POST("oauth/github/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthTokenResponse
}

@Serializable
data class ExchangeCodeRequest(
    @SerialName("code") val code: String,
    @SerialName("code_verifier") val codeVerifier: String,
    @SerialName("redirect_uri") val redirectUri: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class AuthTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Long? = null,
)
