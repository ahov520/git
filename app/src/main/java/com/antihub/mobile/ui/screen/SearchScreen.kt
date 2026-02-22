package com.antihub.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antihub.mobile.core.model.UiState
import com.antihub.mobile.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
) {
    val query by viewModel.query.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.weight(1f),
                label = { Text("搜索仓库") },
                singleLine = true,
            )
            Button(onClick = viewModel::search) {
                Text("搜索")
            }
        }

        when (val current = state) {
            UiState.Loading -> FullscreenLoading()
            UiState.Empty -> FullscreenMessage("输入关键字后点击搜索")
            is UiState.Error -> FullscreenError(current.message, viewModel::search)
            is UiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(current.data, key = { it.id }) { repo ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(repo.fullName, style = MaterialTheme.typography.titleSmall)
                                if (!repo.description.isNullOrBlank()) {
                                    Text(repo.description, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
