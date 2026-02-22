package com.antihub.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.repository.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TranslationViewModel @Inject constructor(
    private val translationRepository: TranslationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<String>>(UiState.Empty)
    val state: StateFlow<UiState<String>> = _state.asStateFlow()

    fun translate(text: String, sourceLang: String? = null, targetLang: String = "zh-CN") {
        val input = text.trim()
        if (input.isBlank()) {
            _state.value = UiState.Empty
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = translationRepository.translate(
                text = input,
                sourceLang = sourceLang,
                targetLang = targetLang,
            )
            _state.value = result.fold(
                onSuccess = { UiState.Success(it.translatedText) },
                onFailure = { UiState.Error(message = it.message ?: "翻译失败") },
            )
        }
    }
}
