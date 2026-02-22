package com.antihub.mobile.core.network

import com.antihub.mobile.data.local.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val securePreferences: SecurePreferences,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = securePreferences.getAccessToken().orEmpty()
        if (token.isBlank()) {
            return chain.proceed(chain.request())
        }

        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "application/vnd.github+json")
            .build()

        return chain.proceed(newRequest)
    }
}
