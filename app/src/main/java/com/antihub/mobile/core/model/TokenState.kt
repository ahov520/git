package com.antihub.mobile.core.model

sealed interface TokenState {
    data object Loading : TokenState
    data object Unauthenticated : TokenState
    data class Authenticated(
        val accessToken: String,
        val expiresAtEpochSeconds: Long?,
    ) : TokenState
}
