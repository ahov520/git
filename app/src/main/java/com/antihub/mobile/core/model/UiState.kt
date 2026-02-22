package com.antihub.mobile.core.model

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(
        val code: Int? = null,
        val message: String,
        val retryable: Boolean = true,
    ) : UiState<Nothing>
}
