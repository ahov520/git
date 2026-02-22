package com.antihub.mobile.ui.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.TokenState
import com.antihub.mobile.data.auth.AuthManager
import com.antihub.mobile.data.local.FeatureFlagStore
import com.antihub.mobile.data.local.FeatureFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val featureFlagStore: FeatureFlagStore,
) : ViewModel() {

    val tokenState: StateFlow<TokenState> = authManager.getTokenState()

    val featureFlags: StateFlow<FeatureFlags> = featureFlagStore.flags.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeatureFlags(
            webviewFallbackEnabled = true,
            autoTranslateEnabled = false,
        ),
    )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.refreshIfNeeded()
        }
    }

    fun login(activity: Activity) {
        _authError.value = null
        authManager.login(activity).onFailure {
            _authError.value = it.message ?: "无法打开浏览器，请检查系统浏览器"
        }
    }

    fun handleOAuthCallback(uri: Uri) {
        viewModelScope.launch {
            authManager.handleAuthCallback(uri).onFailure {
                _authError.value = it.message ?: "OAuth 登录失败"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authManager.logout()
        }
    }

    fun setAutoTranslate(enabled: Boolean) {
        viewModelScope.launch {
            featureFlagStore.setAutoTranslateEnabled(enabled)
        }
    }

    fun setWebviewFallback(enabled: Boolean) {
        viewModelScope.launch {
            featureFlagStore.setWebviewFallbackEnabled(enabled)
        }
    }
}
