package com.antihub.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.domain.model.RepoItem
import com.antihub.mobile.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    val state by viewModel.reposState.collectAsState()

    when (val current = state) {
        UiState.Loading -> FullscreenLoading()
        UiState.Empty -> FullscreenMessage(message = "暂无仓库")
        is UiState.Error -> FullscreenError(message = current.message, onRetry = viewModel::refresh)
        is UiState.Success -> RepoList(repos = current.data)
    }
}

@Composable
private fun RepoList(repos: List<RepoItem>) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = repos, key = { it.id }) { repo ->
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = repo.fullName, style = MaterialTheme.typography.titleMedium)
                    if (!repo.description.isNullOrBlank()) {
                        Text(
                            text = repo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Text(
                        text = "⭐ ${repo.stargazersCount} · Owner: ${repo.ownerLogin}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    TextButton(
                        onClick = { uriHandler.openUri("https://github.com/${repo.fullName}") },
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text("网页回退打开")
                    }
                }
            }
        }
    }
}

@Composable
internal fun FullscreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun FullscreenMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
internal fun FullscreenError(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text(text = "重试")
            }
        }
    }
}
