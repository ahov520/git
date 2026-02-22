package com.antihub.mobile.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.antihub.mobile.core.model.TokenState
import com.antihub.mobile.ui.screen.HomeScreen
import com.antihub.mobile.ui.screen.LoginScreen
import com.antihub.mobile.ui.screen.NotificationsScreen
import com.antihub.mobile.ui.screen.ProfileScreen
import com.antihub.mobile.ui.screen.SearchScreen
import com.antihub.mobile.ui.viewmodel.AuthViewModel

@Composable
fun AppShell(
    authViewModel: AuthViewModel,
) {
    val tokenState by authViewModel.tokenState.collectAsState()

    when (tokenState) {
        TokenState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        TokenState.Unauthenticated -> {
            LoginScreen(authViewModel = authViewModel)
        }

        is TokenState.Authenticated -> {
            AuthenticatedShell(authViewModel = authViewModel)
        }
    }
}

@Composable
private fun AuthenticatedShell(
    authViewModel: AuthViewModel,
) {
    var currentTab by rememberSaveable { mutableStateOf(MainTab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(text = stringResource(id = tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                MainTab.Home -> HomeScreen(viewModel = hiltViewModel())
                MainTab.Notifications -> NotificationsScreen(viewModel = hiltViewModel())
                MainTab.Search -> SearchScreen(viewModel = hiltViewModel())
                MainTab.Profile -> ProfileScreen(
                    authViewModel = authViewModel,
                    profileViewModel = hiltViewModel(),
                    translationViewModel = hiltViewModel(),
                )
            }
        }
    }
}
