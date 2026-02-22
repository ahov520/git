package com.antihub.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.model.RepoItem
import com.antihub.mobile.domain.repository.GitHubRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<UiState<List<RepoItem>>>(UiState.Empty)
    val state: StateFlow<UiState<List<RepoItem>>> = _state.asStateFlow()

    fun onQueryChange(newValue: String) {
        _query.value = newValue
    }

    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) {
            _state.value = UiState.Empty
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = gitHubRepository.searchRepositories(query = q)
            _state.value = result.fold(
                onSuccess = { list -> if (list.isEmpty()) UiState.Empty else UiState.Success(list) },
                onFailure = { UiState.Error(message = it.message ?: "搜索失败") },
            )
        }
    }
}
