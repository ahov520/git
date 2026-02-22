package com.antihub.mobile.core.config

import com.antihub.mobile.BuildConfig

object AppConfig {
    const val githubApiBaseUrl: String = "https://api.github.com/"
    private const val defaultRedirectScheme = "githubmobile"
    private const val defaultRedirectHost = "auth"

    val githubClientId: String = BuildConfig.GITHUB_CLIENT_ID.trim()
    val githubRedirectScheme: String = BuildConfig.GITHUB_REDIRECT_SCHEME.trim().ifBlank { defaultRedirectScheme }
    val githubRedirectHost: String = BuildConfig.GITHUB_REDIRECT_HOST.trim().ifBlank { defaultRedirectHost }
    val authProxyBaseUrl: String = normalizeBaseUrl(BuildConfig.AUTH_PROXY_BASE_URL)
    val translationApiBaseUrl: String = normalizeBaseUrl(BuildConfig.TRANSLATION_API_BASE_URL)
    val translationApiKey: String = BuildConfig.TRANSLATION_API_KEY.trim()

    private fun normalizeBaseUrl(url: String): String {
        if (url.isBlank()) {
            return "https://example.com/"
        }
        return if (url.endsWith('/')) url else "$url/"
    }

    fun oauthRedirectUri(): String = "$githubRedirectScheme://$githubRedirectHost"

    fun oauthConfigError(): String? {
        if (githubClientId.isBlank()) {
            return "未配置 GH_OAUTH_CLIENT_ID，请在构建环境设置 GitHub OAuth Client ID"
        }
        return null
    }
}
