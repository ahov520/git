package com.antihub.mobile.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.antihub.mobile.R

enum class MainTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(labelRes = R.string.label_home, icon = Icons.Filled.Home),
    Notifications(labelRes = R.string.label_notifications, icon = Icons.Filled.Notifications),
    Search(labelRes = R.string.label_search, icon = Icons.Filled.Search),
    Profile(labelRes = R.string.label_profile, icon = Icons.Filled.Person),
}
