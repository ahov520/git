package com.antihub.mobile.data.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.antihub.mobile.core.config.AppConfig
import com.antihub.mobile.core.model.TokenState
import com.antihub.mobile.data.local.SecurePreferences
import com.antihub.mobile.data.remote.AuthApiService
import com.antihub.mobile.data.remote.ExchangeCodeRequest
import com.antihub.mobile.data.remote.RefreshTokenRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface AuthManager {
    fun login(activity: Activity): Result<Unit>
    suspend fun logout()
    suspend fun refreshIfNeeded(): Boolean
    fun getTokenState(): StateFlow<TokenState>
    suspend fun handleAuthCallback(uri: Uri): Result<Unit>
}

@Singleton
class AuthManagerImpl @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val authApiService: AuthApiService,
) : AuthManager {

    private val tokenState = MutableStateFlow<TokenState>(TokenState.Loading)
    private var pendingAuthState: String? = null
    private var pendingCodeVerifier: String? = null

    init {
        hydrateTokenState()
    }

    override fun login(activity: Activity): Result<Unit> {
        val configError = AppConfig.oauthConfigError()
        if (configError != null) {
            Timber.w(configError)
            return Result.failure(IllegalStateException(configError))
        }
        val clientId = AppConfig.githubClientId

        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.codeChallenge(verifier)
        val state = PkceUtil.generateState()
        pendingCodeVerifier = verifier
        pendingAuthState = state
        securePreferences.savePendingPkce(
            state = state,
            codeVerifier = verifier,
        )

        val authUri = Uri.parse("https://github.com/login/oauth/authorize").buildUpon()
            .appendQueryParameter("client_id", clientId.ifBlank { "" })
            .appendQueryParameter("redirect_uri", AppConfig.oauthAuthorizeRedirectUri())
            .appendQueryParameter("scope", "read:user repo notifications")
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()

        val openResult = runCatching {
            val browserIntent = Intent(Intent.ACTION_VIEW, authUri).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            val chooserIntent = Intent.createChooser(browserIntent, "使用浏览器继续 GitHub 授权")
            activity.startActivity(chooserIntent)
        }

        return openResult
    }

    override suspend fun logout() {
        securePreferences.clearAuthToken()
        securePreferences.clearPendingPkce()
        tokenState.value = TokenState.Unauthenticated
    }

    override suspend fun refreshIfNeeded(): Boolean {
        val accessToken = securePreferences.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            tokenState.value = TokenState.Unauthenticated
            return false
        }

        val expiresAt = securePreferences.getExpiresAtEpochSeconds()
        if (expiresAt == null || expiresAt > Instant.now().epochSecond + 60) {
            tokenState.value = TokenState.Authenticated(accessToken, expiresAt)
            return true
        }

        val refreshToken = securePreferences.getRefreshToken().orEmpty()
        if (refreshToken.isBlank()) {
            return false
        }

        return runCatching {
            val refreshed = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
            val newExpiresAt = refreshed.expiresInSeconds?.let { Instant.now().epochSecond + it }
            securePreferences.saveAuthToken(
                accessToken = refreshed.accessToken,
                refreshToken = refreshed.refreshToken,
                expiresAtEpochSeconds = newExpiresAt,
            )
            tokenState.value = TokenState.Authenticated(refreshed.accessToken, newExpiresAt)
            true
        }.getOrElse {
            Timber.e(it, "Failed to refresh token")
            false
        }
    }

    override fun getTokenState(): StateFlow<TokenState> = tokenState.asStateFlow()

    override suspend fun handleAuthCallback(uri: Uri): Result<Unit> {
        if (uri.scheme != AppConfig.githubRedirectScheme) {
            return Result.failure(IllegalArgumentException("Invalid oauth callback uri"))
        }
        if (uri.host != AppConfig.githubRedirectHost) {
            Timber.w("OAuth callback host mismatch: expected=%s actual=%s", AppConfig.githubRedirectHost, uri.host)
        }

        val exchangeConfigError = AppConfig.oauthExchangeConfigError()
        if (exchangeConfigError != null) {
            return Result.failure(IllegalStateException(exchangeConfigError))
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        val verifier = pendingCodeVerifier ?: securePreferences.getPendingCodeVerifier()
        val expectedState = pendingAuthState ?: securePreferences.getPendingOauthState()

        if (code.isNullOrBlank() || state.isNullOrBlank() || verifier.isNullOrBlank()) {
            securePreferences.clearPendingPkce()
            return Result.failure(IllegalStateException("OAuth callback missing required params"))
        }

        if (state != expectedState) {
            securePreferences.clearPendingPkce()
            return Result.failure(IllegalStateException("OAuth state mismatch"))
        }

        return runCatching {
            val response = authApiService.exchangeCode(
                ExchangeCodeRequest(
                    code = code,
                    codeVerifier = verifier,
                    redirectUri = AppConfig.oauthAuthorizeRedirectUri(),
                ),
            )
            val expiresAt = response.expiresInSeconds?.let { Instant.now().epochSecond + it }
            securePreferences.saveAuthToken(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                expiresAtEpochSeconds = expiresAt,
            )
            pendingCodeVerifier = null
            pendingAuthState = null
            securePreferences.clearPendingPkce()
            tokenState.value = TokenState.Authenticated(response.accessToken, expiresAt)
        }
    }

    private fun hydrateTokenState() {
        val accessToken = securePreferences.getAccessToken()
        tokenState.value = if (accessToken.isNullOrBlank()) {
            TokenState.Unauthenticated
        } else {
            TokenState.Authenticated(
                accessToken = accessToken,
                expiresAtEpochSeconds = securePreferences.getExpiresAtEpochSeconds(),
            )
        }
    }
}
