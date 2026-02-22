package com.antihub.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.model.RepoItem
import com.antihub.mobile.domain.usecase.GetRepositoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRepositoriesUseCase: GetRepositoriesUseCase,
) : ViewModel() {

    private val _reposState = MutableStateFlow<UiState<List<RepoItem>>>(UiState.Loading)
    val reposState: StateFlow<UiState<List<RepoItem>>> = _reposState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _reposState.value = UiState.Loading
            val result = getRepositoriesUseCase()
            _reposState.value = result.fold(
                onSuccess = { repos ->
                    if (repos.isEmpty()) UiState.Empty else UiState.Success(repos)
                },
                onFailure = {
                    UiState.Error(message = it.message ?: "仓库加载失败")
                },
            )
        }
    }
}
