package com.antihub.mobile.core.config

import com.antihub.mobile.BuildConfig

object AppConfig {
    const val githubApiBaseUrl: String = "https://api.github.com/"

    val githubClientId: String = BuildConfig.GITHUB_CLIENT_ID
    val githubRedirectScheme: String = BuildConfig.GITHUB_REDIRECT_SCHEME
    val githubRedirectHost: String = BuildConfig.GITHUB_REDIRECT_HOST
    val authProxyBaseUrl: String = normalizeBaseUrl(BuildConfig.AUTH_PROXY_BASE_URL)
    val translationApiBaseUrl: String = normalizeBaseUrl(BuildConfig.TRANSLATION_API_BASE_URL)
    val translationApiKey: String = BuildConfig.TRANSLATION_API_KEY

    private fun normalizeBaseUrl(url: String): String {
        if (url.isBlank()) {
            return "https://example.com/"
        }
        return if (url.endsWith('/')) url else "$url/"
    }

    fun oauthRedirectUri(): String = "$githubRedirectScheme://$githubRedirectHost"
}
