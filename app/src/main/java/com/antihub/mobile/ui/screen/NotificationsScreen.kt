package com.antihub.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.ui.viewmodel.NotificationsViewModel

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
) {
    val state by viewModel.state.collectAsState()

    when (val current = state) {
        UiState.Loading -> FullscreenLoading()
        UiState.Empty -> FullscreenMessage(message = "暂无通知")
        is UiState.Error -> FullscreenError(message = current.message, onRetry = viewModel::refresh)
        is UiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(current.data, key = { it.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                            Text(text = item.repositoryFullName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${item.reason} · ${if (item.unread) "未读" else "已读"}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
