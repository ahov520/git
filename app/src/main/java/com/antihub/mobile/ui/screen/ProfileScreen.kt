package com.antihub.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.ui.viewmodel.AuthViewModel
import com.antihub.mobile.ui.viewmodel.ProfileViewModel
import com.antihub.mobile.ui.viewmodel.TranslationViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    translationViewModel: TranslationViewModel,
) {
    val profileState by profileViewModel.state.collectAsState()
    val translationState by translationViewModel.state.collectAsState()
    val featureFlags by authViewModel.featureFlags.collectAsState()

    var sourceText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val current = profileState) {
            UiState.Loading -> Text("加载用户信息中...")
            UiState.Empty -> Text("暂无用户信息")
            is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                Text(current.data.login, style = MaterialTheme.typography.headlineSmall)
                if (!current.data.bio.isNullOrBlank()) {
                    Text(current.data.bio, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("自动翻译")
            Switch(
                checked = featureFlags.autoTranslateEnabled,
                onCheckedChange = authViewModel::setAutoTranslate,
            )
        }

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("启用 WebView 回退")
            Switch(
                checked = featureFlags.webviewFallbackEnabled,
                onCheckedChange = authViewModel::setWebviewFallback,
            )
        }

        OutlinedTextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("输入要翻译的文本") },
        )

        Button(
            onClick = { translationViewModel.translate(sourceText) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("翻译为中文")
        }

        when (val current = translationState) {
            UiState.Loading -> Text("翻译中...")
            UiState.Empty -> Text("翻译结果将显示在这里")
            is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> Text(current.data, style = MaterialTheme.typography.bodyLarge)
        }

        Button(onClick = authViewModel::logout, modifier = Modifier.fillMaxWidth()) {
            Text("退出登录")
        }
    }
}
