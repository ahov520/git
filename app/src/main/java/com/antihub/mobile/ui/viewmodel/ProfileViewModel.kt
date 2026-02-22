package com.antihub.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.model.UserProfile
import com.antihub.mobile.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<UserProfile>>(UiState.Loading)
    val state: StateFlow<UiState<UserProfile>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = gitHubRepository.getViewer()
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(message = it.message ?: "用户信息加载失败") },
            )
        }
    }
}
