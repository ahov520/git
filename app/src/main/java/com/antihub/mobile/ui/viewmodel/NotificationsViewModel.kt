package com.antihub.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.model.NotificationItem
import com.antihub.mobile.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<NotificationItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<NotificationItem>>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = gitHubRepository.getNotifications(all = false)
            _state.value = result.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) UiState.Empty else UiState.Success(list)
                },
                onFailure = { UiState.Error(message = it.message ?: "通知加载失败") },
            )
        }
    }
}
